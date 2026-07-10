# Architecture Overview

Generated Nexus apps share one layered design. The scaffold client (`app/`) uses **MVC in Kotlin Compose**; generated C++ templates mirror it under `src/model/`, `src/controller/`, and `src/view/`.

## Full stack (single diagram)

![Nexus full stack — scaffold client, TS/XHTML + blueprint authoring, Lua/sol2 scripting, C++ MVC on SDL3/ImGui, Python bridges, Desktop vs Android targets](../assets/diagrams/full-stack-architecture.svg)

This diagram replaces the previous per-use-case flowcharts (trading desk, CAD, scientific viz, etc.). Those workloads all compose the same layers; only the domain code in `model/` and `controller/` changes.

| Layer | Technology | Role |
|-------|------------|------|
| **Scaffold client** | Kotlin Compose MVC | Wizard, imnodes editor, project generation |
| **Authoring** | TS/XHTML, `blueprint.json` | UI components and Langflow-style wiring |
| **Scripting** | Lua 5.4 + **sol2** | Runtime panels, hotkeys |
| **Domain** | C++20 MVC | Model, controller, ImGui/ImPlot view |
| **Rendering** | ImGui + ImPlot on **SDL3** | Desktop OpenGL, Android GLES |
| **Python** | pybind11 (desktop) / **Chaquopy** (Android) | numpy, analytics |
| **Android bridge** | **Djinni** | C++ ↔ Kotlin/JVM |

## App creation wizard

![Wizard flow — six steps from Compose client through template emit to build/run](../assets/diagrams/app-creation-wizard-flow.svg)

## Blueprint / imnodes

`blueprint.json` at the template root wires Python modules, MVC classes, UI pages, and Lua scripts. Re-open in the wizard to rewire without CMake edits.

## Themes and fonts

- Themes: `template/shared/themes/` — loaded by `NexusTheme::applyFromFile()`
- Nerd Font: `template/shared/assets/fonts/` — loaded by `FontConfig::loadNerdFont()`
- Logo: `template/shared/assets/nexus-logo.png` (also `docs/assets/nexus-logo.png`)

## Related

- [Coding with Nexus](../guides/coding-with-nexus.md)
- [Desktop template](../templates/desktop-app.md)
- [Android template](../templates/android-app.md)
