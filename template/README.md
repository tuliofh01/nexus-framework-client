# Nexus templates

General-purpose output templates the Framework client copies when you generate a project. Each ships a **minimal starter** (hello + counter) with full Nexus feature support; optional **plotter examples** live under `examples/plotter/`.

| Template | Path | Target |
|----------|------|--------|
| **Desktop App** | [desktop-app/](desktop-app/) | Windows, macOS, Linux — SDL3 + OpenGL, pybind11 |
| **Android App** | [android-app/](android-app/) | APK — SDL3 GLES, Djinni, Chaquopy |
| **Shared** | [shared/](shared/) | DSL, assets, themes, runtime helpers |

## Adoption paths

| Path | What you get |
|------|----------------|
| **Custom code** | Edit `src/`, `ui/`, `scripts/` — omit or disable flows |
| **Blueprint only** | Wire `blueprint.json` nodes; minimal shell compiles |
| **Blueprint + flows** | Add `flows/flows.json` for background/triggered services |

## Blueprint graph

Each app template ships a generic **`blueprint.json`** with all five Langflow-style node types (`python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`). Schema: [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md).

## Optional examples

| Example | Path | Build flag (desktop) |
|---------|------|----------------------|
| Desmos plotter | `desktop-app/examples/plotter/` | `-DBUILD_NEXUS_EXAMPLES=ON` |
| Desmos plotter (Android) | `android-app/examples/plotter/` | reference sources |

## Shared resources

| Resource | Path |
|----------|------|
| Logo & fonts | [shared/assets/](shared/assets/) |
| ImGui themes | [shared/themes/](shared/themes/) |
| TS/XHTML DSL | [shared/dsl/](shared/dsl/) |
| C++ runtime | [shared/runtime/](shared/runtime/) |

Docs: [docs/README.md](../docs/README.md) · Generate: `./gradlew :cli:run --args="generate --type desktop --name MyApp"`
