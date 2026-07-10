# misc/

Supporting modules and tooling for the Framework scaffold client. Gradle modules `:core` and `:cli` live here; `:app` stays at the repo root.

## Layout

| Path | Role |
|------|------|
| [build-logic/](build-logic/) | Gradle convention plugins (included build) — JVM toolchain 26 |
| [core/](core/) | Generation pipeline (`:core`) — `ProjectGenerator`, `TemplateEngine`, `nxs_config` schema |
| [cli/](cli/) | Headless CLI (`:cli`) — `generate` command |
| [client-setup/](client-setup/) | First-run JDK 26 + Git installers |
| [docker/](docker/) | `Dockerfile` + `docker-compose.yml` for containerized generation |
| [jenkins/](jenkins/) | Optional Jenkins setup — see [jenkins/README.md](jenkins/README.md) |
| [scripts/](scripts/) | Helper scripts — e.g. `generate-in-docker.sh` |

Root [Jenkinsfile](../Jenkinsfile) points at this repo for optional CI.

Gradle still exposes `:core` and `:cli` at the project root via `settings.gradle.kts`:

```kotlin
includeBuild("misc/build-logic")
project(":core").projectDir = file("misc/core")
project(":cli").projectDir = file("misc/cli")
```

## Why `build-logic/` lives here instead of root `buildSrc/`

Gradle only auto-discovers a directory named **`buildSrc/`** at the repository root. To consolidate tooling under `misc/` without losing convention plugins, this repo uses an **included build** at `misc/build-logic/`:

```kotlin
// settings.gradle.kts
includeBuild("misc/build-logic")
```

The precompiled plugin `buildsrc.convention.kotlin-jvm` (JVM toolchain 26, JUnit Platform defaults) is unchanged — only its location moved. Do **not** rename the directory to `misc/buildSrc`; Gradle would not pick it up without `includeBuild`.

## Common commands

```bash
./gradlew :core:compileKotlin
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp
```

Docs: [docs/guides/generation-pipeline.md](../docs/guides/generation-pipeline.md) · [../AGENTS.md](../AGENTS.md)
