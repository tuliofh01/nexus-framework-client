#!/usr/bin/env bash
# Arch Linux — install Compose client prerequisites (JDK 26, Git, build tools).
set -euo pipefail

if ! command -v pacman >/dev/null 2>&1; then
  echo "error: pacman not found; this script is for Arch Linux." >&2
  exit 1
fi

echo "==> Arch Linux: installing JDK 26, Git, and base build tools..."
sudo pacman -Sy --needed \
  jdk-openjdk \
  git \
  base-devel

echo ""
echo "CMake and Ninja are optional — only needed when building generated C++ templates."
echo "  sudo pacman -S --needed cmake ninja"
