# zig-services — Nexus native build sidecar (Phase 1 scaffold)

Optional **Zig** build graph beside CMake for generated desktop apps. CMake remains the default until Phase 3; do not delete `CMakeLists.txt`.

## Requirements

- **Zig 0.14.x** (pinned in `misc/client-setup/env.sh`). Newer Zig may work but is not CI-guaranteed yet.
- Desktop native target (Linux x86_64/aarch64, macOS, Windows) for `hello_cpp` example.

## Formatting

Zig uses the built-in formatter — there is no `.zigfmt` config file:

```bash
zig fmt src/ build.zig jni/
```

Run before every commit that touches `.zig` or `build.zig`. Style rules are documented in [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md#zig-templatedesktop-appzig-services).

## Quick build

```bash
cd template/desktop-app/zig-services
zig build                    # shared lib nexus_zig + hello_cpp
zig build -Dandroid=true     # also compile JNI stub modules (no link yet)
zig build -Dlinkage=static   # static lib instead of shared (default: shared)
```

## Outputs

| Artifact | Description |
|----------|-------------|
| `libnexus_zig.so` / `.dylib` / `.dll` | C ABI: `nxs_alloc`, `nxs_free`, `nexus_zig_version` |
| `hello_cpp` | Tiny C++ executable linked against `nexus_zig` via `zig c++` |
| `zig-out/include/zig_allocator.h` | Header for C++ opt-in (`NXS_ENABLE_ZIG`) |

## CMake coexistence

- **Default:** `cmake --preset debug` in the parent `desktop-app` tree.
- **Zig sidecar:** this directory only; CMake does not invoke Zig in Phase 1.
- Future `nxs_config.json` key: `"build": { "nativeBackend": "cmake" | "zig" }`.

## Install Zig 0.14.x

See [`misc/client-setup/zig/README.md`](../../../misc/client-setup/zig/README.md) or run:

```bash
./misc/client-setup/zig/install-zig.sh
source misc/client-setup/env.sh
zig version   # expect 0.14.x
```

## Layout

```
zig-services/
├── build.zig / build.zig.zon
├── src/           # Zig allocator + C ABI exports
├── jni/           # Android JNI stubs (Phase 4)
├── c_abi/         # zig_allocator.h for C++
└── examples/      # hello_cpp.cpp smoke link test
```

## TODO (post-scaffold)

- [ ] Link real desktop C++ TUs from `../src/` (Phase 1 acceptance)
- [ ] `build.zig.zon` deps for SDL3/imgui (Phase 3)
- [ ] Android NDK sysroot + `libnexus_zig.so` for AGP (Phase 4)
- [ ] `ProjectGenerator` copy when `nativeBackend` is `zig` or `dual`
