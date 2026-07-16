//==============================================================================
// nxs.desktop.controller — Application Controller (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Listens to user actions from the view (and Lua/flows), mutates the
// model, and coordinates with the Python engine. This is the "middleware"
// of the MVC triad — it owns no state, only references to the model and
// Python engine.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// [[nodiscard]]    — compiler warns if caller ignores the return value.
//
// constexpr        — function can run at compile time. Zero runtime cost.
//
// noexcept         — function承诺 not to throw. Enables std::vector
//                    move semantics and other optimisations.
//
// trailing return   — `auto f() -> T` instead of `T f()`. Preferred in
// type               modern C++ for consistency with lambdas and templates.
//
// = delete         — explicitly prevents copy/move. Use when the class
//                    holds non-owning references that must not dangle.
//
// std::string_view — a non-owning view into a string. Cheaper than
//                    const std::string& because it doesn't trigger
//                    implicit allocation. Use for read-only parameters.
//
// ════════════════════════════════════════════════════════════════════════════
// MVC ROLE
// ════════════════════════════════════════════════════════════════════════════
//
//   View (user clicks)  ──►  Controller  ──►  Model (state)
//                                     │
//                                     └──►  PythonEngine (greeting, eval)
//
// The view never touches the model directly. All mutations go through
// controller methods like increment(), decrement(), reset(), and refresh().
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Standard library (private to this module) ──
//
// <cstdio>       — std::fprintf for error logging (C I/O, faster than
//                  iostream for simple formatted output)
// <string>       — std::string for greeting/error storage
// <string_view>  — std::string_view for cheap read-only parameters
//
// These are NOT visible to importers.
#include <cstdio>
#include <string>
#include <string_view>

export module nxs.desktop.controller;

// `sv` suffix: \"text\"sv builds a std::string_view at compile time

// ── Import peer modules ──
//
// `import X;` replaces `#include "X.hpp"`. The compiler resolves these
// at module-scan time, not at preprocess time. Benefits:
//   - No header guards or #pragma once needed
//   - Importers only see the `export`ed API, not private helpers
//   - Faster compilation (module BMI is pre-compiled)
import nxs.desktop.model;
import nxs.desktop.python;

// `sv` suffix: "text"sv builds a std::string_view at compile time
// (no heap allocation). Must appear before first use in this file.
using namespace std::string_view_literals;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Command Dispatcher
// ═══════════════════════════════════════════════════════════════════════════
//
// AppController wires user intent to model mutations. Each public method
// corresponds to a UI action or a Lua/flow trigger. The `model()` accessor
// gives the view read access to the current state without exposing mutable
// references to the controller internals.

export namespace nxs::controller {

/// Application controller: thin middleware between view and model.
///
/// Design rule: the controller is STATELESS. It holds non-owning
/// references to the model and Python engine, both created in main()
/// before the controller. The controller never allocates, never stores
/// data beyond these references, and never calls ImGui or sol2 directly.
///
/// Thread-safety: single-threaded (ImGui frame loop on main thread).
class AppController {
public:
    // ── Construction ───────────────────────────────────────────────────
    //
    // Takes two non-owning references. The constructor is noexcept
    // because reference binding cannot throw.
    //
    // noexcept: enables optimisations in std::vector and std::optional
    // when storing AppController (though we don't — we pass by ref).

    AppController(model::AppModel& model, PythonEngine& python) noexcept
        : m_model{model}, m_python{python} {}

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Why delete? AppController holds non-owning references (&model,
    // &python). If we allowed copying, the copy would reference the
    // SAME objects as the original — but if the original is destroyed,
    // the copy dangles. Safer to prevent copying entirely.
    //
    // = delete: the compiler generates an error if someone tries to
    // copy or move. This is a compile-time safety net.

    AppController(const AppController&) = delete;
    AppController& operator=(const AppController&) = delete;
    AppController(AppController&&) = delete;
    AppController& operator=(AppController&&) = delete;

    ~AppController() = default;  // trivial destructor (no resources to release)

    // ── Counter commands ───────────────────────────────────────────────
    //
    // All three are constexpr + noexcept: they are trivial int
    // operations that can run at compile time and never throw.
    //
    // Why constexpr? If someone writes:
    //   constexpr auto c = AppController{model, python};
    //   c.increment();  // at compile time — impossible here because
    //                    // model/python are runtime objects, but the
    //                    // constexpr annotation is still useful for
    //                    // documentation and for trivial cases.

    /// Increase the counter by one. Called by the Increment button or
    /// a Lua/flow `nxs.increment` invoke.
    constexpr void increment() noexcept {
        m_model.setCounter(m_model.counter() + 1);
    }

    /// Decrease the counter by one. Called by the Decrement button or
    /// a Lua `nxs.decrement` invoke.
    constexpr void decrement() noexcept {
        m_model.setCounter(m_model.counter() - 1);
    }

    /// Reset the counter to zero. Called by the Reset button or a
    /// Lua/flow `nxs.reset` invoke.
    constexpr void reset() noexcept { m_model.setCounter(0); }

    // ── Python greeting refresh ────────────────────────────────────────
    //
    // Evaluate `helpers.greeting()` in the embedded Python interpreter.
    // The result overwrites the model's greeting string if non-empty.
    //
    // NOT constexpr: calls into pybind11 (Python interpreter).
    // NOT noexcept: pybind11 may throw (caught internally by
    // PythonEngine::greeting, but the caller doesn't guarantee this).

    void refresh() {
        // std::string_view for the project name — cheap, non-owning.
        // The compiler knows the length at compile time (it's a literal).
        const auto greeting = m_python.greeting("{{projectName}}"sv);

        // if (!greeting.empty()): check before overwriting. The model's
        // setGreeting takes by-value + std::move (pass-by-value sink
        // idiom — one copy max, often zero).
        if (!greeting.empty()) {
            m_model.setGreeting(greeting);
        }
    }

    // ── Accessors for the view layer ───────────────────────────────────
    //
    // [[nodiscard]]: forces the caller to use the return value.
    // Trailing return type: `auto f() -> T` — preferred in modern C++
    // for consistency with lambdas and templates.
    //
    // noexcept on model(): returning a reference is safe.
    // noexcept on lastPythonError(): returning a const ref is safe.

    /// Mutable model reference — the view reads state through this.
    [[nodiscard]] constexpr auto model() noexcept -> model::AppModel& {
        return m_model;
    }

    /// Forward the last Python error string to the error bar in the view.
    [[nodiscard]] auto lastPythonError() const noexcept
        -> const std::string& {
        return m_python.lastError();
    }

private:
    // ── Data members ───────────────────────────────────────────────────
    //
    // Non-owning references. These do NOT manage lifetime — the objects
    // they point to must outlive the controller. In main(), the order is:
    //   1. AppModel model;      (created first, destroyed last)
    //   2. PythonEngine python;  (created second, destroyed second)
    //   3. AppController ctrl(model, python);  (created last, destroyed first)
    //
    // Destruction order is reverse of construction, so the controller
    // is destroyed before the objects it references. Safe.

    model::AppModel& m_model;    // owned by main(), outlives controller
    PythonEngine& m_python;      // owned by main(), outlives controller
};

}  // namespace nxs::controller
