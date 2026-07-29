#!/usr/bin/env bash
# build_client.sh - cold-clone → runnable Compose Desktop client under builds/clients/
#
# After: git clone … && cd Nexus-Framework
#   zig run misc/client-setup/setup.zig   # once: JDK hints + Zig 0.16.0
#   ./build_client.sh --accept-license    # license + createDistributable + deploy
# Output:
#   builds/clients/NexusFrameworkClient-1.1.0/
#
# Options (forwarded to misc/build_client.sh):
#   ./build_client.sh                 # deploy distributable (default)
#   ./build_client.sh --clean         # clean, then deploy
#   ./build_client.sh --package       # OS packages instead of runnable distro
#   ./build_client.sh --accept-license
#   ./build_client.sh --help
#
# Compile-only (no distributable): ./misc/build_client.sh
set -euo pipefail

die()  { printf '\033[1;31merror:\033[0m %s\n' "$*" >&2; exit 1; }
warn() { printf '\033[1;33mwarn:\033[0m %s\n' "$*" >&2; }
log()  { printf '\033[1;34m==>\033[0m %s\n' "$*" >&2; }

SCRIPT_PATH="${BASH_SOURCE[0]}"
# Ensure this script stays executable after clone on some filesystems.
chmod +x "$SCRIPT_PATH" 2>/dev/null || true
SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_PATH")" && pwd)"
ROOT="$SCRIPT_DIR"
cd "$ROOT"

[[ -f "$ROOT/settings.gradle.kts" ]] || die "not a Nexus Framework repo root (missing settings.gradle.kts)"
[[ -f "$ROOT/build.gradle.kts" ]] || die "missing build.gradle.kts"
[[ -f "$ROOT/gradlew" ]] || die "missing Gradle wrapper (gradlew). Re-clone or restore gradle/wrapper/"

GRADLEW="$ROOT/gradlew"
chmod +x "$GRADLEW" 2>/dev/null || true
[[ -x "$GRADLEW" ]] || die "gradlew is not executable and chmod failed"

MISC_BUILD="$ROOT/misc/build_client.sh"
[[ -f "$MISC_BUILD" ]] || die "missing $MISC_BUILD"
chmod +x "$MISC_BUILD" 2>/dev/null || true
[[ -x "$MISC_BUILD" ]] || die "misc/build_client.sh is not executable and chmod failed"

# First-run env (Zig path). Missing file is OK; JDK toolchain can still download via Foojay.
ENV_SH="$ROOT/misc/client-setup/env.sh"
if [[ -f "$ENV_SH" ]]; then
  # shellcheck source=/dev/null
  source "$ENV_SH"
  log "Sourced misc/client-setup/env.sh"
else
  warn "misc/client-setup/env.sh missing — run: zig run misc/client-setup/setup.zig"
fi

if ! command -v java >/dev/null 2>&1; then
  die "java not found on PATH. Install JDK 26 (Temurin) or run: zig run misc/client-setup/setup.zig"
fi

# Soft check — Gradle Foojay can provision the toolchain, but a local JDK helps cold clones.
java_line="$(java -version 2>&1 | head -1 || true)"
log "Java: $java_line"
if ! java -version 2>&1 | grep -Eq 'version "(1\.)?(2[6-9]|[3-9][0-9])'; then
  warn "JDK 26+ recommended. If Gradle fails on toolchain, install Temurin 26 or re-run setup.zig"
fi

mkdir -p "$ROOT/builds/clients"

case "${1:-}" in
  -h|--help)
    exec "$MISC_BUILD" --help
    ;;
  --package)
    exec "$MISC_BUILD" "$@"
    ;;
  *)
    # Default: full distributable -> builds/clients/NexusFrameworkClient-<ver>/
    exec "$MISC_BUILD" --deploy "$@"
    ;;
esac
