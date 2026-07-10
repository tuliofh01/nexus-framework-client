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

std::string extractJsonString(const std::string& json, const std::string& key) {
    const std::string needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return "";
    pos = json.find(':', pos);
    if (pos == std::string::npos) return "";
    pos = json.find('"', pos);
    if (pos == std::string::npos) return "";
    auto end = json.find('"', pos + 1);
    if (end == std::string::npos) return "";
    return json.substr(pos + 1, end - pos - 1);
}

bool extractJsonBool(const std::string& json, const std::string& key, bool defaultValue) {
    const std::string needle = "\"" + key + "\"";
    auto pos = json.find(needle);
    if (pos == std::string::npos) return defaultValue;
    pos = json.find(':', pos);
    if (pos == std::string::npos) return defaultValue;
    auto tail = json.substr(pos + 1);
    if (tail.find("true") == 0 || tail.find(" true") == 0) return true;
    if (tail.find("false") == 0 || tail.find(" false") == 0) return false;
    return defaultValue;
}

ProtectionConfig loadProtectionConfig(const std::string& path) {
    ProtectionConfig cfg;
    std::ifstream file(path);
    if (!file.is_open()) return cfg;
    std::stringstream ss;
    ss << file.rdbuf();
    const std::string json = ss.str();

    cfg.projectName = extractJsonString(json, "name");
    if (cfg.projectName.empty()) {
        cfg.projectName = extractJsonString(json, "projectName");
    }
    cfg.salt = extractJsonString(json, "salt");
    cfg.createdAt = extractJsonString(json, "createdAt");

    auto blockPos = json.find("\"scriptProtection\"");
    if (blockPos != std::string::npos) {
        auto block = json.substr(blockPos);
        cfg.enabled = extractJsonBool(block, "enabled", false);
        const auto saltInBlock = extractJsonString(block, "salt");
        if (!saltInBlock.empty()) cfg.salt = saltInBlock;
    }

    return cfg;
}

uint32_t magicForKind(const std::string& kind) {
    if (kind == "lua") return nxs::runtime::ScriptArchive::MAGIC_LUA;
    if (kind == "python") return nxs::runtime::ScriptArchive::MAGIC_PYTHON;
    return 0;
}

std::string extensionForKind(const std::string& kind) {
    if (kind == "lua") return ".lua";
    if (kind == "python") return ".py";
    return "";
}

}  // namespace

int main(int argc, char** argv) {
    if (argc < 4 || argc > 5) {
        std::cerr << "Usage: pack_archive <lua|python> <input_dir> <output_file> [nxs_config.json]\n";
        return 1;
    }

    const std::string kind = argv[1];
    const std::string inputDir = argv[2];
    const std::string outputFile = argv[3];
    const uint32_t magic = magicForKind(kind);
    const std::string ext = extensionForKind(kind);

    if (magic == 0 || ext.empty()) {
        std::cerr << "Error: kind must be 'lua' or 'python'\n";
        return 1;
    }

    if (!fs::is_directory(inputDir)) {
        std::cerr << "Error: " << inputDir << " is not a directory\n";
        return 1;
    }

    nxs::runtime::ScriptArchive archive(magic);
    ProtectionConfig protection;

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

    std::vector<fs::path> files;
    for (const auto& entry : fs::recursive_directory_iterator(inputDir)) {
        if (entry.is_regular_file() && entry.path().extension() == ext) {
            files.push_back(entry.path());
        }
    }

    std::sort(files.begin(), files.end());

    for (const auto& path : files) {
        std::ifstream file(path);
        if (!file.is_open()) {
            std::cerr << "  Warning: could not open " << path.filename() << "\n";
            continue;
        }
        std::stringstream ss;
        ss << file.rdbuf();
        std::string source = ss.str();

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
