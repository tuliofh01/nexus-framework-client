//==============================================================================
// nexus.shared.script_archive — Binary Pack Format for Lua/Python Scripts
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Manages packed script archives — binary files that bundle multiple Lua or
// Python source files into a single distributable file (lua.dat or python.dat).
// This is how the Nexus framework ships script code inside the app binary
// without leaving plain-text .lua/.py files on disk.
//
// ════════════════════════════════════════════════════════════════════════════
// ARCHIVE FORMAT (v2)
// ════════════════════════════════════════════════════════════════════════════
//
//   ┌──────────────────────────────────────────────────────────────┐
//   │ Header (32 bytes)                                            │
//   │   magic:     4 bytes ("LUAC" or "PYAC")                     │
//   │   version:   4 bytes (1 = plain, 2 = encrypted)             │
//   │   flags:     4 bytes (bit 0 = encrypted)                    │
//   │   count:     4 bytes (number of entries)                     │
//   │   reserved:  16 bytes (nonce for encryption)                 │
//   ├──────────────────────────────────────────────────────────────┤
//   │ Entry 0: [status(1)][nameLen(2)][srcLen(4)][name][src]      │
//   │ Entry 1: [status(1)][nameLen(2)][srcLen(4)][name][src]      │
//   │ ...                                                          │
//   └──────────────────────────────────────────────────────────────┘
//
//   Magic numbers:
//     0x4C554143 = "LUAC" — Lua Archive
//     0x50594143 = "PYAC" — Python Archive
//
//   When encrypted (version 2), each entry's source bytes are XOR-encrypted
//   with a key derived from project metadata (see ScriptCrypto).
//
// ════════════════════════════════════════════════════════════════════════════
// WHY A BINARY FORMAT
// ════════════════════════════════════════════════════════════════════════════
//
//   • Simple to parse in C++ (no JSON/XML dependency in the runtime)
//   • Fast to load (single file read, no filesystem traversal)
//   • Supports optional encryption (ScriptProtection feature)
//   • Works on both Desktop and Android
//
// ════════════════════════════════════════════════════════════════════════════
// DESIGN NOTE: SINGLE .cppm FILE
// ════════════════════════════════════════════════════════════════════════════
//
// This file merges what was previously a .hpp + .cpp pair plus a module
// implementation unit into a single self-contained .cppm. All module-internal
// helpers live in an anonymous namespace inside the module — importers never
// see them. Users rarely edit archive infrastructure, so keeping it as one
// file reduces mental overhead.
//
// ════════════════════════════════════════════════════════════════════════════
// USAGE
// ════════════════════════════════════════════════════════════════════════════
//
//   import nexus.shared.script_archive;
//   auto archive = nxs::runtime::ScriptArchive{ScriptArchive::MAGIC_LUA};
//   if (archive.load("misc/lua.dat")) {
//       std::string src;
//       archive.getSource("panels", src);  // retrieve "panels.lua" source
//   }
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

#include <algorithm>     // std::ranges::copy for safe byte copies
#include <array>         // std::array<uint8_t, N> for keys and nonces
#include <cstdint>       // uint32_t, uint8_t, etc.
#include <cstring>       // std::memcpy for raw byte operations
#include <fstream>       // std::ifstream, std::ofstream for file I/O
#include <string>        // std::string for entry data
#include <vector>        // std::vector for the entry list

export module nexus.shared.script_archive;

// ── Peer modules used by the implementation ──
//
// These are module imports (not #include). Importers of
// nexus.shared.script_archive do NOT transitively get these —
// they would need their own `import nexus.shared.script_crypto;`
// if they need it.
import nexus.shared.script_crypto;
import nexus.shared.script_protection;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime — Script Archive Manager
// ═══════════════════════════════════════════════════════════════════════════

namespace nxs::runtime {

// ── Module-internal helpers (private to this module) ──

namespace {

/// Derive the archive encryption key from project metadata.
/// The key is deterministic (same project = same key), so the app can
/// decrypt its own archives without storing a key separately.
[[nodiscard]] auto runtimeKey() -> std::array<uint8_t, ScriptCrypto::KEY_SIZE> {
    return ScriptCrypto::deriveKey(
        script_protection::PROJECT_NAME,
        script_protection::SALT,
        script_protection::CREATED_AT);
}

/// Apply XOR decryption to an entry's source data.
/// XOR is symmetric: encrypt(key, data) == decrypt(key, data).
void decryptEntry(std::string& source, const uint8_t key[32], const uint8_t nonce[16]) noexcept {
    ScriptCrypto::xorStream(source, key, nonce);
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// ScriptEntry — one file within the archive
// ═══════════════════════════════════════════════════════════════════════════
//
// Not exported — importers interact with entries only through ScriptArchive
// methods like getSource() and contains().

struct ScriptEntry {
    bool active = true;
    std::string name;     // Module name (e.g. "panels", "functions")
    std::string source;   // Raw source code content
};

// ═══════════════════════════════════════════════════════════════════════════
// ScriptArchive — Public API
// ═══════════════════════════════════════════════════════════════════════════
//
// RAII: owns the entries vector by value. Construction is cheap (just stores
// the magic number). load() does the heavy lifting; save() serialises.

export class ScriptArchive {
public:
    // ── Constants ───────────────────────────────────────────────────────
    //
    // Magic numbers identify the archive type at a glance: reading the
    // first 4 bytes of any .dat file tells you if it's Lua or Python.
    // constexpr: the linker can fold duplicate constants.

    static constexpr uint32_t MAGIC_LUA    = 0x4C554143;  // "LUAC"
    static constexpr uint32_t MAGIC_PYTHON = 0x50594143;  // "PYAC"
    static constexpr uint32_t VERSION_V1 = 1;  // Plain text
    static constexpr uint32_t VERSION_V2 = 2;  // XOR encryption
    static constexpr uint32_t HEADER_SIZE = 32;
    static constexpr uint8_t  FLAG_ENCRYPTED = 0x01;

    // ── Construction ───────────────────────────────────────────────────
    //
    // explicit: prevents accidental conversion from uint32_t to ScriptArchive.

    explicit ScriptArchive(uint32_t magic) noexcept : magic_{magic} {}

    // ── Rule of Five ───────────────────────────────────────────────────
    //
    // ScriptEntry contains std::string (movable) and std::vector (movable),
    // so the default move operations are correct. We default everything.

    ScriptArchive(const ScriptArchive&) = default;
    auto operator=(const ScriptArchive&) -> ScriptArchive& = default;
    ScriptArchive(ScriptArchive&&) noexcept = default;
    auto operator=(ScriptArchive&&) noexcept -> ScriptArchive& = default;
    ~ScriptArchive() = default;

    // ── I/O ────────────────────────────────────────────────────────────

    [[nodiscard]] bool load(const std::string& path);
    [[nodiscard]] bool save(const std::string& path) const;

    // ── Entry management ───────────────────────────────────────────────

    void add(const std::string& name, const std::string& source);
    [[nodiscard]] bool remove(const std::string& name);
    [[nodiscard]] bool contains(const std::string& name) const;
    [[nodiscard]] bool getSource(const std::string& name, std::string& out) const;
    [[nodiscard]] size_t activeCount() const;

    // ── Encryption ─────────────────────────────────────────────────────

    void setEncryption(bool enabled, const uint8_t key[32]) noexcept;
    [[nodiscard]] bool isEncrypted() const noexcept { return encrypt_; }

    // ── Introspection ──────────────────────────────────────────────────

    [[nodiscard]] uint32_t magic() const noexcept { return magic_; }
    [[nodiscard]] const std::vector<ScriptEntry>& entries() const noexcept { return entries_; }

private:
    // ── Data members ───────────────────────────────────────────────────
    //
    // {} brace initialization: all members are zero-initialised by default.
    // No uninitialised fields = no undefined behaviour.

    uint32_t magic_{};
    std::vector<ScriptEntry> entries_{};
    bool encrypt_{false};
    std::array<uint8_t, 32> key_{};
    std::array<uint8_t, 16> buildNonce_{};

    // ── I/O helpers ────────────────────────────────────────────────────

    static bool readExact(std::ifstream& file, char* buf, size_t n);
    static bool writeExact(std::ofstream& file, const char* buf, size_t n);
};

// ═══════════════════════════════════════════════════════════════════════════
// Implementation
// ═══════════════════════════════════════════════════════════════════════════

bool ScriptArchive::readExact(std::ifstream& file, char* buf, size_t n) {
    file.read(buf, static_cast<std::streamsize>(n));
    return file.gcount() == static_cast<std::streamsize>(n);
}

bool ScriptArchive::writeExact(std::ofstream& file, const char* buf, size_t n) {
    file.write(buf, static_cast<std::streamsize>(n));
    return file.good();
}

bool ScriptArchive::load(const std::string& path) {
    entries_.clear();
    encrypt_ = false;
    buildNonce_.fill(0);

    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) return false;

    auto magic = uint32_t{0};
    auto version = uint32_t{0};
    auto count = uint32_t{0};
    auto reserved = std::array<char, 20>{};

    if (!readExact(file, reinterpret_cast<char*>(&magic), 4)) return false;
    if (!readExact(file, reinterpret_cast<char*>(&version), 4)) return false;
    if (!readExact(file, reinterpret_cast<char*>(&count), 4)) return false;
    if (!readExact(file, reserved.data(), 20)) return false;

    if (magic != magic_) return false;
    if (version != VERSION_V1 && version != VERSION_V2) return false;

    const auto encrypted = version == VERSION_V2 &&
        (static_cast<uint8_t>(reserved[0]) & FLAG_ENCRYPTED) != 0;

    auto nonce = std::array<uint8_t, 16>{};
    if (encrypted) {
        std::ranges::copy(reserved.begin() + 1, reserved.begin() + 17, nonce.begin());
    }

    auto key = std::array<uint8_t, 32>{};
    if (encrypted && script_protection::ENABLED) {
        key = runtimeKey();
    }

    for (uint32_t i = 0; i < count; ++i) {
        auto entry = ScriptEntry{};

        auto status = uint8_t{0};
        auto nameLen = uint16_t{0};
        auto srcLen = uint32_t{0};

        if (!readExact(file, reinterpret_cast<char*>(&status), 1)) return false;
        if (!readExact(file, reinterpret_cast<char*>(&nameLen), 2)) return false;
        if (!readExact(file, reinterpret_cast<char*>(&srcLen), 4)) return false;

        entry.active = (status == 'A');

        entry.name.resize(nameLen);
        if (nameLen > 0 && !readExact(file, entry.name.data(), nameLen)) return false;

        entry.source.resize(srcLen);
        if (srcLen > 0 && !readExact(file, entry.source.data(), srcLen)) return false;

        if (encrypted) decryptEntry(entry.source, key.data(), nonce.data());

        entries_.push_back(std::move(entry));
    }

    encrypt_ = encrypted;
    if (encrypted) buildNonce_ = nonce;
    return true;
}

bool ScriptArchive::save(const std::string& path) const {
    std::ofstream file(path, std::ios::binary | std::ios::trunc);
    if (!file.is_open()) return false;

    auto count = uint32_t{0};
    for (const auto& e : entries_) {
        if (e.active) ++count;
    }

    const auto magic = magic_;
    const auto version = encrypt_ ? VERSION_V2 : VERSION_V1;
    auto reserved = std::array<char, 20>{};
    auto nonce = buildNonce_;

    if (encrypt_) {
        reserved[0] = static_cast<char>(FLAG_ENCRYPTED);
        std::ranges::copy(nonce.begin(), nonce.end(), reserved.begin() + 1);
    }

    if (!writeExact(file, reinterpret_cast<const char*>(&magic), 4)) return false;
    if (!writeExact(file, reinterpret_cast<const char*>(&version), 4)) return false;
    if (!writeExact(file, reinterpret_cast<const char*>(&count), 4)) return false;
    if (!writeExact(file, reserved.data(), 20)) return false;

    for (const auto& e : entries_) {
        if (!e.active) continue;

        auto payload = e.source;
        if (encrypt_) decryptEntry(payload, key_.data(), nonce.data());

        const auto status = uint8_t{'A'};
        const auto nameLen = static_cast<uint16_t>(e.name.size());
        const auto srcLen = static_cast<uint32_t>(payload.size());

        if (!writeExact(file, reinterpret_cast<const char*>(&status), 1)) return false;
        if (!writeExact(file, reinterpret_cast<const char*>(&nameLen), 2)) return false;
        if (!writeExact(file, reinterpret_cast<const char*>(&srcLen), 4)) return false;
        if (nameLen > 0 && !writeExact(file, e.name.data(), nameLen)) return false;
        if (srcLen > 0 && !writeExact(file, payload.data(), srcLen)) return false;
    }

    return true;
}

void ScriptArchive::setEncryption(bool enabled, const uint8_t key[32]) noexcept {
    encrypt_ = enabled;
    if (enabled && key != nullptr) {
        std::ranges::copy(key, key + key_.size(), key_.data());
        ScriptCrypto::fillRandomNonce(buildNonce_.data(), buildNonce_.size());
    } else {
        key_.fill(0);
        buildNonce_.fill(0);
    }
}

void ScriptArchive::add(const std::string& name, const std::string& source) {
    for (auto& e : entries_) {
        if (e.name == name) {
            e.active = true;
            e.source = source;
            return;
        }
    }
    entries_.emplace_back(true, name, source);
}

bool ScriptArchive::remove(const std::string& name) {
    for (auto& e : entries_) {
        if (e.name == name) {
            e.active = false;
            return true;
        }
    }
    return false;
}

bool ScriptArchive::contains(const std::string& name) const {
    for (const auto& e : entries_) {
        if (e.name == name && e.active) return true;
    }
    return false;
}

bool ScriptArchive::getSource(const std::string& name, std::string& out) const {
    for (const auto& e : entries_) {
        if (e.name == name && e.active) {
            out = e.source;
            return true;
        }
    }
    return false;
}

size_t ScriptArchive::activeCount() const {
    auto n = size_t{0};
    for (const auto& e : entries_) {
        if (e.active) ++n;
    }
    return n;
}

}  // namespace nxs::runtime
