// Controller layer: reacts to UI/Lua/flow commands and updates the model.
#pragma once

#include "controller/PythonEngine.hpp"
#include "model/AppModel.hpp"

#include <string>
#include <string_view>

namespace nxs::controller {

class AppController {
public:
    AppController(model::AppModel& model, PythonEngine& python);

    void increment() noexcept;
    void decrement() noexcept;
    void reset() noexcept;

    /// Optional Python greeting refresh (helpers.greeting).
    void refresh();

    [[nodiscard]] auto model() noexcept -> model::AppModel& { return m_model; }
    [[nodiscard]] auto lastPythonError() const -> const std::string& { return m_python.lastError(); }

private:
    model::AppModel& m_model;
    PythonEngine& m_python;
};

}  // namespace nxs::controller
