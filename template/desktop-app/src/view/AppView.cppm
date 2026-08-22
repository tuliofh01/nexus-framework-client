module;

#include <imgui.h>

#include <string>

export module nxs.desktop.view;

import nxs.desktop.controller;

export namespace nxs::view {

class AppView {
public:
    explicit AppView(controller::AppController& controller) noexcept
        : m_controller{controller} {}

    AppView(const AppView&) = delete;
    AppView& operator=(const AppView&) = delete;
    AppView(AppView&&) = delete;
    AppView& operator=(AppView&&) = delete;

    ~AppView() = default;

    void draw() {
        const auto* const vp = ImGui::GetMainViewport();
        ImGui::SetNextWindowPos(vp->WorkPos);
        ImGui::SetNextWindowSize(vp->WorkSize);

        ImGui::Begin("{{projectName}}", nullptr,
                     ImGuiWindowFlags_NoDecoration |
                     ImGuiWindowFlags_NoMove |
                     ImGuiWindowFlags_NoBringToFrontOnFocus);

        auto& model = m_controller.model();

        ImGui::TextUnformatted(model.greeting().c_str());
        ImGui::Spacing();
        ImGui::Separator();
        ImGui::Spacing();

        ImGui::Text("Counter: %d", model.counter());
        ImGui::Spacing();

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
