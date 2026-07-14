//==============================================================================
// nxs.android.python — Chaquopy/Djinni Python Facade (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Singleton facade over Chaquopy via the Djinni PythonBridge. C++ never
// embeds CPython on Android — JVM owns the interpreter. The C++ side only
// delegates calls through the bridge shared_ptr.
//
// ════════════════════════════════════════════════════════════════════════════
// SINGLETON PATTERN (Meyer's Singleton)
// ════════════════════════════════════════════════════════════════════════════
//
// PythonEngine::instance() returns a process-wide singleton. The Kotlin
// MainActivity calls AppCore.installPythonBridge() to wire the Chaquopy
// bridge before SDL_main() starts.
//
// The singleton is a Meyer's singleton:
//   - Thread-safe in C++11+ (static local initialization is atomic)
//   - Zero-cost when not used (no global constructor)
//   - No manual cleanup (destructor runs at process exit)
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// static local          — Meyer's singleton (thread-safe, zero overhead)
// std::shared_ptr<T>    — shared ownership of the bridge object
// std::move             — transfers bridge ownership into the member
// = delete              — Rule of Five: non-copyable, non-movable
// [[nodiscard]]         — return values must be checked
// constexpr             — (trivial accessors are implicitly constexpr)
// trailing return       — auto fn() -> Type syntax
// {} brace-init         — uniform initialization for member
// noexcept              — on simple accessors (where safe)
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
// Desktop: pybind11 embed (see nxs.desktop.python).
// Android: Chaquopy on the JVM + a Zig JNI bridge (see zig-services/).
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Djinni bridge headers (not exported to importers) ──
//
// These define nxs::bridge::PythonBridge (the abstract interface) and
// AppCore (the JNI entry point). They are in the global module fragment
// so importers never see them.
#include "app_core.hpp"
#include "python_bridge.hpp"

// ── Standard library (private to this module) ──
#include <memory>   // std::shared_ptr for bridge ownership
#include <string>   // std::string for greeting and errors
#include <utility>  // std::move for ownership transfer

export module nxs.android.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Python Delegation Facade (Android)
// ═══════════════════════════════════════════════════════════════════════════

namespace {
// Type alias: resolves the fully qualified C++ class for PythonBridge.
// In the global Djinni header it's nxs::bridge::PythonBridge. We alias
// it here for brevity and to limit the dependency to a single using decl.
using PythonBridgeImpl = nxs::bridge::PythonBridge;
}  // anonymous namespace

export namespace nxs::controller {

/// Singleton facade over Chaquopy.
///
/// Usage:
///   PythonEngine::instance().setBridge(myBridge);
///   auto greeting = PythonEngine::instance().greeting("MyApp");
///
/// RAII: Meyer's singleton (thread-safe in C++11+). The shared_ptr
///       to PythonBridge is non-owning from the C++ side — the Kotlin
///       side holds the primary reference.
///
/// Non-copyable, non-movable: singleton by design.
export class PythonEngine {
public:
    // ── Singleton access ───────────────────────────────────────────────
    //
    // Meyer's singleton: `static PythonEngine engine;` inside a function
    // is guaranteed to be initialized exactly once, even across threads
    // (C++11 §6.7/4). Returns a reference (caller cannot delete).

    [[nodiscard]] static auto instance() -> PythonEngine& {
        static PythonEngine engine;
        return engine;
    }

    // ── Bridge management ──────────────────────────────────────────────

    /// Set the Chaquopy/Djinni bridge.
    ///
    /// Called by AppCore::install_python_bridge when the Kotlin
    /// MainActivity starts — always before SDL_main().
    ///
    /// std::shared_ptr: shared ownership. The Kotlin side keeps a
    /// reference too. When both drop the reference, the bridge is
    /// destroyed.
    ///
    /// std::move: transfers ownership of the shared_ptr into m_bridge.
    /// After the move, `bridge` is null.
    void setBridge(std::shared_ptr<PythonBridgeImpl> bridge) noexcept {
        m_bridge = std::move(bridge);
    }

    // ── Public API ─────────────────────────────────────────────────────

    /// Delegate to the bridge's greeting() method.
    ///
    /// If the bridge is not installed, sets an error and returns empty.
    /// std::string param: taken by value (the bridge expects a string).
    auto greeting(const std::string& name) noexcept -> std::string {
        if (!m_bridge) {
            m_lastError =
                "PythonBridge not installed (call AppCore.installPythonBridge first)";
            return {};
        }
        const auto result = m_bridge->greeting(name);
        m_lastError.clear();
        return result;
    }

    /// The last error message, or empty if no error.
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    // ── Private constructor (singleton) ────────────────────────────────
    //
    // Construction does nothing — the bridge is set later by setBridge().

    PythonEngine() = default;
    ~PythonEngine() = default;

    // ── Rule of Five: delete copy and move ─────────────────────────────

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;
    PythonEngine(PythonEngine&&) = delete;
    PythonEngine& operator=(PythonEngine&&) = delete;

    std::shared_ptr<PythonBridgeImpl> m_bridge;  ///< Set by setBridge()
    std::string m_lastError;                      ///< Last error message
};

}  // namespace nxs::controller

// ═══════════════════════════════════════════════════════════════════════════
// Djinni AppCore export (wires the Chaquopy bridge)
// ═══════════════════════════════════════════════════════════════════════════
//
// This function is called by the Djinni-generated JNI glue when the Kotlin
// MainActivity starts. It connects the Chaquopy bridge to our C++ singleton.
// The bridge is a std::shared_ptr because the Kotlin side may also keep a
// reference.

export namespace nxs::bridge {

void AppCore::install_python_bridge(
    const std::shared_ptr<PythonBridge>& bridge) {
    // Forward to the singleton — the actual bridge is managed by the
    // Kotlin Chaquopy integration.
    nxs::controller::PythonEngine::instance().setBridge(bridge);
}

}  // namespace nxs::bridge
