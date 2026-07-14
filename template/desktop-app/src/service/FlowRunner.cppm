//==============================================================================
// nxs.desktop.flow — Runtime Flow Runner (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Loads `flows/flows.json` (optional) and dispatches in-process automations:
// background interval loops, event triggers, and startup flows. A NO-OP when
// flows.json is missing or flows are disabled in nxs_config.json.
//
// ════════════════════════════════════════════════════════════════════════════
// RAII (Resource Acquisition Is Initialization)
// ════════════════════════════════════════════════════════════════════════════
//
// FlowRunner owns its flow definitions by value (std::vector<LoadedFlow>).
// No heap allocations escape the class. Construction is cheap; load() parses
// once, tick() dispatches lazily.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from AppController&
// noexcept            — guarantees no exceptions from construction
// = delete            — Rule of Five: non-copyable, non-movable
// [[nodiscard]]       — return values cannot be ignored
// trailing return    — auto fn() -> Type syntax (uniform declarations)
// const auto&        — range-for with const reference to avoid copies
// std::string_view   — non-owning constexpr-capable string params
// std::move          — transfers ownership into vector (push_back)
// static_cast<T>     — explicit numeric conversion (int64 to uint64)
// {} brace-init      — uniform initialization for struct members
// constexpr          — compile-time constants (token strings)
// std::string::npos  — standard sentinel for "not found"
//
// ════════════════════════════════════════════════════════════════════════════
// JSON PARSING
// ════════════════════════════════════════════════════════════════════════════
//
// v1 uses lightweight string extraction (no nlohmann) to keep the template
// dependency-light. Schema correctness is enforced at generation time by
// FlowsValidator in the Kotlin :core module.
//
// ════════════════════════════════════════════════════════════════════════════
// TRIGGER TYPES
// ════════════════════════════════════════════════════════════════════════════
//   * startup  — fires once when the app launches
//   * event    — fires on a named event (e.g. "app.ready", "curve.added")
//   * interval — fires every N ms while the app runs
//   * manual   — fired by Lua/C++ code calling runFlow() directly
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
#include <algorithm>    // std::find, std::clamp (though constexpr here)
#include <cstdio>       // std::fprintf for logging
#include <cstdint>      // std::int64_t, std::uint64_t for intervals
#include <fstream>      // std::ifstream for file reading
#include <sstream>      // std::ostringstream for rdbuf slurp
#include <string>       // std::string for paths and extracted values
#include <string_view>  // std::string_view for constexpr token matching
#include <utility>      // std::move for ownership transfer
#include <vector>       // std::vector for flow storage

export module nxs.desktop.flow;

// ── Import peer modules ──
import nxs.desktop.controller;

// ═══════════════════════════════════════════════════════════════════════════
// Helper utilities (anonymous namespace — NOT exported)
// ═══════════════════════════════════════════════════════════════════════════

namespace {

/// Slurp an entire file into a std::string. Returns empty string on failure.
///
/// Modern C++ patterns:
///   - std::ifstream: RAII file handle — closes automatically on destructor
///   - std::ostringstream + rdbuf(): one-line file slurp with no manual
///     buffer management
///   - [[nodiscard]]: callers must check the result
[[nodiscard]] auto readFile(const std::string_view path) -> std::string {
    std::ifstream in{path.data()};  // {} brace-init for ifstream
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();  // one-liner: read entire stream into string
    return ss.str();
}

/// Extract a boolean value for `key` from a JSON block.
///
/// String manipulation approach (no JSON parser dependency):
///   1. Find `"key"` in the block
///   2. Find `:` after the key
///   3. Check if the tail contains "true" or "false"
///
/// [[nodiscard]]: return value must be checked.
/// std::string_view: for the key parameter (avoids allocation).
/// constexpr values: needle is built at compile time.
[[nodiscard]] auto extractJsonBool(const std::string& json,
                                   const std::string_view key,
                                   bool defaultValue) -> bool {
    const auto needle = std::string{"\""} + key.data() + "\"";
    const auto pos = json.find(needle);
    if (pos == std::string::npos) return defaultValue;
    const auto colon = json.find(':', pos);
    if (colon == std::string::npos) return defaultValue;
    const auto tail = json.substr(colon + 1);
    // Check for "true" at the start or after whitespace
    if (tail.find("true") == 0 ||
        tail.find(" true") != std::string::npos) return true;
    if (tail.find("false") == 0 ||
        tail.find(" false") != std::string::npos) return false;
    return defaultValue;
}

/// Extract a string value for `key` from a JSON block.
///
/// Looks for `"key": "value"` and returns the value without quotes.
/// std::string::npos: standard sentinel for "not found" — used here
/// as an early-exit check.
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

/// Extract a 64-bit integer value for `key` from a JSON block.
///
/// Manual parsing without strtoll/stoi (keeping the module dependency-free).
/// Handles negative numbers. static_cast ensures safe char-to-int64_t.
///
/// [[nodiscard]]: return value must be checked.
/// constexpr: numeric operations are compile-time evaluable.
[[nodiscard]] auto extractJsonInt64(const std::string& json,
                                    const std::string_view key,
                                    std::int64_t fallback = 0) -> std::int64_t {
    const auto needle = std::string{"\""} + key.data() + "\"";
    const auto pos = json.find(needle);
    if (pos == std::string::npos) return fallback;
    const auto colon = json.find(':', pos);
    if (colon == std::string::npos) return fallback;

    // Skip whitespace after colon
    auto idx = colon + 1;
    while (idx < json.size() &&
           (json[idx] == ' ' || json[idx] == '\t')) {
        ++idx;
    }

    // Handle negative numbers
    bool negative = false;
    if (idx < json.size() && json[idx] == '-') {
        negative = true;
        ++idx;
    }

    // Parse digits
    std::int64_t value = 0;
    while (idx < json.size() && json[idx] >= '0' && json[idx] <= '9') {
        value = value * 10 + static_cast<std::int64_t>(json[idx] - '0');
        ++idx;
    }

    return negative ? -value : value;
}

/// Substitute `${state.counter}` tokens in a string with an actual value.
///
/// constexpr std::string_view: the token pattern is a compile-time constant.
/// std::string::replace: in-place token substitution.
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
///
/// Uses {} brace-initialization for all numeric members so there are no
/// uninitialized values even if the JSON parser doesn't set them.
export struct LoadedFlow {
    std::string id;
    std::string mode;
    std::string triggerType;
    std::int64_t intervalMs{0};   ///< ms between interval triggers (0 = not set)
    std::string eventName;        ///< event name for event triggers
    std::string invokeTarget;     ///< e.g. "nxs.increment", "nxs.log"
    std::string invokeArg;        ///< argument string for the invoke target
    bool enabled{true};           ///< false if explicitly disabled in JSON
};

/// Loads flows.json and dispatches automations. Created in main() and
/// ticked every frame. A NO-OP when flows are disabled or absent.
///
/// RAII: owns LoadedFlow definitions by value; no heap escapes.
export class FlowRunner {
public:
    /// explicit: prevents implicit conversion from AppController&.
    /// noexcept: guarantees no exceptions from construction.
    explicit FlowRunner(controller::AppController& controller) noexcept
        : m_controller{controller} {}  // {} brace-init for reference

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Non-copyable: holds a reference to the controller. Copying would
    // create a dangling-reference hazard if the original goes out of scope.

    FlowRunner(const FlowRunner&) = delete;
    FlowRunner& operator=(const FlowRunner&) = delete;
    FlowRunner(FlowRunner&&) = delete;
    FlowRunner& operator=(FlowRunner&&) = delete;

    ~FlowRunner() = default;

    /// Parse nxs_config.json for the enabled flag, then scan flows.json
    /// for trigger blocks. Returns true if at least one flow is active.
    ///
    /// [[nodiscard]]: caller must check if flows actually loaded.
    /// std::string_view: default params avoid heap allocation for literals.
    [[nodiscard]] auto load(
        const std::string_view configPath = "nxs_config.json",
        const std::string_view flowsPath = "flows/flows.json") -> bool {
        m_flows.clear();
        m_active = false;

        // ── Check config-level enabled flag ────────────────────────────
        //
        // nxs_config.json has a "flows": { "enabled": true/false } block.
        // If disabled there, we skip loading entirely — even if flows.json
        // exists.

        const std::string configJson = readFile(configPath);
        if (!configJson.empty()) {
            const auto flowsBlockPos = configJson.find("\"flows\"");
            if (flowsBlockPos != std::string::npos) {
                const auto block = configJson.substr(flowsBlockPos);
                if (!extractJsonBool(block, "enabled", true)) {
                    std::fprintf(stderr,
                        "[FlowRunner] flows disabled in %s\n",
                        configPath.data());
                    return false;
                }
            }
        }

        // ── Parse flows.json ───────────────────────────────────────────
        //
        // Lightweight JSON scanning: find each "id" field, extract the
        // surrounding {} block, pull fields by key.

        const std::string flowsJson = readFile(flowsPath);
        if (flowsJson.empty()) return false;

        // Quick check: is there a "flows" key at all?
        if (flowsJson.find("\"flows\"") == std::string::npos) return false;

        // Iterate over flow blocks by finding "id" keys
        std::size_t searchFrom = 0;
        while (true) {
            const auto flowPos = flowsJson.find("\"id\"", searchFrom);
            if (flowPos == std::string::npos) break;

            // Find the enclosing {} block
            const auto blockStart = flowsJson.rfind('{', flowPos);
            const auto blockEnd   = flowsJson.find('}', flowPos);
            if (blockStart == std::string::npos ||
                blockEnd == std::string::npos) break;

            // Extract the block as a substring
            const std::string block = flowsJson.substr(
                blockStart, blockEnd - blockStart + 1);

            // ── Extract fields from block ──────────────────────────────
            LoadedFlow flow;
            flow.id      = extractJsonString(block, "id");
            flow.mode    = extractJsonString(block, "mode");
            flow.enabled = extractJsonBool(block, "enabled", true);

            // Trigger block — may be nested
            flow.triggerType = extractJsonString(block, "trigger");
            if (flow.triggerType.empty()) {
                // Check for nested "trigger": { "type": "...", ... }
                const auto triggerPos = block.find("\"trigger\"");
                if (triggerPos != std::string::npos) {
                    const auto tb = block.substr(triggerPos);
                    flow.triggerType  = extractJsonString(tb, "type");
                    flow.intervalMs   = extractJsonInt64(tb, "ms", 0);
                    flow.eventName    = extractJsonString(tb, "name");
                }
            }

            // Steps: pull target and args from the first step
            const auto stepsPos = block.find("\"steps\"");
            if (stepsPos != std::string::npos) {
                const auto sb = block.substr(stepsPos);
                flow.invokeTarget = extractJsonString(sb, "target");
                flow.invokeArg    = extractJsonString(sb, "args");
                // Fallback: args may be an array ["value"]
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

            // Only add enabled flows with non-empty IDs
            if (!flow.id.empty() && flow.enabled) {
                m_flows.push_back(std::move(flow));
            }

            searchFrom = blockEnd + 1;
            if (searchFrom >= flowsJson.size()) break;
        }

        m_active = !m_flows.empty();
        if (m_active) {
            std::fprintf(stderr,
                "[FlowRunner] loaded %zu flow(s) from %s\n",
                m_flows.size(), flowsPath.data());
        }
        return m_active;
    }

    /// Fire all flows with trigger type "startup".
    void onStartup() {
        if (!m_active) return;
        // Range-for with const auto&: avoids copying LoadedFlow structs.
        for (const auto& flow : m_flows) {
            if (flow.triggerType == "startup") runFlow(flow);
        }
    }

    /// Fire flows whose trigger type is "event" and eventName matches.
    void onEvent(const std::string_view eventName) {
        if (!m_active) return;
        for (const auto& flow : m_flows) {
            if (flow.triggerType == "event" &&
                flow.eventName == eventName) runFlow(flow);
        }
    }

    /// Called every frame. Fires the first interval flow whose timer
    /// has elapsed (one per tick to avoid stampede).
    void tick(const std::uint64_t frameDeltaMs) {
        if (!m_active) return;
        m_elapsedMs += frameDeltaMs;
        for (const auto& flow : m_flows) {
            if (flow.triggerType != "interval" ||
                flow.intervalMs <= 0) continue;
            if (m_elapsedMs >=
                static_cast<std::uint64_t>(flow.intervalMs)) {
                runFlow(flow);
                m_elapsedMs = 0;
                break;  // one interval per tick
            }
        }
    }

    /// [[nodiscard]]: caller should check if flows are available.
    /// noexcept: simple getter, no allocation.
    [[nodiscard]] auto isActive() const noexcept -> bool {
        return m_active;
    }

private:
    /// Execute a single flow: substitute state tokens, then dispatch.
    void runFlow(const LoadedFlow& flow) {
        if (!flow.invokeTarget.empty()) {
            const auto counter = m_controller.model().counter();
            dispatchInvoke(flow.invokeTarget,
                           substituteState(flow.invokeArg, counter));
        }
    }

    /// Route a (target, arg) pair to the matching controller method.
    ///
    /// This is where the "flow automation" actually happens — it maps
    /// string command names to C++ method calls.
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
    std::vector<LoadedFlow> m_flows;    ///< Parsed flow definitions
    bool m_active{false};               ///< True after load() finds flows
    std::uint64_t m_elapsedMs{0};       ///< Accumulated frame time for intervals
};

}  // namespace nxs::service
