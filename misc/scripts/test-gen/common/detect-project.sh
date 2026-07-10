#!/usr/bin/env bash
# Locate and validate a generated Nexus project.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${SCRIPT_DIR}/env.sh"

detect_project() {
  local project_arg="${1:-}"
  local project_dir
  project_dir="$(resolve_project_dir "${project_arg}")"
  local config="${project_dir}/nxs_config.json"
  if [[ ! -f "${config}" ]]; then
    echo "error: missing ${config}" >&2
    return 1
  fi
  printf '%s\n' "${project_dir}"
}

read_template_type() {
  local config="$1"
  python3 - "${config}" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    cfg = json.load(f)
template = cfg.get("nexus", {}).get("template", "")
targets = cfg.get("targets", {})
if template == "android-app" or "android" in targets:
    print("android")
elif template == "desktop-app" or "desktop" in targets:
    print("desktop")
else:
    print("unknown")
PY
}

read_project_name() {
  local config="$1"
  python3 - "${config}" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    cfg = json.load(f)
print(cfg.get("project", {}).get("name", ""))
PY
}

read_application_id() {
  local config="$1"
  python3 - "${config}" <<'PY'
import json, sys
with open(sys.argv[1], encoding="utf-8") as f:
    cfg = json.load(f)
print(cfg.get("project", {}).get("applicationId", "com.nexus.app"))
PY
}
