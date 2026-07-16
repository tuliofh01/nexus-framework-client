#include "NativePythonBridge.hpp"

#include <cstring>

namespace nxs::bridge::jni {

NativePythonBridge::NativePythonBridge(JNIEnv* env, jobject kotlinBridge) {
    env->GetJavaVM(&m_vm);
    m_kotlinBridge = env->NewGlobalRef(kotlinBridge);

    jclass cls = env->GetObjectClass(kotlinBridge);
    m_greeting = env->GetMethodID(cls, "greeting", "(Ljava/lang/String;)Ljava/lang/String;");
    m_evaluate = env->GetMethodID(
        cls, "evaluate",
        "(Ljava/lang/String;DDI)Lcom/nexus/{{packageName}}/EvalResult;");
}

NativePythonBridge::~NativePythonBridge() {
    if (m_vm) {
        JNIEnv* env = nullptr;
        m_vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
        if (env && m_kotlinBridge) {
            env->DeleteGlobalRef(m_kotlinBridge);
        }
    }
}

std::string NativePythonBridge::greeting(const std::string& name) {
    JNIEnv* env = nullptr;
    bool needsDetach = false;
    jint getEnvResult = m_vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

    if (getEnvResult == JNI_EDETACHED) {
        m_vm->AttachCurrentThread(&env, nullptr);
        needsDetach = true;
    }

    if (!env || !m_kotlinBridge || !m_greeting) {
        if (needsDetach) m_vm->DetachCurrentThread();
        return "Hello, " + name + "!";
    }

    jstring jName = env->NewStringUTF(name.c_str());
    jstring jResult = static_cast<jstring>(
        env->CallObjectMethod(m_kotlinBridge, m_greeting, jName));

    const char* utf = env->GetStringUTFChars(jResult, nullptr);
    std::string result(utf);
    env->ReleaseStringUTFChars(jResult, utf);
    env->DeleteLocalRef(jName);
    env->DeleteLocalRef(jResult);

    if (needsDetach) m_vm->DetachCurrentThread();
    return result;
}

EvalResult NativePythonBridge::evaluate(
    const std::string& function_name, double x_min, double x_max, int32_t samples)
{
    JNIEnv* env = nullptr;
    bool needsDetach = false;
    jint getEnvResult = m_vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);

    if (getEnvResult == JNI_EDETACHED) {
        m_vm->AttachCurrentThread(&env, nullptr);
        needsDetach = true;
    }

    if (!env || !m_kotlinBridge || !m_evaluate) {
        if (needsDetach) m_vm->DetachCurrentThread();
        return {false, "JNI not initialized", {}, {}};
    }

    jstring jFunc = env->NewStringUTF(function_name.c_str());
    jobject jResult = env->CallObjectMethod(
        m_kotlinBridge, m_evaluate, jFunc, x_min, x_max, samples);

    jclass evalCls = env->GetObjectClass(jResult);
    jfieldID okField = env->GetFieldID(evalCls, "ok", "Z");
    jfieldID errorField = env->GetFieldID(evalCls, "error", "Ljava/lang/String;");
    jfieldID xsField = env->GetFieldID(evalCls, "xs", "Ljava/util/List;");
    jfieldID ysField = env->GetFieldID(evalCls, "ys", "Ljava/util/List;");

    bool ok = env->GetBooleanField(jResult, okField);

    jstring jError = static_cast<jstring>(env->GetObjectField(jResult, errorField));
    const char* errorUtf = env->GetStringUTFChars(jError, nullptr);
    std::string error(errorUtf);
    env->ReleaseStringUTFChars(jError, errorUtf);

    auto readDoubleList = [&](jfieldID field) -> std::vector<double> {
        jobject jList = env->GetObjectField(jResult, field);
        jclass listCls = env->GetObjectClass(jList);
        jmethodID sizeMethod = env->GetMethodID(listCls, "size", "()I");
        jmethodID getMethod = env->GetMethodID(listCls, "get", "(I)Ljava/lang/Object;");
        jint size = env->CallIntMethod(jList, sizeMethod);
        std::vector<double> result;
        result.reserve(size);
        for (jint i = 0; i < size; i++) {
            jobject elem = env->CallObjectMethod(jList, getMethod, i);
            jclass doubleCls = env->GetObjectClass(elem);
            jmethodID doubleValue = env->GetMethodID(doubleCls, "doubleValue", "()D");
            result.push_back(env->CallDoubleMethod(elem, doubleValue));
            env->DeleteLocalRef(elem);
        }
        env->DeleteLocalRef(jList);
        return result;
    };

    std::vector<double> xs = readDoubleList(xsField);
    std::vector<double> ys = readDoubleList(ysField);

    env->DeleteLocalRef(jFunc);
    env->DeleteLocalRef(jResult);

    if (needsDetach) m_vm->DetachCurrentThread();
    return {ok, error, std::move(xs), std::move(ys)};
}

}  // namespace nxs::bridge::jni
