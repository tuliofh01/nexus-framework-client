# Nexus Framework — Documentation Hub

Documentation for **The Nexus Framework** scaffold: the Compose Desktop client that generates native C++/Lua/Python apps.

| Doc | What it covers |
|-----|----------------|
| [Architecture overview](architecture/overview.md) | Full-stack + wizard diagrams, layers, themes |
| [Agent readiness](architecture/agent-readiness.md) | AI agent onboarding score (44/100), gaps, fixes |
| [Desktop App template](templates/desktop-app.md) | `template/desktop-app/` — MVC, pybind11, plotter |
| [Android App template](templates/android-app.md) | `template/android-app/` — Djinni, Chaquopy |
| [Shared DSL](templates/shared-dsl.md) | `template/shared/dsl/` — TS + XHTML |
| [Coding with Nexus](guides/coding-with-nexus.md) | UI, MVC, Python, Lua, blueprint, themes, icons |

## What Nexus is built for

Nexus targets **native, data-heavy, and field-deployed tools** — game-engine-grade performance without a browser engine.

| Use case | Why Nexus fits | Template |
|----------|----------------|----------|
| **Trading / market-data desk** | Sub-ms UI; C++ feed parsers; Python analytics in-process | [desktop-app](templates/desktop-app.md) |
| **CAD / mesh / point-cloud viewer** | SDL3 GPU viewport; retained geometry in C++ | [desktop-app](templates/desktop-app.md) |
| **Scientific visualization** | numpy in native memory via pybind11; ImPlot in-process | [desktop-app](templates/desktop-app.md) |
| **Game dev tools** | Immediate-mode UI like engine debug overlays; Lua hot-reload | [desktop-app](templates/desktop-app.md) |
| **Audio / DSP workbench** | Low-latency C++ signal path; scriptable ImGui controls | [desktop-app](templates/desktop-app.md) |
| **DevOps / infra monitor** | Lightweight always-on dashboard; single native binary | [desktop-app](templates/desktop-app.md) |
| **Android field tablet** | SDL3/GLES ImGui; Djinni; Chaquopy; no WebView | [android-app](templates/android-app.md) |
| **Robotics / teleop panel** | Touch ImGui; `android.*` Lua bindings | [android-app](templates/android-app.md) |
| **Embedded HMI** | Same SDL3 backend on desktop and Android | Both |

Flagship sample: **Desmos-style plotter** — Python samples curves, C++ owns data, ImGui draws, Lua/TS-XHTML scripts UI.

## Learning curve

| Skill | Required? | Used for |
|-------|-----------|----------|
| **C++ / CMake** | Yes | MVC, SDL3 main loop |
| **SDL3 / ImGui** | Conceptual | Immediate-mode UI (no DOM) |
| **Lua / sol2** | Optional → recommended | Runtime panels, hotkeys |
| **TypeScript + XHTML** | Optional | Web-familiar UI authoring |
| **Python** | Optional | pybind11 (desktop) / Chaquopy (Android) |
| **Android / Djinni** | Android only | JNI-free bridge, APK |

Progression: [Coding with Nexus — Learning progression](guides/coding-with-nexus.md#learning-progression).

## Architecture

![Full stack architecture](assets/diagrams/full-stack-architecture.svg)

![App creation wizard](assets/diagrams/app-creation-wizard-flow.svg)

Details: [architecture/overview.md](architecture/overview.md)

## Repository map

```
Framework/
├── app/                 Compose Desktop client (MVC: model / view / controller)
├── template/
│   ├── desktop-app/     Desktop output template
│   ├── android-app/     Android output template
│   └── shared/          DSL, assets, themes, runtime
└── docs/                This hub
```

## Quick links

- [README](../README.md) · [README.pt-BR](../README.pt-BR.md)
- `./gradlew :app:run`
- [template/README.md](../template/README.md)
