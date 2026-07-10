#include "ScriptArchive.hpp"

#include "ScriptCrypto.hpp"
#include "ScriptProtectionConfig.hpp"

#include <cstring>

namespace nxs::runtime {

namespace {

std::array<uint8_t, ScriptCrypto::KEY_SIZE> runtimeKey() {
    return ScriptCrypto::deriveKey(
        script_protection::PROJECT_NAME,
        script_protection::SALT,
        script_protection::CREATED_AT);
}

void decryptEntry(std::string& source, const uint8_t key[32], const uint8_t nonce[16]) {
    ScriptCrypto::xorStream(source, key, nonce);
}

}  // namespace

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

    uint32_t magic = 0;
    uint32_t version = 0;
    uint32_t count = 0;
    char reserved[20]{};

    if (!readExact(file, reinterpret_cast<char*>(&magic), 4)) return false;
    if (!readExact(file, reinterpret_cast<char*>(&version), 4)) return false;
    if (!readExact(file, reinterpret_cast<char*>(&count), 4)) return false;
    if (!readExact(file, reserved, 20)) return false;

    if (magic != magic_) return false;
    if (version != VERSION_V1 && version != VERSION_V2) return false;

    const bool encrypted =
        version == VERSION_V2 && (static_cast<uint8_t>(reserved[0]) & FLAG_ENCRYPTED) != 0;
    std::array<uint8_t, 16> nonce{};
    if (encrypted) {
        std::memcpy(nonce.data(), reserved + 1, 16);
    }

    std::array<uint8_t, 32> key{};
    if (encrypted && script_protection::ENABLED) {
        key = runtimeKey();
    }

    for (uint32_t i = 0; i < count; ++i) {
        ScriptEntry entry;

        uint8_t status = 0;
        uint16_t nameLen = 0;
        uint32_t srcLen = 0;

        if (!readExact(file, reinterpret_cast<char*>(&status), 1)) return false;
        if (!readExact(file, reinterpret_cast<char*>(&nameLen), 2)) return false;
        if (!readExact(file, reinterpret_cast<char*>(&srcLen), 4)) return false;

        entry.active = (status == 'A');

        entry.name.resize(nameLen);
        if (nameLen > 0) {
            if (!readExact(file, entry.name.data(), nameLen)) return false;
        }

        entry.source.resize(srcLen);
        if (srcLen > 0) {
            if (!readExact(file, entry.source.data(), srcLen)) return false;
        }

        if (encrypted) {
            decryptEntry(entry.source, key.data(), nonce.data());
        }

        entries_.push_back(std::move(entry));
    }

    encrypt_ = encrypted;
    if (encrypted) {
        buildNonce_ = nonce;
    }

    return true;
}

bool ScriptArchive::save(const std::string& path) const {
    std::ofstream file(path, std::ios::binary | std::ios::trunc);
    if (!file.is_open()) return false;

    uint32_t count = 0;
    for (const auto& e : entries_) {
        if (e.active) ++count;
    }

    const uint32_t magic = magic_;
    const uint32_t version = encrypt_ ? VERSION_V2 : VERSION_V1;
    char reserved[20]{};
    std::array<uint8_t, 16> nonce = buildNonce_;
    if (encrypt_) {
        reserved[0] = static_cast<char>(FLAG_ENCRYPTED);
        std::memcpy(reserved + 1, nonce.data(), 16);
    }

    if (!writeExact(file, reinterpret_cast<const char*>(&magic), 4)) return false;
    if (!writeExact(file, reinterpret_cast<const char*>(&version), 4)) return false;
    if (!writeExact(file, reinterpret_cast<const char*>(&count), 4)) return false;
    if (!writeExact(file, reserved, 20)) return false;

    for (const auto& e : entries_) {
        if (!e.active) continue;

        std::string payload = e.source;
        if (encrypt_) {
            decryptEntry(payload, key_.data(), nonce.data());
        }

        const uint8_t status = 'A';
        const uint16_t nameLen = static_cast<uint16_t>(e.name.size());
        const uint32_t srcLen = static_cast<uint32_t>(payload.size());

        if (!writeExact(file, reinterpret_cast<const char*>(&status), 1)) return false;
        if (!writeExact(file, reinterpret_cast<const char*>(&nameLen), 2)) return false;
        if (!writeExact(file, reinterpret_cast<const char*>(&srcLen), 4)) return false;
        if (nameLen > 0 && !writeExact(file, e.name.data(), nameLen)) return false;
        if (srcLen > 0 && !writeExact(file, payload.data(), srcLen)) return false;
    }

    return true;
}

void ScriptArchive::setEncryption(bool enabled, const uint8_t key[32]) {
    encrypt_ = enabled;
    if (enabled && key != nullptr) {
        std::memcpy(key_.data(), key, key_.size());
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
    size_t n = 0;
    for (const auto& e : entries_) {
        if (e.active) ++n;
    }
    return n;
}

}  // namespace nxs::runtime
