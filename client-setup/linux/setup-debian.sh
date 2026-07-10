#!/usr/bin/env bash
# Debian / Ubuntu — install Compose client prerequisites (JDK 26, Git, build tools).
set -euo pipefail

if ! command -v apt-get >/dev/null 2>&1; then
  echo "error: apt-get not found; this script is for Debian/Ubuntu." >&2
  exit 1
fi

echo "==> Debian/Ubuntu: installing JDK 26, Git, and build essentials..."
sudo apt-get update
sudo apt-get install -y \
  openjdk-26-jdk \
  git \
  build-essential

echo ""
echo "CMake and Ninja are optional — only needed when building generated C++ templates."
echo "  sudo apt-get install -y cmake ninja-build"
