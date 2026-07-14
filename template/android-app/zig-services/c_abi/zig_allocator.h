// C ABI header for Zig arena allocator — mirrors shared/runtime/ZigAllocator.hpp interface.
// Include from C++ when NXS_ENABLE_ZIG is defined.
#pragma once

#include <cstddef>

#ifdef __cplusplus
extern "C" {
#endif

/// Allocate `size` bytes from the arena arena. Returns null on OOM.
void* nxs_alloc(size_t size);

/// No-op for arena allocations — all memory freed by nxs_reset_arena().
void nxs_free(void* ptr, size_t size);

/// Reset the entire arena arena, releasing all allocated memory.
void nxs_reset_arena(void);

/// Version string for runtime ABI compatibility checks.
const char* nexus_zig_version(void);

#ifdef __cplusplus
}
#endif
