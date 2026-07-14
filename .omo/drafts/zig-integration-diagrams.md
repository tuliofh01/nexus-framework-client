---
slug: zig-integration-diagrams
status: approved
intent: clear
pending-action: write .omo/plans/zig-integration-diagrams.md
approach: Minimal v0.2.0 Zig integration — Phase 0 bootstrap (pure Zig, no shell) + Phase 1 zig-services compiling real C++ TU + Phase 2 Langflow importer (parallel) + Phase 3 desktop Zig default + Phase 6 diagram updates. Phases 4-5 deferred to v0.3.0. Bare minimum: `zig run setup.zig` → `zig build` in generated app works.
---

# Draft: zig-integration-diagrams

## System Tree & Architecture (Zig Integration v0.2.0)

### Monorepo Structure (Current)
```
Framework/
├── app/                    # :app — Compose Desktop client (JDK 26)
│   └── src/main/kotlin/nexus/opensource/
│       ├── model/          # CounterModel, NexusBranding
│       ├── view/           # CounterView, GenerateProjectScreen, BlueprintEditor, FlowsEditor
│       └── controller/     # CounterController, GenerateController, BlueprintController, FlowsEditorController
├── misc/
│   ├── core/               # :core — ProjectGenerator, TemplateEngine, Validators, nxs_config.json schema v2
│   ├── cli/                # :cli — headless `generate` command
│   ├── build-logic/        # Included build (JVM toolchain 26, convention plugins)
│   ├── client-setup/       # First-run installers ← **Zig bootstrap added here (Phase 0)**
│   │   ├── linux/
│   │   ├── macos/
│   │   ├── windows/
│   │   └── zig/            # ← NEW: bootstrap.zig, build.zig, setup.zig
│   ├── scripts/
│   │   ├── dev/
│   │   ├── test-gen/
│   │   └── generate-diagrams/  # generate-styled-diagrams.py (7 SVGs → 10 after Phase 6)
│   └── docker/             # Containerized generation
├── template/               # Output templates copied to builds/framework/<name>/
│   ├── desktop-app/        # SDL3 + CMake (default today)
│   │   ├── src/            # C++ MVC (model/, controller/, view/, service/)
│   │   ├── ui/             # TS/XHTML DSL
│   │   ├── scripts/        # Lua panels
│   │   ├── python/         # pybind11 modules
│   │   ├── zig-services/   # ← NEW: Zig sidecar (Phase 1 scaffold exists)
│   │   │   ├── build.zig
│   │   │   ├── build.zig.zon
│   │   │   ├── src/        # main.zig (C ABI), memory.zig (ArenaAllocator)
│   │   │   ├── jni/        # python_bridge.zig, lua_bridge.zig (Phase 4 → v0.3.0)
│   │   │   ├── c_abi/      # zig_allocator.h for C++ opt-in
│   │   │   └── examples/   # hello_cpp.cpp smoke test
│   │   ├── CMakeLists.txt  # 7× FetchContent (SDL3, imgui, sol2, pybind11, etc.)
│   │   ├── blueprint.json
│   │   └── flows/flows.json
│   ├── android-app/        # AGP + CMake + Djinni + Chaquopy (unchanged in v0.2.0)
│   │   ├── app/
│   │   ├── djinni/         # plotter.djinni, app.djinni (stay for v0.2.0)
│   │   └── zig-services/   # ← Phase 4 (v0.3.0)
│   └── shared/             # DSL, themes, fonts, runtime helpers
├── builds/                 # Build artifacts (gitignored)
│   ├── client/             # :app distributions, OS packages
│   └── framework/          # Generated native projects (out-of-source)
│       └── <projectName>/
│           ├── zig-services/  # Copied by ProjectGenerator when nativeBackend=zig|dual
│           ├── CMakeLists.txt # or legacy-cmake-* (Phase 3)
│           └── nxs_config.json
├── docs/
│   ├── architecture/       # zig-patching.md, risk-analysis.md, runtime-stack.md
│   └── assets/diagrams/    # 7 SVGs → 10 after Phase 6
└── AGENTS.md               # Build commands for coding assistants
```

### Build System Hierarchy (After Zig Integration v0.2.0)

```
┌─────────────────────────────────────────────────────────────────────┐
│ GRADLE (JDK 26) — :app, :core, :cli ONLY                           │
│  ├── :app → Compose Desktop UI (Generate Project, Editors)         │
│  ├── :core → ProjectGenerator, TemplateEngine, Validators          │
│  │   └── ProjectGenerator.emit()                                   │
│  │       ├── Reads blueprint.json + flows.json                     │
│  │       ├── Validates via BlueprintValidator / FlowsValidator     │
│  │       ├── Copies template/desktop-app/ or template/android-app/ │
│  │       │   └── **Copies zig-services/ when nativeBackend=zig**   │
│  │       ├── Substitutes {{placeholders}} via TemplateEngine       │
│  │       └── Writes nxs_config.json (schema v2)                    │
│  │           └── "build": { "nativeBackend": "cmake"|"zig"|"dual" │
│  │               "useCmakeFallback": true }                        │
│  │       └── Outputs to builds/framework/<name>/                  │
│  └── :cli → Headless `generate --type desktop|android --name X`   │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│ GENERATED NATIVE PROJECT (builds/framework/<name>/)                │
│                                                                     │
│  DESKTOP APP (v0.2.0)                                              │
│  ┌─────────────────────┐                                           │
│  │ nxs_config.json     │                                           │
│  │ nativeBackend: zig  │                                           │
│  └──────────┬──────────┘                                           │
│             │                                                       │
│  ┌──────────▼──────────┐                                           │
│  │ ZIG SERVICES (NEW)  │                                           │
│  │ zig build           │                                           │
│  │  ├── nexus_zig lib  │                                           │
│  │  │   ├── nxs_alloc  │                                           │
│  │  │   ├── nxs_free   │                                           │
│  │  │   └── nxs_reset  │                                           │
│  │  ├── hello_cpp      │                                           │
│  │  └── (real C++ TUs) │  ← Phase 1: AppModel.cpp, NexusTheme.cpp  │
│  └──────────┬──────────┘                                           │
│             │                                                       │
│  ┌──────────▼──────────┐                                           │
│  │ CMAKE FALLBACK      │                                           │
│  │ (legacy-cmake-*)    │                                           │
│  │ cmake --preset      │                                           │
│  └─────────────────────┘                                           │
└─────────────────────────────────────────────────────────────────────┘
```

### Zig Orchestration Layer Responsibilities (v0.2.0)

| Layer | Responsibility | Phase |
|-------|----------------|-------|
| **Client-setup** | `zig run setup.zig` installs Zig 0.14.0 + JDK 26, writes `env.sh`/`env.bat` | 0 |
| **ProjectGenerator** | Copy `zig-services/` tree when `nativeBackend=zig\|dual` | 1 |
| **zig-services/build.zig** | Compile C++ TUs via `zig c++`, link `nexus_zig`, pack `lua.dat`/`python.dat`, stage assets | 1→3 |
| **build.zig.zon** | Pin deps (SDL3 3.2.4, imgui 1.91.8, etc.) matching CMake `GIT_TAG`s | 1→3 |
| **nxs_config.json** | `build.nativeBackend` switch; `allocator.zigArena` opt-in (Phase 5 → v0.3.0) | 1 |
| **CMake fallback** | `legacy-cmake-debug`/`release` presets; `NXS_USE_ZIG_SIDECAR=ON` prints hint only | 1→3 |

### Dual-Backend Transition (v0.2.0 Scope)

| Phase | Desktop Default | Android Default | CMake Status |
|-------|-----------------|-----------------|--------------|
| 0-1   | `cmake`         | `cmake` + Djinni | Primary |
| 2     | `cmake`         | `cmake` + Djinni | Primary |
| **3 (v0.2.0)** | **`zig`**       | `cmake` + Djinni | `legacy-cmake-*` fallback |
| 4 (v0.3.0) | `zig`           | **`zig`** (JNI)  | Fallback only |
| 5+ (v0.3.0) | `zig` + ArenaAllocator opt-in | `zig` | Archive (docs only) |

---

## Components (topology ledger)
<!-- id | outcome (one line) | status: active|deferred | evidence path -->
zig-bootstrap | Zig 0.14.0 installed via `zig run setup.zig` (pure Zig, no shell) | active | docs/architecture/zig-patching.md:67-124
zig-services-scaffold | zig-services/ compiles real desktop C++ TUs via zig c++ + build.zig.zon | active | template/desktop-app/zig-services/: build.zig:1-52, src/main.zig:1-36
langflow-importer | LangflowTransformationEngine in :core imports Langflow JSON → FlowsFile (enabled=false) | active | docs/architecture/zig-patching.md:187-330
desktop-zig-default | zig-services/ becomes default native backend; CMake renamed to legacy-cmake-* | active | docs/architecture/zig-patching.md:334-367
android-zig-jni | Zig JNI bridges replace Djinni for Android .so | deferred (v0.3.0) | docs/architecture/zig-patching.md:370-420
arena-allocator | Opt-in Zig ArenaAllocator C-ABI for C++ hotspots | deferred (v0.3.0) | docs/architecture/zig-patching.md:423-468
diagram-updates | 3 new SVGs + 3 updated SVGs via generate-styled-diagrams.py | active | misc/scripts/generate-diagrams/generate-styled-diagrams.py:1-1165

## Open assumptions (announced defaults)
<!-- assumption | adopted default | rationale | reversible? -->
Zig version pin | Zig 0.14.0 exact (in bootstrap.zig) | Phase 1-3 require stable C++ ABI; 0.14.x has Android libc issues post-0.14.0 | Reversible in v0.3.0
Gradle scope | Gradle stays for :app/:core/:cli only | User wants minimal JVM; generated apps use Zig | Not reversible (core design decision)
CMake fallback | CMake remains until Phase 3 complete | Risk mitigation per zig-patching.md phased approach | Reversible after Phase 3
Langflow import scope | Phase 2: automation nodes only (LLM/Tool/Agent → invoke steps); blueprint import v1.1 | Matches zig-patching.md mapping matrix | Expandable in v1.1
Diagram style | Use existing generate-styled-diagrams.py style (JetBrainsMono, layered boxes) | Consistency with existing docs/assets/diagrams/*.svg | Reversible

## Findings (cited - path:lines)
- Zig scaffold exists at template/desktop-app/zig-services/ with build.zig, memory.zig (arena allocator), main.zig (C ABI exports), JNI stubs for python_bridge/lua_bridge
- Client-setup needs Zig bootstrap (misc/client-setup/zig/) - not yet created per zig-patching.md Phase 0
- generate-styled-diagrams.py produces 7 existing SVGs; needs 3 new (zig-orchestration-layer, cmake-to-zig-migration, langflow-import-pipeline) + 3 updates
- Risk analysis (zig-surgical-integration-risk-analysis.html) scores 68 with 3 Critical: FetchContent SPOF, CMake→Zig reversal risk, Djinni→Zig JNI gap
- JDK 26 required for :app/:core/:cli (misc/build-logic/kotlin-jvm.gradle.kts:14) - this remains
- Zig does NOT ship Bionic libc - Android needs NDK sysroot (zig-patching.md:114-116)

## Decisions (with rationale)
1. **Phase 0 first** - Must install Zig 0.14.0 before any Zig work; client-setup is the right place
2. **Parallel Phase 1 + Phase 2** - zig-services scaffold and Langflow importer are independent (Kotlin vs Zig)
3. **Diagram updates in Phase 6 but prep in Phase 2** - Schema freeze after Langflow importer allows final diagram content
4. **Keep CMake as default until Phase 3** - Per zig-patching.md phased migration strategy
5. **No global allocator replacement** - ArenaAllocator opt-in only at measured hotspots (FunctionRegistry, PlotController)
6. **v0.2.0 = bare minimum functional** - Phases 0-3 + 6 only; Phases 4-5 deferred to v0.3.0
7. **No ASCII diagrams — all rendered SVGs** - Every diagram must be a beautiful, rendered SVG via `generate-styled-diagrams.py`. No ASCII art in docs, README, or plan artifacts. Diagrams must be attractive, visually clear, and easy to understand for readers. Use layered boxes, consistent color palette, professional arrows, JetBrainsMono font, legends, and proper spacing.

## Scope IN (v0.2.0 — Bare Minimum Functional)
- Phase 0: Zig 0.14.0 bootstrap via `misc/client-setup/zig/bootstrap.zig` (pure Zig, no shell scripts)
  - `misc/client-setup/zig/bootstrap.zig` — downloads/installs Zig 0.14.0, writes env.sh/env.bat
  - `misc/client-setup/zig/build.zig` — builds bootstrap tool as standalone `zig-bootstrap`
  - `misc/client-setup/setup.zig` — entry point: `zig run misc/client-setup/setup.zig`
  - **No .sh/.bat files** — cross-platform Zig code only
  - `AGENTS.md` First-run: `zig run misc/client-setup/setup.zig` → `source misc/client-setup/env.sh` → `./gradlew :app:run`
- Phase 0: env.sh/env.bat exports ZIG_VERSION=0.14.0, ZIG_HOME, ANDROID_NDK (optional)
- Phase 1: zig-services/ builds real desktop C++ TUs via zig c++ (link ../src/AppModel.cpp, ../src/view/NexusTheme.cpp, ../shared/runtime/)
- Phase 1: build.zig.zon with pinned deps matching CMake GIT_TAGs (SDL3 release-3.2.4, imgui 1.91.8)
- Phase 1: ProjectGenerator copies zig-services/ when nativeBackend=zig|dual
- Phase 2: LangflowTransformationEngine.kt + fixtures + Flows Editor import button
- Phase 3: nxs_config.json default nativeBackend=zig for desktop; CMake renamed to legacy-cmake-*
- Phase 6: 3 new SVG diagrams + 3 updated SVGs via generate-styled-diagrams.py
- All diagram SVGs reference Zig orchestration layer, show CMake fallback, Langflow import flow

## Scope OUT (Must NOT have in v0.2.0)
- Zig replacing Gradle for :app, :core, :cli modules
- Global C++ operator new replacement (ArenaAllocator is opt-in C-ABI only, deferred to v0.3.0)
- Langflow runtime embedded in generated apps (import is codegen only)
- One-shot CMake deletion (phased fallback per plan)
- Android Zig JNI (Phase 4 → v0.3.0)
- Zig 0.15+ or unversioned Zig (pin 0.14.0)
- Android builds without NDK (Zig does not ship Bionic libc)

## Open questions
1. Langflow multi-flow policy: one export → one FlowDefinition (MVP) vs multiple subgraphs
2. Zig dep vendor strategy: pure build.zig.zon git URLs (simpler) vs git subtrees in zig-services/vendor/

## Approval gate
status: approved
intent: clear
pending-action: write .omo/plans/zig-integration-diagrams.md
approach: Continue Zig integration from Phase 0 (installs) through Phase 3 (desktop Zig default), with parallel Langflow importer (Phase 2) and diagram updates (Phase 6). Minimize Java/JVM surface by using Zig's native cross-compilation for generated app builds, keeping Gradle only for :app/:core/:cli. Update all architecture diagrams to reflect Zig orchestration layer.
---

# Draft: zig-integration-diagrams

## System Tree & Architecture (Zig Integration)

### Monorepo Structure (Current)
```
Framework/
├── app/                    # :app — Compose Desktop client (JDK 26)
│   └── src/main/kotlin/nexus/opensource/
│       ├── model/          # CounterModel, NexusBranding
│       ├── view/           # CounterView, GenerateProjectScreen, BlueprintEditor, FlowsEditor
│       └── controller/     # CounterController, GenerateController, BlueprintController, FlowsEditorController
├── misc/
│   ├── core/               # :core — ProjectGenerator, TemplateEngine, Validators, nxs_config.json schema v2
│   ├── cli/                # :cli — headless `generate` command
│   ├── build-logic/        # Included build (JVM toolchain 26, convention plugins)
│   ├── client-setup/       # First-run installers (JDK 26 + Git) ← **Zig 0.14.x added here (Phase 0)**
│   │   ├── linux/
│   │   ├── macos/
│   │   ├── windows/
│   │   └── zig/            # ← NEW: install-zig.sh for all platforms
│   ├── scripts/
│   │   ├── dev/
│   │   ├── test-gen/
│   │   └── generate-diagrams/  # generate-styled-diagrams.py (7 SVGs → 10 after Phase 6)
│   └── docker/             # Containerized generation
├── template/               # Output templates copied to builds/framework/<name>/
│   ├── desktop-app/        # SDL3 + CMake (default today)
│   │   ├── src/            # C++ MVC (model/, controller/, view/, service/)
│   │   ├── ui/             # TS/XHTML DSL
│   │   ├── scripts/        # Lua panels
│   │   ├── python/         # pybind11 modules
│   │   ├── zig-services/   # ← NEW: Zig sidecar (Phase 1 scaffold exists)
│   │   │   ├── build.zig
│   │   │   ├── build.zig.zon
│   │   │   ├── src/        # main.zig (C ABI), memory.zig (ArenaAllocator)
│   │   │   ├── jni/        # python_bridge.zig, lua_bridge.zig (Phase 4)
│   │   │   ├── c_abi/      # zig_allocator.h for C++ opt-in
│   │   │   └── examples/   # hello_cpp.cpp smoke test
│   │   ├── CMakeLists.txt  # 7× FetchContent (SDL3, imgui, sol2, pybind11, etc.)
│   │   ├── blueprint.json
│   │   └── flows/flows.json
│   ├── android-app/        # AGP + CMake + Djinni + Chaquopy
│   │   ├── app/
│   │   ├── djinni/         # plotter.djinni, app.djinni (deprecated Phase 4)
│   │   └── zig-services/   # ← NEW: Android Zig JNI (Phase 4)
│   └── shared/             # DSL, themes, fonts, runtime helpers
├── builds/                 # Build artifacts (gitignored)
│   ├── client/             # :app distributions, OS packages
│   └── framework/          # Generated native projects (out-of-source)
│       └── <projectName>/
│           ├── zig-services/  # Copied by ProjectGenerator when nativeBackend=zig|dual
│           ├── CMakeLists.txt # or legacy-cmake-* (Phase 3)
│           └── nxs_config.json
├── docs/
│   ├── architecture/       # zig-patching.md, risk-analysis.md, runtime-stack.md
│   └── assets/diagrams/    # 7 SVGs → 10 after Phase 6
└── AGENTS.md               # Build commands for coding assistants
```

### Build System Hierarchy (After Zig Integration)

```
┌─────────────────────────────────────────────────────────────────────┐
│ GRADLE (JDK 26) — :app, :core, :cli ONLY                           │
│  ├── :app → Compose Desktop UI (Generate Project, Editors)         │
│  ├── :core → ProjectGenerator, TemplateEngine, Validators          │
│  │   └── ProjectGenerator.emit()                                   │
│  │       ├── Reads blueprint.json + flows.json                     │
│   │       ├── Validates via BlueprintValidator / FlowsValidator    │
│   │       ├── Copies template/desktop-app/ or template/android-app/│
│   │       │   └── **Copies zig-services/ when nativeBackend=zig**  │
│   │       ├── Substitutes {{placeholders}} via TemplateEngine      │
│   │       └── Writes nxs_config.json (schema v2)                   │
│   │           └── "build": { "nativeBackend": "cmake"|"zig"|"dual"│
│   │               "useCmakeFallback": true }                       │
│   │       └── Outputs to builds/framework/<name>/                  │
│   └── :cli → Headless `generate --type desktop|android --name X`   │
└─────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│ GENERATED NATIVE PROJECT (builds/framework/<name>/)                │
│                                                                     │
│  DESKTOP APP                          ANDROID APP                   │
│  ┌─────────────────────┐              ┌─────────────────────┐      │
│  │ nxs_config.json     │              │ nxs_config.json     │      │
│  │ nativeBackend: zig  │              │ nativeBackend: zig  │      │
│  └──────────┬──────────┘              └──────────┬──────────┘      │
│             │                                    │                 │
│  ┌──────────▼──────────┐              ┌──────────▼──────────┐      │
│  │ ZIG SERVICES (NEW)  │              │ ZIG SERVICES (P4)   │      │
│  │ zig build           │              │ zig build           │      │
│  │  ├── nexus_zig lib  │              │  ├── python_bridge  │      │
│  │  │   ├── nxs_alloc  │              │  │   (JNI export)    │      │
│  │  │   ├── nxs_free   │              │  ├── lua_bridge     │      │
│  │  │   └── nxs_reset  │              │  │   (JNI export)    │      │
│  │  │   └── ArenaAlloc │              │  └── nexus_zig lib  │      │
│  │  ├── hello_cpp      │              └─────────────────────┘      │
│  │  └── (real C++ TUs) │                    │                       │
│  └──────────┬──────────┘                    ▼                       │
│             │                    ┌─────────────────────┐            │
│  ┌──────────▼──────────┐         │ AGP + Chaquopy      │            │
│  │ CMAKE FALLBACK      │         │ externalNativeBuild │            │
│  │ (legacy-cmake-*)    │         │  → zig .so → jniLibs│            │
│  │ cmake --preset      │         └─────────────────────┘            │
│  └─────────────────────┘                                          │
└─────────────────────────────────────────────────────────────────────┘
```

### Zig Orchestration Layer Responsibilities

| Layer | Responsibility | Phase |
|-------|----------------|-------|
| **Client-setup** | Install Zig 0.14.x, export `ZIG_HOME`, `PATH` | 0 |
| **ProjectGenerator** | Copy `zig-services/` tree when `nativeBackend=zig\|dual` | 1 |
| **zig-services/build.zig** | Compile C++ TUs via `zig c++`, link `nexus_zig`, pack `lua.dat`/`python.dat`, stage assets | 1→3 |
| **build.zig.zon** | Pin deps (SDL3 3.2.4, imgui 1.91.8, etc.) matching CMake `GIT_TAG`s | 1→3 |
| **nxs_config.json** | `build.nativeBackend` switch; `allocator.zigArena` opt-in | 1, 5 |
| **CMake fallback** | `legacy-cmake-debug`/`release` presets; `NXS_USE_ZIG_SIDECAR=ON` prints hint only | 1→3 |

### Dual-Backend Transition (Phased)

| Phase | Desktop Default | Android Default | CMake Status |
|-------|-----------------|-----------------|--------------|
| 0-1   | `cmake`         | `cmake` + Djinni | Primary |
| 2     | `cmake`         | `cmake` + Djinni | Primary |
| 3     | **`zig`**       | `cmake` + Djinni | `legacy-cmake-*` fallback |
| 4     | `zig`           | **`zig`** (JNI)  | Fallback only |
| 5+    | `zig` + ArenaAllocator opt-in | `zig` | Archive (docs only) |

---

## Components (topology ledger)
<!-- id | outcome (one line) | status: active|deferred | evidence path -->
zig-install | Zig 0.14.x installed via client-setup on all platforms | active | docs/architecture/zig-patching.md:67-124
zig-services-scaffold | zig-services/ sidecar builds nexus_zig lib + hello_cpp via zig c++ | active | template/desktop-app/zig-services/: build.zig:1-52, src/main.zig:1-36
langflow-importer | LangflowTransformationEngine in :core imports Langflow JSON → FlowsFile (enabled=false) | active | docs/architecture/zig-patching.md:187-330
desktop-zig-default | zig-services/ becomes default native backend; CMake fallback opt-in | deferred | docs/architecture/zig-patching.md:334-367
android-zig-jni | Zig JNI bridges replace Djinni for Android .so | deferred | docs/architecture/zig-patching.md:370-420
arena-allocator | Opt-in Zig ArenaAllocator C-ABI for C++ hotspots | deferred | docs/architecture/zig-patching.md:423-468
diagram-updates | 3 new SVGs + 3 updated SVGs generated via generate-styled-diagrams.py | active | misc/scripts/generate-diagrams/generate-styled-diagrams.py:1-1165

## Open assumptions (announced defaults)
<!-- assumption | adopted default | rationale | reversible? -->
Zig version pin | Zig 0.14.x (exact patch in env.sh) | Phase 1-4 require stable C++ ABI; 0.14.x has Android libc issues post-0.14.0 | Reversible in Phase 5
Gradle scope | Gradle stays for :app/:core/:cli only | User wants minimal JVM; generated apps use Zig | Not reversible (core design decision)
CMake fallback | CMake remains until Phase 3 complete | Risk mitigation per zig-patching.md phased approach | Reversible after Phase 3
Langflow import scope | Phase 2: automation nodes only (LLM/Tool/Agent → invoke steps); blueprint import v1.1 | Matches zig-patching.md mapping matrix | Expandable in v1.1
Diagram style | Use existing generate-styled-diagrams.py style (JetBrainsMono, layered boxes) | Consistency with existing docs/assets/diagrams/*.svg | Reversible

## Findings (cited - path:lines)
- Zig scaffold exists at template/desktop-app/zig-services/ with build.zig, memory.zig (arena allocator), main.zig (C ABI exports), JNI stubs for python_bridge/lua_bridge
- Client-setup needs Zig install scripts (misc/client-setup/zig/) - not yet created per zig-patching.md Phase 0
- generate-styled-diagrams.py produces 7 existing SVGs; needs 3 new (zig-orchestration-layer, cmake-to-zig-migration, langflow-import-pipeline) + 3 updates
- Risk analysis (zig-surgical-integration-risk-analysis.html) scores 68 with 3 Critical: FetchContent SPOF, CMake→Zig reversal risk, Djinni→Zig JNI gap
- JDK 26 required for :app/:core/:cli (misc/build-logic/kotlin-jvm.gradle.kts:14) - this remains
- Zig does NOT ship Bionic libc - Android needs NDK sysroot (zig-patching.md:114-116)

## Decisions (with rationale)
1. **Phase 0 first** - Must install Zig 0.14.x before any Zig work; client-setup is the right place
2. **Parallel Phase 1 + Phase 2** - zig-services scaffold and Langflow importer are independent (Kotlin vs Zig)
3. **Diagram updates in Phase 6 but prep in Phase 2** - Schema freeze after Langflow importer allows final diagram content
4. **Keep CMake as default until Phase 3** - Per zig-patching.md phased migration strategy
5. **No global allocator replacement** - ArenaAllocator opt-in only at measured hotspots (FunctionRegistry, PlotController)

## Scope IN
- Phase 0: Zig 0.14.x bootstrap via **Zig-built setup tool** (no shell scripts)
  - **`misc/client-setup/zig/bootstrap.zig`** — single Zig source file that:
    - Downloads/verifies Zig 0.14.0 tarball for current platform (Linux/macOS/Windows)
    - Installs to `~/.local/zig-0.14.0/` (Linux/macOS) or `%LOCALAPPDATA%\zig-0.14.0\` (Windows)
    - Writes `env.sh` / `env.bat` with `ZIG_VERSION=0.14.0`, `ZIG_HOME`, `PATH` exports
    - Optionally detects/installs JDK 26 via `zig fetch` + Foojay API (replaces distro-specific shell scripts)
    - Self-contained: `zig run bootstrap.zig` works on any platform with Zig 0.11+ (bootstraps itself)
  - **`misc/client-setup/zig/build.zig`** — builds the bootstrap tool as standalone executable (`zig-bootstrap`)
  - **`misc/client-setup/setup.zig`** — entry point users run: `zig run misc/client-setup/setup.zig` (delegates to bootstrap)
  - **No `.sh`/`.bat` files** — cross-platform Zig code only
  - `AGENTS.md` First-run: `zig run misc/client-setup/setup.zig` → `source misc/client-setup/env.sh` → `./gradlew :app:run`
- Phase 0: env.sh/env.bat exports ZIG_VERSION, ZIG_HOME, ANDROID_NDK (optional)
- Phase 1: zig-services/ builds real desktop C++ TUs via zig c++ (link ../src/ or ../shared/runtime/)
- Phase 1: ProjectGenerator copies zig-services/ when nativeBackend=zig|dual
- Phase 2: LangflowTransformationEngine.kt + fixtures + Flows Editor import button
- Phase 3: nxs_config.json default nativeBackend=zig for desktop; CMake renamed to legacy-cmake-*
- Phase 6: 3 new SVG diagrams + 3 updated SVGs via generate-styled-diagrams.py
- All diagram SVGs reference Zig orchestration layer, show CMake fallback, Langflow import flow

## Scope OUT (Must NOT have)
- Zig replacing Gradle for :app, :core, :cli modules
- Global C++ operator new replacement (ArenaAllocator is opt-in C-ABI only)
- Langflow runtime embedded in generated apps (import is codegen only)
- One-shot CMake deletion (phased fallback per plan)
- Android Zig JNI in Phase 1-3 (Phase 4 only)
- Zig 0.15+ or unversioned Zig (pin 0.14.x)

## Open questions
1. **Exact Zig 0.14.x patch version** → **HYBRID**: Pin `ZIG_VERSION=0.14.0` in env.sh at kickoff; document update policy in misc/client-setup/zig/README.md (test 0.14.1+ in CI before bumping env.sh). Allows reproducible installs today + controlled upgrades.
2. **Langflow multi-flow policy** → **MVP default**: One Langflow export → one `FlowDefinition` with multiple `steps[]` (per zig-patching.md mapping matrix). Document in flows-schema.md; v1.1 can split subgraphs.
3. **Vendor strategy for Zig deps** → **HYBRID**: 
   - **Core deps (SDL3, imgui, sol2, pybind11)**: Git subtrees in `zig-services/vendor/<dep>/` — pinned to exact CMake `GIT_TAG` commit hashes; `build.zig.zon` references local `vendor/` paths for offline/air-gapped builds.
   - **Transitive/utility deps**: Pure `build.zig.zon` git URLs — lighter maintenance.
   - Rationale: Core deps must match CMake exactly for parity; vendor gives audit trail + offline builds. Phase 1 implements subtree vendor for SDL3/imgui; Phase 3 adds rest.

## Approval gate
status: approved
intent: clear
pending-action: execute .omo/plans/zig-integration-diagrams.md
approach: v0.2.0 = Phases 0-3 + 6. Phase 0: pure Zig bootstrap (no shell). Phase 1: zig-services real C++ TUs. Phase 2: Langflow importer (parallel). Phase 3: Desktop Zig default. Phase 6: Beautiful rendered SVGs only (zero ASCII). Phases 4-5 deferred to v0.3.0.
