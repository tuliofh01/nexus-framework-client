//==============================================================================
// nxs.desktop.flow — Runtime Flow Runner (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Loads `flows/flows.json` (optional) and dispatches in-process automations:
// background interval loops, event triggers, and startup flows. A NO-OP when
// flows.json is missing or flows are disabled in nxs_config.json.
//
// == RAII ==
// FlowRunner owns its flow definitions by value (std::vector<LoadedFlow>).
// No heap allocations escape the class. Construction is cheap; load() parses
// once, tick() dispatches lazily.
//
// == JSON PARSING ==
// v1 uses lightweight string extraction (no nlohmann) to keep the template
// dependency-light. Schema correctness is enforced at generation time by
// FlowsValidator in the Kotlin :core module.
//
// == TRIGGER TYPES ==
//   * startup  — fires once when the app launches
//   * event    — fires on a named event (e.g. "app.ready", "curve.added")
//   * interval — fires every N ms while the app runs
//   * manual   — fired by Lua/C++ code calling runFlow() directly
//==============================================================================

module;  // global module fragment

// ── Standard library ──
#include <algorithm>
#include <cstdio>
#include <cstdint>
#include <fstream>
#include <sstream>
#include <string>
#include <string_view>
#include <utility>
#include <vector>

export module nxs.desktop.flow;

// ── Import peer modules ──
import nxs.desktop.controller;

// ═══════════════════════════════════════════════════════════════════════════
// Helper utilities (anonymous namespace, not exported)
// ═══════════════════════════════════════════════════════════════════════════

namespace {

[[nodiscard]] auto readFile(const std::string& path) -> std::string {
    std::ifstream in(path);
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

[[nodiscard]] auto extractJsonBool(const std::string& json,
                                   const std::string& key,
                                   bool defaultValue) -> bool {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return defaultValue;
    pos = json.find(':', pos);
    if (pos == std::string::npos) return defaultValue;
    const auto tail = json.substr(pos + 1);
    if (tail.find("true") == 0 ||
        tail.find(" true") != std::string::npos) return true;
    if (tail.find("false") == 0 ||
        tail.find(" false") != std::string::npos) return false;
    return defaultValue;
}

[[nodiscard]] auto extractJsonString(const std::string& json,
                                     const std::string& key) -> std::string {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return {};
    pos = json.find(':', pos);
    if (pos == std::string::npos) return {};
    pos = json.find('"', pos);
    if (pos == std::string::npos) return {};
    const auto end = json.find('"', pos + 1);
    if (end == std::string::npos) return {};
    return json.substr(pos + 1, end - pos - 1);
}

[[nodiscard]] auto extractJsonInt64(const std::string& json,
                                    const std::string& key,
                                    std::int64_t fallback = 0) -> std::int64_t {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return fallback;
    pos = json.find(':', pos);
    if (pos == std::string::npos) return fallback;
    ++pos;
    while (pos < json.size() &&
           (json[pos] == ' ' || json[pos] == '\t')) ++pos;
    std::int64_t value = 0;
    bool negative = false;
    if (pos < json.size() && json[pos] == '-') {
        negative = true;
        ++pos;
    }
    while (pos < json.size() &&
           json[pos] >= '0' && json[pos] <= '9') {
        value = value * 10 + static_cast<std::int64_t>(json[pos] - '0');
        ++pos;
    }
    return negative ? -value : value;
}

[[nodiscard]] auto substituteState(const std::string& text,
                                   int counter) -> std::string {
    std::string out = text;
    constexpr std::string_view token = "${state.counter}";
    const auto pos = out.find(token);
    if (pos != std::string::npos) {
        out.replace(pos, token.size(), std::to_string(counter));
    }
    return out;
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// nxs::service — Runtime Flow Automation Engine
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::service {

/// One loaded flow definition from flows.json. Populated by load().
export struct LoadedFlow {
    std::string id;
    std::string mode;
    std::string triggerType;
    std::int64_t intervalMs = 0;
    std::string eventName;
    std::string invokeTarget;
    std::string invokeArg;
    bool enabled = true;
};

/// Loads flows.json and dispatches automations. Created in main() and
/// ticked every frame. A NO-OP when flows are disabled or absent.
///
/// RAII: owns LoadedFlow definitions by value; no heap escapes.
export class FlowRunner {
public:
    explicit FlowRunner(controller::AppController& controller) noexcept
        : m_controller(controller) {}

    /// Non-copyable — references binding to controller.
    FlowRunner(const FlowRunner&) = delete;
    FlowRunner& operator=(const FlowRunner&) = delete;
    FlowRunner(FlowRunner&&) = delete;
    FlowRunner& operator=(FlowRunner&&) = delete;

    ~FlowRunner() = default;

    /// Parse nxs_config.json for the enabled flag, then scan flows.json
    /// for trigger blocks. Returns true if at least one flow is active.
    auto load(const std::string& configPath = "nxs_config.json",
              const std::string& flowsPath = "flows/flows.json") -> bool {
        m_flows.clear();
        m_active = false;

        // Check config-level enabled flag
        const std::string configJson = readFile(configPath);
        if (!configJson.empty()) {
            const auto flowsBlockPos = configJson.find("\"flows\"");
            if (flowsBlockPos != std::string::npos) {
                const auto block = configJson.substr(flowsBlockPos);
                if (!extractJsonBool(block, "enabled", true)) {
                    std::fprintf(stderr,
                        "[FlowRunner] flows disabled in %s\n",
                        configPath.c_str());
                    return false;
                }
            }
        }

        const std::string flowsJson = readFile(flowsPath);
        if (flowsJson.empty()) return false;
        if (flowsJson.find("\"flows\"") == std::string::npos) return false;

        // Parse each flow block by finding "id" keys
        std::size_t searchFrom = 0;
        while (true) {
            const auto flowPos = flowsJson.find("\"id\"", searchFrom);
            if (flowPos == std::string::npos) break;

            const auto blockStart = flowsJson.rfind('{', flowPos);
            const auto blockEnd   = flowsJson.find('}', flowPos);
            if (blockStart == std::string::npos ||
                blockEnd == std::string::npos) break;

            const std::string block = flowsJson.substr(
                blockStart, blockEnd - blockStart + 1);

            LoadedFlow flow;
            flow.id      = extractJsonString(block, "id");
            flow.mode    = extractJsonString(block, "mode");
            flow.enabled = extractJsonBool(block, "enabled", true);

            // Nested trigger block
            flow.triggerType = extractJsonString(block, "trigger");
            if (flow.triggerType.empty()) {
                const auto triggerPos = block.find("\"trigger\"");
                if (triggerPos != std::string::npos) {
                    const auto tb = block.substr(triggerPos);
                    flow.triggerType  = extractJsonString(tb, "type");
                    flow.intervalMs   = extractJsonInt64(tb, "ms", 0);
                    flow.eventName    = extractJsonString(tb, "name");
                }
            } else {
                const auto triggerPos = block.find("\"trigger\"");
                if (triggerPos != std::string::npos) {
                    const auto tb = block.substr(triggerPos);
                    if (flow.intervalMs == 0)
                        flow.intervalMs = extractJsonInt64(tb, "ms", 0);
                    if (flow.eventName.empty())
                        flow.eventName = extractJsonString(tb, "name");
                }
            }

            // Steps: pull target and args from the first step
            const auto stepsPos = block.find("\"steps\"");
            if (stepsPos != std::string::npos) {
                const auto sb = block.substr(stepsPos);
                flow.invokeTarget = extractJsonString(sb, "target");
                flow.invokeArg    = extractJsonString(sb, "args");
                if (flow.invokeArg.empty()) {
                    const auto argsPos = sb.find("\"args\"");
                    if (argsPos != std::string::npos) {
                        const auto tail = sb.substr(argsPos);
                        const auto q = tail.find('"',
                            tail.find('[') + 1);
                        if (q != std::string::npos) {
                            const auto eq = tail.find('"', q + 1);
                            if (eq != std::string::npos)
                                flow.invokeArg = tail.substr(
                                    q + 1, eq - q - 1);
                        }
                    }
                }
            }

            if (!flow.id.empty() && flow.enabled)
                m_flows.push_back(std::move(flow));

            searchFrom = blockEnd + 1;
            if (searchFrom >= flowsJson.size()) break;
        }

        m_active = !m_flows.empty();
        if (m_active) {
            std::fprintf(stderr,
                "[FlowRunner] loaded %zu flow(s) from %s\n",
                m_flows.size(), flowsPath.c_str());
        }
        return m_active;
    }

    /// Fire all flows with trigger type "startup".
    void onStartup() {
        if (!m_active) return;
        for (const auto& flow : m_flows) {
            if (flow.triggerType == "startup") runFlow(flow);
        }
    }

    /// Fire flows whose trigger type is "event" and eventName matches.
    void onEvent(const std::string& eventName) {
        if (!m_active) return;
        for (const auto& flow : m_flows) {
            if (flow.triggerType == "event" &&
                flow.eventName == eventName) runFlow(flow);
        }
    }

    /// Called every frame. Fires the first interval flow whose timer
    /// has elapsed (one per tick to avoid stampede).
    void tick(std::uint64_t frameDeltaMs) {
        if (!m_active) return;
        m_elapsedMs += frameDeltaMs;
        for (const auto& flow : m_flows) {
            if (flow.triggerType != "interval" ||
                flow.intervalMs <= 0) continue;
            if (m_elapsedMs >=
                static_cast<std::uint64_t>(flow.intervalMs)) {
                runFlow(flow);
                m_elapsedMs = 0;
                break;
            }
        }
    }

    [[nodiscard]] auto isActive() const noexcept -> bool { return m_active; }

private:
    void runFlow(const LoadedFlow& flow) {
        if (!flow.invokeTarget.empty()) {
            const auto counter = m_controller.model().counter();
            dispatchInvoke(flow.invokeTarget,
                           substituteState(flow.invokeArg, counter));
        }
    }

    void dispatchInvoke(const std::string& target,
                        const std::string& arg) {
        if (target == "nxs.increment") {
            m_controller.increment();
            return;
        }
        if (target == "nxs.reset") {
            m_controller.reset();
            return;
        }
        if (target == "nxs.log") {
            std::fprintf(stderr, "[FlowRunner] %s\n", arg.c_str());
            return;
        }
        std::fprintf(stderr, "[FlowRunner] stub invoke: %s(%s)\n",
                     target.c_str(), arg.c_str());
    }

    controller::AppController& m_controller;
    std::vector<LoadedFlow> m_flows;
    bool m_active = false;
    std::uint64_t m_elapsedMs = 0;
};

}  // namespace nxs::service
