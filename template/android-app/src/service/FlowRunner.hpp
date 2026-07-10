#pragma once

#include "controller/AppController.hpp"

#include <cstdint>
#include <string>
#include <vector>

namespace nxs::service {

struct LoadedFlow {
    std::string id;
    std::string triggerType;
    std::int64_t intervalMs = 0;
    std::string eventName;
    std::string invokeTarget;
    std::string invokeArg;
    bool enabled = true;
};

class FlowRunner {
public:
    explicit FlowRunner(controller::AppController& controller);
    bool load(const std::string& configPath = "nxs_config.json",
              const std::string& flowsPath = "flows/flows.json");
    void onStartup();
    void onEvent(const std::string& eventName);
    void tick(std::uint64_t frameDeltaMs);
    bool isActive() const { return m_active; }

private:
    void runFlow(const LoadedFlow& flow);
    void dispatchInvoke(const std::string& target, const std::string& arg);

    controller::AppController& m_controller;
    std::vector<LoadedFlow> m_flows;
    bool m_active = false;
    std::uint64_t m_elapsedMs = 0;
};

}  // namespace nxs::service
