#include "view/AppView.hpp"

#include <imgui.h>

namespace nxs::view {

AppView::AppView(controller::AppController& controller) : m_controller(controller) {}

void AppView::draw() {
    const auto* vp = ImGui::GetMainViewport();
    ImGui::SetNextWindowPos(vp->WorkPos);
    ImGui::SetNextWindowSize(vp->WorkSize);
    ImGui::Begin("{{projectName}}", nullptr,
                 ImGuiWindowFlags_NoDecoration | ImGuiWindowFlags_NoMove |
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
        ImGui::TextWrapped("Python: %s", m_controller.lastPythonError().c_str());
        ImGui::PopStyleColor();
    }

    ImGui::End();
}

}  // namespace nxs::view
