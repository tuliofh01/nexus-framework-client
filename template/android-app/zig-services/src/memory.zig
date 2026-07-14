//! Arena-backed allocator helpers for Android JNI contexts.
//! Mirrors template/desktop-app/zig-services/src/memory.zig.

const std = @import("std");

threadlocal var tls_arena: std.heap.ArenaAllocator = undefined;
threadlocal var tls_initialized: bool = false;

/// Get or initialize the thread-local arena allocator.
pub fn getThreadLocalArena() *std.heap.ArenaAllocator {
    if (!tls_initialized) {
        tls_arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);
        tls_initialized = true;
    }
    return &tls_arena;
}

/// Reset the thread-local arena — call between JNI frames to release temp allocations.
pub fn resetThreadLocalArena() void {
    if (tls_initialized) {
        _ = tls_arena.reset(.retain_capacity);
    }
}
