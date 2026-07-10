# AGENTS.md — Nexus Android Template

Guide for AI coding assistants working in a **generated** Nexus Android project from `template/android-app/`.

## What this template is

A **general-purpose** SDL3 + ImGui starter (hello + counter) with Chaquopy Python, Djinni `AppCore` bridge, Lua panels, TS/XHTML DSL, `blueprint.json`, and optional `flows/flows.json`.

Optional **plotter sample** under `examples/plotter/`.

## Key directories

| Path | Role |
|------|------|
| `app/src/main/java/com/nexus/{{packageName}}/` | Kotlin (`MainActivity`, `ChaquopyPythonBridge`) |
| `src/` | C++ MVC (`AppModel`, `AppController`, `AppView`) |
| `djinni/app.djinni` | C++↔JVM IDL |
| `djinni-generated/` | Committed Djinni stubs |
| `app/src/main/python/` | `helpers.py` (Chaquopy) |
| `examples/plotter/` | Optional Desmos-style sample |

## Package

Application ID: `com.nexus.{{packageName}}` (derived from project name at generation).

## Docs

- [docs/templates/android-app.md](../../docs/templates/android-app.md)
- [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)
