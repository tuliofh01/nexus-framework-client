//! Android JNI exports for Chaquopy Python bridge.
//! Replaces Djinni-generated `Java_com_nexus_{{packageName}}_AppCore_installPythonBridge`.
//!
//! The actual C++ NativePythonBridge is compiled by zig c++ alongside the app sources.
//! This module provides the Zig JNI entry point that delegates to the C++ bridge.
//!
//! Package: `com.nexus.{{packageName}}`

const jni = @cImport({
    @cInclude("jni.h");
});

/// JNI entry point: `AppCore.installPythonBridge(bridge: PythonBridge)`
/// Called from Kotlin MainActivity.onCreate() to register the Chaquopy-backed bridge
/// before SDL_main runs. Delegates to C++ AppCore::install_python_bridge.
export fn Java_com_nexus_{{packageName}}_AppCore_installPythonBridge(
    env: ?*jni.JNIEnv,
    _: jni.jclass,
    bridge: jni.jobject,
) void {
    c_install_python_bridge(env, bridge);
}

extern fn c_install_python_bridge(env: ?*opaque, bridge: ?*opaque) void;
