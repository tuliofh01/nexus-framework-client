# {{projectName}} — Nexus Android App

General-purpose Nexus Android starter with SDL3 + ImGui, Chaquopy Python, and Djinni C++↔JVM bridge.

## Stack

| Layer | Tech | Where |
|-------|------|-------|
| Activity | Kotlin + SDL3 | `app/src/main/java/com/nexus/{{packageName}}/` |
| Native | C++20 | `src/` (AppModel, AppController, AppView) |
| Python | Chaquopy | `app/src/main/python/helpers.py` |
| Bridge | Djinni `AppCore` | `djinni/app.djinni` |
| Flows (optional) | `flows/flows.json` | native FlowRunner |

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `AppCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL hands off to `SDL_main` in `src/main.cpp`.

## Optional plotter example

See **`examples/plotter/`** for the Desmos-style sample (FunctionRegistry, PlotterView, numpy curves).

## Build

```bash
./gradlew :app:assembleDebug
```

Docs: [docs/templates/android-app.md](../../docs/templates/android-app.md)
