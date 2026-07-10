# Generation Pipeline

The Framework repo scaffolds native C++/Lua/Python projects from bundled templates under `template/`.

## Modules

| Module | Role |
|--------|------|
| `:core` | `ProjectGenerator`, `TemplateEngine`, `nxs_config.json` schema (v2) |
| `:cli` | Headless `generate` command |
| `:app` | Compose Desktop client with a minimal **Generate Project** screen |

## Pipeline stages

| Stage | Action |
|-------|--------|
| **Validate** | Check project name pattern |
| **Prepare directory** | Create `outputPath/projectName` or fail if non-empty (use `--force` to overwrite) |
| **Render template** | Copy `template/desktop-app/` or `template/android-app/` with `{{placeholder}}` substitution |
| **Copy shared** | Copy `template/shared/` to `outputPath/shared/` (sibling — required by CMake `../shared`) |
| **Validate config** | Parse rendered `nxs_config.json` (schema v2) |

## Template selection

| Type | Template folder | CLI `--type` |
|------|-----------------|--------------|
| Desktop | `template/desktop-app/` | `desktop` |
| Android | `template/android-app/` | `android` |

## Placeholders

| Key | Example |
|-----|---------|
| `{{projectName}}` | `MyApp` |
| `{{windowTitle}}` | `MyApp - built with The Nexus Framework` |
| `{{cppStandard}}` | `20` |
| `{{license}}` | `Apache-2.0` |
| `{{appType}}` | `desktop` / `android` |

## CLI usage

```bash
# Dry-run (no files written)
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"

# Generate desktop app (default output: builds/framework/MyApp/)
./gradlew :cli:run --args="generate --type desktop --name MyApp"

# Generate android app with explicit output
./gradlew :cli:run --args="generate --type android --name MyApp --output builds/framework/MyApp"

# Overwrite existing output
./gradlew :cli:run --args="generate --type desktop --name MyApp --force"
```

Short flags: `-t desktop -n MyApp -o builds/framework`

## Compose client (v1)

Run `./gradlew :app:run`, click **Generate Project** on the counter screen, enter a name, pick a template, and generate.

## Docker (optional)

```bash
# Build image (JDK 26 + CMake + Ninja)
docker compose -f misc/docker/docker-compose.yml build

# Generate inside container
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp
```

Image entrypoint: `./gradlew :cli:run`. Override with `--args="generate …"`.

## Jenkins (optional)

See [misc/jenkins/README.md](../../misc/jenkins/README.md). The root `Jenkinsfile` parameterizes `PROJECT_NAME`, `TEMPLATE_TYPE`, and `OUTPUT_DIR`.

## Output layout

```
builds/framework/
  MyApp/          # rendered template (desktop-app or android-app)
  shared/         # copied once per output parent (DSL, themes, runtime)
```

Set `NXS_REPO_ROOT` when running outside the repo tree (CLI and Docker set this automatically in containers).

## Related

- [template/README.md](../../template/README.md) — bundled templates
- [docs/templates/desktop-app.md](../templates/desktop-app.md)
- [docs/templates/android-app.md](../templates/android-app.md)
