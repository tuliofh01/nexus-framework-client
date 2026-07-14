//==============================================================================
// ZigAllocator — Opt-In Arena Allocator Bridge (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Provides a C++-friendly wrapper around Zig's ArenaAllocator. An arena
//   allocator is a fast memory pool that allocates from a single block and
//   frees everything at once (no per-object free). This is ideal for
//   short-lived, high-throughput data like plot samples or frame buffers.
//
// WHY ZIG ALLOCATOR:
//   Zig's std.heap.ArenaAllocator is battle-tested, zero-dependency, and
//   extremely fast for "allocate many, free all at once" patterns. By exposing
//   it through a C-ABI bridge, C++ code can use it without linking the entire
//   Zig standard library.
//
// THE C-ABI BRIDGE:
//   This module declares the C++ interface. The actual implementation lives
//   in Zig (zig-services/src/root.zig) and is linked as a .so/.dll. The C++
//   side calls:
//     nxs_zig_alloc(nbytes)  → returns void* (or null on OOM)
//     nxs_zig_free(ptr)      → frees (or resets arena)
//     nxs_zig_reset_arena()  → frees ALL allocations at once
//
// OPT-IN DESIGN:
//   This is NOT a global replacement for new/delete. It's a surgical tool for
//   measured hotspots only. You call nxs_alloc() where profiling shows wins,
//   and nxs_reset_arena() at a safe point (e.g., end of frame).
//
// C++20 MODULE CONCEPTS:
//   This module exports a struct with static methods. The `std::size_t` type
//   comes from <cstddef> — the C compatibility header for size types.
//
//==============================================================================

export module nexus.shared.zig_allocator;

#include <cstddef>

namespace nxs::runtime {

export struct ZigAllocator {
    // Allocate `nbytes` from the Zig arena. Returns null if allocation fails.
    // Fast O(1) bump-pointer allocation — no search, no lock.
    static void* alloc(std::size_t nbytes);

    // Free a single pointer. In arena mode, this may be a no-op (the real
    // cleanup happens at resetArena()). Kept for API symmetry with malloc/free.
    static void free(void* ptr);

    // Reset the entire arena — frees ALL allocations at once. This is the
    // key operation that makes arena allocation fast: one call, total cleanup.
    static void resetArena();
};

} // namespace nxs::runtime
