// View layer: the Desmos-style single-page plotter, drawn with Dear ImGui
// widgets and an ImPlot chart.
#pragma once

#include "controller/PlotController.hpp"

namespace nxs::view {

class PlotterView {
public:
    explicit PlotterView(controller::PlotController& controller);

    void draw();

private:
    void drawSidebar();
    void drawChart();

    controller::PlotController& m_controller;
    int m_pendingFunction = 0;
};

}  // namespace nxs::view
