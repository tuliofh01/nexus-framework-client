# Coding with Nexus

How to build applications with generated Nexus templates: UI layers, MVC, Python, Lua, blueprint workflow, themes, and Nerd Font icons.

**Templates:** `template/desktop-app/` · `template/android-app/` · `template/shared/`

---

## Learning progression

Nexus has a steeper ramp than Electron or Tauri, but the generated plotter gives you a working app immediately. Follow this path:

| Step | What you do                                                                     | Skills                                               |
|-----|---------------------------------------------------------------------------------|-----------------------------------------------------|
| 1    | Run the generated template as-is                                                | CMake or Gradle, basic C++                           |
| 2    | Tweak MVC — add a function to `FunctionRegistry`, expose it in `PlotController` | C++                                                  |
| 3    | Add Python sampling in `python/functions.py`                                    | Python, numpy                                        |
| 4    | Script panels in `scripts/panels.lua` — hotkeys, quick-add buttons              | Lua, sol2                                            |
| 5    | Author UI in `ui/ui.xhtml` + `ui/ui.ts`                                         | TypeScript, XHTML DSL                                |
| 6    | Rewire `blueprint.json` in the Compose blueprint editor (`:app`)                | Visual flow authoring (imnodes native panel in v1.1) |
| 7    | Apply themes and Nerd Font icons                                                | JSON presets, font assets                            |
**Honest take:** web-only teams will move faster in Electron/Tauri. Nexus pays off when you need native throughput, small binaries, SDL3 cross-platform parity, or Android field tablets without a WebView.

---

## Language assignment

| Layer                   | Language         | Responsibility              |
|------------------------|------------------|----------------------------|
| **UI (immediate-mode)** | Lua, TS/XHTML    | Panels, widgets, layout     |
| **Domain**              | C++20 MVC        | State, algorithms, bindings |
| **Analytics / plots**   | Python           | numpy, curve sampling       |
| **Android host**        | Kotlin + Zig JNI | JVM bridge, Chaquopy        |
**Call rule:** Lua and TS never call Python directly. They invoke C++ controllers; controllers call `PythonEngine` (pybind11 or Chaquopy via Zig JNI).

---

## Build UI (TypeScript + XHTML)

Edit `ui/ui.xhtml` for structure and `ui/ui.ts` for logic. Tags map to ImGui widgets — see the [XHTML/TypeScript DSL section](#xhtmltypescript-dsl) below.

```xml
<panel id="sidebar" title="Functions" width="320">
  <combo id="function-picker" items-source="availableFunctions" bind="pendingFunction"/>
  <button label="Add" on-click="addPending"/>
</panel>
```

The toolchain lowers this into Lua equivalent to `scripts/panels.lua`. You can mix both: XHTML for main layout, Lua for hotkeys and experimental panels.

---

## MVC in C++

```
src/model/       durable state (FunctionRegistry)
src/controller/  commands + Python refresh (PlotController)
src/view/        ImGui draw + sol2 Lua host (PlotterView, LuaPanels)
```

Add a feature:

1. Register data in `model/`.
2. Add a command method in `controller/`.
3. Expose to Lua via `LuaPanels` or to XHTML via `native()` / `invoke()`.
4. Draw widgets in `view/`.

---

## Python backends

### Desktop (pybind11)

`PythonEngine` embeds CPython at startup and imports `python/functions.py`. numpy arrays cross via buffer protocol — samples stay in native memory.

### Android (Chaquopy + Zig JNI)

`PythonBridge.kt` defines the abstract interface. C++ `NativePythonBridge.cpp` calls back into it via JNI. `AppCore.installPythonBridge()` hands it to native code before `SDL_main`.

---

## Blueprint / imnodes workflow

`blueprint.json` is a Langflow-style graph:

- **Nodes:** `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`
- **Edges:** data ports (`sampleCache`, `activeCurves`) and command ports (`commands`)

Open **Generate Project → Edit blueprint** in the Compose client to rewire without editing CMake. The generator validates and emits the same JSON on regen.

Schema reference: [blueprint-schema.md](../templates/blueprint-schema.md)

**v1:** Compose Canvas graph + JSON preview. **v1.1 (planned):** imnodes native panel using the same schema.

---

## Themes

Presets in `template/shared/themes/`:

| Id            | File               | Use                   |
|--------------|--------------------|----------------------|
| `nexus-dark`  | `nexus-dark.json`  | Default desktop       |
| `nexus-light` | `nexus-light.json` | Bright environments   |
| `nexus-field` | `nexus-field.json` | Android field tablets |
Set in `nxs_config.json`:

```json
{ "theme": "nexus-field" }
```

Or at startup:

```cpp
nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-dark.json");
```

`NexusTheme` in `template/shared/runtime/` maps JSON preset ids to ImGui colors and style vars.

---

## Nerd Font icons

ImGui labels can include icon glyphs when a Nerd Font is loaded.

1. Download **JetBrainsMono Nerd Font** (SIL OFL) from [nerdfonts.com](https://www.nerdfonts.com/font-downloads).
2. Save as `assets/fonts/NexusNerdFont-Regular.ttf`.
3. `FontConfig::loadNerdFont()` merges the font into ImGui's atlas (called in `main.cpp`).

Use the [Nerd Fonts Cheat Sheet](https://www.nerdfonts.com/cheat-sheet) for code points. Example in C++:

```cpp
ImGui::Text("\xef\x80\x95 Functions");  // UTF-8 home icon when font loaded
```

In Lua panels, paste UTF-8 literals or define constants. Increase touch targets on Android with `FontConfig::setIconScale(1.25f)`.

Insert the font under `assets/fonts/NexusNerdFont-Regular.ttf` in your generated project.

---

## Adding dependencies in generated apps

> Merged from: `adding-dependencies.md` (now part of this guide)

After running client-setup (JDK 26 + Git) and generating a project, install packages for the three language runtimes:

**Python** (`uv`):
```bash
uv add numpy scipy
uv sync
```
Zig's `build.zig` automatically picks up `.venv` — no extra wiring.

**Lua** (C++ sol2 bindings):
```bash
# Copy module to runtime folder, use sol2 API:
state["module"] = module;
```

**C++** (FetchContent):
```bash
# In build.zig:
include(FetchContent)
FetchContent_Declare(glm GIT_REPOSITORY ...)
# Or add to build.zig.zon for the Zig build path
```

Desktop and Android share the same 3-runtime dependency model — no WebView, no npm, no bundler.

---

## XHTML/TypeScript DSL

> Merged from: `shared-dsl.md` (now part of this guide)

The XHTML + TypeScript DSL lowers declarative UI markup into native ImGui calls — no DOM, no browser engine. Components like `<Panel>`, `<Button>`, `<Slider>` at `template/shared/dsl/` are compiled by a TypeScript transformer to C++ structs, not HTML strings.

Use case: rapid UI prototyping in a familiar syntax without touching C++. The DSL is optional — you can write ImGui calls directly with the same visual result.

---

## Use cases

| Scenario        | Nexus strength                                 |
|----------------|-----------------------------------------------|
| Trading desk    | Sub-ms UI; C++ feeds; Python models in-process |
| CAD / 3D viewer | SDL3 GPU viewport; retained geometry in C++    |
| Scientific viz  | numpy arrays never leave native address space  |
| Game tools      | Immediate-mode UI like engine debug overlays   |
| Audio DSP       | Low-latency C++ signal path                    |
| Field tablet    | SDL3 GLES + Zig JNI + Chaquopy; no WebView     |
| Robotics panel  | Touch ImGui + `android.*` Lua bindings         |
| Infra monitor   | Lightweight native binary, always-on           |
---

---

## Coding styles

### C++20 (desktop + Android templates, `template/shared/runtime/`)

RAII for all resources, `std::span`/`std::string_view` for buffer views, `[[nodiscard]]` on bug-prone returns, `constexpr` for compile-time constants. No C-style casts; use `static_cast`/`reinterpret_cast` with a note. Run `clang-format` from template root.

### Zig (`zig-services/`)

`zig fmt` before commits. Explicit allocators — never assume implicit heap. Error unions (`!T`), `callconv(.C)` on C ABI exports.

### Kotlin (`:app`, `:core`, `:cli`)

Null-safety with `?.`/`?:`/`requireNotNull`, data classes for JSON schemas, `require`/`check` for programmer errors. Keep Gradle config-cache compatible.

### Lua (`scripts/`)

2-space indent, snake_case. Side effects in `cpp.controller`, not in panel layout code.

### TypeScript (`ui/`, `shared/dsl/`)

Semicolons, 4-space indent, `state<T>()`/`native<T>()`/`invoke()` for controller wiring. `readonly` on non-mutated fields.

### Python (`python/`, Chaquopy)

PEP 8, type hints on public functions. Desktop returns numpy tuples for pybind11 buffer protocol.

---

## Related

- [Architecture overview](../architecture/overview.md)
- [Blueprint schema](../templates/blueprint-schema.md)
- [Docs hub](../README.md)
