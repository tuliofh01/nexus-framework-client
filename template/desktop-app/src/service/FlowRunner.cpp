// Minimal flows.json loader — string extraction matches pack_archive.cpp style.
// v1 dispatches invoke steps to AppController / stderr log only.
#include "service/FlowRunner.hpp"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

namespace {

std::string readFile(const std::string& path) {
    std::ifstream in(path);
    if (!in) {
        return {};
    }
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

bool extractJsonBool(const std::string& json, const std::string& key, bool defaultValue) {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) {
        return defaultValue;
    }
    pos = json.find(':', pos);
    if (pos == std::string::npos) {
        return defaultValue;
    }
    const auto tail = json.substr(pos + 1);
    if (tail.find("true") == 0 || tail.find(" true") != std::string::npos) {
        return true;
    }
    if (tail.find("false") == 0 || tail.find(" false") != std::string::npos) {
        return false;
    }
    return defaultValue;
}

std::string extractJsonString(const std::string& json, const std::string& key) {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) {
        return {};
    }
    pos = json.find(':', pos);
    if (pos == std::string::npos) {
        return {};
    }
    pos = json.find('"', pos);
    if (pos == std::string::npos) {
        return {};
    }
    const auto end = json.find('"', pos + 1);
    if (end == std::string::npos) {
        return {};
    }
    return json.substr(pos + 1, end - pos - 1);
}

std::int64_t extractJsonInt64(const std::string& json, const std::string& key, std::int64_t fallback = 0) {
    const auto needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) {
        return fallback;
    }
    pos = json.find(':', pos);
    if (pos == std::string::npos) {
        return fallback;
    }
    ++pos;
    while (pos < json.size() && (json[pos] == ' ' || json[pos] == '\t')) {
        ++pos;
    }
    std::int64_t value = 0;
    bool negative = false;
    if (pos < json.size() && json[pos] == '-') {
        negative = true;
        ++pos;
    }
    while (pos < json.size() && json[pos] >= '0' && json[pos] <= '9') {
        value = value * 10 + (json[pos] - '0');
        ++pos;
    }
    return negative ? -value : value;
}

std::string substituteState(const std::string& text, int counter) {
    std::string out = text;
    const std::string token = "${state.counter}";
    auto pos = out.find(token);
    if (pos != std::string::npos) {
        out.replace(pos, token.size(), std::to_string(counter));
    }
    return out;
}

}  // namespace

namespace nxs::service {

FlowRunner::FlowRunner(controller::AppController& controller) : m_controller(controller) {}

bool FlowRunner::load(const std::string& configPath, const std::string& flowsPath) {
    m_flows.clear();
    m_active = false;

    const std::string configJson = readFile(configPath);
    if (!configJson.empty()) {
        const auto flowsBlockPos = configJson.find("\"flows\"");
        if (flowsBlockPos != std::string::npos) {
            const auto block = configJson.substr(flowsBlockPos);
            if (!extractJsonBool(block, "enabled", true)) {
                std::fprintf(stderr, "[FlowRunner] flows disabled in %s\n", configPath.c_str());
                return false;
            }
        }
    }

    const std::string flowsJson = readFile(flowsPath);
    if (flowsJson.empty()) {
        return false;
    }

    if (flowsJson.find("\"flows\"") == std::string::npos) {
        return false;
    }

    std::size_t searchFrom = 0;
    while (true) {
        const auto flowPos = flowsJson.find("\"id\"", searchFrom);
        if (flowPos == std::string::npos) {
            break;
        }
        const auto blockStart = flowsJson.rfind('{', flowPos);
        const auto blockEnd = flowsJson.find('}', flowPos);
        if (blockStart == std::string::npos || blockEnd == std::string::npos) {
            break;
        }
        const std::string block = flowsJson.substr(blockStart, blockEnd - blockStart + 1);

        LoadedFlow flow;
        flow.id = extractJsonString(block, "id");
        flow.mode = extractJsonString(block, "mode");
        flow.enabled = extractJsonBool(block, "enabled", true);
        flow.triggerType = extractJsonString(block, "trigger");
        if (flow.triggerType.empty()) {
            const auto triggerPos = block.find("\"trigger\"");
            if (triggerPos != std::string::npos) {
                const auto triggerBlock = block.substr(triggerPos);
                flow.triggerType = extractJsonString(triggerBlock, "type");
                flow.intervalMs = extractJsonInt64(triggerBlock, "ms", 0);
                flow.eventName = extractJsonString(triggerBlock, "name");
            }
        } else {
            const auto triggerPos = block.find("\"trigger\"");
            if (triggerPos != std::string::npos) {
                const auto triggerBlock = block.substr(triggerPos);
                if (flow.intervalMs == 0) {
                    flow.intervalMs = extractJsonInt64(triggerBlock, "ms", 0);
                }
                if (flow.eventName.empty()) {
                    flow.eventName = extractJsonString(triggerBlock, "name");
                }
            }
        }

        const auto stepsPos = block.find("\"steps\"");
        if (stepsPos != std::string::npos) {
            const auto stepsBlock = block.substr(stepsPos);
            flow.invokeTarget = extractJsonString(stepsBlock, "target");
            flow.invokeArg = extractJsonString(stepsBlock, "args");
            if (flow.invokeArg.empty()) {
                const auto argsPos = stepsBlock.find("\"args\"");
                if (argsPos != std::string::npos) {
                    const auto argsTail = stepsBlock.substr(argsPos);
                    const auto quote = argsTail.find('"', argsTail.find('[') + 1);
                    if (quote != std::string::npos) {
                        const auto endQuote = argsTail.find('"', quote + 1);
                        if (endQuote != std::string::npos) {
                            flow.invokeArg = argsTail.substr(quote + 1, endQuote - quote - 1);
                        }
                    }
                }
            }
        }

        if (!flow.id.empty() && flow.enabled) {
            m_flows.push_back(flow);
        }

        searchFrom = blockEnd + 1;
        if (searchFrom >= flowsJson.size()) {
            break;
        }
    }

    m_active = !m_flows.empty();
    if (m_active) {
        std::fprintf(stderr, "[FlowRunner] loaded %zu flow(s) from %s\n", m_flows.size(), flowsPath.c_str());
    }
    return m_active;
}

void FlowRunner::onStartup() {
    if (!m_active) {
        return;
    }
    for (const auto& flow : m_flows) {
        if (flow.triggerType == "startup") {
            runFlow(flow);
        }
    }
}

void FlowRunner::onEvent(const std::string& eventName) {
    if (!m_active) {
        return;
    }
    for (const auto& flow : m_flows) {
        if (flow.triggerType == "event" && flow.eventName == eventName) {
            runFlow(flow);
        }
    }
}

void FlowRunner::tick(std::uint64_t frameDeltaMs) {
    if (!m_active) {
        return;
    }
    m_elapsedMs += frameDeltaMs;
    for (const auto& flow : m_flows) {
        if (flow.triggerType != "interval" || flow.intervalMs <= 0) {
            continue;
        }
        if (m_elapsedMs >= static_cast<std::uint64_t>(flow.intervalMs)) {
            runFlow(flow);
            m_elapsedMs = 0;
            break;
        }
    }
}

void FlowRunner::runFlow(const LoadedFlow& flow) {
    if (!flow.invokeTarget.empty()) {
        const int counter = m_controller.model().counter();
        dispatchInvoke(flow.invokeTarget, substituteState(flow.invokeArg, counter));
    }
}

void FlowRunner::dispatchInvoke(const std::string& target, const std::string& arg) {
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
    std::fprintf(stderr, "[FlowRunner] stub invoke: %s(%s)\n", target.c_str(), arg.c_str());
}

}  // namespace nxs::service
