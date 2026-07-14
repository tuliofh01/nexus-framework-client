//==============================================================================
// nxs.android.python — Chaquopy/Djinni Python Facade (C++20 Module)
//
// == WHAT THIS MODULE DOES ==
// Singleton facade over Chaquopy via the Djinni PythonBridge. C++ never
// embeds CPython on Android — JVM owns the interpreter. The C++ side only
// delegates calls through the bridge shared_ptr.
//
// == RAII ==
// Singleton via Meyer's static (thread-safe in C++11+). The shared_ptr
// to PythonBridge is set by the Kotlin MainActivity before SDL_main().
//
// == SINGLETON PATTERN ==
// PythonEngine::instance() returns a process-wide singleton. The Kotlin
// MainActivity calls AppCore.installPythonBridge() to wire the Chaquopy
// bridge before SDL_main() starts.
//
// == DESKTOP VS ANDROID ==
// Desktop uses pybind11 embed (see nxs.desktop.python).
// Android uses Chaquopy on the JVM + a Zig JNI bridge (see zig-services/).
//==============================================================================

module;  // global module fragment

// ── Djinni bridge headers (not exported to importers) ──
#include "app_core.hpp"
#include "python_bridge.hpp"

// ── Standard library ──
#include <memory>
#include <string>
#include <utility>

export module nxs.android.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Python Delegation Facade (Android)
// ═══════════════════════════════════════════════════════════════════════════

namespace {
using PythonBridgeImpl = nxs::bridge::PythonBridge;
}  // anonymous namespace

export namespace nxs::controller {

/// Singleton facade over Chaquopy. Does not own the interpreter — it
/// delegates to the PythonBridge set by the Kotlin MainActivity.
///
/// RAII: Meyer's singleton (thread-safe in C++11+). The shared_ptr
///       to PythonBridge is non-owning from the C++ side.
export class PythonEngine {
public:
    [[nodiscard]] static auto instance() -> PythonEngine& {
        static PythonEngine engine;
        return engine;
    }

    /// Set the Chaquopy/Djinni bridge. Called by AppCore::install_python_bridge
    /// when the Kotlin MainActivity starts.
    void setBridge(std::shared_ptr<PythonBridgeImpl> bridge) {
        m_bridge = std::move(bridge);
    }

    /// Delegate to the bridge's greeting() method.
    auto greeting(const std::string& name) -> std::string {
        if (!m_bridge) {
            m_lastError =
                "PythonBridge not installed (call AppCore.installPythonBridge first)";
            return {};
        }
        const auto result = m_bridge->greeting(name);
        m_lastError.clear();
        return result;
    }

    [[nodiscard]] auto lastError() const -> const std::string& {
        return m_lastError;
    }

private:
    PythonEngine() = default;
    ~PythonEngine() = default;

    PythonEngine(const PythonEngine&) = delete;
    PythonEngine& operator=(const PythonEngine&) = delete;
    PythonEngine(PythonEngine&&) = delete;
    PythonEngine& operator=(PythonEngine&&) = delete;

    std::shared_ptr<PythonBridgeImpl> m_bridge;
    std::string m_lastError;
};

}  // namespace nxs::controller

// ── Djinni AppCore export (wires the Chaquopy bridge) ──

export namespace nxs::bridge {

void AppCore::install_python_bridge(
    const std::shared_ptr<PythonBridge>& bridge) {
    nxs::controller::PythonEngine::instance().setBridge(bridge);
}

}  // namespace nxs::bridge
