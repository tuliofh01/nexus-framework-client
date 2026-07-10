# Nerd Font — icon glyphs for ImGui

Generated Nexus apps can show **Nerd Font** icons in ImGui labels (e.g. `\u{f015}` home, `\u{f188}` bug) when a patched font is loaded at startup.

## Why we do not bundle the full TTF

A complete **JetBrainsMono Nerd Font** Regular TTF is ~2–3 MB. To keep template repos lean, the scaffold **documents** the install step instead of committing the binary. Add the font to your generated project before shipping.

## Recommended font

| Font | License | Download |
|------|---------|----------|
| **JetBrainsMono Nerd Font** | [SIL OFL 1.1](https://scripts.sil.org/OFL) | [nerdfonts.com/font-downloads](https://www.nerdfonts.com/font-downloads) → JetBrainsMono |

After download, rename or copy the Regular face to:

```
assets/fonts/NexusNerdFont-Regular.ttf
```

The C++ `FontConfig` stub in `src/view/FontConfig.*` loads this path relative to the executable (desktop) or APK assets (Android).

## CMake / asset copy (desktop)

`CMakeLists.txt` already copies `scripts/`, `python/`, and `ui/`. Extend the POST_BUILD block:

```cmake
add_custom_command(TARGET {{projectName}} POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy_directory
        ${CMAKE_SOURCE_DIR}/assets
        $<TARGET_FILE_DIR:{{projectName}}>/assets)
```

## Android packaging

Place `NexusNerdFont-Regular.ttf` under `app/src/main/assets/fonts/` and load via SDL RWops or a file path your `FontConfig` resolves on device.

## Glyph reference

Use [Nerd Fonts Cheat Sheet](https://www.nerdfonts.com/cheat-sheet) for code points. In C++ / Lua / TS strings, use UTF-8 sequences or `\u{fXXX}` escapes where your layer supports them.

Example ImGui label after font load:

```cpp
ImGui::Text("%s Functions", ICON_NF_MD_CHART_LINE);  // define ICON_* macros or paste UTF-8
```

See [docs/guides/coding-with-nexus.md](../../../../docs/guides/coding-with-nexus.md#nerd-font-icons) for the full workflow.
