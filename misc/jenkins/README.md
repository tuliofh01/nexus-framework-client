# Jenkins (optional)

Jenkins is **optional**. Local generation works without it:

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
```

## Pipeline file

The canonical pipeline lives at **[misc/jenkins/Jenkinsfile](Jenkinsfile)** (not the repository root).

When creating a **Pipeline** job from SCM (Git):

1. Point the job at this repository.
2. Set **Script Path** to `misc/jenkins/Jenkinsfile` (Pipeline from SCM / multibranch default is `Jenkinsfile` at root — change it).

For a one-off local run with the Jenkins CLI (when a controller is reachable):

```bash
jenkins -f misc/jenkins/Jenkinsfile
```

If your Jenkins installation cannot override the script path, symlink from the repo root:

```bash
ln -sf misc/jenkins/Jenkinsfile Jenkinsfile
```

## Setup

1. Install JDK 26 and register it in Jenkins as a JDK installation named `jdk-26` (matches `Jenkinsfile` `tool` name).
2. Install CMake + Ninja on the agent if you enable `RUN_CMAKE_BUILD` for desktop templates.
3. Create a **Pipeline** job with **Script Path** = `misc/jenkins/Jenkinsfile`.

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
./misc/scripts/dev/generate-in-docker.sh desktop MyApp builds/framework/MyApp
```

See [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md).
