#include "view/PlotterView.hpp"

#include <imgui.h>
#include <implot.h>

namespace nxs::view {

PlotterView::PlotterView(controller::PlotController& controller) : m_controller(controller) {}

void PlotterView::draw() {
    // One full-viewport window: this is a single-page app, like Desmos.
    const ImGuiViewport* vp = ImGui::GetMainViewport();
    ImGui::SetNextWindowPos(vp->WorkPos);
    ImGui::SetNextWindowSize(vp->WorkSize);
    ImGui::Begin("PlotterApp", nullptr,
                 ImGuiWindowFlags_NoDecoration | ImGuiWindowFlags_NoMove |
                     ImGuiWindowFlags_NoBringToFrontOnFocus);

    ImGui::Columns(2, "layout", true);
    ImGui::SetColumnWidth(0, 320.0f);
    drawSidebar();
    ImGui::NextColumn();
    drawChart();
    ImGui::Columns(1);

    ImGui::End();
}

void PlotterView::drawSidebar() {
    auto& registry = m_controller.registry();
    auto& settings = m_controller.settings();

    // ---- Add-function combo -------------------------------------------
    ImGui::TextUnformatted("Functions");
    ImGui::Separator();
    const auto& available = registry.available();
    const char* preview = available[m_pendingFunction].label.c_str();
    if (ImGui::BeginCombo("##function-picker", preview)) {
        for (int i = 0; i < static_cast<int>(available.size()); ++i) {
            bool selected = (i == m_pendingFunction);
            if (ImGui::Selectable(available[i].label.c_str(), selected)) {
                m_pendingFunction = i;
            }
        }
        ImGui::EndCombo();
    }
    ImGui::SameLine();
    if (ImGui::Button("Add")) {
        m_controller.addFunction(available[m_pendingFunction].id);
    }

    // ---- Active curve list: visibility, color, remove ------------------
    ImGui::Spacing();
    std::string toRemove;
    for (auto& series : registry.active()) {
        ImGui::PushID(series.spec.id.c_str());
        ImGui::Checkbox("##visible", &series.visible);
        ImGui::SameLine();
        ImGui::ColorEdit4("##color", series.color,
                          ImGuiColorEditFlags_NoInputs | ImGuiColorEditFlags_NoLabel);
        ImGui::SameLine();
        ImGui::TextUnformatted(series.spec.label.c_str());
        ImGui::SameLine(ImGui::GetColumnWidth() - 30.0f);
        if (ImGui::SmallButton("x")) {
            toRemove = series.spec.id;
        }
        ImGui::PopID();
    }
    if (!toRemove.empty()) {
        m_controller.removeFunction(toRemove);
    }

    // ---- Chart settings -------------------------------------------------
    ImGui::Spacing();
    ImGui::TextUnformatted("Chart");
    ImGui::Separator();
    double range[2] = {settings.xMin, settings.xMax};
    if (ImGui::InputScalarN("x range", ImGuiDataType_Double, range, 2)) {
        m_controller.setRange(range[0], range[1]);
    }
    int samples = settings.sampleCount;
    if (ImGui::SliderInt("samples", &samples, 64, 4096)) {
        m_controller.setSampleCount(samples);
    }
    ImGui::Checkbox("log-scale Y", &settings.logScaleY);
    ImGui::Checkbox("grid", &settings.showGrid);

    // ---- Python error surface ------------------------------------------
    if (!m_controller.lastPythonError().empty()) {
        ImGui::Spacing();
        ImGui::PushStyleColor(ImGuiCol_Text, IM_COL32(255, 96, 96, 255));
        ImGui::TextWrapped("Python: %s", m_controller.lastPythonError().c_str());
        ImGui::PopStyleColor();
    }
}

void PlotterView::drawChart() {
    auto& settings = m_controller.settings();

    ImPlotFlags plotFlags = ImPlotFlags_None;
    ImPlotAxisFlags axisFlags = settings.showGrid ? ImPlotAxisFlags_None : ImPlotAxisFlags_NoGridLines;

    // ImPlot gives us zoom (wheel) and pan (drag) for free; we only pin
    // the initial limits to the sampled x-range.
    if (ImPlot::BeginPlot("##chart", ImVec2(-1, -1), plotFlags)) {
        ImPlot::SetupAxes("x", "f(x)", axisFlags, axisFlags);
        ImPlot::SetupAxisScale(ImAxis_Y1,
                               settings.logScaleY ? ImPlotScale_Log10 : ImPlotScale_Linear);
        ImPlot::SetupAxisLimits(ImAxis_X1, settings.xMin, settings.xMax, ImGuiCond_Once);

        for (const auto& series : m_controller.registry().active()) {
            if (!series.visible || series.xs.empty()) {
                continue;
            }
            ImPlot::SetNextLineStyle(
                ImVec4(series.color[0], series.color[1], series.color[2], series.color[3]), 2.0f);
            ImPlot::PlotLine(series.spec.label.c_str(), series.xs.data(), series.ys.data(),
                             static_cast<int>(series.xs.size()));
        }
        ImPlot::EndPlot();
    }
}

}  // namespace nxs::view
