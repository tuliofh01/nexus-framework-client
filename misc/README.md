# misc/

Supporting tooling for the Framework scaffold client. The Framework client is a **single Gradle module** at the repo root (`src/main/kotlin/com/nexus/framework/`).

## Layout

| Path | Role |
|------|------|
| [build_client.sh](build_client.sh) | Compile the unified client (`compileKotlin`) — **prompts for Nexus License** before Gradle |
| [../build_client.sh](../build_client.sh) | **Preferred:** source env + license + deploy → `builds/clients/NexusFrameworkClient-<ver>/` |
| [build-logic/](build-logic/) | Gradle convention plugins (included build) — JVM toolchain 26 |
| [client-setup/](client-setup/) | First-run JDK 26 + Zig bootstrap — see [client-setup/README.md](client-setup/README.md) |
| [docker/](docker/) | `Dockerfile` + `docker-compose.yml` for containerized generation |
| [jenkins/](jenkins/) | Optional Jenkins CI — [Jenkinsfile](jenkins/Jenkinsfile), [README](jenkins/README.md) |
| [scripts/](scripts/) | Repo automation — dev, test-gen, diagram generation |
| [translations/](translations/) | Localized READMEs — see [translations/README.md](translations/README.md) |

Pipeline definition: [jenkins/Jenkinsfile](jenkins/Jenkinsfile). Configure the job **Script Path** to `misc/jenkins/Jenkinsfile` — see [jenkins/README.md](jenkins/README.md).

Gradle is a **single root project** (`settings.gradle.kts` has no `include(":core")` etc.). Sources live under `src/main/kotlin/com/nexus/framework/` (`core/`, `cli/`, `shared/`, `ui/…`).

```kotlin
pluginManagement {
    includeBuild("misc/build-logic")
}
rootProject.name = "Framework"
```

## Why `build-logic/` lives here instead of root `buildSrc/`

Gradle only auto-discovers a directory named **`buildSrc/`** at the repository root. To consolidate tooling under `misc/` without losing convention plugins, this repo uses an **included build** at `misc/build-logic/` under `pluginManagement`:

```kotlin
// settings.gradle.kts
pluginManagement {
    includeBuild("misc/build-logic")
}
```

The precompiled plugin `buildsrc.convention.kotlin-jvm` (JVM toolchain 26, JUnit Platform defaults) remains available for modules that apply it. Do **not** rename the directory to `misc/buildSrc`; Gradle would not pick it up without `includeBuild`.

## Common commands

```bash
./build_client.sh                      # license + deploy → builds/clients/NexusFrameworkClient-1.1.0/
./misc/build_client.sh                 # license dialog (once) then compile only
./misc/build_client.sh --accept-license  # CI / non-interactive accept
./misc/build_client.sh --show-license  # re-show Nexus License
./misc/build_client.sh --deploy        # same as root ./build_client.sh (no env source)
./gradlew compileKotlin
./gradlew runCli --args="generate --type desktop --name MyApp --dry-run"
./misc/scripts/nexus-dev.sh compile
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture
```

`build_client.sh` stores acceptance in `misc/.license-accepted` (gitignored). See `./misc/build_client.sh --help`.

Docs: [docs/guides/generation-pipeline.md](../docs/guides/generation-pipeline.md) · [../AGENTS.md](../AGENTS.md) · [Nexus License](../LICENSE)
