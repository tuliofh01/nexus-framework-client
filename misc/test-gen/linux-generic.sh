#!/usr/bin/env bash
# Linux generic (POSIX) test-gen entry point.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "${SCRIPT_DIR}/../common/generate-tests.sh" "$@"
