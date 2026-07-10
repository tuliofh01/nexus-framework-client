# Android App template

Nexus Framework Android template — SDL3 + ImGui plotter with Chaquopy Python and a Djinni C++↔JVM bridge.

## Language layout

| Code | Language | Package / path | Edit? |
|------|----------|----------------|-------|
| App logic | **Kotlin** | `app/src/main/java/com/nexus/plotter/` (`MainActivity`, `ChaquopyPythonBridge`, …) | Yes — your app code |
| Djinni JVM stubs | **Kotlin** | `djinni-generated/kotlin/com/nexus/plotter/` (`PlotterCore`, `PythonBridge`, `EvalResult`) | No — regenerate from IDL |
| SDL3 Android glue | **Java** | `app/src/main/java/org/libsdl/app/` | **Do not edit** — vendored upstream SDL3 Android glue |
| Native core | **C++** | `src/`, `djinni-generated/cpp/`, `djinni-generated/jni/` | Yes (C++); JNI is generated |

### Why Java remains for SDL

`org.libsdl.app.*` is copied from the SDL3 release (activity, surface, audio, HID). It is not Nexus application code. Upgrade it only by replacing files from a newer SDL tag — never patch it for plotter features.

### Djinni bridge

The IDL in `djinni/plotter.djinni` defines:

- `eval_result` — record marshalled across JNI as `EvalResult` (Kotlin data class)
- `python_bridge` (`+j`) — implemented in Kotlin via `ChaquopyPythonBridge`
- `plotter_core` (`+c`) — implemented in C++; `PlotterCore.installPythonBridge()` hands the JVM bridge to native code before `SDL_main`

Djinni has no `--kotlin-out` flag. The generator emits **Java** for JVM signature reference and **JNI** for marshalling. Nexus keeps **Kotlin** stubs in `djinni-generated/kotlin/` that match those JNI signatures (`getOk()`, `evaluate(...)`, etc.).

## Regenerate Djinni stubs

After editing `djinni/plotter.djinni`:

```bash
./scripts/regen-djinni.sh
```

This refreshes `djinni-generated/cpp/` and `djinni-generated/jni/`, then prints a diff hint for updating `djinni-generated/kotlin/` if the IDL changed.

Requires [Djinni](https://djinni.xlcpp.dev/) on `PATH`.

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `PlotterCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL3 runs `SDL_main` in `src/main.cpp`.

## Build

```bash
./gradlew :app:assembleDebug
```

Requires Android SDK, NDK, JDK 17+.

## Related

- [Android template docs](../../docs/templates/android-app.md)
- [Coding with Nexus](../../docs/guides/coding-with-nexus.md)
