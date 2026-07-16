#!/usr/bin/env bash
# Shared environment for test-gen scripts.
# Source after resolving SCRIPT_DIR:
#   source "${SCRIPT_DIR}/env.sh"
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TEST_GEN_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
NEXUS_ROOT="$(cd "${TEST_GEN_ROOT}/../.." && pwd)"

export SCRIPT_DIR TEST_GEN_ROOT NEXUS_ROOT
export BUILDS_DIR="${NEXUS_ROOT}/builds/framework"
export TEMPLATES_DIR="${SCRIPT_DIR}/templates"

# Optional client-setup env (JAVA_HOME for Android Gradle tests)
CLIENT_SETUP_ENV="${NEXUS_ROOT}/misc/client-setup/env.sh"
if [[ -f "${CLIENT_SETUP_ENV}" ]]; then
  # shellcheck disable=SC1090
  source "${CLIENT_SETUP_ENV}"
elif [[ -n "${JAVA_HOME:-}" ]]; then
  export PATH="${JAVA_HOME}/bin:${PATH}"
else
  for candidate in \
    /usr/lib/jvm/java-26-openjdk \
    /usr/lib/jvm/java-26-openjdk-amd64 \
    /usr/lib/jvm/java-26-openjdk-aarch64 \
    /opt/homebrew/opt/openjdk@26/libexec/openjdk.jdk/Contents/Home; do
    if [[ -d "${candidate}" ]]; then
      export JAVA_HOME="${candidate}"
      export PATH="${JAVA_HOME}/bin:${PATH}"
      break
    fi
  done
fi

resolve_project_dir() {
  local arg="${1:-}"
  if [[ -z "${arg}" ]]; then
    echo "error: --project <name> or path to generated app required" >&2
    return 1
  fi
  if [[ -d "${arg}" && -f "${arg}/nxs_config.json" ]]; then
    printf '%s\n' "$(cd "${arg}" && pwd)"
    return 0
  fi
  local candidate="${BUILDS_DIR}/${arg}"
  if [[ -d "${candidate}" && -f "${candidate}/nxs_config.json" ]]; then
    printf '%s\n' "$(cd "${candidate}" && pwd)"
    return 0
  fi
  echo "error: no nxs_config.json under '${arg}' or '${candidate}'" >&2
  return 1
}

project_name_from_dir() {
  basename "$1"
}
