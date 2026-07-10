# The Nexus Company's Framework For Native Applications Development

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="The Nexus Framework logo" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

This program is the Nexus Framework's Client. It scaffolds modern native applications in C++ for **Desktop** (Windows, macOS, Linux) and **Android**, using **SDL3** for cross-platform windowing, **sol2** for Lua scripting, **TypeScript + XHTML** for web-familiar UI authoring, and **Python** for embedded analytics. Android projects ship a **Djinni** C++↔Kotlin bridge and **Chaquopy** Python on the JVM side.

## What Nexus is built for

Nexus fits **native, data-heavy, and field-deployed tools** where you need performance and a small footprint without shipping Chromium or a WebView.

| Use case | Why Nexus | Template |
|----------|-----------|----------|
| **Trading / market-data desk** | Sub-ms UI refresh; C++ feed parsers; Python analytics in-process | Desktop |
| **CAD / mesh / point-cloud viewer** | SDL3 GPU viewport + ImGui tool chrome; geometry stays in C++ | Desktop |
| **Scientific visualization** | numpy via pybind11; large arrays in native memory; ImPlot charts | Desktop |
| **Game dev tools** (editors, profilers, pipelines) | Immediate-mode UI like engine debug overlays; Lua hot-reload | Desktop |
| **Audio / DSP workbench** | Low-latency C++ signal path; scriptable control surfaces | Desktop |
| **DevOps / infra monitor** | Lightweight always-on dashboard; single native binary | Desktop |
| **Android field tablet** (inspection, kiosk) | SDL3/GLES ImGui; Djinni bridge; Chaquopy Python | Android |
| **Robotics / teleop panel** | Touch-friendly ImGui; `android.*` Lua bindings | Android |
| **Embedded HMI / industrial panel** | Same SDL3 stack on desktop and Android | Both |

Templates: [docs/templates/desktop-app.md](docs/templates/desktop-app.md) · [docs/templates/android-app.md](docs/templates/android-app.md)

The flagship sample is a **Desmos-style function plotter** — Python (numpy) samples curves, C++ owns the model, ImGui + ImPlot draw the page.

## Learning curve

Nexus asks for more upfront skill than a web shell, but generated projects run on day one.

| Skill | Required? | Role |
|-------|-----------|------|
| **C++ / CMake** | Yes | Domain logic, MVC, build |
| **SDL3 / ImGui** | Conceptual | Immediate-mode UI — redraw every frame, no DOM |
| **Lua / sol2** | Optional → recommended | Runtime panels, hotkeys |
| **TypeScript + XHTML** | Optional | Web-familiar component authoring |
| **Python** | Optional | Desktop: pybind11 · Android: Chaquopy |
| **Android / Djinni** | Android only | JNI-free bridge, APK packaging |

**Recommended progression:** run the template → tweak MVC → add Python functions → script Lua panels → extend the TS/XHTML DSL → rewire `blueprint.json` in the imnodes editor.

**Honest take:** Electron and Tauri are gentler for web-only teams. Nexus is worth the ramp when native throughput, binary size, or Android field deployment matter more than HTML layout flexibility. See [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md).

## Architecture

### Full stack

One diagram covers the scaffold client, authoring layers, C++ MVC on SDL3, Python bridges, and Desktop vs Android targets:

![Nexus full stack architecture — TS/XHTML + blueprint.json authoring, Lua/sol2 scripting, C++ MVC on SDL3/ImGui/ImPlot, pybind11 or Chaquopy+Djinni Python, Compose wizard client](docs/assets/diagrams/full-stack-architecture.svg)

| Layer | Technology |
|-------|------------|
| **Authoring** | TypeScript + XHTML components, **imnodes** / `blueprint.json` (Langflow-style) |
| **Scripting** | Lua ImGui DSL via **sol2** |
| **Domain** | C++20 **MVC** — `model/`, `controller/`, `view/` |
| **Rendering** | Dear ImGui + ImPlot on **SDL3** (OpenGL desktop, GLES Android) |
| **Python** | **pybind11** (desktop) · **Chaquopy** + **Djinni** (Android) |
| **Scaffold client** | Kotlin **Compose Desktop** MVC (`app/`) |

### App creation wizard (v1 roadmap)

Planned flow — not implemented in the client yet:

![Wizard flow — Compose client, six-step wizard, imnodes blueprint editor, template emit, build and run](docs/assets/diagrams/app-creation-wizard-flow.svg)

More detail: [docs/architecture/overview.md](docs/architecture/overview.md) · [docs/README.md](docs/README.md)

## When to choose something else

| You need… | Better fit |
|-----------|------------|
| Rich HTML/CSS or a large existing React/Vue codebase | Electron or Tauri |
| **iOS** from the same toolchain today | Tauri 2 Mobile or native Swift/Kotlin |
| Pixel-perfect OS-native widgets (HIG menus, system file pickers) | Qt, .NET MAUI, platform-native UI |
| Designer-driven UI without code | Figma → web pipelines; ImGui is code-first |

**Nexus limitations (v1):** Compose Desktop scaffolder only; no iOS template; ImGui aesthetics are utilitarian; Chaquopy adds APK size on Android.

## What it does

| App type | Stack | Guide |
|----------|-------|-------|
| **Desktop App** | C++ + Lua (sol2) + TS/XHTML + Python + ImGui on **SDL3** | [docs/templates/desktop-app.md](docs/templates/desktop-app.md) |
| **Android App** | C++ + Lua + ImGui on **SDL3** + **Djinni** + **Chaquopy** | [docs/templates/android-app.md](docs/templates/android-app.md) |

Each project includes `nxs_config.json`, `blueprint.json`, shared **themes** (`nexus-dark`, `nexus-light`, `nexus-field`), optional **Nerd Font** icon setup, and the Nexus logo in `assets/`.

## Repository layout

```
Framework/
├── app/                 Compose Desktop client (`:app`) — MVC under `nexus.opensource/`
├── buildSrc/            Gradle convention plugins (JVM toolchain 26)
├── template/
│   ├── desktop-app/     Desktop output template (C++/CMake plotter sample)
│   ├── android-app/     Android output template (Gradle/Djinni/Chaquopy)
│   └── shared/          DSL, assets, themes, runtime (NexusTheme, FontConfig)
└── docs/                Documentation hub → docs/README.md
```

This is a single-module Gradle client — not the multi-module `:core` / `:cli` / `:client-desktop` layout used elsewhere.

## Prerequisites

- **JDK 26** (required by `buildSrc`; the Foojay resolver can auto-download it)
- Git
- **Generated Desktop** apps: CMake 3.24+, Ninja, C++20, Python 3.10+
- **Generated Android** apps: Android SDK, NDK, JDK 17+

## Quick start

Run the Compose Desktop client (counter MVC demo today):

```bash
./gradlew :app:run
```

Compile and test:

```bash
./gradlew :app:compileKotlin :app:test
```

Build the **desktop template** directly (templates are not emitted by the client yet):

```bash
cd template/desktop-app
cmake --preset debug && cmake --build --preset debug
```

See [template/README.md](template/README.md) for Android build notes.

## Development status

**In this repo today**

- `:app` — Compose Desktop MVC counter demo (`model/`, `view/`, `controller/`)
- `template/desktop-app`, `template/android-app`, `template/shared` — plotter samples, themes, DSL, runtime stubs
- `docs/` — hub, template pages, guides, two architecture SVGs

**v1 roadmap (client)**

- Home screen and 6-step creation wizard (see wizard diagram above)
- Template copy / emit from the client
- imnodes blueprint editor wired to generation

**Deferred**

- Remote template catalog, `python.dat` pack obfuscation, SDL3 Android runner polish, iOS template
