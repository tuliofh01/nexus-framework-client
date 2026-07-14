//==============================================================================
// nxs.android.controller — Application Controller (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Same role as nxs.desktop.controller: listens to user actions from the
// view, mutates the model, and coordinates with the Python engine. The
// only difference is that Android methods are not noexcept (matches the
// original Android codebase style).
//
// == MODERN C++ ==
// Uses RAII (non-owning references to main() locals), [[nodiscard]] on
// const getters, constexpr on simple accessors, deleted copy/move,
// trailing return types, and brace initialization.
//
// == MVC ROLE ==
//   View (user clicks)  ──►  Controller  ──►  Model (state)
//                                     │
//                                     └──►  PythonEngine (greeting)
//==============================================================================

module;  // global module fragment

// ── Standard library ──
#include <cstdio>
#include <string>
#include <string_view>
#include <utility>

export module nxs.android.controller;

// ── Import peer modules ──
import nxs.android.model;
import nxs.android.python;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::controller — Command Dispatcher (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::controller {

export class AppController {
public:
    /// Store references to the model and Python engine. Both outlive the
    /// controller because they are created in main() before the controller.
    AppController(model::AppModel& model, PythonEngine& python)
        : m_model{model}, m_python{python} {}

    /// Non-copyable, non-movable — references bind to main() locals.
    AppController(const AppController&) = delete;
    AppController& operator=(const AppController&) = delete;
    AppController(AppController&&) = delete;
    AppController& operator=(AppController&&) = delete;

    ~AppController() = default;

    // ── Counter commands ───────────────────────────────────────────────

    /// Increase the counter by one.
    constexpr void increment() {
        m_model.setCounter(m_model.counter() + 1);
    }

    /// Decrease the counter by one.
    constexpr void decrement() {
        m_model.setCounter(m_model.counter() - 1);
    }

    /// Reset the counter to zero.
    constexpr void reset() { m_model.setCounter(0); }

    // ── Python greeting refresh ────────────────────────────────────────

    /// Evaluate helpers.greeting() via the Chaquopy/Djinni bridge.
    void refresh() {
        const auto greeting = m_python.greeting("{{projectName}}"sv);
        if (!greeting.empty()) {
            m_model.setGreeting(greeting);
        }
    }

    // ── Accessors for the view layer ───────────────────────────────────

    [[nodiscard]] auto model() -> model::AppModel& { return m_model; }
    [[nodiscard]] auto lastPythonError() const -> const std::string& {
        return m_python.lastError();
    }

private:
    model::AppModel& m_model;
    PythonEngine& m_python;
};

}  // namespace nxs::controller

using namespace std::string_view_literals;
