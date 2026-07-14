//==============================================================================
// nxs.android.model — Application Model (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Defines the root application state for a Nexus Android app. Identical
// in purpose to nxs.desktop.model: a greeting string and a counter int
// that the controller mutates and the view reads.
//
// == MODERN C++ ==
// Uses RAII (value-owning members), constexpr accessors, [[nodiscard]]
// on const getters, brace initialization, trailing return types, and
// std::move on setter params.
//
// == DESKTOP VS ANDROID ==
// Android AppModel noexcept on getters (SDL3/GLES on Android uses a
// simpler C++ subset). The API surface is otherwise identical so
// blueprint.json nodes wire correctly on both platforms.
//==============================================================================

module;  // global module fragment — everything before export is private

// ── Standard library ──
#include <string>
#include <utility>

export module nxs.android.model;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::model — Durable Application State (Android)
// ═══════════════════════════════════════════════════════════════════════════
//
// AppModel is a plain C++20 class. No virtual methods, no inheritance, no
// external dependencies beyond the STL. Thread-safety: Nexus Android apps
// are single-threaded (ImGui frame loop on the SDL3 main thread). No locks
// needed on these accessors.

export namespace nxs::model {

export class AppModel {
public:
    AppModel() = default;
    ~AppModel() = default;

    AppModel(const AppModel&) = default;
    AppModel& operator=(const AppModel&) = default;
    AppModel(AppModel&&) noexcept = default;
    AppModel& operator=(AppModel&&) noexcept = default;

    /// Current counter value. Starts at 0.
    [[nodiscard]] constexpr auto counter() const noexcept -> int {
        return m_counter;
    }

    /// Overwrite the counter. Called by AppController when the user
    /// triggers a counter action.
    constexpr void setCounter(int value) noexcept { m_counter = value; }

    /// The greeting displayed in the main window title bar.
    /// {{projectName}} is substituted at generation time by :core.
    [[nodiscard]] auto greeting() const -> const std::string& {
        return m_greeting;
    }

    /// Change the greeting string at runtime.
    void setGreeting(std::string value) { m_greeting = std::move(value); }

private:
    int m_counter{0};
    std::string m_greeting{"Hello from {{projectName}}"};
};

}  // namespace nxs::model
