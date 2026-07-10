# {{projectName}} — Nexus Desktop App

Generated from the **desktop-app** template. Flagship sample: Desmos-style function plotter — Python (numpy) samples curves, C++ owns the model, ImGui + ImPlot draw the page.

## Stack

| Layer | Tech | Where |
|-------|------|-------|
| Window / GPU | SDL3 (OpenGL 3.3) | `src/main.cpp` |
| UI & charts | Dear ImGui + ImPlot (+ imnodes) | `src/view/` |
| Scripting | Lua 5.4 via sol2 | `src/view/LuaPanels.*`, `scripts/panels.lua` |
| Authoring | TypeScript + XHTML | `ui/ui.xhtml`, `ui/ui.ts` |
| Math | CPython + numpy via pybind11 | `src/controller/PythonEngine.*`, `python/functions.py` |
| App graph | `blueprint.json` (imnodes editor) | project root |

## Layout (MVC)

```
src/
├── model/        FunctionRegistry — catalog + sampled-data caches
├── controller/   PlotController + PythonEngine (pybind11 embed)
└── view/         PlotterView (ImGui/ImPlot) + LuaPanels (sol2)
```

## Build

Prerequisites: CMake ≥ 3.24, Ninja, C++20, Python 3.10+ with dev headers, `pip install -r requirements.txt`.

```bash
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/{{projectName}}/debug/{{projectName}}
```

Build output goes to `../../builds/framework/{{projectName}}/` (see [builds/README.md](../../builds/README.md)).

Docs: [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md)
