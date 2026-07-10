#pragma once

#include "controller/AppController.hpp"

#include <sol/sol.hpp>

#include <string>
#include <vector>

namespace nxs::view {

class LuaPanels {
public:
    explicit LuaPanels(controller::AppController& controller);
    void loadScripts(const std::string& scriptDir = "scripts");
    void drawFrame();
    const std::string& lastError() const { return m_lastError; }

private:
    struct Panel { std::string title; sol::protected_function body; };
    struct Hotkey { int keycode; sol::protected_function action; };
    void bindApi();

    controller::AppController& m_controller;
    sol::state m_lua;
    std::vector<Panel> m_panels;
    std::vector<Hotkey> m_hotkeys;
    std::string m_lastError;
};

}  // namespace nxs::view
