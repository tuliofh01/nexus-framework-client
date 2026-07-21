<!--
  description: The Nexus Framework 1.0.2 is a native app generator — blueprint graphs become C++20 / Lua / Python desktop and Android projects via a Compose Desktop client and Kotlin CLI. Optional Langflow export → flows.json import (not blueprint). SDL3, Zig sidecars, Nexus License (Nexus-1.0).
  keywords: native app generator, blueprint-driven development, C++20 modules, Compose Desktop, SDL3, Zig, Lua, Python, Dear ImGui, Android JNI, Kotlin Gradle, Langflow import, Nexus Framework, Nexus License
-->

# The Nexus Framework

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework logo" width="240" />
</p>

<p align="center"><strong>Sketch an app as a graph. Ship a native binary.</strong></p>

<p align="center">No browser shell. No Electron. No cloud runtime.</p>

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
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus License" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/ImGui-native_UI-green?style=flat-square" alt="Dear ImGui" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Version 1.0.2" /></a>
</p>

> **Zero to binary**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

---

## Table of contents

| # | Section | Covers |
|:--|:--------|:-------|
| 1 | [What is Nexus?](#what-is-nexus) | Pitch, who it's for |
| 2 | [Repository layout](#repository-layout) | Root `:core` / `:cli` / `:app` |
| 3 | [Compose Desktop client](#compose-desktop-client) | Home (= dashboard), editors |
| 4 | [Quick start](#quick-start) | Bootstrap → generate / import-langflow |
| 5 | [Generation pipeline](#generation-pipeline) | Blueprint + flows; Langflow → flows |
| 6 | [Templates & runtime](#templates--runtime) | Desktop / Android stack |
| 7 | [What's in 1.0.2](#whats-in-102) | This release |
| 8 | [Docs & license](#docs--resources) | Guides and ownership |

---

## What is Nexus?

A **native app generator**. You draw architecture as a blueprint graph; Nexus writes a real C++20 / Lua / Python project you can compile and own — desktop (SDL3) or Android (Zig JNI + Chaquopy).

| You want… | Nexus gives you… |
|:----------|:-----------------|
| Desktop / Android tools | Plotters, field tablets, instrument UIs |
| Less Electron tax | Small binaries, fast boot, low idle RAM |
| Languages in one process | C++ + Lua + Python (+ TS → ImGui) |
| Offline-first work | No telemetry, no cloud dependency |

**Not yet:** iOS, CSS-style marketing UIs (ImGui is the UI), pure-Python-only apps.

---

## Repository layout

Gradle modules sit at the **repo root**. One `build.gradle.kts` owns `:core`, `:cli`, and `:app`. Templates keep their own native/`build_app.sh` tooling — separate on purpose.

```
Nexus-Framework/
├── settings.gradle.kts      # include(":core", ":cli", ":app")
├── build.gradle.kts         # single build (JVM toolchain 26)
├── gradle/libs.versions.toml
├── core/                    # :core — ProjectGenerator, schemas
├── cli/                     # :cli  — `generate` + `import-langflow`
├── app/                     # :app  — Compose Desktop client
├── template/                # desktop-app + android-app scaffolds
├── misc/
│   ├── build_client.sh      # license accept + compile :core :cli :app
│   ├── build-logic/         # Gradle conventions
│   ├── client-setup/        # JDK 26 + Zig 0.16.0 bootstrap
│   ├── scripts/ · docker/ · jenkins/ · translations/
├── docs/
└── builds/                  # generated apps + packaged client
```

| Module | Role |
|:-------|:-----|
| `:core` | Generation pipeline (`nexus.opensource.framework.core.*`) |
| `:cli` | `generate` and `import-langflow` (Langflow export → `flows.json`) |
| `:app` | Compose Desktop wizard, editors, debugger |

`misc/` is tooling only — Kotlin sources for `:core` / `:cli` / `:app` live at the repo root (not under `misc/`).

---

## Compose Desktop client

After a short Loading splash, you land on **Home** — the main dashboard (flamingo mascot, generated apps, Create / Open / Langflow import / Analyze). `Welcome` and `Dashboard` remain only as navigation aliases that normalize to Home. Navigation lives in `app/.../App.kt`.

### Screens

| Screen | What it does |
|:-------|:-------------|
| Loading | Boot splash + animated flamingo |
| Home | Main dashboard: create, open generated apps, Langflow import, analyze, What's New |
| Generate | Name + type → `builds/framework/<name>/` |
| Blueprint editor | Skeleton graph + palette (`CUSTOMIZE` for imnodes) |
| Flows editor | Step list + canvas stub (`CUSTOMIZE` for full graph UI) |
| Debugger | Paste/scan logs, severity filters |
| Test runner | Client-side checks |
| What's New | First-welcome version overlay |

### Branding

Flamingo icon, load/transition overlays, window icon, and dark theme live in `FlamingoIcon.kt`, `FlamingoAnimation.kt`, `FlamingoWindowIcon.kt`, and `NexusTheme.kt`. Skeletons are intentional — look for `// CUSTOMIZE:` markers.

### Run the client

```bash
./misc/build_client.sh          # Nexus License accept (once) + compile :core :cli :app
                                # CI: ./misc/build_client.sh --accept-license
./gradlew :app:run
```

Deploy: `./gradlew :app:deployToBuildsClient` → `builds/client/app/`

---

## Quick start

```bash
# 1. Bootstrap once — JDK 26 + Zig 0.16.0
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh

# 2. Compile the Kotlin client / generator (prompts for Nexus License once)
./misc/build_client.sh
# non-interactive: ./misc/build_client.sh --accept-license

# 3. Launch Compose Desktop
./gradlew :app:run

# 4. Or generate from the CLI
./gradlew :cli:run --args="generate --type desktop --name MyApp"

# Optional: Langflow export JSON → flows.json stubs (not blueprint.json)
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MyApp/flows/flows.json"

# 5. Build the generated native app
cd builds/framework/MyApp && ./build_app.sh
```

Details: [misc/client-setup/README.md](misc/client-setup/README.md) · [misc/README.md](misc/README.md)

After editing templates, repack script archives:

```bash
./gradlew :core:packTemplateLuaDat :core:packTemplatePythonDat
```

---

## Generation pipeline

Blueprint in → project tree out. Same inputs → same tree (CI-friendly).

<p align="center">
  <a href="docs/assets/diagrams/activity-generate-pipeline.svg">
    <img src="docs/assets/diagrams/activity-generate-pipeline.svg" alt="UML activity — project generation pipeline" width="520" />
  </a>
</p>

<p align="center">
  <em>Project generation activity diagram</em> —
  <a href="docs/assets/diagrams/activity-generate-pipeline.svg">SVG</a> ·
  <a href="docs/assets/diagrams/activity-diagrams.md">full activity index</a>
</p>

1. **Author** — `blueprint.json` (modules/ports) + `flows.json` (automations). Optionally import a **Langflow-compatible export** into `flows.json` stubs via `import-langflow` / `LangflowTransformationEngine` — **flows only**, never `blueprint.json`.
2. **Generate** — `ProjectGenerator` copies/transforms `template/desktop-app` or `template/android-app` into `builds/framework/<name>/`.
3. **Implement** — domain logic in C++ / Lua / Python / TS as needed.
4. **Build** — `./build_app.sh` runs GCC (C++20 modules), Zig sidecars, Python venv, packaging.

**Langflow:** Nexus reads Langflow-style export JSON as a compatible format and emits disabled Nexus flow stubs for human review. Nexus does not bundle or run Langflow and is **unaffiliated** with the Langflow project. Do not expect Langflow → `blueprint.json`.

Schema: [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md)

---

## Templates & runtime

| Layer | Tech | Role |
|:------|:-----|:-----|
| Window / GPU | SDL3 | Desktop + Android |
| UI | Dear ImGui (+ ImPlot) | Immediate-mode native UI |
| Hot path | C++20 modules (`.cppm`) | App model + shared runtime |
| Scripting | Lua 5.4 + sol2 | Hot-reloadable panels |
| Analytics / ML | Python + pybind11 (Chaquopy on Android) | In-process |
| Sidecars | Zig 0.16.0 | Allocator / C-ABI / JNI — not C++ module compile |
| C++ compile | GCC 14+ via `build_app.sh` | Named modules |

Typical footprint from current templates: ~3–20 MB binary, low idle RAM, sub-second cold start for lean apps. Start with C++ + ImGui; add Lua/Python/flows when you need them — [coding-with-nexus.md](docs/guides/coding-with-nexus.md).

### Why native (short)

| Metric | Electron-class | Nexus native (templates) |
|:-------|:---------------|:-------------------------|
| Install size | 100s of MB | **~3–20 MB** |
| Cold start | Seconds | **Often &lt; 200 ms** |
| Idle RAM | 100s of MB | **Tens of MB** |
| Offline | Cache gymnastics | **Default** |

---

## What's in 1.0.2

**1.0.2** is the current client + generator release:

- **Root Gradle modules** — `:core` and `:cli` live at the repo root (not under `misc/`), with one consolidated `build.gradle.kts` and JVM toolchain **26**.
- **`misc/build_client.sh`** — one-shot compile for `:core` / `:cli` / `:app`, with a **Nexus License** accept dialog (stamp in `misc/.license-accepted`; `--accept-license` for CI).
- **Home dashboard** — animated flamingo mascot, generated-app grid, loading/transition overlays, window icon.
- **Editor skeletons** — Blueprint, Flows, and Debugger wired with `CUSTOMIZE` extension points.
- **Langflow → flows** — CLI `import-langflow` maps Langflow export JSON to `flows.json` stubs only (not blueprint generation).
- **UML activity diagrams** — framework + derived-app flows under [docs/assets/diagrams/](docs/assets/diagrams/activity-diagrams.md) (e.g. [activity-generate-pipeline.svg](docs/assets/diagrams/activity-generate-pipeline.svg)).
- **Nexus License (Nexus-1.0)** — non-commercial use with attribution; commercial toolkit / revenue / commercial-institution use needs owner authorization through **2041-07-21**.

Prior releases (v0.1–v1.0.1) covered Zig sidecars, JNI bridge, and template hardening — see git history if you need archaeology.

### Package map

```
# :app (Compose Desktop)
nexus.opensource
├── App.kt                          # screens + navigation (Home = dashboard)
└── framework/
    ├── controller/                 # Generate, Loading, Blueprint, Flows
    ├── model/                      # DebuggerService, TestRunner, RecentProjectsStore
    └── view/                       # Compose screens

# :core (separate Gradle module — not nested under :app)
nexus.opensource.framework.core
├── model/                          # ProjectSpec, NexusBranding, schemas
└── service/                        # ProjectGenerator, validators, Langflow → flows
```

---

## Docs & resources

| Doc | Covers |
|:----|:-------|
| [docs/hub.md](docs/hub.md) | Documentation hub |
| [docs/architecture/overview.md](docs/architecture/overview.md) | Architecture overview |
| [docs/assets/diagrams/activity-diagrams.md](docs/assets/diagrams/activity-diagrams.md) | UML activity diagrams |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | Coding in generated apps |
| [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) | Blueprint / flows reference |
| [AGENTS.md](AGENTS.md) | Commands for AI assistants |
| [misc/README.md](misc/README.md) | Tooling under `misc/` |

**Ecosystem:** SDL3 · Dear ImGui / ImPlot · sol2 · pybind11 · Chaquopy · Zig · optional Langflow-compatible export import (`flows.json` only; unaffiliated).

---

## License & ownership

This project uses the **[Nexus License](LICENSE)** (identifier: **Nexus-1.0**).

| Use | Allowed? |
|:----|:---------|
| Non-commercial use of the **Toolkit** and of **generated apps** (personal / hobby / non-commercial institutional) | **Yes** (with attribution) |
| **Commercial use of the Toolkit** (selling/redistributing the framework itself) | **Authorization required** through **2041-07-21** |
| Generated app that **produces revenue** (paid app, monetized SaaS, etc.) | **Authorization required** through **2041-07-21** |
| Generated app used in a **commercial institution** (company / for-profit workplace) | **Authorization required** through **2041-07-21** |
| Attribution when you generate or ship a derived app | **Required** (NOTICE / README / About credit) — continues after 2041 |
| Warranty / liability for bugs or misuse | **None** — provided “as is” |
| Responsibility for illegal acts by derived apps | **Yours alone** — the author is not responsible |

**Authorization window:** 2026-07-21 → **2041-07-21** (15 years). After that date, the authorization restrictions above expire unless a later license revision renews them; attribution and no-warranty terms remain.

**Owner / authorization:** [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) — project: [nexus-framework-client](https://github.com/tuliofh01/nexus-framework-client).

**Attribution (derived apps):** Generation writes a `NOTICE` file. Keep it (or an equivalent credit). Example:

> Built with [The Nexus Framework](https://github.com/tuliofh01/nexus-framework-client) — © Túlio Horta (@tuliofh01)

Third-party dependencies keep their own licenses.

Report issues: [github.com/tuliofh01/nexus-framework-client/issues](https://github.com/tuliofh01/nexus-framework-client/issues)

---

*Blueprint your app, generate the tree, ship the binary — then iterate in real code layers.*
