//==============================================================================
// nxs.android.controller — Application Controller (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Same role as nxs.desktop.controller: listens to user actions from the
// view, mutates the model, and coordinates with the Python engine. The
// only difference is that Android methods are not noexcept (matches the
// original Android codebase style).
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from references
// noexcept            — where safe (model accessors, simple getters)
// = delete            — Rule of Five: non-copyable, non-movable
// [[nodiscard]]       — return values must be checked
// constexpr           — increment/decrement/reset are trivial enough
// trailing return    — auto fn() -> Type for uniform declaration syntax
// {} brace-init      — uniform initialization for reference members
// std::string_view   — non-owning constexpr-capable string params
// std::move          — transfers ownership in setter
// sv suffix          — std::string_view_literals for "..."sv syntax
//
// ════════════════════════════════════════════════════════════════════════════
// MVC ROLE
// ════════════════════════════════════════════════════════════════════════════
//
//   View (user clicks)  ──►  Controller  ──►  Model (state)
//                                     │
//                                     └──►  PythonEngine (greeting)
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
#include <cstdio>       // std::fprintf for logging (not used yet, kept for parity)
#include <string>       // std::string for greeting storage
#include <string_view>  // std::string_view for string literal params
#include <utility>      // std::move for ownership transfer

export module nxs.android.controller;

// `sv` suffix: \"text\"sv builds a std::string_view at compile time

// ── Import peer modules ──
//
// The controller wires model state to the Python engine. It imports
// both and mediates between them.
import nxs.android.model;
import nxs.android.python;

// `sv` suffix: "text"sv builds a std::string_view at compile time
// (no heap allocation). Must appear before first use in this file.
using namespace std::string_view_literals;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Command Dispatcher (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::controller {

/// Mediator between the view and the model.
///
/// Holds non-owning references to both the model and the Python engine.
/// Created in main() after both dependencies; must be destroyed before
/// them (reverse construction order).
class AppController {
public:
    /// Store references to the model and Python engine.
    ///
    /// explicit: prevents implicit conversion from (AppModel, PythonEngine).
    AppController(model::AppModel& model, PythonEngine& python) noexcept
        : m_model{model}, m_python{python} {}  // {} brace-init

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Holds references — cannot be copied or moved.

    AppController(const AppController&) = delete;
    AppController& operator=(const AppController&) = delete;
    AppController(AppController&&) = delete;
    AppController& operator=(AppController&&) = delete;

    ~AppController() = default;

    // ── Counter commands ───────────────────────────────────────────────

    /// Increase the counter by one.
    ///
    /// constexpr: trivial enough for compile-time evaluation.
    /// noexcept: no exceptions from simple arithmetic + setter.
    constexpr void increment() noexcept {
        m_model.setCounter(m_model.counter() + 1);
    }

    /// Decrease the counter by one.
    constexpr void decrement() noexcept {
        m_model.setCounter(m_model.counter() - 1);
    }

    /// Reset the counter to zero.
    constexpr void reset() noexcept { m_model.setCounter(0); }

    // ── Python greeting refresh ────────────────────────────────────────

    /// Evaluate helpers.greeting() via the Chaquopy/Zig JNI bridge.
    ///
    /// If the bridge returns a non-empty greeting, we update the model.
    /// On failure, the model keeps its previous greeting and the error
    /// is available via lastPythonError().
    ///
    /// Uses "{{projectName}}"sv — the `sv` literal suffix (from
    /// std::string_view_literals) creates a std::string_view at compile
    /// time with no allocation.
    void refresh() noexcept {
        const auto greeting = m_python.greeting("{{projectName}}"sv);
        if (!greeting.empty()) {
            m_model.setGreeting(greeting);
        }
    }

    // ── Accessors for the view layer ───────────────────────────────────

    /// Mutable model access for the view.
    [[nodiscard]] auto model() noexcept -> model::AppModel& {
        return m_model;
    }

    /// Forward the Python engine's last error.
    [[nodiscard]] auto lastPythonError() const noexcept
        -> const std::string& {
        return m_python.lastError();
    }

private:
    model::AppModel& m_model;      ///< Non-owning: created in main()
    PythonEngine& m_python;        ///< Non-owning: created in main()
};

}  // namespace nxs::controller

// ── String view literal suffix ────────────────────────────────────────────
//
// The `using namespace` directive is file-scoped (not in a header, not in
// a namespace block). It enables the `"..."sv` syntax used in refresh().
// In a .cppm module, this is safe because it does not leak to importers.
