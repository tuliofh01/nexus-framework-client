# AGENTS.md — Nexus Android Template

Guide for AI coding assistants working in a **generated** Nexus Android project from `template/android-app/`.

## What this template is

A **general-purpose** SDL3 + ImGui starter (hello + counter) with Chaquopy Python, Zig JNI bridge, Lua panels, TS/XHTML DSL, `blueprint.json`, and optional `flows/flows.json`.

## JVM on Android (do not “remove Java” blindly)

- **`org/libsdl/app/*.java`** — vendored SDL3 Android Java layer (required).
- **`com/nexus/{{packageName}}/*.kt`** — thin Kotlin shell: Activity, Chaquopy, JNI bridge install.
- **Native** — Zig JNI sidecar in `zig-services/`; Python runs on the JVM via Chaquopy, not embedded CPython in the `.so`.

## Key directories

| Path | Role |
|------|------|
| `app/src/main/java/com/nexus/{{packageName}}/` | Kotlin (`MainActivity`, `AppCore`, `ChaquopyPythonBridge`) |
| `app/src/main/java/org/libsdl/app/` | SDL3 Java bindings (vendored) |
| `src/` | C++20 modules (`AppModel`, `AppController`, `AppView`) |
| `zig-services/` | Zig C-ABI + JNI (`jni/python_bridge.zig`, `jni/lua_bridge.zig`) |
| `app/src/main/python/` | `helpers.py` (Chaquopy) |

## Package

Application ID: `com.nexus.{{packageName}}` (derived from project name at generation).

## Build

```bash
./build_app.sh              # venv + Zig .so (needs NDK) + Gradle APK
./build_app.sh --zig-only   # JNI shared libraries only
./gradlew :app:assembleDebug
```

Zig does **not** compile C++20 named modules — only the JNI/C-ABI sidecar.

## Docs

- [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md)
- [docs/templates/android-app.md](../../docs/templates/android-app.md)
- [zig-services/BUILD.md](zig-services/BUILD.md)
