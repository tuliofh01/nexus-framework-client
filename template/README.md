# Nexus templates

Output templates the Compose Desktop client copies when you generate a project.

| Template | Path | Target |
|----------|------|--------|
| **Desktop App** | [desktop-app/](desktop-app/) | Windows, macOS, Linux — SDL3 + OpenGL, pybind11 Python |
| **Android App** | [android-app/](android-app/) | APK — SDL3 GLES, Djinni, Chaquopy Python |
| **Shared** | [shared/](shared/) | DSL, assets, themes, runtime helpers used by both |

## Shared resources

| Resource | Path |
|----------|------|
| Logo | [shared/assets/nexus-logo.png](shared/assets/nexus-logo.png) |
| Nerd Font setup | [shared/assets/fonts/README.md](shared/assets/fonts/README.md) |
| ImGui themes | [shared/themes/](shared/themes/) — `nexus-dark`, `nexus-light`, `nexus-field` |
| TS/XHTML DSL | [shared/dsl/](shared/dsl/) |
| C++ runtime | [shared/runtime/](shared/runtime/) — `NexusTheme`, `FontConfig` |

Documentation: [docs/README.md](../docs/README.md)
