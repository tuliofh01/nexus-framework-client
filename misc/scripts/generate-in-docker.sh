#!/usr/bin/env bash
# Backward-compat shim — use misc/scripts/dev/generate-in-docker.sh
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "${SCRIPT_DIR}/dev/generate-in-docker.sh" "$@"
