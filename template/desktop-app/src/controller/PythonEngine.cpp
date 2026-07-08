#include "controller/PythonEngine.hpp"

#include <pybind11/embed.h>
#include <pybind11/numpy.h>

#include <cstring>

namespace py = pybind11;

namespace nxs::controller {

struct PythonEngine::Impl {
    py::scoped_interpreter interpreter;  // CPython lives for the app's lifetime
    py::module_ functions;               // the python/functions.py module
};

PythonEngine::PythonEngine() {
    m_impl = new Impl{};
    // python/ is copied next to the executable at build time; make it
    // importable regardless of the process working directory.
    py::module_ sys = py::module_::import("sys");
    sys.attr("path").attr("insert")(0, "python");
    m_impl->functions = py::module_::import("functions");
}

PythonEngine::~PythonEngine() {
    delete m_impl;  // scoped_interpreter finalizes CPython here
}

bool PythonEngine::evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                            std::vector<double>& xs, std::vector<double>& ys) {
    try {
        // functions.evaluate(...) returns a pair of float64 numpy arrays.
        py::tuple result = m_impl->functions.attr("evaluate")(functionName, xMin, xMax, samples);
        auto xArr = result[0].cast<py::array_t<double, py::array::c_style | py::array::forcecast>>();
        auto yArr = result[1].cast<py::array_t<double, py::array::c_style | py::array::forcecast>>();

        // Buffer-protocol access: memcpy straight out of numpy's native
        // storage into our vectors. Nothing crosses a serialization layer.
        xs.resize(static_cast<std::size_t>(xArr.size()));
        ys.resize(static_cast<std::size_t>(yArr.size()));
        std::memcpy(xs.data(), xArr.data(), xs.size() * sizeof(double));
        std::memcpy(ys.data(), yArr.data(), ys.size() * sizeof(double));
        m_lastError.clear();
        return true;
    } catch (const py::error_already_set& e) {
        m_lastError = e.what();
        return false;
    }
}

}  // namespace nxs::controller
