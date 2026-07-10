#!/usr/bin/env bash
# {{MARKER}}
# Build (if needed) and run {{PROJECT_NAME}} binary for a quick smoke check.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT}"

PRESET="${CMAKE_PRESET:-debug}"
BUILD_DIR="${ROOT}/../../builds/framework/{{PROJECT_NAME}}/${PRESET}"
BINARY="${BUILD_DIR}/{{PROJECT_NAME}}"

if [[ ! -x "${BINARY}" ]]; then
  if [[ -f CMakePresets.json ]]; then
    echo "Binary missing — configuring preset '${PRESET}'..."
    cmake --preset "${PRESET}"
    cmake --build --preset "${PRESET}"
  else
    echo "error: no binary at ${BINARY} and no CMakePresets.json" >&2
    exit 1
  fi
fi

echo "Running smoke: ${BINARY}"
"${BINARY}" --smoke-test 2>/dev/null || "${BINARY}" &
PID=$!
sleep 1
if kill -0 "${PID}" 2>/dev/null; then
  kill "${PID}" 2>/dev/null || true
  echo "smoke: process started successfully"
  exit 0
fi
wait "${PID}"
echo "smoke: exited"
