// Smoke test: link a tiny C++ TU against nexus_zig via zig build (zig c++ toolchain).
#include <cstddef>
#include <cstdio>

extern "C" {
const char* nexus_zig_version(void);
void* nxs_alloc(std::size_t size);
void nxs_free(void* ptr);
void nxs_reset_arena(void);
}

int main() {
    const char* ver = nexus_zig_version();
    std::printf("hello_cpp: linked nexus_zig %s\n", ver ? ver : "(null)");

    void* buf = nxs_alloc(64);
    if (buf) {
        std::printf("nxs_alloc ok\n");
        nxs_free(buf);
        nxs_reset_arena();
    }
    return 0;
}
