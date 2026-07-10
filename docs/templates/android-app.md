# Android App template

Generated from `template/android-app/` — the same **Desmos-style plotter** as desktop, adapted for SDL3 on Android NDK with **Djinni** and **Chaquopy**.

## Stack

| Layer | Technology | Location |
|-------|------------|----------|
| Host activity | Kotlin `SDLActivity` subclass | `app/.../MainActivity.kt` |
| Native UI | SDL3 GLES + ImGui + ImPlot | `src/main.cpp`, `src/view/` |
| C++↔JVM bridge | Djinni | `djinni/plotter.djinni`, `djinni-generated/` |
| Python | Chaquopy (JVM-side) | `app/src/main/python/`, `ChaquopyPythonBridge.kt` |
| Scripting | Lua via sol2 | `scripts/panels.lua` |
| Theme | `nexus-field` default | `../shared/themes/nexus-field.json` |
| Icons | Nerd Font (optional) | `app/src/main/assets/fonts/` |

## Kotlin vs Java

| Role | Language | Path |
|------|----------|------|
| Nexus app code | **Kotlin** | `app/src/main/java/com/nexus/plotter/` |
| Djinni JVM stubs | **Kotlin** | `djinni-generated/kotlin/com/nexus/plotter/` |
| SDL3 Android glue | **Java** (vendored) | `app/src/main/java/org/libsdl/app/` — **do not edit** |

`org.libsdl.app` is upstream SDL3 Android glue copied from the SDL release. Treat it as a third-party dependency: replace from a newer SDL tag, never customize for app features.

## Djinni bridge

Edit `djinni/plotter.djinni`, then regenerate:

```bash
cd template/android-app
./scripts/regen-djinni.sh
```

Djinni generates C++ and JNI directly. It has no `--kotlin-out`; Kotlin stubs in `djinni-generated/kotlin/` mirror the JVM signatures JNI expects. After IDL changes, update those Kotlin files to match the staged Java diff the script prints.

Do not hand-edit `djinni-generated/cpp/` or `djinni-generated/jni/`.

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `PlotterCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL3 runs `SDL_main` in `src/main.cpp` — same MVC loop as desktop.

### Python (Android)

Android uses **Chaquopy** — Python sources live in `app/src/main/python/` and are bundled by Gradle. There is **no** `python.dat` on Android (unlike desktop pybind11 embed). Lua still packs to `assets/lua.dat` via Gradle `packLuaDat`.

## Build

Open in Android Studio or:

```bash
./gradlew :app:assembleDebug
```

Requires Android SDK, NDK, JDK 17+.

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
