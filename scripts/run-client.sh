#!/usr/bin/env bash
# run-client.sh — launch the Nexus Framework Compose Desktop client with a
# Skiko renderer that works on the current machine.
#
# WHY THIS EXISTS
# ---------------
# Compose Desktop renders through Skia ("Skiko"). On some Linux/WSL2/NVIDIA
# combinations the default OpenGL path draws glitched buttons, corrupted
# text, or flickering panels. This launcher:
#
#   1. Honors an explicit NEXUS_RENDERER env var (OPENGL|SOFTWARE|DIRECT3D|METAL)
#   2. Otherwise auto-detects risky environments (WSL, XWayland, llvmpipe)
#      and falls back to SOFTWARE rendering (pure CPU — always correct)
#   3. Runs the client via Gradle.
#
# Usage:
#   ./scripts/run-client.sh                 # auto-detect
#   NEXUS_RENDERER=OPENGL ./scripts/run-client.sh
#   ./gradlew run -PnexusRenderer=SOFTWARE  # direct Gradle equivalent
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

renderer="${NEXUS_RENDERER:-}"

if [[ -z "$renderer" ]]; then
  # ── Auto-detection heuristics ────────────────────────────────────────────
  # WSL: /proc/version advertises microsoft. XWayland: session type shows
  # x11 under Wayland. llvmpipe: software GL already — hardware adds nothing.
  if grep -qi microsoft /proc/version 2>/dev/null; then
    renderer="SOFTWARE"
  elif [[ "${XDG_SESSION_TYPE:-}" == "wayland" ]] && [[ -n "${DISPLAY:-}" ]]; then
    renderer="SOFTWARE"   # XWayland bridge — glitch-prone with Skia GL
  elif glxinfo 2>/dev/null | grep -qi llvmpipe; then
    renderer="SOFTWARE"   # no working hardware GL
  else
    renderer="OPENGL"     # default hardware path
  fi
fi

echo "==> Skiko renderer: $renderer"
exec ./gradlew run --console=plain "-PnexusRenderer=$renderer" "$@"
