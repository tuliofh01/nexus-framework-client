# {{projectName}} — Nexus Desktop App

General-purpose Nexus desktop starter: one window, greeting + counter, with full framework feature support.

## Stack

| Layer | Tech | Where | Language |
|-------|------|-------|----------|
| Window / GPU | [SDL3](https://www.libsdl.org/) ([OpenGL 3.3](https://www.opengl.org/)) | `src/main.cpp` | [C++20](https://en.cppreference.com/w/cpp/20) |
| UI | [Dear ImGui](https://github.com/ocornut/imgui) (+ [ImPlot](https://github.com/epezent/implot) / [imnodes](https://github.com/Nelarius/imnodes)) | `src/view/` | [C++20](https://en.cppreference.com/w/cpp/20) |
| Scripting | [Lua 5.4](https://www.lua.org/) via [sol2](https://sol2.readthedocs.io/) | `src/view/LuaPanels.*`, `scripts/panels.lua` | [Lua](https://www.lua.org/) |
| Authoring | [TypeScript](https://www.typescriptlang.org/) + [XHTML](https://www.w3.org/TR/xhtml1/) | `ui/ui.xhtml`, `ui/ui.ts` | [TypeScript](https://www.typescriptlang.org/) |
| Python | [CPython](https://www.python.org/) via [pybind11](https://pybind11.readthedocs.io/) | `src/controller/PythonEngine.*`, `python/helpers.py` | [Python](https://www.python.org/) |
| Build system | [Zig](https://ziglang.org/) / CMake | `zig-services/` / root | [Zig](https://ziglang.org/) |
| App graph | `blueprint.json` | project root | JSON |
| Flows (optional) | `flows/flows.json` | background/triggered services | JSON |

## Layout (MVC)

```
src/
├── model/        AppModel — counter + greeting
├── controller/   AppController + PythonEngine (pybind11 embed)
└── view/         AppView (ImGui) + LuaPanels (sol2)
```

## Adoption paths

1. **Custom code** — edit `src/`, `ui/`, delete `flows/` or set `"flows": { "enabled": false }`
2. **Blueprint only** — wire nodes in `blueprint.json`, keep minimal shell
3. **Blueprint + flows** — enable `flows/flows.json` for background/triggered automation

## Optional plotter example

Desmos-style function plotter lives under **`examples/plotter/`**. Build with:

```bash
cmake --preset debug -DBUILD_NEXUS_EXAMPLES=ON
cmake --build --preset debug --target {{projectName}}_plotter
```

Delete `examples/plotter/` if you do not need the sample.

## Build

```bash
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/{{projectName}}/debug/{{projectName}}
```

Docs: [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md)
