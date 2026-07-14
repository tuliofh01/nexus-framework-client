//==============================================================================
// ScriptCrypto — XOR Stream Encryption for Script Archives (C++20 Module)
//==============================================================================
//
// WHAT THIS MODULE DOES:
//   Provides lightweight encryption for packed script archives (lua.dat, python.dat).
//   This is NOT military-grade security — it's "script protection" that prevents
//   casual source code extraction from shipped binaries. The encryption uses:
//     • A 256-bit key derived from project name + salt + creation timestamp
//     • A 128-bit random nonce per archive
//     • XOR stream cipher (fast, simple, reversible)
//
// WHY XOR ENCRYPTION:
//   For a shipped desktop/Android app, the goal is to make it harder (not
//   impossible) to extract your Lua/Python source from the binary. XOR with
//   a derived key is fast at runtime and sufficient for this use case.
//   If you need real security, use proper AES-GCM — but that adds OpenSSL
//   as a dependency.
//
// KEY DERIVATION:
//   deriveKey(projectName, salt, createdAt) → 32-byte key
//     • projectName: from nxs_config.json
//     • salt: random string generated at project creation time
//     • createdAt: ISO timestamp from nxs_config.json
//   This means each generated project gets a UNIQUE key, even with the
//   same source code.
//
// C++20 MODULE CONCEPTS:
//   This module exports a class with both static methods (called without
//   an instance) and constexpr constants (evaluated at compile time).
//
//==============================================================================

export module nexus.shared.script_crypto;

#include <array>
#include <cstdint>
#include <string>
#include <vector>

namespace nxs::runtime {

export class ScriptCrypto {
public:
    // AES-256 key size — 32 bytes = 256 bits
    static constexpr size_t KEY_SIZE   = 32;
    // Random nonce size — 16 bytes = 128 bits (unique per archive)
    static constexpr size_t NONCE_SIZE = 16;

    /// Derive a 256-bit encryption key from project metadata.
    /// The key is deterministic — same inputs always produce the same key.
    /// This allows the app to decrypt archives without storing the key.
    static std::array<uint8_t, KEY_SIZE> deriveKey(
        const std::string& projectName,
        const std::string& salt,
        const std::string& createdAt);

    /// Fill a buffer with cryptographically random bytes for the nonce.
    /// Uses platform-specific RNG ( /dev/urandom on Linux, BCrypt on Windows).
    static void fillRandomNonce(uint8_t* nonce, size_t len);

    /// Encrypt/decrypt data using XOR stream cipher.
    /// XOR is symmetric: encrypt(key, data) == decrypt(key, data).
    /// This is intentional — the same operation works for both directions.
    static void xorStream(std::string& data, const uint8_t key[KEY_SIZE], const uint8_t nonce[NONCE_SIZE]);

    /// Compute SHA-256 hash of input string.
    /// Used for key derivation (mixed with project metadata).
    static std::vector<uint8_t> sha256(const std::string& input);
};

} // namespace nxs::runtime
