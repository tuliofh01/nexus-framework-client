#include "ZigAllocator.hpp"

#ifdef NXS_ENABLE_ZIG

void* ZigAllocator::alloc(std::size_t nbytes) {
    return nxs_alloc(nbytes);
}

void ZigAllocator::free(void* ptr) {
    nxs_free(ptr);
}

void ZigAllocator::resetArena() {
    nxs_reset_arena();
}

#else

void* ZigAllocator::alloc(std::size_t nbytes) {
    (void)nbytes;
    return nullptr; // TODO: std::malloc fallback or disable call sites when NXS_ENABLE_ZIG is off
}

void ZigAllocator::free(void* ptr) {
    (void)ptr;
}

void ZigAllocator::resetArena() {}

#endif
