// FontConfig — optional Nerd Font loading for ImGui icon glyphs.
//
// Place JetBrainsMono Nerd Font at assets/fonts/NexusNerdFont-Regular.ttf
// (see template/shared/assets/fonts/README.md). If the file is missing,
// ImGui falls back to the default embedded font.
#pragma once

struct ImGuiIO;

namespace nxs::view {

struct FontConfig {
    // Default path relative to the executable / APK assets root.
    static constexpr const char* kNerdFontPath = "assets/fonts/NexusNerdFont-Regular.ttf";

    // Load Nerd Font merged into the default atlas. Returns true when loaded.
    static bool loadNerdFont(ImGuiIO& io, const char* path = kNerdFontPath);

    // Icon size multiplier vs default font (useful for field-tablet theme).
    static void setIconScale(float scale);
};

}  // namespace nxs::view
