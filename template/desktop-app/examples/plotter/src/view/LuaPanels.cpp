#include "view/LuaPanels.hpp"

#include "Paths.hpp"
#include "ScriptArchive.hpp"

#include <imgui.h>

#include <filesystem>

namespace nxs::view {

LuaPanels::LuaPanels(controller::PlotController& controller) : m_controller(controller) {
    m_lua.open_libraries(sol::lib::base, sol::lib::math, sol::lib::string, sol::lib::table);
    bindApi();
}

void LuaPanels::bindApi() {
    sol::table nxs = m_lua.create_named_table("nxs");
    nxs.set_function("add_function", [this](const std::string& id) { m_controller.addFunction(id); });
    nxs.set_function("remove_function", [this](const std::string& id) { m_controller.removeFunction(id); });
    nxs.set_function("set_range", [this](double lo, double hi) { m_controller.setRange(lo, hi); });
    nxs.set_function("set_samples", [this](int n) { m_controller.setSampleCount(n); });
    nxs.set_function("toggle_log_y", [this]() {
        m_controller.settings().logScaleY = !m_controller.settings().logScaleY;
    });
    nxs.set_function("register_panel", [this](const std::string& title, sol::protected_function fn) {
        m_panels.push_back({title, std::move(fn)});
    });
    nxs.set_function("register_hotkey", [this](int imguiKey, sol::protected_function fn) {
        m_hotkeys.push_back({imguiKey, std::move(fn)});
    });

    sol::table ui = m_lua.create_named_table("ui");
    ui.set_function("text", [](const std::string& s) { ImGui::TextUnformatted(s.c_str()); });
    ui.set_function("button", [](const std::string& label) { return ImGui::Button(label.c_str()); });
    ui.set_function("separator", []() { ImGui::Separator(); });
    ui.set_function("same_line", []() { ImGui::SameLine(); });

    sol::table keys = m_lua.create_named_table("keys");
    keys["F1"] = static_cast<int>(ImGuiKey_F1);
    keys["L"] = static_cast<int>(ImGuiKey_L);
}

void LuaPanels::loadScripts(const std::string& scriptDir) {
    m_panels.clear();
    m_hotkeys.clear();
    sol::protected_function_result result =
        m_lua.safe_script_file(scriptDir + "/panels.lua", sol::script_pass_on_error);
    if (!result.valid()) {
        m_lastError = sol::error(result).what();
    } else {
        m_lastError.clear();
    }
}

void LuaPanels::drawFrame() {
    for (const auto& hotkey : m_hotkeys) {
        if (ImGui::IsKeyPressed(static_cast<ImGuiKey>(hotkey.keycode), false)) {
            if (auto r = hotkey.action(); !r.valid()) {
                m_lastError = sol::error(r).what();
            }
        }
    }
    for (const auto& panel : m_panels) {
        ImGui::Begin(panel.title.c_str());
        if (auto r = panel.body(); !r.valid()) {
            m_lastError = sol::error(r).what();
        }
        ImGui::End();
    }
}

}  // namespace nxs::view
