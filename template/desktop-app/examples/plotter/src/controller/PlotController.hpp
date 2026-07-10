// Controller layer: reacts to user input reported by the view, drives
// Python evaluation, and keeps the model's sample caches fresh.
#pragma once

#include "controller/PythonEngine.hpp"
#include "model/FunctionRegistry.hpp"

#include <string>

namespace nxs::controller {

// Chart-wide settings the user can tweak. The view renders widgets for
// these and calls back into the controller when they change.
struct ChartSettings {
    double xMin = -10.0;
    double xMax = 10.0;
    int sampleCount = 512;   // per curve; slider range 64..4096 in the view
    bool logScaleY = false;  // linear/log toggle for the Y axis
    bool showGrid = true;
};

class PlotController {
public:
    PlotController(model::FunctionRegistry& registry, PythonEngine& python);

    // --- commands invoked by the view (and by Lua, see LuaPanels) -------
    void addFunction(const std::string& specId);
    void removeFunction(const std::string& specId);
    void setRange(double xMin, double xMax);
    void setSampleCount(int samples);

    // Re-evaluates every dirty series through Python. Called once per
    // frame before drawing; cheap when nothing changed.
    void refresh();

    ChartSettings& settings() { return m_settings; }
    model::FunctionRegistry& registry() { return m_registry; }
    const std::string& lastPythonError() const { return m_python.lastError(); }

private:
    model::FunctionRegistry& m_registry;
    PythonEngine& m_python;
    ChartSettings m_settings;
};

}  // namespace nxs::controller
