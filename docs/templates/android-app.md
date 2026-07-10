# Android App template

Generated from `template/android-app/` — the same **Desmos-style plotter** as desktop, adapted for SDL3 on Android NDK with **Djinni** and **Chaquopy**.

## Stack

| Layer | Technology | Location |
|-------|------------|----------|
| Host activity | Kotlin `SDLActivity` | `app/.../MainActivity.kt` |
| Native UI | SDL3 GLES + ImGui + ImPlot | `src/main.cpp`, `src/view/` |
| C++↔JVM bridge | Djinni | `djinni/plotter.djinni`, `djinni-generated/` |
| Python | Chaquopy (JVM-side) | `app/src/main/python/`, `ChaquopyPythonBridge.kt` |
| Scripting | Lua via sol2 | `scripts/panels.lua` |
| Theme | `nexus-field` default | `../shared/themes/nexus-field.json` |
| Icons | Nerd Font (optional) | `app/src/main/assets/fonts/` |

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `PlotterCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL3 runs `SDL_main` in `src/main.cpp` — same MVC loop as desktop.

## Djinni IDL

Edit `djinni/plotter.djinni`, then regenerate stubs (see file header). Do not hand-edit `djinni-generated/`.

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

- [Coding with Nexus](../../docs/guides/coding-with-nexus.md)
- [Architecture overview](../../docs/architecture/overview.md)
- [Desktop template](desktop-app.md)
