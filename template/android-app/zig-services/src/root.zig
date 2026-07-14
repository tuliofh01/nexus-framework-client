//! Module root — C ABI exports + JNI bridge stubs for Android.
comptime {
    _ = @import("nexus_exports.zig");  // C ABI: nxs_alloc, nxs_free, nxs_reset_arena, nexus_zig_version
    _ = @import("memory.zig");         // arena-backed allocator
    _ = @import("jni/python_bridge.zig");  // JNI exports for Chaquopy Python bridge
    _ = @import("jni/lua_bridge.zig");     // JNI Lua runtime hooks (Phase 4b)
}
