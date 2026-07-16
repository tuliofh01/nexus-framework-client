#!/usr/bin/env bash
# Nexus Framework local dev workflow helper.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
cd "${ROOT}"

usage() {
  cat <<'EOF'
Usage: nexus-dev.sh <command> [args]

Commands:
  compile          Compile :core, :cli, :app
  test             Run ./gradlew check
  generate         Run CLI generate (pass args after --)
  client           Run Compose client (:app:run)
  docker           Run generate-in-docker.sh (pass args after --)

Examples:
  ./misc/scripts/nexus-dev.sh compile
  ./misc/scripts/nexus-dev.sh generate -- --type desktop --name MyApp --dry-run
  ./misc/scripts/nexus-dev.sh docker -- desktop MyApp builds/framework/MyApp
EOF
}

cmd="${1:-}"
shift || true

case "${cmd}" in
  compile)
    ./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin
    ;;
  test)
    ./gradlew check
    ;;
  generate)
    if [[ "${1:-}" == "--" ]]; then shift; fi
    ./gradlew :cli:run --args="generate $*"
    ;;
  client)
    ./gradlew :app:run
    ;;
  docker)
    if [[ "${1:-}" == "--" ]]; then shift; fi
    exec "${ROOT}/misc/scripts/generate-in-docker.sh" "$@"
    ;;
  -h|--help|help|"")
    usage
    ;;
  *)
    echo "error: unknown command '${cmd}'" >&2
    usage >&2
    exit 1
    ;;
esac
