# zig-services — Nexus desktop C-ABI sidecar

Zig builds the **`nexus_zig`** static library (allocator / version C ABI) and a
small smoke test. The full app (C++20 named modules) is built by
**`../build_app.sh`** with `g++ -fmodules-ts` — Zig’s bundled Clang cannot
compile named modules yet.

## Requirements

- **Zig 0.16.0**
- For the full app: `g++` 14+, pkg-config, SDL3, Lua 5.4, Python 3 (+dev)

## Quick build

```bash
# Full app (recommended)
../build_app.sh

# Or: Zig delegates to the same script
zig build app

# C ABI only + smoke test
zig build
zig build smoke
```

## Outputs

| Artifact | Description |
|----------|-------------|
| `libnexus_zig.a` | C ABI: `nxs_alloc`, `nxs_free`, `nexus_zig_version` |
| `smoke_test` | C++ ↔ Zig link check (`examples/smoke_test.cpp`) |
| `../build/bin/{{projectName}}` | Full app (from `build_app.sh`) |

## Layout

```
zig-services/
├── build.zig / build.zig.zon
├── c_abi/zig_allocator.h
├── examples/smoke_test.cpp   # C ABI smoke only (not an app sample)
└── src/
    ├── root.zig
    ├── nexus_exports.zig
    └── memory.zig
```
