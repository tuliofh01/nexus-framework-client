#include "model/FunctionRegistry.hpp"

#include <algorithm>

namespace nxs::model {

FunctionRegistry::FunctionRegistry() {
    // Built-in catalog. Each entry maps 1:1 to a callable exported by
    // python/functions.py (see REGISTRY there). Colors follow the
    // default ImPlot qualitative palette so the first curves look good
    // without user tweaking.
    m_available = {
        {"sine",       "Sine wave",          "sine",       {0.26f, 0.62f, 0.96f, 1.0f}},
        {"cosine",     "Cosine wave",        "cosine",     {0.96f, 0.55f, 0.26f, 1.0f}},
        {"gaussian",   "Gaussian bell",      "gaussian",   {0.35f, 0.80f, 0.42f, 1.0f}},
        {"polynomial", "Cubic polynomial",   "polynomial", {0.85f, 0.37f, 0.63f, 1.0f}},
        {"damped",     "Damped oscillation", "damped",     {0.65f, 0.52f, 0.93f, 1.0f}},
        {"sinc",       "Sinc",               "sinc",       {0.93f, 0.83f, 0.32f, 1.0f}},
    };
}

std::optional<std::size_t> FunctionRegistry::activate(const std::string& specId) {
    if (isActive(specId)) {
        return std::nullopt;
    }
    auto it = std::find_if(m_available.begin(), m_available.end(),
                           [&](const FunctionSpec& s) { return s.id == specId; });
    if (it == m_available.end()) {
        return std::nullopt;
    }
    PlotSeries series;
    series.spec = *it;
    std::copy(std::begin(it->defaultColor), std::end(it->defaultColor), std::begin(series.color));
    m_active.push_back(std::move(series));
    return m_active.size() - 1;
}

void FunctionRegistry::deactivate(const std::string& specId) {
    std::erase_if(m_active, [&](const PlotSeries& s) { return s.spec.id == specId; });
}

bool FunctionRegistry::isActive(const std::string& specId) const {
    return std::any_of(m_active.begin(), m_active.end(),
                       [&](const PlotSeries& s) { return s.spec.id == specId; });
}

void FunctionRegistry::invalidateAll() {
    for (auto& s : m_active) {
        s.dirty = true;
    }
}

}  // namespace nxs::model
