#!/usr/bin/env bash
# Fedora / RHEL-family — install Compose client prerequisites (JDK 26, Git, build tools).
set -euo pipefail

if command -v dnf >/dev/null 2>&1; then
  PKG=dnf
elif command -v yum >/dev/null 2>&1; then
  PKG=yum
else
  echo "error: dnf/yum not found; this script is for Fedora/RHEL." >&2
  exit 1
fi

echo "==> Fedora/RHEL: installing JDK 26, Git, and development tools..."
sudo "$PKG" install -y \
  java-26-openjdk-devel \
  git \
  gcc \
  gcc-c++ \
  make

echo ""
echo "CMake and Ninja are optional — only needed when building generated C++ templates."
echo "  sudo $PKG install -y cmake ninja-build"
