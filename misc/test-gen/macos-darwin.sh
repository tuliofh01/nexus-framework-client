#!/usr/bin/env bash
# macOS test-gen entry point.
# Optional: brew install googletest
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../common/env.sh
source "${SCRIPT_DIR}/../common/env.sh"
if command -v brew >/dev/null 2>&1; then
  echo "test-gen (darwin): optional gtest — brew install googletest"
fi
exec "${SCRIPT_DIR}/../common/generate-tests.sh" "$@"
