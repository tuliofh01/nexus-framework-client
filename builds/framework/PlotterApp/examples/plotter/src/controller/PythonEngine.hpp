// Plotter example — pybind11 embed for python/functions.py
#pragma once

#include <string>
#include <string_view>
#include <vector>

namespace nxs::controller {

class PythonEngine {
public:
    PythonEngine();
    ~PythonEngine();

    PythonEngine(const PythonEngine&) = delete;
    auto operator=(const PythonEngine&) -> PythonEngine& = delete;

    [[nodiscard]] auto evaluate(std::string_view functionName,
                                double xMin, double xMax, int samples,
                                std::vector<double>& xs,
                                std::vector<double>& ys) -> bool;

    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    struct Impl;
    Impl* m_impl;
    std::string m_lastError;
};

}  // namespace nxs::controller
