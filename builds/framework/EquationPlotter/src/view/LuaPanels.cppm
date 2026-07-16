//==============================================================================
// nxs.desktop.lua — Lua Scripting Panels (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Owns the sol2 Lua state used for runtime scripting panels and hotkeys.
// Scripts loaded from `scripts/panels.lua` (or the packed `lua.dat` archive)
// register panels and hotkeys via the `nxs.*` API. This class pumps those
// registrations every frame.
//
// ════════════════════════════════════════════════════════════════════════════
// RAII (Resource Acquisition Is Initialization)
// ════════════════════════════════════════════════════════════════════════════
//
//   - sol::state is owned by value — construction opens libraries + binds API
//   - Destruction closes Lua cleanly (sol::state destructor)
//   - Panels and hotkeys are RAII containers (std::vector<Panel/Hotkey>) that
//     own sol::protected_function (move-only, non-copyable)
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit           — prevents implicit construction from controller ref
// noexcept           — constructor guarantees no exceptions
// = delete           — Rule of Five: non-copyable, non-movable
// [[nodiscard]]      — return values cannot be ignored
// trailing return   — auto fn() -> Type syntax (uniform function declarations)
// const auto&       — range-for with const reference to avoid copies
// auto r            — type deduction for sol::protected_function result
// static_cast<T>    — explicit type conversion (ImGuiKey enum → int)
// std::move         — transfers ownership of sol::protected_function
// {} brace init     — uniform initialization for struct members
// std::string_view  — non-owning string reference (loadScripts param)
//
// ════════════════════════════════════════════════════════════════════════════
// TS/XHTML DSL
// ════════════════════════════════════════════════════════════════════════════
//
// The TypeScript + XHTML authoring layer (see ui/) lowers into the same
// nxs.register_panel(), ui.button(), etc. calls that Lua scripts use
// directly. Both paths converge here.
//
// ════════════════════════════════════════════════════════════════════════════
// MODULE BOUNDARY
// ════════════════════════════════════════════════════════════════════════════
//
// sol2 headers live in the global fragment so importing modules never
// see them. The `nxs.*` Lua API (add_function, set_range, register_panel,
// register_hotkey) is wired in bindApi().
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── sol2 (Lua VM) ──
// In global fragment so `import nxs.desktop.lua;` never sees sol2 headers.
#include <sol/sol.hpp>

// ── Dear ImGui (for key codes and inline helpers) ──
#include <imgui.h>

// ── Standard library (private to this module) ──
#include <cstdio>       // std::fprintf for error logging
#include <filesystem>   // std::filesystem::exists for archive path check
#include <string>       // std::string for titles and errors
#include <string_view>  // std::string_view for loadScripts path param
#include <utility>      // std::move for ownership transfer
#include <vector>       // std::vector for panels and hotkeys

export module nxs.desktop.lua;

// ── Import peer and shared modules ──
import nxs.desktop.controller;
import nxs.desktop.plot;  // controller::PlotController lives here
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
class LuaPanels {
public:
    /// Bind the `nxs.*` API and open base/math/string/table libs.
    ///
    /// explicit: prevents implicit conversion from PlotController& to LuaPanels.
    /// noexcept: guarantees no exceptions from construction.
    explicit LuaPanels(controller::PlotController& controller) noexcept
        : m_controller{controller} {   // {} brace-init for reference member
        // Open only the libraries we need — not debug/coroutine/io/os.
        // Keeps the Lua surface minimal and secure.
        m_lua.open_libraries(sol::lib::base, sol::lib::math,
                             sol::lib::string, sol::lib::table);
        bindApi();
    }

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // sol::state owns the Lua VM and is non-copyable. Even if it weren't,
    // we don't want two Lua VMs sharing the same controller reference.
    // = delete prevents accidental duplication at compile time.

    LuaPanels(const LuaPanels&) = delete;
    LuaPanels& operator=(const LuaPanels&) = delete;
    LuaPanels(LuaPanels&&) = delete;
    LuaPanels& operator=(LuaPanels&&) = delete;

    ~LuaPanels() = default;

    /// Load (or reload) panels from the packed `lua.dat` archive or
    /// plaintext `scripts/panels.lua`. Safe to call at runtime for
    /// hot-reload; errors are captured into m_lastError, not thrown.
    ///
    /// std::string_view: non-owning reference to the script directory path.
    /// Avoids a heap allocation for a read-only default argument.
    void loadScripts(const std::string_view scriptDir = "scripts") {
        m_panels.clear();
        m_hotkeys.clear();

        // ── Try packed archive first (release mode) ────────────────────
        //
        // ScriptArchive reads LZ4-compressed .dat files. If present, we
        // prefer it over the plaintext directory — smaller and faster.

        const auto archivePath = runtime::Paths::luaArchive();
        if (std::filesystem::exists(archivePath)) {
            // Brace-init for ScriptArchive with MAGIC_LUA constant.
            runtime::ScriptArchive archive{
                runtime::ScriptArchive::MAGIC_LUA};
            if (archive.load(archivePath)) {
                std::string source;
                if (archive.getSource("panels", source)) {
                    // sol::safe_script: executes Lua source in a protected
                    // environment. sol::script_pass_on_error: returns the
                    // error as a result object instead of throwing.
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

        // ── Dev fallback: plaintext scripts/panels.lua ─────────────────
        //
        // When the archive is not staged (development mode), load the
        // raw .lua file. String concatenation with + creates a temporary
        // std::string — acceptable for the development path.

        const std::string path = std::string{scriptDir} + "/panels.lua";
        auto result = m_lua.safe_script_file(
            path, sol::script_pass_on_error);
        if (!result.valid()) {
            m_lastError = sol::error(result).what();
        } else {
            m_lastError.clear();
        }
    }

    /// Called once per frame: fire hotkeys, then draw every registered
    /// panel. Errors in individual panels do not crash the frame loop.
    void drawFrame() {
        // ── Hotkey dispatch ────────────────────────────────────────────
        //
        // Range-for with const auto&: avoids copying the Hotkey struct.
        // ImGui::IsKeyPressed(key, false): returns true on the frame the
        // key was pressed (repeat=false).
        for (const auto& hotkey : m_hotkeys) {
            if (ImGui::IsKeyPressed(
                    static_cast<ImGuiKey>(hotkey.keycode), false)) {
                // sol::protected_function::operator() returns a
                // sol::protected_function_result. `.valid()` checks
                // if the call succeeded.
                if (auto r = hotkey.action(); !r.valid()) {
                    m_lastError = sol::error(r).what();
                }
            }
        }

        // ── Panel dispatch ─────────────────────────────────────────────
        //
        // Each registered panel gets its own ImGui window. If a panel's
        // body function errors, we show a placeholder text instead of
        // crashing the frame loop.
        for (const auto& panel : m_panels) {
            ImGui::Begin(panel.title.c_str());
            if (auto r = panel.body(); !r.valid()) {
                m_lastError = sol::error(r).what();
                ImGui::TextUnformatted("(script error — see log)");
            }
            ImGui::End();  // must match Begin()
        }
    }

    /// The last Lua error message, or empty if no error.
    ///
    /// [[nodiscard]]: callers MUST check or explicitly discard.
    /// noexcept: returning a const reference is safe (no allocation).
    /// trailing return type: -> const std::string& for clarity.
    [[nodiscard]] auto lastError() const noexcept -> const std::string& {
        return m_lastError;
    }

private:
    // ── Internal data structures ───────────────────────────────────────
    //
    // These are private to the class — not exported from the module.
    // sol::protected_function is move-only (owns a sol2 reference).

    struct Panel {
        std::string title;
        sol::protected_function body;  // move-only
    };

    struct Hotkey {
        int keycode;  // ImGuiKey value, stored as int for Lua interop
        sol::protected_function action;  // move-only
    };

    /// Install the `nxs.*` and `ui.*` tables into the Lua global state.
    ///
    /// This is called once during construction. It creates Lua tables
    /// and registers C++ lambdas as callable Lua functions. The lambdas
    /// capture `this` (the LuaPanels instance) by reference.
    ///
    /// Modern C++ patterns here:
    ///   - Lambda captures [this]: captures the surrounding object by ref
    ///   - std::move(fn): transfers ownership of sol::protected_function
    ///     into the vector. After move, `fn` is in valid-but-unspecified
    ///     state.
    ///   - static_cast<int>: explicit conversion from ImGuiKey enum
    void bindApi() {
        // ── nxs.* table: controller commands ───────────────────────────
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

        // ── Panel/hotkey registration ──────────────────────────────────
        //
        // These create Panel/Hotkey structs using aggregate init with
        // {} brace initialization. std::move transfers ownership of the
        // sol::protected_function into the vector element.

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
        // Each lambda wraps a single Dear ImGui call.

        sol::table ui = m_lua.create_named_table("ui");
        ui.set_function("text",
            [](const std::string& s) { ImGui::TextUnformatted(s.c_str()); });
        ui.set_function("button",
            [](const std::string& label) { return ImGui::Button(label.c_str()); });
        ui.set_function("separator", []() { ImGui::Separator(); });
        ui.set_function("same_line", []() { ImGui::SameLine(); });

        // ── keys table: named key codes ────────────────────────────────
        //
        // Lua scripts use keys.F1, keys.F2, keys.L instead of numeric codes.
        // static_cast<int>: explicit conversion from ImGuiKey enum to int.

        sol::table keys = m_lua.create_named_table("keys");
        keys["F1"] = static_cast<int>(ImGuiKey_F1);
        keys["F2"] = static_cast<int>(ImGuiKey_F2);
        keys["L"]  = static_cast<int>(ImGuiKey_L);
    }

    controller::PlotController& m_controller;
    sol::state m_lua;                    ///< Owns the Lua VM (value type)
    std::vector<Panel> m_panels;         ///< Registered panel callbacks
    std::vector<Hotkey> m_hotkeys;       ///< Registered hotkey callbacks
    std::string m_lastError;             ///< Last Lua error message
};

}  // namespace nxs::view
