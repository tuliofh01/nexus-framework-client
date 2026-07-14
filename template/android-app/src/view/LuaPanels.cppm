//==============================================================================
// nxs.android.lua — Lua Scripting Panels (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Owns the sol2 Lua state used for runtime scripting panels and hotkeys.
// Loads panels from `scripts/panels.lua` (or packed `lua.dat` archive).
// The `nxs.*` Lua API (add_function, set_range, register_panel, etc.)
// is wired in bindApi().
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from AppController&
// noexcept            — constructor guarantees no exceptions
// = delete            — Rule of Five: non-copyable, non-movable
// [[nodiscard]]       — return values must be checked
// const auto&         — range-for with const reference (no copy)
// auto r              — type deduction for sol::protected_function result
// static_cast<T>      — explicit conversion (ImGuiKey enum → int)
// std::move           — transfers ownership of sol::protected_function
// {} brace-init       — uniform initialization for structs and members
// std::string_view    — non-owning constexpr-capable string param
// trailing return    — auto fn() -> Type syntax
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
//
// Desktop LuaPanels binds to PlotController (Desmos-style plotter).
// Android LuaPanels binds to AppController (simpler counter API).
// This is the only structural difference between the two modules.
//
// ════════════════════════════════════════════════════════════════════════════
// RAII
// ════════════════════════════════════════════════════════════════════════════
//
// sol::state owns the VM by value; construction opens libs + binds API,
// destruction closes Lua cleanly. Panels and hotkeys are std::vector of
// move-only types (sol::protected_function is move-only).
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── sol2 + ImGui (for keys and inline helpers) ──
#include <sol/sol.hpp>
#include <imgui.h>

// ── Standard library (private to this module) ──
#include <cstdio>       // std::fprintf for logging
#include <filesystem>   // std::filesystem::exists for archive check
#include <string>       // std::string for titles and errors
#include <string_view>  // std::string_view for path params
#include <utility>      // std::move for ownership transfer
#include <vector>       // std::vector for panels and hotkeys

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
///
/// RAII: sol::state owns the Lua VM; construction opens libs + binds API,
///       destruction closes Lua cleanly.
export class LuaPanels {
public:
    /// Bind the `nxs.*` API and open base/math/string/table libs.
    ///
    /// explicit: prevents implicit conversion from AppController&.
    /// noexcept: guarantees no exceptions from construction.
    explicit LuaPanels(controller::AppController& controller) noexcept
        : m_controller{controller} {  // {} brace-init for reference
        // Open only the libraries we need — keeps the Lua surface minimal.
        m_lua.open_libraries(sol::lib::base, sol::lib::math,
                             sol::lib::string, sol::lib::table);
        bindApi();
    }

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // sol::state owns the Lua VM and is non-copyable.

    LuaPanels(const LuaPanels&) = delete;
    LuaPanels& operator=(const LuaPanels&) = delete;
    LuaPanels(LuaPanels&&) = delete;
    LuaPanels& operator=(LuaPanels&&) = delete;

    ~LuaPanels() = default;

    /// Load (or reload) panels from the packed `lua.dat` archive or
    /// plaintext `scripts/panels.lua`. Safe to call at runtime for
    /// hot-reload; errors are captured into m_lastError, not thrown.
    ///
    /// std::string_view: non-owning reference to script directory path.
    void loadScripts(const std::string_view scriptDir = "scripts") {
        m_panels.clear();
        m_hotkeys.clear();

        // ── Try packed archive first (release mode) ────────────────────

        const auto archivePath = runtime::Paths::luaArchive();
        if (std::filesystem::exists(archivePath)) {
            runtime::ScriptArchive archive{
                runtime::ScriptArchive::MAGIC_LUA};
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

        // ── Dev fallback: plaintext panels.lua ─────────────────────────
        //
        // When the archive is not staged, load the raw .lua file.

        const std::string path = std::string{scriptDir} + "/panels.lua";
        auto result = m_lua.safe_script_file(
            path, sol::script_pass_on_error);
        if (!result.valid()) {
            m_lastError = sol::error(result).what();
        } else {
            m_lastError.clear();
        }
    }

    /// Fire hotkeys, then draw every registered panel.
    ///
    /// Called once per frame. Errors in individual panels do not crash
    /// the frame loop — they are captured into m_lastError.
    void drawFrame() {
        // ── Hotkey dispatch ────────────────────────────────────────────
        //
        // Range-for with const auto&: avoids copying the Hotkey struct.
        // static_cast<ImGuiKey>: explicit conversion from stored int.
        for (const auto& hotkey : m_hotkeys) {
            if (ImGui::IsKeyPressed(
                    static_cast<ImGuiKey>(hotkey.keycode), false)) {
                if (auto r = hotkey.action(); !r.valid()) {
                    m_lastError = sol::error(r).what();
                }
            }
        }

        // ── Panel dispatch ─────────────────────────────────────────────
        //
        // Each registered panel gets its own ImGui window.
        for (const auto& panel : m_panels) {
            ImGui::Begin(panel.title.c_str());
            if (auto r = panel.body(); !r.valid()) {
                m_lastError = sol::error(r).what();
            }
            ImGui::End();  // must match Begin()
        }
    }

    /// [[nodiscard]]: callers must check or explicitly discard.
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    // ── Internal data structures ───────────────────────────────────────
    //
    // sol::protected_function is move-only (non-copyable sol2 reference).

    struct Panel {
        std::string title;
        sol::protected_function body;
    };

    struct Hotkey {
        int keycode;  // ImGuiKey as int (for Lua interop)
        sol::protected_function action;
    };

    /// Install the `nxs.*` and `ui.*` tables into the Lua global state.
    ///
    /// Modern C++ patterns here:
    ///   - Lambda captures [this]: captures the surrounding object by ref
    ///   - std::move(fn): transfers ownership of sol::protected_function
    ///   - static_cast<int>: explicit ImGuiKey → int conversion
    void bindApi() {
        // ── nxs.* table: controller commands ───────────────────────────
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

        // ── Panel/hotkey registration ──────────────────────────────────
        //
        // std::move transfers ownership of sol::protected_function into
        // the vector element, avoiding copies of the Lua reference.

        nxs.set_function("register_panel",
            [this](const std::string& title, sol::protected_function fn) {
                m_panels.push_back(Panel{title, std::move(fn)});
            });
        nxs.set_function("register_hotkey",
            [this](int imguiKey, sol::protected_function fn) {
                m_hotkeys.push_back(Hotkey{imguiKey, std::move(fn)});
            });

        // ── ui.* table: ImGui widget wrappers ──────────────────────────
        //
        // These allow Lua scripts to call ui.text(), ui.button(), etc.
        sol::table ui = m_lua.create_named_table("ui");
        ui.set_function("text",
            [](const std::string& s) { ImGui::TextUnformatted(s.c_str()); });
        ui.set_function("button",
            [](const std::string& label) { return ImGui::Button(label.c_str()); });
        ui.set_function("separator", []() { ImGui::Separator(); });
        ui.set_function("same_line", []() { ImGui::SameLine(); });

        // ── keys table: named key codes ────────────────────────────────
        //
        // static_cast<int>: ImGuiKey enum → int for Lua interop.
        sol::table keys = m_lua.create_named_table("keys");
        keys["F1"] = static_cast<int>(ImGuiKey_F1);
    }

    controller::AppController& m_controller;
    sol::state m_lua;                       ///< Owns the Lua VM (value type)
    std::vector<Panel> m_panels;            ///< Registered panel callbacks
    std::vector<Hotkey> m_hotkeys;          ///< Registered hotkey callbacks
    std::string m_lastError;                ///< Last Lua error message
};

}  // namespace nxs::view
