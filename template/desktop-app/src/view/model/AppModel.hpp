// Model layer: durable app state. The controller mutates this; the view reads it.
#pragma once

#include <string>

namespace nxs::model {

class AppModel {
public:
    int counter() const { return m_counter; }
    void setCounter(int value) { m_counter = value; }

    const std::string& greeting() const { return m_greeting; }
    void setGreeting(std::string value) { m_greeting = std::move(value); }

private:
    int m_counter = 0;
    std::string m_greeting = "Hello from {{projectName}}";
};

}  // namespace nxs::model
