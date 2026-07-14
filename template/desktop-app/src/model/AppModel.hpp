// Model layer: durable app state. The controller mutates this; the view reads it.
#pragma once

#include <string>

namespace nxs::model {

class AppModel {
public:
    [[nodiscard]] auto counter() const -> int { return m_counter; }
    void setCounter(int value) noexcept { m_counter = value; }

    [[nodiscard]] auto greeting() const -> const std::string& { return m_greeting; }
    void setGreeting(std::string value) { m_greeting = std::move(value); }

private:
    int m_counter = 0;
    std::string m_greeting = "Hello from {{projectName}}";
};

}  // namespace nxs::model
