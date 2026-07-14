//==============================================================================
// FontConfig — Nerd Font Loader for ImGui Icon Glyphs (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Loads a JetBrainsMono Nerd Font into ImGui's font atlas so you can render
//   icon glyphs (arrows, checkmarks, gears) alongside regular text in your UI.
//
// C++20 MODULE CONCEPTS:
//   This is a module interface unit (.cppm) that exports the FontConfig struct.
//
//   Notice the forward declaration: `struct ImGuiIO;`
//   This tells the compiler "ImGuiIO exists somewhere" without including the
//   full imgui.h header. This keeps the module interface minimal — the actual
//   imgui.h is only needed in the .cpp implementation file.
//
// NERD FONTS:
//   Nerd Fonts patch popular programming fonts with 3,000+ icon glyphs.
//   The Nexus framework uses them for UI elements like:
//     \uf013  = gear icon (settings)
//     \uf00c  = checkmark (success)
//     \uf075  = chat bubble (messages)
//
//   If the font file is missing, ImGui silently falls back to its built-in
//   monospace font — your app works either way, just without icons.
//
// USAGE PATTERN:
//   import nexus.shared.font_config;
//   nxs::view::FontConfig::loadNerdFont(ImGui::GetIO());
//
//==============================================================================

export module nexus.shared.font_config;

// Forward declaration — we only need to know ImGuiIO exists, not its full definition.
// This is a key technique for keeping module interfaces lightweight.
struct ImGuiIO;

namespace nxs::view {

export struct FontConfig {
    // Default path relative to the executable (desktop) or APK assets root (Android).
    // The template copies this font from shared/assets/fonts/ during project generation.
    static constexpr const char* kNerdFontPath = "assets/fonts/NexusNerdFont-Regular.ttf";

    // Load the Nerd Font and merge it into ImGui's default font atlas.
    // Returns true if the font loaded successfully, false if the file was missing.
    // The font becomes the active font for all subsequent ImGui text rendering.
    static bool loadNerdFont(ImGuiIO& io, const char* path = kNerdFontPath);

    // Scale icons relative to the base font size.
    // Use 1.25f for Android field tablets (larger touch targets), 1.0f for desktop.
    static void setIconScale(float scale);
};

} // namespace nxs::view
