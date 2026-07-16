//==============================================================================
// nxs.desktop.python — Embedded Python Interpreter Facade (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Wraps pybind11's embedded CPython interpreter. Owns the interpreter
// lifetime (construction = start, destruction = finalize). Exposes
// function evaluation and greeting helpers used by the controller.
//
// ════════════════════════════════════════════════════════════════════════════
// KEY DESIGN: PIMPL (Pointer to Implementation)
// ════════════════════════════════════════════════════════════════════════════
//
// The pybind11 headers are hidden behind a PImpl (`struct Impl`). This
// means:
//   - Importers of `nxs.desktop.python` never see <pybind11/embed.h>
//   - Compilation units that only `import nxs.desktop.python;` are free
//     of Python headers — faster builds, cleaner includes
//   - The public API only exposes opaque std::unique_ptr<Impl>
//
// How PImpl works:
//   1. In the class declaration, we declare `struct Impl;` (forward decl)
//   2. We store `std::unique_ptr<Impl> m_impl;` (pointer, not value)
//   3. The actual struct definition lives in the .cppm implementation
//   4. Importers only see the forward declaration — they can't access
//      the pybind11 types inside Impl
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// std::unique_ptr<T> — RAII smart pointer. Owns the PImpl struct.
//   - Destructor calls `delete` automatically
//   - Cannot be copied (prevents double-free)
//   - Can be moved (transfers ownership)
//   - `std::make_unique<T>(...)` creates one safely (no raw new)
//
// std::string_view    — non-owning string view. Used for function names
//                       and project names — avoids heap allocation.
//
// std::move           — transfers ownership of py::module_ into `out`.
//                       After move, the source is in a valid-but-
//                       unspecified state.
//
// static_cast<T>      — explicit type conversion. Used here to convert
//                       pybind11's ssize_t to std::size_t safely.
//
// std::memcpy         — byte-level memory copy. Fastest way to transfer
//                       data between C++ vectors and pybind11 arrays.
//                       Safe because both are POD types (double).
//
// ════════════════════════════════════════════════════════════════════════════
// RAII (Resource Acquisition Is Initialization)
// ════════════════════════════════════════════════════════════════════════════
//
// Constructor: starts pybind11's scoped_interpreter (initializes CPython)
// Destructor:  finalizes CPython (automatic via scoped_interpreter's dtor)
//
// Only ONE interpreter can exist per process (pybind11 limitation).
// The class is non-copyable/non-movable to prevent accidental duplication.
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
//
// Desktop: pybind11 embed — CPython inside the native process.
// Android: Chaquopy on the JVM — see nxs.android.python.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── pybind11 headers (NOT exported to importers) ──
//
// These are in the global module fragment so they are private to this
// translation unit. Any file that does `import nxs.desktop.python;`
// will NOT see these includes. This is the whole point of PImpl.
#include <pybind11/embed.h>    // py::scoped_interpreter, py::module_
#include <pybind11/numpy.h>    // py::array_t for numpy buffer protocol

// ── Standard library (private to this module) ──
//
// <cstring>       — std::memcpy for byte-level array transfer
// <filesystem>    — std::filesystem::exists for archive path check
// <memory>        — std::unique_ptr, std::make_unique for PImpl
// <string>        — std::string for error messages and return values
// <string_view>   — std::string_view for cheap function/project names
// <utility>       — std::move for ownership transfer
// <vector>        — std::vector<double> for x/y sample arrays
#include <algorithm>
#include <cstring>
#include <filesystem>
#include <memory>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

export module nxs.desktop.python;

// ── Import shared modules (used by the implementation, not the interface) ──
//
// These are imported for the implementation body below. Importers of
// nxs.desktop.python do NOT transitively get these — they would need
// their own `import nexus.shared.paths;` if they need it.
import nexus.shared.paths;
import nexus.shared.script_archive;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Python Evaluation Engine
// ═══════════════════════════════════════════════════════════════════════════

// Namespace alias: `py` is the conventional short name for pybind11.
// This is file-local (not exported) — importers don't see it.
namespace py = pybind11;

export namespace nxs::controller {

/// Facade over the embedded CPython interpreter.
///
/// Construction starts the interpreter and imports `python/functions.py`.
/// Destruction finalizes the interpreter. Only one interpreter can exist
/// per process (pybind11 limitation).
///
/// PImpl pattern: the public API exposes only `std::unique_ptr<Impl>`,
/// hiding all pybind11 types from importers.
///
/// Non-copyable, non-movable: the interpreter is a process-global
/// resource. Copying would create two interpreters (undefined behaviour).
class PythonEngine {
public:
    PythonEngine();   // starts the interpreter and imports python/functions.py
    ~PythonEngine();  // finalizes the interpreter

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // pybind11's scoped_interpreter is non-copyable. Even if it weren't,
    // we don't want two interpreters per process. = delete prevents
    // accidental duplication at compile time.

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;
    PythonEngine(PythonEngine&&) = delete;
    PythonEngine& operator=(PythonEngine&&) = delete;

    // ── Evaluation API ─────────────────────────────────────────────────
    //
    // evaluate(): calls a Python function over [xMin, xMax] with
    // `samples` points. Returns std::optional-style bool:
    //   - true  → success, xs/ys filled
    //   - false → Python error, lastError() has the message
    //
    // Parameters use std::string_view (non-owning) for the function
    // name — avoids heap allocation for a read-only string.
    //
    // xs/ys are output parameters passed by mutable reference. The
    // function resizes them as needed. This avoids returning a pair
    // (which would require std::move on large vectors).

    [[nodiscard]] auto evaluate(std::string_view functionName,
                                double xMin, double xMax, int samples,
                                std::vector<double>& xs,
                                std::vector<double>& ys) -> bool;

    /// Evaluate helpers.greeting(projectName) via the archive or python/ import.
    [[nodiscard]] auto greeting(std::string_view projectName) -> std::string;

    /// The last Python error message, or empty if no error.
    ///
    // noexcept: returning a const reference is safe (no allocation).
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    // ── PImpl: Pointer to Implementation ───────────────────────────────
    //
    // `struct Impl;` is a forward declaration. The actual definition
    // (with pybind11 types) lives further down in this file.
    //
    // `std::unique_ptr<Impl>` is an owning pointer that:
    //   - Calls `delete` on destruction (RAII)
    //   - Cannot be copied (prevents double-free)
    //   - Stores the pybind11 interpreter and module references
    //
    // std::make_unique<Impl>() in the constructor is safe — it calls
    // `new` internally but wraps it in a unique_ptr immediately.

    struct Impl;  // forward declaration — actual definition below
    std::unique_ptr<Impl> m_impl;
    std::string m_lastError;
};

}  // export namespace nxs::controller

// ═══════════════════════════════════════════════════════════════════════════
// Implementation details (non-exported: anonymous namespaces and definitions
// must live OUTSIDE an `export namespace` block per [module.interface])
// ═══════════════════════════════════════════════════════════════════════════

namespace nxs::controller {

namespace {

/// Prefer packed python.dat in release; fall back to plaintext python/ for
/// development iteration. Returns true if the archive was loaded successfully.
///
/// This function demonstrates several modern C++ patterns:
///   - std::filesystem::exists: portable file existence check
///   - std::string out-parameter: avoids returning large objects
///   - py::module_::create: creates a new Python module in-process
///   - py::exec: executes Python source code within a module's dict
auto importFromArchive(py::module_& out) -> bool {
    const auto archivePath = runtime::Paths::pythonArchive();
    if (!std::filesystem::exists(archivePath)) {
        return false;
    }

    // ScriptArchive reads packed .dat files (LZ4-compressed Python source).
    // MAGIC_PYTHON is a magic number that identifies the archive type.
    auto archive = runtime::ScriptArchive(runtime::ScriptArchive::MAGIC_PYTHON);
    if (!archive.load(archivePath)) {
        return false;
    }

    std::string source;
    if (!archive.getSource("functions", source)) {
        return false;
    }

    // Execute the Python source in a new module, then register it in
    // sys.modules so `import functions` works from other Python code.
    // types.ModuleType("functions") creates an empty module object —
    // pybind11 has no direct "create plain module" helper for this.
    auto sys = py::module_::import("sys");
    auto types = py::module_::import("types");
    auto mod = types.attr("ModuleType")("functions").cast<py::module_>();
    py::exec(source, mod.attr("__dict__"));
    sys.attr("modules")["functions"] = mod;

    // std::move: transfers ownership of the py::module_ into `out`.
    // After this, `mod` is in a valid-but-unspecified state (we don't
    // use it again).
    out = std::move(mod);
    return true;
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// PImpl struct definition (the actual implementation)
// ═══════════════════════════════════════════════════════════════════════════
//
// This is where the pybind11 types live. By defining them here (after
// the class declaration), importers never see them.

struct PythonEngine::Impl {
    py::scoped_interpreter interpreter;  // RAII: starts CPython on construction
    py::module_ functions;               // reference to python/functions.py
};

// ═══════════════════════════════════════════════════════════════════════════
// Constructor / Destructor
// ═══════════════════════════════════════════════════════════════════════════

PythonEngine::PythonEngine()
    : m_impl{std::make_unique<Impl>()} {  // {} brace-init for unique_ptr
    // Try to load from the packed archive first (release mode).
    // Fall back to plaintext python/ directory (development mode).
    if (!importFromArchive(m_impl->functions)) {
        auto sys = py::module_::import("sys");
        sys.attr("path").attr("insert")(0, "python");
        try {
            m_impl->functions = py::module_::import("functions");
        } catch (const py::error_already_set&) {
            // Fall back to the minimal greeting helper if functions.py was
            // removed while adapting the generated project.
            try {
                m_impl->functions = py::module_::import("helpers");
            } catch (const py::error_already_set& e2) {
                // Leave an empty module so calls fail soft via lastError
                // instead of terminating the app during startup.
                m_lastError = e2.what();
                m_impl->functions = py::module_::import("types")
                    .attr("ModuleType")("functions").cast<py::module_>();
            }
        }
    }
}

PythonEngine::~PythonEngine() = default;
// unique_ptr<Impl> destructor calls `delete` on Impl, which destroys
// the scoped_interpreter, which finalizes CPython. Full RAII chain.

// ═══════════════════════════════════════════════════════════════════════════
// Public methods
// ═══════════════════════════════════════════════════════════════════════════

auto PythonEngine::greeting(std::string_view projectName) -> std::string {
    try {
        // pybind11 attribute access: `functions.attr("greeting")(name)`
        // calls the Python function `greeting(name)` and returns a
        // pybind11 object. `.cast<std::string>()` converts it to C++.
        return m_impl->functions.attr("greeting")(projectName)
               .cast<std::string>();
    } catch (const py::error_already_set& e) {
        // py::error_already_set: pybind11's exception for Python errors.
        // `.what()` returns the Python traceback as a string.
        m_lastError = e.what();
        return {};  // empty string on failure
    }
}

auto PythonEngine::evaluate(std::string_view functionName,
                            double xMin, double xMax, int samples,
                            std::vector<double>& xs,
                            std::vector<double>& ys) -> bool {
    try {
        // Call the Python function: `functions.evaluate(name, min, max, n)`
        // Returns a Python tuple of two numpy arrays: (xs, ys).
        auto result = m_impl->functions.attr("evaluate")(functionName,
                        xMin, xMax, samples)
                        .cast<py::tuple>();

        // Cast the Python tuple elements to pybind11 array_t<double>.
        // py::array::c_style: ensures C-contiguous memory layout.
        // py::array::forcecast: forces conversion if the array isn't
        // already the right type.
        auto xArr = result[0].cast<
            py::array_t<double, py::array::c_style | py::array::forcecast>>();
        auto yArr = result[1].cast<
            py::array_t<double, py::array::c_style | py::array::forcecast>>();

        // Resize the output vectors to match the numpy array sizes.
        // static_cast<std::size_t>: explicit conversion from pybind11's
        // ssize_t (signed) to std::size_t (unsigned). Safe because sizes
        // are always non-negative.
        xs.resize(static_cast<std::size_t>(xArr.size()));
        ys.resize(static_cast<std::size_t>(yArr.size()));

        // std::ranges::copy: element-wise copy from numpy's buffer to our
        // vector. The compiler optimises this to memcpy for trivially
        // copyable types like double. Safer than raw std::memcpy because
        // it respects iterator semantics and avoids manual byte arithmetic.
        std::ranges::copy(xArr.data(), xArr.data() + xs.size(), xs.data());
        std::ranges::copy(yArr.data(), yArr.data() + ys.size(), ys.data());

        m_lastError.clear();
        return true;  // success
    } catch (const py::error_already_set& e) {
        m_lastError = e.what();
        return false;  // Python error — caller checks lastError()
    }
}

}  // namespace nxs::controller
