//==============================================================================
// nxs.desktop.model — Application Model (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Defines the root application state for a Nexus desktop app. The model
// is the single source of truth for all data the view reads: a greeting
// string and a counter integer. The controller mutates this state; the
// view reads it; the model never initiates actions itself.
//
// ════════════════════════════════════════════════════════════════════════════
// C++20 MODULE STRUCTURE (for readers new to modules)
// ════════════════════════════════════════════════════════════════════════════
//
// A C++20 module has three parts:
//
//   1. module;           — the "global module fragment". Everything here
//                          (#include, macros, private helpers) is visible
//                          ONLY to this .cppm file. Importers never see it.
//                          Think of it as the "private implementation" section.
//
//   2. export module X;  — declares this file as the interface for module X.
//                          Other files reach this code via `import X;`.
//
//   3. export namespace/class/func — the public API that importers can use.
//
// This replaces the old .hpp/.cpp split: one .cppm file IS both header
// and source. The compiler handles the rest.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// [[nodiscard]]    — compiler warns if caller ignores the return value.
//                    Use on every getter that returns by value/reference.
//
// constexpr        — function can be evaluated at compile time when inputs
//                    are known at compile time. Zero runtime cost.
//
// noexcept         — function承诺 not to throw. Enables optimisations
//                    (std::vector can move elements instead of copying).
//
// trailing return   — `auto f() -> int` instead of `int f()`. Preferred in
// type               modern C++ for consistency with lambdas and templates.
//
// {} init          — brace-initialization. Prevents narrowing conversions
//                    (e.g. double → int) and zero-initialises scalars.
//
// = delete         — explicitly prevents copy/move. Use when the class
//                    holds non-owning references that must not be dangling.
//
// std::move        — transfers ownership of a string into the member.
//                    Avoids a deep copy when the caller no longer needs
//                    the original.
//
// ════════════════════════════════════════════════════════════════════════════
// THREAD SAFETY
// ════════════════════════════════════════════════════════════════════════════
//
// Nexus desktop apps are single-threaded (ImGui frame loop on the main
// thread). No locks or atomics are needed on these accessors.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// Standard library headers that the IMPLEMENTATION needs but that we do
// NOT want to leak to importers. Everything before `export module` is
// invisible to code that does `import nxs.desktop.model;`.
#include <string>       // std::string — our greeting storage
#include <utility>      // std::move — ownership transfer for setters

export module nxs.desktop.model;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::model — Durable Application State
// ═══════════════════════════════════════════════════════════════════════════
//
// AppModel is a plain C++20 value type. No virtual methods, no inheritance,
// no heap allocation beyond the std::string member. It is cheap to copy
// (though we choose to pass by reference to avoid even that).
//
// Design rule: the model is PASSIVE. It never calls the controller or
// view. It only stores data and provides accessors.

export namespace nxs::model {

class AppModel {
public:
    // ── Construction / Destruction ─────────────────────────────────────
    //
    // default constructor: zero-initialises m_counter, default-constructs
    // m_greeting to "Hello from {{projectName}}".
    //
    // = default tells the compiler to generate the obvious implementation.
    // We write it explicitly so readers see the five special members.

    AppModel() = default;
    ~AppModel() = default;

    // Copy: allowed (model is a simple value type).
    AppModel(const AppModel&) = default;
    AppModel& operator=(const AppModel&) = default;

    // Move: allowed and noexcept (std::string has a noexcept move).
    AppModel(AppModel&&) noexcept = default;
    AppModel& operator=(AppModel&&) noexcept = default;

    // ── Counter accessors ──────────────────────────────────────────────
    //
    // counter() returns the current value. [[nodiscard]] means the
    // compiler warns if someone writes `model.counter();` and throws
    // away the result — almost certainly a bug.
    //
    // constexpr + noexcept: this is a trivial getter. It can run at
    // compile time and will never throw. Zero runtime overhead.

    [[nodiscard]] constexpr auto counter() const noexcept -> int {
        return m_counter;
    }

    // setCounter: mutates the counter. constexpr because it's a simple
    // assignment. noexcept because int assignment cannot throw.
    constexpr void setCounter(int value) noexcept {
        m_counter = value;
    }

    // ── Greeting accessors ─────────────────────────────────────────────
    //
    // greeting() returns a const reference — no copy, just a view into
    // the stored string. [[nodiscard]] guards against accidental discard.
    //
    // NOT constexpr: std::string operations are not constexpr in C++20
    // (they are in C++26, but we target C++20).
    //
    // NOT noexcept: returning a const ref to std::string is safe, but
    // we follow the convention that string accessors are not noexcept
    // because downstream operations on the string may throw.

    [[nodiscard]] auto greeting() const -> const std::string& {
        return m_greeting;
    }

    // setGreeting: takes a std::string BY VALUE, then std::move's it in.
    // This is the "pass-by-value sink" idiom:
    //
    //   - If the caller passes an lvalue (e.g. a local variable), it is
    //     copied once into the parameter, then moved into m_greeting.
    //   - If the caller passes an rvalue (e.g. std::string("hi")), it is
    //     moved directly into the parameter, then moved into m_greeting.
    //
    // Net effect: at most one copy, one move. Often zero copies.
    // This is better than `const std::string&` + copy because it avoids
    // a separate copy when the caller already has a temporary.

    void setGreeting(std::string value) {
        m_greeting = std::move(value);
    }

private:
    // ── Data members ───────────────────────────────────────────────────
    //
    // {} brace-initialisation: m_counter{0} zero-initialises the int.
    // Without {}, an uninitialized int would contain garbage (undefined
    // behaviour if read before assignment). {} is always safe.
    //
    // m_greeting{"Hello from {{projectName}}"}: the {{projectName}}
    // placeholder is replaced by the Kotlin :core generator when the
    // project is scaffolded.

    int m_counter{0};
    std::string m_greeting{"Hello from {{projectName}}"};
};

}  // namespace nxs::model
