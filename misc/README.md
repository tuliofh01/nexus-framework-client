# misc/

Supporting modules and tooling for the Framework scaffold client. Gradle modules `:core` and `:cli` live here; `:app` stays at the repo root.

## Layout

| Path | Role |
|------|------|
| [core/](core/) | Generation pipeline (`:core`) — `ProjectGenerator`, `TemplateEngine`, `nxs_config` schema |
| [cli/](cli/) | Headless CLI (`:cli`) — `generate` command |
| [docker/](docker/) | `Dockerfile` + `docker-compose.yml` for containerized generation |
| [jenkins/](jenkins/) | Optional Jenkins setup — see [jenkins/README.md](jenkins/README.md) |
| [scripts/](scripts/) | Helper scripts — e.g. `generate-in-docker.sh` |

Root [Jenkinsfile](../Jenkinsfile) points at this repo for optional CI.

Gradle still exposes `:core` and `:cli` at the project root via `settings.gradle.kts`:

```kotlin
project(":core").projectDir = file("misc/core")
project(":cli").projectDir = file("misc/cli")
```

## Why `buildSrc/` stays at the repository root

Gradle requires `buildSrc/` at the **repository root** for convention plugins. Moving it under `misc/` would break plugin discovery and the shared JVM/Compose build logic used by `:core`, `:cli`, and `:app`.

Keep `buildSrc/` next to `settings.gradle.kts` and `gradlew`. Only the generation pipeline, CLI, Docker, Jenkins, and script helpers live here under `misc/`.

## Common commands

```bash
./gradlew :core:compileKotlin
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp
```

Docs: [docs/guides/generation-pipeline.md](../docs/guides/generation-pipeline.md) · [../AGENTS.md](../AGENTS.md)
