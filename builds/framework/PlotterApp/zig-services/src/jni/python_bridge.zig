//! Android JNI stubs for Chaquopy Python bridge — Phase 4 parity with Djinni `plotter.djinni`.
//! Package placeholder: `com.nexus.plotterapp` → replace `plotterapp` at generate time.
//!
//! TODO: wire real JNIEnv marshalling and link against NDK sysroot when `-Dandroid=true`.

/// Stub: `Java_com_nexus_MyApp_ChaquopyPythonBridge_nativeInit`
export fn Java_com_nexus_MyApp_ChaquopyPythonBridge_nativeInit() void {
    // TODO Phase 4 — install Python bridge hooks from native side
}

/// Stub: `Java_com_nexus_MyApp_PlotterCore_installPythonBridge`
export fn Java_com_nexus_MyApp_PlotterCore_installPythonBridge() void {
    // TODO Phase 4 — mirror Djinni `install_python_bridge`
}

/// Stub: `Java_com_nexus_MyApp_PlotterCore_evaluate`
export fn Java_com_nexus_MyApp_PlotterCore_evaluate() void {
    // TODO Phase 4 — return jdoubleArray from C++ PlotterCore
}
