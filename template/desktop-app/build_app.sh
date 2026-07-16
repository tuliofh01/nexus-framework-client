#!/usr/bin/env bash
# build_app.sh — set up deps, Python venv, and build a Nexus desktop app.
#
# Usage (from the generated project root):
#   ./build_app.sh              # setup + build
#   ./build_app.sh --setup-only # venv + dep fetch only
#   ./build_app.sh --clean      # wipe build/ and reconfigure
#
# Requires: Zig 0.16.0, g++ 14+ with -fmodules-ts, pkg-config,
#           SDL3, Lua 5.4, Python 3.x (+dev), OpenGL.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ZIG_DIR="$ROOT/zig-services"
BUILD_DIR="$ROOT/build"
VENV_DIR="$ROOT/.venv"
DEPS_DIR="$ZIG_DIR/deps"
JOBS="${JOBS:-$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4)}"

SETUP_ONLY=0
CLEAN=0
for arg in "$@"; do
  case "$arg" in
    --setup-only) SETUP_ONLY=1 ;;
    --clean) CLEAN=1 ;;
    -h|--help)
      sed -n '2,12p' "$0"
      exit 0
      ;;
  esac
done

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*" >&2; }
warn() { printf '\033[1;33mwarn:\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31merror:\033[0m %s\n' "$*" >&2; exit 1; }

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "missing command: $1"
}

# ── Resolve project name from nxs_config.json ──────────────────────────────
PROJECT_NAME="App"
if [[ -f "$ROOT/nxs_config.json" ]]; then
  if command -v python3 >/dev/null 2>&1; then
    PROJECT_NAME="$(python3 -c "import json; print(json.load(open('$ROOT/nxs_config.json'))['project']['name'])" 2>/dev/null || echo App)"
  fi
fi

# ── Toolchain checks ───────────────────────────────────────────────────────
check_tools() {
  log "Checking tools"
  need_cmd zig
  need_cmd g++
  need_cmd pkg-config
  need_cmd python3
  need_cmd curl
  need_cmd tar

  local zig_ver
  zig_ver="$(zig version)"
  log "Zig $zig_ver"
  [[ "$zig_ver" == "0.16.0" ]] ||
    die "Zig 0.16.0 is required (found $zig_ver)"

  pkg-config --exists sdl3 || die "SDL3 not found (pkg-config sdl3). Install libsdl3-dev / sdl3."
  pkg-config --exists lua5.4 || pkg-config --exists lua54 || die "Lua 5.4 not found (liblua5.4-dev)."
  pkg-config --exists python3-embed || pkg-config --exists python3 || die "Python3 embed headers missing."
}

# ── Python venv ─────────────────────────────────────────────────────────────
setup_venv() {
  log "Python venv → $VENV_DIR"
  if [[ ! -d "$VENV_DIR" ]]; then
    python3 -m venv "$VENV_DIR"
  fi
  # shellcheck disable=SC1091
  source "$VENV_DIR/bin/activate"
  python -m pip install --upgrade pip wheel >/dev/null
  if [[ -f "$ROOT/requirements.txt" ]]; then
    log "pip install -r requirements.txt"
    python -m pip install -r "$ROOT/requirements.txt"
  else
    warn "no requirements.txt — skipping pip install"
  fi
  # Export so pybind11 / embedded interpreter can find the venv
  export VIRTUAL_ENV="$VENV_DIR"
  export PATH="$VENV_DIR/bin:$PATH"
  if [[ -d "$VENV_DIR/include" ]]; then
    export CPATH="${VENV_DIR}/include:${CPATH:-}"
  fi
}

# ── Fetch third-party C++ deps into zig-services/deps ───────────────────────
fetch_tarball() {
  local name="$1"
  local url="$2"
  local dest="$DEPS_DIR/$name"
  if [[ -d "$dest" ]]; then
    return 0
  fi
  log "Fetching $name"
  mkdir -p "$DEPS_DIR"
  local tmp
  tmp="$(mktemp -d)"
  curl -fsSL "$url" -o "$tmp/pkg.tgz"
  mkdir -p "$tmp/extract"
  tar -xzf "$tmp/pkg.tgz" -C "$tmp/extract"
  # unwrap single top-level directory
  local inner
  inner="$(find "$tmp/extract" -mindepth 1 -maxdepth 1 -type d | head -1)"
  mv "$inner" "$dest"
  rm -rf "$tmp"
}

setup_deps() {
  mkdir -p "$DEPS_DIR"

  # Prefer already-fetched zig-pkg trees when present
  local zig_pkg="$ZIG_DIR/zig-pkg"
  if [[ -d "$zig_pkg" ]]; then
    for hash_dir in "$zig_pkg"/*; do
      [[ -d "$hash_dir" ]] || continue
      if [[ -f "$hash_dir/imgui.h" && ! -e "$DEPS_DIR/imgui" ]]; then
        ln -sfn "$hash_dir" "$DEPS_DIR/imgui"
        log "Using zig-pkg imgui → deps/imgui"
      fi
      if [[ -d "$hash_dir/include/sol" && ! -e "$DEPS_DIR/sol2" ]]; then
        ln -sfn "$hash_dir" "$DEPS_DIR/sol2"
        log "Using zig-pkg sol2 → deps/sol2"
      fi
      if [[ -d "$hash_dir/include/pybind11" && ! -e "$DEPS_DIR/pybind11" ]]; then
        ln -sfn "$hash_dir" "$DEPS_DIR/pybind11"
        log "Using zig-pkg pybind11 → deps/pybind11"
      fi
    done
  fi

  fetch_tarball imgui   "https://github.com/ocornut/imgui/archive/refs/tags/v1.91.8.tar.gz"
  fetch_tarball sol2    "https://github.com/ThePhD/sol2/archive/refs/tags/v3.3.1.tar.gz"
  fetch_tarball pybind11 "https://github.com/pybind/pybind11/archive/refs/tags/v2.13.6.tar.gz"
  fetch_tarball implot  "https://github.com/epezent/implot/archive/refs/tags/v0.16.tar.gz"
  fetch_tarball imnodes "https://github.com/Nelarius/imnodes/archive/refs/tags/v0.5.tar.gz"

  [[ -f "$DEPS_DIR/imgui/imgui.h" ]] || die "imgui missing under $DEPS_DIR/imgui"
  [[ -d "$DEPS_DIR/sol2/include" || -d "$DEPS_DIR/sol2/include/sol" || -f "$DEPS_DIR/sol2/include/sol/sol.hpp" ]] \
    || [[ -f "$DEPS_DIR/sol2/sol/sol.hpp" ]] \
    || die "sol2 headers missing"

  patch_sol2
}

# sol2 3.3.1 ships an invalid template body in optional<T&>::emplace (calls a
# nonexistent 'construct' member and never returns). GCC 14+ diagnoses broken
# template bodies even when uninstantiated inside module units, so delete it.
patch_sol2() {
  local impl
  for impl in "$DEPS_DIR/sol2/include/sol/optional_implementation.hpp" \
              "$DEPS_DIR/sol2/sol/optional_implementation.hpp"; do
    [[ -f "$impl" ]] || continue
    grep -q "nexus patch" "$impl" && return 0
    python3 - "$impl" <<'PY'
import sys, pathlib
p = pathlib.Path(sys.argv[1])
old = """\t\ttemplate <class... Args>
\t\tT& emplace(Args&&... args) noexcept {
\t\t\tstatic_assert(std::is_constructible<T, Args&&...>::value, "T must be constructible with Args");

\t\t\t*this = nullopt;
\t\t\tthis->construct(std::forward<Args>(args)...);
\t\t}"""
new = """\t\t// [nexus patch] invalid body upstream (no 'construct' member, no
\t\t// return). GCC 14+ rejects it inside module units, so delete it.
\t\ttemplate <class... Args>
\t\tT& emplace(Args&&... args) noexcept = delete;"""
t = p.read_text()
if old in t:
    p.write_text(t.replace(old, new))
    print(f"patched sol2: {p}")
PY
  done
}

resolve_include() {
  # Print -I flags for deps
  local flags=()
  flags+=("-I$DEPS_DIR/imgui" "-I$DEPS_DIR/imgui/backends")
  flags+=("-I$DEPS_DIR/implot" "-I$DEPS_DIR/imnodes")
  if [[ -d "$DEPS_DIR/sol2/include" ]]; then
    flags+=("-I$DEPS_DIR/sol2/include")
  else
    flags+=("-I$DEPS_DIR/sol2")
  fi
  if [[ -d "$DEPS_DIR/pybind11/include" ]]; then
    flags+=("-I$DEPS_DIR/pybind11/include")
  else
    flags+=("-I$DEPS_DIR/pybind11")
  fi
  flags+=("-I$ROOT/shared/runtime" "-I$ROOT/src" "-I$ROOT/src/bridge" "-I$ZIG_DIR/c_abi")
  local word
  for word in $(pkg-config --cflags sdl3); do flags+=("$word"); done
  if pkg-config --exists lua5.4; then
    for word in $(pkg-config --cflags lua5.4); do flags+=("$word"); done
  else
    for word in $(pkg-config --cflags lua54); do flags+=("$word"); done
  fi
  if pkg-config --exists python3-embed; then
    for word in $(pkg-config --cflags python3-embed); do flags+=("$word"); done
  else
    for word in $(pkg-config --cflags python3); do flags+=("$word"); done
  fi
  # Prefer venv python includes when present
  local py_ver
  py_ver="$(python3 -c 'import sys; print(f"{sys.version_info.major}.{sys.version_info.minor}")')"
  if [[ -d "$VENV_DIR/include/python${py_ver}" ]]; then
    flags+=("-I$VENV_DIR/include/python${py_ver}")
  fi
  printf '%s\n' "${flags[@]}"
}

# ── Zig C ABI library ──────────────────────────────────────────────────────
# ── build.zig.zon fingerprint ───────────────────────────────────────────────
# Zig requires .fingerprint where the high 32 bits equal crc32(package name).
# The template cannot pre-compute it for arbitrary project names, so patch it.
fix_zon_fingerprint() {
  local zon="$ZIG_DIR/build.zig.zon"
  [[ -f "$zon" ]] || return 0
  python3 - "$zon" <<'ZPY'
import re, sys, zlib
p = sys.argv[1]
t = open(p).read()
m = re.search(r'\.name\s*=\s*\.(?:@")?([A-Za-z0-9_]+)"?', t)
if not m:
    sys.exit(0)
want_hi = zlib.crc32(m.group(1).encode())
fp = re.search(r'\.fingerprint\s*=\s*0x([0-9a-fA-F]+)', t)
if fp and (int(fp.group(1), 16) >> 32) == want_hi:
    sys.exit(0)
new = (want_hi << 32) | 0xbaecc26b
if fp:
    t = t.replace(f"0x{fp.group(1)}", f"0x{new:016x}", 1)
else:
    t = re.sub(r'(\.version\s*=\s*"[^"]*",)', rf'\1\n    .fingerprint = 0x{new:016x},', t, count=1)
open(p, "w").write(t)
print(f"fixed fingerprint -> 0x{new:016x}", file=sys.stderr)
ZPY
}

build_zig() {
  log "Building nexus_zig (Zig)"
  fix_zon_fingerprint
  mkdir -p "$BUILD_DIR/zig-cache" "$BUILD_DIR/zig-out"
  (
    cd "$ZIG_DIR"
    zig build \
      --cache-dir "$BUILD_DIR/zig-cache" \
      --prefix "$BUILD_DIR/zig-out" \
      -Doptimize=Debug
  )
  # Locate static lib
  local lib
  lib="$(find "$BUILD_DIR/zig-out" -name 'libnexus_zig.a' | head -1)"
  [[ -n "$lib" ]] || die "libnexus_zig.a not produced"
  echo "$lib"
}

# ── Dear ImGui static library (g++) ─────────────────────────────────────────
build_imgui() {
  log "Building imgui_bundle.a"
  local objdir="$BUILD_DIR/obj/imgui"
  mkdir -p "$objdir"
  local incs
  mapfile -t incs < <(resolve_include)
  local srcs=(
    "$DEPS_DIR/imgui/imgui.cpp"
    "$DEPS_DIR/imgui/imgui_draw.cpp"
    "$DEPS_DIR/imgui/imgui_tables.cpp"
    "$DEPS_DIR/imgui/imgui_widgets.cpp"
    "$DEPS_DIR/imgui/backends/imgui_impl_sdl3.cpp"
    "$DEPS_DIR/imgui/backends/imgui_impl_opengl3.cpp"
    "$DEPS_DIR/implot/implot.cpp"
    "$DEPS_DIR/implot/implot_items.cpp"
    "$DEPS_DIR/imnodes/imnodes.cpp"
  )
  local objs=()
  local src base obj
  for src in "${srcs[@]}"; do
    [[ -f "$src" ]] || die "missing source: $src"
    base="$(basename "$src" .cpp)"
    obj="$objdir/${base}.o"
    if [[ ! -f "$obj" || "$src" -nt "$obj" ]]; then
      g++ -std=c++20 -O2 -fPIC -DIMNODES_NAMESPACE=imnodes \
        -DIMGUI_DEFINE_MATH_OPERATORS \
        "${incs[@]}" -c "$src" -o "$obj"
    fi
    objs+=("$obj")
  done
  local out="$BUILD_DIR/lib/libimgui_bundle.a"
  mkdir -p "$BUILD_DIR/lib"
  ar rcs "$out" "${objs[@]}"
  echo "$out"
}

# ── C++20 modules (g++ -fmodules-ts) ────────────────────────────────────────
compile_module() {
  local src="$1"
  local obj="$2"
  shift 2
  local incs=("$@")
  mkdir -p "$(dirname "$obj")"
  # gcm.cache must live in BUILD_DIR so all TUs share BMI files
  g++ -std=c++23 -fmodules-ts -fPIC -O2 -DNXS_ZIG_LINKED -Wno-template-body \
    -fmodule-mapper=|"$BUILD_DIR/module-mapper.sh" \
    "${incs[@]}" \
    -c "$src" -o "$obj"
}

# Fallback without mapper: compile from BUILD_DIR so gcm.cache is shared
compile_module_cwd() {
  local src="$1"
  local obj="$2"
  shift 2
  local incs=("$@")
  mkdir -p "$(dirname "$obj")"
  ( cd "$BUILD_DIR" && g++ -std=c++23 -fmodules-ts -fPIC -O2 -DNXS_ZIG_LINKED -Wno-template-body \
      "${incs[@]}" -c "$src" -o "$obj" )
}

build_cxx() {
  log "Compiling C++20 modules (g++)"
  local incs
  mapfile -t incs < <(resolve_include)
  # Make include paths absolute for cwd-relative compiles
  local abs_incs=()
  local f
  for f in "${incs[@]}"; do
    if [[ "$f" == -I/* ]]; then
      abs_incs+=("$f")
    elif [[ "$f" == -I* ]]; then
      abs_incs+=("-I$ROOT/${f#-I}")
    else
      abs_incs+=("$f")
    fi
  done

  mkdir -p "$BUILD_DIR/obj" "$BUILD_DIR/gcm.cache"
  # BMIs (gcm.cache) and objects must stay in sync: if the cache was wiped,
  # stale .o timestamps would skip recompiles and importers would fail with
  # "failed to read compiled module". Force a full module rebuild instead.
  if [[ -z "$(ls -A "$BUILD_DIR/gcm.cache" 2>/dev/null)" ]]; then
    rm -f "$BUILD_DIR/obj/"*.o
  fi
  # Work inside BUILD_DIR so gcm.cache is reused
  local GCM="$BUILD_DIR"
  local objs=()

  compile_one() {
    local rel="$1"
    local src="$ROOT/$rel"
    local obj="$BUILD_DIR/obj/${rel//\//_}.o"
    mkdir -p "$(dirname "$obj")"
    if [[ ! -f "$obj" || "$src" -nt "$obj" ]]; then
      log "  g++ $rel"
      ( cd "$GCM" && g++ -std=c++23 -fmodules-ts -fPIC -O2 -DNXS_ZIG_LINKED -Wno-template-body \
          "${abs_incs[@]}" -c "$src" -o "$obj" )
    fi
    objs+=("$obj")
  }

  # Shared leaves → shared dependents → app model → controllers → view/services → main
  compile_one shared/runtime/script_protection.cppm
  compile_one shared/runtime/script_crypto.cppm
  compile_one shared/runtime/paths.cppm
  compile_one shared/runtime/zig_allocator.cppm
  compile_one shared/runtime/font_config.cppm
  compile_one shared/runtime/nexus_theme.cppm
  compile_one shared/runtime/script_archive.cppm

  compile_one src/model/AppModel.cppm
  compile_one src/model/FunctionRegistry.cppm
  compile_one src/controller/PythonEngine.cppm
  compile_one src/controller/AppController.cppm
  compile_one src/controller/PlotController.cppm
  compile_one src/view/AppView.cppm
  compile_one src/view/LuaPanels.cppm
  compile_one src/service/FlowRunner.cppm
  if [[ -f "$ROOT/src/bridge/NexusBridge.cppm" ]]; then
    compile_one src/bridge/NexusBridge.cppm
  fi
  compile_one src/main.cpp

  # Persist object list
  printf '%s\n' "${objs[@]}" > "$BUILD_DIR/objects.list"
}

link_app() {
  log "Linking $PROJECT_NAME"
  local imgui_lib="$1"
  local zig_lib="$2"
  mapfile -t objs < "$BUILD_DIR/objects.list"

  local ldflags=()
  local word
  for word in $(pkg-config --libs sdl3); do ldflags+=("$word"); done
  if pkg-config --exists lua5.4; then
    for word in $(pkg-config --libs lua5.4); do ldflags+=("$word"); done
  else
    for word in $(pkg-config --libs lua54); do ldflags+=("$word"); done
  fi
  if pkg-config --exists python3-embed; then
    for word in $(pkg-config --libs python3-embed); do ldflags+=("$word"); done
  else
    for word in $(pkg-config --libs python3); do ldflags+=("$word"); done
  fi
  ldflags+=(-lGL -lpthread -ldl -lm)

  local out="$BUILD_DIR/bin/$PROJECT_NAME"
  mkdir -p "$BUILD_DIR/bin"
  g++ -std=c++20 -o "$out" "${objs[@]}" \
    "$imgui_lib" "$zig_lib" \
    "${ldflags[@]}"
  log "Built $out"
  # Convenience symlink matching zig-out layout
  mkdir -p "$ROOT/zig-out/bin"
  ln -sfn "$out" "$ROOT/zig-out/bin/$PROJECT_NAME"
}

# ── Sync script_protection.cppm with generated ScriptProtectionConfig.hpp ──
fix_script_protection() {
  local cppm="$ROOT/shared/runtime/script_protection.cppm"
  local hdr="$ROOT/shared/runtime/ScriptProtectionConfig.hpp"
  [[ -f "$cppm" && -f "$hdr" ]] || return 0
  log "Syncing script_protection.cppm from ScriptProtectionConfig.hpp"
  python3 - "$hdr" "$cppm" <<'PY'
import re, sys
hdr, cppm = sys.argv[1], sys.argv[2]
text = open(hdr).read()
def grab(name, default):
    m = re.search(rf'constexpr\s+(?:bool|const char\*)\s+{name}\s*=\s*([^;]+);', text)
    return m.group(1).strip() if m else default
enabled = grab("ENABLED", "false")
project = grab("PROJECT_NAME", '""')
salt = grab("SALT", '""')
created = grab("CREATED_AT", '""')
open(cppm, "w").write(f'''//==============================================================================
// nexus.shared.script_protection — generated values (from ScriptProtectionConfig.hpp)
//==============================================================================
module;

export module nexus.shared.script_protection;

namespace nxs::runtime::script_protection {{

export constexpr bool ENABLED = {enabled};
export constexpr const char* PROJECT_NAME = {project};
export constexpr const char* SALT = {salt};
export constexpr const char* CREATED_AT = {created};

}}  // namespace nxs::runtime::script_protection
''')
PY
}

# ── Main ────────────────────────────────────────────────────────────────────
main() {
  [[ -f "$ROOT/nxs_config.json" ]] || die "run from a generated Nexus project root (missing nxs_config.json)"
  [[ -d "$ZIG_DIR" ]] || die "missing zig-services/ — is this a Nexus desktop template?"

  if [[ "$CLEAN" -eq 1 ]]; then
    log "Cleaning $BUILD_DIR"
    rm -rf "$BUILD_DIR" "$ROOT/zig-out"
  fi

  check_tools
  setup_venv
  setup_deps
  fix_script_protection

  if [[ "$SETUP_ONLY" -eq 1 ]]; then
    log "Setup complete (--setup-only)"
    exit 0
  fi

  local zig_lib imgui_lib
  zig_lib="$(build_zig)"
  imgui_lib="$(build_imgui)"
  build_cxx
  link_app "$imgui_lib" "$zig_lib"

  cat <<EOF

────────────────────────────────────────
  Build OK

  Run:
    source .venv/bin/activate
    ./build/bin/$PROJECT_NAME
    # or:  ./zig-out/bin/$PROJECT_NAME

  Smoke (Zig C ABI):
    (cd zig-services && zig build smoke --prefix ../build/zig-out --cache-dir ../build/zig-cache)
────────────────────────────────────────
EOF
}

main "$@"
