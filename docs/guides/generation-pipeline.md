# Generation Pipeline

The Framework repo scaffolds native C++/Lua/Python projects from bundled templates under `template/`.

## Modules

| Module | Role |
|--------|------|
| `:core` | `ProjectGenerator`, `TemplateEngine`, `nxs_config.json` schema (v2) |
| `:cli` | Headless `generate` command |
| `:app` | Compose Desktop client — **Generate Project** + **Blueprint Editor** (v1 JSON graph) |

## Pipeline stages

| Stage | Action |
|-------|--------|
| **Validate** | Check project name pattern |
| **Prepare directory** | Create `outputPath/projectName` or fail if non-empty (use `--force` to overwrite) |
| **Render template** | Copy `template/desktop-app/` or `template/android-app/` with `{{placeholder}}` substitution |
| **Copy shared** | Copy `template/shared/` to `outputPath/shared/` (sibling — required by CMake `../shared`) |
| **Validate config** | Parse rendered `nxs_config.json` (schema v2) |
| **Validate blueprint** | Parse `blueprint.json` via `BlueprintValidator` |

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
| `{{createdAt}}` | ISO-8601 timestamp (script protection key material) |
| `{{scriptProtectionEnabled}}` | `true` / `false` |
| `{{scriptProtectionSalt}}` | UUID salt when protection enabled |

## Script archives (`lua.dat` / `python.dat`)

Desktop templates pack `scripts/` and `python/` into binary archives beside the executable under `misc/`. Android packs Lua into APK `assets/lua.dat` via Gradle `packLuaDat` (Python stays on Chaquopy paths — no `python.dat` on Android).

| Archive | Magic | Source | Desktop output | Android output |
|---------|-------|--------|----------------|----------------|
| `lua.dat` | `LUAC` | `scripts/**/*.lua` | `misc/lua.dat` | `build/assets/lua.dat` |
| `python.dat` | `PYAC` | `python/**/*.py` | `misc/python.dat` | N/A (Chaquopy) |

**Format:** 32-byte header (magic, version, count, reserved). Version 2 sets `reserved[0] |= 0x01` and stores a 16-byte nonce; payloads use **nxs-v1** stream obfuscation (`SHA256(projectName + salt + createdAt)` XOR stream). This is obfuscation, not DRM.

`nxs_config.json` keys:

```json
"scriptProtection": { "enabled": true, "salt": "…", "algorithm": "nxs-v1" },
"project": { "createdAt": "…" }
```

The generator also renders `shared/runtime/ScriptProtectionConfig.hpp` from `ScriptProtectionConfig.hpp.in`.

**Gradle validation tasks** (host CMake required):

```bash
./gradlew :core:packTemplateLuaDat
./gradlew :core:packTemplatePythonDat
```

Disable encryption for local iteration: `./gradlew :cli:run --args="generate --type desktop --name MyApp --script-protection false"`

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

Run `./gradlew :app:run`, click **Generate Project**, enter a name, pick a template, optionally **Edit blueprint** (Compose graph editor), then generate.

Custom blueprints from the editor are written to the output project as `blueprint.json` (template placeholders like `{{projectName}}` are substituted).

Schema: [blueprint-schema.md](../templates/blueprint-schema.md)

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
