// Controller-side facade over the embedded Python interpreter.
//
// Desktop: implemented with pybind11 embed — python/functions.py is
// imported in-process and its numpy results are read through the buffer
// protocol, so sample arrays stay in native memory (no serialization).
//
// The Android template exposes the same interface, but the implementation
// calls Chaquopy through the Djinni-generated bridge instead.
#pragma once

#include <memory>
#include <string>
#include <vector>

namespace nxs::controller {

class PythonEngine {
public:
    PythonEngine();   // starts the interpreter and imports python/functions.py
    ~PythonEngine();  // finalizes the interpreter

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;

    // Evaluates the named function over [xMin, xMax] with `samples` points.
    // Fills xs/ys (resized as needed) and returns false on Python errors,
    // leaving the error text in lastError().
    bool evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                  std::vector<double>& xs, std::vector<double>& ys);

    const std::string& lastError() const { return m_lastError; }

private:
    struct Impl;   // hides pybind11 headers from the rest of the app
    std::unique_ptr<Impl> m_impl;
    std::string m_lastError;
};

}  // namespace nxs::controller
