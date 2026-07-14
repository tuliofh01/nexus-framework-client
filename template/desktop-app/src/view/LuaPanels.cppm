//==============================================================================
// nxs.desktop.lua — Lua Scripting Panels (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Owns the sol2 Lua state used for runtime scripting panels and hotkeys.
// Scripts loaded from `scripts/panels.lua` (or the packed `lua.dat` archive)
// register panels and hotkeys via the `nxs.*` API. This class pumps those
// registrations every frame.
//
// == RAII ==
// sol2 state is owned by value — construction opens libraries + binds API,
// destruction closes Lua cleanly. Panels and hotkeys are RAII containers
// (std::vector of move-only types).
//
// == TS/XHTML DSL ==
// The TypeScript + XHTML authoring layer (see ui/) lowers into the same
// nxs.register_panel(), ui.button(), etc. calls that Lua scripts use
// directly. Both paths converge here.
//
// == MODULE BOUNDARY ==
// sol2 headers live in the global fragment so importing modules never
// see them. The `nxs.*` Lua API (add_function, set_range, register_panel,
// register_hotkey) is wired in bindApi().
//==============================================================================

module;  // global module fragment

// ── sol2 ──
#include <sol/sol.hpp>

// ── Dear ImGui (for keys and inline helpers) ──
#include <imgui.h>

// ── Standard library ──
#include <cstdio>
#include <filesystem>
#include <string>
#include <utility>
#include <vector>

export module nxs.desktop.lua;

// ── Import peer and shared modules ──
import nxs.desktop.controller;
import nexus.shared.paths;
import nexus.shared.script_archive;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — Lua Runtime Panel Manager
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::view {

/// Manages the sol2 Lua state and pumps registered panel callbacks each
/// frame. Constructing LuaPanels opens Lua standard libraries and wires
/// the `nxs.*` API via bindApi(); call loadScripts() to load user panels.
///
/// RAII: sol::state owns the Lua VM; construction opens libs + binds API,
///        destruction closes Lua cleanly.
export class LuaPanels {
public:
    /// Bind the `nxs.*` API and open base/math/string/table libs.
    explicit LuaPanels(controller::PlotController& controller)
        : m_controller(controller) {
        m_lua.open_libraries(sol::lib::base, sol::lib::math,
                             sol::lib::string, sol::lib::table);
        bindApi();
    }

    /// Non-copyable — sol::state owns the Lua VM.
    LuaPanels(const LuaPanels&) = delete;
    LuaPanels& operator=(const LuaPanels&) = delete;
    LuaPanels(LuaPanels&&) = delete;
    LuaPanels& operator=(LuaPanels&&) = delete;

    ~LuaPanels() = default;

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
                m_lastError = "lua.dat missing entry: panels";
                return;
            }
            m_lastError = "Failed to load lua.dat";
            return;
        }

        // Dev fallback: load plaintext scripts/panels.lua when archive
        // is not staged.
        auto result = m_lua.safe_script_file(
            scriptDir + "/panels.lua", sol::script_pass_on_error);
        if (!result.valid()) {
            m_lastError = sol::error(result).what();
        } else {
            m_lastError.clear();
        }
    }

    /// Called once per frame: fire hotkeys, then draw every registered
    /// panel. Errors in individual panels do not crash the frame loop.
    void drawFrame() {
        for (const auto& hotkey : m_hotkeys) {
            if (ImGui::IsKeyPressed(
                    static_cast<ImGuiKey>(hotkey.keycode), false)) {
                if (auto r = hotkey.action(); !r.valid()) {
                    m_lastError = sol::error(r).what();
                }
            }
        }
        for (const auto& panel : m_panels) {
            ImGui::Begin(panel.title.c_str());
            if (auto r = panel.body(); !r.valid()) {
                m_lastError = sol::error(r).what();
                ImGui::TextUnformatted("(script error — see log)");
            }
            ImGui::End();
        }
    }

    /// The last Lua error message, or empty if no error.
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    struct Panel {
        std::string title;
        sol::protected_function body;
    };
    struct Hotkey {
        int keycode;  // ImGuiKey value
        sol::protected_function action;
    };

    /// Install the `nxs.*` and `ui.*` tables into the Lua global state.
    void bindApi() {
        sol::table nxs = m_lua.create_named_table("nxs");

        nxs.set_function("add_function",
            [this](const std::string& id) { m_controller.addFunction(id); });
        nxs.set_function("remove_function",
            [this](const std::string& id) { m_controller.removeFunction(id); });
        nxs.set_function("set_range",
            [this](double lo, double hi) { m_controller.setRange(lo, hi); });
        nxs.set_function("set_samples",
            [this](int n) { m_controller.setSampleCount(n); });
        nxs.set_function("toggle_log_y", [this]() {
            m_controller.settings().logScaleY =
                !m_controller.settings().logScaleY;
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
        keys["F2"] = static_cast<int>(ImGuiKey_F2);
        keys["L"]  = static_cast<int>(ImGuiKey_L);
    }

    controller::PlotController& m_controller;
    sol::state m_lua;
    std::vector<Panel> m_panels;
    std::vector<Hotkey> m_hotkeys;
    std::string m_lastError;
};

}  // namespace nxs::view
