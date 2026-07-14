#pragma once

#include "python_bridge.hpp"

#include <memory>

namespace nxs::bridge {

class PythonBridge;

class AppCore {
public:
    virtual ~AppCore() = default;

    static void install_python_bridge(const std::shared_ptr<PythonBridge>& bridge);
    static PythonBridge* get_bridge();

private:
    static std::shared_ptr<PythonBridge> s_bridge;
};

}  // namespace nxs::bridge
