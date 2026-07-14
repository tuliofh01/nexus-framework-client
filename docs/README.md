# Nexus Framework — Documentation Hub

Documentation for the **Framework** scaffold client: Compose Desktop UI + Gradle generation pipeline for native C++/Lua/Python apps.

## Navigation

| Doc | What it covers |
|-----|----------------|
| [Architecture overview](architecture/overview.md) | Full-stack layers, language stack, risk analysis, 10 architecture diagrams |
| [Agent readiness](architecture/agent-readiness.md) | AI agent onboarding score, gaps, fixes |
| [Zig patching](architecture/zig-patching.md) | Phased Zig native-build orchestration + Langflow importer |
| [Desktop App template](templates/desktop-app.md) | `template/desktop-app/` — MVC, pybind11, plotter |
| [Android App template](templates/android-app.md) | `template/android-app/` — Zig JNI (Djinni retired), Chaquopy |
| [Blueprint schema](templates/blueprint-schema.md) | `blueprint.json` + `flows.json` — nodes, validation, runtime automations |
| [Coding with Nexus](guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes, XHTML DSL, adding dependencies |
| [Coding styles](guides/coding-styles.md) | C++20, Zig, Kotlin, Lua, TS, Python — authoritative rules |
| [Generation pipeline](guides/generation-pipeline.md) | `ProjectGenerator`, CLI, Docker, Jenkins |

## Architecture diagrams

![Full stack architecture](assets/diagrams/full-stack-architecture.svg)

![Generation and builds flow](assets/diagrams/generation-builds-flow.svg)

![Desktop vs Android runtime](assets/diagrams/desktop-vs-android-runtime.svg)

## Related READMEs

| Path | Purpose |
|------|---------|
| [../README.md](../README.md) · [../misc/translations/README.pt-BR.md](../misc/translations/README.pt-BR.md) | Project overview (EN / pt-BR) |
| [../misc/client-setup/README.md](../misc/client-setup/README.md) | First-run JDK 26 + Git setup |
| [../builds/README.md](../builds/README.md) | `builds/client/` and `builds/framework/` layout |
| [../template/README.md](../template/README.md) | Output templates index |
| [../misc/README.md](../misc/README.md) | Generation pipeline modules, Docker, Jenkins, scripts |
| [../AGENTS.md](../AGENTS.md) | Build commands for coding assistants |

## Quick commands

```bash
source ../misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
```
