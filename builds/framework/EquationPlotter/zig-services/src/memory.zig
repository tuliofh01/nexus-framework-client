const std = @import("std");

/// Global arena for transient allocations — reset at frame boundaries when opt-in enabled.
/// C++ callers must treat individual nxs_free as no-op until nxs_reset_arena (see c_abi header).
var g_arena = std.heap.ArenaAllocator.init(std.heap.page_allocator);

/// Mirrors the C-visible layout documented in `c_abi/zig_allocator.h`.
pub const ZigAllocator = extern struct {
    alloc_fn: *const fn (usize) callconv(.c) ?*anyopaque,
    free_fn: *const fn (?*anyopaque) callconv(.c) void,
    reset_fn: *const fn () callconv(.c) void,
};

pub fn zigAlloc(size: usize) ?*anyopaque {
    const slice = g_arena.allocator().alloc(u8, size) catch return null;
    return slice.ptr;
}

pub fn zigFree(ptr: ?*anyopaque) void {
    _ = ptr;
    // Arena frees are batched — individual free is a no-op until reset.
}

pub fn resetArena() void {
    _ = g_arena.reset(.retain_capacity);
}

export fn nxs_zig_allocator_vtable() ZigAllocator {
    const allocFn = struct {
        fn alloc(size: usize) callconv(.c) ?*anyopaque {
            return zigAlloc(size);
        }
    }.alloc;
    const freeFn = struct {
        fn free(ptr: ?*anyopaque) callconv(.c) void {
            zigFree(ptr);
        }
    }.free;
    const resetFn = struct {
        fn reset() callconv(.c) void {
            resetArena();
        }
    }.reset;
    return .{
        .alloc_fn = allocFn,
        .free_fn = freeFn,
        .reset_fn = resetFn,
    };
}

test "arena alloc returns memory" {
    const ptr = zigAlloc(16) orelse return error.TestExpectedEqual;
    try std.testing.expect(ptr != null);
    resetArena();
}
