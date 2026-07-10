# Architecture Overview

Generated Nexus apps share one layered design. The scaffold client (`app/`) uses **MVC in Kotlin Compose**; generated C++ templates mirror it under `src/model/`, `src/controller/`, and `src/view/`.

## Full stack

![Nexus full stack — Compose client, generation pipeline, TS/XHTML + blueprint, Lua/sol2, C++ MVC on SDL3/ImGui, Python bridges, Desktop vs Android](../assets/diagrams/full-stack-architecture.svg)

This diagram replaces earlier per-use-case flowcharts. Trading desks, CAD viewers, and scientific tools all compose the same layers; only domain code in `model/` and `controller/` changes.

| Layer | Technology | Role |
|-------|------------|------|
| **Scaffold client** | Kotlin Compose MVC (`:app`) | Generate Project screen; v1 wizard + imnodes |
| **Generation** | `:core`, `:cli` in `misc/` | Template emit → `builds/framework/<name>/` |
| **Authoring** | TS/XHTML, `blueprint.json` | UI components and Langflow-style wiring |
| **Scripting** | Lua 5.4 + **sol2** | Runtime panels, hotkeys |
| **Domain** | C++20 MVC | Model, controller, ImGui/ImPlot view |
| **Rendering** | ImGui + ImPlot on **SDL3** | Desktop OpenGL, Android GLES |
| **Python** | pybind11 (desktop) / **Chaquopy** (Android) | numpy, analytics |
| **Android bridge** | **Djinni** | C++ ↔ Kotlin/JVM |

## Generation and builds

![Generation flow — client-setup through :app/:cli to builds/framework and native binary](../assets/diagrams/generation-builds-flow.svg)

## Desktop vs Android runtime

![Desktop vs Android — shared MVC/ImGui/SDL3; pybind11 vs Chaquopy + Djinni](../assets/diagrams/desktop-vs-android-runtime.svg)

## Blueprint / imnodes

`blueprint.json` at the template root wires Python modules, MVC classes, UI pages, and Lua scripts. Re-open in the wizard (v1) to rewire without CMake edits.

## Themes and fonts

- Themes: `template/shared/themes/` — loaded by `NexusTheme::applyFromFile()`
- Nerd Font: `template/shared/assets/fonts/` — loaded by `FontConfig::loadNerdFont()`
- Logo: `template/shared/assets/nexus-logo.png` (also `docs/assets/nexus-logo.png`)

## Related

- [Coding with Nexus](../guides/coding-with-nexus.md)
- [Desktop template](../templates/desktop-app.md)
- [Android template](../templates/android-app.md)
