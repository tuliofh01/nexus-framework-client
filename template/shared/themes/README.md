# ImGui theme presets

JSON themes consumed by `NexusTheme::applyFromFile()` at startup. Schema: `$schema` → `https://nexus.dev/schemas/theme-1.json`.

| File | Use |
|------|-----|
| [nexus-dark.json](nexus-dark.json) | Default — desktop plotter |
| [nexus-light.json](nexus-light.json) | Bright office / demos |
| [nexus-field.json](nexus-field.json) | High-contrast Android field tablets |

Set `"theme": "nexus-dark"` in `nxs_config.json`, or pass a path in `main.cpp`:

```cpp
nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-field.json");
```

Copy a preset and adjust `imgui.colors` / `imgui.style`. See [docs/guides/coding-with-nexus.md](../../../docs/guides/coding-with-nexus.md#themes).
