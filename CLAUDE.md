# CLAUDE.md

Guidance for Claude Code (claude.ai/code) working in this repository.

## Project Overview

The Nexus Framework Client — a Kotlin/JVM multi-module Gradle project with **Jetpack Compose (Compose Multiplatform Desktop)** UI, structured as **MVC**.

Generated native apps (documented in `README.md`) target C++ + Lua (**sol2**) + TypeScript/XHTML + Python on **SDL3**, with **Djinni** for Android C++↔Kotlin bridging and Langflow-style **imnodes** JSON flow templates. This repo is the Kotlin scaffolder/client, not those generated apps.

## Commands

- `./gradlew :app:run` — run the Compose Desktop client
- `./gradlew :app:build` / `./gradlew :app:compileKotlin` — build / compile `:app`
- `./gradlew build` / `./gradlew check` / `./gradlew clean`
- `./gradlew :utils:test` — module tests
- `./gradlew :utils:test --tests "nexus.opensource.utils.NexusBrandingTest"` — single test class

Tests use JUnit Platform (`kotlin("test")`).

## Architecture (MVC + Compose)

Subprojects in `settings.gradle.kts`:

| Module | Role |
|--------|------|
| `:app` | Compose Desktop executable. Main: `nexus.opensource.app.AppKt`. Depends on `:utils`. |
| `:utils` | Shared library (`NexusBranding`, kotlinx ecosystem). |

Package layout under `app/src/main/kotlin/nexus/opensource/app/`:

```
App.kt                 # main() → Compose Window (title via NexusBranding)
model/                 # immutable data (e.g. CounterModel)
view/                  # @Composable UI + @Preview (e.g. CounterView)
controller/            # mutable Compose state + actions (e.g. CounterController)
```

`utils/src/main/kotlin/nexus/opensource/utils/NexusBranding.kt` — `windowTitle(projectName)` → `{name} - built with The Nexus Framework`.

Compose plugins (version catalog `gradle/libs.versions.toml`): `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.compose` (1.11.1). Repos: Maven Central, `google()`, Compose dev Maven. Convention plugin `buildsrc.convention.kotlin-jvm` pins **JVM toolchain 26**.

## Conventions

- Dependencies only via `gradle/libs.versions.toml`.
- Build + configuration cache enabled — keep build logic cache-compatible.
- Kotlin 2.4.0.
- Docs: `README.md` (EN) ↔ `README.pt-BR.md` (pt-BR); architecture diagrams are SVGs in `docs/assets/diagrams/` (no Mermaid / no checked-in `.puml`).
