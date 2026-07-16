// Controller layer: reacts to user input reported by the view, drives
// Python evaluation, and keeps the model's sample caches fresh.
#pragma once

#include "controller/PythonEngine.hpp"
#include "model/FunctionRegistry.hpp"

#include <string>
#include <string_view>

namespace nxs::controller {

struct ChartSettings {
    double xMin = -10.0;
    double xMax = 10.0;
    int sampleCount = 512;
    bool logScaleY = false;
    bool showGrid = true;
};

class PlotController {
public:
    PlotController(model::FunctionRegistry& registry, PythonEngine& python);

    void addFunction(const std::string& specId);
    void removeFunction(const std::string& specId);
    void setRange(double xMin, double xMax);
    void setSampleCount(int samples);

    void refresh();

    [[nodiscard]] auto settings() -> ChartSettings& { return m_settings; }
    [[nodiscard]] auto registry() -> model::FunctionRegistry& { return m_registry; }
    [[nodiscard]] auto lastPythonError() const -> const std::string& { return m_python.lastError(); }

private:
    model::FunctionRegistry& m_registry;
    PythonEngine& m_python;
    ChartSettings m_settings;
};

}  // namespace nxs::controller
