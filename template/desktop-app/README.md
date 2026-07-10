# {{projectName}} — Nexus Desktop App

General-purpose Nexus desktop starter: one window, greeting + counter, with full framework feature support.

## Stack

| Layer | Tech | Where |
|-------|------|-------|
| Window / GPU | SDL3 (OpenGL 3.3) | `src/main.cpp` |
| UI | Dear ImGui (+ ImPlot/imnodes available) | `src/view/` |
| Scripting | Lua 5.4 via sol2 | `src/view/LuaPanels.*`, `scripts/panels.lua` |
| Authoring | TypeScript + XHTML | `ui/ui.xhtml`, `ui/ui.ts` |
| Python | CPython via pybind11 | `src/controller/PythonEngine.*`, `python/helpers.py` |
| App graph | `blueprint.json` | project root |
| Flows (optional) | `flows/flows.json` | background/triggered services |

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
