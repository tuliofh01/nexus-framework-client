// Plotter example — pybind11 embed for python/functions.py
#pragma once

#include <string>
#include <vector>

namespace nxs::controller {

class PythonEngine {
public:
    PythonEngine();
    ~PythonEngine();

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;

    bool evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                  std::vector<double>& xs, std::vector<double>& ys);

    const std::string& lastError() const { return m_lastError; }

private:
    struct Impl;
    Impl* m_impl;
    std::string m_lastError;
};

}  // namespace nxs::controller
