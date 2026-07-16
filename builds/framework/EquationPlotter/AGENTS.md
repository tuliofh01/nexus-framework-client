# AGENTS.md — Nexus Desktop Template

Guide for AI coding assistants working in a **generated** Nexus desktop project from `template/desktop-app/`.

## What this template is

A **general-purpose** SDL3 + Dear ImGui starter (hello + counter) with full Nexus feature support: Lua panels, TS/XHTML DSL, pybind11 Python, `blueprint.json`, and optional `flows/flows.json`. MVC lives under `src/`.

## Key directories

| Path | Role |
|------|------|
| `src/model/` | `AppModel` — counter + greeting |
| `src/controller/` | `AppController`, `PythonEngine` (pybind11) |
| `src/view/` | `AppView` (ImGui), `LuaPanels` (sol2) |
| `src/service/` | `FlowRunner` — optional flows runtime |
| `ui/` | `ui.xhtml` + `ui.ts#AppPage` (TS/XHTML DSL) |
| `scripts/` | `panels.lua` (Lua runtime panels) |
| `python/` | `helpers.py` (embedded Python) |
| `blueprint.json` | Langflow-style app graph |
| `flows/flows.json` | Optional background/triggered services |
| `zig-services/` | Zig C-ABI library (`nexus_zig`) |
| `build_app.sh` | Full build: venv + deps + g++ modules + link |

## Adoption paths

1. **Custom** — edit `src/`, disable flows in `nxs_config.json`
2. **Blueprint only** — wire nodes in `blueprint.json`
3. **Blueprint + flows** — enable `flows/flows.json`

## Build

```bash
./build_app.sh              # recommended (venv + C++20 modules via g++)
cd zig-services && zig build app   # same, delegated from Zig
```

Zig does **not** compile C++20 named modules; `build_app.sh` uses `g++ -fmodules-ts`.

## Docs

- [Architecture overview](../../docs/architecture/overview.md)
- [Coding with Nexus](../../docs/guides/coding-with-nexus.md)
- [Blueprint schema](../../docs/templates/blueprint-schema.md)
