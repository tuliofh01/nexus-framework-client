# Nexus templates

Output templates the Framework client copies when you generate a project.

| Template | Path | Target |
|----------|------|--------|
| **Desktop App** | [desktop-app/](desktop-app/) | Windows, macOS, Linux — SDL3 + OpenGL, pybind11 |
| **Android App** | [android-app/](android-app/) | APK — SDL3 GLES, Djinni, Chaquopy |
| **Shared** | [shared/](shared/) | DSL, assets, themes, runtime helpers |

## Shared resources

| Resource | Path |
|----------|------|
| Logo & fonts | [shared/assets/](shared/assets/) |
| ImGui themes | [shared/themes/](shared/themes/) — `nexus-dark`, `nexus-light`, `nexus-field` |
| TS/XHTML DSL | [shared/dsl/](shared/dsl/) |
| C++ runtime | [shared/runtime/](shared/runtime/) — `NexusTheme`, `FontConfig` |

Docs: [docs/README.md](../docs/README.md) · Generate: `./gradlew :cli:run --args="generate --type desktop --name MyApp"`
