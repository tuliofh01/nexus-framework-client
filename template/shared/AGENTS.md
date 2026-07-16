# AGENTS.md — Nexus Shared Template

Guide for AI coding assistants working with **shared Nexus resources** copied into generated desktop and Android projects. This folder is not a standalone app — the Framework client merges it into each scaffold at generation time.

## What this folder is

Cross-target building blocks for Nexus apps: the **TypeScript/XHTML DSL** (web-familiar UI authoring lowered to ImGui), **ImGui theme presets**, bundled **assets** (logo, icon fonts), **C++ runtime helpers** (`NexusTheme`, `FontConfig`, script archive crypto), and the **pack_archive** host tool that builds `lua.dat` / `python.dat`. Desktop and Android templates both compile `runtime/` and reference `dsl/`, `themes/`, and `assets/`.

## Key directories

| Path       | Role                                                                 |
|-----------|---------------------------------------------------------------------|
| `dsl/`     | TS/XHTML DSL — `core.ts`, `components.ts`, `tags.ts`, `index.ts`     |
| `themes/`  | ImGui JSON presets (`nexus-dark`, `nexus-light`, `nexus-field`)      |
| `assets/`  | Logo (`nexus-logo.png`), Nerd Font glyphs (`fonts/`)                 |
| `runtime/` | `NexusTheme`, `FontConfig`, `ScriptArchive`, `ScriptCrypto`, `Paths` |
| `tools/`   | `pack_archive.cpp` — host CLI for LUAC/PYAC archives                 |
After generation, copies land in the project as:

| Shared source   | Typical generated path                                           |
|----------------|-----------------------------------------------------------------|
| `dsl/`          | Referenced from `ui/ui.ts` imports; sources stay alongside `ui/` |
| `themes/*.json` | `assets/themes/`                                                 |
| `assets/`       | `assets/` (desktop) or `app/src/main/assets/` (Android)          |
| `runtime/`      | Compiled into native binary via parent `build.zig`               |
## Build involvement

**Desktop** — parent `build.zig` adds `runtime/` sources and builds `pack_archive` for `pack_lua_dat` / `pack_python_dat` targets.

**Android** — `app/build.gradle.kts` builds host `pack_archive` for Gradle `packLuaDat`; native code links `runtime/` via `zig-services/build.zig`.

Host-only tool:

```bash
zig build-exe tools/pack_archive.cpp --name pack_archive
# pack_archive <root> <out.dat> --lua|--python [--encrypt]
```

## Conventions

- **Coding styles:** follow [docs/guides/coding-styles.md](../../docs/guides/coding-styles.md) — C++20 in `runtime/`, TS in `dsl/`, `clang-format` in this folder.
- **DSL tags:** XHTML tags in `tags.ts` map to Dear ImGui / ImPlot widgets (`Button`, `Slider`, `Plot`, etc. in `components.ts`).
- **Component base:** `core.ts` defines `Component`, `StyleProps`, callbacks — runtime walks the tree each frame.
- **Themes:** JSON schema `$schema` → `https://nexus.dev/schemas/theme-1.json`. Keys under `imgui.colors` / `imgui.style`. Desktop defaults to `nexus-dark`; Android defaults to `nexus-field`.
- **Script archives:** `ScriptArchive` writes magic headers **LUAC** (`lua.dat`) or **PYAC** (`python.dat`). v2 optional **nxs-v1** stream obfuscation when `nxs_config.json` → `scriptProtection.enabled` is true (salt from generation).
- **Paths:** `Paths.hpp` resolves `misc/lua.dat` and `misc/python.dat` relative to the binary (desktop) or asset roots (Android).
- **Fonts:** Nerd Font optional for icon glyphs in ImGui labels — place `NexusNerdFont-Regular.ttf` in `assets/fonts/`.

## Where to edit

| Change                            | Location                                                       |
|----------------------------------|---------------------------------------------------------------|
| New ImGui widget in DSL           | `dsl/components.ts`, `dsl/tags.ts`, export from `dsl/index.ts` |
| Shared styles / Component API     | `dsl/core.ts`                                                  |
| Dark / light / field theme colors | `themes/nexus-*.json`                                          |
| Theme loader behavior             | `runtime/NexusTheme.*`                                         |
| Font registration / icon glyphs   | `runtime/FontConfig.*`, `assets/fonts/`                        |
| Archive format / encryption       | `runtime/ScriptArchive.*`, `runtime/ScriptCrypto.*`            |
| Pack CLI flags                    | `tools/pack_archive.cpp`                                       |
| Bundled logo                      | `assets/nexus-logo.png`                                        |
## DSL ↔ blueprint ↔ runtime

- Blueprint `ui.page` nodes reference `ui/ui.xhtml` and `ui/ui.ts#<PageClass>` in each app template.
- Controller pattern in app `ui/ui.ts`:

```typescript
counter = state<number>(0);
greeting = native<string>("model.greeting");
increment(): void { this.invoke("nxs.increment"); }
```

- `state()` ↔ two-way `bind=` in XHTML; `native()` ↔ read-only C++ projection; `invoke()` ↔ sol2 controller commands.
- Lua panels (`scripts/panels.lua`) and DSL pages both connect to `cpp.controller` via blueprint `commands` edges.

## Python / Lua (shared concerns)

| Concern        | Desktop                                   | Android                                       |
|---------------|-------------------------------------------|----------------------------------------------|
| Lua pack       | Zig `pack_lua_dat` → `misc/lua.dat`       | Gradle `packLuaDat` → `build/assets/lua.dat`  |
| Python pack    | Zig `pack_python_dat` → `misc/python.dat` | N/A — Chaquopy bundles `app/src/main/python/` |
| Runtime loader | `ScriptArchive` + `Paths` in `runtime/`   | Same C++ runtime; Lua from APK assets         |
| Dev fallback   | Plaintext `scripts/`, `python/` dirs      | Plaintext `scripts/` only                     |
Shared `runtime/` code is target-agnostic; embedding differs per template (`pybind11` vs Chaquopy).

## Do not edit

- **`ScriptProtectionConfig.hpp`** generated from `.hpp.in` at configure time — change the template or `nxs_config.json` salt instead.
- **Vendored SDL / ImGui / sol2 sources** — these live in the parent template `build.zig` dependency trees, not here.
- **Do not add logos or fonts to `.gitignore`** in consumer projects.

## Docs

- [Architecture overview](../../docs/architecture/overview.md) — layers, risk, templates, build orchestration
- [Coding with Nexus](../../docs/guides/coding-with-nexus.md) — UI, MVC, Python, Lua, themes, DSL, coding styles
- [Blueprint schema](../../docs/templates/blueprint-schema.md) — `blueprint.json` + `flows.json` reference
- [Template README](../../template/README.md) — bundled template index
