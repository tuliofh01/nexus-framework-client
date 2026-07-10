#include "view/LuaPanels.hpp"

#include "Paths.hpp"
#include "ScriptArchive.hpp"

#include <imgui.h>

#include <filesystem>
#include <iostream>

namespace nxs::view {

LuaPanels::LuaPanels(controller::AppController& controller) : m_controller(controller) {
    m_lua.open_libraries(sol::lib::base, sol::lib::math, sol::lib::string, sol::lib::table);
    bindApi();
}

void LuaPanels::bindApi() {
    sol::table nxs = m_lua.create_named_table("nxs");
    nxs.set_function("increment", [this]() { m_controller.increment(); });
    nxs.set_function("decrement", [this]() { m_controller.decrement(); });
    nxs.set_function("reset", [this]() { m_controller.reset(); });
    nxs.set_function("log", [](const std::string& msg) {
        std::fprintf(stderr, "[Lua] %s\n", msg.c_str());
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
}

void LuaPanels::loadScripts(const std::string& scriptDir) {
    m_panels.clear();
    m_hotkeys.clear();
    const std::string archivePath = runtime::Paths::luaArchive();
    if (std::filesystem::exists(archivePath)) {
        runtime::ScriptArchive archive(runtime::ScriptArchive::MAGIC_LUA);
        if (archive.load(archivePath)) {
            std::string source;
            if (archive.getSource("panels", source)) {
                auto result = m_lua.safe_script(source, sol::script_pass_on_error);
                if (!result.valid()) m_lastError = sol::error(result).what();
                else m_lastError.clear();
                return;
            }
        }
    }
    auto result = m_lua.safe_script_file(scriptDir + "/panels.lua", sol::script_pass_on_error);
    if (!result.valid()) m_lastError = sol::error(result).what();
    else m_lastError.clear();
}

void LuaPanels::drawFrame() {
    for (const auto& hotkey : m_hotkeys) {
        if (ImGui::IsKeyPressed(static_cast<ImGuiKey>(hotkey.keycode), false)) {
            if (auto r = hotkey.action(); !r.valid()) m_lastError = sol::error(r).what();
        }
    }
    for (const auto& panel : m_panels) {
        ImGui::Begin(panel.title.c_str());
        if (auto r = panel.body(); !r.valid()) m_lastError = sol::error(r).what();
        ImGui::End();
    }
}

}  // namespace nxs::view
