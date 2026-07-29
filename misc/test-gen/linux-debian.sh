#!/usr/bin/env bash
# Linux Debian/Ubuntu test-gen entry point.
# Optional: apt install libgtest-dev
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../common/env.sh
source "${SCRIPT_DIR}/../common/env.sh"
if command -v apt-get >/dev/null 2>&1; then
  echo "test-gen (debian): optional gtest — apt install libgtest-dev"
fi
exec "${SCRIPT_DIR}/../common/generate-tests.sh" "$@"
