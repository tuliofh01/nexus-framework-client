#include "app_core.hpp"

namespace nxs::bridge {

std::shared_ptr<PythonBridge> AppCore::s_bridge = nullptr;

void AppCore::install_python_bridge(const std::shared_ptr<PythonBridge>& bridge) {
    s_bridge = bridge;
}

PythonBridge* AppCore::get_bridge() {
    return s_bridge.get();
}

}  // namespace nxs::bridge
