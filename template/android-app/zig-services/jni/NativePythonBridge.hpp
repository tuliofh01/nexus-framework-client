#pragma once

#include "python_bridge.hpp"

#include <jni.h>

#include <memory>

namespace nxs::bridge::jni {

class NativePythonBridge final : public PythonBridge {
public:
    NativePythonBridge(JNIEnv* env, jobject kotlinBridge);
    ~NativePythonBridge() override;

    std::string greeting(const std::string& name) override;

    EvalResult evaluate(const std::string& function_name, double x_min, double x_max,
                        int32_t samples) override;

private:
    JavaVM* m_vm = nullptr;
    jobject m_kotlinBridge = nullptr;
    jmethodID m_greeting = nullptr;
    jmethodID m_evaluate = nullptr;
};

}  // namespace nxs::bridge::jni
