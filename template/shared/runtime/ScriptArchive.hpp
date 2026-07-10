#pragma once

// Binary archive of Lua or Python script sources.
// Magic LUAC (lua.dat) or PYAC (python.dat); v2 supports optional nxs-v1 encryption.

#include <array>
#include <cstdint>
#include <fstream>
#include <string>
#include <vector>

namespace nxs::runtime {

struct ScriptEntry {
    bool active = true;
    std::string name;
    std::string source;
};

class ScriptArchive {
public:
    static constexpr uint32_t MAGIC_LUA = 0x4C554143;     // "LUAC"
    static constexpr uint32_t MAGIC_PYTHON = 0x50594143;  // "PYAC"
    static constexpr uint32_t VERSION_V1 = 1;
    static constexpr uint32_t VERSION_V2 = 2;
    static constexpr uint32_t HEADER_SIZE = 32;
    static constexpr uint8_t FLAG_ENCRYPTED = 0x01;

    explicit ScriptArchive(uint32_t magic) : magic_(magic) {}

    bool load(const std::string& path);
    bool save(const std::string& path) const;

    void add(const std::string& name, const std::string& source);
    bool remove(const std::string& name);
    bool contains(const std::string& name) const;
    bool getSource(const std::string& name, std::string& out) const;
    size_t activeCount() const;

    void setEncryption(bool enabled, const uint8_t key[32]);
    bool isEncrypted() const { return encrypt_; }
    uint32_t magic() const { return magic_; }

    const std::vector<ScriptEntry>& entries() const { return entries_; }

private:
    uint32_t magic_;
    std::vector<ScriptEntry> entries_;
    bool encrypt_ = false;
    std::array<uint8_t, 32> key_{};
    std::array<uint8_t, 16> buildNonce_{};

    static bool readExact(std::ifstream& file, char* buf, size_t n);
    static bool writeExact(std::ofstream& file, const char* buf, size_t n);
};

}  // namespace nxs::runtime
