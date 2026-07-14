// Controller-side facade over the embedded Python interpreter.
//
// Desktop: implemented with pybind11 embed — python/functions.py is
// imported in-process and its numpy results are read through the buffer
// protocol, so sample arrays stay in native memory (no serialization).
#pragma once

#include <memory>
#include <string>
#include <string_view>
#include <vector>

namespace nxs::controller {

class PythonEngine {
public:
    PythonEngine();   // starts the interpreter and imports python/functions.py
    ~PythonEngine();  // finalizes the interpreter

    PythonEngine(const PythonEngine&) = delete;
    auto operator=(const PythonEngine&) -> PythonEngine& = delete;

    // Evaluates the named function over [xMin, xMax] with `samples` points.
    // Fills xs/ys (resized as needed) and returns false on Python errors,
    // leaving the error text in lastError().
    [[nodiscard]] auto evaluate(std::string_view functionName, double xMin, double xMax, int samples,
                  std::vector<double>& xs, std::vector<double>& ys) -> bool;

    [[nodiscard]] auto lastError() const -> const std::string& { return m_lastError; }

    // Evaluates helpers.greeting(projectName) via the archive or python/ import.
    [[nodiscard]] auto greeting(std::string_view projectName) -> std::string;

private:
    struct Impl;   // hides pybind11 headers from the rest of the app
    std::unique_ptr<Impl> m_impl;
    std::string m_lastError;
};

}  // namespace nxs::controller
