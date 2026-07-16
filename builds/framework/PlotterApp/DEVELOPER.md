# PlotterApp — Developer Guide

This document explains how to build, extend, and ship the generated native desktop app.

## Quick start

```bash
# Build everything (debug)
cd PlotterApp
zig build

# Run
./zig-out/bin/PlotterApp

# Build + run in one step
zig build run

# Release build
zig build -Doptimize=ReleaseSafe
```

## Project layout

```
PlotterApp/
├── build.zig              # Zig build definition
├── build.zig.zon          # Zig package dependencies
├── nxs_config.json        # Nexus project configuration
├── blueprint.json         # App structure graph (MVC wiring)
│
├── src/                   # C++ application code
│   ├── main.cpp           # Entry point (SDL3 + ImGui bootstrap)
│   ├── model/             # Model layer — durable state
│   │   └── AppModel.cpp/hpp
│   ├── controller/        # Controller layer — commands + logic
│   │   ├── AppController.cpp/hpp
│   │   └── PythonEngine.cpp/hpp
│   ├── view/              # View layer — ImGui UI
│   │   ├── AppView.cpp/hpp
│   │   └── LuaPanels.cpp/hpp
│   └── service/           # Optional runtime automations
│       └── FlowRunner.cpp/hpp
│
├── shared/                # Cross-target runtime helpers
│   └── runtime/
│       ├── NexusTheme.cpp/hpp    # ImGui theme loader
│       ├── FontConfig.cpp/hpp     # Nerd Font + font config
│       ├── ScriptArchive.cpp/hpp  # Lua/Python archive loader
│       ├── ScriptCrypto.cpp/hpp   # Archive crypto
│       └── Paths.hpp              # Runtime path resolution
│
├── scripts/               # Lua runtime scripts
│   └── panels.lua
├── python/                # Python functions
│   └── functions.py
├── ui/                    # TS/XHTML DSL pages
│   ├── ui.xhtml
│   └── ui.ts
│
├── flows/                 # Optional in-app automations
│   └── flows.json
├── assets/                # Themes, fonts, logos
│   └── themes/
│       └── nexus-dark.json
└── zig-out/               # Build output
```

## Adding C++ code

1. Create your source files in the appropriate `src/` subdirectory
2. Add the source path to `build.zig`'s `exe.addCSourceFile` call
3. Rebuild: `zig build`

### Conventions

- **C++20** standard — use `std::span`, `std::string_view`, `[[nodiscard]]`, `auto` return types
- **RAII** — no manual `new`/`delete`; use `std::unique_ptr`, `std::vector`, `std::string`
- **Headers** live next to implementations (`src/model/AppModel.hpp` alongside `src/model/AppModel.cpp`)
- **Namespaces** — `nxs::model`, `nxs::controller`, `nxs::view`, `nxs::service`, `nxs::runtime`

## Adding dependencies

Dependencies are managed in `build.zig.zon`. The build system currently uses:

| Library                                                                                              | How it's provided                                | Language                             |
|-----------------------------------------------------------------------------------------------------|--------------------------------------------------|-------------------------------------|
| **[SDL3](https://www.libsdl.org/)**                                                                  | System package or vendored                       | [C](https://en.cppreference.com/w/c) |
| **[Dear ImGui](https://github.com/ocornut/imgui)**                                                   | Vendored sources in `zig build`                  | [C++](https://isocpp.org/)           |
| **[ImPlot](https://github.com/epezent/implot)** / **[ImNodes](https://github.com/Nelarius/imnodes)** | Vendored sources                                 | [C++](https://isocpp.org/)           |
| **[sol2](https://sol2.readthedocs.io/)** + **[Lua 5.4](https://www.lua.org/)**                       | System package or vendored                       | [Lua](https://www.lua.org/)          |
| **[pybind11](https://pybind11.readthedocs.io/)** + **[Python3](https://www.python.org/)**            | System package (`pkg-config` / `python3-config`) | [Python](https://www.python.org/)    |
| **[Zig](https://ziglang.org/)** build system                                                         | Pinned 0.14.x via `setup.zig`                    | [Zig](https://ziglang.org/)          |
To add a new dependency:

```bash
# Add a Zig package
zig fetch --save https://github.com/user/lib

# Or for C/C++ libs, add a fetch step in build.zig
```

For system libraries, the Zig build script runs `pkg-config`; ensure the library is installed:

```bash
# Debian/Ubuntu
sudo apt install libsdl3-dev python3-dev

# macOS
brew install sdl3 python3

# Windows (vcpkg)
vcpkg install sdl3
```

## Building for other platforms

```bash
# Cross-compile for Windows (from Linux/macOS)
zig build -Dtarget=x86_64-windows

# Cross-compile for macOS (from Linux)
zig build -Dtarget=aarch64-macos

# See all targets
zig targets
```

## Working with Lua

Edit `scripts/panels.lua` to add ImGui panels and hotkeys:

```lua
nxs.register_panel("My Panel", function()
    if ui.button("Click me") then
        nxs.log("Button was clicked")
    end
    ui.separator()
    ui.text("Hello from Lua!")
end)

nxs.register_hotkey(keys.F1, function()
    nxs.log("F1 pressed")
end)
```

Lua scripts are packed into `misc/lua.dat` at build time. During development, the app also checks for plaintext `scripts/panels.lua` next to the binary for faster iteration.

## Working with Python

Edit `python/functions.py` to add Python functions callable from C++:

```python
def greeting(project_name: str) -> str:
    return f"Hello from Python, {project_name}!"

def evaluate(func: str, x_min: float, x_max: float, samples: int):
    import numpy as np
    xs = np.linspace(x_min, x_max, samples)
    ys = eval(func)(xs) if isinstance(eval(func), type(lambda: 0)) else xs * 0
    return xs, ys
```

Python sources are packed into `misc/python.dat` at build time, with a plaintext `python/` fallback during development.

## Blueprint & Flows

- **`blueprint.json`** — defines the app's MVC structure (model, controller, view, Lua, Python nodes). Edit in any JSON editor or through the Nexus Framework client.
- **`flows/flows.json`** — optional in-app automations (timers, events, background loops). Loaded by `FlowRunner` at startup.

## Updating the theme

Theme colors are in `assets/themes/nexus-dark.json`. Edit and rebuild to see changes:

```json
{
    "imgui": {
        "colors": {
            "Text": [0.9, 0.9, 0.9, 1.0],
            "Button": [0.2, 0.5, 0.9, 1.0]
        }
    }
}
```

## Debugging

```bash
# Build with debug symbols
zig build

# Run with ASan (compile with -fsanitize=address)
zig build -Doptimize=Debug

# Native debugging
zig build
lldb ./zig-out/bin/PlotterApp
```

## Where to go next

| Task                    | File                            |
|------------------------|--------------------------------|
| Add a new C++ module    | `src/` → update `build.zig`     |
| Change the UI layout    | `src/view/AppView.cpp`          |
| Add Lua panels          | `scripts/panels.lua`            |
| Add Python analytics    | `python/functions.py`           |
| Edit the app graph      | `blueprint.json`                |
| Add runtime automations | `flows/flows.json`              |
| Customize theme colors  | `assets/themes/nexus-dark.json` |
| Edit the XHTML/TS UI    | `ui/ui.xhtml`, `ui/ui.ts`       |