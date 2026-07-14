//==============================================================================
// nxs.desktop.model — Application Model (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Defines the root application state for a Nexus desktop app. The model owns
// the "truth" — a greeting string and a counter int — that the controller
// mutates and the view reads. Every other module in the app derives its
// transient state from this single source.
//
// == C++20 MODULE CONCEPTS ==
// This is a **module interface unit** (.cppm). The `export` keyword makes
// `AppModel` visible to any translation unit that writes
// `import nxs.desktop.model;`. Everything else in this module (the includes
// in the global fragment, the namespace internals) is private.
//
// == MVC ROLE ==
//   ┌─────────┐     ┌──────────────┐     ┌────────┐
//   │  Model  │◄────│  Controller  │◄────│  View  │
//   │ (state) │     │ (commands)   │     │ (UI)   │
//   └─────────┘     └──────────────┘     └────────┘
//          │                                    │
//          └──── AppModel ──── nxs::model ──────┘
//
// The model never references the controller or the view. The controller
// receives user intent, calls setter methods here, and the view re-reads
// every frame for its ImGui widgets.
//==============================================================================

module;  // global module fragment — everything before `export module` is private

// ── Standard library ──
#include <string>

export module nxs.desktop.model;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::model — Durable Application State
// ═══════════════════════════════════════════════════════════════════════════
//
// AppModel is a plain C++20 class. No virtual methods, no inheritance, no
// external dependencies beyond the STL. It is deliberately simple — all
// business complexity lives in the controller or service layer.
//
// Thread-safety note: Nexus desktop apps are single-threaded (ImGui frame
// loop). No locks needed on these accessors. If a future version uses worker
// threads, wrap access behind a mutex or an atomic<int> for the counter.

export namespace nxs::model {

export class AppModel {
public:
    /// Current counter value. Starts at 0.
    /// The controller increments this in response to user button clicks.
    [[nodiscard]] auto counter() const -> int { return m_counter; }

    /// Overwrite the counter. Called by AppController when the user
    /// triggers a counter action (increment, reset, or custom value).
    void setCounter(int value) noexcept { m_counter = value; }

    /// The greeting displayed in the main window title bar.
    /// The template placeholder {{projectName}} is substituted at generation
    /// time by the Nexus ProjectGenerator (Kotlin :core module).
    [[nodiscard]] auto greeting() const -> const std::string& { return m_greeting; }

    /// Change the greeting string at runtime.
    /// The view re-reads this every frame, so the title bar updates
    /// immediately on the next ImGui::Begin() call.
    void setGreeting(std::string value) { m_greeting = std::move(value); }

private:
    int m_counter = 0;
    std::string m_greeting = "Hello from {{projectName}}";
};

}  // namespace nxs::model
