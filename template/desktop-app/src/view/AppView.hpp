// View layer: minimal ImGui starter — greeting + counter controls.
#pragma once

#include "controller/AppController.hpp"

namespace nxs::view {

class AppView {
public:
    explicit AppView(controller::AppController& controller);

    void draw();

private:
    controller::AppController& m_controller;
};

}  // namespace nxs::view
