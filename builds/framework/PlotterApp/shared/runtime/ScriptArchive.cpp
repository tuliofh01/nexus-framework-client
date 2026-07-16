#include "ScriptArchive.hpp"

#include "ScriptCrypto.hpp"
#include "ScriptProtectionConfig.hpp"

#include <algorithm>
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

auto ScriptArchive::readExact(std::ifstream& file, char* buf, size_t n) -> bool {
    file.read(buf, static_cast<std::streamsize>(n));
    return file.gcount() == static_cast<std::streamsize>(n);
}

auto ScriptArchive::writeExact(std::ofstream& file, const char* buf, size_t n) -> bool {
    file.write(buf, static_cast<std::streamsize>(n));
    return file.good();
}

auto ScriptArchive::load(const std::string& path) -> bool {
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

auto ScriptArchive::save(const std::string& path) const -> bool {
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
        if (encrypt_) {
            decryptEntry(payload, key_.data(), nonce.data());
        }

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

void ScriptArchive::setEncryption(bool enabled, const uint8_t key[32]) {
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

auto ScriptArchive::remove(const std::string& name) -> bool {
    for (auto& e : entries_) {
        if (e.name == name) {
            e.active = false;
            return true;
        }
    }
    return false;
}

auto ScriptArchive::contains(const std::string& name) const -> bool {
    for (const auto& e : entries_) {
        if (e.name == name && e.active) return true;
    }
    return false;
}

auto ScriptArchive::getSource(const std::string& name, std::string& out) const -> bool {
    for (const auto& e : entries_) {
        if (e.name == name && e.active) {
            out = e.source;
            return true;
        }
    }
    return false;
}

auto ScriptArchive::activeCount() const -> size_t {
    auto n = size_t{0};
    for (const auto& e : entries_) {
        if (e.active) ++n;
    }
    return n;
}

}  // namespace nxs::runtime
