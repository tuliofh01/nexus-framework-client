#!/usr/bin/env bash
# Linux Fedora/RHEL test-gen entry point.
# Optional: dnf install gtest-devel
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=../common/env.sh
source "${SCRIPT_DIR}/../common/env.sh"
if command -v dnf >/dev/null 2>&1; then
  echo "test-gen (fedora): optional gtest — dnf install gtest-devel"
fi
exec "${SCRIPT_DIR}/../common/generate-tests.sh" "$@"
