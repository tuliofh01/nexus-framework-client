#pragma once

#include <filesystem>
#include <string>

namespace nxs::runtime {

struct Paths {
    [[nodiscard]] static auto executableDir() -> std::string {
        return std::filesystem::current_path().string();
    }

    [[nodiscard]] static auto miscDir() -> std::string {
        return executableDir() + "/misc";
    }

    [[nodiscard]] static auto luaArchive() -> std::string {
        return miscDir() + "/lua.dat";
    }

    [[nodiscard]] static auto pythonArchive() -> std::string {
        return miscDir() + "/python.dat";
    }
};

}  // namespace nxs::runtime
