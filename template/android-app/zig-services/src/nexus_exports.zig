//! C ABI exports — mirrors template/desktop-app/zig-services/src/nexus_exports.zig.
//! Used by C++ code compiled via zig c++ when NXS_ENABLE_ZIG is defined.

const std = @import("std");

var arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);

/// Allocate `size` bytes from the arena arena. Never frees individually;
/// call nxs_reset_arena() to release all arena memory.
export fn nxs_alloc(size: usize) ?*anyopaque {
    const slice = arena.allocator().alloc(u8, size) catch return null;
    return slice.ptr;
}

/// No-op for arena allocations — all memory freed by nxs_reset_arena().
export fn nxs_free(_: ?*anyopaque, _: usize) void {}

/// Reset the entire arena arena, releasing all allocated memory.
export fn nxs_reset_arena() void {
    arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
}

/// Version string for runtime ABI compatibility checks.
export fn nexus_zig_version() [*:0]const u8 {
    return "0.3.0-android";
}
