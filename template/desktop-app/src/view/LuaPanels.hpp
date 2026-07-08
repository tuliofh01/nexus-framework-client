// The Lua (sol2) scripting layer. scripts/panels.lua registers extra
// ImGui panels and hotkeys at runtime; this class owns the Lua state,
// exposes the `nxs.*` API to it, and pumps the registered callbacks
// every frame. The TS/XHTML DSL (see ui/) lowers into the same API.
#pragma once

#include "controller/PlotController.hpp"

#include <sol/sol.hpp>

#include <string>
#include <vector>

namespace nxs::view {

class LuaPanels {
public:
    explicit LuaPanels(controller::PlotController& controller);

    // Loads (or reloads) scripts/panels.lua. Safe to call at runtime for
    // hot-reload; errors are captured, not thrown.
    void loadScripts(const std::string& scriptDir = "scripts");

    // Called once per frame: fires hotkeys, then draws every panel the
    // scripts registered via nxs.register_panel().
    void drawFrame();

    const std::string& lastError() const { return m_lastError; }

private:
    struct Panel {
        std::string title;
        sol::protected_function body;
    };
    struct Hotkey {
        int keycode;  // ImGuiKey value
        sol::protected_function action;
    };

    void bindApi();  // installs the nxs.* table into the Lua state

    controller::PlotController& m_controller;
    sol::state m_lua;
    std::vector<Panel> m_panels;
    std::vector<Hotkey> m_hotkeys;
    std::string m_lastError;
};

}  // namespace nxs::view
