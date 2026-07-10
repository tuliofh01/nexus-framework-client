# Jenkins (optional)

Jenkins is **optional**. Local generation works without it:

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
```

## Setup

1. Install JDK 26 and register it in Jenkins as a JDK installation named `jdk-26` (matches `Jenkinsfile` `tool` name).
2. Install CMake + Ninja on the agent if you enable `RUN_CMAKE_BUILD` for desktop templates.
3. Create a **Pipeline** job pointing at this repository's `Jenkinsfile`.

## Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| `PROJECT_NAME` | `MyApp` | Project name passed to the generator |
| `TEMPLATE_TYPE` | `desktop` | `desktop` or `android` |
| `OUTPUT_DIR` | `builds/framework` | Parent output directory |
| `RUN_CMAKE_BUILD` | `false` | Run CMake configure/build after generate (desktop only) |
| `DRY_RUN` | `false` | Preview render paths without writing files |

## Example

Generate a desktop app into `builds/framework/MyApp/`:

- `PROJECT_NAME` = `MyApp`
- `TEMPLATE_TYPE` = `desktop`
- `OUTPUT_DIR` = `builds/framework`

## Docker alternative

For containerized generation (no Jenkins agent setup):

```bash
./scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp
```

See [docs/guides/generation-pipeline.md](../docs/guides/generation-pipeline.md).
