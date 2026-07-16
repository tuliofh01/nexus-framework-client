# {{projectName}} — Nexus Desktop App

Nexus desktop starter with a safe equation plotter and full framework feature
support. Enter expressions such as `y=sin(x)` or `y=x^3-2*x`, choose the
X range and sample count, and render the result through ImPlot.

## Stack

| Layer | Tech | Where | Language |
|-------|------|-------|----------|
| Window / GPU | [SDL3](https://www.libsdl.org/) ([OpenGL 3.3](https://www.opengl.org/)) | `src/main.cpp` | [C++20](https://en.cppreference.com/w/cpp/20) |
| UI | [Dear ImGui](https://github.com/ocornut/imgui) (+ [ImPlot](https://github.com/epezent/implot) / [imnodes](https://github.com/Nelarius/imnodes)) | `src/view/` | [C++20](https://en.cppreference.com/w/cpp/20) |
| Scripting | [Lua 5.4](https://www.lua.org/) via [sol2](https://sol2.readthedocs.io/) | `src/view/LuaPanels.*`, `scripts/panels.lua` | [Lua](https://www.lua.org/) |
| Authoring | [TypeScript](https://www.typescriptlang.org/) + [XHTML](https://www.w3.org/TR/xhtml1/) | `ui/ui.xhtml`, `ui/ui.ts` | [TypeScript](https://www.typescriptlang.org/) |
| Python | [CPython](https://www.python.org/) via [pybind11](https://pybind11.readthedocs.io/) | `src/controller/PythonEngine.*`, `python/functions.py` | [Python](https://www.python.org/) |
| Build orchestration | `build_app.sh` + GCC + [Zig 0.16.0](https://ziglang.org/) | project root / `zig-services/` | Shell, C++20, Zig |
| App graph | `blueprint.json` | project root | JSON |
| Flows (optional) | `flows/flows.json` | background/triggered services | JSON |

## Layout (MVC)

```
src/
├── model/        AppModel + FunctionRegistry
├── controller/   AppController + PlotController + PythonEngine
└── view/         AppView (ImGui/ImPlot) + LuaPanels (sol2)
```

## Adoption paths

1. **Custom code** — edit `src/`, `ui/`, delete `flows/` or set `"flows": { "enabled": false }`
2. **Blueprint only** — wire nodes in `blueprint.json`, keep minimal shell
3. **Blueprint + flows** — enable `flows/flows.json` for background/triggered automation

## Build

**One-shot script (recommended)** — creates a Python venv, installs
`requirements.txt`, fetches C++ deps, builds the Zig C-ABI library, and
compiles all C++20 modules with `g++ -fmodules-ts`:

```bash
./build_app.sh              # setup + build → build/bin/{{projectName}}
./build_app.sh --setup-only # venv + dependency fetch only
source .venv/bin/activate && ./build/bin/{{projectName}}
```

Equivalent via Zig (delegates to the same script):

```bash
cd zig-services && zig build app
```

Requires: Zig 0.16.0, g++ 14+, pkg-config, SDL3, Lua 5.4, Python 3 (+dev).

The equation evaluator interprets a restricted Python AST; it does not call
`eval`. Run its focused tests with:

```bash
python -m unittest python/test_functions.py
```

Docs: [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md)
