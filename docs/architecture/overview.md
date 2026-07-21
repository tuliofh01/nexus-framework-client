# Architecture Overview

Generated Nexus apps share one layered design. The scaffold client (`app/`) uses **MVC in Kotlin Compose**; generated C++ templates mirror it under `src/model/`, `src/controller/`, and `src/view/`.

For a multi-dimensional map — **historical lineage** (CMake→Zig, Djinni→Zig JNI, Langflow UX), **functional responsibilities** per language at runtime, and **syntactic authoring** (what you write vs what gets lowered) — see the [Language stack section](#language-stack-runtime) below.

## Full stack + blueprint authoring

### Nexus full-stack architecture
*Client, generation pipeline, templates, and native runtimes*

End-to-end view from the Compose scaffolder (`:app`) through `:core` generation into C++/Lua/Python on SDL3. Start here when explaining how blueprint nodes become runtime modules.

![Nexus full-stack architecture](../assets/diagrams/full-stack-architecture.svg)

### Langflow vs n8n vs Nexus blueprint
*Typed in-app DAG vs runtime workflow automation vs design-time codegen*

Clarifies why `blueprint.json` is build-time structure (Langflow-style) rather than n8n-style runtime automation.

![Langflow vs n8n vs Nexus blueprint](../assets/diagrams/langflow-vs-n8n-blueprint.svg)

This diagram replaces earlier per-use-case flowcharts. Trading desks, CAD viewers, and scientific tools all compose the same layers; only domain code in `model/` and `controller/` changes.

### Langflow vs n8n

- **Langflow:** visual DAG of typed nodes for ML/LLM — Nexus reuses this UX for **native app structure** (`python.module` → `cpp.controller` → `ui.page`, etc.).
- **n8n:** workflow automation with triggers and integrations at **runtime** — Nexus blueprint is **build-time only**; optional n8n-style hooks are roadmap.
- **Client path:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** → custom graph passed via `ProjectSpec.blueprint` to `ProjectGenerator`.

| Layer               | Technology                                  | Role                                                                                    |
|--------------------|---------------------------------------------|----------------------------------------------------------------------------------------|
| **Scaffold client** | Kotlin Compose MVC (`:app`)                 | **Generate Project** + **Edit blueprint** + **Edit flows** (v1 Compose editors)         |
| **Blueprint graph** | `blueprint.json` (imnodes schema)           | Langflow-style nodes + edges — generation consumes the graph                            |
| **Runtime flows**   | `flows/flows.json` (optional)               | Background/triggered services — `FlowRunner` no-op when disabled                        |
| **Generation**      | `:core`, `:cli` at repo root                | `ProjectGenerator`, `BlueprintValidator`, `FlowsValidator` → `builds/framework/<name>/` |
| **Authoring**       | TS/XHTML, Lua, Python files                 | UI components and runtime panels referenced by blueprint nodes                          |
| **Scripting**       | Lua 5.4 + **sol2**                          | Runtime panels, hotkeys (`lua.script` nodes)                                            |
| **Domain**          | C++20 MVC                                   | `cpp.model`, `cpp.controller`, ImGui/ImPlot view (`ui.page`)                            |
| **Rendering**       | ImGui + ImPlot on **SDL3**                  | Desktop OpenGL, Android GLES                                                            |
| **Python**          | pybind11 (desktop) / **Chaquopy** (Android) | `python.module` nodes — numpy, analytics                                                |
| **Android bridge**  | **Zig JNI** (replaces Djinni)               | C++  Kotlin/JVM                                                                         |
### Blueprint node types

| `type`           | Generated artifact                      |
|-----------------|----------------------------------------|
| `python.module`  | `python/functions.py` — sampling, numpy |
| `cpp.model`      | `src/model/` — domain state             |
| `cpp.controller` | `src/controller/` — commands            |
| `ui.page`        | `ui/ui.ts`, `ui/ui.xhtml`               |
| `lua.script`     | `scripts/panels.lua`                    |
Edges wire data flow (e.g. `evaluate` → `sampleCache` → `activeCurves` → `commands`). See [blueprint-schema.md](../templates/blueprint-schema.md) and template samples under `template/*/blueprint.json`.

**Client path:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint**. v1.1 adds a native imnodes panel using the same JSON.

### Langflow-style nodes vs n8n

`blueprint.json` follows a **Langflow-style** typed graph: nodes are app modules, edges are data/command ports inside the generated MVC stack. **n8n** sits at a different layer — workflow automation across external services (webhooks, REST, schedules). Nexus does not replace n8n; a generated app can call an n8n webhook from Python or Lua while the blueprint documents internal wiring only.

| Layer               | Tool                   | Role                                                                |
|--------------------|------------------------|--------------------------------------------------------------------|
| In-app authoring    | Nexus `blueprint.json` | Structure C++/Python/Lua/UI modules and their connections           |
| AI flow authoring   | Langflow               | Chain LLM and tool nodes (analogous mental model, different domain) |
| External automation | n8n                    | Integrate SaaS, cron jobs, webhooks outside the native binary       |
Full comparison table: [blueprint-schema.md § Langflow vs n8n](../templates/blueprint-schema.md#langflow-style-nodes-vs-n8n).

## Generation and builds

### Generation and builds flow
*From client-setup and Gradle modules to `builds/framework/<name>/`*

Pipeline from first-run setup through `:app`/`:cli` into emitted templates; validates `blueprint.json` and optional `flows/flows.json` after emit.

![Generation and builds flow](../assets/diagrams/generation-builds-flow.svg)

The pipeline validates `blueprint.json` and optional `flows/flows.json` after template emit. Custom graphs from the Compose editors are passed via `ProjectSpec.blueprint` and `ProjectSpec.flows`.

### Pipeline stages

| Stage                | Action                                                                       |
|---------------------|-----------------------------------------------------------------------------|
| **Validate**         | Check project name pattern                                                   |
| **Prepare directory**| Create `outputPath/projectName` or fail if non-empty (`--force` to overwrite) |
| **Render template**  | Copy `template/desktop-app/` or `template/android-app/` with placeholder sub  |
| **Copy shared**      | Copy `template/shared/` to `outputPath/shared/`                              |
| **Validate config**  | Parse rendered `nxs_config.json` (schema v2)                                 |
| **Validate blueprint**| Parse `blueprint.json` via `BlueprintValidator`                             |

### Template selection

| Type    | Template folder        | CLI `--type` |
|---------|-----------------------|--------------|
| Desktop | `template/desktop-app/`| `desktop`     |
| Android | `template/android-app/`| `android`     |

### Placeholders

| Key                       | Example                                       |
|--------------------------|-----------------------------------------------|
| `{{projectName}}`         | `MyApp`                                        |
| `{{windowTitle}}`         | `MyApp - built with The Nexus Framework`       |
| `{{cppStandard}}`         | `20`                                           |
| `{{license}}`             | `Nexus-1.0` (see repo [LICENSE](../../LICENSE)) |
| `{{appType}}`             | `desktop` / `android`                          |
| `{{createdAt}}`           | ISO-8601 timestamp                             |
| `{{scriptProtectionEnabled}}` | `true` / `false`                           |
| `{{scriptProtectionSalt}}`| UUID salt when protection enabled              |

### Script archives

Desktop templates pack `scripts/` and `python/` into binary archives beside the executable. Android packs Lua into APK assets (Python stays on Chaquopy paths).

| Archive     | Magic | Source               | Desktop output      | Android output           |
|------------|-------|----------------------|---------------------|-------------------------|
| `lua.dat`   | `LUAC` | `scripts/**/*.lua`   | `misc/lua.dat`       | `build/assets/lua.dat`   |
| `python.dat`| `PYAC` | `python/**/*.py`     | `misc/python.dat`    | N/A (Chaquopy)           |

### CLI usage

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"     # dry-run
./gradlew :cli:run --args="generate --type desktop --name MyApp"               # generate
./gradlew :cli:run --args="generate --type android --name MyApp --output builds/framework/MyApp"
./gradlew :cli:run --args="generate --type desktop --name MyApp --force"       # overwrite
```

Docker: `./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp`
Jenkins pipeline: `misc/jenkins/Jenkinsfile` (parameterized: `PROJECT_NAME`, `TEMPLATE_TYPE`, `OUTPUT_DIR`).

## Desktop vs Android runtime

### Desktop vs Android runtime
*Shared MVC on SDL3/ImGui; pybind11 vs Chaquopy + Zig JNI (Djinni retired)*

Same `blueprint.json` node graph on both templates; only the Python bridge and OS host differ at runtime.

![Desktop vs Android runtime](../assets/diagrams/desktop-vs-android-runtime.svg)

Both templates share the same `blueprint.json` node graph; only the Python bridge and OS host differ at runtime.

### Desktop template

Generated from `template/desktop-app/`. SDL3 + OpenGL 3.3 window, Dear ImGui + ImPlot UI, Lua 5.4 via sol2 scripting, pybind11 Python embed, TS/XHTML authoring. MVC layout: `src/model/`, `src/controller/`, `src/view/`. Build with `cd zig-services && zig build`.

### Android template

Generated from `template/android-app/`. SDL3 GLES + ImGui, Chaquopy Python, hand-authored Zig JNI bridge (3 files in `zig-services/jni/`). Boot: `AppCore.loadLibrary` → `installPythonBridge` → `SDL_main`. Build: `zig build -Dtarget=aarch64-linux-android` then `./gradlew :app:assembleDebug`.

## Themes and fonts

- Themes: `template/shared/themes/` — loaded by `NexusTheme::applyFromFile()`
- Nerd Font: `template/shared/assets/fonts/` — loaded by `FontConfig::loadNerdFont()`
- Logo: `template/shared/assets/nexus-logo.png` (also `docs/assets/nexus-logo.png`)

## Language stack (runtime)

Nexus uses 7 languages across 3 boundaries. Each lives in its natural layer — no one-language-fits-all:

- **Kotlin** — Compose Desktop UI + generation pipeline (`:app` / `:core` / `:cli`)
- **C++20** — Runtime MVC with RAII, `std::ranges`, `constexpr` (`template/*/src/`)
- **Zig 0.16.0** — C-ABI/JNI sidecars and allocator (`zig-services/`)
- **Lua 5.4** — sol2 runtime scripting — panels, hotkeys, quick iteration (`scripts/`)
- **Python 3.11+** — pybind11 embedded NumPy/scipy (desktop) or Chaquopy (Android)
- **TypeScript + XHTML** — Declarative UI bindings and markup that lower to ImGui calls

The **generation boundary** is crossed by `ProjectGenerator` (Kotlin → native source trees).  
The **build boundary** by Zig (`build.zig` → compiled binary).  
The **runtime boundary** by sol2, pybind11, and Chaquopy (in-process language bridges).

## Risk analysis

**Overall score: 72 / 100 — High Risk.** Main concerns:

| Risk                                                        | Impact                                        | Mitigation                                                                        |
|------------------------------------------------------------|-----------------------------------------------|----------------------------------------------------------------------------------|
| Doc–code drift (README oversells wizard features)           | High — agent/contributor confusion            | Wave 3 README rewrite reduces gap; `nxs_config.json` v2 schema is source of truth |
| Dual-repo template sync (this repo  nexus-framework-client) | Medium — stale templates                      | CI-generated, diff-gated on release                                               |
| Synchronous Python on UI thread                             | Medium — frame drops on heavy inference       | Async queue planned for v0.4                                                      |
| FetchContent network dependency                             | Low — Zig pinned tarballs are offline-capable | `build.zig.zon` with vendored fallback                                            |
Full retrospective in [misc/README.md](../../misc/README.md).

## Architecture diagrams

| Diagram                      | File                                                                                        |
|-----------------------------|--------------------------------------------------------------------------------------------|
| Full-stack architecture      | [full-stack-architecture.svg](../assets/diagrams/full-stack-architecture.svg)               |
| Generation and builds flow   | [generation-builds-flow.svg](../assets/diagrams/generation-builds-flow.svg)                 |
| Desktop vs Android runtime   | [desktop-vs-android-runtime.svg](../assets/diagrams/desktop-vs-android-runtime.svg)         |
| Blueprint vs flows           | [blueprint-vs-flows-layers.svg](../assets/diagrams/blueprint-vs-flows-layers.svg)           |
| Python desktop vs Android    | [python-desktop-vs-android-flow.svg](../assets/diagrams/python-desktop-vs-android-flow.svg) |
| Langflow adoption            | [langflow-adoption-workflow.svg](../assets/diagrams/langflow-adoption-workflow.svg)         |
| Langflow vs n8n vs blueprint | [langflow-vs-n8n-blueprint.svg](../assets/diagrams/langflow-vs-n8n-blueprint.svg)           |
| Nexus blueprint structure    | [nexus-blueprint-app-structure.svg](../assets/diagrams/nexus-blueprint-app-structure.svg)   |
| UML activity diagrams        | [activity-diagrams.md](../assets/diagrams/activity-diagrams.md) — SVG index (client, generator, derived-app samples) |
| RAG chatbot flow             | [langflow-rag-chatbot.svg](../assets/diagrams/langflow-rag-chatbot.svg)                     |
| Agent with tools             | [langflow-agent-tools.svg](../assets/diagrams/langflow-agent-tools.svg)                     |
## Related

- [Blueprint schema](../templates/blueprint-schema.md)
- [Coding with Nexus](../guides/coding-with-nexus.md)
- [Docs hub](../README.md)
