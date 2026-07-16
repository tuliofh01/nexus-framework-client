//! Module root — C ABI exports + JNI bridges for Android.
//!
//! C++20 named modules live under `src/` / `shared/` and are NOT compiled
//! by Zig. This library only exports the allocator C ABI and the Kotlin/JNI
//! entry points for Chaquopy (python_bridge) and Lua stubs (lua_bridge).
//!
//! JNI sources are pulled in via the `jni_python` / `jni_lua` modules added
//! from build.zig (they live next to `src/`, not inside it).
comptime {
    _ = @import("nexus_exports.zig");
    _ = @import("memory.zig");
    _ = @import("jni_python");
    _ = @import("jni_lua");
}
