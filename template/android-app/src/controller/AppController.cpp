#include "controller/AppController.hpp"

namespace nxs::controller {

AppController::AppController(model::AppModel& model, PythonEngine& python)
    : m_model(model), m_python(python) {}

void AppController::increment() {
    m_model.setCounter(m_model.counter() + 1);
}

void AppController::decrement() {
    m_model.setCounter(m_model.counter() - 1);
}

void AppController::reset() {
    m_model.setCounter(0);
}

void AppController::refresh() {
    const std::string greeting = m_python.greeting("{{projectName}}");
    if (!greeting.empty()) {
        m_model.setGreeting(greeting);
    }
}

}  // namespace nxs::controller
