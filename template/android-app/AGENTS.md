# AGENTS.md — Nexus Android Template

Guide for AI coding assistants working in a **generated** Nexus Android project from `template/android-app/`.

## What this template is

A **general-purpose** SDL3 + ImGui starter (hello + counter) with Chaquopy Python, **Zig JNI** bridge, Lua panels, TS/XHTML DSL, `blueprint.json`, and optional `flows/flows.json`.

Optional **plotter sample** under `examples/plotter/`.

## Build

**Default — Zig (recommended):**

```bash
cd zig-services
zig build -Dtarget=aarch64-linux-android    # device
zig build -Dtarget=x86_64-linux-android     # emulator
```

Output `.so` lands in `zig-out/lib/`.

**Legacy — CMake (fallback):**

```bash
cmake --preset android-arm64
cmake --build --preset android-arm64
```

## Key directories

| Path | Role |
|------|------|
| `app/src/main/java/com/nexus/{{packageName}}/` | Kotlin (`MainActivity`, `ChaquopyPythonBridge`) |
| `src/` | C++ MVC (`AppModel`, `AppController`, `AppView`) |
| `zig-services/` | **Zig JNI bridge** — `build.zig`, `jni/python_bridge.zig`, `jni/lua_bridge.zig` |
| `djinni-generated/` | **Deprecated** Djinni stubs (Phase 4 legacy) |
| `app/src/main/python/` | `helpers.py` (Chaquopy) |
| `examples/plotter/` | Optional Desmos-style sample |

## Package

Application ID: `com.nexus.{{packageName}}` (derived from project name at generation).

## JNI bridge

The native JNI entry point (`AppCore.installPythonBridge`) is an `export fn` in Zig:
- `zig-services/jni/python_bridge.zig` — JNI export → delegates to C++
- `zig-services/jni/lua_bridge.zig` — Lua runtime hooks (Phase 4b)

The Djinni path is **deprecated**. New JNI bridges should be authored as Zig exports.

## Docs

- [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md) — C++20, Kotlin, Lua, TS, Python
- [docs/templates/android-app.md](../../docs/templates/android-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
- [docs/guides/legacy-djinni.md](../../docs/guides/legacy-djinni.md) — archived Djinni instructions
