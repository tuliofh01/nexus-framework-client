#include "controller/AppController.hpp"

namespace nxs::controller {

AppController::AppController(model::AppModel& model, PythonEngine& python)
    : m_model(model), m_python(python) {}

void AppController::increment() noexcept {
    m_model.setCounter(m_model.counter() + 1);
}

void AppController::decrement() noexcept {
    m_model.setCounter(m_model.counter() - 1);
}

void AppController::reset() noexcept {
    m_model.setCounter(0);
}

void AppController::refresh() {
    const auto greeting = m_python.greeting("{{projectName}}");
    if (!greeting.empty()) {
        m_model.setGreeting(greeting);
    }
}

}  // namespace nxs::controller
