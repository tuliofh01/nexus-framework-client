#include "controller/PythonEngine.hpp"

#include "eval_result.hpp"
#include "plotter_core.hpp"
#include "python_bridge.hpp"

namespace nxs::controller {

PythonEngine& PythonEngine::instance() {
    static PythonEngine engine;
    return engine;
}

void PythonEngine::setBridge(std::shared_ptr<nxs::bridge::PythonBridge> bridge) {
    m_bridge = std::move(bridge);
}

bool PythonEngine::evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                            std::vector<double>& xs, std::vector<double>& ys) {
    if (!m_bridge) {
        m_lastError = "PythonBridge not installed (call PlotterCore.installPythonBridge first)";
        return false;
    }
    nxs::bridge::EvalResult result = m_bridge->evaluate(functionName, xMin, xMax, samples);
    if (!result.ok) {
        m_lastError = result.error;
        xs.clear();
        ys.clear();
        return false;
    }
    xs = std::move(result.xs);
    ys = std::move(result.ys);
    m_lastError.clear();
    return true;
}

}  // namespace nxs::controller

namespace nxs::bridge {

void PlotterCore::install_python_bridge(const std::shared_ptr<PythonBridge>& bridge) {
    nxs::controller::PythonEngine::instance().setBridge(bridge);
}

}  // namespace nxs::bridge
