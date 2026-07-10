# The Nexus Company's Framework For Native Applications Development

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="The Nexus Framework logo" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

**The Nexus Framework** scaffolds native C++/Lua/Python applications for **Desktop** (Windows, macOS, Linux) and **Android** — SDL3 windowing, sol2 scripting, TypeScript + XHTML UI authoring, and embedded Python (pybind11 on desktop, Chaquopy + Djinni on Android). ImGui + ImPlot render immediate-mode UIs without a browser engine.

## What this repo is

| Today | Roadmap (v1) |
|-------|----------------|
| Compose Desktop client (`:app`) — Counter MVC demo + **Generate Project** screen | Home screen and 6-step creation wizard |
| Generation pipeline (`:core`, `:cli`) — copy/emit templates to `builds/framework/<name>/` | imnodes blueprint editor wired to generation |
| Bundled templates — desktop plotter, Android plotter, shared DSL/themes | Remote template catalog, `python.dat` pack, iOS template |

This is the **Framework** monorepo (`:app`, `:core`, `:cli`). It is not the separate [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) repo (`:client-desktop` wizard there).

## First run

Run one platform setup script, load the env file, then Gradle:

| Platform | Setup | Env |
|----------|-------|-----|
| Linux | `./client-setup/linux/setup.sh` | `source client-setup/env.sh` |
| macOS | `./client-setup/macos/setup.sh` | `source client-setup/env.sh` |
| Windows | `client-setup\windows\setup.bat` | `call client-setup\env.bat` |

Requires **JDK 26** and Git — see [client-setup/README.md](client-setup/README.md).

## Quick start

```bash
source client-setup/env.sh          # after first-run setup
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
│   ├── docker/          Optional containerized generation
│   ├── jenkins/         Optional Jenkins setup notes
│   └── scripts/         Repo automation (e.g. `generate-in-docker.sh`)
├── client-setup/        First-run JDK 26 + Git installers
├── builds/              Client → builds/client/ · apps → builds/framework/<name>/
├── template/
│   ├── desktop-app/     Desktop output (C++/CMake plotter)
│   ├── android-app/     Android output (Gradle/Djinni/Chaquopy)
│   └── shared/          DSL, assets, themes, runtime
├── docs/                Documentation hub → docs/README.md
└── Jenkinsfile          Optional pipeline entry
```

## Use cases

Nexus fits **native, data-heavy, and field-deployed tools** where throughput and binary size matter more than HTML layout.

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

## Learning curve

| Skill | Required? | Role |
|-------|-----------|------|
| C++ / CMake | Yes | Domain logic, MVC, build |
| SDL3 / ImGui | Conceptual | Immediate-mode UI — no DOM |
| Lua / sol2 | Optional → recommended | Runtime panels, hotkeys |
| TypeScript + XHTML | Optional | Web-familiar UI authoring |
| Python | Optional | pybind11 (desktop) · Chaquopy (Android) |
| Android / Djinni | Android only | JNI-free bridge, APK |

Progression: run template → tweak MVC → add Python → script Lua → extend TS/XHTML → edit `blueprint.json`. [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

## Architecture

![Nexus full stack — TS/XHTML + blueprint.json, Lua/sol2, C++ MVC on SDL3/ImGui/ImPlot, Python bridges, Compose client](docs/assets/diagrams/full-stack-architecture.svg)

![Wizard flow — planned 6-step Compose client (v1 roadmap)](docs/assets/diagrams/app-creation-wizard-flow.svg)

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

**Shipped:** `:app` (Counter + Generate Project), `:core` / `:cli` (template emit), `template/*`, `builds/`, `client-setup/`, `docs/`.

**Not yet:** full wizard UI, imnodes editor integration, remote catalog, iOS template, SDL3 Android runner polish.

**Limitations (v1):** Compose Desktop scaffolder only; ImGui aesthetics are utilitarian; Chaquopy adds APK size on Android; no iOS from this toolchain today.

**Branch:** active development on **`master`** (`origin/master`). If your clone defaults to `main`, run `git checkout master`.
