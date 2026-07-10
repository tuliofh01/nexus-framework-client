// Controller-side facade over Chaquopy via Djinni PythonBridge.
#pragma once

#include <memory>
#include <string>

namespace nxs::controller {

class PythonEngine {
public:
    static PythonEngine& instance();

    void setBridge(std::shared_ptr<class PythonBridgeImpl> bridge);

    std::string greeting(const std::string& name);

    const std::string& lastError() const { return m_lastError; }

private:
    PythonEngine() = default;
    std::shared_ptr<class PythonBridgeImpl> m_bridge;
    std::string m_lastError;
};

}  // namespace nxs::controller
