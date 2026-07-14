#pragma once

#include <cstddef>

/// Opt-in Zig arena allocator hook for generated C++ (default OFF — CMake path unchanged).
/// Enable with `-DNXS_ENABLE_ZIG=ON` and link `nexus_zig` from `zig-services/`.
#ifdef NXS_ENABLE_ZIG

#include "zig_allocator.h"

struct ZigAllocator {
    static void* alloc(std::size_t nbytes);
    static void free(void* ptr);
    static void resetArena();
};

#else

struct ZigAllocator {
    static void* alloc(std::size_t nbytes);
    static void free(void* ptr);
    static void resetArena();
};

#endif
