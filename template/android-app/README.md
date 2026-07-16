# {{projectName}} — Nexus Android App

General-purpose Nexus Android starter with SDL3 + ImGui, Chaquopy Python, and Zig JNI bridge.

## Stack

| Layer            | Tech                                                                     | Where                                          | Language                          |
|-----------------|--------------------------------------------------------------------------|------------------------------------------------|----------------------------------|
| Activity         | [Kotlin](https://kotlinlang.org/) + [SDL3](https://www.libsdl.org/)      | `app/src/main/java/com/nexus/{{packageName}}/` | [Kotlin](https://kotlinlang.org/) |
| Native           | [C++20](https://en.cppreference.com/w/cpp/20)                            | `src/` (AppModel, AppController, AppView)      | [C++](https://isocpp.org/)        |
| Python           | [Chaquopy](https://chaquo.com/chaquopy/)                                 | `app/src/main/python/helpers.py`               | [Python](https://www.python.org/) |
| Bridge           | **Zig JNI** (hand-authored C++ in `zig-services/`)                       | `zig-services/jni/`                            | C++ / Zig                         |
| Scripting        | [Lua 5.4](https://www.lua.org/) via [sol2](https://sol2.readthedocs.io/) | `src/view/LuaPanels.*`                         | [Lua](https://www.lua.org/)       |
| Flows (optional) | `flows/flows.json`                                                       | native FlowRunner                              | JSON                              |
## Boot sequence

1. `AppCore` loads the native .so via `System.loadLibrary`.
2. `MainActivity.onCreate` calls `AppCore.installPythonBridge()`.
3. SDL hands off to `SDL_main` in `src/main.cpp`.

## Optional plotter example

See **`examples/plotter/`** for the Desmos-style sample (FunctionRegistry, PlotterView, numpy curves).

## Build

```bash
# Compile native .so
cd zig-services
zig build -Dtarget=aarch64-linux-android    # arm64 device
zig build -Dtarget=x86_64-linux-android     # x86_64 emulator

# Full APK
cd ..
./gradlew :app:assembleDebug
```

Docs: [zig-services/BUILD.md](zig-services/BUILD.md)
