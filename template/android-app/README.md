# {{projectName}} — Nexus Android App

Generated from the **android-app** template. SDL3 + ImGui plotter with Chaquopy Python and a Djinni C++↔JVM bridge.

## Language layout

| Code | Language | Path | Edit? |
|------|----------|------|-------|
| App logic | Kotlin | `app/src/main/java/com/nexus/plotter/` | Yes |
| Djinni JVM stubs | Kotlin | `djinni-generated/kotlin/` | Regenerate from IDL |
| SDL3 glue | Java | `app/src/main/java/org/libsdl/app/` | **Do not edit** — vendored SDL3 |
| Native core | C++ | `src/`, `djinni-generated/cpp/`, `djinni-generated/jni/` | Yes (C++; JNI generated) |

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `PlotterCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL3 runs `SDL_main` in `src/main.cpp`.

## Build

```bash
./gradlew :app:assembleDebug
```

Requires Android SDK, NDK, JDK 17+.

## Regenerate Djinni stubs

After editing `djinni/plotter.djinni`:

```bash
./scripts/regen-djinni.sh
```

Requires [Djinni](https://djinni.xlcpp.dev/) on `PATH`.

Docs: [docs/templates/android-app.md](../../docs/templates/android-app.md) · [Coding with Nexus](../../docs/guides/coding-with-nexus.md)
