#pragma once

#include <filesystem>
#include <string>

namespace nxs::runtime {

struct Paths {
    static std::string executableDir() {
        return std::filesystem::current_path().string();
    }

    static std::string miscDir() { return executableDir() + "/misc"; }

    static std::string luaArchive() { return miscDir() + "/lua.dat"; }

    static std::string pythonArchive() { return miscDir() + "/python.dat"; }
};

}  // namespace nxs::runtime
