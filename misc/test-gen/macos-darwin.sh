#!/usr/bin/env bash
# macOS test-gen entry point.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"
echo "test-gen (darwin): optional gtest — brew install googletest"
exec "${SCRIPT_DIR}/generate-tests.sh" "$@"
