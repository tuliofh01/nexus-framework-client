#include "ScriptCrypto.hpp"

#include <algorithm>
#include <array>
#include <cstring>
#include <random>

namespace nxs::runtime {

namespace {

struct Sha256Ctx {
    uint32_t state[8];
    uint64_t bitcount;
    uint8_t buffer[64];
};

[[nodiscard]] auto rotr(uint32_t x, uint32_t n) noexcept -> uint32_t {
    return (x >> n) | (x << (32 - n));
}

void sha256Transform(Sha256Ctx* ctx, const uint8_t data[64]) noexcept {
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

    auto a = ctx->state[0], b = ctx->state[1], c = ctx->state[2], d = ctx->state[3];
    auto e = ctx->state[4], f = ctx->state[5], g = ctx->state[6], h = ctx->state[7];

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

    ctx->state[0] += a; ctx->state[1] += b; ctx->state[2] += c; ctx->state[3] += d;
    ctx->state[4] += e; ctx->state[5] += f; ctx->state[6] += g; ctx->state[7] += h;
}

void sha256Init(Sha256Ctx* ctx) noexcept {
    ctx->state[0] = 0x6a09e667; ctx->state[1] = 0xbb67ae85;
    ctx->state[2] = 0x3c6ef372; ctx->state[3] = 0xa54ff53a;
    ctx->state[4] = 0x510e527f; ctx->state[5] = 0x9b05688c;
    ctx->state[6] = 0x1f83d9ab; ctx->state[7] = 0x5be0cd19;
    ctx->bitcount = 0;
}

void sha256Update(Sha256Ctx* ctx, const uint8_t* data, size_t len) noexcept {
    for (size_t i = 0; i < len; ++i) {
        ctx->buffer[ctx->bitcount % 64] = data[i];
        ++ctx->bitcount;
        if (ctx->bitcount % 64 == 0) {
            sha256Transform(ctx, ctx->buffer);
        }
    }
}

void sha256Final(Sha256Ctx* ctx, uint8_t hash[32]) noexcept {
    auto bitlen = ctx->bitcount * 8;
    auto pad = uint8_t{0x80};
    sha256Update(ctx, &pad, 1);
    while (ctx->bitcount % 64 != 56) {
        pad = 0;
        sha256Update(ctx, &pad, 1);
    }
    auto lenbuf = std::array<uint8_t, 8>{};
    for (auto i = 7; i >= 0; --i) {
        lenbuf[static_cast<std::size_t>(i)] = static_cast<uint8_t>(bitlen & 0xff);
        bitlen >>= 8;
    }
    sha256Update(ctx, lenbuf.data(), 8);
    for (auto i = 0; i < 8; ++i) {
        hash[static_cast<std::size_t>(i * 4)]     = static_cast<uint8_t>((ctx->state[i] >> 24) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 1)] = static_cast<uint8_t>((ctx->state[i] >> 16) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 2)] = static_cast<uint8_t>((ctx->state[i] >> 8) & 0xff);
        hash[static_cast<std::size_t>(i * 4 + 3)] = static_cast<uint8_t>(ctx->state[i] & 0xff);
    }
}

}  // namespace

auto ScriptCrypto::sha256(const std::string& input) -> std::vector<uint8_t> {
    auto ctx = Sha256Ctx{};
    sha256Init(&ctx);
    sha256Update(&ctx, reinterpret_cast<const uint8_t*>(input.data()), input.size());
    auto out = std::vector<uint8_t>(32);
    sha256Final(&ctx, out.data());
    return out;
}

auto ScriptCrypto::deriveKey(
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

void ScriptCrypto::fillRandomNonce(uint8_t* nonce, size_t len) {
    auto rd = std::random_device{};
    auto gen = std::mt19937{rd()};
    auto dist = std::uniform_int_distribution<int>{0, 255};
    for (size_t i = 0; i < len; ++i) {
        nonce[i] = static_cast<uint8_t>(dist(gen));
    }
}

void ScriptCrypto::xorStream(
    std::string& data,
    const uint8_t key[KEY_SIZE],
    const uint8_t nonce[NONCE_SIZE]) noexcept
{
    for (size_t i = 0; i < data.size(); ++i) {
        const auto streamByte = key[i % KEY_SIZE] ^ nonce[i % NONCE_SIZE];
        data[i] = static_cast<char>(static_cast<uint8_t>(data[i]) ^ streamByte);
    }
}

}  // namespace nxs::runtime
