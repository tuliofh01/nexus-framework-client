//==============================================================================
// nxs.android.lua — Lua Scripting Panels (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Owns the sol2 Lua state used for runtime scripting panels and hotkeys.
// Loads panels from `scripts/panels.lua` (or packed `lua.dat` archive).
// The `nxs.*` Lua API (add_function, set_range, register_panel, etc.)
// is wired in bindApi().
//
// == DESKTOP VS ANDROID ==
// Desktop LuaPanels binds to PlotController (Desmos-style plotter).
// Android LuaPanels binds to AppController (simpler counter API).
// This is the only structural difference between the two modules.
//
// == TS/XHTML DSL ==
// The TypeScript + XHTML authoring layer lowers into the same nxs.* API
// that Lua scripts use directly. Both paths converge here.
//==============================================================================

module;  // global module fragment

// ── sol2 + ImGui (for keys and inline helpers) ──
#include <sol/sol.hpp>
#include <imgui.h>

// ── Standard library ──
#include <cstdio>
#include <filesystem>
#include <string>
#include <vector>

export module nxs.android.lua;

// ── Import peer and shared modules ──
import nxs.android.controller;
import nexus.shared.paths;
import nexus.shared.script_archive;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — Lua Runtime Panel Manager (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::view {

/// Manages the sol2 Lua state and pumps registered panel callbacks each
/// frame. Constructing LuaPanels opens Lua standard libraries and wires
/// the `nxs.*` API via bindApi(); call loadScripts() to load user panels.
export class LuaPanels {
public:
    /// Bind the `nxs.*` API and open base/math/string/table libs.
    explicit LuaPanels(controller::AppController& controller)
        : m_controller(controller) {
        m_lua.open_libraries(sol::lib::base, sol::lib::math,
                             sol::lib::string, sol::lib::table);
        bindApi();
    }

    /// Load (or reload) panels from the packed `lua.dat` archive or
    /// plaintext `scripts/panels.lua`. Safe to call at runtime for
    /// hot-reload; errors are captured, not thrown.
    void loadScripts(const std::string& scriptDir = "scripts") {
        m_panels.clear();
        m_hotkeys.clear();

        const std::string archivePath = runtime::Paths::luaArchive();
        if (std::filesystem::exists(archivePath)) {
            runtime::ScriptArchive archive(
                runtime::ScriptArchive::MAGIC_LUA);
            if (archive.load(archivePath)) {
                std::string source;
                if (archive.getSource("panels", source)) {
                    auto result = m_lua.safe_script(
                        source, sol::script_pass_on_error);
                    if (!result.valid()) {
                        m_lastError = sol::error(result).what();
                    } else {
                        m_lastError.clear();
                    }
                    return;
                }
            }
        }

        // Dev fallback: load plaintext panels.lua when archive not staged.
        auto result = m_lua.safe_script_file(
            scriptDir + "/panels.lua", sol::script_pass_on_error);
        if (!result.valid()) {
            m_lastError = sol::error(result).what();
        } else {
            m_lastError.clear();
        }
    }

    /// Fire hotkeys, then draw every registered panel.
    void drawFrame() {
        for (const auto& hotkey : m_hotkeys) {
            if (ImGui::IsKeyPressed(
                    static_cast<ImGuiKey>(hotkey.keycode), false)) {
                if (auto r = hotkey.action(); !r.valid())
                    m_lastError = sol::error(r).what();
            }
        }
        for (const auto& panel : m_panels) {
            ImGui::Begin(panel.title.c_str());
            if (auto r = panel.body(); !r.valid())
                m_lastError = sol::error(r).what();
            ImGui::End();
        }
    }

    const std::string& lastError() const { return m_lastError; }

private:
    struct Panel {
        std::string title;
        sol::protected_function body;
    };
    struct Hotkey {
        int keycode;
        sol::protected_function action;
    };

    /// Install the `nxs.*` and `ui.*` tables into the Lua global state.
    void bindApi() {
        sol::table nxs = m_lua.create_named_table("nxs");

        nxs.set_function("increment",
            [this]() { m_controller.increment(); });
        nxs.set_function("decrement",
            [this]() { m_controller.decrement(); });
        nxs.set_function("reset",
            [this]() { m_controller.reset(); });
        nxs.set_function("log",
            [](const std::string& msg) {
                std::fprintf(stderr, "[Lua] %s\n", msg.c_str());
            });

        nxs.set_function("register_panel",
            [this](const std::string& title, sol::protected_function fn) {
                m_panels.push_back({title, std::move(fn)});
            });
        nxs.set_function("register_hotkey",
            [this](int imguiKey, sol::protected_function fn) {
                m_hotkeys.push_back({imguiKey, std::move(fn)});
            });

        sol::table ui = m_lua.create_named_table("ui");
        ui.set_function("text",
            [](const std::string& s) { ImGui::TextUnformatted(s.c_str()); });
        ui.set_function("button",
            [](const std::string& label) { return ImGui::Button(label.c_str()); });
        ui.set_function("separator", []() { ImGui::Separator(); });
        ui.set_function("same_line", []() { ImGui::SameLine(); });

        sol::table keys = m_lua.create_named_table("keys");
        keys["F1"] = static_cast<int>(ImGuiKey_F1);
    }

    controller::AppController& m_controller;
    sol::state m_lua;
    std::vector<Panel> m_panels;
    std::vector<Hotkey> m_hotkeys;
    std::string m_lastError;
};

}  // namespace nxs::view
