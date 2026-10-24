//==============================================================================
// nxs.desktop.view — Chart host (C++20 Module Interface Unit)
//
// Per the architecture split, C++ owns the *compute* and the rendering host.
// Widgets and controls live in Lua (scripts/panels.lua) and the TS/XHTML
// interface (ui/ui.xhtml + ui/ui.ts). This view therefore contains no
// training widgets and no menu buttons — it only hosts the ImPlot canvas.
//==============================================================================

module;

#include <imgui.h>
#include <implot.h>

#include <string>

export module nxs.desktop.view;

import nxs.desktop.controller;
import nxs.desktop.plot;
import nxs.desktop.func;

export namespace nxs::view {

class AppView {
public:
    explicit AppView(controller::AppController& app,
                     controller::PlotController& plot) noexcept
        : m_app{app}, m_plot{plot} {}

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

        ImGui::TextUnformatted(m_app.model().greeting().c_str());
        ImGui::Separator();
        drawChart();

        if (!m_app.lastPythonError().empty()) {
            ImGui::Spacing();
            ImGui::PushStyleColor(ImGuiCol_Text, IM_COL32(255, 96, 96, 255));
            ImGui::TextWrapped("Python: %s",
                               m_app.lastPythonError().c_str());
            ImGui::PopStyleColor();
        }

        ImGui::End();
    }

private:
    void drawChart() {
        if (!ImPlot::BeginPlot("##chart", ImVec2(-1.0f, -1.0f))) {
            return;
        }
        ImPlot::SetupAxes("x", "y",
                          ImPlotAxisFlags_AutoFit,
                          ImPlotAxisFlags_AutoFit);

        // Iterate the model's active series; the controller has already
        // sampled them through Python (functions.evaluate).
        for (const auto& series : m_plot.registry().active()) {
            if (!series.visible || series.xs.empty()) {
                continue;
            }
            ImPlot::SetNextLineStyle(
                ImVec4(series.color[0], series.color[1],
                       series.color[2], series.color[3]),
                2.0f);
            ImPlot::PlotLine(series.spec.label.c_str(),
                             series.xs.data(), series.ys.data(),
                             static_cast<int>(series.xs.size()));
        }
        ImPlot::EndPlot();
    }

    controller::AppController& m_app;
    controller::PlotController& m_plot;
};

}  // namespace nxs::view
