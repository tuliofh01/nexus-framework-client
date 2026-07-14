//==============================================================================
// nxs.desktop.controller — Application Controller (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Listens to user actions from the view (and Lua/flows), mutates the model,
// and coordinates with the Python engine. This is the "middleware" of the MVC
// triad — it owns no state, only references to the model and Python engine.
//
// == MVC ROLE ==
//   View (user clicks)  ──►  Controller  ──►  Model (state)
//                                     │
//                                     └──►  PythonEngine (greeting, eval)
//
// The view never touches the model directly. All mutations go through
// controller methods like increment(), decrement(), reset(), and refresh().
//==============================================================================

module;  // global module fragment — private to this translation unit

// ── Standard library ──
#include <cstdio>
#include <string>
#include <string_view>

export module nxs.desktop.controller;

// ── Import peer modules ──
import nxs.desktop.model;
import nxs.desktop.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Command Dispatcher
// ═══════════════════════════════════════════════════════════════════════════
//
// AppController wires user intent to model mutations. Each public method
// corresponds to a UI action or a Lua/flow trigger. The `model()` accessor
// gives the view read access to the current state without exposing mutable
// references to the controller internals.

export namespace nxs::controller {

export class AppController {
public:
    /// Store references to the model and Python engine. Both outlive the
    /// controller because they are created in main() before the controller.
    AppController(model::AppModel& model, PythonEngine& python)
        : m_model(model), m_python(python) {}

    // ── Counter commands ───────────────────────────────────────────────

    /// Increase the counter by one. Called by the Increment button or
    /// a Lua/flow `nxs.increment` invoke.
    void increment() noexcept { m_model.setCounter(m_model.counter() + 1); }

    /// Decrease the counter by one. Called by the Decrement button or
    /// a Lua `nxs.decrement` invoke.
    void decrement() noexcept { m_model.setCounter(m_model.counter() - 1); }

    /// Reset the counter to zero. Called by the Reset button or a
    /// Lua/flow `nxs.reset` invoke.
    void reset() noexcept { m_model.setCounter(0); }

    // ── Python greeting refresh ────────────────────────────────────────

    /// Evaluate `helpers.greeting()` in the embedded Python interpreter.
    /// The result overwrites the model's greeting string if non-empty.
    /// Errors are silently swallowed — the view surfaces them via
    /// lastPythonError().
    void refresh() {
        const auto greeting = m_python.greeting("{{projectName}}");
        if (!greeting.empty()) {
            m_model.setGreeting(greeting);
        }
    }

    // ── Accessors for the view layer ───────────────────────────────────

    /// Mutable model reference — the view reads state through this.
    [[nodiscard]] auto model() noexcept -> model::AppModel& { return m_model; }

    /// Forward the last Python error string to the error bar in the view.
    [[nodiscard]] auto lastPythonError() const -> const std::string& {
        return m_python.lastError();
    }

private:
    model::AppModel& m_model;    // owned by main(), outlives controller
    PythonEngine& m_python;      // owned by main(), outlives controller
};

}  // namespace nxs::controller
