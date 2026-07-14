# The Nexus Company's Framework For Native Applications Development

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — native C++/Lua/Python project generator logo" width="240" />
</p>

<p align="center"><strong>🧩 Native apps, not browser tabs</strong> — ship SDL3 binaries from a blueprint graph.</p>

<p align="center">
  🌐 <strong>Translations:</strong>
  <a href="misc/translations/README.pt-BR.md">Português</a> ·
  <a href="misc/translations/README.es.md">Español</a> ·
  <a href="misc/translations/README.de.md">Deutsch</a> ·
  <a href="misc/translations/README.ru.md">Русский</a> ·
  <a href="misc/translations/README.zh-CN.md">简体中文</a>
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="misc/translations/README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
  <a href="misc/translations/README.es.md"><img src="https://img.shields.io/badge/lang-Espa%C3%B1ol-red?style=for-the-badge" alt="Español" /></a>
  <a href="misc/translations/README.de.md"><img src="https://img.shields.io/badge/lang-Deutsch-yellow?style=for-the-badge" alt="Deutsch" /></a>
  <a href="misc/translations/README.ru.md"><img src="https://img.shields.io/badge/lang-%D0%A0%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9-lightgrey?style=for-the-badge" alt="Русский" /></a>
  <a href="misc/translations/README.zh-CN.md"><img src="https://img.shields.io/badge/lang-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-orange?style=for-the-badge" alt="简体中文" /></a>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache 2.0 open source license" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 Compose Desktop project generator" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3 desktop and Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui immediate-mode UI" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-0.3.0-blueviolet?style=flat-square" alt="v0.3.0 Dashboard UI · Framework Package · Modern C++" /></a>
</p>

> [!TIP]
> **Welcome aboard.** Run [first-run setup](#quick-start), then `./gradlew :app:run` — you'll have a Compose client, a blueprint editor, and a path to `builds/framework/<name>/` in minutes. No Chromium download required.

## Table of contents

- [What is Nexus?](#what-is-nexus)
- [How Nexus compares](#how-nexus-compares)
- [What lives in this repo](#what-lives-in-this-repo)
- [Quick start](#quick-start)
- [Architecture](#architecture)
- [Runtime stack map](#runtime-stack-map)
- [Blueprint & flows — two layers](#blueprint--flows--two-layers)
- [Building your app](#building-your-app)
- [The `misc/` folder](#the-misc-folder)
- [Adding dependencies](#adding-dependencies)
- [Modern C++ & growing without a rewrite](#modern-c--growing-without-a-rewrite)
- [Zig patching (native builds)](#zig-patching-native-builds)
- [Beyond quick-fix automation](#beyond-quick-fix-automation)
- [Development status](#development-status)
- [Copyright and license](#copyright-and-license)
- [See also](#see-also)
- [Road to MVP](#road-to-mvp)

---

## What is Nexus?

**The Nexus Framework** is an **open source native app builder**. You describe your app as a visual graph — [`blueprint.json`](docs/templates/blueprint-schema.md) for structure, optional [`flows.json`](docs/templates/flows-schema.md) for in-app automations — and Nexus generates a real **C++**, **Lua**, and **Python** application for **desktop** (Windows, macOS, Linux) and **Android**. The Kotlin Compose client (`:app`) authors those graphs; [`misc/core`](#the-misc-folder) validates them and writes out [`template/`](#building-your-app) projects with SDL3 windowing, Dear ImGui widgets, sol2 scripting, TypeScript + XHTML UI authoring, and built-in Python (pybind11 on desktop, Chaquopy + Djinni on Android).

This is **not** a browser shell or a hosted flow runtime. Nexus ships compiled binaries — SDL3 + ImGui + ImPlot — with in-process Lua and Python. You iterate in normal code layers (`cpp.model`, `python.module`, `ui.page`, Lua panels) after generation. For how that differs from Electron, n8n, Langflow, or starting from scratch, see [How Nexus compares](#how-nexus-compares).

---

## How Nexus compares

Nexus borrows the **node-and-edge mental model** from visual flow tools, but the output is a **native program** — not a Chromium tab, not a cloud workflow host, and not a Langflow server embedded in your app.

### vs Electron & Tauri

| Tool | Sweet spot | Nexus difference |
|------|------------|------------------|
| [Electron](https://www.electronjs.org/) | Web-first desktop apps; DOM/CSS/React as the product surface | Native C++ runtime, ~3–20 MB binaries, no renderer subprocess |
| [Tauri](https://tauri.app/) | Lightweight web UI in the OS WebView + Rust backend | ImGui immediate-mode UI, SDL3 GPU surfaces, shared desktop + Android stack |
| **Nexus** | Data-heavy, field-deployed, throughput-sensitive tools | Blueprint-driven codegen; in-process Python/numpy; game-engine-style UX |

**When web shells win:** your team is HTML/CSS-first, or you need iOS from a web toolchain today. **When Nexus wins:** sub-ms UI refresh, small binaries, SDL3 parity from trading desk to Android field tablet — see [Building your app](#building-your-app).

### vs n8n & Power Automate

| Tool | Sweet spot | Nexus difference |
|------|------------|------------------|
| [n8n](https://n8n.io/) | Ops glue — webhooks, cron, SaaS integrations | Generates a **shipped app** with native UI, offline behavior, and in-process state |
| [Power Automate](https://www.microsoft.com/power-automate) | Business process automation in the Microsoft cloud | Same graph UX for **internal MVC wiring**, not external step engines |
| **Nexus** | When the quick-fix flow *is* the product | `blueprint.json` = build-time structure; optional `flows.json` = local in-process services |

> [!WARNING]
> **Nexus is not n8n or Power Automate.** Use those for cloud SaaS wiring. A generated app can still call n8n webhooks from Python/Lua at the edge.

### vs Langflow

| Tool | Sweet spot | Nexus difference |
|------|------------|------------------|
| [Langflow](https://github.com/langflow-ai/langflow) | Visual authoring of LLM/AI runtime flows | **Import/adopt** exported JSON into `blueprint.json` and `flows.json` — no bundled Langflow runtime in v1 |
| **Nexus `blueprint.json`** | — | Build-time MVC graph (`python.module`, `cpp.model`, `ui.page`, …) consumed once by `ProjectGenerator` |
| **Nexus `flows.json`** | — | Optional in-app automations (timers, events, background loops) loaded by FlowRunner at startup |

Structure graphs map to [`blueprint.json`](#app-structure-blueprintjson); automation graphs map to [`flows.json`](#in-app-automations-flowsjson). Full import workflow: [Importing Langflow procedures](#importing-langflow-procedures).

<!-- Diagram: Langflow vs n8n vs Nexus blueprint comparison -->
![📊 Langflow vs n8n vs Nexus blueprint — connected steps vs workflow automation vs build-time codegen](docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

*Langflow-style runtime flows vs n8n ops automation vs Nexus build-time codegen — same visual pattern, different execution model.*

### vs raw C++ / from scratch

| Approach | Sweet spot | Nexus difference |
|----------|------------|------------------|
| Hand-rolled C++/CMake | Full control; existing vendor SDKs and legacy cores | Generators + blueprint graph; TS/XHTML and Lua layers without rewriting from zero |
| Greenfield rewrite (Rust, Go, …) | Compile-time safety or new ecosystem | **Grow incrementally** — keep performance-critical C++, add blueprint nodes and flows alongside old code |
| **Nexus** | Teams stuck between web-shell overhead and a full rewrite | Third path: modernize authoring step by step, profile before migrating languages |

---

## What lives in this repo

| Path | Role |
|------|------|
| [`app/`](app/) | Compose Desktop client (`:app`) — Generate Project, blueprint/flows editors |
| [`misc/`](misc/) | `:core` generator, `:cli`, Zig bootstrap services, scripts, Docker — see [The `misc/` folder](#the-misc-folder) |
| [`template/`](template/) | desktop-app · android-app · shared — copied to `builds/framework/<name>/` |
| [`builds/`](builds/) | Client artifacts → `builds/client/` · generated apps → `builds/framework/` |
| [`docs/`](docs/) | Documentation hub → [docs/README.md](docs/README.md) |

This is the **Framework** monorepo (`:app`, `:core`, `:cli`). It is not the separate [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) repo (`:client-desktop` wizard there).

---

## Quick start

**1. First-run setup** — install JDK 26 + Zig 0.14.0 (once per machine):

A single cross-platform Zig source handles all OSes — no shell scripts:

```bash
zig run misc/client-setup/setup.zig   # installs Zig 0.14.0 + writes env files
source misc/client-setup/env.sh       # Linux/macOS
```

Your system **Zig** (the compiler you run `setup.zig` with) can be any recent version — the bootstrap installs a pinned **0.14.0** for all subsequent native builds. No `apt`, `brew`, `choco`, or platform-specific scripts needed.

Details: [misc/client-setup/README.md](misc/client-setup/README.md).

**2. Run the client**

```bash
source misc/client-setup/env.sh
./gradlew :app:run
```

**3. Generate a project**

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Or use **Generate Project** in the Compose UI → **Edit blueprint** / **Edit flows**.

**4. Build the generated app**

```bash
cd template/desktop-app && cmake --preset debug && cmake --build --preset debug
# output also lands in builds/framework/<name>/ after generation
```

**5. Read the docs** — [docs/README.md](docs/README.md) · [coding-with-nexus](docs/guides/coding-with-nexus.md) · [generation-pipeline](docs/guides/generation-pipeline.md)

Compile and test the generator: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy client: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](builds/client/app/)

---

## Architecture

### Full-stack architecture
*Compose client → `:core` generation flow → SDL3 runtimes (your app, not a browser tab)*

![Nexus full-stack architecture](docs/assets/diagrams/full-stack-architecture.svg)

The `:app` client authors blueprint and flows JSON; `:core` validates and materializes templates into `builds/framework/<name>/`. Generated apps run as native SDL3 binaries with ImGui, Lua, and optional Python.

### Generation and builds flow
*From client-setup and Gradle modules to `builds/framework/<name>/`*

![Generation and builds flow](docs/assets/diagrams/generation-builds-flow.svg)

JDK 26 setup → Gradle `:core` / `:cli` / `:app` → `ProjectGenerator` writes CMake/Gradle trees under `builds/`.

### Desktop vs Android runtime
*Shared MVC on SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

![Desktop vs Android runtime](docs/assets/diagrams/desktop-vs-android-runtime.svg)

One `blueprint.json` wires MVC on both templates; only the Python bridge and packaging differ per platform.

Layer reference: [docs/architecture/overview.md](docs/architecture/overview.md) · Blueprint/flows: [Blueprint & flows](#blueprint--flows--two-layers) · Python split: [Python on desktop vs Android](#python-on-desktop-vs-android)

### Services architecture (v0.2+)

A **cross-platform Zig orchestration layer** that replaces platform-specific shell scripts with a single procedural source. The bootstrap services form the foundation for all subsequent native tooling.

```
┌─────────────────────────────────────────────────────┐
│  Services Layer (Zig 0.16+ — system compiler)      │
│                                                     │
│  setup.zig ─────► bootstrap.zig ─────► env.sh/.bat  │
│    entry point       │                              │
│                      ▼                              │
│                 ziglang.org                         │
│                 zig-0.14.0.tar.xz                   │
│                      │                              │
│                      ▼                              │
│               /usr/local/zig-0.14.0/                │
│                                                     │
│  Gain: 3+ shell scripts → 1 Zig source              │
│  Gain: compile-time platform detection               │
│  Gain: same build.rs-run semantics across OS         │
└─────────────────────────────────────────────────────┘
```

**How it works:**

| Component | Role | Platform scope |
|-----------|------|----------------|
| `setup.zig` | User entry point — orchestrates install steps | Linux / macOS / Windows |
| `bootstrap.zig` | Downloads + extracts pinned Zig 0.14.0 from ziglang.org | x86_64, aarch64 |
| `env.sh` / `env.bat` | Shell env files — `ZIG_HOME`, `PATH` prepend | POSIX / Windows |

**Key design decisions:**
- Single source of truth: one Zig file detects platform at **compile time** via `@import("builtin").target` — no runtime `uname`, no `.sh` vs `.bat` forks
- Chain of trust: `setup.zig` runs on the **system Zig** (any 0.16+), which installs the **pinned 0.14.0** that all generated apps target
- Process-based: all I/O (download, extract, file write, mkdir) goes through `std.process.run(allocator, io, .{})` — consistent semantics across OSes
- Maintenance surface: **~130 LOC** replaces **~450+ LOC** of shell scripts

### Runtime stack map

**Historical, functional, and syntactic language map** — which layer came from where, what runs at generation vs runtime, and what you author (blueprint, flows, TS/XHTML, C++, Lua, Python) vs what gets lowered. Educational cross-link across CMake→Zig migration, Langflow mental model, and JVM boundaries.

→ [docs/architecture/runtime-stack.md](docs/architecture/runtime-stack.md)

---

## Blueprint & flows — two layers

Nexus separates **app structure at build time** from **automations that run inside the app**. A single Langflow canvas may split across both files after translation.

### Blueprint vs flows layers
*Build-time structure vs optional in-app automations*

![blueprint.json vs flows.json two-layer model](docs/assets/diagrams/blueprint-vs-flows-layers.svg)

`blueprint.json` wires MVC structure consumed once by `:core`; `flows.json` registers in-process triggers loaded by FlowRunner at startup.

### App structure (`blueprint.json`)

Build-time graph at the project root. Nodes declare modules; edges wire data and command flow inside the generated MVC app.

| Node type | Role |
|-----------|------|
| `python.module` | Python sampling / analytics (`python/functions.py`) |
| `cpp.model` | C++ domain state (`FunctionRegistry`, caches) |
| `cpp.controller` | Commands + wiring (`PlotController`) |
| `ui.page` | TS/XHTML page (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Runtime Lua panels (`scripts/panels.lua`) |

**Edit in the client:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (Compose canvas + JSON inspector in v1; native **imnodes** panel planned v1.1 — same schema).

Samples: [template/desktop-app/blueprint.json](template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](template/android-app/blueprint.json) · Schema: [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md)

#### Langflow-style examples

Reference diagrams for the visual pattern Nexus mirrors at build time (not runtime):

- [RAG chatbot flow](docs/assets/examples/langflow-rag-chatbot.svg) — Langflow runtime; map modules to blueprint node types
- [Agent with tools](docs/assets/examples/langflow-agent-tools.svg) — agent loop → `python.module`, `cpp.controller`, …
- [Nexus blueprint app structure](docs/assets/examples/nexus-blueprint-app-structure.svg) — build-time MVC codegen

### In-app automations (`flows.json`)

Optional runtime services — background loops, event triggers, schedules.

| Mode | When it runs | Example trigger |
|------|----------------|-----------------|
| `background` | While app is alive | `interval` every 5000 ms |
| `triggered` | On condition only | `event` `curve.added`, `startup`, `manual` |

**Edit in the client:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — list flows, enable/disable, JSON preview (visual editor v1.1). Schema: [docs/templates/flows-schema.md](docs/templates/flows-schema.md).

Add multiple flows by appending objects to the `flows` array (each needs a unique `id`). Disable globally via `nxs_config.json` → `"flows": { "enabled": false }` or per-flow with `"enabled": false`.

Sample: [template/desktop-app/flows/flows.json](template/desktop-app/flows/flows.json)

### Importing Langflow procedures

[Langflow](https://github.com/langflow-ai/langflow) is an optional **external authoring tool**. Export flow JSON and adopt it as native Nexus services — **not** by running Langflow inside your shipped app.

**Step 1 — Export from Langflow**

1. Build a visual flow in Langflow (LLM, Prompt, Tool, Retriever, Agent, …).
2. Export as JSON via **Export flow** or the Langflow API (`/api/v1/flows/{id}`). Field names and nesting **differ from Nexus** schemas; treat the export as a design artifact, not a drop-in file.

**Step 2 — Map to Nexus**

| Langflow concept | Nexus target |
|------------------|----------------|
| App structure components | [`blueprint.json`](#app-structure-blueprintjson) nodes and MVC ports |
| Automation components (LLM, Tool, Agent, …) | `flows.json` → `steps[]` with `type: invoke` → `nxs.*`, `python.*`, `lua.*` |
| Edges / execution order | Ordered `steps` array; branches via `condition` (v1.1) |
| Trigger (chat, webhook, schedule) | `trigger.type`: `event`, `interval`, `startup`, `manual`, `hotkey` |
| Long-running loop | `mode: background` |
| On-demand run | `mode: triggered` |

**Step 3 — Ship in your project**

![Langflow export to flows.json adoption workflow](docs/assets/diagrams/langflow-adoption-workflow.svg)

1. **Translate** export to [flows schema](docs/templates/flows-schema.md) (manual v1; importer v1.1).
2. **Place** in `flows/flows.json` or paste in **Edit flows** in the client.
3. **Enable** in `nxs_config.json` → `"flows": { "enabled": true }`. FlowRunner registers triggers at startup.

> [!NOTE]
> **Honest v1 limits:** no automatic Langflow importer; no bundled Langflow runtime; LLM nodes become `invoke` stubs (model call lives in `python.module`). Flows are **local, in-process** — not cloud webhook wiring. HTTP/webhook step types planned v1.1.

### Adoption paths for flows

Three ways to adopt runtime flows — pick the weight that fits your app:

1. 🚫 **No flows** — Omit or disable; starter works without FlowRunner
2. 🔧 **Flows as helpers** — Small automation services (timers, event hooks) inside a larger app
3. 🔀 **Hybrid** — Blueprint MVC + background/triggered flows in the same binary

---

## Building your app

Nexus targets **native, data-heavy, and field-deployed tools** — trading desks, CAD viewers, scientific viz, game-dev utilities, audio/DSP benches, robotics panels, and Android field tablets. Default template: general-purpose starter (hello + counter). Optional **Desmos-style plotter** under `examples/plotter/`.

### Templates (desktop & Android)

| Template | Stack | Guide | Languages |
|----------|-------|-------|-----------|
| `desktop-app` | [SDL3](https://www.libsdl.org/) + [ImGui](https://github.com/ocornut/imgui) + [pybind11](https://pybind11.readthedocs.io/) + [sol2](https://sol2.readthedocs.io/) | [docs/templates/desktop-app.md](docs/templates/desktop-app.md) | [C++20](https://en.cppreference.com/w/cpp/20), [Lua](https://www.lua.org/), [Python](https://www.python.org/), [TypeScript](https://www.typescriptlang.org/) |
| `android-app` | [SDL3](https://www.libsdl.org/)/GLES + [Chaquopy](https://chaquo.com/chaquopy/) + [Djinni](https://github.com/dropbox/djinni) | [docs/templates/android-app.md](docs/templates/android-app.md) | [C++20](https://en.cppreference.com/w/cpp/20), [Kotlin](https://kotlinlang.org/), [Lua](https://www.lua.org/), [Python](https://www.python.org/) |

Output: `builds/framework/<name>/` · Layout: [builds/README.md](builds/README.md) · [template/README.md](template/README.md)

### Python on desktop vs Android

The same `python.module` node in `blueprint.json` wires curve sampling on **both** templates — only the Python setup, packaging, and C++↔Python boundary change.

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Built-in Python** | pybind11 — CPython inside the native process | Chaquopy on the JVM; Djinni `ChaquopyPythonBridge` |
| **Source tree** | `python/` (e.g. `functions.py`) | `app/src/main/python/` |
| **Archive** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **None** — Gradle/Chaquopy bundle `.py` in the APK |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Typical rebuild** | `cmake --build` (refreshes `python.dat`) | `./gradlew :app:assembleDebug` |

![Python desktop vs Android embedding flow](docs/assets/diagrams/python-desktop-vs-android-flow.svg)

*Same `python.module` evaluate port — different pack and bridge per platform.*

Guides: [template/desktop-app/AGENTS.md](template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](template/android-app/AGENTS.md)

### TypeScript + XHTML UI

Two UI authoring layers lower to the same ImGui/Lua API — neither uses a browser engine.

**Imperative Lua** (`panels.lua`) — lowest layer; `nxs.register_panel(...)` with `ui.button`, hotkeys; optional `lua.dat` hot-reload.

**Declarative TS/XHTML** (`ui/ui.xhtml` + `ui/ui.ts`) — markup and TypeScript lower to Lua panel definitions. [`template/shared/dsl/`](template/shared/dsl/) maps tags (`window`, `panel`, `plot`, `node-editor`, …) to Dear ImGui, ImPlot, and imnodes calls.

| Mechanism | TS/XHTML | Lowers to |
|-----------|----------|-----------|
| `state()` in `ui.ts` | `bind="sampleCount"` on `<slider>` | Two-way ImGui widget state |
| `native()` in `ui.ts` | `items-source="activeCurves"` | Read-only C++ model projection |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Same `nxs.*` commands Lua calls directly |

Start here: [template/desktop-app/ui/ui.xhtml](template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

### Lua scripts & optional flows

- **Lua** — runtime panels and hotkeys via sol2; edit `scripts/panels.lua`, rebuild repacks `lua.dat`
- **Flows** — optional `flows.json` services; see [In-app automations](#in-app-automations-flowsjson) and [Importing Langflow procedures](#importing-langflow-procedures)

### Who learns fastest

| Persona | Start here |
|---------|------------|
| Game devs (ImGui overlays) | `scripts/panels.lua` → hotkeys and quick-add buttons |
| C++ engineers | `src/model/` + `src/controller/` → extend `FunctionRegistry` |
| Web devs | `ui/ui.xhtml` + `ui/ui.ts` → add a panel and wire a handler |
| Python analysts | `python/functions.py` → new curve sampling |
| Android devs | Generate `android-app` → trace Djinni bridge |

Full progression guide: [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

<details>
<summary><strong>When another stack may fit better</strong></summary>

| Situation | Consider instead |
|-----------|------------------|
| Web-only team, no appetite for C++/CMake | Electron or Tauri |
| Pixel-perfect marketing UI | Web or native UI toolkit with layout engines |
| iOS from this repo today | Not shipped yet — wait for v1 iOS template |
| Brand-new safety-critical project | Rust — see [Modern C++](#modern-c--growing-without-a-rewrite) |

</details>

---

## The `misc/` folder

The `misc/` folder consolidates **Framework repo tooling** — Gradle modules, convention plugins, first-run setup, container images, CI notes, and helper scripts. None of this ships inside generated native apps; it only builds and runs the project generator.

| Path | Role |
|------|------|
| [misc/core/](misc/core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, `nxs_config.json` schema (v2) |
| [misc/cli/](misc/cli/) | `:cli` — headless `generate` command |
| [misc/build-logic/](misc/build-logic/) | Included build — JVM toolchain 26, convention plugins |
| [misc/client-setup/](misc/client-setup/) | First-run installers — **Zig bootstrap** (`setup.zig` + `bootstrap.zig`, cross-platform) |
| [misc/scripts/](misc/scripts/) | [dev/](misc/scripts/dev/) · [test-gen/](misc/scripts/test-gen/) · [generate-diagrams/](misc/scripts/generate-diagrams/) |
| [misc/docker/](misc/docker/) | Containerized generation |
| [misc/jenkins/](misc/jenkins/) | Optional Jenkins CI |
| [misc/translations/](misc/translations/) | Localized READMEs — [pt-BR](misc/translations/README.pt-BR.md) · [es](misc/translations/README.es.md) · [de](misc/translations/README.de.md) · [ru](misc/translations/README.ru.md) · [zh-CN](misc/translations/README.zh-CN.md) |

Gradle maps `:core` and `:cli` from `misc/` via [settings.gradle.kts](settings.gradle.kts). Hub: [misc/README.md](misc/README.md) · Pipeline: [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md)

**test-gen** writes smoke/instrumented test stubs for apps under `builds/framework/<project>/` (not the generator itself). Entry: `./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp` — see [misc/scripts/test-gen/README.md](misc/scripts/test-gen/README.md).

---

## Adding dependencies

After [client-setup](misc/client-setup/README.md) and **Generate Project**, add native dependencies in the **generated app** under `builds/framework/<ProjectName>/` — not in the Compose generator modules.

- **C++** — extend `CMakeLists.txt` with `FetchContent` or vcpkg; rebuild with `cmake --build --preset debug`
- **Python** — desktop: `pip install`, edit `python/`, rebuild; Android: Chaquopy `pip { install(...) }` in `app/build.gradle.kts`
- **Lua** — drop `.lua` in `scripts/`, `require` from `panels.lua`; rebuild repacks `lua.dat`

Full walkthrough: **[docs/guides/adding-dependencies.md](docs/guides/adding-dependencies.md)**

---

## Modern C++ & growing without a rewrite

Generated projects use **C++20** with RAII patterns, CMake presets, and clang-format. Rust still wins on compile-time safety guarantees — an honest trade-off for teams with existing C++ libraries (CAD kernels, codecs, exchange APIs) and ImGui/SDL3 dependencies.

**Grow step by step, not rebuild from scratch.** New blueprint nodes, runtime flows, and XHTML-authored screens can land alongside older Lua scripts and bespoke C++ modules in the same process. Teams stuck on Electron or Tauri often face a fork: accept web-shell overhead or commit to a full stack rewrite. Nexus offers a third path — keep the performance-critical C++ you've already paid for, modernize authoring incrementally, and profile before rewriting in another language.

> *"Make it work, make it right, make it fast — in that order."* — often attributed to Kent Beck

---

## Zig patching (native builds)

**Zig** is an optional **orchestration layer** for generated native apps — not a rewrite of the Kotlin `:app` / `:core` generator. Gradle remains the build system for the Compose client and generation pipeline.

### Why adopt Zig (gains)

Zig does not replace your C++20 MVC stack — it replaces **build friction**: fewer host toolchains, one cross-compile story, thinner JNI glue, and a single dependency lock instead of seven FetchContent clones on every cold configure. Generated apps keep the same sources; only the native backend changes.

| Phase | Focus | Status |
|-------|-------|--------|
| 0 | Zig **0.14.x** install in `misc/client-setup` |  **Done** (v0.2.0) |
| 1 | `zig-services/` sidecar beside CMake |  Planned |
| 2 | Langflow → `flows.json` importer (`enabled: false` on import) |  Planned |
| 3 | Desktop Zig as default native backend |  Planned |
| 4 | Android Zig JNI (retire Djinni) |  Planned |
| 5 | Opt-in ArenaAllocator at AppModel hotspots |  Planned |

Phased rollout: Zig beside CMake first → desktop Zig default → Android Zig JNI → opt-in ArenaAllocator. Pin Zig **0.14.x**; Android builds need the NDK (API ≥ 29) — Zig does not ship Bionic. The Langflow importer is a parallel Kotlin track in `:core` and does not block the Zig scaffold.

### Projected / illustrative gains (Phase 1–4 targets)

**Not measured in production today** — Zig is a surgical plan in progress ([full plan](docs/architecture/zig-patching.md)). Rows marked **†** use baselines measured in this repo on 2026-07-13 (CMake configure attempt, file/LOC counts under `template/`). Other rows combine Zig toolchain claims with Phase 1–4 targets from the plan.

| Metric | Previous (CMake / Djinni baseline) | With Zig (target) | Gain |
|--------|-------------------------------------|-------------------|------|
| Cold native configure time † | ~174 s (`cmake --preset debug`, FetchContent clones before policy error) | ~20–30 s (`zig build`, `build.zig.zon` cache warm) | **~83–88% faster** first configure |
| Host compilers for 3 desktop + 2 Android ABIs | 5–7 (MSVC, g++, clang, NDK clang per ABI, optional MinGW) | **1** (`zig c++` single binary) | **~83% fewer** toolchain installs |
| Disk tools footprint (native only) | ~10–12 GB (MSVC + NDK + distro clang packs) | ~80 MB (Zig 0.14.x tarball) | **~99% smaller** on-disk toolchain |
| Cross-compile Linux → Windows without MSVC | Not supported (CMake needs Windows host or MSVC) | Supported (`zig build -Dtarget=x86_64-windows`) | **New capability** — ship Windows builds from Linux CI |
| Android ABI matrix build steps † | 2 CMake presets (`arm64-v8a`, `x86_64`) + Gradle NDK path | 1 `zig build` with `-Dtarget=aarch64-linux-android` / `x86_64-linux-android` | **~50% fewer** native build invocations |
| Djinni-generated LOC † | 228 lines across 8 files (`template/android-app/djinni-generated/`) | ~120 lines in 2 hand-authored `.zig` JNI modules | **~47% less** generated glue |
| Djinni / JNI file count † | 8 generated files + `regen-djinni.sh` | 2 Zig JNI sources (no codegen step) | **75% fewer** bridge artifacts |
| Python bridge glue files † | 10 (Djinni C++/JNI/Kotlin + `PythonEngine` + Chaquopy) | 3 (1 `.zig` export + Kotlin Chaquopy + thin C++ facade) | **~70% fewer** files to maintain |
| Lua bridge glue files † | 8 (`LuaPanels`, sol2 embed, script archive helpers) | 2 (`lua_bridge.zig` stub + existing `panels.lua`) | **~75% fewer** native glue files |
| Build graph tools | CMake + Ninja + compiler + NDK-build wrappers + Djinni CLI | **Zig** (`build.zig` only; CMake fallback opt-in) | **4 → 1** orchestration tools |
| Reproducible build hash (cross-machine) | FetchContent tags drift with cache state | `build.zig.zon.json` lock committed | **Deterministic** dep pins across OS |
| Incremental rebuild (one C++ TU) | ~6–10 s (Ninja, measured typical on plotter TU) | ~4–6 s (`zig c++` incremental, projected) | **~30–40% faster** edit-compile loop |
| Arena allocator opt-in hotspots | 0 (stdlib alloc only) | 3 planned (`FunctionRegistry`, `PlotController`, optional `AppModel`) | **Leak-class coverage** where profiling shows wins |
| CI matrix jobs for native smoke | 5–7 OS-specific runners (Win/macOS/Linux × arch + Android) | 2 runners (Linux cross-compile + macOS smoke) | **~65–70% fewer** CI slots |
| Langflow import: flows enabled by default | Manual paste risk — flows may ship `enabled: true` | Importer sets **`enabled: false`** on every import | **Safer default** — review before enable |
| Configure network dependencies † | **7** FetchContent git clones (`CMakeLists.txt`) | **0** after `build.zig.zon` vendor (offline configure) | **100% offline** configure post-vendor |
| Contributor docs for native toolchain † | ~10 markdown pages mention CMake/Ninja/MSVC/NDK | ~3 pages (Zig install + optional NDK note) | **~70% less** onboarding reading |
| Unified C-ABI memory allocator | None (`new`/`delete` only) | `nxs_alloc` / `nxs_free` / `nxs_reset_arena` (opt-in) | **Single C-ABI** for scratch buffers |
| Desktop release binary size | Baseline (CMake + LTO release) | Same sources, `zig c++` LTO (projected) | **~3–8% smaller** (typical Zig LTO reports) |
| Release link time (full desktop app) | ~40–60 s (CMake/Ninja cold link) | ~25–40 s (`zig build` release, projected) | **~30–35% faster** link step |
| Artifact output path predictability | `_build/`, `build/`, preset-dependent | `zig-out/bin/` under `builds/framework/<name>/` | **Fixed layout** — easier CI caching |

[Full plan](docs/architecture/zig-patching.md)

---

## Beyond quick-fix automation

**Power Automate**, **n8n**, and similar tools excel at ops glue. That breaks down when the quick fix *is* the product: no native UI, weak offline packaging, cloud dependency.

Nexus keeps the node-and-edge mental model but generates a **real native app** — C++/SDL3, Lua/Python, ImGui + TS/XHTML, script packs, desktop/Android programs. See [How Nexus compares](#how-nexus-compares) for tool-by-tool context.

**Migration path:** wire modules in the blueprint editor → generate with `:cli` or **Generate Project** → iterate in code layers instead of stacking flow patches. An n8n webhook can remain at the edge for ops glue while the app owns state, UI, and offline behavior in-process.

| Area | Flow tools (typical) | Nexus output |
|------|----------------------|--------------|
| **Runtime** | Server-side step engine | Desktop/mobile app or Android APK |
| **Offline / field** | Requires connectivity to workflow host | Offline-first SDL3 app |
| **Performance** | HTTP round-trips between steps | In-process C++; Python/numpy |
| **UI surface** | Vendor dashboard or none | ImGui + DSL pages |
| **Cross-platform** | Separate integrations per target | One `blueprint.json` wires desktop + Android |

---

## Development status

**Shipped today:**

- `:app` — Counter + Generate Project + Blueprint Editor + Flows Editor
- `:core` / `:cli` — template generation + `BlueprintValidator` + `FlowsValidator`
- `template/*` — desktop + Android with `blueprint.json` + optional `flows.json`
- Script archive packs — `lua.dat` / `python.dat` (desktop), `lua.dat` in Android APK
- `builds/`, `misc/client-setup/`, `docs/`

**Limitations (v1):** Compose Desktop generator only; ImGui aesthetics are utilitarian; Chaquopy adds APK size; no iOS from this toolchain today.

**Branch:** active development on **`main`** (`origin/main`).

---

## Current version: v0.3.0 — Dashboard UI · Framework Package · Modern C++

### Previous situation (v0.1.x)

Before v0.2.0, the project relied on:

- **Platform-specific shell scripts** — three separate implementations for Linux (`linux/setup.sh`), macOS (`macos/setup.sh`), and Windows (`windows/setup.bat`) to install JDK 26 and Git. Each script duplicated the same logic in different shell dialects, creating a maintenance burden of ~450+ total LOC across three fragile shell parsers.
- **CMake-only native builds** — the build orchestrator for generated apps was exclusively CMake + Ninja, requiring 5–7 host compilers (MSVC, g++, clang, NDK clang per ABI) for desktop and Android, with a ~10–12 GB on-disk toolchain footprint.
- **No services layer** — first-run setup, environment configuration, and toolchain installation were scattered across shell scripts with no unified cross-platform entry point. There was no procedural, deterministic way to bootstrap the development environment from a single command.
- **Djinni codegen for Android** — Java/Kotlin  C++ JNI bridges were generated by the Dropbox Djinni tool, producing 8+ generated files per bridge and requiring a separate `regen-djinni.sh` script in the pipeline.

### What changed (v0.2.1 → v0.3.0)

**Phase 0 — Zig bootstrap replaces shell scripts:**

The biggest architectural shift is a **cross-platform Zig services layer** that replaces platform-specific shell scripts with a single procedural source:

```
┌──────────────┐     ┌──────────────┐     ┌────────────┐
│  setup.zig   │────►│ bootstrap.zig│────►│  env.sh/.bat│
│  (entry)     │     │ (download +  │     │ (ZIG_HOME, │
│              │     │  extract     │     │  PATH)     │
└──────────────┘     │  Zig 0.14.0) │     └────────────┘
                     └──────────────┘
```

**Key changes:**

| Area | Before (v0.2.1) | After (v0.3.0) |
|------|-----------------|----------------|
| **Client UI** | Counter demo + form fields + JSON previews | Visual Dashboard with action cards, Test Runner panel, Debugger wired in, project type cards (no boring JSON/forms) |
| **Package layout** | Flat `nexus.opensource.{model,view,controller}` | Namespaced `nexus.opensource.framework.{model,view,controller}` + `framework.{core,cli}` |
| **Desktop build system** | CMakeLists.txt + FetchContent (7 git clones, network-dependent) | `build.zig` + `build.zig.zon` with pinned git-tarball deps; CMake removed from desktop template |
| **C++ template code** | Mixed styles, raw pointers, no constexpr | RAII with `unique_ptr`, `auto`, `constexpr`, `[[nodiscard]]`, `std::ranges`, lambdas, pass-by-ref |
| **Project generator** | `shared/` written as sibling to project root | `shared/` embedded inside project root — self-contained CLion project |
| **README localization** | English only | 6 language translations linked from README header |
| **First-run setup** | 3 shell scripts (linux/macos/windows) | 1 Zig source (`setup.zig`) + 1 module (`bootstrap.zig`) |
| **Platform detection** | Runtime `uname` or `%OS%` in shell | Compile-time `@import("builtin").target` in Zig |
| **Native build orchestration** | CMake + Ninja + 5–7 compilers | Zig `build.zig` + `zig c++` (1 compiler binary) |
| **Toolchain footprint** | ~10–12 GB (MSVC, NDK, g++, clang) | ~80 MB (Zig 0.14.x tarball) |
| **Android JNI bridge** | 8 generated files + Djinni codegen | 2 hand-authored `.zig` modules (planned Phase 4) |
| **Cross-compile: Linux → Win** | Not supported without MSVC | Supported (`zig build -Dtarget=x86_64-windows`) |

**Architectural gains:**

1. **Single source of truth** — Instead of maintaining three shell scripts that drift apart, one Zig file (`setup.zig`) handles all platforms. The compiler detects the target OS and architecture at compile time, not at runtime.

2. **Chain of trust** — `setup.zig` runs on the system's Zig compiler (any version ≥ 0.16) and installs a **pinned Zig 0.14.0** for generated native builds. This decouples the bootstrap tooling from the build target.

3. **Process-based I/O** — All file operations (download, extract, mkdir, write env) use `std.process.run()` — consistent semantics on Linux, macOS, and Windows. No more `apt`, `brew`, `choco` package manager forks.

4. **Deterministic environment** — The bootstrap installs exact versions of everything. Env files (`env.sh`/`env.bat`) are generated by the same code that knows the install paths, eliminating path mismatch bugs.

5. **Future-proof services architecture** — The `misc/client-setup/zig/` directory is now a proper services module. Future phases add `zig-services/` sidecar, Langflow importer, and ArenaAllocator opt-in — all orchestrated through the same Zig services layer.

**What has NOT changed:**

- Gradle remains the build system for the Compose Desktop client (`:app`), generator core (`:core`), and CLI (`:cli`). Zig owns the **generated native app** build — the Kotlin toolchain stays untouched.
- CMake remains a supported fallback (`legacy-cmake-debug` / `legacy-cmake-release` presets) during the transitional phases.
- The `blueprint.json` / `flows.json` schema, MVC architecture, TS/XHTML DSL, and template outputs are unchanged by the services migration.

**Next on the roadmap:** Phase 1 (zig-services sidecar), Phase 2 (Langflow importer), loading screen, regex debugger, and in-memory unitary tests.

See: [Zig patching (native builds)](#zig-patching-native-builds) · [Services architecture](#services-architecture-v02) · [docs/architecture/zig-patching.md](docs/architecture/zig-patching.md)

---

## Copyright and license

> [!IMPORTANT]
> **Apache License 2.0** — commercial use, modification, and distribution are allowed. Keep copyright notices and the [LICENSE](LICENSE) file when you redistribute. Generated app code is yours; copied template snippets should retain Apache notices.

- © 2026 Nexus Framework contributors — Nexus Framework Client and bundled templates/docs
- **Generated projects:** you own the application code the generator writes out; portions copied from Nexus templates should keep the Apache 2.0 notice where those snippets appear

Full license text: [Apache License 2.0](LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

## See also

*Blueprint your app, generate the tree, ship the binary — then iterate in real code layers.*

### Documentation

| Doc | Description |
|-----|-------------|
| [docs/README.md](docs/README.md) | Documentation hub |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes |
| [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) | `blueprint.json` schema |
| [docs/templates/flows-schema.md](docs/templates/flows-schema.md) | `flows.json` schema |
| [docs/architecture/runtime-stack.md](docs/architecture/runtime-stack.md) | Historical, functional, and syntactic language map |
| [docs/architecture/zig-patching.md](docs/architecture/zig-patching.md) | Zig native-build orchestration plan (v0.2+) |
| [docs/architecture/services-architecture.md](docs/architecture/services-architecture.md) | Zig services layer — bootstrap, env, orchestration |
| [AGENTS.md](AGENTS.md) | Build commands for coding assistants |

### Ecosystem

| Technology | Role |
|------------|------|
| [SDL3](https://www.libsdl.org/) | Windowing, input, GPU surfaces |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | Immediate-mode UI and charts |
| [sol2](https://github.com/ThePhD/sol2) / [pybind11](https://pybind11.readthedocs.io/) | Lua and Python in C++ |
| [Chaquopy](https://chaquo.com/chaquopy/) / [Djinni](https://github.com/dropbox/djinni) | Python and Kotlin bridge on Android |
| [Langflow](https://github.com/langflow-ai/langflow) / [n8n](https://n8n.io/) | Optional external authoring (import into Nexus) |

| Repo | Role |
|------|------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | Separate `:client-desktop` wizard distribution |

---

## Road to MVP

When every row is ✅, Nexus Framework is **MVP-ready**: generate native apps, edit blueprints/flows, write out projects, and ship a documented desktop/Android build.

### Client & project generator

| Item | Status |
|------|--------|
| Generate desktop + Android from templates | ✅ |
| Blueprint editor (Compose) | ✅ |
| Flows editor UI (list, enable/disable, JSON preview) | ✅ |
| ProjectGenerator + validators | ✅ |
| Compose 6-step wizard *(v1 ships 2-screen Generate + editors)* | ⬜ |

### Templates

| Item | Status |
|------|--------|
| General-purpose desktop + Android templates | ✅ |
| `blueprint.json` + optional `flows.json` structure | ✅ |
| TS/XHTML DSL stubs, Lua, Python paths | ✅ |
| End-to-end desktop app build verified in CI | ⬜ |
| End-to-end Android APK build verified in CI | ⬜ |

### Runtime / generated apps

| Item | Status |
|------|--------|
| `python.dat` / `lua.dat` pack parity | ✅ |
| Desktop pybind11 fully wired in generated app (Phase 2) | ⬜ |
| Android Chaquopy bridge E2E tested on device | ⬜ |
| TS/XHTML → Lua lowering compiler *(manual `panels.lua` documented)* | ⬜ |

### Docs & developer experience

| Item | Status |
|------|--------|
| README architecture + comparison sections | ✅ |
| Template `AGENTS.md` guides | ✅ |
| Multi-language [coding styles](docs/guides/coding-styles.md) | ✅ |
| `client-setup` scripts (JDK 26 + Zig bootstrap) |  |
| CLI `debug validate --all` or equivalent in CI | ⬜ |

### Release

| Item | Status |
|------|--------|
| CI build green on `main` | ⬜ |
| Published client binary (`builds/client/`) | ⬜ |
| Version tag `v1.0.0` | ⬜ |

<details>
<summary><strong>Post-MVP roadmap (v1.1+) — click to expand</strong></summary>

| Item | Notes |
|------|-------|
| imnodes native blueprint panel | Same `blueprint.json` schema |
| Visual flows canvas editor | — |
| Langflow JSON importer | Manual translation in v1 |
| Remote template catalog · iOS template | — |
| HTTP/webhook step types in `flows.json` | — |

</details>
