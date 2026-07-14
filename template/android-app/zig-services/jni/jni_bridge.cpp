// JNI bridge helper — called from Zig export fn Java_com_nexus_{{packageName}}_AppCore_*
// Compiles via zig c++ alongside app sources. Kept minimal; replaces Djinni-generated glue.
// Include paths resolve via build.zig: djinni_root/cpp, djinni_root/jni.
#include "app_core.hpp"
#include "NativePythonBridge.hpp"

#include <memory>

extern "C" {

/// Called from Zig python_bridge.zig — delegates to C++ AppCore.
void c_install_python_bridge(JNIEnv* env, jobject bridge) {
    nxs::bridge::AppCore::install_python_bridge(
        std::make_shared<nxs::bridge::jni::NativePythonBridge>(env, bridge));
}

}  // extern "C"
