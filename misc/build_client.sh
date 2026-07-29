#!/usr/bin/env bash
# build_client.sh — compile the Nexus Kotlin client / generator via Gradle.
#
# Analogous to template/*/build_app.sh for native apps: this builds the
# unified Framework client (Compose Desktop + generation engine + CLI).
#
# Usage (from anywhere in the repo):
#   ./misc/build_client.sh                 # accept license (once) then compile
#   ./misc/build_client.sh --clean         # clean then compile
#   ./misc/build_client.sh --test          # compile + test
#   ./misc/build_client.sh --deploy        # license + createDistributable → builds/clients/
#   ./misc/build_client.sh --package       # license + OS packages → builds/clients/.../packages/
#   ./misc/build_client.sh --accept-license  # non-interactive accept + stamp
#   ./misc/build_client.sh --show-license  # re-show dialog (clears stamp first)
#
# Prefer the repo-root wrapper for a full distributable build:
#   ./build_client.sh
#
# License: Nexus License (Nexus-1.0) — see LICENSE at the repo root.
#   • Non-commercial Toolkit + derived apps OK (with attribution)
#   • Through 2041-07-21, @tuliofh01 authorization needed for:
#       Toolkit commercial use; revenue-producing derived apps;
#       derived apps used in commercial institutions
#
# Requires: JDK compatible with the Gradle toolchain (see misc/client-setup/),
#           and the repo-root Gradle wrapper (./gradlew).
# Optional GUI: zenity, kdialog, or yad (falls back to terminal Y/N).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GRADLEW="$ROOT/gradlew"
LICENSE_FILE="$ROOT/LICENSE"
# Per-repo stamp so we do not nag after the first acceptance in this clone.
ACCEPT_STAMP="$ROOT/misc/.license-accepted"
LICENSE_ID="Nexus-1.0"

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*" >&2; }
warn() { printf '\033[1;33mwarn:\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31merror:\033[0m %s\n' "$*" >&2; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "missing command: $1"
}

usage() {
  cat <<'EOF' >&2
build_client.sh — compile Nexus Framework client via Gradle

Usage:
  ./misc/build_client.sh [options]

Options:
  --clean            Run Gradle clean before compile / deploy
  --test             Also run test (compile-only mode; deploy/package always run test)
  --deploy           Build + test + copy distributable to builds/clients/NexusFrameworkClient-<ver>/
  --package          Build + test + OS packages into builds/clients/.../packages/
  --skip-tests       Skip unit tests (not recommended; deploy normally depends on test)
  --accept-license   Accept the Nexus License without a dialog (writes stamp)
  --show-license     Clear stamp and show the license dialog again
  -h, --help         Show this help

Repo-root shortcut (sources misc/client-setup/env.sh, then --deploy which runs tests):
  ./build_client.sh

License (Nexus License / Nexus-1.0):
  Full text: LICENSE at the repository root
  • Non-commercial Toolkit + generated apps: allowed (with attribution)
  • Through 2041-07-21, prior authorization from Túlio Horta (@tuliofh01)
    is required for:
      - Commercial use of the Toolkit itself
      - Generated apps that produce revenue
      - Generated apps used in a commercial institution
  • After 2041-07-21 those authorization restrictions expire unless renewed;
    attribution continues
  • No warranty; author not liable for misuse of derived apps

Acceptance is stored once per clone in misc/.license-accepted.
EOF
}

CLEAN=0
RUN_TEST=0
DEPLOY=0
PACKAGE=0
SKIP_TESTS=0
ACCEPT_LICENSE=0
SHOW_LICENSE=0
for arg in "$@"; do
  case "$arg" in
    --clean) CLEAN=1 ;;
    --test) RUN_TEST=1 ;;
    --deploy) DEPLOY=1 ;;
    --package) PACKAGE=1 ;;
    --skip-tests) SKIP_TESTS=1 ;;
    --accept-license) ACCEPT_LICENSE=1 ;;
    --show-license) SHOW_LICENSE=1 ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      die "unknown argument: $arg (try --help)"
      ;;
  esac
done

if [[ "$DEPLOY" -eq 1 && "$PACKAGE" -eq 1 ]]; then
  die "use either --deploy or --package (not both)"
fi

write_accept_stamp() {
  mkdir -p "$(dirname "$ACCEPT_STAMP")"
  cat >"$ACCEPT_STAMP" <<EOF
# Nexus License acceptance stamp — do not commit if you prefer per-machine consent.
# Accepted: $(date -Iseconds 2>/dev/null || date)
# License: $LICENSE_ID
# Repo: https://github.com/tuliofh01/nexus-framework-client
accepted=true
EOF
}

license_summary_text() {
  cat <<EOF
Nexus License ($LICENSE_ID)

Full text: $LICENSE_FILE
Repo: https://github.com/tuliofh01/nexus-framework-client
Owner: Túlio Horta (@tuliofh01)

Key terms:
• Non-commercial use of the Toolkit and of generated apps is allowed
  (personal / hobby / non-commercial institutional), with attribution.
• Through 2041-07-21, prior written authorization from @tuliofh01 is
  required for: (1) commercial use of the Toolkit itself; (2) generated
  apps that produce revenue; (3) generated apps used in a commercial
  institution (company / for-profit workplace).
• After 2041-07-21 those authorization restrictions expire unless renewed;
  attribution continues.
• NO WARRANTY. The author is not responsible for criminal or unlawful
  use of derived apps.

Do you accept the Nexus License and wish to continue building?
EOF
}

# Returns 0 if accepted, 1 if declined.
prompt_license_gui_or_tty() {
  local summary
  summary="$(license_summary_text)"
  local title="Nexus License — Accept to build"
  # Only open GUI when interactive and a display is available (avoids hanging agents/CI).
  local use_gui=0
  if [[ -t 0 ]] && [[ -n "${DISPLAY:-}${WAYLAND_DISPLAY:-}" ]]; then
    use_gui=1
  fi

  if [[ "$use_gui" -eq 1 ]] && command -v zenity >/dev/null 2>&1; then
    # Prefer text-info with checkbox if supported; else question dialog.
    if zenity --help 2>&1 | grep -q -- '--text-info'; then
      if [[ -f "$LICENSE_FILE" ]]; then
        zenity --text-info --title="$title" --filename="$LICENSE_FILE" \
          --width=720 --height=520 --ok-label="Accept" --cancel-label="Decline" \
          --checkbox="I accept the Nexus License ($LICENSE_ID)" 2>/dev/null \
          && return 0
        # Older zenity without checkbox: fall through to question with summary
      fi
    fi
    zenity --question --title="$title" --width=520 \
      --ok-label="Accept" --cancel-label="Decline" \
      --text="$summary" 2>/dev/null && return 0
    return 1
  fi

  if [[ "$use_gui" -eq 1 ]] && command -v kdialog >/dev/null 2>&1; then
    if [[ -f "$LICENSE_FILE" ]]; then
      kdialog --textbox "$LICENSE_FILE" 720 520 --title "$title" >/dev/null 2>&1 || true
    fi
    kdialog --yesno "$summary" --title "$title" --yes-label "Accept" --no-label "Decline" \
      2>/dev/null && return 0
    return 1
  fi

  if [[ "$use_gui" -eq 1 ]] && command -v yad >/dev/null 2>&1; then
    if [[ -f "$LICENSE_FILE" ]]; then
      yad --text-info --filename="$LICENSE_FILE" --title="$title" \
        --width=720 --height=520 --button="Accept:0" --button="Decline:1" \
        2>/dev/null && return 0
      return 1
    fi
    yad --text="$summary" --title="$title" --width=520 \
      --button="Accept:0" --button="Decline:1" 2>/dev/null && return 0
    return 1
  fi

  # Terminal fallback
  printf '\n' >&2
  license_summary_text >&2
  printf '\n' >&2
  if [[ ! -t 0 ]]; then
    die "Nexus License not accepted (non-interactive). Re-run with --accept-license or in a terminal / with zenity|kdialog|yad."
  fi
  local reply
  read -r -p "Accept the Nexus License and continue? [y/N] " reply
  case "$reply" in
    y|Y|yes|YES) return 0 ;;
    *) return 1 ;;
  esac
}

ensure_license_accepted() {
  [[ -f "$LICENSE_FILE" ]] || die "missing LICENSE at $LICENSE_FILE"

  if [[ "$SHOW_LICENSE" -eq 1 ]]; then
    rm -f "$ACCEPT_STAMP"
    log "Cleared license stamp — will show Nexus License dialog"
  fi

  if [[ "$ACCEPT_LICENSE" -eq 1 ]]; then
    write_accept_stamp
    log "Nexus License accepted via --accept-license → $ACCEPT_STAMP"
    return 0
  fi

  if [[ -f "$ACCEPT_STAMP" ]] && grep -q 'accepted=true' "$ACCEPT_STAMP" 2>/dev/null; then
    log "Nexus License already accepted (see $ACCEPT_STAMP)"
    return 0
  fi

  log "Nexus License ($LICENSE_ID) — acceptance required before compile"
  if prompt_license_gui_or_tty; then
    write_accept_stamp
    log "Nexus License accepted → $ACCEPT_STAMP"
    return 0
  fi

  die "Nexus License declined — build cancelled (no Gradle run). Re-run and Accept, or use --accept-license."
}

main() {
  cd "$ROOT"

  [[ -f "$ROOT/settings.gradle.kts" ]] || die "not a Nexus Framework repo root (missing settings.gradle.kts)"
  [[ -f "$GRADLEW" ]] || die "missing Gradle wrapper at $GRADLEW"

  ensure_license_accepted

  need_cmd java
  log "Repo root → $ROOT"
  log "Java $(java -version 2>&1 | head -1)"

  if [[ ! -x "$GRADLEW" ]]; then
    warn "gradlew is not executable — chmod +x gradlew"
    chmod +x "$GRADLEW"
  fi

  # Optional env from first-run bootstrap (Zig path, etc.)
  if [[ -f "$ROOT/misc/client-setup/env.sh" ]]; then
    # shellcheck source=/dev/null
    source "$ROOT/misc/client-setup/env.sh"
    log "Sourced misc/client-setup/env.sh"
  fi

  if [[ "$CLEAN" -eq 1 ]]; then
    log "Cleaning Gradle build outputs"
    "$GRADLEW" --no-daemon clean
  fi

  local tasks=()
  if [[ "$PACKAGE" -eq 1 ]]; then
    log "Packaging Compose Desktop client → builds/clients/NexusFrameworkClient-<ver>/packages/"
    if [[ "$SKIP_TESTS" -eq 1 ]]; then
      warn "Skipping tests (--skip-tests); Gradle deployPackage still dependsOn test unless you use -x"
      tasks+=(deployPackageToBuildsClient)
      "$GRADLEW" --no-daemon -x test "${tasks[@]}"
    else
      log "Running unit tests before package (fail → no distributable)"
      tasks+=(deployPackageToBuildsClient)
      "$GRADLEW" --no-daemon "${tasks[@]}"
    fi
  elif [[ "$DEPLOY" -eq 1 ]]; then
    log "Deploying Compose Desktop distributable → builds/clients/NexusFrameworkClient-<ver>/"
    if [[ "$SKIP_TESTS" -eq 1 ]]; then
      warn "Skipping tests (--skip-tests)"
      tasks+=(deployToBuildsClient)
      "$GRADLEW" --no-daemon -x test "${tasks[@]}"
    else
      log "Running unit tests before deploy (fail → no distributable)"
      tasks+=(deployToBuildsClient)
      "$GRADLEW" --no-daemon "${tasks[@]}"
    fi
  else
    log "Compiling Kotlin Framework client (unified module)"
    tasks+=(compileKotlin)
    if [[ "$RUN_TEST" -eq 1 ]]; then
      log "Including test"
      tasks+=(test)
    fi
    "$GRADLEW" --no-daemon "${tasks[@]}"
  fi

  local client_out=""
  if [[ "$DEPLOY" -eq 1 || "$PACKAGE" -eq 1 ]]; then
    client_out="$(find "$ROOT/builds/clients" -maxdepth 1 -type d -name 'NexusFrameworkClient-*' 2>/dev/null | sort | tail -1 || true)"
  fi

  cat <<EOF

────────────────────────────────────────
  Build OK

  Next:
    ./gradlew run
    ./gradlew runCli --args="generate --type desktop --name MyApp --dry-run"
    ./misc/scripts/nexus-dev.sh generate -- --type desktop --name MyApp --dry-run
    ./build_client.sh                 # test + distributable → builds/clients/
    ./misc/build_client.sh --deploy   # same without sourcing env first

EOF
  if [[ -n "$client_out" ]]; then
    cat <<EOF
  Distributable:
    $client_out
EOF
  fi
  cat <<EOF

  Or re-run this script:
    ./misc/build_client.sh
    ./misc/build_client.sh --clean
    ./misc/build_client.sh --test
    ./misc/build_client.sh --deploy
    ./misc/build_client.sh --show-license   # review Nexus License again
────────────────────────────────────────
EOF
}

main "$@"
