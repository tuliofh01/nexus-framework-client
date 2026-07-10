# AGENTS.md — Nexus Desktop App

Guide for AI coding assistants working in **this generated desktop project** (scaffolded from `template/desktop-app/`), not the Nexus Framework scaffolder repo.

## What this project is

A **Desmos-style function plotter** demonstrating the full Nexus desktop stack: SDL3 windowing, Dear ImGui + ImPlot UI, Lua panels via sol2, TypeScript/XHTML authoring, and embedded Python (pybind11 + numpy) for curve sampling. MVC lives under `src/`; `blueprint.json` documents the Langflow-style wiring between layers.

## Key directories

| Path | Role |
|------|------|
| `src/model/` | `FunctionRegistry` — catalog + sampled-data caches |
| `src/controller/` | `PlotController`, `PythonEngine` (pybind11 embed) |
| `src/view/` | `PlotterView` (ImGui/ImPlot), `LuaPanels` (sol2) |
| `ui/` | TypeScript + XHTML DSL (`ui.xhtml`, `ui.ts`) |
| `scripts/` | Lua sources → packed to `misc/lua.dat` |
| `python/` | Python sources → packed to `misc/python.dat` |
| `blueprint.json` | App graph (`python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`) |
| `flows/flows.json` | Optional runtime services (background/triggered) — **optional**; delete or disable to skip |
| `src/service/FlowRunner.*` | Loads flows.json; NO-OP when missing or `"flows": []` |
| `nxs_config.json` | Nexus schema v2 — features, build paths, script protection, optional `flows.enabled` |
| `assets/` | Themes, fonts, logo (copied from shared template) |
| `misc/` | Runtime script archives (`lua.dat`, `python.dat`) |

Shared C++ runtime (`NexusTheme`, `FontConfig`, `ScriptArchive`) is compiled from `../shared/runtime/` via CMake.

## Build / run

**Prerequisites:** CMake ≥ 3.24, Ninja, C++20, Python 3.10+ dev headers, `pip install -r requirements.txt`.

```bash
cmake --preset debug
cmake --build --preset debug
./../../builds/framework/{{projectName}}/debug/{{projectName}}
```

Without presets:

```bash
cmake -B ../../builds/framework/{{projectName}}/debug -G Ninja -DCMAKE_BUILD_TYPE=Debug
cmake --build ../../builds/framework/{{projectName}}/debug
```

Output path comes from `nxs_config.json` → `build.outputDir` (default `../../builds/framework/{{projectName}}/`).

Optional: `-DNXS_PREFER_SYSTEM_DEPS=ON` to prefer system/vcpkg packages over FetchContent for SDL3, ImGui, etc.

## Conventions

- **Window title:** `{{projectName}} - built with The Nexus Framework` (from `nxs_config.json` → `project.windowTitle`).
- **C++ standard:** C++20 (`project.cppStandard` in `nxs_config.json`).
- **Namespaces:** `nxs::model::`, `nxs::controller::`, `nxs::view::` — match `blueprint.json` node `data.class` values.
- **blueprint.json:** Langflow-style graph; node types `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`. Edges use `port` names (`evaluate`, `sampleCache`, `commands`, `activeCurves`). Edit via Nexus client Blueprint Editor or by hand; generator validates on emit.
- **flows/flows.json (optional):** Runtime automation — background intervals and event triggers. Disable by deleting the file or setting `nxs_config.json` → `"flows": { "enabled": false }`. Plotter works unchanged without FlowRunner.
- **nxs_config.json:** Schema v2. Key flags: `features.python.embedding` = `pybind11`, `features.lua.scriptDir` = `scripts`, `scriptProtection.enabled` controls v2 encryption on archives, `flows.enabled` toggles FlowRunner.
- **Script archives:** CMake targets `pack_lua_dat` and `pack_python_dat` build `misc/lua.dat` (LUAC) and `misc/python.dat` (PYAC) via `template/shared/tools/pack_archive.cpp`. Set `"scriptProtection": { "enabled": false }` during development for plaintext archives. Plaintext `scripts/` and `python/` remain as dev fallback when archives are missing.

## Where to edit

| Change | Location |
|--------|----------|
| Curve sampling / numpy math | `python/functions.py` |
| Python↔C++ bridge | `src/controller/PythonEngine.*` |
| Plot commands & orchestration | `src/controller/PlotController.*` |
| Function catalog & caches | `src/model/FunctionRegistry.*` |
| ImGui layout & charts | `src/view/PlotterView.*` |
| Lua panels & hotkeys | `scripts/panels.lua`, `src/view/LuaPanels.*` |
| TS/XHTML UI authoring | `ui/ui.xhtml`, `ui/ui.ts` |
| App wiring graph | `blueprint.json` |
| Runtime flows (optional) | `flows/flows.json`, `src/service/FlowRunner.*` |
| ImGui theme preset | `assets/themes/*.json` or `nxs_config.json` → `theme` |
| Build output / features | `nxs_config.json` |
| CMake deps / link flags | `CMakeLists.txt`, `CMakePresets.json` |

## Python integration (pybind11)

- Sources: `python/functions.py` (blueprint node `py-functions` exports e.g. `evaluate`).
- Engine: `PythonEngine` embeds CPython, loads `misc/python.dat` at runtime (falls back to `python/` tree).
- Flow: `PlotController` calls Python `evaluate` → caches samples in `FunctionRegistry` → `PlotterView` renders via ImPlot.
- Dependencies: `requirements.txt` (numpy). Rebuild after Python changes so `pack_python_dat` refreshes the archive.

## Lua integration (sol2)

- Sources: `scripts/panels.lua` (blueprint node `lua-panels`).
- Loader: `LuaPanels` reads `misc/lua.dat` entry `panels` (falls back to `scripts/panels.lua`).
- Binding: sol2 exposes C++ controller commands; Lua and TS/XHTML both route to `PlotController` via `commands` port.
- Hotkeys: declared in `blueprint.json` → `lua.script` node `data.hotkeys`.

## TypeScript / XHTML DSL

- Page: `ui/ui.xhtml` + `ui/ui.ts#PlotterPage` (blueprint `ui.page` node).
- Shared DSL primitives: `../shared/dsl/` (`core.ts`, `components.ts`, `tags.ts`).
- Patterns: `state()` for two-way bind, `native()` for C++ model projection, `invoke()` for controller commands.

## Do not edit

- **FetchContent vendored trees** under the CMake build dir (SDL3, imgui, sol2, pybind11, etc.) — bump versions in `CMakeLists.txt` instead.
- **`misc/lua.dat` / `misc/python.dat`** by hand — regenerate via CMake pack targets.
- **Generated build output** under `../../builds/framework/{{projectName}}/` except as run artifacts.

## Docs

- [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
- [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md)
- [docs/templates/shared-dsl.md](../../docs/templates/shared-dsl.md)
- [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)
