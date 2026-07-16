# Android App template

Generated from `template/android-app/` — the same **Desmos-style plotter** as desktop, adapted for SDL3 on Android with **Zig JNI** and **Chaquopy**.

## Stack

| Layer          | Technology                                         | Location                                  |
|---------------|----------------------------------------------------|------------------------------------------|
| Host activity  | Kotlin `SDLActivity` subclass                      | `app/.../MainActivity.kt`                 |
| Native UI      | SDL3 GLES + ImGui + ImPlot                         | `src/main.cpp`, `src/view/`               |
| C++↔JVM bridge | **Zig JNI** (hand-authored C++ in `zig-services/`) | `zig-services/jni/`                       |
| Python         | Chaquopy (JVM-side)                                | `app/src/main/python/`, `PythonBridge.kt` |
| Scripting      | Lua via sol2                                       | `scripts/panels.lua`                      |
| Theme          | `nexus-field` default                              | `../shared/themes/nexus-field.json`       |
| Icons          | Nerd Font (optional)                               | `app/src/main/assets/fonts/`              |
## Kotlin vs Java

| Role              | Language            | Path                                                  |
|------------------|---------------------|------------------------------------------------------|
| Nexus app code    | **Kotlin**          | `app/src/main/java/com/nexus/{{packageName}}/`        |
| SDL3 Android glue | **Java** (vendored) | `app/src/main/java/org/libsdl/app/` — **do not edit** |
`org.libsdl.app` is upstream SDL3 Android glue copied from the SDL release. Treat it as a third-party dependency: replace from a newer SDL tag, never customize for app features.

## JNI bridge

The native ↔ Kotlin bridge lives in `zig-services/jni/` as hand-authored C++ files:

| File                         | Role                                                            |
|-----------------------------|----------------------------------------------------------------|
| `jni/jni_bridge.cpp`         | Zig JNI `export fn` entry points (called from Kotlin `AppCore`) |
| `jni/NativePythonBridge.cpp` | C++ bridge object that calls back into Kotlin `PythonBridge`    |
| `jni/app_core.cpp`           | `AppCore` native methods wrapping the bridge                    |
No IDL codegen step, no regen script, no generated stubs. Edit the C++ and Kotlin files in lockstep when adding new JNI methods.

## Boot sequence

1. `AppCore` loads the native `.so` via `System.loadLibrary`.
2. `MainActivity.onCreate` calls `AppCore.installPythonBridge(PythonBridge)`.
3. SDL3 runs `SDL_main` in `src/main.cpp` — same MVC loop as desktop.

### Python (Android)

Android uses **Chaquopy** — Python sources live in `app/src/main/python/` and are bundled by Gradle. There is **no** `python.dat` on Android (unlike desktop pybind11 embed). Lua packs to `assets/lua.dat` via Gradle `packLuaDat`.

## Build

```bash
# 1. Compile native .so
cd zig-services
zig build -Dtarget=aarch64-linux-android

# 2. Full APK
cd ..
./gradlew :app:assembleDebug
```

Requires Android SDK, NDK (r26+), Zig 0.14.x (pinned in `misc/client-setup/`), JDK 17+.

## Field tablet theme

Android `main.cpp` defaults to **nexus-field** — high contrast, larger touch padding. Override via `nxs_config.json`:

```json
{ "theme": "nexus-dark" }
```

## Related

- [Template README](../template/android-app/README.md)
- [Coding with Nexus](../guides/coding-with-nexus.md)
- [Architecture overview](../architecture/overview.md)
- [Desktop template](desktop-app.md)
