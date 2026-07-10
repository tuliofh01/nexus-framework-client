#include "FontConfig.hpp"

#include <imgui.h>

#include <cstdio>

namespace nxs::view {

namespace {
float g_iconScale = 1.0f;
}

bool FontConfig::loadNerdFont(ImGuiIO& io, const char* path) {
    // Nerd Fonts patch the Private Use Area — merge into default font so
    // UTF-8 icon literals render in labels and buttons.
    static const ImWchar iconRanges[] = {0xE000, 0xF8FF, 0};

    ImFontConfig cfg;
    cfg.MergeMode = true;
    cfg.PixelSnapH = true;
    cfg.GlyphMinAdvanceX = 13.0f * g_iconScale;

    ImFont* font = io.Fonts->AddFontFromFileTTF(path, 16.0f * g_iconScale, &cfg, iconRanges);
    if (!font) {
        std::fprintf(stderr,
                     "FontConfig: Nerd Font not found at %s — using default ImGui font. "
                     "See assets/fonts/README.md\n",
                     path);
        return false;
    }
    return true;
}

void FontConfig::setIconScale(float scale) {
    g_iconScale = scale > 0.0f ? scale : 1.0f;
}

}  // namespace nxs::view
