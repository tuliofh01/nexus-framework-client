#include "ScriptCrypto.hpp"

#include <cstring>
#include <random>

namespace nxs::runtime {

namespace {

struct Sha256Ctx {
    uint32_t state[8];
    uint64_t bitcount;
    uint8_t buffer[64];
};

static uint32_t rotr(uint32_t x, uint32_t n) { return (x >> n) | (x << (32 - n)); }

static void sha256Transform(Sha256Ctx* ctx, const uint8_t data[64]) {
    static const uint32_t k[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
    };
    uint32_t w[64];
    for (int i = 0; i < 16; ++i) {
        w[i] = (static_cast<uint32_t>(data[i * 4]) << 24) |
               (static_cast<uint32_t>(data[i * 4 + 1]) << 16) |
               (static_cast<uint32_t>(data[i * 4 + 2]) << 8) |
               static_cast<uint32_t>(data[i * 4 + 3]);
    }
    for (int i = 16; i < 64; ++i) {
        uint32_t s0 = rotr(w[i - 15], 7) ^ rotr(w[i - 15], 18) ^ (w[i - 15] >> 3);
        uint32_t s1 = rotr(w[i - 2], 17) ^ rotr(w[i - 2], 19) ^ (w[i - 2] >> 10);
        w[i] = w[i - 16] + s0 + w[i - 7] + s1;
    }
    uint32_t a = ctx->state[0], b = ctx->state[1], c = ctx->state[2], d = ctx->state[3];
    uint32_t e = ctx->state[4], f = ctx->state[5], g = ctx->state[6], h = ctx->state[7];
    for (int i = 0; i < 64; ++i) {
        uint32_t S1 = rotr(e, 6) ^ rotr(e, 11) ^ rotr(e, 25);
        uint32_t ch = (e & f) ^ ((~e) & g);
        uint32_t temp1 = h + S1 + ch + k[i] + w[i];
        uint32_t S0 = rotr(a, 2) ^ rotr(a, 13) ^ rotr(a, 22);
        uint32_t maj = (a & b) ^ (a & c) ^ (b & c);
        uint32_t temp2 = S0 + maj;
        h = g;
        g = f;
        f = e;
        e = d + temp1;
        d = c;
        c = b;
        b = a;
        a = temp1 + temp2;
    }
    ctx->state[0] += a;
    ctx->state[1] += b;
    ctx->state[2] += c;
    ctx->state[3] += d;
    ctx->state[4] += e;
    ctx->state[5] += f;
    ctx->state[6] += g;
    ctx->state[7] += h;
}

static void sha256Init(Sha256Ctx* ctx) {
    ctx->state[0] = 0x6a09e667;
    ctx->state[1] = 0xbb67ae85;
    ctx->state[2] = 0x3c6ef372;
    ctx->state[3] = 0xa54ff53a;
    ctx->state[4] = 0x510e527f;
    ctx->state[5] = 0x9b05688c;
    ctx->state[6] = 0x1f83d9ab;
    ctx->state[7] = 0x5be0cd19;
    ctx->bitcount = 0;
}

static void sha256Update(Sha256Ctx* ctx, const uint8_t* data, size_t len) {
    for (size_t i = 0; i < len; ++i) {
        ctx->buffer[ctx->bitcount % 64] = data[i];
        ctx->bitcount++;
        if (ctx->bitcount % 64 == 0) {
            sha256Transform(ctx, ctx->buffer);
        }
    }
}

static void sha256Final(Sha256Ctx* ctx, uint8_t hash[32]) {
    uint64_t bitlen = ctx->bitcount * 8;
    uint8_t pad = 0x80;
    sha256Update(ctx, &pad, 1);
    while (ctx->bitcount % 64 != 56) {
        pad = 0;
        sha256Update(ctx, &pad, 1);
    }
    uint8_t lenbuf[8];
    for (int i = 7; i >= 0; --i) {
        lenbuf[i] = static_cast<uint8_t>(bitlen & 0xff);
        bitlen >>= 8;
    }
    sha256Update(ctx, lenbuf, 8);
    for (int i = 0; i < 8; ++i) {
        hash[i * 4] = static_cast<uint8_t>((ctx->state[i] >> 24) & 0xff);
        hash[i * 4 + 1] = static_cast<uint8_t>((ctx->state[i] >> 16) & 0xff);
        hash[i * 4 + 2] = static_cast<uint8_t>((ctx->state[i] >> 8) & 0xff);
        hash[i * 4 + 3] = static_cast<uint8_t>(ctx->state[i] & 0xff);
    }
}

}  // namespace

std::vector<uint8_t> ScriptCrypto::sha256(const std::string& input) {
    Sha256Ctx ctx;
    sha256Init(&ctx);
    sha256Update(&ctx, reinterpret_cast<const uint8_t*>(input.data()), input.size());
    std::vector<uint8_t> out(32);
    sha256Final(&ctx, out.data());
    return out;
}

std::array<uint8_t, ScriptCrypto::KEY_SIZE> ScriptCrypto::deriveKey(
    const std::string& projectName,
    const std::string& salt,
    const std::string& createdAt) {
    const std::string material = projectName + salt + createdAt;
    auto hash = sha256(material);
    std::array<uint8_t, KEY_SIZE> key{};
    std::memcpy(key.data(), hash.data(), KEY_SIZE);
    return key;
}

void ScriptCrypto::fillRandomNonce(uint8_t* nonce, size_t len) {
    std::random_device rd;
    std::mt19937 gen(rd());
    std::uniform_int_distribution<int> dist(0, 255);
    for (size_t i = 0; i < len; ++i) {
        nonce[i] = static_cast<uint8_t>(dist(gen));
    }
}

void ScriptCrypto::xorStream(
    std::string& data,
    const uint8_t key[KEY_SIZE],
    const uint8_t nonce[NONCE_SIZE]) {
    for (size_t i = 0; i < data.size(); ++i) {
        const uint8_t streamByte = key[i % KEY_SIZE] ^ nonce[i % NONCE_SIZE];
        data[i] = static_cast<char>(static_cast<uint8_t>(data[i]) ^ streamByte);
    }
}

}  // namespace nxs::runtime
