//==============================================================================
// nxs.desktop.training — Training engine (C++20 module)
//
// Owns the genetic search loop and all numerical work (via nxs.desktop.compute):
//   • population of MLP genomes
//   • evaluation on a held-out tail (out-of-sample fitness)
//   • elite selection, crossover, Gaussian mutation
//   • NXSW .bin checkpoints
//
// Python is used only for higher-level concerns: preparing the feature dataset
// (data acquisition / normalisation policy) and persisting study rows to
// SQLite. Widgets live in Lua / TS-XHTML.
//==============================================================================

module;

#include <algorithm>
#include <cmath>
#include <cstdint>
#include <cstdio>
#include <cstring>
#include <filesystem>
#include <fstream>
#include <random>
#include <string>
#include <utility>
#include <vector>

export module nxs.desktop.training;

import nxs.desktop.compute;
import nxs.desktop.python;

export namespace nxs::training {

struct Dataset {
    std::size_t n{0};  // rows
    std::size_t d{0};  // features per row
    std::vector<float> x;  // n * d, row-major
    std::vector<float> y;  // n
};

class TrainingEngine {
public:
    static constexpr std::size_t kPopulation = 12;
    static constexpr std::size_t kElite = 3;
    static constexpr int kEvolveEveryHours = 10;

    explicit TrainingEngine(controller::PythonEngine& python,
                            std::string modelsDir = "models")
        : m_python{python}, m_modelsDir{std::move(modelsDir)} {
        std::error_code ec;
        std::filesystem::create_directories(m_modelsDir, ec);
    }

    TrainingEngine(const TrainingEngine&) = delete;
    TrainingEngine& operator=(const TrainingEngine&) = delete;
    TrainingEngine(TrainingEngine&&) = delete;
    TrainingEngine& operator=(TrainingEngine&&) = delete;
    ~TrainingEngine() = default;

    // ── UI-facing commands ────────────────────────────────────────────────
    void start() {
        ensureDataset();
        ensurePopulation();
        m_active = true;
        m_status = "training";
        evolveOnce();  // generation 0001 immediately
    }

    void stop() {
        m_active = false;
        m_status = "paused";
    }

    void reset() {
        m_active = false;
        m_hours = 0;
        m_generation = 0;
        m_best = -1.0e9;
        m_accum = 0.0f;
        m_population.clear();
        m_status = "reset";
    }

    /// Advance the simulated-hour clock. One hour per 250 ms of wall time.
    void update(float dtSeconds) {
        if (!m_active) {
            m_accum = 0.0f;
            return;
        }
        constexpr float secondsPerHour = 0.25f;
        m_accum += dtSeconds;
        while (m_accum >= secondsPerHour && m_hours < 99'999) {
            m_accum -= secondsPerHour;
            ++m_hours;
            if (m_hours % kEvolveEveryHours == 0) {
                evolveOnce();
            }
        }
        if (m_hours >= 99'999) m_active = false;
    }

    // ── Read-only accessors for Lua/TS widgets ────────────────────────────
    [[nodiscard]] auto active() const noexcept -> bool { return m_active; }
    [[nodiscard]] auto hours() const noexcept -> int { return m_hours; }
    [[nodiscard]] auto generation() const noexcept -> int { return m_generation; }
    [[nodiscard]] auto best() const noexcept -> double { return m_best; }
    [[nodiscard]] auto status() const noexcept -> const std::string& { return m_status; }

private:
    void ensureDataset() {
        if (m_data.n > 0) return;
        const auto path = (std::filesystem::path{m_modelsDir} / "dataset.bin").string();
        if (m_python.prepareDataset() && loadDataset(path)) return;
        synthesizeDataset();
    }

    void ensurePopulation() {
        if (!m_population.empty()) return;
        m_population.reserve(kPopulation);
        for (std::size_t i = 0; i < kPopulation; ++i) {
            m_population.push_back(
                compute::initGenome(42 + i, m_data.d, {16, 8}));
        }
    }

    auto loadDataset(const std::string& path) -> bool {
        std::ifstream in(path, std::ios::binary);
        if (!in) return false;
        char magic[4]{};
        in.read(magic, 4);
        if (std::memcmp(magic, "NXSD", 4) != 0) return false;
        std::uint32_t n = 0;
        std::uint32_t d = 0;
        in.read(reinterpret_cast<char*>(&n), sizeof(n));
        in.read(reinterpret_cast<char*>(&d), sizeof(d));
        if (!in || n < 64 || d == 0) return false;
        m_data.n = n;
        m_data.d = d;
        m_data.x.resize(static_cast<std::size_t>(n) * d);
        m_data.y.resize(n);
        in.read(reinterpret_cast<char*>(m_data.x.data()),
                static_cast<std::streamsize>(m_data.x.size() * sizeof(float)));
        in.read(reinterpret_cast<char*>(m_data.y.data()),
                static_cast<std::streamsize>(m_data.y.size() * sizeof(float)));
        return static_cast<bool>(in);
    }

    void synthesizeDataset() {
        constexpr std::size_t d = 8;
        constexpr std::size_t n = 2048;
        std::mt19937 rng{42};
        std::normal_distribution<float> normal{0.0f, 1.0f};
        std::uniform_real_distribution<float> uniform{0.0f, 1.0f};

        std::vector<float> returns(n);
        bool stress = false;
        for (std::size_t i = 0; i < n; ++i) {
            if (uniform(rng) < 0.02f) stress = !stress;
            const float vol = stress ? 0.030f : 0.010f;
            const float drift = stress ? -0.0012f : 0.0004f;
            returns[i] = drift + vol * normal(rng);
        }

        const std::size_t rows = n - d - 1;
        m_data.n = rows;
        m_data.d = d;
        m_data.x.resize(rows * d);
        m_data.y.resize(rows);

        std::vector<float> mean(d, 0.0f);
        for (std::size_t i = 0; i < rows; ++i) {
            for (std::size_t j = 0; j < d; ++j) {
                const float v = returns[i + j];
                m_data.x[i * d + j] = v;
                mean[j] += v;
            }
            m_data.y[i] = returns[i + d];
        }
        for (auto& m : mean) m /= static_cast<float>(rows);

        std::vector<float> sd(d, 0.0f);
        for (std::size_t i = 0; i < rows; ++i) {
            for (std::size_t j = 0; j < d; ++j) {
                const float dv = m_data.x[i * d + j] - mean[j];
                sd[j] += dv * dv;
            }
        }
        for (std::size_t j = 0; j < d; ++j) {
            sd[j] = std::sqrt(sd[j] / static_cast<float>(rows)) + 1e-9f;
        }
        for (std::size_t i = 0; i < rows; ++i) {
            for (std::size_t j = 0; j < d; ++j) {
                m_data.x[i * d + j] = (m_data.x[i * d + j] - mean[j]) / sd[j];
            }
        }
    }

    void evolveOnce() {
        ensureDataset();
        ensurePopulation();

        const std::size_t split = m_data.n * 7 / 10;
        const std::size_t vn = m_data.n - split;
        std::vector<float> xv(m_data.x.begin() + static_cast<std::ptrdiff_t>(split * m_data.d),
                             m_data.x.end());
        std::vector<float> yv(m_data.y.begin() + static_cast<std::ptrdiff_t>(split),
                             m_data.y.end());

        std::vector<std::pair<double, std::size_t>> scored;
        scored.reserve(m_population.size());
        for (std::size_t i = 0; i < m_population.size(); ++i) {
            scored.emplace_back(
                compute::fitness(m_population[i], xv, yv, vn, m_data.d), i);
        }
        std::sort(scored.begin(), scored.end(),
                  [](const auto& a, const auto& b) { return a.first > b.first; });

        m_best = scored.front().first;
        std::vector<compute::Genome> elites;
        for (std::size_t e = 0; e < kElite && e < scored.size(); ++e) {
            elites.push_back(m_population[scored[e].second]);
        }

        const auto bestPath = (std::filesystem::path{m_modelsDir} / "best.bin").string();
        (void)compute::saveGenome(bestPath, elites.front());
        char eliteName[32];
        std::snprintf(eliteName, sizeof eliteName, "elite_%04d.bin", m_generation);
        const auto elitePath =
            (std::filesystem::path{m_modelsDir} / eliteName).string();
        (void)compute::saveGenome(elitePath, elites.front());

        std::vector<compute::Genome> next = elites;
        std::uint64_t seed = 1000 + static_cast<std::uint64_t>(m_generation);
        while (next.size() < kPopulation) {
            const auto& a = elites[seed % elites.size()];
            const auto& b = elites[(seed * 7 + 3) % elites.size()];
            auto child = compute::crossover(a, b, seed++);
            next.push_back(compute::mutate(child, seed++, 0.15f, 0.08f));
        }
        m_population = std::move(next);
        ++m_generation;

        // Higher-level persistence (SQLite) stays in Python.
        const auto stats = compute::seriesStats(m_data.y);
        const auto prop = compute::wilson(stats.hits, stats.n);
        (void)m_python.studyPersist(
            "ASSET", "1d", stats.n, stats.mean, stats.vol, stats.sharpe,
            stats.maxDrawdown, stats.hitRate, stats.skew, stats.kurtosis,
            m_generation, m_best, prop.p, prop.lo, prop.hi);

        char line[160];
        std::snprintf(line, sizeof line,
                      "gen %04d | best %+.3f | p(success) %.2f [%.2f,%.2f]",
                      m_generation, m_best, prop.p, prop.lo, prop.hi);
        m_status = line;
    }

    controller::PythonEngine& m_python;
    std::string m_modelsDir;
    Dataset m_data;
    std::vector<compute::Genome> m_population;
    bool m_active{false};
    int m_hours{0};
    int m_generation{0};
    double m_best{-1.0e9};
    float m_accum{0.0f};
    std::string m_status{"idle"};
};

}  // namespace nxs::training
