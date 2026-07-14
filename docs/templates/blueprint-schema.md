# blueprint.json schema (v1)

Langflow-style app graph at the project root. The Compose **Blueprint Editor** in `:app` reads and writes this file; generation validates it and copies or overrides it into `builds/framework/<name>/`.

**Future (v1.1):** native **imnodes** panel embedded via C++/JNI interop will use the same JSON — no schema migration planned.

## Langflow-style nodes vs n8n

Nexus users often ask how `blueprint.json` relates to **Langflow** and **n8n**. All three use nodes and edges, but the layers differ.

![Langflow vs n8n vs Nexus blueprint](../assets/diagrams/langflow-vs-n8n-blueprint.svg)

| | **Nexus `blueprint.json`** | **Langflow** | **n8n** |
|---|---------------------------|--------------|---------|
| **Purpose** | Author in-app MVC wiring for generated native apps | Author LLM / component chains for AI apps | Automate external services (webhooks, APIs, schedules) |
| **Node types** | `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script` | LLM, Prompt, Tool, Memory, … | HTTP, Webhook, Cron, CRM connectors, … |
| **Execution** | Consumed at generation; runtime is C++/Lua/Python on SDL3 | Flow runtime executes the graph server-side | Workflow engine on n8n host |
| **Where it runs** | Generated desktop binary or Android APK | Langflow server / desktop | n8n cloud or self-hosted |
| **When to use** | Rewire screens, controllers, Python sampling inside your app | Prototype and ship AI agent pipelines | Ops glue, ETL, third-party integrations |

**Nexus does not replace n8n.** Use `blueprint.json` for **internal** app structure (Langflow mental model). Use n8n when the generated app must trigger external automation — e.g. call an n8n webhook from `python/functions.py` or `scripts/panels.lua` while the blueprint stays focused on MVC edges (`evaluate` → `sampleCache` → `commands`).

**Structure vs automation:** Langflow exports that wire screens, controllers, and Python modules map here. Runtime automation (timers, event hooks, background loops) maps to [`flows.json`](flows-schema.md) instead — see [Langflow adoption](flows-schema.md#langflow-adoption-v1) and [README § Using Langflow to author flows](../../README.md#using-langflow-to-author-flows).

**Client path:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint**. v1 ships a Compose canvas + JSON inspector; v1.1 adds imnodes with the same schema. All v1 node types use `editor.paradigm: "langflow"`; future **n8n-style** automation nodes may use `"n8n"` — not shipped in v1.


## Top-level fields

| Field | Type | Description |
|-------|------|-------------|
| `$schema` | string | `https://nexus.dev/schemas/blueprint-1.json` |
| `name` | string | Human-readable flow name (e.g. `MyApp flow`) |
| `description` | string | Optional notes |
| `editor` | object | Authoring metadata (`tool`, `version`, `grid_snap`, `paradigm`) |
| `nodes` | array | Graph nodes |
| `edges` | array | Directed connections between nodes |

### `editor.paradigm`

| Value | Meaning |
|-------|---------|
| `langflow` (default) | Typed DAG nodes — all v1 node types |
| `n8n` | Reserved for future runtime automation hooks — no v1 node types |

## Node types

All v1 types use the **Langflow-style** paradigm (`BlueprintParadigm.LANGFLOW`). Each maps to generated source artifacts:

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

## Runtime flows (`flows.json`)

> Merged from: `flows-schema.md` (now part of this doc)

**flows.json** (optional) adds runtime automation to a generated app — background loops, event triggers, and scheduled tasks. Distinct from `blueprint.json` which wires design-time MVC.

| Schema | Purpose | Editor |
|--------|---------|--------|
| `blueprint.json` | App structure (pages, models, services) | Compose Canvas |
| `flows/flows.json` | Runtime service automations | Enable/disable list |

Flows live under `runtimes/flows/` in the generated app tree. Enable via `nxs_config.json`:
```json
{ "flows": { "enabled": true, "max_scheduled": 10, "max_concurrent": 4 } }
```

---

## Editor path (v1 vs v1.1)

| Version | UI | Notes |
|---------|-----|-------|
| **v1 (shipped)** | Compose Canvas + JSON inspector in `:app` | Add/remove nodes, drag positions, connect edges, preview JSON |
| **v1.1 (planned)** | imnodes native panel | Same file; grid snap from `editor.grid_snap` |

Run `./gradlew :app:run` → **Generate Project** → **Edit blueprint**.
