#!/usr/bin/env bash
# Linux Debian/Ubuntu test-gen entry point.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"
echo "test-gen (debian): optional gtest — apt install libgtest-dev"
exec "${SCRIPT_DIR}/generate-tests.sh" "$@"
