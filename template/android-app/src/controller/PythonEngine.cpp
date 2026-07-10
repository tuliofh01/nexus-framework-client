#include "controller/PythonEngine.hpp"

#include "app_core.hpp"
#include "python_bridge.hpp"

namespace nxs::controller {

using PythonBridgeImpl = nxs::bridge::PythonBridge;

PythonEngine& PythonEngine::instance() {
    static PythonEngine engine;
    return engine;
}

void PythonEngine::setBridge(std::shared_ptr<PythonBridgeImpl> bridge) {
    m_bridge = std::move(bridge);
}

std::string PythonEngine::greeting(const std::string& name) {
    if (!m_bridge) {
        m_lastError = "PythonBridge not installed (call AppCore.installPythonBridge first)";
        return {};
    }
    const std::string result = m_bridge->greeting(name);
    m_lastError.clear();
    return result;
}

}  // namespace nxs::controller

namespace nxs::bridge {

void AppCore::install_python_bridge(const std::shared_ptr<PythonBridge>& bridge) {
    nxs::controller::PythonEngine::instance().setBridge(bridge);
}

}  // namespace nxs::bridge
