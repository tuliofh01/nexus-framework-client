const std = @import("std");
const memory = @import("memory.zig");

/// Semantic version string for the zig-services sidecar (scaffold).
pub const VERSION: [:0]const u8 = "nexus-zig-services/0.1.0-scaffold";

export fn nexus_zig_version() [*:0]const u8 {
    return VERSION.ptr;
}

/// C ABI allocation stubs — delegate to arena-backed allocator (Phase 5 expands usage).
/// ENHANCE: thread-local arenas when desktop render thread splits from UI thread.
export fn nxs_alloc(size: usize) ?*anyopaque {
    return memory.zigAlloc(size);
}

export fn nxs_free(ptr: ?*anyopaque) void {
    memory.zigFree(ptr);
}

export fn nxs_reset_arena() void {
    memory.resetArena();
}

/// Legacy aliases for early C++ hook naming (cAlloc/cFree).
export fn cAlloc(size: usize) ?*anyopaque {
    return nxs_alloc(size);
}

export fn cFree(ptr: ?*anyopaque) void {
    nxs_free(ptr);
}

test "version string is non-empty" {
    try std.testing.expect(VERSION.len > 0);
}
