# ImGui theme presets

JSON theme files consumed by `NexusTheme::applyFromFile()` at app startup. Each file uses the Nexus theme schema (`$schema`: `https://nexus.dev/schemas/theme-1.json`) with an `imgui` object mapping to Dear ImGui style vars and `ImGuiCol_*` entries.

| File | Use |
|------|-----|
| [nexus-dark.json](nexus-dark.json) | Default — matches scaffold branding and desktop plotter |
| [nexus-light.json](nexus-light.json) | Bright office / demo screenshots |
| [nexus-field.json](nexus-field.json) | High-contrast Android field tablets and outdoor kiosks |

## Selecting a theme

Set `theme` in `nxs_config.json` (generated projects):

```json
{
  "theme": "nexus-dark"
}
```

Or pass an explicit path at startup in `main.cpp`:

```cpp
nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-field.json");
```

## Extending

Copy a preset and adjust `imgui.colors` RGBA tuples (0–1 floats) and `imgui.style` keys. Color names match ImGui enum names without the `ImGuiCol_` prefix. See [docs/guides/coding-with-nexus.md](../../../docs/guides/coding-with-nexus.md#themes).
