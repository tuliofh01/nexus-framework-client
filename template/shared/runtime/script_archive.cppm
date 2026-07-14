//==============================================================================
// ScriptArchive — Binary Pack Format for Lua/Python Scripts (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Manages packed script archives — binary files that bundle multiple Lua or
//   Python source files into a single distributable file (lua.dat or python.dat).
//   This is how the Nexus framework ships script code inside the app binary
//   without leaving plain-text .lua/.py files on disk.
//
// ARCHIVE FORMAT (v2):
//   ┌─────────────────────────────────────────────────┐
//   │ Header (32 bytes)                               │
//   │   magic:     4 bytes ("LUAC" or "PYAC")        │
//   │   version:   4 bytes (1 = plain, 2 = encrypted)│
//   │   flags:     4 bytes (bit 0 = encrypted)       │
//   │   count:     4 bytes (number of entries)        │
//   │   reserved:  16 bytes (nonce for encryption)    │
//   ├─────────────────────────────────────────────────┤
//   │ Entry 0: [name_len][name_bytes][src_len][src]   │
//   │ Entry 1: [name_len][name_bytes][src_len][src]   │
//   │ ...                                             │
//   └─────────────────────────────────────────────────┘
//
//   When encrypted (version 2), each entry's source bytes are XOR-encrypted
//   with a key derived from project metadata (see ScriptCrypto).
//
// WHY THIS FORMAT:
//   • Simple to parse (no JSON/XML dependency in the runtime)
//   • Fast to load (single file read, no filesystem traversal)
//   • Supports optional encryption (ScriptProtection feature)
//   • Works on both desktop and Android
//
// MAGIC NUMBERS:
//   0x4C554143 = "LUAC" (Lua Archive)
//   0x50594143 = "PYAC" (Python Archive)
//   These are ASCII "LUAC" and "PYAC" stored as big-endian 32-bit integers.
//
//==============================================================================

export module nexus.shared.script_archive;

#include <array>
#include <cstdint>
#include <fstream>
#include <string>
#include <vector>

namespace nxs::runtime {

// Represents a single script file within the archive.
// The `active` flag allows disabling entries without removing them.
struct ScriptEntry {
    bool active = true;
    std::string name;     // Module name (e.g., "panels", "functions")
    std::string source;   // Raw source code content
};

export class ScriptArchive {
public:
    // Magic number constants — identify the archive type
    static constexpr uint32_t MAGIC_LUA    = 0x4C554143;  // "LUAC" — Lua archive
    static constexpr uint32_t MAGIC_PYTHON = 0x50594143;  // "PYAC" — Python archive

    // Version constants — format evolution
    static constexpr uint32_t VERSION_V1 = 1;  // Plain text, no encryption
    static constexpr uint32_t VERSION_V2 = 2;  // Optional XOR encryption

    static constexpr uint32_t HEADER_SIZE = 32;   // Fixed header size in bytes
    static constexpr uint8_t  FLAG_ENCRYPTED = 0x01;  // Bit 0 of flags field

    /// Construct an archive for a specific script type (MAGIC_LUA or MAGIC_PYTHON).
    explicit ScriptArchive(uint32_t magic) : magic_(magic) {}

    /// Load an archive from disk. Returns false if the file doesn't exist,
    /// has an invalid magic number, or is corrupted.
    [[nodiscard]] bool load(const std::string& path);

    /// Save the archive to disk. Creates the file or overwrites it completely.
    [[nodiscard]] bool save(const std::string& path) const;

    /// Add a script entry to the archive. If an entry with the same name exists,
    /// it gets replaced.
    void add(const std::string& name, const std::string& source);

    /// Remove an entry by name. Returns false if the entry wasn't found.
    bool remove(const std::string& name);

    /// Check if an entry with the given name exists in the archive.
    [[nodiscard]] bool contains(const std::string& name) const;

    /// Retrieve the source code for a named entry. Returns false if not found.
    [[nodiscard]] bool getSource(const std::string& name, std::string& out) const;

    /// How many entries are currently active (not removed/disabled).
    [[nodiscard]] size_t activeCount() const;

    /// Enable XOR encryption for this archive. The key is derived from project
    /// metadata (see ScriptCrypto::deriveKey). Must be called before save().
    void setEncryption(bool enabled, const uint8_t key[32]);

    /// Check if encryption is enabled for this archive.
    [[nodiscard]] bool isEncrypted() const { return encrypt_; }

    /// Get the magic number (MAGIC_LUA or MAGIC_PYTHON).
    [[nodiscard]] uint32_t magic() const { return magic_; }

    /// Access all entries (for iteration during save).
    [[nodiscard]] const std::vector<ScriptEntry>& entries() const { return entries_; }

private:
    uint32_t magic_;
    std::vector<ScriptEntry> entries_;
    bool encrypt_ = false;
    std::array<uint8_t, 32> key_{};        // Encryption key (32 bytes)
    std::array<uint8_t, 16> buildNonce_{}; // Random nonce for this archive

    // Helper: read exactly N bytes from a file stream. Returns false on EOF/error.
    static bool readExact(std::ifstream& file, char* buf, size_t n);
    // Helper: write exactly N bytes to a file stream. Returns false on I/O error.
    static bool writeExact(std::ofstream& file, const char* buf, size_t n);
};

} // namespace nxs::runtime
