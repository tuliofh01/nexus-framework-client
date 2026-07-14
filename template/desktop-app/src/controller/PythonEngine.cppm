//==============================================================================
// nxs.desktop.python — Embedded Python Interpreter Facade (C++20 Module)
//
// == WHAT THIS MODULE DOES ==
// Wraps pybind11's embedded CPython interpreter. Owns the interpreter
// lifetime (construction = start, destruction = finalize). Exposes
// function evaluation and greeting helpers used by the controller.
//
// == KEY DESIGN ==
// The pybind11 headers are hidden behind a PImpl (`struct Impl`) so that
// importing modules never see <pybind11/embed.h>. This keeps compilation
// units that only `import nxs.desktop.python;` free of Python headers.
//
// == DESKTOP VS ANDROID ==
// Desktop uses pybind11 embed — CPython inside the native process.
// Android uses Chaquopy on the JVM (see nxs.android.python).
//==============================================================================

module;  // global module fragment

// ── pybind11 (embed + numpy for buffer-protocol reads) ──
#include <pybind11/embed.h>
#include <pybind11/numpy.h>

// ── Standard library ──
#include <cstring>
#include <filesystem>
#include <memory>
#include <string>
#include <string_view>
#include <vector>

export module nxs.desktop.python;

// ── Import shared modules used by the implementation ──
import nexus.shared.paths;
import nexus.shared.script_archive;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Python Evaluation Engine
// ═══════════════════════════════════════════════════════════════════════════

namespace py = pybind11;

export namespace nxs::controller {

/// Facade over the embedded CPython interpreter. Constructing a
/// PythonEngine starts the interpreter and imports `python/functions.py`;
/// destroying it finalizes the interpreter. Only one interpreter can
/// exist per process (pybind11 limitation).
export class PythonEngine {
public:
    PythonEngine();   // starts the interpreter and imports python/functions.py
    ~PythonEngine();  // finalizes the interpreter

    PythonEngine(const PythonEngine&) = delete;
    auto operator=(const PythonEngine&) -> PythonEngine& = delete;

    /// Evaluate the named function over [xMin, xMax] with `samples` points.
    /// Fills xs/ys (resized as needed) and returns false on Python errors,
    /// leaving the error text in lastError().
    [[nodiscard]] auto evaluate(std::string_view functionName,
                                double xMin, double xMax, int samples,
                                std::vector<double>& xs,
                                std::vector<double>& ys) -> bool;

    /// Evaluate helpers.greeting(projectName) via the archive or python/ import.
    [[nodiscard]] auto greeting(std::string_view projectName) -> std::string;

    /// The last Python error message, or empty if no error.
    [[nodiscard]] auto lastError() const -> const std::string& {
        return m_lastError;
    }

private:
    struct Impl;  // hides pybind11 types from the module interface
    std::unique_ptr<Impl> m_impl;
    std::string m_lastError;
};

// ── Implementation details ───────────────────────────────────────────────

namespace {

/// Prefer packed python.dat in release; fall back to plaintext python/ for
/// development iteration. Returns true if the archive was loaded successfully.
bool importFromArchive(py::module_& out) {
    const auto archivePath = runtime::Paths::pythonArchive();
    if (!std::filesystem::exists(archivePath)) {
        return false;
    }

    auto archive = runtime::ScriptArchive(runtime::ScriptArchive::MAGIC_PYTHON);
    if (!archive.load(archivePath)) {
        return false;
    }

    std::string source;
    if (!archive.getSource("functions", source)) {
        return false;
    }

    auto sys = py::module_::import("sys");
    auto mod = py::module_::create("functions");
    py::exec(source, mod.attr("__dict__"));
    sys.attr("modules")["functions"] = mod;
    out = mod;
    return true;
}

}  // anonymous namespace

// PImpl structure: holds the scoped interpreter and the functions module ref.
struct PythonEngine::Impl {
    py::scoped_interpreter interpreter;
    py::module_ functions;
};

PythonEngine::PythonEngine() {
    m_impl = std::make_unique<Impl>();
    if (!importFromArchive(m_impl->functions)) {
        auto sys = py::module_::import("sys");
        sys.attr("path").attr("insert")(0, "python");
        m_impl->functions = py::module_::import("functions");
    }
}

PythonEngine::~PythonEngine() = default;

auto PythonEngine::greeting(std::string_view projectName) -> std::string {
    try {
        return m_impl->functions.attr("greeting")(projectName)
               .cast<std::string>();
    } catch (const py::error_already_set& e) {
        m_lastError = e.what();
        return {};
    }
}

auto PythonEngine::evaluate(std::string_view functionName,
                            double xMin, double xMax, int samples,
                            std::vector<double>& xs,
                            std::vector<double>& ys) -> bool {
    try {
        auto result = m_impl->functions.attr("evaluate")(functionName,
                        xMin, xMax, samples);
        auto xArr = result[0].cast<
            py::array_t<double, py::array::c_style | py::array::forcecast>>();
        auto yArr = result[1].cast<
            py::array_t<double, py::array::c_style | py::array::forcecast>>();

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
