//! Module root — pulls in C ABI exports and optional JNI stubs.
comptime {
    _ = @import("nexus_exports.zig");  // C ABI: nxs_alloc, nxs_free, nxs_reset_arena, nexus_zig_version
    _ = @import("memory.zig");         // arena-backed allocator
    if (@import("build_options").android) {
        _ = @import("jni/python_bridge.zig");
        _ = @import("jni/lua_bridge.zig");
    }
}
