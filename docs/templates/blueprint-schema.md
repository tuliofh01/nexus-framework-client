# blueprint.json schema (v1)

Langflow-style app graph at the project root. The Compose **Blueprint Editor** in `:app` reads and writes this file; generation validates it and copies or overrides it into `builds/framework/<name>/`.

**Future (v1.1):** native **imnodes** panel embedded via C++/JNI interop will use the same JSON — no schema migration planned.

## Top-level fields

| Field | Type | Description |
|-------|------|-------------|
| `$schema` | string | `https://nexus.dev/schemas/blueprint-1.json` |
| `name` | string | Human-readable flow name (e.g. `MyApp flow`) |
| `description` | string | Optional notes |
| `editor` | object | Authoring metadata (`tool`, `version`, `grid_snap`) |
| `nodes` | array | Graph nodes |
| `edges` | array | Directed connections between nodes |

## Node types

| `type` | Role | Typical `data` keys |
|--------|------|---------------------|
| `python.module` | Python sampling / analytics | `source`, `exports`, `packages` |
| `cpp.model` | C++ domain state | `class`, `catalog` |
| `cpp.controller` | Commands + orchestration | `class`, `settings` |
| `ui.page` | TS/XHTML page | `source`, `controllerScript`, `widgets` |
| `lua.script` | Runtime Lua panels | `source`, `hotkeys` |

Each node:

```json
{
  "id": "controller-plot",
  "type": "cpp.controller",
  "position": { "x": 320, "y": 220 },
  "data": { "class": "nxs::controller::PlotController" }
}
```

## Edges

```json
{
  "id": "e1",
  "source": "py-functions",
  "target": "controller-plot",
  "port": "evaluate"
}
```

- `source` / `target` must reference existing node `id`s.
- `port` names the data or command channel (e.g. `sampleCache`, `commands`).

## Sample

Bundled plotter samples:

- [template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json)
- [template/android-app/blueprint.json](../../template/android-app/blueprint.json)

## Validation (`:core`)

`BlueprintValidator` checks:

- Unique node and edge ids
- Known node types
- Edge endpoints exist
- Non-blank blueprint name

`ProjectGenerator` validates rendered `blueprint.json` after template emit and writes a custom blueprint when the Compose editor passes one in `ProjectSpec.blueprint`.

## Editor path (v1 vs v1.1)

| Version | UI | Notes |
|---------|-----|-------|
| **v1 (shipped)** | Compose Canvas + JSON inspector in `:app` | Add/remove nodes, drag positions, connect edges, preview JSON |
| **v1.1 (planned)** | imnodes native panel | Same file; grid snap from `editor.grid_snap` |

Run `./gradlew :app:run` → **Generate Project** → **Edit blueprint**.
