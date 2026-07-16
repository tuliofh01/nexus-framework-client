#!/usr/bin/env bash
# build_app.sh — set up and build the {{projectName}} Android app.
#
# Acts as the bridge between the toolchains involved:
#   * Python  — venv + requirements.txt (host-side tooling; the on-device
#               Python runtime is embedded by Chaquopy through Gradle).
#   * Zig     — cross-compiles the JNI shared library (zig-services/) for
#               each Android ABI when the NDK is available.
#   * Gradle  — assembles the APK (Kotlin + Chaquopy + packaged .so files).
#
# Usage:
#   ./build_app.sh              # setup + zig .so + gradle assembleDebug
#   ./build_app.sh --setup-only # venv + checks only
#   ./build_app.sh --zig-only   # only rebuild the JNI .so files
#
# Requires: Zig 0.16.0, python3, JDK 17+, Android SDK.
#           Android NDK (API >= 29) for the Zig-built JNI library.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ZIG_DIR="$ROOT/zig-services"
VENV_DIR="$ROOT/.venv"
JNILIBS_DIR="$ROOT/app/src/main/jniLibs"
API_LEVEL="${ANDROID_API:-29}"

SETUP_ONLY=0
ZIG_ONLY=0
for arg in "$@"; do
  case "$arg" in
    --setup-only) SETUP_ONLY=1 ;;
    --zig-only) ZIG_ONLY=1 ;;
    -h|--help) sed -n '2,18p' "$0"; exit 0 ;;
  esac
done

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*" >&2; }
warn() { printf '\033[1;33mwarn:\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31merror:\033[0m %s\n' "$*" >&2; exit 1; }

# ── Locate SDK / NDK ────────────────────────────────────────────────────────
find_sdk() {
  for cand in "${ANDROID_HOME:-}" "${ANDROID_SDK_ROOT:-}" \
              "$HOME/Android/Sdk" /opt/android-sdk; do
    [[ -n "$cand" && -d "$cand/platforms" ]] && { echo "$cand"; return 0; }
  done
  return 1
}

find_ndk() {
  for cand in "${ANDROID_NDK:-}" "${ANDROID_NDK_HOME:-}"; do
    [[ -n "$cand" && -d "$cand/toolchains" ]] && { echo "$cand"; return 0; }
  done
  # newest side-by-side NDK under the SDK, if any
  if [[ -n "${SDK:-}" && -d "$SDK/ndk" ]]; then
    local newest
    newest="$(ls -1 "$SDK/ndk" 2>/dev/null | sort -V | tail -1)"
    [[ -n "$newest" ]] && { echo "$SDK/ndk/$newest"; return 0; }
  fi
  return 1
}

# ── Checks ──────────────────────────────────────────────────────────────────
log "Checking tools"
command -v python3 >/dev/null || die "missing command: python3"
command -v zig >/dev/null || warn "zig not found — JNI .so build will be skipped"

SDK="$(find_sdk || true)"
NDK="$(find_ndk || true)"
[[ -n "$SDK" ]] && log "Android SDK: $SDK" || warn "Android SDK not found (set ANDROID_HOME)"
[[ -n "$NDK" ]] && log "Android NDK: $NDK" || warn "Android NDK not found (set ANDROID_NDK) — using prebuilt/CMake .so path"

# ── Python venv (host tooling; Chaquopy pip is configured in Gradle) ───────
log "Python venv → $VENV_DIR"
[[ -d "$VENV_DIR" ]] || python3 -m venv "$VENV_DIR"
# shellcheck disable=SC1091
source "$VENV_DIR/bin/activate"
if [[ -f "$ROOT/requirements.txt" ]]; then
  log "pip install -r requirements.txt"
  pip install --quiet --upgrade pip
  pip install -r "$ROOT/requirements.txt"
fi

[[ "$SETUP_ONLY" == 1 ]] && { log "Setup complete."; exit 0; }

# ── build.zig.zon fingerprint ───────────────────────────────────────────────
# Zig requires .fingerprint where the high 32 bits equal crc32(package name).
# The template cannot pre-compute it for arbitrary project names, so patch it.
fix_zon_fingerprint() {
  local zon="$ZIG_DIR/build.zig.zon"
  [[ -f "$zon" ]] || return 0
  python3 - "$zon" <<'ZPY'
import re, sys, zlib
p = sys.argv[1]
t = open(p).read()
m = re.search(r'\.name\s*=\s*\.(?:@")?([A-Za-z0-9_]+)"?', t)
if not m:
    sys.exit(0)
want_hi = zlib.crc32(m.group(1).encode())
fp = re.search(r'\.fingerprint\s*=\s*0x([0-9a-fA-F]+)', t)
if fp and (int(fp.group(1), 16) >> 32) == want_hi:
    sys.exit(0)
new = (want_hi << 32) | 0xbaecc26b
if fp:
    t = t.replace(f"0x{fp.group(1)}", f"0x{new:016x}", 1)
else:
    t = re.sub(r'(\.version\s*=\s*"[^"]*",)', rf'\1\n    .fingerprint = 0x{new:016x},', t, count=1)
open(p, "w").write(t)
print(f"fixed fingerprint -> 0x{new:016x}", file=sys.stderr)
ZPY
}

# ── Zig-built JNI shared library per ABI ───────────────────────────────────
build_zig_so() {
  local ztarget="$1" abi="$2"
  log "zig build → $abi"
  ( cd "$ZIG_DIR" && zig build \
      -Dtarget="$ztarget" \
      -Doptimize=ReleaseSafe \
      ${NDK:+-Dandroid-ndk="$NDK"} \
      -Dandroid-api="$API_LEVEL" \
      --prefix "$ZIG_DIR/zig-out/$abi" )
  mkdir -p "$JNILIBS_DIR/$abi"
  cp -f "$ZIG_DIR/zig-out/$abi/lib/"lib*.so "$JNILIBS_DIR/$abi/" 2>/dev/null \
    || warn "no .so produced for $abi"
}

fix_zon_fingerprint

if command -v zig >/dev/null && [[ -n "$NDK" ]]; then
  build_zig_so aarch64-linux-android arm64-v8a
  build_zig_so x86_64-linux-android x86_64
elif command -v zig >/dev/null; then
  warn "Skipping Android .so cross-build (no NDK). Native host check only:"
  ( cd "$ZIG_DIR" && zig build --prefix "$ZIG_DIR/zig-out/host" )
fi

[[ "$ZIG_ONLY" == 1 ]] && { log "Zig JNI build complete."; exit 0; }

# ── Gradle APK ──────────────────────────────────────────────────────────────
if [[ -n "$SDK" ]]; then
  [[ -f "$ROOT/local.properties" ]] || printf 'sdk.dir=%s\n' "$SDK" > "$ROOT/local.properties"
  log "gradle :app:assembleDebug"
  ( cd "$ROOT" && ./gradlew --no-daemon :app:assembleDebug )
  log "APK: app/build/outputs/apk/debug/"
else
  warn "Android SDK missing — skipped Gradle. Install the SDK and rerun."
  exit 1
fi
