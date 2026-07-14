# {{projectName}} — Nexus Android App

General-purpose Nexus Android starter with SDL3 + ImGui, Chaquopy Python, and Djinni C++↔JVM bridge.

## Stack

| Layer | Tech | Where | Language |
|-------|------|-------|----------|
| Activity | [Kotlin](https://kotlinlang.org/) + [SDL3](https://www.libsdl.org/) | `app/src/main/java/com/nexus/{{packageName}}/` | [Kotlin](https://kotlinlang.org/) |
| Native | [C++20](https://en.cppreference.com/w/cpp/20) | `src/` (AppModel, AppController, AppView) | [C++](https://isocpp.org/) |
| Python | [Chaquopy](https://chaquo.com/chaquopy/) | `app/src/main/python/helpers.py` | [Python](https://www.python.org/) |
| Bridge | [Djinni](https://github.com/dropbox/djinni) `AppCore` | `djinni/app.djinni` | IDL |
| Scripting | [Lua 5.4](https://www.lua.org/) via [sol2](https://sol2.readthedocs.io/) | `src/view/LuaPanels.*` | [Lua](https://www.lua.org/) |
| Flows (optional) | `flows/flows.json` | native FlowRunner | JSON |

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
