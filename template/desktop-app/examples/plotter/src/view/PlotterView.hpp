// View layer: the Desmos-style single-page plotter, drawn with Dear ImGui
// widgets and an ImPlot chart. Stateless apart from transient widget
// state — everything durable lives in the model/controller.
#pragma once

#include "controller/PlotController.hpp"

namespace nxs::view {

class PlotterView {
public:
    explicit PlotterView(controller::PlotController& controller);

    // Draws the full page. Call once per frame between ImGui::NewFrame()
    // and ImGui::Render().
    void draw();

private:
    void drawSidebar();  // function picker, per-curve controls, chart settings
    void drawChart();    // the ImPlot plot itself

    controller::PlotController& m_controller;
    int m_pendingFunction = 0;  // index into registry().available() for the combo
};

}  // namespace nxs::view
