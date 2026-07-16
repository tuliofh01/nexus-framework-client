#ifndef NEXUS_ZIG_ALLOCATOR_H
#define NEXUS_ZIG_ALLOCATOR_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

/** Semantic version of the zig-services sidecar (scaffold). */
const char* nexus_zig_version(void);

/** Arena-backed allocation — opt-in via NXS_ENABLE_ZIG in generated C++. */
void* nxs_alloc(size_t size);
void nxs_free(void* ptr);
void nxs_reset_arena(void);

/** Legacy aliases used by early C++ hook prototypes. */
void* cAlloc(size_t size);
void cFree(void* ptr);

typedef void* (*nxs_alloc_fn)(size_t);
typedef void (*nxs_free_fn)(void*);
typedef void (*nxs_reset_fn)(void);

typedef struct ZigAllocatorVTable {
    nxs_alloc_fn alloc;
    nxs_free_fn free;
    nxs_reset_fn reset;
} ZigAllocatorVTable;

#ifdef __cplusplus
}
#endif

#endif /* NEXUS_ZIG_ALLOCATOR_H */
