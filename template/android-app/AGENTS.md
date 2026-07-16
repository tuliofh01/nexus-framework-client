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

## Key directories

| Path                                           | Role                                                                            |
|-----------------------------------------------|--------------------------------------------------------------------------------|
| `app/src/main/java/com/nexus/{{packageName}}/` | Kotlin (`MainActivity`, `AppCore`, `PythonBridge`)                              |
| `src/`                                         | C++ MVC (`AppModel`, `AppController`, `AppView`)                                |
| `zig-services/`                                | **Zig JNI bridge** — `build.zig`, `jni/python_bridge.zig`, `jni/lua_bridge.zig` |
| `app/src/main/python/`                         | `helpers.py` (Chaquopy)                                                         |
| `examples/plotter/`                            | Optional Desmos-style sample                                                    |
## Package

Application ID: `com.nexus.{{packageName}}` (derived from project name at generation).

## Docs

- [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md) — C++20, Kotlin, Lua, TS, Python
- [docs/templates/android-app.md](../../docs/templates/android-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
