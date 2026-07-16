//==============================================================================
// nxs.android.view — ImGui View (C++20 Module Interface Unit)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Renders the main application window using Dear ImGui. Identical to
// nxs.desktop.view: draws greeting, counter, three buttons, and optional
// Python error bar. Stateless — reads from the controller every frame.
//
// ════════════════════════════════════════════════════════════════════════════
// MODERN C++ IDIOMS USED HERE
// ════════════════════════════════════════════════════════════════════════════
//
// explicit            — prevents implicit conversion from AppController&
// noexcept            — constructor guarantees no exceptions
// = delete            — Rule of Five: non-copyable, non-movable
// const auto* const   — deep const (pointer AND target are immutable)
// auto&               — type deduction for model reference
// c_str()             — std::string → const char* for ImGui C API
// {} brace-init       — uniform initialization for reference member
// IM_COL32            — packs RGBA into 32-bit uint for ImGui
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
//
// ════════════════════════════════════════════════════════════════════════════
// MODULE STRUCTURE
// ════════════════════════════════════════════════════════════════════════════
//
// Dear ImGui headers live in the global module fragment so importing
// modules never see them.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// Dear ImGui headers — NOT exported to importers.
#include <imgui.h>

export module nxs.android.view;

// ── Import peer modules ──
//
// The view depends on the controller to access model state and trigger
// actions. It does NOT import the model directly — the controller is
// the single gateway.
import nxs.android.controller;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — ImGui Viewport Renderer (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::view {

/// Single-page ImGui view: greeting, counter, three buttons, optional
/// Python error bar. Created once in main() and called every frame.
///
/// RAII: non-owning reference to controller; no heap allocations.
/// Lifetime: stack-allocated in main(), must outlive the controller.
class AppView {
public:
    /// explicit: prevents implicit conversion from AppController& to AppView.
    /// noexcept: guarantees no exceptions from construction.
    explicit AppView(controller::AppController& controller) noexcept
        : m_controller{controller} {}  // {} brace-init for reference

    // ── Rule of Five: delete copy and move ─────────────────────────────
    //
    // Non-copyable: holds a reference to the controller. Copying would
    // create a dangling-reference hazard.
    // Non-movable: references cannot be reseated in C++.

    AppView(const AppView&) = delete;
    AppView& operator=(const AppView&) = delete;
    AppView(AppView&&) = delete;
    AppView& operator=(AppView&&) = delete;

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
    /// model directly — that is the MVC boundary.
    void draw() {
        // ── Window setup ───────────────────────────────────────────────
        //
        // ImGui::GetMainViewport(): returns the main display viewport.
        // WorkPos/WorkSize: excludes OS navigation bars (the "work area").
        //
        // const auto* const vp: deep const — the pointer AND the pointed-
        // to object are immutable. This documents read-only access.
        const auto* const vp = ImGui::GetMainViewport();
        ImGui::SetNextWindowPos(vp->WorkPos);
        ImGui::SetNextWindowSize(vp->WorkSize);

        // Window flags disable decoration, movement, and z-ordering.
        // This creates a fullscreen "app shell" with no title bar.
        ImGui::Begin("{{projectName}}", nullptr,
                     ImGuiWindowFlags_NoDecoration |
                     ImGuiWindowFlags_NoMove |
                     ImGuiWindowFlags_NoBringToFrontOnFocus);

        // ── Read state through the controller ──────────────────────────
        //
        // auto& model: deduces AppModel& from controller.model().
        // We never call model() directly — always go through the controller
        // to maintain the MVC boundary.

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
    controller::AppController& m_controller;  ///< Non-owning reference
};

}  // namespace nxs::view
