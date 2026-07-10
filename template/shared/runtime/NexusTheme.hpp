// NexusTheme — applies ImGui style presets from shared theme JSON files.
// JSON files live in template/shared/themes/ and are copied to assets/themes/
// in generated projects. This loader recognizes preset ids and applies matching
// ImGui colors/style; extend parseThemeJson() for custom keys.
#pragma once

#include <string>

namespace nxs::runtime {

class NexusTheme {
public:
    // Apply a built-in preset id: "nexus-dark" | "nexus-light" | "nexus-field"
    static void applyPreset(const std::string& presetId);

    // Load assets/themes/<name>.json or an explicit path; falls back to nexus-dark.
    static void applyFromFile(const std::string& path);

    // Read NXS_THEME env var or nxs_config.json "theme" field (stub: defaults dark).
    static void applyFromConfig(const std::string& configTheme = "nexus-dark");
};

}  // namespace nxs::runtime
