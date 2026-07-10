# AGENTS.md — Nexus Framework Client

Guide for AI coding assistants in **this** Kotlin/Compose Desktop client repo.

## What this repo is

Compose Desktop + Gradle scaffolder for The Nexus Framework. It does not run generated C++ apps — it is the client that will generate them.

| Module | Role |
|--------|------|
| `:app` | Compose Desktop client — **MVC** under `nexus.opensource` (`model/`, `view/`, `controller/`) |
| `docs/assets/diagrams/` | PlantUML-rendered architecture SVGs referenced from README |

Generated-app stack (see README): C++ + Lua (**sol2**) + TypeScript/XHTML + Python on **SDL3**; Android uses **Djinni**; wizard authors Langflow-style JSON flows via **imnodes**.

## Conventions

- **JDK / toolchain**: Java 26 via Foojay in `buildSrc` convention plugin (`kotlin-jvm.gradle.kts`).
- **Compose**: plugins from version catalog; `@Preview` in `view/` for IDE UI designer.
- **Window title**: `NexusBranding.windowTitle(...)` → `{projectName} - built with The Nexus Framework`.
- Dependencies only in `gradle/libs.versions.toml`. Repos: Maven Central, Google, Compose dev.

## Build / run

```bash
./gradlew :app:compileKotlin
./gradlew :app:run
./gradlew :app:test
```

## Where to edit

| Change | Location |
|--------|----------|
| Entry / window | `app/src/main/kotlin/nexus/opensource/App.kt` |
| Model | `app/src/main/kotlin/nexus/opensource/model/` |
| View + `@Preview` | `app/src/main/kotlin/nexus/opensource/view/` |
| Controller | `app/src/main/kotlin/nexus/opensource/controller/` |
| Product docs | `README.md`, `README.pt-BR.md` |
| Architecture SVGs | `docs/assets/diagrams/full-stack-architecture.svg`, `app-creation-wizard-flow.svg` |

## Docs

- EN: [README.md](README.md) · pt-BR: [README.pt-BR.md](README.pt-BR.md)
- Agent detail: [CLAUDE.md](CLAUDE.md)
