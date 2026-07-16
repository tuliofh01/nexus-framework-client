#ifndef NEXUS_SCRIPT_ARCHIVE_HPP
#define NEXUS_SCRIPT_ARCHIVE_HPP

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
    static constexpr uint32_t MAGIC_LUA    = 0x4C554143;
    static constexpr uint32_t MAGIC_PYTHON = 0x50594143;
    static constexpr uint32_t VERSION_V1 = 1;
    static constexpr uint32_t VERSION_V2 = 2;
    static constexpr uint32_t HEADER_SIZE = 32;
    static constexpr uint8_t  FLAG_ENCRYPTED = 0x01;

    explicit ScriptArchive(uint32_t magic) noexcept : magic_{magic} {}

    ScriptArchive(const ScriptArchive&) = default;
    auto operator=(const ScriptArchive&) -> ScriptArchive& = default;
    ScriptArchive(ScriptArchive&&) noexcept = default;
    auto operator=(ScriptArchive&&) noexcept -> ScriptArchive& = default;
    ~ScriptArchive() = default;

    [[nodiscard]] bool load(const std::string& path);
    [[nodiscard]] bool save(const std::string& path) const;

    void add(const std::string& name, const std::string& source);
    [[nodiscard]] bool remove(const std::string& name);
    [[nodiscard]] bool contains(const std::string& name) const;
    [[nodiscard]] bool getSource(const std::string& name, std::string& out) const;
    [[nodiscard]] size_t activeCount() const;

    void setEncryption(bool enabled, const uint8_t key[32]) noexcept;
    [[nodiscard]] bool isEncrypted() const noexcept { return encrypt_; }

    [[nodiscard]] uint32_t magic() const noexcept { return magic_; }
    [[nodiscard]] const std::vector<ScriptEntry>& entries() const noexcept { return entries_; }

private:
    uint32_t magic_{};
    std::vector<ScriptEntry> entries_{};
    bool encrypt_{false};
    std::array<uint8_t, 32> key_{};
    std::array<uint8_t, 16> buildNonce_{};

    static bool readExact(std::ifstream& file, char* buf, size_t n);
    static bool writeExact(std::ofstream& file, const char* buf, size_t n);
};

}  // namespace nxs::runtime

#endif  // NEXUS_SCRIPT_ARCHIVE_HPP
