// Controller layer: reacts to UI/Lua/flow commands and updates the model.
#pragma once

#include "controller/PythonEngine.hpp"
#include "model/AppModel.hpp"

#include <string>

namespace nxs::controller {

class AppController {
public:
    AppController(model::AppModel& model, PythonEngine& python);

    void increment();
    void decrement();
    void reset();

    // Optional Python greeting refresh (helpers.greeting).
    void refresh();

    model::AppModel& model() { return m_model; }
    const std::string& lastPythonError() const { return m_python.lastError(); }

private:
    model::AppModel& m_model;
    PythonEngine& m_python;
};

}  // namespace nxs::controller
