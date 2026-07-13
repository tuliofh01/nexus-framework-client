#include "service/FlowRunner.hpp"

// Android v1 stub — full invoke dispatch lands with Chaquopy bridge parity (Phase 2).
// Still loads flows.json so logging and enabled checks match desktop behavior.

#include <cstdio>
#include <fstream>
#include <sstream>

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
    pos = json.find('"', pos);
    const auto end = json.find('"', pos + 1);
    return json.substr(pos + 1, end - pos - 1);
}

}  // namespace

namespace nxs::service {

FlowRunner::FlowRunner(controller::AppController& controller) : m_controller(controller) {}

bool FlowRunner::load(const std::string&, const std::string& flowsPath) {
    m_flows.clear();
    const std::string flowsJson = readFile(flowsPath);
    if (flowsJson.empty()) return false;
    m_active = flowsJson.find("\"flows\"") != std::string::npos;
    return m_active;
}

void FlowRunner::onStartup() {
    std::fprintf(stderr, "[FlowRunner] {{projectName}} started\n");
}

void FlowRunner::onEvent(const std::string& eventName) {
    if (eventName == "app.ready") {
        std::fprintf(stderr, "[FlowRunner] App ready\n");
    }
}

void FlowRunner::tick(std::uint64_t) {}

void FlowRunner::runFlow(const LoadedFlow&) {}
void FlowRunner::dispatchInvoke(const std::string& target, const std::string& arg) {
    if (target == "nxs.log") std::fprintf(stderr, "[FlowRunner] %s\n", arg.c_str());
}

}  // namespace nxs::service
