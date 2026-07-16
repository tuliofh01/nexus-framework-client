//==============================================================================
// nexus.shared.theme — ImGui Style Preset Loader (C++20 Module)
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Loads and applies visual themes (colors, spacing, rounding) to Dear ImGui
// from built-in presets or JSON files stored in assets/themes/. This gives
// your app a polished, consistent look without hardcoding colours everywhere.
//
// Three presets ship with every generated project:
//   • nexus-dark  — dark background, blue accents (desktop default)
//   • nexus-light — light background, blue accents
//   • nexus-field — high-contrast, larger touch targets (Android default)
//
// ════════════════════════════════════════════════════════════════════════════
// ARCHITECTURE ROLE
// ════════════════════════════════════════════════════════════════════════════
//
// This is a shared runtime utility used by both Desktop and Android apps.
// It lives in template/shared/ and gets copied into generated projects.
// The same NexusTheme class works in both environments — only the default
// preset differs (desktop uses nexus-dark, Android uses nexus-field).
//
// ════════════════════════════════════════════════════════════════════════════
// USAGE
// ════════════════════════════════════════════════════════════════════════════
//
//   import nexus.shared.theme;
//   nxs::runtime::NexusTheme::applyFromFile("assets/themes/nexus-dark.json");
//
// ════════════════════════════════════════════════════════════════════════════
// WHY A SINGLE .cppm FILE
// ════════════════════════════════════════════════════════════════════════════
//
// This file is a "module interface unit" that BOTH declares and implements
// the module in one place. There is no separate .cpp file. Everything in
// the global module fragment (before `export module`) is private; everything
// after is the module. Implementation details like applyDark() live in an
// anonymous namespace inside the module — importers cannot reach them.
//
// This replaces the old .hpp + .cpp pair with one self-contained file.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

#include <imgui.h>       // ImGui style API — colours, spacing, rounding
#include <cstdio>        // std::fprintf for error logging
#include <fstream>       // std::ifstream for reading theme JSON files
#include <sstream>       // std::ostringstream for one-line file slurp
#include <string>        // std::string for preset IDs and file paths

export module nexus.shared.theme;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime — ImGui Theme Controller
// ═══════════════════════════════════════════════════════════════════════════
//
// DESIGN NOTE: NexusTheme is a "utility class" with only static methods.
// It is never instantiated. All three public methods are self-contained
// and stateless.
//
// There are two paths to load a theme:
//   1. applyPreset("nexus-dark")  — hardcoded colours, no file I/O
//   2. applyFromFile("path.json") — reads a JSON file, detects preset ID
//
// applyFromConfig() chooses between the two based on nxs_config.json.

namespace nxs::runtime {

// ── Module-internal theme implementations ──
//
// These are in an anonymous namespace so importers cannot access them.
// Each function sets ImGui style properties for one visual theme.

namespace {

void applyDark() noexcept {
    auto& s = ImGui::GetStyle();
    ImGui::StyleColorsDark();
    s.WindowRounding = 6.0f;
    s.FrameRounding = 4.0f;
    s.GrabRounding = 4.0f;
    s.WindowPadding = ImVec2(12.0f, 12.0f);
    s.FramePadding = ImVec2(8.0f, 4.0f);
    s.ItemSpacing = ImVec2(8.0f, 6.0f);
    auto* c = s.Colors;
    c[ImGuiCol_WindowBg] = ImVec4(0.08f, 0.09f, 0.11f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.12f, 0.28f, 0.55f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.18f, 0.38f, 0.62f, 1.0f);
    c[ImGuiCol_ButtonHovered] = ImVec4(0.24f, 0.48f, 0.78f, 1.0f);
    c[ImGuiCol_CheckMark] = ImVec4(0.35f, 0.65f, 0.95f, 1.0f);
}

void applyLight() noexcept {
    auto& s = ImGui::GetStyle();
    ImGui::StyleColorsLight();
    s.WindowRounding = 6.0f;
    s.FrameRounding = 4.0f;
    s.WindowPadding = ImVec2(12.0f, 12.0f);
    auto* c = s.Colors;
    c[ImGuiCol_WindowBg] = ImVec4(0.96f, 0.97f, 0.98f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.20f, 0.45f, 0.72f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.22f, 0.48f, 0.78f, 1.0f);
    c[ImGuiCol_Text] = ImVec4(0.12f, 0.14f, 0.18f, 1.0f);
}

void applyField() noexcept {
    // Nexus-field builds on nexus-dark but enlarges touch targets and
    // boosts contrast for outdoor / high-ambient-light use.
    applyDark();
    auto& s = ImGui::GetStyle();
    s.WindowRounding = 4.0f;
    s.FrameRounding = 6.0f;
    s.WindowPadding = ImVec2(16.0f, 16.0f);
    s.FramePadding = ImVec2(12.0f, 8.0f);
    s.ItemSpacing = ImVec2(12.0f, 10.0f);
    s.ScrollbarSize = 20.0f;
    s.TouchExtraPadding = ImVec2(4.0f, 4.0f);
    auto* c = s.Colors;
    c[ImGuiCol_Text] = ImVec4(1.0f, 1.0f, 1.0f, 1.0f);
    c[ImGuiCol_Border] = ImVec4(0.45f, 0.50f, 0.55f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.10f, 0.55f, 0.65f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.12f, 0.55f, 0.62f, 1.0f);
    c[ImGuiCol_CheckMark] = ImVec4(0.20f, 0.90f, 0.75f, 1.0f);
}

/// Slurp an entire file into a std::string. Returns empty string on failure.
/// [[nodiscard]]: callers must check the result before using it.
[[nodiscard]] auto readFile(const std::string& path) -> std::string {
    auto in = std::ifstream{path};
    if (!in) return {};
    auto ss = std::ostringstream{};
    ss << in.rdbuf();
    return ss.str();
}

/// Lightweight JSON preset ID detector. Scans the file text for one of the
/// known preset strings. Returns "nexus-dark" as the default fallback.
/// This avoids pulling in a full JSON parser just to detect the theme ID.
[[nodiscard]] auto detectPresetId(const std::string& json) -> std::string {
    if (json.find("\"nexus-light\"") != std::string::npos) return "nexus-light";
    if (json.find("\"nexus-field\"") != std::string::npos) return "nexus-field";
    if (json.find("\"nexus-dark\"") != std::string::npos)  return "nexus-dark";
    return "nexus-dark";
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// Public API
// ═══════════════════════════════════════════════════════════════════════════

export struct NexusTheme {
    /// Apply a built-in preset by ID: "nexus-dark", "nexus-light", or "nexus-field".
    /// These are hardcoded colour schemes — no file I/O needed.
    /// noexcept: simple string comparison and style assignment.
    static void applyPreset(const std::string& presetId) noexcept {
        if (presetId == "nexus-light") {
            applyLight();
        } else if (presetId == "nexus-field") {
            applyField();
        } else {
            applyDark();
        }
    }

    /// Load and apply a theme from a JSON file path.
    /// Falls back to "nexus-dark" if the file cannot be read.
    static void applyFromFile(const std::string& path) noexcept {
        const auto json = readFile(path);
        if (json.empty()) {
            std::fprintf(stderr, "NexusTheme: could not read %s — using nexus-dark\n",
                         path.c_str());
            applyDark();
            return;
        }
        applyPreset(detectPresetId(json));
    }

    /// Read the theme name from nxs_config.json or the NXS_THEME env var.
    /// Useful for runtime theme switching. The configTheme parameter
    /// is typically read from nxs_config.json's "theme" field at startup.
    static void applyFromConfig(const std::string& configTheme) noexcept {
        applyPreset(configTheme.empty() ? "nexus-dark" : configTheme);
    }
};

}  // namespace nxs::runtime
