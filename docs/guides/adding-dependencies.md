# Adding dependencies after client-setup

How to install **C++**, **Lua**, and **Python** packages in a **generated** Nexus app — after you have finished [misc/client-setup/](../../misc/client-setup/README.md) (JDK 26 + Git) and emitted a project with **Generate Project** or `:cli`.

This guide applies to projects under **`builds/framework/<ProjectName>/`**, not to the Compose scaffolder repo itself.

---

## Prerequisites

| Step | What | Where |
|------|------|-------|
| Client setup | JDK 26, Git, basic build tools | [misc/client-setup/](../../misc/client-setup/README.md) |
| Generate a project | Desktop or Android template | `./gradlew :app:run` or `./gradlew :cli:run --args="generate …"` |
| Open the output | Generated app root | `builds/framework/<ProjectName>/` |

Each generated tree includes `nxs_config.json` (schema v2), `blueprint.json`, and platform build files. CMake presets write binaries under `builds/framework/<ProjectName>/debug` or `release` (see `build.outputDir` in config).

**Optional:** [misc/scripts/test-gen/](../../misc/scripts/test-gen/README.md) can scaffold smoke-test stubs for an already-generated app — useful after you add dependencies and want a quick CTest or `androidTest` harness.

---

## C++ — Desktop template

Desktop native code lives in `src/` (MVC). Third-party C++ libraries are usually added in the project root **`CMakeLists.txt`** at `builds/framework/<ProjectName>/CMakeLists.txt`.

### System packages (recommended first)

[client-setup](../../misc/client-setup/README.md) installs JDK and Git only. For **generated** desktop builds, install CMake ≥ 3.24, Ninja, a C++20 compiler, and SDL3 runtime deps separately.

| Distro | Example install |
|--------|-----------------|
| **Debian / Ubuntu** | `sudo apt install cmake ninja-build g++ python3-dev libgl1-mesa-dev libasound2-dev libpulse-dev libx11-dev libxext-dev libxcursor-dev libxrandr-dev libxi-dev libxss-dev libdrm-dev libgbm-dev libwayland-dev libdecor-0-dev` |
| **Fedora / RHEL** | `sudo dnf install cmake ninja-build gcc-c++ python3-devel mesa-libGL-devel alsa-lib-devel pulseaudio-libs-devel libX11-devel libXext-devel libXcursor-devel libXrandr-devel libXi-devel libXScrnSaver-devel libdrm-devel mesa-libgbm-devel wayland-devel libdecor-devel` |
| **Arch** | `sudo pacman -S cmake ninja gcc python libgl libx11 libxext libxcursor libxrandr libxi libxss libdrm mesa wayland libdecor` |

macOS: `brew install cmake ninja python@3.11` (SDL3 deps are usually satisfied via FetchContent).

### CMake FetchContent (Nexus default)

The desktop template vendors core deps with **FetchContent** — no separate package manager required for SDL3, Dear ImGui, ImPlot, imnodes, Lua, sol2, or pybind11:

```cmake
include(FetchContent)

FetchContent_Declare(mylib
    GIT_REPOSITORY https://github.com/example/mylib.git
    GIT_TAG        v1.0.0)
FetchContent_MakeAvailable(mylib)

target_link_libraries({{projectName}} PRIVATE mylib::mylib)
```

Existing declarations are near the top of `CMakeLists.txt`. Reconfigure after edits:

```bash
cd builds/framework/MyApp
cmake --preset debug
cmake --build --preset debug
```

Or, from the generated project directory:

```bash
cmake --build build    # when using -B build
```

### Prefer system packages

Preset **`release-system-deps`** sets `NXS_PREFER_SYSTEM_DEPS=ON`, which tries `find_package(SDL3)` and `find_package(Lua)` before FetchContent. Use when your distro ships compatible versions:

```bash
cmake --preset release-system-deps
cmake --build --preset release
```

### Optional: vcpkg manifest

Nexus templates do not ship a vcpkg baseline. If you prefer vcpkg:

1. Add `vcpkg.json` at the project root.
2. Pass `-DCMAKE_TOOLCHAIN_FILE=<vcpkg>/scripts/buildsystems/vcpkg.cmake` at configure time.
3. Link targets in `CMakeLists.txt` as usual.

Keep FetchContent for Nexus-bundled deps unless you intentionally replace them with vcpkg ports.

### Wire a new library into `src/`

Typical flow:

1. Add `FetchContent_Declare` / `find_package` in `CMakeLists.txt`.
2. `target_link_libraries({{projectName}} PRIVATE …)` on the main executable (and on `{{projectName}}_plotter` if you build examples).
3. `#include` headers in `src/model/`, `src/controller/`, or `src/view/` and call the API from MVC code.
4. Rebuild — `pack_lua_dat` / `pack_python_dat` run automatically when those targets are `ALL`.

See [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) for directory layout.

---

## C++ — Android (NDK)

Android native code is built through Gradle **`externalNativeBuild`** → root **`CMakeLists.txt`** (sibling to `app/`).

| Piece | Location |
|-------|----------|
| Gradle CMake hook | `app/build.gradle.kts` → `externalNativeBuild { cmake { path = file("../CMakeLists.txt") } }` |
| Native library | `add_library({{projectName}} SHARED …)` in `CMakeLists.txt` |
| JVM bridge | Djinni — `djinni/app.djinni`, `djinni-generated/` |

### FetchContent on Android

The Android template also FetchContent's SDL3, ImGui, Lua, and sol2. Caveats:

- **Network at configure time** — first Gradle sync downloads Git deps; cache `_deps/` under the CMake build dir.
- **No pybind11 on NDK** — Python is **Chaquopy on the JVM**, not embedded in the `.so`.
- **Prefab** — prefer libraries published as Android Prefab AARs when available; otherwise FetchContent into a `STATIC` helper target and link into `{{projectName}}`.
- **ABI filters** — `ndk { abiFilters }` in `app/build.gradle.kts` must match devices you test (`arm64-v8a`, `x86_64` by default).

Rebuild:

```bash
cd builds/framework/MyApp
./gradlew :app:assembleDebug
```

Full Android layout: [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md) · [docs/templates/android-app.md](../templates/android-app.md).

---

## Python — Desktop (pybind11)

Desktop embeds CPython via **pybind11**. Sources pack into **`misc/python.dat`** at build time; dev plaintext lives in **`python/`**.

### Dev environment

| Requirement | Notes |
|-------------|-------|
| `python3-dev` / `Python3 Development` | CMake `find_package(Python3 … Development.Embed)` |
| Matching interpreter | Same Python that `pip install` targets |
| Optional venv | Configure with `-DPython3_ROOT_DIR=/path/to/venv` if you isolate deps |

Install template requirements (plotter sample uses numpy):

```bash
cd builds/framework/MyApp
pip install -r requirements.txt
```

`nxs_config.json` records the path under `features.python.requirements` (desktop uses **`requirements.txt`**, not `features.python.packages`).

### Add application Python

1. Edit or add modules under **`python/`** (entry module `functions.py` is loaded by `PythonEngine`).
2. Rebuild — CMake target **`pack_python_dat`** repacks `python/**/*.py` into `misc/python.dat` and copies beside the executable.

```bash
cmake --build --preset debug --target pack_python_dat
cmake --build --preset debug
```

At runtime, `PythonEngine` loads from `misc/python.dat` first, then falls back to the plaintext `python/` directory next to the binary for local iteration.

### Extra pip packages

Install into the **same interpreter** CMake embeds. For numpy-heavy code, keep `pybind11/numpy.h` includes in `src/controller/PythonEngine.cpp` (already used in the plotter).

Document deps in **`requirements.txt`** and in `blueprint.json` on the `python.module` node (`packages` array) for blueprint-driven workflows:

```json
"packages": ["numpy"]
```

### Blueprint vs config

| Field | Platform | Purpose |
|-------|----------|---------|
| `features.python.requirements` | Desktop | Path to `requirements.txt` in `nxs_config.json` |
| `blueprint.json` → `python.module.packages` | Both | Declarative list for docs / editor (mirror in requirements or Chaquopy) |

---

## Python — Android (Chaquopy)

Android does **not** use `python.dat`. Chaquopy bundles **`app/src/main/python/`** and pip wheels into the APK.

### `nxs_config.json`

```json
"python": {
  "enabled": true,
  "embedding": "chaquopy",
  "sourceDir": "app/src/main/python",
  "packages": ["numpy"]
}
```

The `packages` array documents intent; Gradle **`chaquopy { pip { … } }`** is what actually installs wheels.

### Gradle pip block

In `app/build.gradle.kts`:

```kotlin
chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install("numpy")
            install("pandas")   // add more as needed
        }
    }
}
```

Keep `nxs_config.json` `features.python.packages` in sync for tooling that reads the manifest.

### Application sources

| Path | Role |
|------|------|
| `app/src/main/python/helpers.py` | Default module (extend or add siblings) |
| `app/.../ChaquopyPythonBridge.kt` | JVM bridge called from Djinni `PythonBridge` |

Rebuild APK:

```bash
./gradlew :app:assembleDebug
```

Chaquopy downloads wheels on first build — allow network and expect larger APKs.

---

## Lua

Lua has **no package manager** in the Nexus default stack. Scripts ship as plaintext and as **`misc/lua.dat`** (desktop) or **`assets/lua.dat`** (Android).

### Where scripts live

| Path | Purpose |
|------|---------|
| `scripts/panels.lua` | Entry — registers ImGui panels via `nxs.register_panel` |
| `scripts/*.lua` | Additional modules (require from `panels.lua`) |

Desktop packs via CMake target **`pack_lua_dat`**; Android packs via Gradle **`packLuaDat`** before `preBuild`.

### Add a Lua module

1. Create `scripts/my_panel.lua`.
2. In `panels.lua`:

```lua
local my_panel = require("my_panel")
my_panel.register(nxs, ui)
```

3. Rebuild:

```bash
# Desktop
cmake --build --preset debug --target pack_lua_dat
cmake --build --preset debug

# Android
./gradlew :app:assembleDebug
```

`sol2` loads from the packed archive at runtime (`LuaPanels`); plaintext `scripts/` is also copied/staged for dev.

### LuaRocks (advanced, not default)

You *can* install pure-Lua rocks and copy `.lua` files into `scripts/`, but Nexus does not integrate LuaRocks. Prefer vendoring small `.lua` files or keeping logic in C++/Python for anything non-trivial.

---

## Workflow summary

| Language | Desktop — where to add | Desktop — rebuild | Android — where to add | Android — rebuild |
|----------|------------------------|-------------------|------------------------|-------------------|
| **C++** | `CMakeLists.txt`, `src/` | `cmake --build --preset debug` | `CMakeLists.txt`, `src/`, `app/build.gradle.kts` | `./gradlew :app:assembleDebug` |
| **Python** | `python/`, `requirements.txt` | `cmake --build --preset debug` (runs `pack_python_dat`) | `app/src/main/python/`, `chaquopy { pip }`, `nxs_config.json` `packages` | `./gradlew :app:assembleDebug` |
| **Lua** | `scripts/*.lua` | `cmake --build --preset debug` (runs `pack_lua_dat`) | `scripts/*.lua` | `./gradlew :app:assembleDebug` (runs `packLuaDat`) |

---

## Client integration workflow

1. **Finish** [client-setup](../../misc/client-setup/README.md) and `source misc/client-setup/env.sh`.
2. **Generate** — `./gradlew :app:run` → **Generate Project**, or CLI:

   ```bash
   ./gradlew :cli:run --args="generate --type desktop --name MyApp"
   ```

3. **Open** `builds/framework/MyApp/` in your editor or IDE.
4. **Add dependencies** using the sections above (CMake / pip / `scripts/`).
5. **Rebuild** the native app (not the scaffolder) with the commands in the summary table.
6. **Optional** — scaffold tests:

   ```bash
   ./misc/scripts/test-gen/linux/generic.sh --project MyApp
   ```

Cross-links:

- [docs/guides/coding-with-nexus.md](coding-with-nexus.md) — MVC, Lua, Python call rules
- [docs/guides/generation-pipeline.md](generation-pipeline.md) — how projects are emitted
- [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md) — generated-app conventions
- [docs/templates/desktop-app.md](../templates/desktop-app.md) · [docs/templates/android-app.md](../templates/android-app.md) — template reference
