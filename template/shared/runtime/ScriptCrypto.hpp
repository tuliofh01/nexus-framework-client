#pragma once

#include <array>
#include <cstdint>
#include <string>
#include <vector>

namespace nxs::runtime {

/** Key derivation and stream obfuscation for script archives (nxs-v1).
 *
 * Intentionally lightweight: no OpenSSL dependency in generated apps. SHA-256 is
 * embedded so pack_archive and runtime share one code path; XOR stream is symmetric
 * (encrypt == decrypt) to keep load/save logic identical.
 */
class ScriptCrypto {
public:
    static constexpr size_t KEY_SIZE = 32;
    static constexpr size_t NONCE_SIZE = 16;

    static std::array<uint8_t, KEY_SIZE> deriveKey(
        const std::string& projectName,
        const std::string& salt,
        const std::string& createdAt);

    static void fillRandomNonce(uint8_t* nonce, size_t len);

    static void xorStream(
        std::string& data,
        const uint8_t key[KEY_SIZE],
        const uint8_t nonce[NONCE_SIZE]);

    static std::vector<uint8_t> sha256(const std::string& input);
};

}  // namespace nxs::runtime
