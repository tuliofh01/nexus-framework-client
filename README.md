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

## What this repo is

| Today | Roadmap (v1) |
|-------|----------------|
| Compose Desktop client (`:app`) — Counter MVC demo + **Generate Project** screen | Home screen and 6-step creation wizard |
| Generation pipeline (`:core`, `:cli` in `misc/`) — emit templates to `builds/framework/<name>/` | imnodes blueprint editor wired to generation |
| Bundled templates — desktop plotter, Android plotter, shared DSL/themes | Remote template catalog, `python.dat` / `lua.dat` packs, iOS template |

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
├── buildSrc/            Gradle convention plugins (JVM toolchain 26) — **must stay at repo root**
├── misc/
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
| **`python.dat` / `lua.dat` protection** | Roadmap encrypted packs ship logic without loose `.py`/`.lua` on disk | Not a first-class concern in typical Electron/Tauri asset models |
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
| 5 | Blueprint workflow | Edit `blueprint.json` | Rewire modules | Re-open in wizard (v1) | Protect with `.dat` (roadmap) | Same shared MVC |

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

Layer reference: [docs/architecture/overview.md](docs/architecture/overview.md)

## Documentation

| Doc | Description |
|-----|-------------|
| [docs/README.md](docs/README.md) | Documentation hub |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes |
| [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md) | AI agent onboarding |
| [docs/architecture/risk-analysis.md](docs/architecture/risk-analysis.md) | Architecture risks |
| [AGENTS.md](AGENTS.md) | Build commands for coding assistants |

## Development status and limitations

**Shipped:** `:app` (Counter + Generate Project), `:core` / `:cli` (template emit), `template/*`, `builds/`, `misc/client-setup/`, `docs/`.

**Not yet:** full wizard UI, imnodes editor integration, remote catalog, iOS template, SDL3 Android runner polish, `python.dat` / `lua.dat` packs.

**Limitations (v1):** Compose Desktop scaffolder only; ImGui aesthetics are utilitarian; Chaquopy adds APK size on Android; no iOS from this toolchain today.

**Branch:** active development on **`master`** (`origin/master`). If your clone defaults to `main`, run `git checkout master`.
