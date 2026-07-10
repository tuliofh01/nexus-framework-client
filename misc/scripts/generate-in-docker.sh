#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

TYPE="${1:-desktop}"
NAME="${2:-MyApp}"
OUTPUT="${3:-builds/framework/${NAME}}"

docker compose -f misc/docker/docker-compose.yml run --rm framework-generate \
  --args="generate --type ${TYPE} --name ${NAME} --output ${OUTPUT}"
