#include "controller/PlotController.hpp"

#include <algorithm>

namespace nxs::controller {

PlotController::PlotController(model::FunctionRegistry& registry, PythonEngine& python)
    : m_registry(registry), m_python(python) {}

void PlotController::addFunction(const std::string& specId) {
    m_registry.activate(specId);  // new series start dirty and get sampled on refresh()
}

void PlotController::removeFunction(const std::string& specId) {
    m_registry.deactivate(specId);
}

void PlotController::setRange(double xMin, double xMax) {
    if (xMin >= xMax) {
        return;  // ignore degenerate ranges from half-typed input
    }
    m_settings.xMin = xMin;
    m_settings.xMax = xMax;
    m_registry.invalidateAll();
}

void PlotController::setSampleCount(int samples) {
    samples = std::clamp(samples, 64, 4096);
    if (samples == m_settings.sampleCount) {
        return;
    }
    m_settings.sampleCount = samples;
    m_registry.invalidateAll();
}

void PlotController::refresh() {
    for (auto& series : m_registry.active()) {
        if (!series.dirty) {
            continue;
        }
        bool ok = m_python.evaluate(series.spec.pythonName, m_settings.xMin, m_settings.xMax,
                                    m_settings.sampleCount, series.xs, series.ys);
        // Even on failure we clear the flag so a broken script doesn't
        // spam the interpreter every frame; the view surfaces lastError().
        series.dirty = false;
        if (!ok) {
            series.xs.clear();
            series.ys.clear();
        }
    }
}

}  // namespace nxs::controller
