<!--
  description: Nexus Framework generates native C++/Lua/Python desktop and Android apps from visual blueprints. No Electron, no cloud, no browser — just a compiled binary you control. SDL3 + ImGui + Zig 0.14 build system. 
  keywords: native app generator, C++ framework, C++20 modules, Lua scripting, Python embedded, SDL3, ImGui, Zig build, Zig JNI, Compose Desktop, blueprint-driven development, cross-platform desktop, Android native app, no Electron, project generator, graph-based architecture, arena allocator, pybind11, sol2, Chaquopy
-->
# The Nexus Framework — Native App Generator: C++ + Lua + Python from Visual Blueprints

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework — Native C++ Lua Python Project Generator" width="240" />
</p>

<p align="center"><strong>🧩 Sketch an app as a graph. Get a compiled native binary. No browser. No Electron. No cloud.</strong></p>

<p align="center"><em>Simple when you want it. Powerful when you need it.</em></p>

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
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache License 2.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3 Cross Platform" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.14-orange?style=flat-square&logo=zig" alt="Zig 0.14 Native Builds" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/ImGui-native_UI-green?style=flat-square" alt="Dear ImGui Native UI" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.1-blueviolet?style=flat-square" alt="Version 1.0.1" /></a>
</p>

> **🚀 Zero to binary in five minutes**  
> `zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh && ./gradlew :app:run`  
> No Chrome download. No Docker. No npm install. No cloud dependency.  
> Just a native SDL3 binary you control.

---

## Table of Contents

- [What is Nexus?](#what-is-nexus)
- [Why native matters](#why-native-matters)
- [What can you build with it?](#what-can-you-build-with-it)
- [Architecture overview](#architecture-overview)
- [Interface overview — mockups](#interface-overview--mockups)
- [The generation pipeline](#the-generation-pipeline)
- [Blueprint & flows](#blueprint--flows)
- [Building your app](#building-your-app)
- [Nexus vs the alternatives](#nexus-vs-the-alternatives)
- [How progressive enhancement works](#how-progressive-enhancement-works)
- [Performance & footprint](#performance--footprint)
- [Quick start](#quick-start)
- [The full workflow](#the-full-workflow)
- [Who is this for?](#who-is-this-for)
- [What makes it special](#what-makes-it-special)
- [The Zig story](#the-zig-story)
- [Community & contributions](#community--contributions)
- [The misc/ folder](#the-misc-folder)
- [Roadmap](#roadmap)
- [Docs & resources](#docs--resources)
- [Copyright & ownership model](#copyright--ownership-model)
- [See also](#see-also)

---

## What is Nexus?

**Nexus makes native app development as simple as sketching a diagram.** You draw your app's architecture as a graph — add a UI page, drop in a Python module, wire in some Lua scripting — and Nexus writes out a complete, buildable project. No hand-rolling build systems. No wiring language bridges. No fighting with CMake.

**It works for any app, simple or complex:**

- **Simple app** — A C++ counter with ImGui? Generate it in one command. The template gives you SDL3 windowing, input handling, and a working UI loop. Add your logic, build, ship. You don't need to understand half the stack to get a working binary.
- **Complex app** — Need Python analytics in-process? Lua panels that reload at runtime? TypeScript UI that lowers to native widgets? Add nodes to your blueprint, re-generate. Each layer stacks without breaking what came before. When you need the performance, it's there — 3 MB binary, 200 ms boot, zero-copy Python interop.

**This is the core idea: Nexus is simple enough that a single-node C++ app makes sense, and powerful enough that a 7-language polyglot app with runtime flows doesn't feel like a hack.** You don't adopt the whole stack. You start where you're comfortable and expand only when you need to. The generated project is always a normal, editable Zig/C++ tree — no framework lock-in at any stage.

**The result is a real native binary:**
- **3–20 MB** — no Chromium, no Node.js, no VM tax
- **Boots in under 200 ms** straight to an interactive ImGui frame
- **15–40 MB RAM at idle** — leaves headroom for your data
- **Works fully offline** — no telemetry, no cloud dependency
- **Cross-compilable** from Linux to Windows in one Zig command

**Nexus is not a workflow engine** (like n8n or Langflow). Those connect cloud APIs at runtime. Nexus generates a native desktop or Android binary from your blueprint — same visual graph paradigm, completely different output.

**Nexus is not an Electron alternative.** Electron puts a browser around your content. Nexus generates native code from a blueprint. One is a runtime; the other is a code generator.

---

## Why native matters

The software industry spent a decade convincing itself that shipping a browser is an acceptable way to deliver a desktop app. For chat clients and CRUD dashboards, that trade-off works. But a whole category of software needs more — and the web shell tax becomes a dealbreaker:

- **Trading terminals** that tick at 60 Hz and process market data in-process
- **Scientific instruments** where a 200 MB installer won't fit on the embedded target
- **Robotics control panels** that need direct serial port and GPU access
- **Field-deployed Android tablets** running ML inference offline
- **Data acquisition tools** that must boot and capture before the operator finishes walking to the machine

| What matters          | Web shell tax                  | Nexus native                     |
|----------------------:|:------------------------------|:---------------------------------|
| **Install size**       | 120–200 MB (Chromium)         | **3–20 MB** (SDL3 + your code)   |
| **Cold start**         | 2–8 seconds (renderer)        | **< 200 ms** (no sandbox)        |
| **RAM at idle**        | 150–500 MB                    | **15–40 MB**                     |
| **GPU access**         | WebGL (limited)               | **Vulkan / GLES / Metal native** |
| **File system**        | Sandboxed, async, mediated    | **POSIX / Win32 direct**         |
| **Offline**            | Cache-manifest dance          | **Always offline by default**    |
| **Build determinism**  | npm roulette                  | **Pinned toolchain, offline**    |
**Nexus exists for the apps where native performance is the requirement, not a nice-to-have.** If you're shipping an Electron wrapper around a web app and your users are happy, Nexus is not for you. If you're fighting Chromium's memory allocator on a sensor-processing app, read on.

---

## What can you build with it?

| Your goal...                                                   | What you get                                                                    |
|--------------------------------------------------------------:|:-------------------------------------------------------------------------------|
| **Plot waveforms with live Lua scripting**                     | C++20 data model + ImPlot canvas + Lua console + Python FFT — one binary        |
| **Field-deploy an Android tablet with ML inference**           | Touch SDL3/GLES + Chaquopy Python + Zig JNI bridge to native sensors            |
| **Build a configurable dashboard with hot reload**             | Blueprint nodes per panel + `flows.json` automations + Lua at runtime           |
| **Ship a desktop tool with web-style UI (but native)**         | XHTML + TypeScript that lowers to ImGui calls — no browser                      |
| **C++ perf + Python analysis in one process**                  | Both in-process — no IPC, no serialization, no numpy-copy overhead              |
| **Cross-compile Linux to Windows from CI**                     | `zig build -Dtarget=x86_64-windows` — no MSVC VM                                |
*This isn't aspirational — these are the existing templates in action.*

---

## Architecture overview

Nexus follows a three-layer architecture: you **author** a graph in a Compose Desktop client, the **generator** creates a native project tree, and the **runtime** executes it. Each layer is independently testable and replaceable.

### Layer 1: Authoring (Compose Desktop + CLI)

The `:app` module is a Compose Desktop UI for editing blueprint graphs and flow configurations — a visual canvas with drag-and-drop nodes, edge wiring, and a JSON inspector. The `:cli` module runs the same generation pipeline from the terminal, perfect for CI/CD and headless environments.

### Layer 2: Generation (`ProjectGenerator` in `:core`)

This is where the magic happens. The `ProjectGenerator` reads a `blueprint.json` graph, validates it against the v2 schema, and materializes template files into a complete project tree:

- Source files for every declared node type (C++, Python, Lua, TypeScript, XHTML)
- Build configuration (`build.zig` + `build.zig.zon`)
- Scripting bridges (pybind11 module definitions, sol2 bindings, Lua entry points)
- Platform-specific packaging (desktop installer config, Android Gradle wrapper)
- `nxs_config.json` — the project's identity and dependency manifest

The generation is **deterministic** — same blueprint + same template version = identical output tree, every time. This makes it CI-friendly and auditable.

### Layer 3: Runtime (SDL3 + ImGui + polyglot bridges)

The generated app runs as a native SDL3 process with:
- **Immediate-mode UI** via Dear ImGui and ImPlot — full redraw in < 0.5 ms, no framework overhead
- **Lua 5.4** via sol2 — scripting panels that can be edited and reloaded at runtime
- **Python 3.11+** via pybind11 (desktop) or Chaquopy (Android) — NumPy, scipy, ML models in-process
- **TypeScript + XHTML** — declarative UI that lowers to Lua/ImGui calls at build time

### Visual architecture diagram

The full architecture — from client through generation to runtime:

![Nexus Full-Stack Architecture — Compose Desktop Client to SDL3 Runtime](docs/assets/diagrams/full-stack-architecture.svg)

Desktop vs Android runtime — same blueprint, different Python bridge:

![Desktop vs Android Runtime — Shared MVC with pybind11 vs Chaquopy + Zig JNI](docs/assets/diagrams/desktop-vs-android-runtime.svg)

### Interface overview — mockups

The Compose Desktop client provides five main screens. Here's what each one looks like:

| Screen               | Mockup                                                   | What it does                                                               |
|---------------------:|:---------------------------------------------------------|:---------------------------------------------------------------------------|
| **Dashboard**        | ![Dashboard](docs/assets/examples/mockup-dashboard.svg)  | 5-card launchpad: Generate, Blueprint Editor, Flows, Debugger, Test Runner  |
| **Generate Project** | ![Generate](docs/assets/examples/mockup-generate-project.svg) | Project name, type selector, output path, Generate button                   |
| **Blueprint Editor** | ![Blueprint](docs/assets/examples/mockup-blueprint-editor.svg) | Visual DAG canvas — drag nodes, draw edges, inspector sidebar               |
| **Flows Editor**     | ![Flows](docs/assets/examples/mockup-flows-editor.svg)   | Flow definitions with enable/disable toggles, reload, preview               |
| **Debugger**         | ![Debugger](docs/assets/examples/mockup-debugger.svg)    | Pattern-based log scanner with live filtering and result history            |
### Polyglot design: 7 languages, 3 boundaries

Nexus doesn't force one language to do everything. Each language lives in its natural layer:

| Language         | Where                              | What it owns                                                |
|-----------------:|:------------------------------------|:------------------------------------------------------------|
| **Kotlin**       | `:app` / `:core` / `:cli`           | Compose Desktop UI + generation pipeline + CLI               |
| **C++20**        | `src/` in generated project         | Runtime MVC — RAII, `std::ranges`, `[[nodiscard]]`           |
| **Zig 0.14**     | `zig-services/` + `client-setup/`   | Build orchestration, cross-compilation, arena allocator      |
| **Lua 5.4**      | `scripts/` in generated project     | sol2 scripting — panels, hotkeys, runtime iteration          |
| **Python 3.11+** | `python/` in generated project      | pybind11 embedded NumPy / scipy analytics                    |
| **TypeScript**   | `ui/ui.ts`                          | Declarative UI bindings (lowers to Lua)                      |
| **XHTML**        | `ui/ui.xhtml`                       | XML UI markup (lowers to ImGui calls)                        |
The **generation boundary** is crossed by `ProjectGenerator` (Kotlin → native source trees).  
The **build boundary** is crossed by Zig (`build.zig` → compiled binary).  
The **runtime boundary** is crossed by sol2, pybind11, and Chaquopy (in-process language bridges).

### Cross-language bridge

All languages communicate in-process through the NexusBridge registry — no IPC, no serialization, no REST:

![Cross-Language Bridge — C++, Lua, Python, Zig, TypeScript in one process](docs/assets/diagrams/cross-language-bridge.svg)

### Development workflow

The typical edit-compile-debug cycle takes seconds, not minutes:

![Development Workflow — Blueprint to Generated App to Binary](docs/assets/diagrams/dev-workflow.svg)

---

## The generation pipeline

The generation pipeline is where your blueprint becomes code. It's a straightforward chain: **blueprint → validate → materialize → output**. Every run is deterministic — same graph + same templates = identical tree, every time.

![Generation & Builds Flow — From Gradle Modules to builds/framework Output](docs/assets/diagrams/generation-builds-flow.svg)

Generated project tree layout (output in `builds/framework/<name>/`):

```
builds/framework/MyApp/
├── build.zig              # Zig build orchestration
├── build.zig.zon          # Zig dependency manifest
├── nxs_config.json        # Project config (v2 schema)
├── src/                   # C++20 model + controller
├── python/                # Python analytics
├── scripts/               # Lua runtime panels
├── ui/                    # TypeScript + XHTML
└── flows/                 # Runtime automations
```

**Key design decisions:**
- **Templates are bundled, not fetched** — `misc/templates/` lives in the repo. No network at generation time.
- **Placeholders use `{{doubleCurly}}`** — consistent across all template files for all languages.
- **Generation is additive** — if a file already exists in the output directory, it's not overwritten. You can regenerate without losing your work.
- **Build files are generated** — `build.zig` and `build.zig.zon` come from the template, so the output tree is immediately buildable with zero configuration.
- **You never outgrow it** — use the blueprint for rapid iteration, then edit the generated project directly when you need freedom. The output is a normal Zig/C++ tree, not a framework runtime you're locked into.

---

## Blueprint & flows

Two JSON files, two concerns, one app. Nexus separates **what your app is** (blueprint — structure) from **what your app does** (flows — behavior).

### `blueprint.json` — the app's anatomy

A build-time graph at the project root. Nodes declare modules; edges declare data flow direction.

| Node type        | Runtime role                              | Source location                |
|-----------------:|:------------------------------------------|:-------------------------------|
| `python.module`  | In-process Python analytics, filtering    | `python/functions.py`          |
| `cpp.model`      | C++20 domain state with RAII              | `src/model/`                   |
| `cpp.controller` | Commands, event wiring, undo/redo         | `src/controller/`              |
| `ui.page`        | Declarative page layout (XHTML + TS)      | `ui/ui.xhtml` + `ui/ui.ts`     |
| `lua.script`     | Live-editable Lua panels and hotkeys      | `scripts/panels.lua`           |
Each node maps to a **generated source directory** and a **build target** — the blueprint is both an architecture diagram and a makefile.

The visual structure of blueprint vs flows:

![Blueprint vs Flows — Two-Layer JSON Model of Build-Time Structure and Runtime Behavior](docs/assets/diagrams/blueprint-vs-flows-layers.svg)

And the generated app's MVC architecture from the blueprint:

![Nexus Blueprint App Structure — Build-Time MVC Codegen from Node Graph](docs/assets/diagrams/nexus-blueprint-app-structure.svg)

**Sample:** [template/desktop-app/blueprint.json](template/desktop-app/blueprint.json)  
**Schema:** [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md)

### `flows.json` — the app's behavior

Optional runtime services that execute inside the app process:

| Mode         | Trigger             | Use case                               |
|-------------:|:--------------------|:---------------------------------------|
| `background` | Every N ms interval | Poll sensor, check queue depth          |
| `triggered`  | Event + condition   | React to data arrival, connection state |
| `startup`    | App launch          | Preload datasets, init hardware         |
| `manual`     | User action         | On-demand analysis pipeline             |
**Sample:** [template/desktop-app/flows/flows.json](template/desktop-app/flows/flows.json)  
**Schema:** [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) (flows section)

### Langflow import

[Langflow](https://github.com/langflow-ai/langflow) is supported as an **optional external authoring tool** — you can design AI flows in Langflow's visual editor and import them into Nexus. The `LangflowTransformationEngine` (Kotlin, 12 tests) handles:

- Component mapping (Langflow nodes → Nexus flow steps)
- Trigger inference (startup vs event vs interval)
- Topological sort and deduplication
- Schema validation

**Safety default:** every imported flow starts with `enabled: false`. You review and opt in.

![Langflow Export to flows.json — Adoption Workflow](docs/assets/diagrams/langflow-adoption-workflow.svg)

Reference diagrams for AI flow patterns:

- [RAG Chatbot](docs/assets/diagrams/langflow-rag-chatbot.svg) — map retrieval-augmented generation steps to blueprint node types
- [Agent with Tools](docs/assets/diagrams/langflow-agent-tools.svg) — agent loop maps to `python.module`, `cpp.controller`, and Lua panels

#### Practical walkthrough: Langflow → Nexus flow

1. **Design in Langflow** — create a chat/agent flow with Langflow's visual editor
2. **Export JSON** — Langflow's ReactFlow-compatible export contains `{ nodes, edges }`
3. **Import in Nexus client** — Flows Editor → **Import Langflow JSON**, select your file
4. **Review** — all imported flows are `enabled: false` by default; review steps, triggers, and conditions
5. **Enable** — toggle flows you want active; the generated app's `FlowRunner` executes them at runtime

The import maps Langflow component types to Nexus flow steps: `ChatInput` → `manual` trigger, `Schedule` → `interval` trigger, `Agent`/`LLM`/`Tool` → `invoke` steps. Unrecognized nodes are skipped with warnings.

---

## IDE & tools support

Nexus works with standard development tools — no proprietary IDE or plugin required:

| Tool               | Works with                              | Notes                                        |
|-------------------:|:----------------------------------------|:---------------------------------------------|
| **VS Code**        | C++, Lua, Python, TS, JSON, Zig         | Open the generated project root                |
| **CLion / IntelliJ** | Kotlin (`:app`), C++ (`src/`), Gradle | Import `build.gradle.kts` for the client       |
| **Zig LSP** (zls)  | `zig-services/`, `setup.zig`            | Auto-detects `build.zig` in project root       |
| **clangd**         | C++20 module-aware completion           | `.clang-format` + `compile_commands.json`      |
| **GitHub Copilot** | All languages                           | Context from project structure and existing    |
| **Langflow**       | External AI flow authoring              | Export JSON → import into Flows Editor         |
| **n8n**            | External workflow automation            | App calls n8n webhooks from Python/Lua         |
| **Docker**         | Containerized generation for CI         | `docker compose -f misc/docker/docker-compose.yml` |
| **Jenkins**        | CI/CD with parameterized builds         | `misc/jenkins/Jenkinsfile`                     |

### Debugging tools

| Tool           | Use                                                  |
|---------------|------------------------------------------------------|
| `lldb` / `gdb`| Native C++ debugging with debug symbols               |
| `adb logcat`  | Android log filtering by tag (SDL, Nexus, Python)     |
| Zig build     | `-Doptimize=Debug` for address sanitizer              |
| Compose client| Pattern-based Debugger panel with live log scanning   |

---

## Building your app

### Templates

| Template      | Stack                                  | When to choose                                  |
|--------------:|:---------------------------------------|:------------------------------------------------|
| `desktop-app` | SDL3 + ImGui + pybind11 + sol2 + TS    | You need a native desktop binary, multi-language |
| `android-app` | SDL3/GLES + Chaquopy + Zig JNI bridge  | You need the same app on Android tablets         |
Output goes to `builds/framework/<project-name>/`. See [builds/LAYOUT.md](builds/LAYOUT.md) for the layout.

### Understanding your generated project

When you run `generate`, the pipeline writes a complete, buildable project tree to `builds/framework/<name>/`. Here's what each piece does:

```
builds/framework/MyApp/
├── build.zig              # Zig build graph — compile C++, link SDL3, pack archives
├── build.zig.zon          # Zig dependency manifest — pinned tarballs, no network after vendor
├── nxs_config.json        # Project identity — name, version, template, dependencies (v2 schema)
├── src/                   # YOUR C++20 code — MVC from blueprint
│   ├── model/             #   domain state (AppModel.cppm)
│   ├── controller/        #   command routing (AppController.cppm)
│   └── view/              #   ImGui + ImPlot screens (AppView.cppm)
├── python/                # YOUR Python analytics (functions.py) — pybind11 in-process
├── scripts/               # YOUR Lua panels (panels.lua) — sol2 hot-reloadable
├── ui/                    # YOUR TypeScript + XHTML — declarative UI lowering to ImGui
│   ├── ui.xhtml           #   markup (sliders, plots, panels)
│   └── ui.ts              #   bindings (state(), on-click, items-source)
├── flows/                 # YOUR runtime automations (flows.json) — triggers + steps
├── zig-services/          # Zig build sidecar — c_abi/, jni/ (Android only)
└── shared/                # Runtime helpers — theme, fonts, script archive code
```

**Where to write your code:**

| You want to...                   | File to open                          |
|--------------------------------:|:--------------------------------------|
| Change the app's domain state    | `src/model/AppModel.cppm`             |
| Add a new UI screen              | `ui/ui.xhtml` + `ui/ui.ts`            |
| Write a Python analysis pipeline | `python/functions.py`                 |
| Add a Lua hotkey or panel        | `scripts/panels.lua`                  |
| Wire a new automation flow       | `flows/flows.json`                    |
| Add a C++ dependency             | `build.zig.zon` + `build.zig`         |
| Configure the project            | `nxs_config.json`                     |
**Build after every change:**

```bash
zig build                    # incremental — < 30s after first build
./build/MyApp                # run the binary (Linux/macOS)
```

The blueprint is consumed at **generation time** only. Once emitted, the generated tree is a normal C++/Zig project — you can edit files, add dependencies, and restructure freely. Regeneration is additive (doesn't overwrite existing files), so you can fix a mistake in the blueprint and re-run `generate` without losing custom code.

### Where to start, by persona

| You are...                         | Your entry point                                                     |
|----------------------------------:|:---------------------------------------------------------------------|
| **Just want a working app fast**   | Generate, write `src/`, `zig build` — binary in five minutes          |
| **Game dev** (ImGui comfortable)   | `scripts/panels.lua` — hotkeys, overlay panels, quick-add buttons     |
| **C++ backend engineer**           | `src/model/` + `src/controller/` — extend logic, generate UI          |
| **Web developer** exploring native | `ui/ui.xhtml` + `ui/ui.ts` — declarative markup, no browser           |
| **Python analyst** shipping a tool | `python/functions.py` — write logic, get an ImGui viewer for free     |
| **Android developer**              | Generate `android-app` — touch SDL3 UI with Zig JNI bridge            |
### Quick wins — minimal effort, real app

Nexus's templates ship with everything wired. Here's how little code you need to write for a working application:

| Goal                 | What you do                             | What you get                              |
|--------------------:|:----------------------------------------|:------------------------------------------|
| C++ + ImGui window   | Generate → edit model → `zig build`     | 3 MB binary with ImGui UI                  |
| Add a Lua panel      | Add `lua.script` node → re-generate     | Hot-reloadable console + panels            |
| Add Python analytics | Add `python.module` node → re-generate  | In-process NumPy, zero-copy C++ access     |
| Add a plot           | Already there — ImPlot ships by default | `ImPlot::PlotLine()` in your view code     |
**You never touch CMake, pybind11 build config, sol2 registration, or JNI boilerplate.** The templates own that complexity. You own your domain logic.

Full guide: [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)  
Coding styles: [docs/guides/coding-styles.md](docs/guides/coding-styles.md)

### Python: desktop vs Android

| Aspect          | Desktop (pybind11)                  | Android (Chaquopy)                   |
|----------------:|:-------------------------------------|:------------------------------------|
| **Bridge**      | CPython linked into native process   | Jython on JVM + Zig JNI bridge       |
| **Source tree** | `python/functions.py`                | `app/src/main/python/`               |
| **Archive**     | `python.dat` packed at build time    | Bundled in APK by Gradle             |
| **Rebuild**     | `zig build` re-packs `python.dat`    | `./gradlew :app:assembleDebug`       |
![Python Desktop vs Android Embedding Flow — pybind11 vs Chaquopy](docs/assets/diagrams/python-desktop-vs-android-flow.svg)

### TypeScript + XHTML UI

You get two UI authoring modes, neither uses a browser engine:

- **Imperative Lua** (`panels.lua`) — `nxs.register_panel(...)` with `ui.button()`, hotkeys, callbacks. Editable while the app runs.
- **Declarative TS/XHTML** (`ui/ui.xhtml` + `ui/ui.ts`) — markup and TypeScript that lower to Lua/ImGui calls at build time.

| TS/XHTML construct                     | Runtime equivalent                              |
|---------------------------------------:|:-------------------------------------------------|
| `bind="sampleCount"` on `<slider>`      | Two-way ImGui slider → C++ model state            |
| `items-source="activeCurves"` on `<listbox>` | Read-only projection of C++ data                  |
| `on-click="addPending"`                 | Same `nxs.*` command Lua calls directly           |
Start here: [template/desktop-app/ui/ui.xhtml](template/desktop-app/ui/ui.xhtml)

---

## Nexus vs the alternatives

### vs Electron, Tauri, Flutter

|                   | Electron             | Tauri                | Flutter              | **Nexus**                   |
|------------------:|:---------------------|:---------------------|:---------------------|:-----------------------------|
| **Runtime**        | Chromium + Node.js   | OS WebView + Rust    | Dart + Skia          | **C++20 + SDL3 native**      |
| **Binary size**    | 120–200 MB           | 5–15 MB              | 15–50 MB             | **3–20 MB**                  |
| **RAM at rest**    | 150–500 MB           | 50–100 MB            | 50–100 MB            | **15–40 MB**                 |
| **Cold boot**      | 2–8 s                | 1–2 s                | 1–2 s                | **< 200 ms**                 |
| **UI model**       | DOM / CSS / React    | HTML in WebView      | Widget tree          | **Immediate-mode ImGui**     |
| **Scripting**      | Node.js (same proc)  | Rust commands        | Dart isolates        | **Lua + Python in-process**  |
| **Platforms**      | Desktop              | Desktop + mobile WV  | Desktop + mobile     | **Desktop + Android SDL3**   |
| **Codegen**        | —                    | —                    | —                    | **Blueprint-driven**         |
| **Offline**        | Partial (cache API)  | Partial              | Full                 | **Full (always offline)**    |
| **SDK footprint**  | npm + node_modules   | Rust toolchain       | Flutter SDK + Dart   | **Zig 0.14 (~80 MB)**        |
**When Nexus wins:** sub-millisecond UI response, consistent codebase from trading terminal to Android field tablet, in-process Python for NumPy/CUDA without serialization, offline-first requirement, small binary requirement.

**When alternatives win:** your team is HTML/CSS-first, iOS is required from the same codebase, or you're building a traditional consumer app where ecosystem matters more than performance.

### vs n8n, Langflow, Power Automate

This is the most common confusion. **Nexus is NOT a workflow engine.** Here's the distinction:

|                    | n8n / Langflow                    | **Nexus**                                         |
|-------------------:|:----------------------------------|:-------------------------------------------------|
| **Output**          | Cloud automations, API workflows  | **Native desktop / Android binary**               |
| **Runtime**         | Node.js on a server               | **SDL3 + ImGui on your hardware**                 |
| **User interface**  | Web dashboard                     | **Compiled native UI**                            |
| **When it runs**    | As a hosted service               | **On your user's machine**                         |
| **Offline**         | Requires network                  | **Always offline by default**                      |
| **Graph approach**  | Connected steps at runtime        | **Build-time codegen + optional runtime flows**    |
n8n connects SaaS APIs. Langflow connects LLM chains. Nexus connects C++ models, Python modules, and Lua scripts into a compiled application. **If your flow IS the product** (not the orchestration behind it), Nexus is for you.

> **Can they complement each other?** Absolutely. A generated Nexus app can call n8n webhooks or Langflow endpoints from its Python and Lua layers.

### vs bare C++ / CMake from scratch

You can hand-roll SDL3 + ImGui + pybind11. Many of us have. Nexus exists because:

- **Blueprint iteration is cheaper than CMake refactoring** — changing a node type regenerates the build graph, no manual target editing
- **Cross-platform Zig builds** — one `build.zig` replaces platform-specific CMake presets (CMake fully removed from both templates)
- **Scripting bridges are pre-tested** — pybind11 and sol2 bindings come generated and tested
- **Deterministic generation** — CI can regenerate and diff the output tree

Nexus doesn't replace C++ — you still write your domain logic in C++20. It replaces the scaffolding you'd otherwise write for the tenth time.

---

## How progressive enhancement works

Nexus is designed so you don't have to adopt everything at once. Start with a single C++ node — the simplest possible app — and add Lua, Python, TypeScript, and flows only when you need them. **Each stage is optional. Each stage is one blueprint node and one re-generate away.**

### Stage 1: Just C++ on SDL3

Generate the project, get a bare ImGui window with SDL3. Write your C++20 domain logic in `src/model/` and `src/controller/`. This is the equivalent of hand-rolling ImGui + SDL3 — but with correct build files, cross-platform routing, and input handling already working.

You don't need Lua. You don't need Python. You don't need TypeScript. You get a functioning native app.

### Stage 2: Add Lua panels

Add a `lua.script` node to your blueprint. Re-generate. You now have a live Lua console and a panel system. Write `scripts/panels.lua` with hotkeys and quick-add buttons. Lua reloads without recompiling — great for iteration while the app is running against live data.

### Stage 3: Add Python analytics

Add a `python.module` node. Re-generate. pybind11 bindings are generated for your C++ model types. Write `python/functions.py` with NumPy pipelines. Your Python code runs in-process with zero-copy access to C++ state — no pickle, no serialization, no REST endpoint.

### Stage 4: Add TypeScript/XHTML UI

Add a `ui.page` node. Re-generate. Write UI structure in XHTML and logic in TypeScript. The build pipeline lowers both to Lua/ImGui calls — you get a declarative UI authoring experience that produces native widgets.

### Stage 5: Add flows

Add `flows.json` with background loops, event triggers, and scheduled tasks. The FlowRunner loads them at startup. Your app now has automations — polling sensors, reacting to events, preloading data.

### The progression visualized

![Desktop vs Android Runtime — Shared MVC with pybind11 vs Chaquopy + Zig JNI](docs/assets/diagrams/desktop-vs-android-runtime.svg)

Each stage adds capabilities without breaking what came before.
The blueprint graph encodes which stages are active.

This staged approach means your project's complexity grows with its requirements, not with the framework's marketing ambitions.

---

## Performance & footprint

Generated apps are lean because the toolchain is lean and there's no browser involved:

| Metric             | What to expect                                              |
|-------------------:|:------------------------------------------------------------|
| **Binary size**    | 3–20 MB (your code + SDL3 + ImGui + sol2 + pybind11)         |
| **RAM at idle**    | 15–40 MB                                                     |
| **RAM under load** | 50–150 MB (with embedded Python + NumPy)                     |
| **Cold start**     | < 200 ms to first ImGui frame                                |
| **UI refresh**     | Full redraw < 0.5 ms (immediate mode on GPU)                 |
| **Build (cold)**   | 15–60 s first build (Zig downloads + compiles deps)          |
| **Build (incr.)**  | < 10 s after changing a single `.cppm`/`.lua`/`.py` file     |
| **Cross-compile**  | `zig build -Dtarget=x86_64-windows` from Linux, no MSVC      |
| **Toolchain size** | ~80 MB (Zig 0.14.0) — CMake, NDK, Djinni all removed        |
| **Template size**  | ~2.5 MB (both templates + shared, legacy removed)            |

These aren't aspirational targets — they're measurements from the existing templates.

---

## Quick start

```bash
# 1. Bootstrap (once per machine) — installs JDK 26 + Zig 0.14.0
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh

# 2. Launch the Compose Desktop client
./gradlew :app:run

# 3. Generate a project from the CLI
./gradlew :cli:run --args="generate --type desktop --name MyApp"

# 4. Build the generated app
cd builds/framework/MyApp && zig build

# 5. Edit the blueprint, edit the flows, ship the binary
```

Your system Zig (any version) runs the bootstrap once, which pins a known-good **Zig 0.14.0** for all native builds. Full details: [misc/client-setup/README.md](misc/client-setup/README.md).

**Compile and test the generator:** `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

**Deploy the client:** `./gradlew :app:deployToBuildsClient` → [builds/client/app/](builds/client/app/)  
**Package installers:** `./gradlew :app:deployPackageToBuildsClient` → [builds/client/packages/](builds/client/packages/)

---

## The full workflow

![Nexus Full-Stack Architecture — Compose Desktop Client to SDL3 Runtime](docs/assets/diagrams/full-stack-architecture.svg)

1. **Sketch** — Define modules in `blueprint.json`. Add automations in `flows.json`. Use the Compose Desktop client or a text editor.
2. **Generate** — The Kotlin pipeline reads your graph and materializes a complete project tree into `builds/framework/<name>/`.
3. **Code** — Write your domain logic: C++ for performance, Python for analysis, Lua for scripting, TypeScript for UI structure.
4. **Build** — One command. Zig orchestrates C++ compilation, Lua bytecode, Python archives, and TypeScript lowering.
5. **Ship** — A native binary (~3-20 MB) or Android APK. Platform installers via Gradle deploy tasks. Always offline.

---

## Who is this for?

**You should use Nexus if:**

- You're building a **desktop or Android application** — from a simple data plotter to a multi-engine trading terminal
- You want **native performance and small footprint** without the Electron tax
- You value **simplicity** — one command generates a working project, one language (Zig) replaces four build tools
- You need **multiple languages in one process** — C++ for speed, Python for analysis, Lua for scripting, TypeScript for UI
- Your app must work **offline** — factory floor, boat, field, tablet without connectivity
- You're tired of **hand-rolling CMake + pybind11 + sol2** for the tenth time

**There's no "who should wait" here.** Nexus scales down to single-node C++ apps and up to polyglot pipelines. If you need a native binary, you're the target audience. The only genuine gaps:

| Not yet                     | Coming in                             |
|---------------------------:|:--------------------------------------|
| iOS support                 | v0.5+ (not on current roadmap)        |
| Pixel-perfect marketing UIs | ImGui improving but not CSS           |
| Pure-Python toolkit         | Nexus expects C++ at the core          |
| Beginner-friendly docs      | We're working on it — PRs welcome      |
---

## What makes it special

**Simple when you want it, powerful when you don't.** Nexus is the only framework where generating a single-node C++ app makes as much sense as a 7-language polyglot pipeline. The blueprint is optional the moment you outgrow it — the generated project is a normal Zig/C++ tree you can edit by hand, add dependencies to, and restructure freely. No lock-in, no framework runtime to ship.

**Blueprint-driven codegen.** Most frameworks generate from a single option or a configuration file. Nexus generates from a directed graph — your app's architecture is the input, not its settings. Change the graph, re-generate, get a different app.

**Graph-native app structure.** The same visual paradigm that made n8n and Langflow intuitive for automation now applies to application architecture. Your app is a graph of modules. Nexus makes that explicit, editable, and generative.

**Progressive language layers.** C++ for the hot path. Python for analysis. Lua for quick iteration. TypeScript for UI structure. Each language does what it's best at — no one-language-fits-all compromise. They communicate in-process through generated bindings, not over IPC or REST.

**Zig-native builds.** One tool (80 MB) replaces CMake + Ninja + NDK-build + Djinni CLI (10-12 GB). Offline-first dependency management. Cross-compilation as a first-class feature. CMake is fully removed from both templates — Zig handles C++ compilation, JNI bridges, and host tooling.

**Offline by design.** No cloud dependency, no telemetry, no runtime that phones home. Your binary works wherever your users are.

**Deterministic generation.** Same blueprint + same templates = identical output tree. CI can regenerate and diff. No hidden state, no network at generation time.

---

## Modern C++ at the core

Nexus templates are **C++20 from the ground up** — no C-with-classes, no raw pointers masquerading as ownership, no hidden header spaghetti. Every line in the generated app's `src/` and `runtime/` directories is written to modern C++20 conventions.

### C++20 modules (not headers)

The old `.h` + `.cpp` split is gone. Nexus templates use **module interface units** (`.cppm` files) — **22 of them** across the shared runtime and both app templates. Every shared runtime module (font config, theme, paths, script archive, crypto, protection, zig allocator) is now a **fully self-contained `.cppm` file** with no separate implementation `.cpp` — one file, one module, no split. Each module declares exactly what it exports and nothing leaks:

```
// ❌ Old way: #include "model.h" — drags in every transitive header
// ✅ Nexus way: import nxs.desktop.model;

export module nxs.desktop.model;   // declare module
export class AppModel { ... };     // only this is visible to importers
```

The global module fragment (everything before `export module`) is private — standard headers stay internal. Importers see only the public API. No macro leaks, no ODR violations, no circular includes.

### Self-contained modules with built-in documentation

Every shared runtime `.cppm` file includes a detailed educational header explaining **what the module does, why it exists, how to use it, and the C++20 concepts involved** — like a textbook sidebar alongside production code. These files replace what were previously `.hpp` + `.cpp` pairs plus a separate module implementation unit, all merged into one self-contained, documented file. Importers see only the public API; implementation details stay in the module's anonymous namespace, invisible and unreachable.

### Every modern C++20 idiom, in practice

The generated code reads like a living style guide:

| C++20 feature                              | Where Nexus uses it                                                    |
|-------------------------------------------:|:------------------------------------------------------------------------|
| **Modules** (`.cppm`)                      | All model, controller, view, service, runtime — 22 interface units       |
| **`[[nodiscard]]`**                        | Every getter, query, factory — compiler catches unused results           |
| **`constexpr`**                            | All trivial accessors, size queries, compile-time constants              |
| **`noexcept`**                             | Move constructors, swap, trivial setters — optimal vector reallocation   |
| **Trailing return types**                  | `auto counter() const -> int` — consistent syntax everywhere             |
| **`= default` / `= delete`**               | Model is value type, bridge classes non-copyable                         |
| **Brace initialization `{}`**              | Every member — zero-initializes scalars, prevents narrowing              |
| **Pass-by-value + `std::move`**            | Sink parameters — at most one copy, often zero                           |
| **`std::unique_ptr` with custom deleters** | RAII wrappers for SDL resources — automatic cleanup on scope exit        |
| **`std::optional`, `std::variant`**        | Flow runner state, polymorphic event payloads                            |
| **`std::ranges`**                          | Pipeline data transformations in controller logic                         |
### Example: RAII SDL3 resource management

```cpp
// Unique deleters enable unique_ptr for SDL resources
struct SdlWindowDeleter {
    void operator()(SDL_Window* w) const noexcept {
        if (w) SDL_DestroyWindow(w);
    }
};
using UniqueWindow = std::unique_ptr<SDL_Window, SdlWindowDeleter>;

auto window = UniqueWindow(
    SDL_CreateWindow("MyApp", 1280, 800, SDL_WINDOW_OPENGL));
// destructor fires automatically — no goto cleanup, no close_window label
```

### Blueprint-generated C++

The `blueprint.json` graph directly controls what C++ modules are generated:

| Blueprint node   | Generates                             | C++20 features involved                            |
|-----------------:|:--------------------------------------|:--------------------------------------------------|
| `cpp.model`      | `src/model/AppModel.cppm`             | `class`, `constexpr`, `[[nodiscard]]`, `noexcept`  |
| `cpp.controller` | `src/controller/AppController.cppm`   | Module imports, `std::function` callbacks          |
| `ui.page`        | Reference to view classes             | Modules importing model types for display          |
| `lua.script`     | sol2 panel registrations              | `std::function`, lambda captures, `auto`           |
### Why C++20 and not Rust

This question comes up often enough to address directly. Rust's memory safety guarantees are excellent, and Nexus evaluated it during early architecture work. Here's why C++20 won for the **generated app runtime**:

| Concern                            | C++20 in Nexus                                                       | Rust equivalent                                                      | Nexus's take                                                                                                    |
|-----------------------------------:|:---------------------------------------------------------------------|:---------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------|
| **Blueprint codegen**              | Kotlin writes `class`, `constexpr`, `auto` — simple substitution     | `pub struct`, `impl` blocks, lifetimes, derive macros                | C++ is simpler to generate correctly from non-Rust tooling                                                       |
| **In-process scripting**           | pybind11 + sol2 are mature — ~15 years of edge-case testing          | pyo3 + mlua exist but smaller ecosystems, more restrictive licensing | ~15 years of pybind11 production vs ~5 for pyo3                                                                  |
| **SDL3 / ImGui interop**           | C++ is SDL3/ImGui's native language — zero glue code                 | sys crate wrappers, CStr conversions, unsafe blocks everywhere       | C++ calls directly; Rust wraps in unsafe, eroding headline safety advantage                                      |
| **Android JNI**                    | `zig c++` compiles the same `.cppm` to ARM — no extra build step     | `cargo-ndk` + separate Rust toolchain + NDK compat shims             | One Zig binary serves both C++ compilation and Zig orchestration                                                  |
| **Team familiarity**               | C++ is baseline for SDL/ImGui systems programmers                    | Extra language requirement on top of already-polyglot stack           | Nexus already needs C++, Lua, Python, Zig, TS — Rust raises the bar without proportional benefit                 |
| **Determinism for generated code** | One obvious way to write most constructs — output is predictable      | Multiple valid patterns (owned/borrowed, dyn/impl, Arc/Rc)           | Generated code should be boring and predictable, not idiomatic and diverse                                        |
| **Safety posture**                 | RAII + `[[nodiscard]]` + `constexpr` + sanitizers + Zig's flags      | Borrow checker guarantees memory safety at compile time              | A fair loss — but Nexus apps are single-threaded ImGui loops, safety gap narrower in practice than on paper      |
**The honest summary:** Rust would have been a reasonable choice, but C++20 was the better fit for a **code-generated, multi-language, SDL3-native** project. The killer arguments were pybind11/sol2 maturity and the zero-glue SDL3 interop — C++ talks to ImGui and SDL3 natively, while Rust wraps them in unsafe blocks that erode its headline safety advantage. Nexus apps are also single-threaded ImGui loops where the borrow checker's hardest-won guarantees (data race prevention) provide less marginal benefit than in a concurrent server.

**If you prefer Rust for your own generated code:** Nexus generates C++20, but nothing stops you from adding Rust modules via FFI. The generated `build.zig` can vendor a Cargo workspace, and pybind11 modules can call Rust libraries through a C-compatible layer. Nexus picks a default to ship a coherent product — it doesn't lock you out of the ecosystem.

### Why C++20 and not an earlier standard

Nexus targets C++20 because that's the version where modules became practically usable in major compilers (GCC 14+, Clang 18+, MSVC 2022 17.5+). The Zig `zig c++` compiler (Clang-based) fully supports `.cppm` modules, so the generated code compiles the same way on every platform — no preprocessor hacks, no platform-specific `#ifdef` guards for module support.

**The result:** generated C++ code that would not look out of place in a CppCon talk. Students learn modern idioms by reading the output. Senior engineers find nothing to fix. And the Zig compiler enforces it all consistently across Windows, macOS, Linux, and Android.

---

## The Zig story

Nexus uses **Zig 0.14** as its native build orchestrator, C++ compiler driver, JNI bridge runtime, and arena allocator backend. Zig replaced four separate tools (CMake, Ninja, NDK-build, Djinni CLI) and eliminated ~11 GB of SDK weight. Here is what Zig concretely buys you:

### Performance

- **80 MB toolchain** replaces **10–12 GB** of SDKs (MSVC, NDK, g++, clang, Djinni CLI). `zig build` downloads and self-caches all dependencies as pinned tarballs — no system package manager required.
- **Arena allocator** (opt-in) replaces per-frame `malloc`/`free` for plot samples and frame buffers. The Zig `std.heap.ArenaAllocator` bumps a pointer on allocation and reclaims everything on reset — zero fragmentation, zero per-object free cost. Enabled by defining `NXS_ZIG_LINKED` in the build — C++ calls `nxs_alloc()` / `nxs_reset_arena()` through a C ABI.
- **Cross-compilation** is a one-flag operation: `zig build -Dtarget=x86_64-windows` from Linux produces a Windows binary. No cross-toolchain setup, no MSVC licenses.

### Ease of maintenance

- **One `build.zig`** replaces CMakeLists.txt + CMakePresets.json + NDK-build scripts + Djinni IDL. All compilation flags, dependency resolution, module scanning, and archive packing live in the same file.
- **C++20 modules** (`.cppm` files) need `-fmodules-ts` + `-fmodules-codegen` flags that `build.zig` sets uniformly — no per-target CMake `target_compile_features` or generator expressions.
- **Android JNI bridge** went from **7 C++ files** (classes, headers, virtual dispatch, shared_ptr) to **1 pure-Zig file** (`python_bridge.zig`) that exports 5 C ABI functions. The call chain collapsed from 5 layers (Kotlin → Djinni → C++ singleton → virtual dispatch → JNI) to 3 (Kotlin → Zig C ABI → JNI). No codegen regeneration step, no IDL compiler.
- **Offline dependencies**: `build.zig.zon` pins exact tarball digests. No `FetchContent` network calls on rebuild. `zig build --fetch` vendors everything once.

### Design simplification

- **Zig replaces the C++ bridge layer entirely**. Before, Python→C++→Zig required virtual dispatch through a C++ singleton (`PythonBridgeImpl`) with `std::shared_ptr` ownership. After, C++ calls Zig C ABI functions directly — `zig_python_greeting("MyApp")` returns a heap-allocated `const char*` through a plain `extern "C"` declaration. No constructors, no destructors, no reference counting.
- **No header explosion**: A C++ bridge file like `app_core.hpp` pulled in JNI headers, NDK types, and `shared_ptr`. The Zig replacement has zero non-standard `#include` directives — it accesses JNI through the `JavaVM*` pointer it stores at init time.
- **Unified build graph**: The same `build.zig` compiles C++20 modules, links system SDL3, fetches and builds ImGui from source, compiles Zig C ABI exports, and packs Lua/Python archives into `.dat` files. No `ExternalProject_Add`, no `add_custom_target`, no shell scripts.

### Where Zig runs

| Context             | What Zig does                                                       |
|-------------------:|:--------------------------------------------------------------------|
| **Build time** (host)  | Compiles C++20 modules, links deps, packs archives, runs smoke tests |
| **Desktop runtime**    | Arena allocator (opt-in frame allocation) via C ABI                  |
| **Android runtime**    | JNI bridge — stores `JavaVM*`, dispatches `greeting`/`evaluate`     |

### Concrete numbers

| Metric                    | Before (CMake + Djinni)     | After (Zig 0.14)            |
|--------------------------:|:----------------------------|:----------------------------|
| SDK size                  | ~10–12 GB                   | ~80 MB                      |
| Build tools               | 4 (CMake, Ninja, NDK, Djinni) | 1 (`zig build`)           |
| Android bridge files      | 7 C++, ~240 LOC             | 1 Zig, ~120 LOC             |
| Call chain layers         | 5                           | 3                           |
| Cross-compile setup       | Per-target toolchains       | `-Dtarget=x86_64-windows`   |
| Dependency freshness      | Network fetch per build     | Pinned tarball digest       |

---

## Project evolution

From its initial prototype to v1.0.1, Nexus evolved through five phases — each one removing a dependency, simplifying the stack, or hardening the templates. The Kotlin generator (`:core`) and the Compose Desktop client (`:app`) never needed restructuring; the evolution was entirely in the generated output and the docs.

### Phase 1 — Initial prototype (v0.1)

CMake-based build with 7× FetchContent dependencies. Djinni IDL for Android JNI bridges. Two separate `CMakeLists.txt` trees (desktop + Android). Each template had its own `CMakePresets.json`. The shared runtime was compiled separately for each target. Build required CMake 3.24+, Ninja, NDK-build, and the Djinni CLI — four tools, ~10-12 GB of SDK.

### Phase 2 — Zig sidecar (v0.2)

`zig-services/` directory added to both templates as a Zig build sidecar. C++ sources compiled via `zig c++`. ImGui/ImPlot/ImNodes fetched via `build.zig.zon` instead of CMake FetchContent. CMake remained the default — Zig was opt-in. The `pack_archive` host tool migrated from CMake to `zig build-exe`.

### Phase 3 — Zig default (v0.3)

Zig became the default build path for desktop. CMake demoted to fallback (`legacy-cmake-*` presets). Bootstrap unified into a single `misc/client-setup/setup.zig` (130 LOC) replacing 3 shell scripts per platform (~450 LOC). Android still used CMake + Djinni.

### Phase 4 — Zig JNI + CMake/Djinni removal (v1.0.0 → v1.0.1)

**The big cleanup.** Android switched from Djinni IDL codegen to hand-authored C++ JNI bridge files in `zig-services/jni/` — 3 files replacing 8 Djinni-generated stubs + the `regen-djinni.sh` script. CMakeLists.txt and CMakePresets.json deleted from both templates. All template source files converted from `.cpp` to `.cppm` (C++20 module interface units, 22 across shared runtime and both templates). The generator schema was updated: `nxs_config.json` dropped `djinni` and `presets` keys, gained `nativeBackend: "zig"`.

### Phase 5 — Industrialization + Template hardening (v1.0.1)

Mockup SVGs added for all client screens (dashboard, generate, blueprint editor, flows editor). Architecture diagrams regenerated with Catppuccin theme, CMake→Zig annotations, and corrected phase indicators. README overhaul: SEO metadata, persona-based quick-start table, "What makes it special" tagline, restructured sections. All legacy Djinni/CMake references purged from docs, configs, and template AGENTS files. Agent working artifacts (`.omo/`) removed. Toolchain metric updated: "CMake removed" not "no CMake needed". Template obsolete files (NEXUS-BRIDGE.md, plotter README, empty dirs) removed. Docs consolidated to 4 files + hub with cross-refs fixed.

**C++20 template modernization:** Every non-modular `.cpp` file in both templates was brought inline with modern C++20 idioms — trailing return types, `[[nodiscard]]` on every getter and factory, `std::string_view` for read-only parameters, `noexcept` on move/swap operations, `constexpr` on compile-time constants, `std::ranges::copy` replacing raw `std::memcpy` on byte vectors, and concept constraints (`streamable`) on template parameters. RAII `unique_ptr` deleters now manage SDL resources automatically — no manual `goto cleanup` labels, no resource leaks.

**Shared runtime module consolidation:** Non-user-editable infrastructure files (font config, themes, paths, script archive, crypto, script protection, zig allocator) were converted from `.hpp` + `.cpp` pairs plus separate module implementation units into single self-contained `.cppm` module interface units. Each includes detailed educational comments explaining architecture decisions, usage patterns, and C++20 concepts — the output doubles as a learning resource for developers reading the generated code. Build files updated to remove stale implementation references; the `pack_archive` standalone tool retains its legacy `.hpp`/`.cpp` pair since it predates the module build system.

### Where we are today

| Metric                | Before (v0.1)                               | After (v1.0.1)                                           |
|----------------------:|:---------------------------------------------|:----------------------------------------------------------|
| **Build tools**       | CMake + Ninja + NDK + Djinni (4 tools)       | `zig build` (1 tool)                                       |
| **Android JNI**       | 8 Djinni files + `regen-djinni.sh`           | 1 pure-Zig file — 5 C ABI exports                          |
| **Bootstrap**         | 3 shell scripts per OS (~450 LOC)            | 1 `setup.zig` (~130 LOC)                                   |
| **Toolchain weight**  | MSVC + NDK + g++ + clang (~10–12 GB)         | Zig 0.14.0 (~80 MB)                                        |
| **C++ source format** | `.cpp` + `.hpp` (traditional)                | `.cppm` — 22 C++20 module interface units                   |
| **C++20 idioms**      | Mixed C++98/17 across files                  | Uniform: `[[nodiscard]]`, trailing returns, `constexpr`     |
| **Shared runtime**    | `.hpp` + `.cpp` pairs + module impls         | Single self-contained `.cppm` per module + docs             |
| **Cross-compilation** | Not supported (needed MSVC)                  | `zig build -Dtarget=x86_64-windows`                        |
| **Dependencies**      | 7 FetchContent clones, network-dependent     | `.zon` pinned tarballs, offline                             |
| **Docs**              | Sparse, Djinni-focused, 10 files             | 4 docs + hub, cross-refs fixed, SVG diagrams                |
| **Template files**    | CMake + Djinni + backups (~30 KB)            | Purged obsolete files (~10 KB saved)                        |
| **SDL resource mgmt** | Manual `SDL_Destroy*` + `goto cleanup`       | RAII `unique_ptr` with custom deleters                      |
---

## Community & contributions

Nexus is Apache 2.0 licensed and built in the open.

**Where the active work is:**

| Area             | What it involves                                           | Skill level         |
|-----------------:|:------------------------------------------------------------|:--------------------|
| `:core` pipeline | Kotlin — generator, template engine, config v2 schema       | Intermediate Kotlin |
| `:app` client    | Compose Desktop — blueprint editor, flows editor, wizard    | Compose Desktop UI  |
| `template/`      | C++20, Zig, Lua, Python, TS — the generated app's runtime   | Your stack choice   |
| `zig-services/`      | Zig — build orchestration, JNI bridges, allocator modules                  | Zig                 |
| `misc/client-setup/` | Zig — bootstrap installer, env management                                  | Zig + packaging     |
| **Docs**             | Markdown — you're reading them                                             | Anyone              |
Report issues, open PRs, or start a discussion on the [GitHub repository](https://github.com/tuliofh01/nexus-framework-client).

---

## The misc/ folder

Framework repo tooling — none of this ships inside generated native apps.

| Path                                     | Contents                                                      |
|-----------------------------------------:|:---------------------------------------------------------------|
| [misc/core/](misc/core/)                 | Generator, template engine, config v2 schema                     |
| [misc/cli/](misc/cli/)                   | Headless `generate` CLI command                                  |
| [misc/build-logic/](misc/build-logic/)   | Gradle build — JVM toolchain 26, convention plugins              |
| [misc/client-setup/](misc/client-setup/) | First-run bootstrap — **Zig `setup.zig`**                        |
| [misc/scripts/](misc/scripts/)           | Dev helpers, smoke tests, diagram generators                     |
| [misc/docker/](misc/docker/)             | Containerized generation for CI                                  |
| [misc/jenkins/](misc/jenkins/)           | Optional Jenkins CI pipeline                                     |
| [misc/translations/](misc/translations/) | Localized READMEs                                                |
Hub: [misc/README.md](misc/README.md)

### Navigating `nexus.opensource.framework.*` — app package guide

The Compose Desktop client lives under `app/src/main/kotlin/nexus/opensource/framework/`. It follows MVC — controllers own state and logic, models hold data structures, views render UI. The generation pipeline (`:core`) and CLI (`:cli`) live in `misc/` and expose their public API under `nexus.opensource.framework.core.*`.

```
nexus.opensource.framework
├── controller/     State + orchestration (4 files)
├── model/          Data structures + services (2 files)
├── view/           Compose UI screens (7 files)
└── core/           (from :core module, under misc/core/)
    ├── model/      ProjectSpec, NexusBranding, NexusConfigSchema
    └── service/    ProjectGenerator, TemplateEngine
```

| Package                  | Role (MVC)                          | Key types                                                        | You use it when...                                         |
|------------------------:|:-------------------------------------|:-----------------------------------------------------------------|:------------------------------------------------------------|
| `framework.controller`   | Screen state + business logic         | `*Controller` classes — hold `mutableStateOf` properties          | Wiring a Compose screen                                       |
| `framework.model`        | In-memory debugging + testing         | `TestRunner`, `DebuggerService`                                   | Adding test cases, scanning runtime logs                      |
| `framework.view`         | Compose `@Composable` screens         | `*View` composables — one per controller                          | Building or modifying the UI                                  |
| `framework.core.model`   | Shared generation types               | `ProjectSpec`, `NexusBranding`, `NexusConfigSchema`               | Creating a spec for `ProjectGenerator.generate()`              |
| `framework.core.service` | Generator engine (no UI deps)         | `ProjectGenerator`, `TemplateEngine`                              | Generating a project tree from a blueprint                    |
**Typical import pattern in controller code:**

```kotlin
// From :app — same package, no special import needed
import nexus.opensource.framework.controller.GenerateController
import nexus.opensource.framework.model.TestRunner

// From :core (misc/core/) — project root wiring makes these resolve
import nexus.opensource.framework.core.model.ProjectSpec
import nexus.opensource.framework.core.model.NexusBranding
import nexus.opensource.framework.core.service.ProjectGenerator
```

The `:core` module lives at `misc/core/` but is wired into the project by `settings.gradle.kts` — you never need to know the directory; the package `nexus.opensource.framework.core.*` resolves automatically.

**Compile, test, and generate:**

```bash
./misc/scripts/dev/nexus-dev.sh compile        # :core + :cli + :app
./misc/scripts/dev/nexus-dev.sh test           # full check
./gradlew :cli:run --args="generate --type desktop --name MyApp"
./misc/scripts/test-gen/linux/generic.sh --project MyApp
```

**After changing template files**, repack the compressed Lua/Python blobs:

```bash
./gradlew :core:packTemplateLuaDat
./gradlew :core:packTemplatePythonDat
```

---

## Roadmap

**v1.0.x** — post-release stabilization and ecosystem:

- Structured error reporting (machine-readable JSON from `ProjectGenerator`, not log scraping)
- IDE integration support (language servers for generated Lua and Python layers)
- First-run experience polish (the Zig bootstrap is fast, but the flow could be friendlier)
- Additional generated app examples and starter blueprints

Full roadmap: **[misc/ROADMAP.md](misc/ROADMAP.md)**

---

## Docs & resources

| Doc | Covers |
|----:|:-------|
| [docs/hub.md](docs/hub.md) | Documentation hub |
| [docs/architecture/overview.md](docs/architecture/overview.md) | Architecture, language stack, pipeline, templates, risk |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes, deps |
| [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) | `blueprint.json` + `flows.json` reference |
| [AGENTS.md](AGENTS.md) | Build commands for AI assistants |
| [misc/ROADMAP.md](misc/ROADMAP.md) | v1.0.x roadmap |
### Ecosystem

| Technology | Role in Nexus |
|-----------:|:--------------|
| [SDL3](https://www.libsdl.org/) | Windowing, input, GPU — desktop + Android |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | Immediate-mode UI + scientific charts |
| [sol2](https://github.com/ThePhD/sol2) | Lua 5.4 bindings — in-process, hot-reloadable |
| [pybind11](https://pybind11.readthedocs.io/) | Python bindings — NumPy, scipy, ML in-process |
| [Chaquopy](https://chaquo.com/chaquopy/) | Python for Android via Zig JNI |
| [Langflow](https://github.com/langflow-ai/langflow) | Optional external AI flow authoring |
| [n8n](https://n8n.io/) | Optional external automation — webhooks |
---

## Copyright & ownership model

Understanding who owns what is straightforward with Nexus, but worth stating explicitly.

### The framework itself (this repo)

The Nexus Framework — the Kotlin generator, the Compose Desktop client, the CLI, the build logic, the scripts, and the documentation — is licensed under **Apache License 2.0**. You can:

- Use it commercially to build and ship applications
- Modify it for your internal needs
- Redistribute it with your own changes
- Sell products built with it

The only requirement: retain the copyright notice and the [LICENSE](LICENSE) file if you redistribute the framework itself (modified or not).

### What you generate (your app)

**The generated project tree — the source files, build configuration, and assets in `builds/framework/<name>/` — is YOUR code.** Nexus is a tool, not a publisher. You own the output completely.

You can:
- License your generated app under any terms you choose (proprietary, GPL, MIT, — your call)
- Sell it, ship it, embed it in proprietary hardware
- Modify the generated files without attribution to Nexus
- Remove or replace any template snippets

The one nuance: if you copy template files directly (not generated, but literally copied from `template/desktop-app/`), those specific files retain their Apache 2.0 headers. In practice, the generation pipeline handles this — the manifest in `nxs_config.json` tracks which templates were used and their licenses.

### What this means for your business

| Concern | Answer |
|--------:|:--------|
| Can I sell apps built with Nexus? | **Yes.** No royalties, no fees, no revenue sharing. |
| Do I need to open-source my app? | **No.** Apache 2.0 doesn't require derivative works to be open-source. |
| Do I need to credit Nexus in my UI? | **No.** No attribution requirement in generated code. |
| Can my legal team audit this? | **Yes.** Full license in [LICENSE](LICENSE) — 98 words of legible terms. |
| Third-party dependency licenses? | `build.zig.zon` + `nxs_config.json` list all. Review before shipping. |
### The copyright line

 2026 Nexus Framework contributors. The Nexus Framework Client, bundled templates, documentation, and build tooling are developed in the open by contributors. Each contributor retains copyright over their individual contributions under the Apache 2.0 terms.

---

## See also

- **[misc/ROADMAP.md](misc/ROADMAP.md)** — v1.0.x roadmap and remaining milestones
- **[docs/hub.md](docs/hub.md)** — full documentation hub
- **[AGENTS.md](AGENTS.md)** — condensed build commands for AI coding assistants
- **[The Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client)** — separate distribution for the Compose Desktop wizard
- **[API Integration Skills](https://github.com/tuliofh01/api-integration-skills)** — turnkey tutorials for Flask, FastAPI, chart.js, Vercel, Clerk, more

*Blueprint your app, generate the tree, ship the binary — then iterate in real code layers.*
