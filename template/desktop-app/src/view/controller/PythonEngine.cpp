#include "controller/PythonEngine.hpp"

#include "Paths.hpp"
#include "ScriptArchive.hpp"

#include <pybind11/embed.h>
#include <pybind11/numpy.h>

#include <cstring>
#include <filesystem>

namespace py = pybind11;

namespace nxs::controller {

struct PythonEngine::Impl {
    py::scoped_interpreter interpreter;
    py::module_ functions;
};

namespace {

bool importFromArchive(py::module_& out) {
    const std::string archivePath = runtime::Paths::pythonArchive();
    if (!std::filesystem::exists(archivePath)) {
        return false;
    }

    runtime::ScriptArchive archive(runtime::ScriptArchive::MAGIC_PYTHON);
    if (!archive.load(archivePath)) {
        return false;
    }

    std::string source;
    if (!archive.getSource("functions", source)) {
        return false;
    }

    py::module_ sys = py::module_::import("sys");
    py::module_ mod = py::module_::create("functions");
    py::exec(source, mod.attr("__dict__"));
    sys.attr("modules")["functions"] = mod;
    out = mod;
    return true;
}

}  // namespace

PythonEngine::PythonEngine() {
    m_impl = new Impl{};
    if (!importFromArchive(m_impl->functions)) {
        // Dev fallback: import from python/ next to the executable.
        py::module_ sys = py::module_::import("sys");
        sys.attr("path").attr("insert")(0, "python");
        m_impl->functions = py::module_::import("functions");
    }
}

PythonEngine::~PythonEngine() {
    delete m_impl;
}

bool PythonEngine::evaluate(const std::string& functionName, double xMin, double xMax, int samples,
                            std::vector<double>& xs, std::vector<double>& ys) {
    try {
        py::tuple result = m_impl->functions.attr("evaluate")(functionName, xMin, xMax, samples);
        auto xArr = result[0].cast<py::array_t<double, py::array::c_style | py::array::forcecast>>();
        auto yArr = result[1].cast<py::array_t<double, py::array::c_style | py::array::forcecast>>();

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
