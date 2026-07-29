#!/usr/bin/env bash
# build_client.sh - package the Nexus Compose Desktop client into builds/clients/
#
# Sources misc/client-setup/env.sh (Zig/JDK path hints from first-run setup),
# accepts the Nexus License once per clone, then runs Gradle
# deployToBuildsClient -> builds/clients/NexusFrameworkClient-<version>/
#
# Usage from repo root:
#   ./build_client.sh                 # license + distributable deploy
#   ./build_client.sh --clean         # clean, then deploy
#   ./build_client.sh --package       # OS packages instead of runnable distro
#   ./build_client.sh --accept-license
#   ./build_client.sh --help
#
# Compile-only (no distributable): ./misc/build_client.sh
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
SCRIPT_DIR="$(cd "$(dirname "$SCRIPT_PATH")" && pwd)"
ROOT="$SCRIPT_DIR"
cd "$ROOT"

ENV_SH="$ROOT/misc/client-setup/env.sh"
if [[ -f "$ENV_SH" ]]; then
  # shellcheck source=/dev/null
  source "$ENV_SH"
fi

MISC_BUILD="$ROOT/misc/build_client.sh"
if [[ ! -x "$MISC_BUILD" ]]; then
  chmod +x "$MISC_BUILD"
fi

case "${1:-}" in
  -h|--help)
    exec "$MISC_BUILD" --help
    ;;
  --package)
    exec "$MISC_BUILD" "$@"
    ;;
  *)
    # Default: full distributable -> builds/clients/NexusFrameworkClient-<ver>/
    exec "$MISC_BUILD" --deploy "$@"
    ;;
esac
