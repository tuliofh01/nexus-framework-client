# Architecture Overview

Generated Nexus apps share one layered design. The scaffold client (`app/`) uses **MVC in Kotlin Compose**; generated C++ templates mirror it under `src/model/`, `src/controller/`, and `src/view/`.

## Full stack + blueprint authoring

![Nexus full stack — Compose client, blueprint.json graph, generation, C++ MVC on SDL3/ImGui, Python bridges](../assets/diagrams/full-stack-architecture.svg)

![Langflow vs n8n vs Nexus blueprint](../assets/diagrams/langflow-vs-n8n-blueprint.svg)

This diagram replaces earlier per-use-case flowcharts. Trading desks, CAD viewers, and scientific tools all compose the same layers; only domain code in `model/` and `controller/` changes.

### Langflow vs n8n

- **Langflow:** visual DAG of typed nodes for ML/LLM — Nexus reuses this UX for **native app structure** (`python.module` → `cpp.controller` → `ui.page`, etc.).
- **n8n:** workflow automation with triggers and integrations at **runtime** — Nexus blueprint is **build-time only**; optional n8n-style hooks are roadmap.
- **Client path:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** → custom graph passed via `ProjectSpec.blueprint` to `ProjectGenerator`.

| Layer | Technology | Role |
|-------|------------|------|
| **Scaffold client** | Kotlin Compose MVC (`:app`) | **Generate Project** + **Edit blueprint** + **Edit flows** (v1 Compose editors) |
| **Blueprint graph** | `blueprint.json` (imnodes schema) | Langflow-style nodes + edges — generation consumes the graph |
| **Runtime flows** | `flows/flows.json` (optional) | Background/triggered services — `FlowRunner` no-op when disabled |
| **Generation** | `:core`, `:cli` in `misc/` | `ProjectGenerator`, `BlueprintValidator`, `FlowsValidator` → `builds/framework/<name>/` |
| **Authoring** | TS/XHTML, Lua, Python files | UI components and runtime panels referenced by blueprint nodes |
| **Scripting** | Lua 5.4 + **sol2** | Runtime panels, hotkeys (`lua.script` nodes) |
| **Domain** | C++20 MVC | `cpp.model`, `cpp.controller`, ImGui/ImPlot view (`ui.page`) |
| **Rendering** | ImGui + ImPlot on **SDL3** | Desktop OpenGL, Android GLES |
| **Python** | pybind11 (desktop) / **Chaquopy** (Android) | `python.module` nodes — numpy, analytics |
| **Android bridge** | **Djinni** | C++ ↔ Kotlin/JVM |

### Blueprint node types

| `type` | Generated artifact |
|--------|-------------------|
| `python.module` | `python/functions.py` — sampling, numpy |
| `cpp.model` | `src/model/` — domain state |
| `cpp.controller` | `src/controller/` — commands |
| `ui.page` | `ui/ui.ts`, `ui/ui.xhtml` |
| `lua.script` | `scripts/panels.lua` |

Edges wire data flow (e.g. `evaluate` → `sampleCache` → `activeCurves` → `commands`). See [blueprint-schema.md](../templates/blueprint-schema.md) and template samples under `template/*/blueprint.json`.

**Client path:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint**. v1.1 adds a native imnodes panel using the same JSON.

### Langflow-style nodes vs n8n

`blueprint.json` follows a **Langflow-style** typed graph: nodes are app modules, edges are data/command ports inside the generated MVC stack. **n8n** sits at a different layer — workflow automation across external services (webhooks, REST, schedules). Nexus does not replace n8n; a generated app can call an n8n webhook from Python or Lua while the blueprint documents internal wiring only.

| Layer | Tool | Role |
|-------|------|------|
| In-app authoring | Nexus `blueprint.json` | Structure C++/Python/Lua/UI modules and their connections |
| AI flow authoring | Langflow | Chain LLM and tool nodes (analogous mental model, different domain) |
| External automation | n8n | Integrate SaaS, cron jobs, webhooks outside the native binary |

Full comparison table: [blueprint-schema.md § Langflow vs n8n](../templates/blueprint-schema.md#langflow-style-nodes-vs-n8n).

## Generation and builds

![Generation flow — client-setup through :app/:cli to builds/framework and native binary](../assets/diagrams/generation-builds-flow.svg)

The pipeline validates `blueprint.json` and optional `flows/flows.json` after template emit. Custom graphs from the Compose editors are passed via `ProjectSpec.blueprint` and `ProjectSpec.flows`.

## Desktop vs Android runtime

![Desktop vs Android — shared MVC/ImGui/SDL3; pybind11 vs Chaquopy + Djinni](../assets/diagrams/desktop-vs-android-runtime.svg)

Both templates share the same `blueprint.json` node graph; only the Python bridge and OS host differ at runtime.

## Themes and fonts

- Themes: `template/shared/themes/` — loaded by `NexusTheme::applyFromFile()`
- Nerd Font: `template/shared/assets/fonts/` — loaded by `FontConfig::loadNerdFont()`
- Logo: `template/shared/assets/nexus-logo.png` (also `docs/assets/nexus-logo.png`)

## Related

- [Blueprint schema](../templates/blueprint-schema.md)
- [Flows schema](../templates/flows-schema.md)
- [Coding with Nexus](../guides/coding-with-nexus.md)
- [Generation pipeline](../guides/generation-pipeline.md)
- [Desktop template](../templates/desktop-app.md)
- [Android template](../templates/android-app.md)
