#include "../runtime/ScriptArchive.hpp"
#include "../runtime/ScriptCrypto.hpp"

#include <algorithm>
#include <filesystem>
#include <fstream>
#include <iostream>
#include <sstream>
#include <string>
#include <vector>

namespace fs = std::filesystem;

namespace {

struct ProtectionConfig {
    bool enabled = false;
    std::string projectName;
    std::string salt;
    std::string createdAt;
};

[[nodiscard]] auto extractJsonString(const std::string& json, const std::string_view key) -> std::string {
    const auto needle = std::string{"\""} + key.data() + "\"";
    const auto pos = json.find(needle);
    if (pos == std::string::npos) return {};
    const auto colon = json.find(':', pos);
    if (colon == std::string::npos) return {};
    const auto quote = json.find('"', colon);
    if (quote == std::string::npos) return {};
    const auto end = json.find('"', quote + 1);
    if (end == std::string::npos) return {};
    return json.substr(quote + 1, end - quote - 1);
}

[[nodiscard]] auto extractJsonBool(const std::string& json, const std::string_view key, bool defaultValue) -> bool {
    const auto needle = std::string{"\""} + key.data() + "\"";
    const auto pos = json.find(needle);
    if (pos == std::string::npos) return defaultValue;
    const auto colon = json.find(':', pos);
    if (colon == std::string::npos) return defaultValue;
    const auto tail = json.substr(colon + 1);
    if (tail.find("true") == 0 || tail.find(" true") == 0 || tail.find("true ") != std::string::npos) return true;
    if (tail.find("false") == 0 || tail.find(" false") == 0) return false;
    return defaultValue;
}

[[nodiscard]] auto loadProtectionConfig(const std::string& path) -> ProtectionConfig {
    auto cfg = ProtectionConfig{};
    auto file = std::ifstream{path};
    if (!file.is_open()) return cfg;
    auto ss = std::stringstream{};
    ss << file.rdbuf();
    const auto json = ss.str();

    cfg.projectName = extractJsonString(json, "name");
    if (cfg.projectName.empty()) {
        cfg.projectName = extractJsonString(json, "projectName");
    }
    cfg.salt = extractJsonString(json, "salt");
    cfg.createdAt = extractJsonString(json, "createdAt");

    const auto blockPos = json.find("\"scriptProtection\"");
    if (blockPos != std::string::npos) {
        const auto block = json.substr(blockPos);
        cfg.enabled = extractJsonBool(block, "enabled", false);
        const auto saltInBlock = extractJsonString(block, "salt");
        if (!saltInBlock.empty()) cfg.salt = saltInBlock;
    }

    return cfg;
}

[[nodiscard]] auto magicForKind(const std::string_view kind) -> uint32_t {
    if (kind == "lua") return nxs::runtime::ScriptArchive::MAGIC_LUA;
    if (kind == "python") return nxs::runtime::ScriptArchive::MAGIC_PYTHON;
    return 0;
}

[[nodiscard]] auto extensionForKind(const std::string_view kind) -> std::string {
    if (kind == "lua") return ".lua";
    if (kind == "python") return ".py";
    return {};
}

}  // namespace

auto main(int argc, char** argv) -> int {
    if (argc < 4 || argc > 5) {
        std::cerr << "Usage: pack_archive <lua|python> <input_dir> <output_file> [nxs_config.json]\n";
        return 1;
    }

    const auto kind = std::string_view{argv[1]};
    const auto inputDir = std::string{argv[2]};
    const auto outputFile = std::string{argv[3]};
    const auto magic = magicForKind(kind);
    const auto ext = extensionForKind(kind);

    if (magic == 0 || ext.empty()) {
        std::cerr << "Error: kind must be 'lua' or 'python'\n";
        return 1;
    }

    if (!fs::is_directory(inputDir)) {
        std::cerr << "Error: " << inputDir << " is not a directory\n";
        return 1;
    }

    auto archive = nxs::runtime::ScriptArchive{magic};
    auto protection = ProtectionConfig{};

    if (argc == 5) {
        protection = loadProtectionConfig(argv[4]);
        if (protection.enabled && !protection.projectName.empty() && !protection.salt.empty() &&
            !protection.createdAt.empty()) {
            auto key = nxs::runtime::ScriptCrypto::deriveKey(
                protection.projectName, protection.salt, protection.createdAt);
            archive.setEncryption(true, key.data());
            std::cout << "  Script protection: enabled (nxs-v1)\n";
        }
    }

    auto files = std::vector<fs::path>{};
    for (const auto& entry : fs::recursive_directory_iterator(inputDir)) {
        if (entry.is_regular_file() && entry.path().extension() == ext) {
            files.push_back(entry.path());
        }
    }

    std::sort(files.begin(), files.end());

    for (const auto& path : files) {
        auto file = std::ifstream{path};
        if (!file.is_open()) {
            std::cerr << "  Warning: could not open " << path.filename() << "\n";
            continue;
        }
        auto ss = std::stringstream{};
        ss << file.rdbuf();
        auto source = ss.str();

        auto rel = fs::relative(path, inputDir).replace_extension("").string();
        for (auto& ch : rel) {
            if (ch == '/' || ch == '\\') ch = '.';
        }
        archive.add(rel, source);
        std::cout << "  Packed: " << rel << " (" << source.size() << " bytes)\n";
    }

    if (archive.activeCount() == 0) {
        std::cerr << "Error: no " << ext << " files found in " << inputDir << "\n";
        return 1;
    }

    if (archive.save(outputFile)) {
        std::cout << "Wrote " << archive.activeCount() << " scripts to " << outputFile;
        if (archive.isEncrypted()) std::cout << " (encrypted v2)";
        std::cout << "\n";
        return 0;
    }

    std::cerr << "Error: failed to write " << outputFile << "\n";
    return 1;
}
