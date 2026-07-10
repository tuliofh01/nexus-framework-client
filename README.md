# The Nexus Company's Framework For Native Applications Development

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="The Nexus Framework logo" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

**The Nexus Framework** scaffolds native C++/Lua/Python applications for **Desktop** (Windows, macOS, Linux) and **Android** — SDL3 windowing, sol2 scripting, TypeScript + XHTML UI authoring, and embedded Python (pybind11 on desktop, Chaquopy + Djinni on Android). ImGui + ImPlot render immediate-mode UIs without a browser engine.

If you're evaluating **web-shell** stacks — **Electron** (Chromium + JavaScript) or **Tauri** (OS WebView + Rust) — Nexus is a different bet: native C++ runtime, immediate-mode widgets, and in-process Lua/Python instead of HTML layout engines. Those tools excel when DOM/CSS is the product surface; Nexus excels when throughput, binary size, and a shared SDL3 stack across desktop and Android field hardware matter more.

## Blueprint nodes: Langflow-style vs n8n

Nexus ships a **Langflow-style app graph** at the project root. Nodes declare modules (`python.module`, `cpp.model`, `ui.page`, …); edges wire data and command flow inside the generated MVC app. The **`:core` generator** validates and consumes the graph when materializing `builds/framework/<name>/`.

![Langflow vs n8n vs Nexus blueprint — typed DAG vs workflow automation vs design-time codegen](docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

| Node type | Role |
|-----------|------|
| `python.module` | Python sampling / analytics (`python/functions.py`) |
| `cpp.model` | C++ domain state (`FunctionRegistry`, caches) |
| `cpp.controller` | Commands + orchestration (`PlotController`) |
| `ui.page` | TS/XHTML page (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Runtime Lua panels (`scripts/panels.lua`) |

| | **Langflow** | **n8n** | **Nexus `blueprint.json`** |
|---|-------------|---------|---------------------------|
| **Purpose** | ML / LLM flow authoring | Workflow automation (triggers, HTTP, integrations) | **Build-time** native app structure |
| **Node model** | Typed components (model, tool, memory) | Steps + triggers (webhook, cron, Slack, …) | Typed modules (`python.module`, `cpp.model`, …) |
| **Edges** | Data between components | Event / payload routing | MVC ports (`evaluate`, `sampleCache`, `commands`, …) |
| **Execution** | **Runtime** — user runs the flow | **Runtime** — schedule or webhook fires | **Design-time** — `ProjectGenerator` validates + emits |

**Edit in the client:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (Compose canvas + JSON inspector in v1; native **imnodes** panel planned for v1.1 — same schema). Samples: [template/desktop-app/blueprint.json](template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](template/android-app/blueprint.json). Schema: [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md).

### Langflow-style nodes vs n8n

Both tools use a **node-and-edge mental model**, but they solve different problems. Nexus `blueprint.json` is closer to **Langflow** (typed in-app graph) than to **n8n** (external workflow automation). Nexus does **not** replace n8n; the two can coexist. For when a flow should graduate into shipped native software — not just ops glue — see [Beyond quick-fix automation](#beyond-quick-fix-automation-from-flows-to-real-applications) below.

| | **Nexus `blueprint.json`** (Langflow-style) | **n8n** |
|---|---------------------------------------------|---------|
| **Purpose** | Author **in-app structure** — which C++/Python/Lua/UI modules connect and how data flows inside your native app | Automate **external workflows** — webhooks, REST APIs, schedules, SaaS integrations |
| **Node types** | `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script` | HTTP Request, Webhook, Cron, Slack, Postgres, … |
| **Execution model** | Graph is consumed at **generation**; runtime is compiled C++/Lua/Python on SDL3 | Server-side workflow engine runs steps on triggers or schedules |
| **Where it runs** | Inside the generated desktop binary or Android APK | n8n instance (cloud or self-hosted) |
| **When to use** | Rewiring plotter MVC, adding screens, mapping Python samples → controller → UI | Ops automation, ETL, alerting, glue between third-party services |

**Coexistence:** a generated Nexus app can call an n8n webhook from Python or Lua (e.g. post telemetry, trigger a downstream pipeline) while `blueprint.json` stays focused on **internal** app wiring — the same separation Langflow uses for LLM chains vs. what n8n uses for integration flows. Optional **n8n-style automation hooks** inside the blueprint schema are roadmap only; v1 node types are all Langflow-style (`editor.paradigm: langflow`).

**Client mapping:** **Edit blueprint** in `:app` mirrors the Langflow canvas — drag nodes, connect ports, preview JSON. v1.1 embeds **imnodes** natively with the same file; no schema migration planned.

**Visual examples:** See [RAG chatbot](docs/assets/examples/langflow-rag-chatbot.svg), [agent with tools](docs/assets/examples/langflow-agent-tools.svg), and [Nexus blueprint app structure](docs/assets/examples/nexus-blueprint-app-structure.svg) for Langflow-style node graphs and how Nexus maps the same mental model to design-time `blueprint.json`.

## What this repo is

| Today | Roadmap (v1.1+) |
|-------|-----------------|
| Compose Desktop client (`:app`) — Counter MVC demo + **Generate Project** + **Blueprint Editor** (JSON graph) | Full 6-step wizard, imnodes native panel |
| Generation pipeline (`:core`, `:cli` in `misc/`) — emit templates to `builds/framework/<name>/` | Remote template catalog, iOS template |
| Script archive packs — `lua.dat` + `python.dat` (desktop), `lua.dat` in APK assets (Android) | Chaquopy `python.dat` (not applicable — sources ship in APK) |

This is the **Framework** monorepo (`:app`, `:core`, `:cli`). It is not the separate [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) repo (`:client-desktop` wizard there).

## First run

Run one platform setup script, load the env file, then Gradle:

| Platform | Setup | Env |
|----------|-------|-----|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Requires **JDK 26** and Git — see [misc/client-setup/README.md](misc/client-setup/README.md).

## Quick start

```bash
source misc/client-setup/env.sh          # after first-run setup
./gradlew :app:run                  # Compose client
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Compile and test: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy client: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](builds/client/app/)

Build desktop template: `cd template/desktop-app && cmake --preset debug && cmake --build --preset debug`

Output layout: [builds/README.md](builds/README.md) · Templates: [template/README.md](template/README.md)

## Repository layout

```
Framework/
├── app/                 Compose Desktop client (`:app`) — MVC under `nexus.opensource/`
├── misc/
│   ├── build-logic/     Gradle convention plugins (included build, JVM toolchain 26)
│   ├── core/            Generation pipeline (`:core`) — ProjectGenerator, nxs_config schema
│   ├── cli/             Headless `generate` command (`:cli`)
│   ├── client-setup/    First-run JDK 26 + Git installers
│   ├── docker/          Optional containerized generation
│   ├── jenkins/         Optional Jenkins setup notes
│   └── scripts/         Repo automation (e.g. `generate-in-docker.sh`)
├── builds/              Client → builds/client/ · apps → builds/framework/<name>/
├── template/
│   ├── desktop-app/     Desktop output (C++/CMake plotter)
│   ├── android-app/     Android output (Gradle/Djinni/Chaquopy)
│   └── shared/          DSL, assets, themes, runtime
├── docs/                Documentation hub → docs/README.md
└── Jenkinsfile          Optional pipeline entry
```

## Use cases — what Nexus is built for

Nexus targets **native, data-heavy, and field-deployed tools** — trading desks, CAD viewers, scientific viz, game-dev utilities, audio/DSP benches, robotics panels, and Android field tablets. Throughput, binary size, and a shared SDL3 stack matter more than HTML layout.

| Use case | Why Nexus | Template |
|----------|-----------|----------|
| Trading / market-data desk | Sub-ms UI; C++ parsers; Python in-process | Desktop |
| CAD / mesh / point-cloud viewer | SDL3 GPU viewport; geometry in C++ | Desktop |
| Scientific visualization | numpy via pybind11; ImPlot charts | Desktop |
| Game dev tools | Immediate-mode UI; Lua hot-reload | Desktop |
| Audio / DSP workbench | Low-latency C++ signal path | Desktop |
| DevOps / infra monitor | Lightweight single binary | Desktop |
| Android field tablet | SDL3/GLES ImGui; Djinni; Chaquopy | Android |
| Robotics / teleop panel | Touch ImGui; `android.*` Lua bindings | Android |
| Embedded HMI | Same SDL3 stack on desktop and Android | Both |

Flagship sample: **Desmos-style plotter** — Python samples curves, C++ owns the model, ImGui draws. [docs/templates/desktop-app.md](docs/templates/desktop-app.md) · [docs/templates/android-app.md](docs/templates/android-app.md)

---

## Why Nexus performs better

Nexus is built for **throughput, footprint, and field deployment** — not for rendering marketing websites. Where Electron bundles Chromium and Tauri delegates to the OS WebView, generated Nexus apps stay in native address space end-to-end: C++ domain logic, SDL3 GPU surfaces, ImGui/ImPlot widgets, and optional Lua/Python layers without a browser process.

| Advantage | What it means in practice | Electron / Tauri context |
|-----------|---------------------------|---------------------------|
| **Binary size** | ~3–20 MB native binary + assets (grows with vendored `libs/`) | Electron installers commonly **85–250 MB** (bundled Chromium); Tauri typically **3–15 MB** but still ships WebView + frontend bundle |
| **No Chromium / WebView** | UI is ImGui + SDL3/OpenGL — no renderer subprocess, no DOM layout/paint | Electron = full browser stack; Tauri = system WebView + JS runtime |
| **Native memory for arrays** | Meshes, order books, and numpy buffers stay in the C++ heap; Python via pybind11/Chaquopy without marshaling through JS | Web shells copy or serialize data across JS boundaries |
| **SDL3 cross-platform** | Same windowing/input layer on Windows, macOS, Linux, and Android GLES | Mobile is secondary or a separate toolchain in most web-shell stacks |
| **sol2 + Lua hot-reload** | Edit `panels.lua`, repack optional `lua.dat` — runtime UI panels without recompiling C++ | Frontend HMR helps, but still an HTML/CSS/JS round-trip |
| **`python.dat` / `lua.dat` protection** | Optional v2 encrypted packs ship logic without loose `.py`/`.lua` on disk (desktop `misc/`; Android `lua.dat` in APK assets) | Not a first-class concern in typical Electron/Tauri asset models |
| **Sub-ms ImGui refresh** | Immediate-mode UI targets **<1 ms** per Dear ImGui guidance; no layout thrash | WebView layout + paint cycles dominate steady-state CPU |
| **Field tablet APK** | Android template: full-screen SDL3/GLES ImGui + Chaquopy Python on rugged devices — no WebView | Electron Android is not primary; Tauri Mobile remains WebView-based |
| **Same blueprint, desktop + Android** | One `blueprint.json` / imnodes workflow wires MVC on both templates | Separate web + mobile pipelines are common |
| **Djinni vs hand-rolled JNI** | Generated type-safe C++ ↔ Kotlin bridge on Android | N/A on desktop web shells; manual JNI boilerplate on native Android hybrids |

**When Nexus wins:** native throughput, small binaries, SDL3 parity from trading desk to Android field tablet, in-process Python/numpy, blueprint-driven rewiring, and game-engine-style immediate-mode UX — without paying for a browser engine you don't need.

**When Electron or Tauri wins:** your team is web-first, the UI is HTML/CSS/React, or you need iOS from a web-shell toolchain today. That's a fair trade — not a failure mode.

> **Honest caveat:** cross-framework benchmarks vary by app complexity, OS, and measurement method. Always profile *your* workload before choosing on size or RAM alone.

---

## Learning curve

Nexus has a real ramp — CMake, C++20, and immediate-mode UI are part of the deal — but the generated plotter gives you a working app on day one. The path below is designed to scan quickly.

### Who learns Nexus fastest

| Persona | Why it clicks | Start here |
|---------|---------------|------------|
| **Game devs** (ImGui debug overlays) | Already think in immediate-mode panels and hotkeys | `scripts/panels.lua` → tweak hotkeys and quick-add buttons |
| **C++ engineers** (CAD, scientific, trading) | Own the performance-critical path; Python/Lua are optional layers | `src/model/` + `src/controller/` → add a domain type to `FunctionRegistry` |
| **Web devs** (component mental models) | TS/XHTML DSL maps tags and `on-click` to familiar patterns — no DOM, native ImGui widgets | `ui/ui.xhtml` + `ui/ui.ts` → add a panel and wire a handler |
| **Python-first analysts** | Keep numpy/math in Python; C++ handles render and input | `python/functions.py` → new curve sampling without rewriting math in JS |
| **Android devs** (Kotlin, NDK-curious) | Djinni generates the JNI-free bridge; SDL3 hosts full-screen native UI | Generate `android-app` template → trace `MainActivity` → Djinni → C++ core |

**Harder fit:** designers-only or React-only teams who expect CSS layout and won't touch CMake/C++. Nexus is utilitarian ImGui, not a design system — willingness to read C++ and run a native build matters.

### Skills matrix

| Skill | Required? | Role in Nexus |
|-------|-----------|---------------|
| C++20 / CMake | **Yes** | Domain logic, MVC, native build |
| SDL3 / ImGui | Conceptual | Immediate-mode UI — widgets, not HTML |
| Lua / sol2 | Optional → recommended | Runtime panels, hotkeys, quick experiments |
| TypeScript + XHTML | Optional | Web-familiar UI authoring → native widgets |
| Python | Optional | pybind11 (desktop) · Chaquopy (Android) |
| Android / Djinni | Android only | JNI-free bridge, APK packaging |
| Kotlin Compose | Scaffold client only | `:app` wizard — not the generated app |

### Progression path (by persona)

| Step | Everyone | Game dev | C++ engineer | Web dev | Python analyst | Android dev |
|------|----------|----------|--------------|---------|----------------|-------------|
| 1 | Run generated template | ✓ plotter + Lua panels | ✓ CMake build | ✓ open UI files | ✓ run + edit Python | ✓ Gradle/APK |
| 2 | Tweak one visible behavior | Hotkey in `panels.lua` | New model field | Button in `ui.xhtml` | New function in `functions.py` | Trace Djinni bridge |
| 3 | Wire MVC end-to-end | Lua → controller call | Controller command | TS handler → C++ | C++ refresh from Python | Kotlin ↔ C++ eval |
| 4 | Extend authoring | Mix Lua + XHTML | Add ImPlot series | Full sidebar panel | numpy → ImPlot path | `android.*` Lua API |
| 5 | Blueprint workflow | Edit in Compose editor (v1) | Rewire modules | imnodes panel (v1.1) | Protect with `.dat` (roadmap) | Same shared MVC |

Full guide: [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

<details>
<summary><strong>When another stack may fit better</strong> (honest caveat)</summary>

| Situation | Consider instead |
|-----------|------------------|
| Web-only team, no appetite for C++/CMake | Electron or Tauri — faster ramp for HTML/CSS teams |
| Pixel-perfect marketing UI or design-system fidelity | Web or native UI toolkit with layout engines |
| iOS from this repo today | Not shipped yet — wait for v1 iOS template or use platform-native Swift |
| Greenfield safety-critical with compile-time memory proofs | Rust — see [Modern C++ in Nexus](#modern-c-in-nexus) below |

**Worth the Nexus ramp when:** you need native throughput, small binaries, SDL3 parity across desktop and Android field tablets, in-process Python/numpy, or blueprint-driven rewiring without a browser engine.

</details>

---

## Python: Desktop vs Android

The same `python.module` node in [`blueprint.json`](docs/templates/blueprint-schema.md) wires curve sampling on **both** templates — only the embed, packaging, and C++↔Python boundary change. Generated-project guides: [template/desktop-app/AGENTS.md](template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](template/android-app/AGENTS.md).

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Embedding** | pybind11 — CPython inside the native process | Chaquopy on the JVM; Djinni `ChaquopyPythonBridge` |
| **Source tree** | `python/` (e.g. `functions.py`) | `app/src/main/python/` |
| **Archive** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **None** — Gradle/Chaquopy bundle `.py` in the APK |
| **Runtime loader** | `PythonEngine` in `src/controller/` | `PlotterCore` → Djinni → Kotlin `ChaquopyPythonBridge` |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Typical rebuild** | `cmake --build` (refreshes `python.dat`) | `./gradlew :app:assembleDebug` |

```
blueprint.json  (python.module  →  port "evaluate")
        │
        ├─ Desktop ───────────────────────────────────────────┐
        │   python/functions.py                               │
        │        │  CMake: pack_python_dat                    │
        │        ▼                                            │
        │   misc/python.dat (PYAC)                            │
        │        │  PythonEngine (pybind11 embed)             │
        │        ▼                                            │
        │   PlotController → FunctionRegistry → ImPlot        │
        │                                                     │
        └─ Android ───────────────────────────────────────────┤
            app/src/main/python/functions.py                  │
                 │  Gradle + Chaquopy (no python.dat)         │
                 ▼                                            │
            ChaquopyPythonBridge (Djinni)                     │
                 ▼                                            │
            PlotController → FunctionRegistry → ImPlot  ◄─────┘
```

Both paths honor the same blueprint edge: Python `evaluate` → controller `sampleCache` → model caches → ImPlot draw. Desktop favors a single native binary with optional encrypted script packs; Android favors JVM-managed Python with type-safe JNI via Djinni. See [Desktop vs Android runtime](docs/assets/diagrams/desktop-vs-android-runtime.svg).

---

## TypeScript + XHTML DSL

Nexus exposes **two UI authoring layers** that lower to the same ImGui/Lua API — imperative Lua for quick panels, declarative TS/XHTML when a component mental model fits better. Neither path uses a browser engine.

### Imperative Lua (`panels.lua`)

Runtime panels register through sol2: `nxs.register_panel(...)` with `ui.button`, `ui.text`, `ui.separator`, and `nxs.register_hotkey`. This is the lowest layer — edit `scripts/panels.lua`, optionally repack `lua.dat`, hot-reload without recompiling C++.

```lua
nxs.register_panel("Quick add", function()
    if ui.button("sin(x)") then nxs.add_function("sine") end
end)
```

### Declarative TS/XHTML (`ui/`)

[`ui/ui.xhtml`](template/desktop-app/ui/ui.xhtml) + [`ui/ui.ts`](template/desktop-app/ui/ui.ts) describe the same sidebar and chart in markup and TypeScript. The toolchain lowers them into Lua panel definitions equivalent to `panels.lua` — **not** Node or a WebView.

| Mechanism | TS/XHTML | Lowers to |
|-----------|----------|-----------|
| `state()` in `ui.ts` | `bind="sampleCount"` on `<slider>` | Two-way ImGui widget state |
| `native()` in `ui.ts` | `items-source="activeCurves"` | Read-only C++ model projection (`FunctionRegistry`) |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Same `nxs.*` commands Lua calls directly |

### `ComponentTag` → native widgets

[`template/shared/dsl/tags.ts`](template/shared/dsl/tags.ts) maps every XHTML tag to a Dear ImGui, ImPlot, or imnodes draw call. [`components.ts`](template/shared/dsl/components.ts) provides typed classes per tag; [`core.ts`](template/shared/dsl/core.ts) defines the `Component` base, style props, and event callbacks the native runtime walks each frame.

| Tag (examples) | Native API |
|----------------|------------|
| `window`, `panel`, `button`, `slider`, `checkbox` | Dear ImGui |
| `plot`, `plot-line`, `plot-scatter`, `plot-bars` | ImPlot |
| `node-editor` | imnodes (`BeginNodeEditor`) |

Key widgets for the plotter sample: **Window**, **Panel**, **Button**, **Slider**, **Plot**, **PlotLine**. Future blueprint **imnodes** panel (v1.1) reuses the same `NodeEditor` tag on the same schema.

**Where to start:** [template/shared/dsl/](template/shared/dsl/) · sample markup [template/desktop-app/ui/ui.xhtml](template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

---

## Modern C++ in Nexus

Generated projects use **C++20** with conventions that address common legacy C++ pain points. Rust still wins on compile-time safety guarantees — this is an honest trade-off, not a language war.

| Topic | Nexus templates (C++20) | Rust (context) |
|-------|-------------------------|----------------|
| **Memory** | `shared_ptr` / RAII patterns; no raw owning pointers in template code; `.clang-format` enforced | Ownership + borrow checker — stronger static guarantees |
| **Concurrency** | `std::mutex`, atomics; `std::jthread` where threads are used | Fearless concurrency by default |
| **Tooling** | CMake presets (debug/release), Ninja, `compile_commands.json`, clang-format in every template | `cargo` — excellent, different ecosystem |
| **UI / media stack** | ImGui, SDL3, sol2, pybind11, ImPlot — mature, battle-tested | No direct ImGui-first equivalent; egui/wgpu paths differ |
| **Android NDK** | Djinni + SDL3 GLES — proven C++ on device | Possible via FFI, less turnkey for this stack |

**Rust is often the better default** for greenfield safety-critical services, async web backends, or teams already standardized on `cargo` and `#![deny(unsafe_code)]`.

**Modern C++ + Nexus fits** when you already depend on C++ libraries (CAD kernels, codecs, exchange APIs), need ImGui immediate-mode tooling UX, want pybind11/Chaquopy in-process Python, or must ship the same SDL3 stack on desktop and Android without rewriting in a new language.

---

## Architecture

![Nexus full stack — Compose client, generation pipeline, TS/XHTML + blueprint, Lua/sol2, C++ MVC on SDL3/ImGui/ImPlot, Python bridges, Djinni](docs/assets/diagrams/full-stack-architecture.svg)

![Generation and builds flow — client-setup → :app/:cli → builds/framework/&lt;name&gt; → native app](docs/assets/diagrams/generation-builds-flow.svg)

![Desktop vs Android runtime — shared MVC/ImGui/SDL3, pybind11 vs Chaquopy+Djinni](docs/assets/diagrams/desktop-vs-android-runtime.svg)

![Langflow vs n8n vs Nexus blueprint — design-time typed DAG vs runtime automation](docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

Layer reference: [docs/architecture/overview.md](docs/architecture/overview.md) · Python split: [Python: Desktop vs Android](#python-desktop-vs-android) · UI authoring: [TypeScript + XHTML DSL](#typescript--xhtml-dsl)

## Documentation

| Doc | Description |
|-----|-------------|
| [docs/README.md](docs/README.md) | Documentation hub |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes |
| [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) | `blueprint.json` — Langflow-style nodes vs n8n |
| [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md) | AI agent onboarding |
| [docs/architecture/risk-analysis.md](docs/architecture/risk-analysis.md) | Architecture risks |
| [AGENTS.md](AGENTS.md) | Build commands for coding assistants |
| [template/desktop-app/AGENTS.md](template/desktop-app/AGENTS.md) | Generated desktop plotter — pybind11, Lua, TS/XHTML |
| [template/android-app/AGENTS.md](template/android-app/AGENTS.md) | Generated Android plotter — Chaquopy, Djinni |

## Development status and limitations

**Shipped:** `:app` (Counter + Generate Project + Blueprint Editor), `:core` / `:cli` (template emit + `BlueprintValidator`), `template/*`, script archive packs (`lua.dat` / `python.dat` on desktop, `lua.dat` in Android APK), `builds/`, `misc/client-setup/`, `docs/`.

**Not yet:** full 6-step wizard, imnodes **native** panel (v1 ships Compose JSON graph editor), remote catalog, iOS template, SDL3 Android runner polish.

**Limitations (v1):** Compose Desktop scaffolder only; ImGui aesthetics are utilitarian; Chaquopy adds APK size on Android; no iOS from this toolchain today.

**Branch:** active development on **`main`** (`origin/main`).

---

## Beyond quick-fix automation: from flows to real applications

**Power Automate**, **n8n**, and similar workflow tools occupy a clear sweet spot: glue scripts, webhook chains, SaaS integrations, and one-off “quick fixes” that keep operations moving. They excel at ops automation — connecting Slack to Postgres, fanning out webhooks, scheduled ETL — without anyone shipping a product binary.

That strength becomes a limitation when the quick fix is supposed to *be* the product. Flow canvases offer no native UI/runtime beyond the vendor shell, weak packaging for offline or field use, and a cloud dependency that makes a deployable desktop or mobile binary an afterthought. Growing a flow into a multi-feature application usually means patching nodes forever: brittle, hard to test, and expensive to version like real software.

**Nexus** keeps the same **design-time** mental model — nodes and edges in [`blueprint.json`](docs/templates/blueprint-schema.md) (see [Langflow-style nodes vs n8n](#langflow-style-nodes-vs-n8n) above) — but the `:core` generator emits a **real native application**: C++/SDL3 windowing, Lua and Python logic layers, ImGui + TS/XHTML DSL pages, encrypted script packs (`lua.dat` / `python.dat`), and Android/desktop binaries from one scaffold. The graph is authored like Langflow; the artifact is compiled MVC on SDL3, not a server-side workflow engine.

**Migration path:** start where you already think — wire modules in the blueprint editor → generate with `:cli` or **Generate Project** → iterate in normal code layers (`cpp.model`, `python.module`, `ui.page`, Lua panels) instead of stacking flow patches. An n8n or Power Automate webhook can remain at the edge for ops glue while the app owns state, UI, and offline behavior in-process.

**Capabilities beyond flows:**

| Area | Flow tools (typical) | Nexus output |
|------|----------------------|--------------|
| **Runtime** | Server-side step engine, browser admin UI | Native desktop binary or Android APK |
| **Offline / field** | Requires connectivity to the workflow host | Offline-first SDL3 app; script packs in the bundle |
| **Performance** | HTTP round-trips between steps | Game-loop-friendly C++; in-process Python/numpy |
| **UI surface** | Vendor dashboard or none | ImGui + DSL pages; [Desmos-style plotter](docs/templates/desktop-app.md) sample |
| **Cross-platform** | Separate integrations per target | One [`blueprint.json`](docs/templates/blueprint-schema.md) wires [desktop + Android](docs/assets/diagrams/desktop-vs-android-runtime.svg) |
| **Authoring UX** | n8n / Power Automate canvas | Compose blueprint editor today; native **imnodes** panel (v1.1) on the same schema |

See the [full-stack architecture](docs/assets/diagrams/full-stack-architecture.svg) and [generation → builds flow](docs/assets/diagrams/generation-builds-flow.svg) for how the design-time graph becomes runtime code.

**Honest caveat:** Nexus is **not** a drop-in replacement for Power Automate or n8n when the problem is purely **cloud webhook orchestration** between SaaS APIs. Use those tools for integration glue; use Nexus when the quick-fix flow should graduate into shipped software — a native surface, testable modules, and room to grow beyond the next node patch.
