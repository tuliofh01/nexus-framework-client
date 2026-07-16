# AGENTS.md — Nexus Desktop Template

Guide for AI coding assistants working in a **generated** Nexus desktop project from `template/desktop-app/`.

## What this template is

A **general-purpose** SDL3 + Dear ImGui starter (hello + counter) with full Nexus feature support: Lua panels, TS/XHTML DSL, pybind11 Python, `blueprint.json`, and optional `flows/flows.json`. MVC lives under `src/`.

An optional **Desmos-style plotter** sample lives under `examples/plotter/` — build with `-DBUILD_NEXUS_EXAMPLES=ON`.

## Key directories

| Path                | Role                                        |
|--------------------|--------------------------------------------|
| `src/model/`        | `AppModel` — counter + greeting             |
| `src/controller/`   | `AppController`, `PythonEngine` (pybind11)  |
| `src/view/`         | `AppView` (ImGui), `LuaPanels` (sol2)       |
| `src/service/`      | `FlowRunner` — optional flows runtime       |
| `ui/`               | `ui.xhtml` + `ui.ts#AppPage` (TS/XHTML DSL) |
| `scripts/`          | `panels.lua` (Lua runtime panels)           |
| `python/`           | `helpers.py` (embedded Python)              |
| `blueprint.json`    | Langflow-style app graph                    |
| `flows/flows.json`  | Optional background/triggered services      |
| `examples/plotter/` | Optional plotter demo (delete if unused)    |
## Adoption paths

1. **Custom** — edit `src/`, disable flows in `nxs_config.json`
2. **Blueprint only** — wire nodes in `blueprint.json`
3. **Blueprint + flows** — enable `flows/flows.json`

## Build

```bash
zig build
```

Optional plotter: `zig build -DBUILD_NEXUS_EXAMPLES`

## Docs

- [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md) — C++20, Zig, Lua, TS, Python
- [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) (includes flows schema section)
