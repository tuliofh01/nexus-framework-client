# AGENTS.md — Nexus Framework

Guide for AI coding assistants working in **this** Kotlin/Compose Desktop + generation pipeline repo.

## What this repo is

Compose Desktop client + Gradle scaffolder for **The Nexus Framework**. It generates native C++/Lua/Python projects from bundled templates; it does not run the generated apps.

| Module | Role |
|--------|------|
| `:core` | `ProjectGenerator`, `TemplateEngine`, `nxs_config.json` schema (v2) — sources in `misc/core/` |
| `:cli` | Headless `generate` command — sources in `misc/cli/` |
| `:app` | Compose Desktop client — **MVC** under `nexus.opensource` (`model/`, `view/`, `controller/`) |
| `misc/` | Generation pipeline (`:core`, `:cli`), Docker, Jenkins, scripts — see [misc/README.md](misc/README.md) |
| `misc/client-setup/` | First-run JDK 26 + Git installers — run **before** first `./gradlew :app:run` ([misc/client-setup/README.md](misc/client-setup/README.md)) |
| `template/desktop-app/` | Desktop output (SDL3 + pybind11 path, **Zig build default**) |
| `template/android-app/` | Android output (Chaquopy + **Zig JNI bridge**, Djinni deprecated) |
| `template/shared/` | Shared DSL, themes, runtime helpers |
| `docs/assets/diagrams/` | Architecture SVGs referenced from README |

**Generated-app stack:** For “what language does what” (historical lineage, runtime roles, authoring vs lowered), see **[docs/architecture/overview.md](docs/architecture/overview.md#language-stack-runtime)** (Language stack section). Summary: C++ + Lua (**sol2**) + TypeScript/XHTML + Python on **SDL3**; Android uses **Zig JNI** + Chaquopy (Djinni deprecated). Compose `:app` still needs **JDK 26** — generated desktop apps do not.

## First run (human or agent)

**Option A — Zig bootstrap (recommended, cross-platform):**

```bash
zig run misc/client-setup/setup.zig   # installs Zig 0.14.0 + writes env files
source misc/client-setup/env.sh       # Linux/macOS
call misc\client-setup\env.bat        # Windows
```

**Option B — Legacy shell scripts (fallback):**

```bash
./misc/client-setup/linux/setup.sh    # or macos/setup.sh / windows/setup.bat
source misc/client-setup/env.sh       # Windows: call misc\client-setup\env.bat
```

JDK **26** is required (`misc/build-logic` `jvmToolchain(26)`). Do not assume JDK 21.  
Zig **0.14.0** is required for generated native app builds — the bootstrap installs it automatically.

## Build / run

```bash
./gradlew :core:compileKotlin          # compile generator core
./gradlew :core:packTemplateLuaDat     # host pack template lua.dat (LUAC)
./gradlew :core:packTemplatePythonDat  # host pack template python.dat (PYAC)
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
./gradlew :app:run                    # Compose client (Generate Project button)
./gradlew :app:build
./gradlew check
```

### Deploy artifacts (`builds/client/`)

Top-level `builds/` layout — see [builds/README.md](builds/README.md):

| Path | Contents |
|------|----------|
| `builds/client/app/` | Runnable Compose Desktop distribution |
| `builds/client/packages/` | OS installers (`.deb`, `.rpm`, `.dmg`, …) |
| `builds/framework/<projectName>/` | Out-of-source Zig/CMake trees for scaffolded native apps |

```bash
./gradlew :app:deployToBuildsClient          # after createDistributable → builds/client/app/
./gradlew :app:deployPackageToBuildsClient    # after packageDistributionForCurrentOS → builds/client/packages/
```

Gradle still writes intermediate outputs under `app/build/`; the `deploy*` tasks copy finished artifacts into `builds/client/`.

### Android Zig JNI build

Build the native `.so` for Android (requires `ANDROID_NDK`):

```bash
cd template/android-app/zig-services
zig build -Dtarget=aarch64-linux-android    # arm64 device
zig build -Dtarget=x86_64-linux-android     # x86_64 emulator
```

The Gradle APK (`./gradlew :app:assembleDebug`) picks up the Zig-produced `.so` from `jniLibs/`. Legacy CMake fallback: `cmake --preset android-arm64`.

## Architecture (MVC + Composed)

Packages under `app/src/main/kotlin/nexus/opensource/`:

```
App.kt                 # main() → Counter or Generate screen
model/                 # CounterModel, NexusBranding
view/                  # CounterView, GenerateProjectScreen
controller/            # CounterController, GenerateController
```

Generation logic lives in `:core` (`nexus.opensource.core`).

## Conventions

- **Coding styles:** follow [docs/guides/coding-styles.md](docs/guides/coding-styles.md) for C++20, Zig, Kotlin, Lua, TS, and Python.
- **JDK / toolchain**: Java 26 via Foojay in `misc/build-logic` convention plugin.
- **Kotlin**: 2.4.0.
- **Window title**: `NexusBranding.windowTitle(...)` → `{projectName} - built with The Nexus Framework`.
- **Template placeholders**: `{{projectName}}`, `{{windowTitle}}`, `{{cppStandard}}`, etc.
- **Default output**: `builds/framework/{projectName}/`
- **Config file**: `nxs_config.json` schema v2 (see `misc/core/.../NexusConfigSchema.kt`).
- Build + configuration cache enabled — keep build logic cache-compatible.

## Where to edit

| Change | Location |
|--------|----------|
| Generation pipeline | `misc/core/.../service/ProjectGenerator.kt` |
| CLI commands | `misc/cli/.../FrameworkCli.kt` |
| Compose UI | `app/.../view/`, `app/.../controller/` |
| Desktop template | `template/desktop-app/` |
| Android template | `template/android-app/` |
| Docker generation | `misc/docker/`, `misc/scripts/dev/generate-in-docker.sh` |
| Test generation (built apps) | `misc/scripts/test-gen/` — smoke tests under `builds/framework/<name>/` |
| Repo scripts index | `misc/scripts/README.md` — `dev/`, `test-gen/`, `generate-diagrams/` |
| Jenkins (optional) | `misc/jenkins/Jenkinsfile`, `misc/jenkins/README.md` |
| `misc/` layout + `build-logic` included build | `misc/README.md` |

## Docs

- **Architecture overview (start here):** [docs/architecture/overview.md](docs/architecture/overview.md)
- Hub: [docs/README.md](docs/README.md)
- Coding styles: [docs/guides/coding-styles.md](docs/guides/coding-styles.md)
- Generation: [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md)
- Agent gaps: [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md)
- Client setup: [misc/client-setup/README.md](misc/client-setup/README.md)

## Zig patching

Phased native-build orchestration for **generated template apps** — see [docs/architecture/zig-patching.md](docs/architecture/zig-patching.md).

- **Do not** replace all CMake in one PR; CMake stays the fallback during transition.
- **Phase order:** 0 install → 1 `zig-services/` sidecar → 2 Langflow importer (parallel Kotlin track) → 3 desktop Zig default → 4 Android JNI → 5 ArenaAllocator opt-in → 6 docs/diagrams.
- **`zig-services/`** lives under `template/desktop-app/zig-services/` and `template/android-app/zig-services/` (mirrored in generated output).
- **Android JNI**: Zig `export fn` in `jni/python_bridge.zig` replaces Djinni-generated JNI glue (Djinni retired).
- **Keep Gradle** for `:app`, `:core`, and `:cli` — Zig owns generated native binaries only.
- **Pin Zig 0.14.x** in `misc/client-setup/env.sh`; Android needs NDK (API ≥ 29); Zig does not ship Bionic.
