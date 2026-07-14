//! Android JNI stubs for Lua runtime bridges.
//! Phase 4b: Wire real JNIEnv marshalling for Lua panel hooks.
//!
//! Package: `com.nexus.{{packageName}}`

const jni = @cImport({
    @cInclude("jni.h");
});

/// Stub: `Java_com_nexus_{{packageName}}_LuaRuntime_nativeLoadScript`
/// Will replace LuaPanels JNI path in Phase 4b.
export fn Java_com_nexus_{{packageName}}_LuaRuntime_nativeLoadScript() void {
    // TODO Phase 4b — load Lua script from asset archive
}

/// Stub: `Java_com_nexus_{{packageName}}_LuaRuntime_nativeEvalPanel`
export fn Java_com_nexus_{{packageName}}_LuaRuntime_nativeEvalPanel() void {
    // TODO Phase 4b — evaluate Lua expression and return result
}
