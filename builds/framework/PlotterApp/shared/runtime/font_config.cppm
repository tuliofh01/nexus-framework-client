//==============================================================================
// nexus.shared.font_config — Nerd Font Loader for ImGui Icon Glyphs
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Loads a JetBrainsMono Nerd Font into ImGui's font atlas so your UI can
// render icon glyphs (gears, checkmarks, arrows) alongside regular text.
// Nerd Fonts patch popular programming fonts with 3,000+ icon glyphs from
// Font Awesome, Material Design, Octicons, and more.
//
// If the font file is missing, ImGui silently falls back to its built-in
// monospace font — your app works either way, just without icons.
//
// ════════════════════════════════════════════════════════════════════════════
// WHY A SEPARATE MODULE
// ════════════════════════════════════════════════════════════════════════════
//
// Font setup is a cross-cutting concern used by both Desktop and Android
// templates. Keeping it as a shared module means both generated apps share
// exactly the same font-loading code — one code path, one place to fix.
//
// ════════════════════════════════════════════════════════════════════════════
// USAGE
// ════════════════════════════════════════════════════════════════════════════
//
//   import nexus.shared.font_config;
//   nxs::view::FontConfig::loadNerdFont(ImGui::GetIO());
//
// The call is a no-op if the .ttf file is absent.
//
// ════════════════════════════════════════════════════════════════════════════
// C++20 MODULE STRUCTURE (for readers new to modules)
// ════════════════════════════════════════════════════════════════════════════
//
// This file is a "module interface unit" (.cppm) that BOTH declares and
// implements the module in one place. There is no separate .cpp file.
//
//   • Everything in the global module fragment (before `export module`)
//     is private — importers never see it.
//
//   • Everything after `export module` and marked `export` is the public API.
//
//   • Implementation details can live in anonymous namespaces (`namespace {}`)
//     inside the module — importers cannot access them.
//
// This eliminates the traditional .hpp (declaration) + .cpp (implementation)
// split. One file, one module, one place to edit.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// ── Third-party headers ──
// These are in the global fragment so importers never see ImGui headers.
#include <imgui.h>
#include <cstdio>

export module nexus.shared.font_config;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::view — Nerd Font Configuration
// ═══════════════════════════════════════════════════════════════════════════
//
// DESIGN NOTE: FontConfig is a "utility struct" — it exports only static
// methods and is never instantiated. All state is kept in the module-level
// anonymous namespace so importers cannot access it directly.
//
// Thread-safety: Nexus apps are single-threaded (ImGui frame loop on the
// main thread). g_iconScale is set once at startup and read-only thereafter.

namespace nxs::view {

// ── Module-internal state (invisible to importers) ──
namespace {
    /// Icon scale factor. Set by setIconScale() at startup.
    /// 1.0f = desktop (standard size), 1.25f = Android tablets (larger touch targets).
    float g_iconScale = 1.0f;
}

export struct FontConfig {
    // ── Constants ───────────────────────────────────────────────────────
    //
    // kNerdFontPath is a constexpr string literal — the compiler evaluates
    // it at compile time. Zero runtime cost. The path is relative to the
    // executable (desktop) or APK assets root (Android).

    static constexpr const char* kNerdFontPath = "assets/fonts/NexusNerdFont-Regular.ttf";

    // ── Public API ──────────────────────────────────────────────────────
    //
    // loadNerdFont: finds and merges the Nerd Font .ttf into ImGui's font
    // atlas. Returns bool so the caller can decide whether to show icon-less
    // fallback UI. noexcept because font loading is best-effort — we log
    // the error and return false rather than throwing.

    [[nodiscard]] static bool loadNerdFont(ImGuiIO& io, const char* path = kNerdFontPath) noexcept {
        // Nerd Fonts place icons in the Unicode Private Use Area (PUA):
        //   0xE000 — 0xF8FF
        // These ranges encode Font Awesome, Devicons, Octicons, and more.
        // By merging them into the default font with cfg.MergeMode = true,
        // UTF-8 icon literals like "\uf013" render as actual gear glyphs.
        static constexpr ImWchar iconRanges[] = {0xE000, 0xF8FF, 0};

        auto cfg = ImFontConfig{};
        cfg.MergeMode = true;
        cfg.PixelSnapH = true;
        cfg.GlyphMinAdvanceX = 13.0f * g_iconScale;

        auto* font = io.Fonts->AddFontFromFileTTF(path, 16.0f * g_iconScale, &cfg, iconRanges);
        if (!font) {
            std::fprintf(stderr,
                "FontConfig: Nerd Font not found at %s — using default ImGui font. "
                "See assets/fonts/README.md\n", path);
            return false;
        }
        return true;
    }

    /// Adjust icon scaling for different screen densities.
    /// noexcept: trivial setter, never throws.
    static void setIconScale(float scale) noexcept {
        g_iconScale = scale > 0.0f ? scale : 1.0f;
    }
};

}  // namespace nxs::view
