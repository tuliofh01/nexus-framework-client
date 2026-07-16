# zig-services — Nexus native build system

The **default** build backend for generated desktop apps. Replaces CMake as the primary build system.

## Requirements

- **Zig 0.14.x** (pinned in `misc/client-setup/env.sh`)
- System dependencies (installed via package manager):
  - [SDL3](https://www.libsdl.org/) development headers
  - [Python 3.10+](https://www.python.org/) development headers
  - [Lua 5.4](https://www.lua.org/) development headers
  - OpenGL development headers

## Quick build

```bash
cd template/desktop-app/zig-services
zig build                    # builds PlotterApp executable + nexus_zig library
zig build run                # builds and runs PlotterApp
zig build -Doptimize=ReleaseSafe  # release build
```

## Outputs

| Artifact                              | Description                                         |
|--------------------------------------|----------------------------------------------------|
| `zig-out/bin/PlotterApp`         | Main application executable                         |
| `zig-out/bin/pack_archive`            | Lua/Python archive packer tool                      |
| `zig-out/bin/smoke_test`              | C++/Zig C ABI smoke test (NOT the app entry point)  |
| `libnexus_zig.so` / `.dylib` / `.dll` | C ABI: `nxs_alloc`, `nxs_free`, `nexus_zig_version` |
| `zig-out/include/zig_allocator.h`     | Header for C++ opt-in (`NXS_ENABLE_ZIG`)            |
## What it builds

All C++ sources under `../src/` and `../shared/runtime/` are compiled with `zig c++` (Clang-based toolchain). External dependencies are:

| Library                                                                                      | Source                                      | Language                             |
|---------------------------------------------------------------------------------------------|---------------------------------------------|-------------------------------------|
| [SDL3](https://www.libsdl.org/)                                                              | System package (`pkg-config`)               | [C](https://en.cppreference.com/w/c) |
| [Dear ImGui](https://github.com/ocornut/imgui)                                               | `build.zig.zon` (git tarball)               | [C++](https://isocpp.org/)           |
| [ImPlot](https://github.com/epezent/implot) / [ImNodes](https://github.com/Nelarius/imnodes) | Vendored under imgui `misc/`                | [C++](https://isocpp.org/)           |
| [sol2](https://sol2.readthedocs.io/)                                                         | `build.zig.zon` (git tarball) — header-only | [C++](https://isocpp.org/)           |
| [Lua 5.4](https://www.lua.org/)                                                              | System package (`pkg-config`)               | [C](https://en.cppreference.com/w/c) |
| [pybind11](https://pybind11.readthedocs.io/)                                                 | `build.zig.zon` (git tarball) — header-only | [C++](https://isocpp.org/)           |
| [Python 3](https://www.python.org/)                                                          | System package (`pkg-config`)               | [C](https://en.cppreference.com/w/c) |
## Cross-compilation

```bash
# Cross-compile for Windows (from Linux/macOS)
zig build -Dtarget=x86_64-windows

# Cross-compile for macOS (from Linux)
zig build -Dtarget=aarch64-macos

# See all targets
zig targets
```

## Adding new C++ sources

1. Add the `.cpp` path to the `app_sources` or `shared_sources` array in `build.zig`
2. Rebuild: `zig build`

## Layout

```
zig-services/
├── build.zig / build.zig.zon   # Zig build definition + dependency manifest
├── src/                         # Zig allocator + C ABI exports
├── jni/                         # Android JNI stubs (Phase 4)
├── c_abi/                       # zig_allocator.h for C++
└── examples/                    # smoke_test.cpp C ABI link verification
```

## Adding new dependencies

```bash
# Add a Zig package
zig fetch --save https://github.com/user/lib

# For C/C++ system libraries, install via your package manager
sudo apt install libfoo-dev
```

## Cross-target

Desktop and Android builds both use Zig exclusively.
