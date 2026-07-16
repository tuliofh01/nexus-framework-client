# {{projectName}} — Nexus Android App

General-purpose Nexus Android starter with SDL3 + ImGui, Chaquopy Python, and a Zig JNI bridge.

## Why Kotlin and Java appear here

| Code | Language | Required? | Role |
|------|----------|-----------|------|
| `com/nexus/{{packageName}}/*.kt` | **Kotlin** | Yes | Android `Activity`, Chaquopy bootstrap, JNI bridge install |
| `org/libsdl/app/*.java` | **Java** | Yes (SDL3) | Vendored SDL3 Android bindings — not app business logic |
| `src/` | **C++20** | Yes | SDL_main, ImGui MVC, Lua, optional flows |
| `zig-services/` | **Zig** | Yes | C-ABI + JNI sidecar (`python_bridge.zig`) |
| `app/src/main/python/` | **Python** | Optional | Chaquopy modules (JVM-hosted interpreter) |

Android **always** needs a JVM process for the launcher Activity and for Chaquopy.
Native game/UI code runs in C++ under `src/`; Kotlin loads the Zig-built
`lib{{projectName}}.so` for the Chaquopy bridge.

## Stack

| Layer | Tech | Where |
|-------|------|-------|
| Activity | Kotlin + SDL3 | `app/src/main/java/com/nexus/{{packageName}}/` |
| Native UI | C++20 modules | `src/` (AppModel, AppController, AppView) |
| Python | Chaquopy | `app/src/main/python/helpers.py` |
| Bridge | Zig JNI | `zig-services/jni/` + Kotlin types |
| Flows (optional) | `flows/flows.json` | native FlowRunner |

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `AppCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL hands off to `SDL_main` in `src/main.cpp`.

## Build

```bash
./build_app.sh              # venv + Zig .so (needs NDK) + Gradle APK
./build_app.sh --zig-only   # JNI shared libraries only
./build_app.sh --setup-only # venv + tool checks only
./gradlew :app:assembleDebug
```

Zig does **not** compile C++20 named modules — only the JNI/C-ABI sidecar.

Docs: [docs/templates/android-app.md](../../docs/templates/android-app.md) · [zig-services/BUILD.md](zig-services/BUILD.md)
