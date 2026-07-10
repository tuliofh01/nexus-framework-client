# Desktop App template

Generated from `template/desktop-app/` — a **Desmos-style function plotter** demonstrating the full Nexus desktop stack.

## Stack

| Layer | Technology | Location |
|-------|------------|----------|
| Window / GPU | SDL3 (OpenGL 3.3) | `src/main.cpp` |
| UI | Dear ImGui + ImPlot | `src/view/PlotterView.*` |
| Scripting | Lua 5.4 via sol2 | `scripts/panels.lua`, `src/view/LuaPanels.*` |
| Authoring | TypeScript + XHTML | `ui/ui.xhtml`, `ui/ui.ts` |
| Python | pybind11 embed | `python/functions.py`, `src/controller/PythonEngine.*` |
| Blueprint | imnodes / Langflow JSON | `blueprint.json` |
| Theme | JSON presets | `../shared/themes/` → `assets/themes/` |
| Icons | Nerd Font (optional) | `../shared/assets/fonts/` |

## MVC layout

```
src/
├── model/        FunctionRegistry — catalog + sample caches
├── controller/   PlotController, PythonEngine
└── view/         PlotterView (ImGui/ImPlot), LuaPanels (sol2)
```

Shared runtime (`NexusTheme`, `FontConfig`) lives in `template/shared/runtime/` and is compiled in via CMake.

## Build

```bash
cmake --preset debug
cmake --build --preset debug
./builds/debug/{{projectName}}
```

Prerequisites: CMake ≥ 3.24, Ninja, C++20, Python 3.10+ dev headers, `pip install -r requirements.txt`.

## Assets at startup

In `docs/templates/desktop-app.md` — see [template/shared/assets/fonts/README.md](../../template/shared/assets/fonts/README.md).

## Related

- [Coding with Nexus](../../docs/guides/coding-with-nexus.md)
- [Architecture overview](../../docs/architecture/overview.md)
- [Shared DSL](../shared/dsl/)
