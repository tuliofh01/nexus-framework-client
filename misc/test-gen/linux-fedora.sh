#!/usr/bin/env bash
# Linux Fedora/RHEL test-gen entry point.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"
echo "test-gen (fedora): optional gtest — dnf install gtest-devel"
exec "${SCRIPT_DIR}/generate-tests.sh" "$@"
