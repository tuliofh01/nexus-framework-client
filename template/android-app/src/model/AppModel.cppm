//==============================================================================
// nxs.android.model — Application Model (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Defines the root application state for a Nexus Android app. Identical
// in purpose to nxs.desktop.model: a greeting string and a counter int
// that the controller mutates and the view reads.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// = default                — compiler generates copy/move (trivial members)
// = default + noexcept     — move constructor/assignment are noexcept
// [[nodiscard]]            — return values must not be ignored
// constexpr                — counter methods: compile-time evaluable
// noexcept                 — simple accessors and mutators never throw
// {} brace-init            — uniform initialization for all members
// auto fn() -> Type        — trailing return type syntax
// std::move                — transfers ownership in setGreeting() setter
// std::string_view         — (used by controller, stored as std::string)
//
// ════════════════════════════════════════════════════════════════════════════
// THREAD SAFETY
// ════════════════════════════════════════════════════════════════════════════
//
// Nexus Android apps are single-threaded (ImGui frame loop on the SDL3
// main thread). No locks needed on these accessors. The model is a plain
// C++20 class with no virtual methods, no inheritance, and no external
// dependencies beyond the STL.
//
// ════════════════════════════════════════════════════════════════════════════
// DESKTOP VS ANDROID
// ════════════════════════════════════════════════════════════════════════════
//
// Android AppModel adds noexcept on all methods (SDL3/GLES on Android uses
// a simpler C++ subset). The API surface is otherwise identical so
// blueprint.json nodes wire correctly on both platforms.
//==============================================================================

module;  // ── global module fragment — everything before export is private ──

// ── Standard library (private to this module) ──
#include <string>       // std::string for greeting storage
#include <utility>      // std::move for ownership transfer in setter

export module nxs.android.model;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::model — Durable Application State (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::model {

/// Root application state for a Nexus Android app.
///
/// AppModel is a plain C++20 class. It holds:
///   - m_counter:  an integer counter (starts at 0)
///   - m_greeting: a display string (template placeholder at generation)
///
/// The controller is the sole mutator. The view reads through the
/// controller. This is the MVC "M" — it knows nothing about UI or
/// Python.
export class AppModel {
public:
    AppModel() = default;
    ~AppModel() = default;

    // ── Rule of Five: defaulted copy/move ──────────────────────────────
    //
    // Unlike the controller and view (which hold references), the model
    // owns all its state by value. Copy and move are safe and useful:
    // the counter and string can be duplicated freely.
    //
    // Move is noexcept: std::string's move constructor is noexcept in
    // C++17+. Marking it noexcept enables compiler optimisations (e.g.
    // containers resize without extra copies).

    AppModel(const AppModel&) = default;
    AppModel& operator=(const AppModel&) = default;
    AppModel(AppModel&&) noexcept = default;
    AppModel& operator=(AppModel&&) noexcept = default;

    // ── Counter accessors ──────────────────────────────────────────────

    /// Current counter value. Starts at 0.
    ///
    /// [[nodiscard]]: callers must not ignore the return value.
    /// constexpr: trivially computable at compile time.
    /// noexcept: no exceptions possible.
    [[nodiscard]] constexpr auto counter() const noexcept -> int {
        return m_counter;
    }

    /// Overwrite the counter. Called by AppController when the user
    /// triggers a counter action.
    constexpr void setCounter(int value) noexcept { m_counter = value; }

    // ── Greeting accessors ─────────────────────────────────────────────

    /// The greeting displayed in the main window title bar.
    /// {{projectName}} is substituted at generation time by :core.
    ///
    /// Returns a const reference to avoid copying the string.
    /// noexcept: returning a reference is safe (no allocation).
    [[nodiscard]] auto greeting() const noexcept -> const std::string& {
        return m_greeting;
    }

    /// Change the greeting string at runtime.
    ///
    /// std::string value: taken by value (sink idiom), then std::move'd
    /// into the member. If the caller passes a temporary, the move is
    /// free; if they pass an lvalue, they must explicitly std::move.
    /// This avoids overloading for const std::string& and std::string&&.
    void setGreeting(std::string value) noexcept {
        m_greeting = std::move(value);
    }

private:
    int m_counter{0};                                     ///< Starts at 0
    std::string m_greeting{"Hello from {{projectName}}"}; ///< Template placeholder
};

}  // namespace nxs::model
