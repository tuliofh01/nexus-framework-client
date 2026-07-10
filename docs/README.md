# Nexus Framework — Documentation Hub

Documentation for the **Framework** scaffold client: Compose Desktop UI + Gradle generation pipeline for native C++/Lua/Python apps.

## Navigation

| Doc | What it covers |
|-----|----------------|
| [Architecture overview](architecture/overview.md) | Full-stack layers, generation flow, Desktop vs Android |
| [Agent readiness](architecture/agent-readiness.md) | AI agent onboarding score, gaps, fixes |
| [Risk analysis](architecture/risk-analysis.md) | Architecture risks and mitigations |
| [Desktop App template](templates/desktop-app.md) | `template/desktop-app/` — MVC, pybind11, plotter |
| [Android App template](templates/android-app.md) | `template/android-app/` — Djinni, Chaquopy |
| [Blueprint schema](templates/blueprint-schema.md) | `blueprint.json` — Langflow-style nodes, validation, vs n8n |
| [Shared DSL](templates/shared-dsl.md) | `template/shared/dsl/` — TypeScript + XHTML |
| [Coding with Nexus](guides/coding-with-nexus.md) | UI, MVC, Python, Lua, blueprint, themes, icons |
| [Generation pipeline](guides/generation-pipeline.md) | `ProjectGenerator`, CLI, Docker, Jenkins |

## Architecture diagrams

![Full stack architecture](assets/diagrams/full-stack-architecture.svg)

![Generation and builds flow](assets/diagrams/generation-builds-flow.svg)

![Desktop vs Android runtime](assets/diagrams/desktop-vs-android-runtime.svg)

## Related READMEs

| Path | Purpose |
|------|---------|
| [../README.md](../README.md) · [../README.pt-BR.md](../README.pt-BR.md) | Project overview (EN / pt-BR) |
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
