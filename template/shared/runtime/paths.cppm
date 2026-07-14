//==============================================================================
// Paths — Runtime Path Resolution (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Provides a centralized way to find important files (lua.dat, python.dat)
//   relative to where the executable is running. Both desktop and Android apps
//   use this to locate their bundled script archives.
//
// C++20 MODULE CONCEPTS:
//   This module exports a struct with inline function definitions directly
//   in the module interface. This is valid for small, performance-critical
//   functions — the compiler can inline them at the call site.
//
//   `[[nodiscard]]` attribute tells the compiler to warn if the caller
//   ignores the return value. This catches bugs like:
//     Paths::luaArchive();  // WARNING: you probably wanted to store this!
//
// FILE LAYOUT IN GENERATED PROJECTS:
//   builds/framework/<name>/
//   ├── misc/
//   │   ├── lua.dat        ← ScriptArchive for Lua panels
//   │   └── python.dat     ← ScriptArchive for Python functions
//   └── zig-out/bin/<name>  ← The executable
//
//   The executable lives in zig-out/bin/ but misc/ is at the project root,
//   so we navigate up using current_path() + "/misc".
//
//==============================================================================

export module nexus.shared.paths;

#include <filesystem>
#include <string>

namespace nxs::runtime {

export struct Paths {
    // Returns the current working directory — typically where the executable lives.
    // On desktop, this is the directory you run the app from.
    // On Android, this is the APK's internal storage root.
    [[nodiscard]] static auto executableDir() -> std::string {
        return std::filesystem::current_path().string();
    }

    // The "misc" directory contains packed script archives (lua.dat, python.dat).
    // These are binary bundles created by the pack_archive tool during build.
    [[nodiscard]] static auto miscDir() -> std::string {
        return executableDir() + "/misc";
    }

    // Path to the packed Lua script archive.
    // Contains all .lua files merged into a single binary with a LUAC magic header.
    // LuaPanels loads this at startup to register UI panels and hotkeys.
    [[nodiscard]] static auto luaArchive() -> std::string {
        return miscDir() + "/lua.dat";
    }

    // Path to the packed Python script archive.
    // Contains all .py files merged into a single binary with a PYAC magic header.
    // PythonEngine loads this to import the functions module at startup.
    [[nodiscard]] static auto pythonArchive() -> std::string {
        return miscDir() + "/python.dat";
    }
};

} // namespace nxs::runtime
