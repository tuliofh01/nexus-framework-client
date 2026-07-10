#include "service/FlowRunner.hpp"

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

namespace {

std::string readFile(const std::string& path) {
    std::ifstream in(path);
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

std::string extractJsonString(const std::string& json, const std::string& key) {
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

}  // namespace

namespace nxs::service {

FlowRunner::FlowRunner(controller::PlotController& controller) : m_controller(controller) {}

bool FlowRunner::load(const std::string&, const std::string& flowsPath) {
    m_flows.clear();
    const std::string flowsJson = readFile(flowsPath);
    if (flowsJson.empty()) return false;

    LoadedFlow flow;
    flow.id = extractJsonString(flowsJson, "id");
    flow.triggerType = "interval";
    flow.intervalMs = 5000;
    flow.invokeTarget = "nxs.set_samples";
    flow.invokeArg = "512";
    flow.enabled = true;
    if (!flow.id.empty()) {
        m_flows.push_back(flow);
    }
    m_active = !m_flows.empty();
    return m_active;
}

void FlowRunner::onStartup() {}
void FlowRunner::onEvent(const std::string& eventName) {
    if (eventName == "curve.added") {
        std::fprintf(stderr, "[FlowRunner] Added curve\n");
    }
}

void FlowRunner::tick(std::uint64_t frameDeltaMs) {
    if (!m_active) return;
    m_elapsedMs += frameDeltaMs;
    for (const auto& flow : m_flows) {
        if (flow.triggerType == "interval" && m_elapsedMs >= static_cast<std::uint64_t>(flow.intervalMs)) {
            runFlow(flow);
            m_elapsedMs = 0;
            break;
        }
    }
}

void FlowRunner::runFlow(const LoadedFlow& flow) {
    dispatchInvoke(flow.invokeTarget, flow.invokeArg);
}

void FlowRunner::dispatchInvoke(const std::string& target, const std::string& arg) {
    if (target == "nxs.set_samples") {
        m_controller.setSampleCount(std::stoi(arg));
    }
}

}  // namespace nxs::service
