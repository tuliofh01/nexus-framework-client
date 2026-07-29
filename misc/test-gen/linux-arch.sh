#!/usr/bin/env bash
# Linux Arch test-gen entry point.
# Optional: pacman -S --needed gtest
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../common/env.sh
source "${SCRIPT_DIR}/../common/env.sh"
if command -v pacman >/dev/null 2>&1; then
  echo "test-gen (arch): optional gtest — pacman -S gtest"
fi
exec "${SCRIPT_DIR}/../common/generate-tests.sh" "$@"
