# The Nexus Company's Framework For Native Applications Development

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="The Nexus Framework logo" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

This program is the Nexus Framework's Client. It allows one to create, with relative ease, modern-looking applications,
mainly using C++. It was designed to replace Electron or Tauri solutions, creating robust template code for both
Desktop/Laptop Computers (cross-platform support from Windows, macOS, and Linux) and Android Smartphones (general devices).
Besides making use of native C++, Lua (ImGui DSL, bound through **sol2**), TypeScript and XHTML (a DSL abstraction that
depicts foreign components with a component model familiar to web developers) and Python (embedded scripts, providing
extended functionality and top performance) are also used. Generated apps render through **SDL3**, which guarantees the
same windowing/input/GPU backend works on desktop and Android, and ship a default set of C++ packages — **Djinni** among
them — that bridge the C++ core to the Android (Kotlin/JVM) side.

## Why should you adopt Nexus?
Nexus targets teams who want **UIs powerful and stylish enough to compete with modern front-end solutions** without the 
need to embed a full browser engine to your codebase.

|                   |             **Nexus**              | Electron                 | Tauri                 |
| ----------------- | :--------------------------------: | ------------------------ | --------------------- |
| UI runtime        | Native C++ + ImGui on SDL3 + Lua DSL (sol2) | Chromium + HTML/JS       | WebView + Rust shell  |
| Language focus    | C++20 + Lua + TypeScript/XHTML + Python | JavaScript/TypeScript    | Rust + frontend       |
| UI authoring      | TS/XHTML components + imnodes flow GUI (JSON templates) | HTML/CSS/JS | HTML/CSS/JS |
| Binary size       |       Smaller native binary        | Large (bundled Chromium) | Smaller than Electron |
| Android output    | Same wizard generates APK template | Separate tooling         | Mobile not primary    |
| Script protection |  Optional encrypted `lua.dat` v2   | N/A                      | N/A                   |


## Use cases in which The Nexus Framework performs better than Electron or Tauri
Nexus fits **native, data-heavy, or field-deployed tools** where a browser shell adds weight without
adding value. This program ships two templates — [Desktop App](docs/templates/desktop/usage.md) and 
[Android App](docs/templates/android/usage.md) — that share the same Lua ImGui DSL.

| Scenario                                                           | Why Nexus                                                                      | Template |
| ------------------------------------------------------------------ | ------------------------------------------------------------------------------ | -------- |
| **Trading / market-data desk**                                     | Sub-ms UI refresh, direct C++ feed parsers, Python for analytics scripts       | Desktop  |
| **CAD / mesh / point-cloud viewer**                                | OpenGL viewport + ImGui panels; no DOM layout thrash                           | Desktop  |
| **Game dev tools** (level editor, asset pipeline, profilers)       | Same immediate-mode patterns as engine debug UIs; Lua hot-reload via `lua.dat` | Desktop  |
| **Scientific visualization**                                       | numpy + matplotlib in-process (pybind11); large arrays stay in native memory   | Desktop  |
| **Audio / DSP workbench**                                          | Low-latency C++ signal path with scriptable control surfaces                   | Desktop  |
| **Android field tablet** (inspection, inventory, ruggedized kiosk) | Native SDL3/GLES ImGui + Djinni C++↔Kotlin bridge + Chaquopy Python; no WebView | Android  |
| **Robotics / teleop panel**                                        | Touch-friendly ImGui + `android.*` Lua bindings (sol2) for sensors/camera      | Android  |
| **DevOps / infra monitor**                                         | Lightweight always-on dashboard; smaller footprint than Electron tray apps     | Desktop  |

### Flagship architecture

Generated apps share one layered design, top to bottom:

1. **TypeScript + XHTML abstraction** — the authoring surface. XHTML markup declares the component tree and TypeScript drives its logic, giving web developers a familiar component model. These are *abstractions depicting foreign components*: each tag/component maps onto a native ImGui widget, so no browser engine is ever embedded.
2. **Visual flow authoring (Langflow-style)** — during app creation the wizard opens an **imnodes** node-graph GUI. Screens, data flows, and component wiring are crafted visually and saved as **JSON templates**, which the generator consumes the same way Langflow consumes its flow JSON. The JSON templates ship with the project and can be re-opened and re-edited later.
3. **Lua ImGui DSL via sol2** — the runtime scripting layer. TypeScript/XHTML components and JSON flow templates lower into Lua panel definitions; **sol2** (which replaces the previous Lua binding layer) exposes the C++ core to those scripts.
4. **C++ core on SDL3** — domain logic, rendering, and I/O. **SDL3** provides the window, input, and GPU backend on every target — it is what assures the same app works on Android as well as Windows/macOS/Linux, with no per-platform windowing code.
5. **Djinni bridge (Android)** — generated projects include a default set of C++ packages that bridge C++ to the platform; on Android, **Djinni** generates the interface code connecting the C++ core to Kotlin/JVM, replacing hand-written JNI.

![Layered architecture — TS/XHTML and imnodes JSON templates lower into the Lua ImGui DSL, bound to the C++ core via sol2, rendered through SDL3, bridged to Kotlin via Djinni](docs/assets/diagrams/layered-architecture.svg)

**Trading desk** — wire feeds stay in C++; Lua drives panels; Python runs ad-hoc models:

![Trading desk architecture — C++ feed parser and order book render through an SDL3/ImGui frame; TS/XHTML components define Lua panels via sol2; Python models attach via pybind11](docs/assets/diagrams/trading-desk.svg)

**Scientific visualization** — arrays never leave native address space:

![Scientific visualization architecture — C++ loads and filters data, numpy arrays via pybind11 feed matplotlib and SDL3-hosted ImGui plots, Lua layout scripts attach via sol2](docs/assets/diagrams/scientific-visualization.svg)

**CAD / 3D viewer** — retained geometry in C++, immediate-mode chrome in Lua:

![CAD viewer architecture — mesh loader feeds GPU buffers into an SDL3 GPU-context viewport; Lua tool palette via sol2 drives ImGui overlays; C++ ray picking updates selection](docs/assets/diagrams/cad-viewer.svg)

**Android field tablet** — one APK, native SDL3/GLES UI, Djinni-bridged C++ core, bundled Python:

![Android field tablet architecture — C++ core on SDL3 NDK, Djinni bridge to the Kotlin host, Lua UI via sol2, Chaquopy CPython, full-screen SDL3 GLES UI](docs/assets/diagrams/android-field-tablet.svg)

**Game tools pipeline** — editor UX matches in-engine debug overlays:

![Game tools pipeline architecture — asset watcher feeds C++ domain logic into an SDL3 scene view; Lua panel definitions via sol2 drive ImGui; lua.dat pack ships to artists](docs/assets/diagrams/game-tools-pipeline.svg)

More depth: [docs/templates/desktop/architecture.md](docs/templates/desktop/architecture.md) · [docs/templates/android/architecture.md](docs/templates/android/architecture.md) · [docs/overview/positioning-vs-tauri.md](docs/overview/positioning-vs-tauri.md)

## Performance snapshot

Cross-framework numbers vary by app complexity, OS, and measurement method. The table below mixes **measured** third-party benchmarks with **typical ranges** from industry write-ups and **Nexus estimates** derived from architecture (no bundled Chromium/WebView in generated apps).

| Metric                    | Nexus generated app _(estimate)_                                                                                                     | Tauri 2.x _(measured / typical)_                                                                                                                                                                                                                   | Electron _(measured / typical)_                                                                                                                                                                                                          | Native ImGui tool _(reference)_                                                                                                                                                                                     |
| ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Installer / bundle**    | ~3–20 MB (single binary + assets; grows with vendored `libs/`)                                                                       | **8.6 MB** measured vs **244 MB** Electron (6-window demo, macOS `.app`) [[1]](https://www.gethopp.app/blog/tauri-vs-electron) · typical **3–15 MB** [[2]](https://javascript-news.org/tauri-vs-electron-bundle-size-and-memory-footprint-in-2026) | **85–250 MB** typical minimum [[2]](https://javascript-news.org/tauri-vs-electron-bundle-size-and-memory-footprint-in-2026) · **80–150 MB** installers [[3]](https://blog.openreplay.com/comparing-electron-tauri-desktop-applications/) | ImGui library **~25k LOC**, integrates into your binary [[4]](https://news.ycombinator.com/item?id=24986908)                                                                                                        |
| **Idle RAM (1 window)**   | **~25–80 MB** _(estimate: SDL3 + ImGui + Lua VM via sol2; no browser process)_                                                                | **42 MB** idle single-window community bench [[5]](https://tech-insider.org/tauri-vs-electron-2026/) · **30–50 MB** typical [[3]](https://blog.openreplay.com/comparing-electron-tauri-desktop-applications/)                                      | **168 MB** idle single-window community bench [[5]](https://tech-insider.org/tauri-vs-electron-2026/) · **150–300 MB** typical [[3]](https://blog.openreplay.com/comparing-electron-tauri-desktop-applications/)                         | Varies by app; cross-platform ImGui+GLFW demos show **native-scale** RAM vs Electron in side-by-side OS task managers [[6]](https://anthonytietjen.blogspot.com/2025/03/cross-platform-desktop-c-gui-app-with.html) |
| **Idle RAM (6 windows)**  | Scales with view count; no multi-process Chromium                                                                                    | **172 MB** measured [[1]](https://www.gethopp.app/blog/tauri-vs-electron)                                                                                                                                                                          | **409 MB** measured [[1]](https://www.gethopp.app/blog/tauri-vs-electron)                                                                                                                                                                | —                                                                                                                                                                                                                   |
| **Cold startup**          | **~0.2–1 s** _(estimate: native process + GL context)_                                                                               | **380 ms** community bench [[5]](https://tech-insider.org/tauri-vs-electron-2026/) · **<500 ms** typical [[7]](https://www.raftlabs.com/blog/tauri-vs-electron-pros-cons/)                                                                         | **1.4 s** community bench [[5]](https://tech-insider.org/tauri-vs-electron-2026/) · **1–3 s** typical [[3]](https://blog.openreplay.com/comparing-electron-tauri-desktop-applications/)                                                  | Dominated by asset load; UI code often **<1 ms CPU/frame** for ImGui itself [[4]](https://news.ycombinator.com/item?id=24986908)                                                                                    |
| **UI CPU (steady state)** | Immediate-mode redraw; target **<1 ms** UI time per Dear ImGui guidance [[8]](https://ocornut-imgui.mintlify.app/guides/performance) | WebView + JS layout/paint                                                                                                                                                                                                                          | Chromium renderer + JS                                                                                                                                                                                                                   | Same ImGui targets                                                                                                                                                                                                  |

**How to read this:** Electron trades size and baseline RAM for **consistent HTML rendering everywhere**. Tauri reduces bundle and idle memory by using the **OS WebView** [[2]](https://javascript-news.org/tauri-vs-electron-bundle-size-and-memory-footprint-in-2026). Nexus generated apps skip both browser stacks: UI is **ImGui on SDL3 (OpenGL/Vulkan)**, scripted in Lua through sol2 and authored as TypeScript/XHTML components, with optional embedded Python — closer to game-engine tooling than to a web shell.

> **Honest caveat:** Community benchmarks (e.g. [[5]](https://tech-insider.org/tauri-vs-electron-2026/)) are useful for order-of-magnitude comparisons but are not independent lab tests. Always profile _your_ workload. PkgPulse's 2026 guide recommends benchmarking your own app before choosing on size alone [[9]](https://www.pkgpulse.com/guides/electron-vs-tauri-2026).

## When to choose something else

| You need…                                                                             | Better fit                                                                       |
| ------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------- |
| Rich HTML/CSS layout, web component libraries, or a large existing React/Vue codebase | **Electron** or **Tauri** — DOM/CSS is the right tool                            |
| Maximum sandboxing and memory safety in the shell language                            | **Tauri** (Rust) or a fully Rust UI stack                                        |
| **iOS** from the same mobile toolchain today                                          | **Tauri 2 Mobile** or native Swift/Kotlin                                        |
| Pixel-perfect system-native widgets (menus, file pickers that match OS HIG exactly)   | **Qt**, **.NET MAUI**, or platform-native UI                                     |
| Designer-driven visual UI without code                                                | **Figma → web** pipelines; ImGui is code-first                                   |
| Huge document-style UI (complex text flow, accessibility tree)                        | Web or retained-mode toolkit (Qt, WPF)                                           |
| Zero learning curve for web-only teams                                                | Electron/Tauri; Nexus's TS/XHTML layer softens the curve, but the toolchain still assumes comfort with **C++/CMake** and optional Lua/Python |

**Nexus limitations today (v1):** Compose Desktop scaffolder only (not the generated app runtime); pybind11 desktop Python pack deferred to v1.1; no iOS template; ImGui aesthetics are utilitarian; Chaquopy adds APK size and NDK complexity on Android. See [docs/deploy/DEPLOY_TODO.md](docs/deploy/DEPLOY_TODO.md).

## What it does

Generates ready-to-build projects from two templates:

| App type        | Stack                                                              | Guide                                                              |
| --------------- | ------------------------------------------------------------------ | ------------------------------------------------------------------ |
| **Desktop App** | C++ + Lua (sol2) + TypeScript/XHTML + Python + ImGui on **SDL3**                        | [docs/templates/desktop/usage.md](docs/templates/desktop/usage.md) |
| **Android App** | C++ + Lua (sol2) + ImGui on **SDL3** + **Djinni** C++↔Kotlin bridge + **Chaquopy Python** + `android.*` Lua bindings | [docs/templates/android/usage.md](docs/templates/android/usage.md) |

Each project includes `nxs_config.json`, vendored `libs/` (SDL3, sol2, Djinni support code among the default C++ packages), CMake presets, Docker/modern C++ dev files (optional), script archive protection (optional), the **JSON flow templates** authored in the imnodes node-graph GUI during creation (Langflow-style, re-editable later), and a **structural blueprint** (`docs/blueprint/` — YAML layout, PlantUML architecture, Mermaid data-flow, dashboard view plan).

## Repository layout

```
├── cli/                 Headless CLI (generate, catalog, debug validate)
├── core/                Shared generation logic + catalog JSON
├── client-desktop/      Primary UI — Compose Desktop (Linux, Windows, macOS)
├── templates/
│   ├── basic-app/       Desktop App output
│   └── android-app/     Android App output
└── docs/                Documentation hub → docs/README.md
```

> **Note:** The legacy `:client` Android APK wizard module has been removed. Use `:client-desktop` instead.

## Prerequisites

- **JDK 21** (required for Kotlin/Compose Desktop toolchain)
- Git
- For generated **Desktop** apps: CMake 3.20+, Ninja, C++20 compiler
- For generated **Android** apps: Android SDK, NDK, JDK 17+

## Desktop client quick start

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk   # if default java is not 21
./gradlew :client-desktop:run
```

Home screen → **Create Desktop App** or **Create Android App** → 6-step wizard → project opens in **Project Debug Workbench**.

## CLI quick start

```bash
./gradlew :cli:run --args="version"
./gradlew :cli:run --args="generate --type desktop --name MyApp --output ~/Projects"
./gradlew :cli:run --args="generate --type android --name MyAndroidApp --output ~/Projects"
./gradlew :cli:run --args="debug validate --all"
```

Full reference: [docs/client/cli.md](docs/client/cli.md)

## Development status

**v1 focus:** Compose Desktop client, shared `:core` generator, Desktop + Android templates with workbench validation, `android-api.json` manifest, Gradle `lua.dat` pack, script archive encryption (obfuscation).

**Deferred to v1.1:** pybind11 embed + `python.dat` pack for desktop, SDL3 Android runner polish, remote catalog.

See [docs/deploy/DEPLOY_TODO.md](docs/deploy/DEPLOY_TODO.md) for the full checklist.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
