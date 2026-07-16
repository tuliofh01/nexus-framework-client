#!/usr/bin/env bash
# Generate smoke-test stubs for a built Nexus app.
set -euo pipefail

TEST_GEN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=env.sh
source "${TEST_GEN_DIR}/env.sh"
# shellcheck source=detect-project.sh
source "${TEST_GEN_DIR}/detect-project.sh"

DRY_RUN=0
FORCE=0
PROJECT_ARG=""

usage() {
  cat <<'EOF'
Usage: generate-tests.sh [--dry-run] [--force] (--project <name> | <path>)

  --project <name>   Project under builds/framework/<name>
  <path>             Direct path to generated app root (nxs_config.json)
  --dry-run          Print planned writes without creating files
  --force            Overwrite files that contain the nexus-test-gen marker
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    --force) FORCE=1; shift ;;
    --project) PROJECT_ARG="${2:-}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    --) shift; break ;;
    -*) echo "error: unknown option $1" >&2; usage >&2; exit 1 ;;
    *) PROJECT_ARG="$1"; shift ;;
  esac
done

if [[ -z "${PROJECT_ARG}" ]]; then
  usage >&2
  exit 1
fi

PROJECT_DIR="$(detect_project "${PROJECT_ARG}")"
CONFIG="${PROJECT_DIR}/nxs_config.json"
TEMPLATE_TYPE="$(read_template_type "${CONFIG}")"
PROJECT_NAME="$(read_project_name "${CONFIG}")"
[[ -n "${PROJECT_NAME}" ]] || PROJECT_NAME="$(project_name_from_dir "${PROJECT_DIR}")"

MARKER="nexus-test-gen: generated"
GENERATED_AT="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

should_write() {
  local target="$1"
  if [[ ! -f "${target}" ]]; then
    return 0
  fi
  if [[ "${FORCE}" -eq 1 ]]; then
    return 0
  fi
  if grep -q "${MARKER}" "${target}" 2>/dev/null; then
    echo "skip (already generated): ${target}"
    return 1
  fi
  echo "skip (exists, not generated — use --force): ${target}"
  return 1
}

write_file() {
  local rel="$1"
  local content="$2"
  local target="${PROJECT_DIR}/${rel}"
  if ! should_write "${target}"; then
    return 0
  fi
  if [[ "${DRY_RUN}" -eq 1 ]]; then
    echo "would write: ${target}"
    return 0
  fi
  mkdir -p "$(dirname "${target}")"
  printf '%s' "${content}" > "${target}"
  echo "wrote: ${target}"
}

render_template() {
  local template_file="$1"
  shift
  local content
  content="$(<"${template_file}")"
  while [[ $# -ge 2 ]]; do
    local key="$1"
    local val="$2"
    content="${content//${key}/${val}}"
    shift 2
  done
  printf '%s' "${content}"
}

PLANNED_FILES=()

emit_desktop() {
  local smoke_cpp run_sh readme_fragment
  smoke_cpp="$(render_template "${TEMPLATES_DIR}/smoke_test.cpp.tpl" \
    "{{PROJECT_NAME}}" "${PROJECT_NAME}" \
    "{{MARKER}}" "${MARKER}")"
  run_sh="$(render_template "${TEMPLATES_DIR}/run_smoke.sh.tpl" \
    "{{PROJECT_NAME}}" "${PROJECT_NAME}" \
    "{{MARKER}}" "${MARKER}")"
  readme_fragment="$(render_template "${TEMPLATES_DIR}/README.desktop.fragment.md.tpl" \
    "{{PROJECT_NAME}}" "${PROJECT_NAME}" \
    "{{MARKER}}" "${MARKER}")"

  PLANNED_FILES=(
    "tests/smoke_test.cpp"
    "tests/run_smoke.sh"
    "tests/nexus_generated/README.fragment.md"
    "tests/nexus_generated/metadata.json"
  )
  write_file "tests/smoke_test.cpp" "${smoke_cpp}"
  write_file "tests/run_smoke.sh" "${run_sh}"
  write_file "tests/nexus_generated/README.fragment.md" "${readme_fragment}"

  if [[ "${DRY_RUN}" -eq 0 ]]; then
    chmod +x "${PROJECT_DIR}/tests/run_smoke.sh" 2>/dev/null || true
  fi
}

emit_android() {
  local app_id kotlin_pkg kotlin_path readme_fragment
  app_id="$(read_application_id "${CONFIG}")"
  kotlin_pkg="${app_id}"
  kotlin_path="app/src/androidTest/java/$(echo "${kotlin_pkg}" | tr '.' '/')"
  local smoke_kt
  smoke_kt="$(render_template "${TEMPLATES_DIR}/SmokeInstrumentedTest.kt.tpl" \
    "{{PACKAGE_NAME}}" "${kotlin_pkg}" \
    "{{PROJECT_NAME}}" "${PROJECT_NAME}" \
    "{{MARKER}}" "${MARKER}")"
  readme_fragment="$(render_template "${TEMPLATES_DIR}/README.android.fragment.md.tpl" \
    "{{PROJECT_NAME}}" "${PROJECT_NAME}" \
    "{{MARKER}}" "${MARKER}")"

  PLANNED_FILES=(
    "${kotlin_path}/SmokeInstrumentedTest.kt"
    "tests/nexus_generated/README.fragment.md"
    "tests/nexus_generated/metadata.json"
  )
  write_file "${kotlin_path}/SmokeInstrumentedTest.kt" "${smoke_kt}"
  write_file "tests/nexus_generated/README.fragment.md" "${readme_fragment}"
}

emit_metadata() {
  local files_json=""
  local f
  for f in "${PLANNED_FILES[@]}"; do
    if [[ -n "${files_json}" ]]; then files_json+=", "; fi
    files_json+="\"${f}\""
  done
  local metadata
  metadata="$(cat <<EOF
{
  "marker": "${MARKER}",
  "generatedAt": "${GENERATED_AT}",
  "project": "${PROJECT_NAME}",
  "template": "${TEMPLATE_TYPE}",
  "projectDir": "${PROJECT_DIR}",
  "files": [${files_json}]
}
EOF
)"
  write_file "tests/nexus_generated/metadata.json" "${metadata}"
}

echo "test-gen: project=${PROJECT_NAME} type=${TEMPLATE_TYPE} dir=${PROJECT_DIR}"

case "${TEMPLATE_TYPE}" in
  desktop) emit_desktop ;;
  android) emit_android ;;
  *)
    echo "error: unsupported template type '${TEMPLATE_TYPE}' in ${CONFIG}" >&2
    exit 1
    ;;
esac

emit_metadata
echo "test-gen: done"
