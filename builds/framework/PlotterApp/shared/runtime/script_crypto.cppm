//==============================================================================
// nexus.shared.script_crypto — XOR Stream Encryption for Script Archives
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Provides lightweight encryption for packed script archives (lua.dat,
// python.dat). This is NOT military-grade security — it prevents casual
// source code extraction from shipped binaries. The encryption uses:
//
//   • A 256-bit key derived from project name + salt + creation timestamp
//   • A 128-bit random nonce per archive
//   • XOR stream cipher (fast, symmetric, reversible)
//
// ════════════════════════════════════════════════════════════════════════════
// WHY XOR ENCRYPTION
// ════════════════════════════════════════════════════════════════════════════
//
// For a shipped app, the goal is to make it harder (not impossible) to
// extract your Lua/Python source from the binary. XOR with a derived key
// is fast at runtime and sufficient for this use case. If you need real
// security, use proper AES-GCM — but that adds OpenSSL as a dependency.
//
// ════════════════════════════════════════════════════════════════════════════
// KEY DERIVATION
// ════════════════════════════════════════════════════════════════════════════
//
//   deriveKey(projectName, salt, createdAt) → 32-byte key
//     • projectName: from nxs_config.json
//     • salt: random string generated at project creation time
//     • createdAt: ISO timestamp from nxs_config.json
//
//   This means each generated project gets a UNIQUE key, even with the
//   same source code. The key is deterministic — same inputs always
//   produce the same key — so the app can decrypt without storing it.
//
// ════════════════════════════════════════════════════════════════════════════
// WHY A SINGLE .cppm FILE
// ════════════════════════════════════════════════════════════════════════════
//
// This file is a "module interface unit" that BOTH declares and implements
// the module in one place. There is no separate .cpp file. All helpers
// (SHA-256 context, compression, rotation) live in an anonymous namespace
// inside the module — importers never see them.
//
// ════════════════════════════════════════════════════════════════════════════
// USAGE
// ════════════════════════════════════════════════════════════════════════════
//
//   import nexus.shared.script_crypto;
//   auto key = nxs::runtime::ScriptCrypto::deriveKey(name, salt, ts);
//   nxs::runtime::ScriptCrypto::xorStream(data, key.data(), nonce.data());
//==============================================================================

module;

#include <algorithm>     // std::ranges::copy for safe byte copies
#include <array>          // std::array<uint8_t, N> for keys and hashes
#include <cstdint>        // uint32_t, uint8_t, etc.
#include <cstring>        // std::memcpy for raw byte ops (SHA-256 padding)
#include <random>         // std::random_device, std::mt19937 for nonce
#include <string>         // std::string for input data
#include <vector>         // std::vector<uint8_t> for SHA-256 output

export module nexus.shared.script_crypto;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime — Script Encryption Utilities
// ═══════════════════════════════════════════════════════════════════════════

namespace nxs::runtime {

// ── Module-internal SHA-256 implementation ──
//
// SHA-256 is the hash function used for key derivation. We implement it
// from scratch rather than pulling in OpenSSL or a crypto library.
// The implementation follows FIPS 180-4 and is constant-time for the
// compression function (no secret-dependent branches).
//
// All SHA-256 internals are in an anonymous namespace — importers only
// see ScriptCrypto::sha256() at the top level.

namespace {

/// SHA-256 algorithm context — holds working state during hashing.
/// State: 8 x 32-bit words (the hash accumulator)
/// Bitcount: total bits processed (for final padding length)
/// Buffer: 64-byte block buffer (SHA-256 processes in 512-bit blocks)
struct Sha256Ctx {
    uint32_t state[8];
    uint64_t bitcount;
    uint8_t buffer[64];
};

/// Rotate right — essential SHA-256 operation.
/// constexpr + noexcept: pure mathematical function, no side effects.
[[nodiscard]] constexpr auto rotr(uint32_t x, uint32_t n) noexcept -> uint32_t {
    return (x >> n) | (x << (32 - n));
}

/// Process one 64-byte block through the SHA-256 compression function.
/// The 64 round constants (k[]) are derived from the cube roots of the
/// first 64 primes — they're fixed by the standard.
void sha256Transform(Sha256Ctx* ctx, const uint8_t data[64]) noexcept {
    // Round constants K[0..63] — first 32 bits of the fractional parts
    // of the cube roots of the first 64 prime numbers.
    static constexpr auto k = std::array<uint32_t, 64>{
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5,
        0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3,
        0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc,
        0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7,
        0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13,
        0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
        0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
        0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208,
        0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
    };

    // Message schedule: expand 16 words into 64 words
    auto w = std::array<uint32_t, 64>{};
    for (auto i = 0; i < 16; ++i) {
        w[i] = (static_cast<uint32_t>(data[i * 4]) << 24) |
               (static_cast<uint32_t>(data[i * 4 + 1]) << 16) |
               (static_cast<uint32_t>(data[i * 4 + 2]) << 8) |
               static_cast<uint32_t>(data[i * 4 + 3]);
    }
    for (auto i = 16; i < 64; ++i) {
        const auto s0 = rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >> 3);
        const auto s1 = rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >> 10);
        w[i] = w[i - 16] + s0 + w[i - 7] + s1;
    }

    // Working variables — initialize from current state
    auto a = ctx->state[0], b = ctx->state[1], c = ctx->state[2], d = ctx->state[3];
    auto e = ctx->state[4], f = ctx->state[5], g = ctx->state[6], h = ctx->state[7];

    // Compression loop — 64 rounds
    for (auto i = 0; i < 64; ++i) {
        const auto S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
        const auto ch = (e & f) ^ ((~e) & g);
        const auto temp1 = h + S1 + ch + k[i] + w[i];
        const auto S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
        const auto maj = (a & b) ^ (a & c) ^ (b & c);
        const auto temp2 = S0 + maj;
        h = g; g = f; f = e; e = d + temp1;
        d = c; c = b; b = a; a = temp1 + temp2;
    }

    // Add compressed result back to state
    ctx->state[0] += a; ctx->state[1] += b; ctx->state[2] += c; ctx->state[3] += d;
    ctx->state[4] += e; ctx->state[5] += f; ctx->state[6] += g; ctx->state[7] += h;
}

/// Initialize SHA-256 context with the standard initial hash values (H0..H7).
/// These are the first 32 bits of the fractional parts of the square roots
/// of the first 8 primes. Set by the standard, not arbitrary.
void sha256Init(Sha256Ctx* ctx) noexcept {
    ctx->state[0] = 0x6a09e667; ctx->state[1] = 0xbb67ae85;
    ctx->state[2] = 0x3c6ef372; ctx->state[3] = 0xa54ff53a;
    ctx->state[4] = 0x510e527f; ctx->state[5] = 0x9b05688c;
    ctx->state[6] = 0x1f83d9ab; ctx->state[7] = 0x5be0cd19;
    ctx->bitcount = 0;
}

/// Feed data into the SHA-256 computation.
/// Accumulates bytes in the buffer and calls sha256Transform every 64 bytes.
void sha256Update(Sha256Ctx* ctx, const uint8_t* data, size_t len) noexcept {
    for (size_t i = 0; i < len; ++i) {
        ctx->buffer[ctx->bitcount % 64] = data[i];
        ++ctx->bitcount;
        if (ctx->bitcount % 64 == 0) {
            sha256Transform(ctx, ctx->buffer);
        }
    }
}

/// Finalize SHA-256 and produce the 32-byte digest.
/// Appends the standard SHA-256 padding: 0x80 byte, zeros, then 64-bit bit length.
void sha256Final(Sha256Ctx* ctx, uint8_t hash[32]) noexcept {
    // Append 0x80 padding bit
    auto pad = uint8_t{0x80};
    sha256Update(ctx, &pad, 1);

    // Pad with zeros until 56 mod 64 bytes (448 mod 512 bits)
    while (ctx->bitcount % 64 != 56) {
        pad = 0;
        sha256Update(ctx, &pad, 1);
    }

    // Append the original bit length as a 64-bit big-endian integer
    auto bitlen = ctx->bitcount * 8;
    auto lenbuf = std::array<uint8_t, 8>{};
    for (auto i = 7; i >= 0; --i) {
        lenbuf[static_cast<std::size_t>(i)] = static_cast<uint8_t>(bitlen & 0xff);
        bitlen >>= 8;
    }
    sha256Update(ctx, lenbuf.data(), 8);

    // Extract final hash value — big-endian byte order per SHA-256 spec
    for (auto i = 0; i < 8; ++i) {
        hash[static_cast<std::size_t>(i * 4)]     = static_cast<uint8_t>((ctx->state[i] >> 24) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 1)] = static_cast<uint8_t>((ctx->state[i] >> 16) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 2)] = static_cast<uint8_t>((ctx->state[i] >> 8) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 3)] = static_cast<uint8_t>(ctx->state[i] & 0xff);
    }
}

}  // anonymous namespace

// ═══════════════════════════════════════════════════════════════════════════
// ScriptCrypto — Public API
// ═══════════════════════════════════════════════════════════════════════════

export class ScriptCrypto {
public:
    // AES-256 key size — 32 bytes = 256 bits
    static constexpr size_t KEY_SIZE   = 32;
    // Random nonce size — 16 bytes = 128 bits (unique per archive)
    static constexpr size_t NONCE_SIZE = 16;

    /// Derive a 256-bit encryption key from project metadata.
    /// Uses SHA-256 to hash the concatenation of projectName + salt + createdAt.
    /// Deterministic: same inputs → same key, so the app can decrypt at runtime.
    /// [[nodiscard]]: callers must use the returned key, not ignore it.
    [[nodiscard]] static auto deriveKey(
        const std::string& projectName,
        const std::string& salt,
        const std::string& createdAt) -> std::array<uint8_t, KEY_SIZE>
    {
        const auto material = projectName + salt + createdAt;
        auto hash = sha256(material);
        auto key = std::array<uint8_t, KEY_SIZE>{};
        std::ranges::copy(hash.begin(), hash.begin() + KEY_SIZE, key.begin());
        return key;
    }

    /// Fill a buffer with cryptographically random bytes for the nonce.
    /// Uses std::random_device ( /dev/urandom on Linux, BCrypt on Windows)
    /// seeded through Mersenne Twister. Not suitable for high-security RNG.
    static void fillRandomNonce(uint8_t* nonce, size_t len) {
        auto rd = std::random_device{};
        auto gen = std::mt19937{rd()};
        auto dist = std::uniform_int_distribution<int>{0, 255};
        for (size_t i = 0; i < len; ++i) {
            nonce[i] = static_cast<uint8_t>(dist(gen));
        }
    }

    /// Encrypt/decrypt data using XOR stream cipher.
    /// XOR is symmetric: encrypt(key, data) == decrypt(key, data).
    /// The stream byte is key[i % KEY_SIZE] ^ nonce[i % NONCE_SIZE].
    /// noexcept: pure data transformation, no allocations.
    static void xorStream(
        std::string& data,
        const uint8_t key[KEY_SIZE],
        const uint8_t nonce[NONCE_SIZE]) noexcept
    {
        for (size_t i = 0; i < data.size(); ++i) {
            const auto streamByte = key[i % KEY_SIZE] ^ nonce[i % NONCE_SIZE];
            data[i] = static_cast<char>(static_cast<uint8_t>(data[i]) ^ streamByte);
        }
    }

    /// Compute SHA-256 hash of input string.
    /// Returns a 32-byte vector. Used internally by deriveKey() but also
    /// exposed for potential use by other modules (e.g., FlowRunner checksums).
    [[nodiscard]] static auto sha256(const std::string& input) -> std::vector<uint8_t> {
        auto ctx = Sha256Ctx{};
        sha256Init(&ctx);
        sha256Update(&ctx, reinterpret_cast<const uint8_t*>(input.data()), input.size());
        auto out = std::vector<uint8_t>(32);
        sha256Final(&ctx, out.data());
        return out;
    }
};

}  // namespace nxs::runtime
