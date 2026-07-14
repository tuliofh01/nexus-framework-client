//==============================================================================
// nxs.android.view — ImGui View (C++20 Module Interface Unit)
//
// == WHAT THIS MODULE DOES ==
// Renders the main application window using Dear ImGui. Identical to
// nxs.desktop.view: draws greeting, counter, three buttons, and optional
// Python error bar. Stateless — reads from the controller every frame.
//
// == MODULE STRUCTURE ==
// Dear ImGui headers live in the global module fragment so importing
// modules never see them.
//==============================================================================

module;  // global module fragment

// ── Dear ImGui ──
#include <imgui.h>

export module nxs.android.view;

// ── Import peer modules ──
import nxs.android.controller;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — ImGui Viewport Renderer (Android)
// ═══════════════════════════════════════════════════════════════════════════

export namespace nxs::view {

/// Single-page ImGui view: greeting, counter, three buttons, optional
/// Python error bar. Created once in main() and called every frame.
export class AppView {
public:
    explicit AppView(controller::AppController& controller)
        : m_controller(controller) {}

    /// Called once per frame from the main loop. Lays out all widgets
    /// inside a fullscreen decorated window.
    void draw() {
        const ImGuiViewport* vp = ImGui::GetMainViewport();
        ImGui::SetNextWindowPos(vp->WorkPos);
        ImGui::SetNextWindowSize(vp->WorkSize);
        ImGui::Begin("{{projectName}}", nullptr,
                     ImGuiWindowFlags_NoDecoration |
                     ImGuiWindowFlags_NoMove |
                     ImGuiWindowFlags_NoBringToFrontOnFocus);

        auto& model = m_controller.model();
        ImGui::TextUnformatted(model.greeting().c_str());
        ImGui::Separator();
        ImGui::Text("Counter: %d", model.counter());

        if (ImGui::Button("Increment")) {
            m_controller.increment();
        }
        ImGui::SameLine();
        if (ImGui::Button("Decrement")) {
            m_controller.decrement();
        }
        ImGui::SameLine();
        if (ImGui::Button("Reset")) {
            m_controller.reset();
        }

        if (!m_controller.lastPythonError().empty()) {
            ImGui::Spacing();
            ImGui::PushStyleColor(ImGuiCol_Text, IM_COL32(255, 96, 96, 255));
            ImGui::TextWrapped("Python: %s",
                               m_controller.lastPythonError().c_str());
            ImGui::PopStyleColor();
        }

        ImGui::End();
    }

private:
    controller::AppController& m_controller;
};

}  // namespace nxs::view
