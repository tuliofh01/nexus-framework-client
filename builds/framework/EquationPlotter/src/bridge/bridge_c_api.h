//==============================================================================
// bridge_c_api.h — C ABI for Zig ↔ C++ interop
//
// Uses a compile-time toggle NXS_ZIG_LINKED:
//   • DEFINED   → extern symbols (provided by Zig at link time)
//   • UNDEFINED → inline stubs (graceful degradation without Zig)
//
// Function names match zig-services/c_abi/zig_allocator.h and the actual
// Zig exports in zig-services/src/nexus_exports.zig.
//
// == Zig side (zig-services/src/nexus_exports.zig) ==
//   export fn nexus_zig_version() callconv(.C) [*:0]const u8 { ... }
//   export fn nxs_alloc(size: usize) callconv(.C) ?*anyopaque { ... }
//   export fn nxs_free(ptr: ?*anyopaque) callconv(.C) void { ... }
//   export fn nxs_reset_arena() callconv(.C) void { ... }
//==============================================================================

#pragma once

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

#ifdef NXS_ZIG_LINKED

/// Version string from Zig runtime (e.g. "nexus-zig-services/0.1.0-scaffold").
const char* nexus_zig_version(void);

/// Allocate memory through Zig's arena allocator.
void* nxs_alloc(size_t size);

/// Free memory allocated by Zig (no-op for arena; individual frees are
/// batched until the next nxs_reset_arena call).
void  nxs_free(void* ptr);

/// Reset the Zig arena — frees all arena allocations since the last reset.
/// When opt-in enabled, call once per frame boundary.
void  nxs_reset_arena(void);

#else

// Stubs: when Zig is not linked, the program compiles and runs using
// malloc/free directly — no Zig dependency.

#include <stdlib.h>

static inline const char* nexus_zig_version(void) { return nullptr; }
static inline void*       nxs_alloc(size_t size)  { return malloc(size); }
static inline void        nxs_free(void* ptr)     { free(ptr); }
static inline void        nxs_reset_arena(void)   {}

#endif

#ifdef __cplusplus
}
#endif
