//==============================================================================
// nxs.desktop.compute — Numerical kernels (C++20 module)
//
// Owns the *compute*: MLP forward pass, fitness, genetic mutation/crossover,
// Wilson intervals, series statistics, and the NXSW weight-binary format.
//
// Higher-level orchestration (data acquisition, SQLite study persistence,
// training policy) lives in Python; widgets live in Lua / TS-XHTML.
//==============================================================================

module;

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstring>
#include <fstream>
#include <limits>
#include <optional>
#include <random>
#include <string>
#include <vector>

export module nxs.desktop.compute;

export namespace nxs::compute {

// ── Genome: a tiny MLP (dense layers, tanh hidden, linear output) ──────────
struct Layer {
    std::uint32_t in{0};
    std::uint32_t out{0};
    std::vector<float> w;  // row-major [in * out]
    std::vector<float> b;  // [out]
};

using Genome = std::vector<Layer>;

inline auto seedRng(std::uint64_t seed) -> std::mt19937 {
    return std::mt19937{static_cast<std::uint32_t>(seed)};
}

/// Xavier-scaled random genome with `hidden` widths between input and 1.
inline auto initGenome(std::uint64_t seed, std::size_t input,
                       const std::vector<std::size_t>& hidden) -> Genome {
    auto rng = seedRng(seed);
    std::normal_distribution<float> normal{0.0f, 1.0f};

    std::vector<std::uint32_t> dims;
    dims.push_back(static_cast<std::uint32_t>(input));
    for (auto h : hidden) dims.push_back(static_cast<std::uint32_t>(h));
    dims.push_back(1);

    Genome genome;
    genome.reserve(dims.size() - 1);
    for (std::size_t i = 0; i + 1 < dims.size(); ++i) {
        Layer layer;
        layer.in = dims[i];
        layer.out = dims[i + 1];
        const auto scale = 1.0f / std::sqrt(static_cast<float>(layer.in));
        layer.w.resize(static_cast<std::size_t>(layer.in) * layer.out);
        layer.b.assign(layer.out, 0.0f);
        for (auto& value : layer.w) value = normal(rng) * scale;
        genome.push_back(std::move(layer));
    }
    return genome;
}

/// Forward pass for one sample of `input.size()` features.
inline auto forward(const Genome& genome, const std::vector<float>& input)
    -> std::vector<float> {
    std::vector<float> activation = input;
    for (std::size_t li = 0; li < genome.size(); ++li) {
        const auto& layer = genome[li];
        std::vector<float> next(layer.out, 0.0f);
        for (std::uint32_t o = 0; o < layer.out; ++o) {
            float sum = layer.b[o];
            for (std::uint32_t i = 0; i < layer.in; ++i) {
                sum += activation[i] * layer.w[static_cast<std::size_t>(i) * layer.out + o];
            }
            next[o] = (li + 1 < genome.size()) ? std::tanh(sum) : sum;
        }
        activation = std::move(next);
    }
    return activation;
}

/// Out-of-sample Sharpe of a long/short signal on the provided slice.
inline auto fitness(const Genome& genome, const std::vector<float>& x,
                    const std::vector<float>& y, std::size_t n, std::size_t d)
    -> double {
    if (n < 32 || d == 0) return -1.0e9;
    double sum = 0.0;
    double sum_sq = 0.0;
    std::vector<float> row(d);
    for (std::size_t i = 0; i < n; ++i) {
        std::copy_n(x.begin() + static_cast<std::ptrdiff_t>(i * d), d, row.begin());
        const float pred = forward(genome, row)[0];
        const double signal = (pred > 0.0f) ? 1.0 : ((pred < 0.0f) ? -1.0 : 0.0);
        const double strat = signal * static_cast<double>(y[i]);
        sum += strat;
        sum_sq += strat * strat;
    }
    const double mean = sum / static_cast<double>(n);
    const double var = std::max(0.0, sum_sq / static_cast<double>(n) - mean * mean);
    const double sd = std::sqrt(var);
    if (sd < 1.0e-12) return -1.0e9;
    return mean / sd * std::sqrt(252.0);
}

/// Gaussian mutation on a random subset of weights and biases.
inline auto mutate(const Genome& genome, std::uint64_t seed, float rate,
                   float sigma) -> Genome {
    auto rng = seedRng(seed);
    std::normal_distribution<float> normal{0.0f, sigma};
    std::uniform_real_distribution<float> uniform{0.0f, 1.0f};

    Genome out = genome;
    for (auto& layer : out) {
        for (auto& value : layer.w) {
            if (uniform(rng) < rate) value += normal(rng);
        }
        for (auto& value : layer.b) {
            if (uniform(rng) < rate) value += normal(rng);
        }
    }
    return out;
}

/// Uniform per-weight crossover of two genomes with identical shapes.
inline auto crossover(const Genome& a, const Genome& b, std::uint64_t seed)
    -> Genome {
    auto rng = seedRng(seed);
    std::uniform_real_distribution<float> coin{0.0f, 1.0f};

    Genome out = a;
    const auto layers = std::min(a.size(), b.size());
    for (std::size_t li = 0; li < layers; ++li) {
        const auto count = std::min(out[li].w.size(), b[li].w.size());
        for (std::size_t i = 0; i < count; ++i) {
            if (coin(rng) < 0.5f) out[li].w[i] = b[li].w[i];
        }
        const auto bcount = std::min(out[li].b.size(), b[li].b.size());
        for (std::size_t i = 0; i < bcount; ++i) {
            if (coin(rng) < 0.5f) out[li].b[i] = b[li].b[i];
        }
    }
    return out;
}

// ── Statistics ─────────────────────────────────────────────────────────────
struct Stats {
    int n{0};
    int hits{0};
    double mean{0.0};
    double vol{0.0};
    double sharpe{0.0};
    double maxDrawdown{0.0};
    double hitRate{0.0};
    double skew{0.0};
    double kurtosis{0.0};
};

inline auto seriesStats(const std::vector<float>& returns) -> Stats {
    Stats s;
    const auto n = static_cast<int>(returns.size());
    if (n == 0) return s;

    double sum = 0.0;
    for (float r : returns) sum += r;
    s.mean = sum / n;

    double var = 0.0;
    for (float r : returns) {
        const double d = r - s.mean;
        var += d * d;
    }
    var /= std::max(1, n - 1);
    s.vol = std::sqrt(var);

    double m3 = 0.0;
    double m4 = 0.0;
    if (s.vol > 0.0) {
        for (float r : returns) {
            const double z = (r - s.mean) / s.vol;
            m3 += z * z * z;
            m4 += z * z * z * z;
            if (r > 0.0f) ++s.hits;
        }
        s.skew = m3 / n;
        s.kurtosis = m4 / n;
    } else {
        for (float r : returns) if (r > 0.0f) ++s.hits;
    }

    s.sharpe = (s.vol > 0.0) ? s.mean / s.vol * std::sqrt(252.0) : 0.0;
    s.hitRate = static_cast<double>(s.hits) / n;

    double equity = 1.0;
    double peak = 1.0;
    double worst = 0.0;
    for (float r : returns) {
        equity *= (1.0 + r);
        peak = std::max(peak, equity);
        if (peak > 0.0) worst = std::min(worst, (equity - peak) / peak);
    }
    s.maxDrawdown = worst;
    s.n = n;
    return s;
}

struct Proportion {
    double p{0.0};
    double lo{0.0};
    double hi{0.0};
};

/// Wilson score interval for a binomial proportion (95% by default).
inline auto wilson(int hits, int n, double z = 1.96) -> Proportion {
    Proportion out;
    if (n <= 0) return out;
    const double p = static_cast<double>(hits) / n;
    const double denom = 1.0 + z * z / n;
    out.p = p;
    out.lo = (p + z * z / (2.0 * n) - z * std::sqrt(p * (1.0 - p) / n + z * z / (4.0 * n * n))) / denom;
    out.hi = (p + z * z / (2.0 * n) + z * std::sqrt(p * (1.0 - p) / n + z * z / (4.0 * n * n))) / denom;
    out.lo = std::clamp(out.lo, 0.0, 1.0);
    out.hi = std::clamp(out.hi, 0.0, 1.0);
    return out;
}

// ── NXSW-v1 weight binary format ───────────────────────────────────────────
inline auto saveGenome(const std::string& path, const Genome& genome) -> bool {
    std::ofstream out(path, std::ios::binary | std::ios::trunc);
    if (!out) return false;
    out.write("NXSW", 4);
    const std::uint16_t version = 1;
    const std::uint16_t layers = static_cast<std::uint16_t>(genome.size());
    out.write(reinterpret_cast<const char*>(&version), sizeof(version));
    out.write(reinterpret_cast<const char*>(&layers), sizeof(layers));
    for (const auto& layer : genome) {
        out.write(reinterpret_cast<const char*>(&layer.in), sizeof(layer.in));
        out.write(reinterpret_cast<const char*>(&layer.out), sizeof(layer.out));
        out.write(reinterpret_cast<const char*>(layer.w.data()),
                  static_cast<std::streamsize>(layer.w.size() * sizeof(float)));
        out.write(reinterpret_cast<const char*>(layer.b.data()),
                  static_cast<std::streamsize>(layer.b.size() * sizeof(float)));
    }
    return static_cast<bool>(out);
}

inline auto loadGenome(const std::string& path) -> std::optional<Genome> {
    std::ifstream in(path, std::ios::binary);
    if (!in) return std::nullopt;
    char magic[4]{};
    in.read(magic, 4);
    if (std::memcmp(magic, "NXSW", 4) != 0) return std::nullopt;
    std::uint16_t version = 0;
    std::uint16_t layers = 0;
    in.read(reinterpret_cast<char*>(&version), sizeof(version));
    in.read(reinterpret_cast<char*>(&layers), sizeof(layers));
    if (version != 1) return std::nullopt;

    Genome genome;
    genome.reserve(layers);
    for (std::uint16_t li = 0; li < layers; ++li) {
        Layer layer;
        in.read(reinterpret_cast<char*>(&layer.in), sizeof(layer.in));
        in.read(reinterpret_cast<char*>(&layer.out), sizeof(layer.out));
        if (!in) return std::nullopt;
        layer.w.resize(static_cast<std::size_t>(layer.in) * layer.out);
        layer.b.resize(layer.out);
        in.read(reinterpret_cast<char*>(layer.w.data()),
                static_cast<std::streamsize>(layer.w.size() * sizeof(float)));
        in.read(reinterpret_cast<char*>(layer.b.data()),
                static_cast<std::streamsize>(layer.b.size() * sizeof(float)));
        if (!in) return std::nullopt;
        genome.push_back(std::move(layer));
    }
    return genome;
}

}  // namespace nxs::compute
