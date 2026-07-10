# flows.json schema (v1)

Optional **runtime automation** graph — background loops, event triggers, and scheduled tasks inside a generated native app. Distinct from [`blueprint.json`](blueprint-schema.md), which documents **design-time** MVC wiring.

**Client path:** `./gradlew :app:run` → **Generate Project** → **Edit flows** (list, enable/disable, JSON preview in v1).

## Two layers (do not mix)

| Layer | File | Purpose |
|-------|------|---------|
| **App structure** | `blueprint.json` | Langflow-style wiring (`ui.page`, `cpp.model`, `python.module`, …) |
| **Runtime flows** | `flows/flows.json` | Optional service automations: background loops, event triggers, schedules |

Enable or disable flows in `nxs_config.json`:

```json
"flows": {
  "path": "flows/flows.json",
  "enabled": true
}
```

When `enabled` is `false`, `flows.json` is missing, or `"flows": []`, the native **FlowRunner** is a no-op — default plotter behavior is unchanged.

## Top-level fields

| Field | Type | Description |
|-------|------|-------------|
| `version` | int | Schema version (`1`) |
| `flows` | array | List of flow definitions |

## Flow definition

```json
{
  "id": "sync-curves",
  "name": "Resample on timer",
  "enabled": true,
  "mode": "background",
  "trigger": { "type": "interval", "ms": 5000 },
  "steps": [
    { "type": "invoke", "target": "nxs.set_samples", "args": ["${state.sampleCount}"] }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique flow id (required) |
| `name` | string | Human-readable label |
| `enabled` | bool | When `false`, runner skips this flow |
| `mode` | string | `background` or `triggered` |
| `trigger` | object | When the flow fires (see below) |
| `steps` | array | Ordered steps to execute |

### Modes

| Mode | Behavior |
|------|----------|
| `background` | Scheduler runs while the app is alive (e.g. interval timers) |
| `triggered` | Fires only when the trigger condition occurs |

## Trigger types

| `type` | Fields | Fires when |
|--------|--------|------------|
| `interval` | `ms` (positive int) | Every N milliseconds (usually `background` mode) |
| `event` | `name` (string) | App emits named event (e.g. `curve.added`) |
| `hotkey` | `key` (string) | User presses bound hotkey (v1 stub) |
| `startup` | — | Once after FlowRunner loads |
| `manual` | — | UI button or JVM stub (v1.1) |

Example triggers:

```json
{ "type": "interval", "ms": 5000 }
{ "type": "event", "name": "curve.added" }
{ "type": "hotkey", "key": "F5" }
{ "type": "startup" }
{ "type": "manual" }
```

## Step types (v1)

| `type` | Fields | Description |
|--------|--------|-------------|
| `invoke` | `target`, `args` | Call nxs/python/lua command (e.g. `nxs.log`, `nxs.set_samples`) |
| `condition` | `when` | Branch stub — evaluated in v1.1 |
| `delay` | `ms` | Pause before next step |

### Invoke targets (v1 samples)

| Target | Description |
|--------|-------------|
| `nxs.set_samples` | Calls controller `setSampleCount` (plotter) |
| `nxs.log` | Writes to stderr log |

Placeholders in `args`: `${state.sampleCount}` resolves from runtime chart settings.

### v1.1 roadmap

- `invoke` → HTTP/webhook steps (local only; not a replacement for n8n cloud)
- Visual flow canvas in Compose client
- Manual trigger buttons wired to native FlowRunner

## Multiple flows

Add several objects to the `flows` array — each with a unique `id`. Background and triggered flows can coexist:

```json
{
  "version": 1,
  "flows": [
    {
      "id": "sync-curves",
      "mode": "background",
      "trigger": { "type": "interval", "ms": 5000 },
      "steps": [{ "type": "invoke", "target": "nxs.set_samples", "args": ["512"] }]
    },
    {
      "id": "on-add",
      "mode": "triggered",
      "trigger": { "type": "event", "name": "curve.added" },
      "steps": [{ "type": "invoke", "target": "nxs.log", "args": ["Added curve"] }]
    }
  ]
}
```

## Langflow adoption (v1)

You can author automation graphs in [Langflow](https://github.com/langflow-ai/langflow) and adopt them into Nexus — Nexus does **not** run the Langflow runtime. Export flow JSON from Langflow (UI export or API), then manually map components to `steps[]` with `type: invoke`, edges to ordered steps, and triggers to `trigger.type` (`event`, `interval`, `startup`, `manual`, `hotkey`). Structural wiring (screens, controllers) belongs in [`blueprint.json`](blueprint-schema.md); runtime automation belongs here. Place the translated file at `flows/flows.json`, enable in `nxs_config.json`, and **FlowRunner** registers triggers at startup. An automatic Langflow importer is planned for v1.1; until then, LLM nodes become `invoke` stubs backed by `python.module` implementations. See [README § Using Langflow to author flows](../../README.md#using-langflow-to-author-flows) for the full mapping table and adoption workflow.

## Adoption paths

| Path | What you ship |
|------|----------------|
| **No flows** | Delete `flows/flows.json` or set `"flows": { "enabled": false }` — full custom app, zero automation layer |
| **Flows as helpers** | Small background/triggered services inside a larger MVC app |
| **Hybrid** | Blueprint-driven MVC + optional `flows.json` for timers and events |
| **Langflow export** | Design in Langflow → translate JSON → `flows.json` (manual v1; importer v1.1) |

## Runtime implementation

| Platform | Component |
|----------|-----------|
| Desktop | `src/service/FlowRunner.hpp` + `.cpp` — loaded from `main.cpp` |
| Android | Same C++ FlowRunner in SDL_main; `FlowRunner.kt` JVM stub for future manual triggers |

Generation validates `flows.json` via `FlowsValidator` in `:core` when a custom graph is supplied through `ProjectSpec.flows`.

## Related

- [blueprint-schema.md](blueprint-schema.md) — design-time app graph
- [desktop-app.md](desktop-app.md) · [android-app.md](android-app.md)
- Template samples: `template/desktop-app/flows/flows.json`, `template/android-app/flows/flows.json`
