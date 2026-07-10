# Nexus templates

Output templates the Framework client copies when you generate a project.

| Template | Path | Target |
|----------|------|--------|
| **Desktop App** | [desktop-app/](desktop-app/) | Windows, macOS, Linux — SDL3 + OpenGL, pybind11 |
| **Android App** | [android-app/](android-app/) | APK — SDL3 GLES, Djinni, Chaquopy |
| **Shared** | [shared/](shared/) | DSL, assets, themes, runtime helpers |

## Blueprint graph

Each app template ships a sample **`blueprint.json`** with all five Langflow-style node types (`python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script`). The generator validates the graph at emit time; edit it in the Compose client before generating. Schema: [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md).

## Optional runtime flows

Each app template may include **`flows/flows.json`** with sample background and triggered flows. Disable by deleting the file or setting `"flows": { "enabled": false }` in `nxs_config.json`. Schema: [docs/templates/flows-schema.md](../docs/templates/flows-schema.md).

## Shared resources

| Resource | Path |
|----------|------|
| Logo & fonts | [shared/assets/](shared/assets/) |
| ImGui themes | [shared/themes/](shared/themes/) — `nexus-dark`, `nexus-light`, `nexus-field` |
| TS/XHTML DSL | [shared/dsl/](shared/dsl/) |
| C++ runtime | [shared/runtime/](shared/runtime/) — `NexusTheme`, `FontConfig` |

Docs: [docs/README.md](../docs/README.md) · Generate: `./gradlew :cli:run --args="generate --type desktop --name MyApp"`
