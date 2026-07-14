module nexus.shared.theme;

#include <imgui.h>

#include <cstdio>
#include <fstream>
#include <sstream>
#include <string>

namespace nxs::runtime {
namespace {

void applyDark() {
    ImGuiStyle& s = ImGui::GetStyle();
    ImGui::StyleColorsDark();
    s.WindowRounding = 6.0f;
    s.FrameRounding = 4.0f;
    s.GrabRounding = 4.0f;
    s.WindowPadding = ImVec2(12.0f, 12.0f);
    s.FramePadding = ImVec2(8.0f, 4.0f);
    s.ItemSpacing = ImVec2(8.0f, 6.0f);
    ImVec4* c = s.Colors;
    c[ImGuiCol_WindowBg] = ImVec4(0.08f, 0.09f, 0.11f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.12f, 0.28f, 0.55f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.18f, 0.38f, 0.62f, 1.0f);
    c[ImGuiCol_ButtonHovered] = ImVec4(0.24f, 0.48f, 0.78f, 1.0f);
    c[ImGuiCol_CheckMark] = ImVec4(0.35f, 0.65f, 0.95f, 1.0f);
}

void applyLight() {
    ImGuiStyle& s = ImGui::GetStyle();
    ImGui::StyleColorsLight();
    s.WindowRounding = 6.0f;
    s.FrameRounding = 4.0f;
    s.WindowPadding = ImVec2(12.0f, 12.0f);
    ImVec4* c = s.Colors;
    c[ImGuiCol_WindowBg] = ImVec4(0.96f, 0.97f, 0.98f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.20f, 0.45f, 0.72f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.22f, 0.48f, 0.78f, 1.0f);
    c[ImGuiCol_Text] = ImVec4(0.12f, 0.14f, 0.18f, 1.0f);
}

void applyField() {
    applyDark();
    ImGuiStyle& s = ImGui::GetStyle();
    s.WindowRounding = 4.0f;
    s.FrameRounding = 6.0f;
    s.WindowPadding = ImVec2(16.0f, 16.0f);
    s.FramePadding = ImVec2(12.0f, 8.0f);
    s.ItemSpacing = ImVec2(12.0f, 10.0f);
    s.ScrollbarSize = 20.0f;
    s.TouchExtraPadding = ImVec2(4.0f, 4.0f);
    ImVec4* c = s.Colors;
    c[ImGuiCol_Text] = ImVec4(1.0f, 1.0f, 1.0f, 1.0f);
    c[ImGuiCol_Border] = ImVec4(0.45f, 0.50f, 0.55f, 1.0f);
    c[ImGuiCol_TitleBgActive] = ImVec4(0.10f, 0.55f, 0.65f, 1.0f);
    c[ImGuiCol_Button] = ImVec4(0.12f, 0.55f, 0.62f, 1.0f);
    c[ImGuiCol_CheckMark] = ImVec4(0.20f, 0.90f, 0.75f, 1.0f);
}

std::string readFile(const std::string& path) {
    std::ifstream in(path);
    if (!in) return {};
    std::ostringstream ss;
    ss << in.rdbuf();
    return ss.str();
}

std::string detectPresetId(const std::string& json) {
    if (json.find("\"nexus-light\"") != std::string::npos) return "nexus-light";
    if (json.find("\"nexus-field\"") != std::string::npos) return "nexus-field";
    if (json.find("\"nexus-dark\"") != std::string::npos)  return "nexus-dark";
    return "nexus-dark";
}

} // anonymous namespace

void NexusTheme::applyPreset(const std::string& presetId) {
    if (presetId == "nexus-light") { applyLight(); }
    else if (presetId == "nexus-field") { applyField(); }
    else { applyDark(); }
}

void NexusTheme::applyFromFile(const std::string& path) {
    const std::string json = readFile(path);
    if (json.empty()) {
        std::fprintf(stderr, "NexusTheme: could not read %s — using nexus-dark\n", path.c_str());
        applyDark();
        return;
    }
    applyPreset(detectPresetId(json));
}

void NexusTheme::applyFromConfig(const std::string& configTheme) {
    applyPreset(configTheme.empty() ? "nexus-dark" : configTheme);
}

} // namespace nxs::runtime
