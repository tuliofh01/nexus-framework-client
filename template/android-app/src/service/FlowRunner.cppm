//==============================================================================
// nxs.android.flow — Runtime Flow Runner (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Android v1 stub — loads `flows/flows.json` for logging and enabled-check
// parity with the desktop FlowRunner. Full invoke dispatch lands with
// Chaquopy bridge parity in a later phase.
//
// == DESKTOP VS ANDROID ==
// Desktop FlowRunner dispatches invoke targets (nxs.increment, nxs.reset,
// nxs.log) directly. Android stubs the invoke dispatch until the Chaquopy
// bridge is fully wired. Logging and enable/disable checks are identical.
//==============================================================================

module;  // global module fragment

// ── Standard library ──
#include <cstdio>
#include <cstdint>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

export module nxs.android.flow;

// ── Import peer modules ──
import nxs.android.controller;

// ═══════════════════════════════════════════════════════════════════════════
// Helper utilities (anonymous namespace, not exported)
// ═══════════════════════════════════════════════════════════════════════════

namespace {

std::string readFile(const std::string& path) {
    std::ifstream in(path);
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

std::string extractJsonString(const std::string& json,
                              const std::string& key) {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return {};
    pos = json.find(':', pos);
    pos = json.find('"', pos);
    const auto end = json.find('"', pos + 1);
    return json.substr(pos + 1, end - pos - 1);
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// nxs::service — Runtime Flow Automation Engine (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::service {

/// One loaded flow definition from flows.json.
export struct LoadedFlow {
    std::string id;
    std::string triggerType;
    std::int64_t intervalMs = 0;
    std::string eventName;
    std::string invokeTarget;
    std::string invokeArg;
    bool enabled = true;
};

/// Loads flows.json and checks enabled state. Invoke dispatch is stubbed
/// until Chaquopy bridge parity. Created in main() and ticked every frame.
export class FlowRunner {
public:
    explicit FlowRunner(controller::AppController& controller)
        : m_controller(controller) {}

    /// Parse flows.json and check if flows are enabled. Returns true if
    /// at least one flow is active.
    bool load(const std::string& configPath = "nxs_config.json",
              const std::string& flowsPath = "flows/flows.json") {
        m_flows.clear();
        const std::string flowsJson = readFile(flowsPath);
        if (flowsJson.empty()) return false;
        m_active = flowsJson.find("\"flows\"") != std::string::npos;
        return m_active;
    }

    /// Log startup on first flow activation.
    void onStartup() {
        std::fprintf(stderr, "[FlowRunner] {{projectName}} started\n");
    }

    /// Log when the app is ready.
    void onEvent(const std::string& eventName) {
        if (eventName == "app.ready") {
            std::fprintf(stderr, "[FlowRunner] App ready\n");
        }
    }

    /// Stubbed on Android — full dispatch lands with Chaquopy bridge parity.
    void tick(std::uint64_t) {}

    bool isActive() const { return m_active; }

private:
    void runFlow(const LoadedFlow&) {}
    void dispatchInvoke(const std::string& target,
                        const std::string& arg) {
        if (target == "nxs.log")
            std::fprintf(stderr, "[FlowRunner] %s\n", arg.c_str());
    }

    controller::AppController& m_controller;
    std::vector<LoadedFlow> m_flows;
    bool m_active = false;
    std::uint64_t m_elapsedMs = 0;
};

}  // namespace nxs::service
