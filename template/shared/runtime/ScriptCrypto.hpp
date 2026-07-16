#ifndef NEXUS_SCRIPT_CRYPTO_HPP
#define NEXUS_SCRIPT_CRYPTO_HPP

#include <array>
#include <cstddef>
#include <cstdint>
#include <string>
#include <vector>

namespace nxs::runtime {

class ScriptCrypto {
public:
    static constexpr size_t KEY_SIZE   = 32;
    static constexpr size_t NONCE_SIZE = 16;

    static auto deriveKey(
        const std::string& projectName,
        const std::string& salt,
        const std::string& createdAt) -> std::array<uint8_t, KEY_SIZE>;

    static void fillRandomNonce(uint8_t* nonce, size_t len);
    static void xorStream(
        std::string& data,
        const uint8_t key[KEY_SIZE],
        const uint8_t nonce[NONCE_SIZE]) noexcept;

    static auto sha256(const std::string& input) -> std::vector<uint8_t>;
};

}  // namespace nxs::runtime

#endif  // NEXUS_SCRIPT_CRYPTO_HPP
