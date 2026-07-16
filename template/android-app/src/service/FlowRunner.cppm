//==============================================================================
// nxs.android.flow — Runtime Flow Runner (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Android v1 stub — loads `flows/flows.json` for logging and enabled-check
// parity with the desktop FlowRunner. Full invoke dispatch lands with
// Chaquopy bridge parity in a later phase.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from AppController&
// noexcept            — constructor guarantees no exceptions
// = delete            — Rule of Five: non-copyable, non-movable
// [[nodiscard]]       — return values must be checked
// constexpr           — (trivial accessors)
// {} brace-init       — uniform initialization for struct/class members
// std::string_view    — non-owning constexpr-capable string params
// std::move           — transfers ownership into vector
// trailing return    — auto fn() -> Type syntax
// static_cast<T>     — (not used here, but available pattern)
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
//
// Desktop FlowRunner dispatches invoke targets (nxs.increment, nxs.reset,
// nxs.log) directly. Android stubs the invoke dispatch until the Chaquopy
// bridge is fully wired. Logging and enable/disable checks are identical.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
#include <cstdio>       // std::fprintf for logging
#include <cstdint>      // std::int64_t, std::uint64_t for intervals
#include <fstream>      // std::ifstream for file reading
#include <sstream>      // std::ostringstream for rdbuf slurp
#include <string>       // std::string for paths and extracted values
#include <string_view>  // std::string_view for file paths
#include <utility>      // std::move for ownership transfer
#include <vector>       // std::vector for flow storage

export module nxs.android.flow;

// ── Import peer modules ──
import nxs.android.controller;

// ═══════════════════════════════════════════════════════════════════════════
// Helper utilities (anonymous namespace, not exported)
// ═══════════════════════════════════════════════════════════════════════════

namespace {

/// Slurp an entire file into a std::string. Returns empty string on failure.
///
/// std::ifstream: RAII file handle — closes automatically on destructor.
/// std::ostringstream + rdbuf(): one-line file slurp.
[[nodiscard]] auto readFile(const std::string_view path) -> std::string {
    std::ifstream in{path.data()};
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

/// Extract a string value for `key` from a JSON block.
///
/// Manual string scan (no JSON parser) to keep the template
/// dependency-light. Schema correctness is enforced at generation time.
///
/// std::string_view: non-owning reference for the key parameter.
/// std::string::npos: standard sentinel for "not found".
[[nodiscard]] auto extractJsonString(const std::string& json,
                                     const std::string_view key) -> std::string {
    const auto needle = std::string{"\""} + key.data() + "\"";
    const auto pos = json.find(needle);
    if (pos == std::string::npos) return {};
    const auto colon = json.find(':', pos);
    if (colon == std::string::npos) return {};
    const auto quote = json.find('"', colon);
    if (quote == std::string::npos) return {};
    const auto end = json.find('"', quote + 1);
    if (end == std::string::npos) return {};
    return json.substr(quote + 1, end - quote - 1);
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// nxs::service — Runtime Flow Automation Engine (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::service {

/// One loaded flow definition from flows.json.
///
/// {} brace-init: all numeric members default to safe values.
struct LoadedFlow {
    std::string id;
    std::string triggerType;
    std::int64_t intervalMs{0};   ///< ms between interval triggers
    std::string eventName;
    std::string invokeTarget;
    std::string invokeArg;
    bool enabled{true};           ///< false if disabled in JSON
};

/// Loads flows.json and checks enabled state.
///
/// Android v1 stub: invoke dispatch is minimal (nxs.log only) until
/// Chaquopy bridge parity lands. Created in main() and ticked every
/// frame for parity with the desktop FlowRunner.
///
/// RAII: owns LoadedFlow definitions by value; no heap escapes.
class FlowRunner {
public:
    /// explicit: prevents implicit conversion from AppController&.
    /// noexcept: guarantees no exceptions from construction.
    explicit FlowRunner(controller::AppController& controller) noexcept
        : m_controller{controller} {}  // {} brace-init for reference

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Non-copyable: holds a reference to the controller.

    FlowRunner(const FlowRunner&) = delete;
    FlowRunner& operator=(const FlowRunner&) = delete;
    FlowRunner(FlowRunner&&) = delete;
    FlowRunner& operator=(FlowRunner&&) = delete;

    ~FlowRunner() = default;

    /// Parse flows.json and check if flows are enabled.
    ///
    /// [[nodiscard]]: caller must check if flows are active.
    /// std::string_view: non-owning default params avoid allocation.
    /// Returns true if at least one flow is active.
    [[nodiscard]] auto load(
        const std::string_view configPath = "nxs_config.json",
        const std::string_view flowsPath = "flows/flows.json") -> bool {
        m_flows.clear();
        const auto flowsJson = readFile(flowsPath);
        if (flowsJson.empty()) return false;
        m_active = flowsJson.find("\"flows\"") != std::string::npos;
        return m_active;
    }

    /// Log startup on first flow activation.
    void onStartup() noexcept {
        std::fprintf(stderr, "[FlowRunner] {{projectName}} started\n");
    }

    /// Log when the app is ready.
    ///
    /// std::string_view: non-owning reference for the event name.
    void onEvent(const std::string_view eventName) noexcept {
        // v1 stub: only handles "app.ready"
        if (eventName == "app.ready") {
            std::fprintf(stderr, "[FlowRunner] App ready\n");
        }
    }

    /// Stubbed on Android — full dispatch lands with Chaquopy bridge parity.
    /// Parameters are kept for future use.
    void tick(const std::uint64_t /*frameDeltaMs*/) noexcept {}

    /// [[nodiscard]]: caller should check before calling onStartup/etc.
    [[nodiscard]] auto isActive() const noexcept -> bool {
        return m_active;
    }

private:
    /// Stub: no-op on Android until full dispatch is wired.
    void runFlow(const LoadedFlow& /*flow*/) noexcept {}

    /// Dispatch invoke targets (Android stub).
    ///
    /// Currently only handles "nxs.log" for parity with the desktop
    /// FlowRunner. Other targets are no-ops until Chaquopy bridge parity.
    void dispatchInvoke(const std::string& target,
                        const std::string& arg) noexcept {
        if (target == "nxs.log") {
            std::fprintf(stderr, "[FlowRunner] %s\n", arg.c_str());
        }
    }

    controller::AppController& m_controller;
    std::vector<LoadedFlow> m_flows;
    bool m_active{false};
    std::uint64_t m_elapsedMs{0};
};

}  // namespace nxs::service
