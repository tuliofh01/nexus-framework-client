//==============================================================================
// NexusTheme — ImGui Style Preset Loader (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Loads and applies visual themes (colors, spacing, rounding) to Dear ImGui
//   from JSON files stored in assets/themes/. This gives your app a polished,
//   consistent look without hardcoding colors everywhere.
//
// C++20 MODULE CONCEPTS:
//   This file is a "module interface unit" (.cppm). It defines what symbols
//   are EXPORTED (visible to importers) using the `export` keyword.
//
//   `export module nexus.shared.theme;`  — declares this as module "nexus.shared.theme"
//   `export class NexusTheme`            — makes the class visible to code that does:
//       import nexus.shared.theme;
//
//   Unlike traditional #include headers, modules:
//     • Don't pollute the global namespace
//     • Compile faster (no repeated header parsing)
//     • Enforce clear public/private boundaries
//
// USAGE PATTERN:
//   import nexus.shared.theme;
//   nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-dark.json");
//
// ARCHITECTURE ROLE (MVC):
//   This is a shared runtime utility used by BOTH desktop and Android apps.
//   It lives in template/shared/ and gets copied into generated projects.
//
//==============================================================================

export module nexus.shared.theme;

#include <string>

namespace nxs::runtime {

export class NexusTheme {
public:
    /// Apply a built-in preset by ID: "nexus-dark", "nexus-light", or "nexus-field".
    /// These are hardcoded color schemes that work without any JSON files.
    static void applyPreset(const std::string& presetId);

    /// Load and apply a theme from a specific JSON file path.
    /// The JSON format defines ImGui colors under "imgui.colors" and style under "imgui.style".
    /// Falls back to "nexus-dark" if the file can't be loaded.
    static void applyFromFile(const std::string& path);

    /// Read theme from nxs_config.json or NXS_THEME environment variable.
    /// Default is "nexus-dark" — useful for runtime theme switching.
    static void applyFromConfig(const std::string& configTheme = "nexus-dark");
};

} // namespace nxs::runtime
