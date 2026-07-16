//==============================================================================
// nexus.shared.zig_allocator — Opt-In Arena Allocator Bridge (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Provides a C++-friendly wrapper around Zig's ArenaAllocator. An arena
// allocator is a fast memory pool that allocates from a single block and
// frees everything at once (no per-object free). This is ideal for
// short-lived, high-throughput data like plot samples or frame buffers.
//
// ════════════════════════════════════════════════════════════════════════════
// WHY ZIG ALLOCATOR
// ════════════════════════════════════════════════════════════════════════════
//
// Zig's std.heap.ArenaAllocator is battle-tested, zero-dependency, and
// extremely fast for "allocate many, free all at once" patterns. By exposing
// it through a C-ABI bridge, C++ code can use it without linking the entire
// Zig standard library.
//
// ════════════════════════════════════════════════════════════════════════════
// THE C-ABI BRIDGE
// ════════════════════════════════════════════════════════════════════════════
//
// This module declares the C++ interface. The actual implementation lives
// in Zig (zig-services/src/root.zig) and is linked as a .so/.dll. The C++
// side calls:
//
//     nxs_alloc(nbytes)     → returns void* (or null on OOM)
//     nxs_free(ptr)         → frees (or no-op in arena mode)
//     nxs_reset_arena()     → frees ALL allocations at once
//
// The C ABI header (c_abi/zig_allocator.h) declares these with extern "C".
//
// ════════════════════════════════════════════════════════════════════════════
// OPT-IN DESIGN
// ════════════════════════════════════════════════════════════════════════════
//
// This is NOT a global replacement for new/delete. It's a surgical tool for
// measured hotspots only. You call alloc() where profiling shows wins,
// and resetArena() at a safe point (e.g., end of frame).
//
// When NXS_ENABLE_ZIG is not defined, all methods are no-ops — the app
// continues to use standard malloc/free without any code change.
//
// ════════════════════════════════════════════════════════════════════════════
// C++20 MODULE CONCEPTS
// ════════════════════════════════════════════════════════════════════════════
//
// This file is a self-contained module interface unit (.cppm) with NO
// separate implementation file. The C ABI functions are declared in the
// global module fragment (private to this TU) and the wrapper methods are
// defined inline in the struct.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

#include <cstddef>  // std::size_t

// ── C ABI functions from zig-services (private to this module) ──
// These are declared here rather than #include "zig_allocator.h" to keep
// the module self-contained. The actual implementations live in Zig.
extern "C" {
    [[nodiscard]] void* nxs_alloc(std::size_t size);
    void nxs_free(void* ptr) noexcept;
    void nxs_reset_arena() noexcept;
}

export module nexus.shared.zig_allocator;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime — Zig Arena Allocator Wrapper
// ═══════════════════════════════════════════════════════════════════════════

namespace nxs::runtime {

export struct ZigAllocator {
    /// Allocate `nbytes` from the Zig arena. Returns null if allocation fails.
    /// Fast O(1) bump-pointer allocation — no search, no lock.
    [[nodiscard]] static void* alloc(std::size_t nbytes) {
        return nxs_alloc(nbytes);
    }

    /// Free a single pointer. In arena mode, this may be a no-op (the real
    /// cleanup happens at resetArena()). Kept for API symmetry with malloc/free.
    static void free(void* ptr) noexcept {
        nxs_free(ptr);
    }

    /// Reset the entire arena — frees ALL allocations at once. This is the
    /// key operation that makes arena allocation fast: one call, total cleanup.
    static void resetArena() noexcept {
        nxs_reset_arena();
    }
};

}  // namespace nxs::runtime
