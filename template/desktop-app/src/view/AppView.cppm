//==============================================================================
// nxs.desktop.view — ImGui Application View (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Renders the main application window using Dear ImGui. Draws the greeting,
// the counter, and the three control buttons (Increment / Decrement / Reset).
// The view is stateless — it reads from the controller/model every frame.
//
// ════════════════════════════════════════════════════════════════════════════
// MVC ROLE (Model-View-Controller)
// ════════════════════════════════════════════════════════════════════════════
//
// The view is the "dumb" terminal in MVC:
//   - Model:      holds state (counter, greeting) — never touched by view
//   - Controller: mediates — view calls controller methods, not model mutators
//   - View:       reads state from controller/model, emits button-click
//                 commands back to controller. Never mutates the model.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit constructor    — prevents implicit conversion from a single
//                           AppController& argument to AppView.
//
// noexcept                — guarantees no exceptions from construction.
//                           Enables compiler optimisations (e.g. std::move
//                           can skip try/catch blocks).
//
// = delete                — compile-time deletion of copy/move operations.
//                           Prevents accidental duplication of a view that
//                           holds a reference to the controller.
//
// const auto* const vp    — pointer to viewport, both the pointer and
//                           the pointed-to object are const (deep const).
//
// auto& model             — reference to the model obtained through the
//                           controller. `auto` deduces AppModel&.
//
// c_str()                 — std::string::c_str() returns a const char*
//                           required by ImGui's C API (TextUnformatted).
//
// IM_COL32(r,g,b,a)       — macro that packs RGBA into a 32-bit unsigned
//                           integer for ImGui's immediate-mode colour API.
//
// ════════════════════════════════════════════════════════════════════════════
// IMGUI IMMEDIATE-MODE PATTERN
// ════════════════════════════════════════════════════════════════════════════
//
// Immediate-mode UI: you "draw" widgets every frame. There is no retained
// widget tree. The return value of Button() is true on the frame it was
// clicked. This eliminates event handlers and callback registration.
//
// Flow: Begin() → widget calls → End(). Repeat ~60×/second.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// Dear ImGui headers — NOT exported to importers.
// These are C headers, not C++20 modules. They stay in the global module
// fragment so that `import nxs.desktop.view;` never exposes ImGui to
// downstream translation units.
#include <imgui.h>

export module nxs.desktop.view;

// ── Import peer modules ──
//
// The view depends on the controller to access model state and trigger
// actions. It does NOT import the model directly — the controller is
// the single gateway.
import nxs.desktop.controller;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — ImGui Viewport Renderer
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::view {

/// Single-page ImGui view: greeting, counter, three buttons, optional
/// Python error bar. Created once in main() and called every frame.
///
/// Lifetime: lives on the stack in main(). Holds a reference to the
/// controller — must outlive the controller it references.
export class AppView {
public:
    /// explicit: prevents implicit conversion from AppController& to AppView.
    /// noexcept: guarantees no exceptions from construction.
    explicit AppView(controller::AppController& controller) noexcept
        : m_controller{controller} {}  // {} brace-init for reference member

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Non-copyable: the view holds a reference to the controller.
    // Copying would create a dangling-reference hazard if the original
    // goes out of scope. = delete prevents this at compile time.
    //
    // Non-movable: references cannot be reseated in C++. If we moved
    // the view, the reference would still point to the original object.
    // Preventing move makes the ownership model explicit.

    AppView(const AppView&) = delete;
    AppView& operator=(const AppView&) = delete;
    AppView(AppView&&) = delete;
    AppView& operator=(AppView&&) = delete;

    /// Defaulted destructor — nothing to clean up (reference is not owned).
    ~AppView() = default;

    /// Called once per frame from the main loop.
    ///
    /// ImGui immediate-mode pattern:
    ///   1. SetNextWindowPos/Size: position the window fullscreen
    ///   2. Begin/End: wrap all widget calls
    ///   3. Widget return values: Button() returns true on click frame
    ///
    /// The view reads state (greeting, counter) from the controller and
    /// calls controller methods on button clicks. It never touches the
    /// model directly.
    void draw() {
        // ── Window setup ───────────────────────────────────────────────
        //
        // ImGui::GetMainViewport(): returns the main display viewport.
        // WorkPos/WorkSize: excludes OS taskbar/dock (the "work area").
        //
        // const auto* const vp: deep const — the pointer AND the pointed-
        // to object are immutable. Good practice for read-only access.
        const auto* const vp = ImGui::GetMainViewport();
        ImGui::SetNextWindowPos(vp->WorkPos);
        ImGui::SetNextWindowSize(vp->WorkSize);

        // Window flags disable decoration (title bar, resize, close),
        // movement (locked to fullscreen), and z-ordering (never pops
        // in front of other windows). This creates an "app shell" feel.
        ImGui::Begin("{{projectName}}", nullptr,
                     ImGuiWindowFlags_NoDecoration |
                     ImGuiWindowFlags_NoMove |
                     ImGuiWindowFlags_NoBringToFrontOnFocus);

        // ── Read state through the controller ──────────────────────────
        //
        // auto& model: deduces AppModel& from controller.model().
        // We never call model methods directly — always through controller.
        auto& model = m_controller.model();

        // TextUnformatted: raw string display, no printf-style parsing.
        // model.greeting() returns std::string; .c_str() gives the const
        // char* that ImGui's C API expects.
        ImGui::TextUnformatted(model.greeting().c_str());
        ImGui::Separator();
        ImGui::Text("Counter: %d", model.counter());

        // ── Button row ─────────────────────────────────────────────────
        //
        // ImGui::Button() returns true on the frame it was clicked.
        // This is the immediate-mode pattern: no callbacks, no event
        // handlers. We check the return value and call the controller.
        //
        // SameLine(): places the next widget on the same horizontal line.
        // Without it, each button would be on its own row.

        if (ImGui::Button("Increment")) {
            m_controller.increment();  // controller mutates model
        }
        ImGui::SameLine();
        if (ImGui::Button("Decrement")) {
            m_controller.decrement();
        }
        ImGui::SameLine();
        if (ImGui::Button("Reset")) {
            m_controller.reset();
        }

        // ── Error display ──────────────────────────────────────────────
        //
        // Only shown when the Python engine has an error.
        // PushStyleColor/PopStyleColor: RAII-style colour stack.
        // PushStyleColor pushes a colour override; PopStyleColor pops it.
        // IM_COL32(r,g,b,a): packs four 8-bit channels into a 32-bit uint.
        // TextWrapped: word-wraps long error messages.

        if (!m_controller.lastPythonError().empty()) {
            ImGui::Spacing();
            ImGui::PushStyleColor(ImGuiCol_Text, IM_COL32(255, 96, 96, 255));
            ImGui::TextWrapped("Python: %s",
                               m_controller.lastPythonError().c_str());
            ImGui::PopStyleColor();  // RAII: restore original colour
        }

        ImGui::End();  // must match Begin()
    }

private:
    // Reference to the controller — NOT owned. The view does not create
    // or destroy the controller. The reference is immutable (cannot be
    // reseated after construction).
    controller::AppController& m_controller;
};

}  // namespace nxs::view
