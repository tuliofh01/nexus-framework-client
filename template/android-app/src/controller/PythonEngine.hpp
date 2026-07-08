// Android PythonEngine: same public API as desktop, but evaluation goes
// through the Djinni PythonBridge (Kotlin + Chaquopy) instead of pybind11.
#pragma once

#include <memory>
#include <string>
#include <vector>

namespace nxs::bridge {
class PythonBridge;
}

namespace nxs::controller {

class PythonEngine {
public:
    PythonEngine() = default;
    ~PythonEngine() = default;

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;

    // Called once from PlotterCore::install_python_bridge before the
    // SDL/ImGui loop starts.
    void setBridge(std::shared_ptr<nxs::bridge::PythonBridge> bridge);

    bool evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                  std::vector<double>& xs, std::vector<double>& ys);

    const std::string& lastError() const { return m_lastError; }

    // Singleton accessor used by PlotterCore to install the bridge into
    // the engine owned by main().
    static PythonEngine& instance();

private:
    std::shared_ptr<nxs::bridge::PythonBridge> m_bridge;
    std::string m_lastError;
};

}  // namespace nxs::controller
