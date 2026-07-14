//==============================================================================
// bridge_c_api.h — C ABI for Zig ↔ C++ interop
//
// == DESIGN ==
// This header uses a compile-time toggle NXS_ZIG_LINKED:
//
//   • When DEFINED  (set by build.zig when Zig runtime is linked):
//     Declares extern symbols — Zig provides the implementation at link time.
//
//   • When UNDEFINED (C++ compilation without Zig):
//     Provides inline stub implementations (nullptr / null / no-op)
//     so the program compiles and runs gracefully without Zig.
//
// == ZIG SIDE (zig-services/src/root.zig) ==
//
//   export fn nxs_zig_version() callconv(.C) [*:0]const u8 {
//       return @import("builtin").zig_version_string;
//   }
//   export fn nxs_zig_alloc(size: usize) callconv(.C) ?*anyopaque {
//       const ptr = @import("std").heap.c_allocator.alloc(u8, size) catch return null;
//       return @ptrCast(ptr.ptr);
//   }
//   export fn nxs_zig_free(ptr: ?*anyopaque) callconv(.C) void {
//       if (ptr) |p| {
//           const slice: []u8 = @as([*]u8, @ptrCast(p))[0..0];
//           @import("std").heap.c_allocator.free(slice);
//       }
//   }
//
// == C++ SIDE ==
//   #include "bridge_c_api.h"
//   #ifdef NXS_ZIG_LINKED
//       printf("Zig linked: %s\n", nxs_zig_version());
//   #else
//       printf("Zig not linked\n");
//   #endif
//==============================================================================

#pragma once

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

// ── When NXS_ZIG_LINKED is set at compile time (by build.zig or -D flag) ──

#ifdef NXS_ZIG_LINKED

/// Version string from Zig runtime (e.g. "0.14.0").
const char* nxs_zig_version(void);

/// Allocate memory through Zig's arena allocator.
void* nxs_zig_alloc(size_t size);

/// Free memory allocated by Zig.
void  nxs_zig_free(void* ptr);

// ── Stubs for when Zig is NOT linked (graceful degradation) ──

#else

#include <stdlib.h>

static inline const char* nxs_zig_version(void) { return nullptr; }
static inline void*       nxs_zig_alloc(size_t size) { return malloc(size); }
static inline void        nxs_zig_free(void* ptr) { free(ptr); }

#endif

#ifdef __cplusplus
}
#endif
