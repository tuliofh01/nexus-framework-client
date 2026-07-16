//==============================================================================
// nexus.shared.script_protection — Compile-Time Script Security Config
//
// ════════════════════════════════════════════════════════════════════════════
// WHAT THIS MODULE DOES
// ════════════════════════════════════════════════════════════════════════════
//
// Holds the configuration constants that control whether script archives
// (lua.dat, python.dat) are encrypted at build time. These values are
// injected by the ProjectGenerator from nxs_config.json during generation.
//
// ════════════════════════════════════════════════════════════════════════════
// C++20 MODULE CONCEPTS
// ════════════════════════════════════════════════════════════════════════════
//
// This module exports `constexpr` constants — values known at COMPILE TIME.
// Because they're constexpr, the compiler can optimize away the entire
// encryption code path when ENABLED is false (dead code elimination).
//
//   `export constexpr` means: "This constant is visible to importers AND
//   its value is fixed before the program runs."
//
// ════════════════════════════════════════════════════════════════════════════
// WHY A SEPARATE MODULE
// ════════════════════════════════════════════════════════════════════════════
//
// These constants are generated per-project (different salt, project name,
// creation date for each app). Keeping them in their own module means the
// rest of the runtime doesn't need to recompile when only the config changes.
//
// ════════════════════════════════════════════════════════════════════════════
// SECURITY NOTE
// ════════════════════════════════════════════════════════════════════════════
//
// ENABLED defaults to false. When true, the generator fills in PROJECT_NAME,
// SALT, and CREATED_AT from nxs_config.json, and ScriptCrypto uses them to
// derive a unique encryption key per project.
//
// The actual PROJECT_NAME, SALT, and CREATED_AT strings are placed here
// by the Kotlin ProjectGenerator at project creation time.
//
// This is a self-contained .cppm — no separate .cpp implementation file.
//==============================================================================

module;  // ── global module fragment (private to this TU) ──

// No includes needed — all constexpr, no runtime dependencies.

export module nexus.shared.script_protection;

// ═══════════════════════════════════════════════════════════════════════════
// nxs::runtime::script_protection — Compile-Time Encryption Config
// ═══════════════════════════════════════════════════════════════════════════

namespace nxs::runtime::script_protection {

/// Master switch for script encryption. When false, archives are stored as
/// plain text (easier for development/debugging). When true, the generator
/// injects real values below and ScriptArchive applies XOR encryption.
export constexpr bool ENABLED = false;

/// Project name from nxs_config.json — used as part of the encryption key.
/// The generator replaces this with the actual project name at creation time.
export constexpr const char* PROJECT_NAME = "";

/// Random salt generated at project creation — makes each project's key
/// unique even if two projects have the same name and creation date.
export constexpr const char* SALT = "";

/// ISO timestamp of project creation — adds time-based entropy to the key.
/// Format: "YYYY-MM-DDTHH:MM:SSZ" (ISO 8601).
export constexpr const char* CREATED_AT = "";

}  // namespace nxs::runtime::script_protection
