# AGENTS.md — Nexus Android App

Guide for AI coding assistants working in **this generated Android project** (scaffolded from `template/android-app/`), not the Nexus Framework scaffolder repo.

## What this project is

The same **Desmos-style function plotter** as the desktop template, adapted for SDL3 on Android NDK with OpenGL ES, Dear ImGui + ImPlot, Lua via sol2, and **Chaquopy** Python on the JVM. A **Djinni** bridge connects native C++ (`PlotterCore`) to Kotlin (`ChaquopyPythonBridge`). MVC lives under `src/`; Kotlin hosts SDL activity and Python lifecycle.

## Key directories

| Path | Role |
|------|------|
| `app/src/main/java/com/nexus/plotter/` | Kotlin app code (`NexusApplication`, `MainActivity`, `ChaquopyPythonBridge`) |
| `app/src/main/python/` | Chaquopy Python sources (bundled by Gradle, no `python.dat`) |
| `src/model/`, `src/controller/`, `src/view/` | Native MVC — mirrors desktop layout |
| `djinni/plotter.djinni` | IDL for C++↔JVM bridge |
| `djinni-generated/` | Generated C++, JNI, Kotlin stubs — regen from IDL |
| `scripts/` | Lua sources → packed to `build/assets/lua.dat` |
| `ui/` | TypeScript + XHTML DSL |
| `blueprint.json` | App graph (same node types as desktop) |
| `flows/flows.json` | Optional runtime services — delete or disable in `nxs_config.json` to skip |
| `src/service/FlowRunner.*` | Native flow runner (NO-OP when flows disabled) |
| `app/.../FlowRunner.kt` | JVM stub for future manual triggers |
| `nxs_config.json` | Nexus schema v2 — Android targets, Chaquopy, Djinni, optional `flows` |
| `app/src/main/assets/` | Themes, fonts, staged `lua.dat` |

**Vendored (do not edit):** `app/src/main/java/org/libsdl/app/` — upstream SDL3 Android glue.

## Build / run

**Prerequisites:** Android SDK, NDK, JDK 17+, Android Studio or CLI Gradle.

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug    # device/emulator attached
```

Native C++ builds via CMake (`CMakeLists.txt` at template root, triggered by `app/build.gradle.kts` `externalNativeBuild`). Debug APK: `app/build/outputs/apk/debug/`.

CMake presets (host-side native tooling):

```bash
cmake --preset debug
cmake --build --preset debug
```

## Conventions

- **Application ID:** `com.nexus.plotter` (`nxs_config.json` → `project.applicationId`).
- **Default theme:** `nexus-field` (high-contrast field tablet) — override via `"theme": "nexus-dark"` in `nxs_config.json`.
- **ABIs:** `arm64-v8a`, `x86_64` (`targets.android.ndkAbis`).
- **blueprint.json:** Same schema v1 as desktop — node types `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`. Python node `data.source` points to `app/src/main/python/functions.py` on Android.
- **flows/flows.json (optional):** Runtime automation — disable via `nxs_config.json` → `"flows": { "enabled": false }` or delete the file.
- **nxs_config.json:** `features.python.embedding` = `chaquopy` (not pybind11). `features.djinni` points to IDL and `djinni-generated/`.
- **Script archives:** Gradle task `packLuaDat` packs `scripts/**/*.lua` → `build/assets/lua.dat`. **No `python.dat` on Android** — Chaquopy bundles `app/src/main/python/` directly. `scriptProtection` still applies to `lua.dat` when enabled.

## Where to edit

| Change | Location |
|--------|----------|
| Python curve math | `app/src/main/python/functions.py` |
| Chaquopy ↔ native bridge | `ChaquopyPythonBridge.kt`, `djinni/plotter.djinni` |
| Native Python engine glue | `src/controller/PythonEngine.*` |
| Plot commands | `src/controller/PlotController.*` |
| Function catalog | `src/model/FunctionRegistry.*` |
| ImGui / ImPlot UI | `src/view/PlotterView.*` |
| Lua panels | `scripts/panels.lua`, `src/view/LuaPanels.*` |
| TS/XHTML UI | `ui/ui.xhtml`, `ui/ui.ts` |
| Activity / Chaquopy init | `NexusApplication.kt`, `MainActivity.kt` |
| C++↔JVM API surface | `djinni/plotter.djinni` → run `./scripts/regen-djinni.sh` |
| App wiring graph | `blueprint.json` |
| Theme / SDK versions | `nxs_config.json`, `app/build.gradle.kts` |

## Boot sequence

1. `NexusApplication` starts Chaquopy.
2. `MainActivity.onCreate` calls `PlotterCore.installPythonBridge(ChaquopyPythonBridge())`.
3. SDL3 runs `SDL_main` in `src/main.cpp` — same MVC loop as desktop.

## Python integration (Chaquopy)

- Sources: `app/src/main/python/` (Gradle `chaquopy` plugin; packages e.g. `numpy` in `nxs_config.json` → `features.python.packages`).
- Bridge: Djinni `PythonBridge` interface; Kotlin `ChaquopyPythonBridge` implements JVM side; C++ `PlotterCore` calls through generated JNI.
- **Unlike desktop:** no pybind11 embed, no `misc/python.dat`. Edit Python files and rebuild APK — Chaquopy packages them.
- Flow: same blueprint ports — `evaluate` → `sampleCache` → `activeCurves` → `commands`.

## Lua integration (sol2)

- Sources: `scripts/panels.lua`.
- Loader: `LuaPanels` reads APK asset `lua.dat` (paths `misc/lua.dat`, `lua.dat`) with fallback to plaintext `scripts/panels.lua`.
- Built by Gradle `packLuaDat` before asset merge (uses host `pack_archive` binary from CMake).
- Hotkeys from `blueprint.json` → `lua.script` node.

## Djinni workflow

Edit `djinni/plotter.djinni`, then:

```bash
./scripts/regen-djinni.sh
```

Requires [Djinni](https://djinni.xlcpp.dev/) on `PATH`. Do **not** hand-edit `djinni-generated/cpp/` or `djinni-generated/jni/`. Update `djinni-generated/kotlin/` to match JVM signatures after IDL changes (script prints staged diff).

## TypeScript / XHTML DSL

Same pattern as desktop: `ui/ui.xhtml` + `ui/ui.ts#PlotterPage`, shared primitives from `../shared/dsl/`. Assets include `ui/` via Gradle `sourceSets`.

## Do not edit

- **`org/libsdl/app/**`** — vendored SDL3 Android glue; replace from a newer SDL tag, never customize for app features.
- **`djinni-generated/cpp/`**, **`djinni-generated/jni/`** — regenerate from IDL.
- **`build/assets/lua.dat`** — regenerate via `./gradlew packLuaDat`.
- **Generated APK / `.cxx/` build caches** — rebuild instead of patching.

## Docs

- [docs/templates/android-app.md](../../docs/templates/android-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
- [docs/templates/shared-dsl.md](../../docs/templates/shared-dsl.md)
- [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)
