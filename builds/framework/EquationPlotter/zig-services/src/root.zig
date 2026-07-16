//! Module root — C ABI exports for desktop.
//!
//! Android JNI bridges live under `template/android-app/zig-services/jni/`,
//! not here. Desktop apps call Zig only through the C ABI in this module.
comptime {
    _ = @import("nexus_exports.zig");
    _ = @import("memory.zig");
}
