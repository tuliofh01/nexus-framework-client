//==============================================================================
// nxs.android.python — Chaquopy/Zig Python Facade (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Singleton facade over Chaquopy via a pure-Zig JNI bridge. C++ never
// embeds CPython on Android — JVM owns the interpreter. The C++ side
// delegates calls through C ABI functions exported by the Zig JNI bridge
// (zig-services/jni/python_bridge.zig).
//
// ════════════════════════════════════════════════════════════════════════════
// CALL CHAIN
// ════════════════════════════════════════════════════════════════════════════
//
//   Kotlin MainActivity
//     → AppCore.installPythonBridge(bridge)
//       → JNI export: Java_com_nexus_AppCore_installPythonBridge (Zig)
//         → stores JavaVM*, bridge ref, method IDs in Zig globals
//   ... later, C++ frame:
//     → PythonEngine::greeting("MyApp")
//       → zig_python_greeting("MyApp")   (C ABI → Zig)
//         → JNI CallObjectMethodA on bridge.greeting()
//         → returns const char* (heap-allocated)
//       → wraps in std::string, frees Zig allocation
//
// ════════════════════════════════════════════════════════════════════════════
// ZIG BRIDGE INTERFACE
// ════════════════════════════════════════════════════════════════════════════
//
// The Zig bridge (python_bridge.zig) exports these C ABI functions:
//
//   bool zig_python_bridge_is_installed()
//     → Returns true after Zig has stored the Kotlin bridge reference.
//
//   const char* zig_python_greeting(const char* name)
//     → Calls bridge.greeting(name) via JNI.
//     → Returns heap-allocated C string (caller must zig_free_string).
//     → Returns nullptr if bridge not installed or JNI fails.
//
//   ZigEvalResult zig_python_evaluate(func, xmin, xmax, samples)
//     → Calls bridge.evaluate(func, xmin, xmax, samples) via JNI.
//     → Returns struct with heap-allocated arrays (caller must
//       zig_free_eval_result).
//
//   void zig_free_string(const char* ptr)
//     → Frees a string returned by zig_python_greeting.
//
//   void zig_free_eval_result(ZigEvalResult result)
//     → Frees arrays inside an evaluation result.
//
// See zig-services/jni/README.md for full interface documentation.
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
// Desktop: pybind11 embed (see nxs.desktop.python).
// Android: Chaquopy on the JVM + a Zig JNI bridge (see zig-services/).
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Zig C ABI types (not exported to importers) ──
//
// These mirror the extern struct definitions in python_bridge.zig.
// The memory layout MUST match between Zig and C++ (both use C-compatible
// struct layout). Fields are arranged to minimize padding.
struct ZigEvalResult {
    bool ok;
    const char* error;
    const double* xs;
    int32_t xs_len;
    const double* ys;
    int32_t ys_len;
};

// ── Zig C ABI function declarations (private to this module) ──
//
// These are exported by zig-services/jni/python_bridge.zig. They handle
// all JNI thread attachment, method lookup, and field extraction. The
// C++ side only needs to call them and free the results.
extern "C" {
    [[nodiscard]] bool zig_python_bridge_is_installed() noexcept;
    [[nodiscard]] const char* zig_python_greeting(const char* name) noexcept;
    [[nodiscard]] ZigEvalResult zig_python_evaluate(
        const char* func, double xmin, double xmax, int32_t samples) noexcept;
    void zig_free_string(const char* ptr) noexcept;
    void zig_free_eval_result(ZigEvalResult result) noexcept;
}

// ── Standard library (private to this module) ──
#include <cstdint>   // int32_t
#include <cstring>   // std::strlen
#include <string>     // std::string for greeting and errors
#include <string_view>// std::string_view for API params
#include <vector>     // std::vector for evaluate result arrays

export module nxs.android.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime — EvalResult (C++21owned wrapper)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::runtime {

/// Result of evaluating a Python function — owned C++ types.
/// Constructed from a ZigEvalResult via fromZig(), which takes ownership
/// of the heap-allocated data and frees the Zig allocation.
/// Rule of Five: default copy/move/destroy (members are self-owned).
export struct EvalResult {
    bool ok{false};
    std::string error{};
    std::vector<double> xs{};
    std::vector<double> ys{};

    /// Adopt heap data from a ZigEvalResult into owned C++ containers.
    /// Frees the Zig-allocated memory after copying.
    static auto fromZig(ZigEvalResult z) -> EvalResult {
        auto r = EvalResult{};
        r.ok = z.ok;
        if (z.error) r.error = std::string(z.error);
        if (z.xs && z.xs_len > 0) {
            r.xs.assign(z.xs, z.xs + z.xs_len);
        }
        if (z.ys && z.ys_len > 0) {
            r.ys.assign(z.ys, z.ys + z.ys_len);
        }
        zig_free_eval_result(z);
        return r;
    }
};

}  // namespace nxs::runtime

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Python Delegation Facade (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::controller {

/// Singleton facade over Chaquopy via the Zig JNI bridge.
///
/// Usage:
///   auto result = PythonEngine::instance().greeting("MyApp");
///   auto eval = PythonEngine::instance().evaluate("sin", 0.0, 3.14, 100);
///
/// The bridge is installed by the Kotlin MainActivity before SDL_main()
/// starts — no manual setup needed in C++.
///
/// RAII: Meyer's singleton (thread-safe in C++11+). The bridge reference
///       lives in Zig process-wide globals, not C++ shared_ptr.
///
/// Non-copyable, non-movable: singleton by design.
export class PythonEngine {
public:
    // ── Singleton access ───────────────────────────────────────────────
    //
    // Meyer's singleton: static local initialization is thread-safe
    // (C++11 §6.7/4). Returns a reference (caller cannot delete).

    [[nodiscard]] static auto instance() -> PythonEngine& {
        static PythonEngine engine;
        return engine;
    }

    // ── Public API ─────────────────────────────────────────────────────

    /// Delegate to the Zig bridge's greeting() method.
    ///
    /// If the bridge is not installed, sets lastError() and returns empty.
    /// std::string_view param: accepts "..."sv, const char*, and std::string
    /// without allocation. Zig expects a null-terminated C string.
    auto greeting(std::string_view name) noexcept -> std::string {
        if (!zig_python_bridge_is_installed()) {
            m_lastError =
                "PythonBridge not installed (MainActivity must call AppCore.installPythonBridge first)";
            return {};
        }
        // zig_python_greeting expects null-terminated C string; string_view
        // may not be null-terminated, so we allocate a temporary.
        // If the caller passes "..."sv (a string literal), it happens to
        // be null-terminated, but we don't rely on that.
        const auto cname = std::string{name};
        const auto* cstr = zig_python_greeting(cname.c_str());
        if (!cstr) {
            m_lastError = "Zig bridge returned null from greeting";
            return {};
        }
        std::string result(cstr);
        zig_free_string(cstr);
        m_lastError.clear();
        return result;
    }

    /// Evaluate a Python function via the Zig bridge.
    ///
    /// Returns a nxs::runtime::EvalResult with ok, error, xs, and ys.
    /// xs and ys are heap-allocated by Zig and adopted into std::vector
    /// by fromZig().
    auto evaluate(
        std::string_view function_name,
        double x_min, double x_max,
        int32_t samples) noexcept -> nxs::runtime::EvalResult
    {
        if (!zig_python_bridge_is_installed()) {
            m_lastError = "PythonBridge not installed";
            return {false, "PythonBridge not installed", {}, {}};
        }
        const auto cname = std::string{function_name};
        const auto zr = zig_python_evaluate(
            cname.c_str(), x_min, x_max, samples);
        m_lastError.clear();
        return nxs::runtime::EvalResult::fromZig(zr);
    }

    /// The last error message, or empty if no error.
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    // ── Private constructor (singleton) ────────────────────────────────

    PythonEngine() = default;
    ~PythonEngine() = default;

    // ── Rule of Five: delete copy and move ─────────────────────────────

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;
    PythonEngine(PythonEngine&&) = delete;
    PythonEngine& operator=(PythonEngine&&) = delete;

    std::string m_lastError{};  ///< Last error message
};

}  // namespace nxs::controller
