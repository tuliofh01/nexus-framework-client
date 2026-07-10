# AGENTS.md — Nexus Framework Client

Guide for AI coding assistants (Cursor, Claude Code, and similar) working in **this** Kotlin/Compose Desktop client repo.

## What this repo is

Compose Desktop + Gradle scaffolder for **The Nexus Framework**. It does not run generated C++ apps — it is the client that will generate them.

| Module | Role |
|--------|------|
| `:app` | Compose Desktop client — **MVC** under `nexus.opensource` (`model/`, `view/`, `controller/`) |
| `docs/assets/diagrams/` | PlantUML-rendered architecture SVGs referenced from README |

**Generated-app stack** (see README): C++ + Lua (**sol2**) + TypeScript/XHTML + Python on **SDL3**; Android uses **Djinni**; wizard authors Langflow-style JSON flows via **imnodes**.

## Build / run

```bash
./gradlew :app:run                    # run the Compose Desktop client
./gradlew :app:build                  # build :app
./gradlew :app:compileKotlin          # compile :app only
./gradlew build                       # full project build
./gradlew check                       # run all checks
./gradlew clean                       # clean build outputs
./gradlew :app:test                   # unit tests (JUnit Platform)
./gradlew :app:test --tests "nexus.opensource.model.NexusBrandingTest"  # single test class
```

## Architecture (MVC + Compose)

Single subproject `:app` in `settings.gradle.kts`.

Package layout under `app/src/main/kotlin/nexus/opensource/`:

```
App.kt                 # main() → Compose Window (title via NexusBranding)
model/                 # immutable data + shared types (e.g. CounterModel, NexusBranding)
view/                  # @Composable UI + @Preview (e.g. CounterView)
controller/            # mutable Compose state + actions (e.g. CounterController)
```

Compose plugins (version catalog `gradle/libs.versions.toml`): `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.compose` (1.11.1). Repos: Maven Central, `google()`, Compose dev Maven.

## Conventions

- **JDK / toolchain**: Java 26 via Foojay in `buildSrc` convention plugin (`kotlin-jvm.gradle.kts`).
- **Kotlin**: 2.4.0.
- **Compose**: plugins from version catalog; `@Preview` in `view/` for IDE UI designer.
- **Window title**: `NexusBranding.windowTitle(...)` → `{projectName} - built with The Nexus Framework`.
- Dependencies only in `gradle/libs.versions.toml`. Repos: Maven Central, Google, Compose dev.
- Build + configuration cache enabled — keep build logic cache-compatible.

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
- Hub: [docs/README.md](docs/README.md) — architecture, templates, guides
- Agent gaps: [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md) — doc/code mismatches and onboarding checklist
