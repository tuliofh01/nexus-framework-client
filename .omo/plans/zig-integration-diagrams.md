# zig-integration-diagrams - Work Plan

## TL;DR (For humans)
<!-- Fill this LAST, after the detailed plan below is written, so it summarizes the REAL plan. -->
<!-- Plain English for a non-engineer: NO file paths, NO todo numbers, NO wave/agent/tool names. -->

**What you'll get:** A phased migration that replaces CMake with Zig as the native build orchestrator for generated desktop/Android apps, while keeping Gradle for the Compose client. Includes a Langflow JSON importer that safely disables imported flows by default. All architecture diagrams updated to show the new Zig orchestration layer.

**Why this approach:** Zig's single-binary cross-compilation (~80MB vs ~10GB toolchain) eliminates FetchContent network dependency (the #1 CI blocker) and enables Linux→Windows builds without MSVC. Phased CMake fallback prevents build breakage. Langflow import is parallel Kotlin work that doesn't block Zig.

**What it will NOT do:** Replace Gradle for the Compose client (:app/:core/:cli). Replace global C++ allocator (ArenaAllocator is opt-in C-ABI only). Embed Langflow runtime in shipped apps. Delete CMake in one shot. Use Zig 0.15+ or Android without NDK.

**Effort:** XL
**Risk:** Medium - Zig 0.14.x Android Bionic libc issues (Z1), dual backend drift (Z3), but phased with CMake fallback mitigates
**Decisions to sanity-check:** Exact Zig 0.14.x patch pin; Langflow multi-flow policy (one export → one flow MVP); Zig dep vendor strategy (subtrees vs build.zig.zon git URLs)

Your next move: **approve** to begin Phase 0 (Zig install scripts), or request high-accuracy review first.

---

> TL;DR (machine): XL effort, Medium risk — 7-phase Zig native build migration (Phases 0-3 desktop, 4 Android, 5 allocator, 6 docs) + parallel Langflow importer + 6 diagram updates

## Scope
### Must have
- Zig 0.14.x installed via client-setup on Linux/macOS/Windows with env exports
- zig-services/ sidecar compiles real desktop C++ TUs via zig c++ with build.zig.zon deps
- LangflowTransformationEngine imports Langflow JSON → FlowsFile (enabled: false default)
- Desktop Zig becomes default nativeBackend; CMake renamed to legacy-cmake-* fallback
- Android Zig JNI bridges replace Djinni for plotter.djinni (evaluate, install_python_bridge)
- Opt-in ArenaAllocator C-ABI for measured C++ hotspots (FunctionRegistry, PlotController)
- 3 new SVGs + 3 updated SVGs generated; all docs/translations synced

### Must NOT have (guardrails, anti-slop, scope boundaries)
- Zig replacing Gradle for :app, :core, :cli modules
- Global C++ operator new/delete replacement (ArenaAllocator is narrow C-ABI opt-in only)
- Langflow runtime bundled in generated apps (import is codegen/adoption only)
- One-shot CMake deletion (phased A→D migration with fallback until Phase 4)
- Zig 0.15+ or unpinned Zig version (pin 0.14.x in env.sh)
- Android builds without NDK (Zig does not ship Bionic libc)

## Verification strategy
> Zero human intervention - all verification is agent-executed.
- Test decision: tests-after + framework (JUnit for :core, Zig test for zig-services, shell smoke for generated apps)
- Evidence: .omo/evidence/task-<N>-zig-integration-diagrams.<ext>

## Execution strategy
### Parallel execution waves
> Target 5-8 todos per wave. Fewer than 3 (except the final) means you under-split.

### Dependency matrix
| Todo | Depends on | Blocks | Can parallelize with |
| --- | --- | --- | --- |
| 1. Phase 0: Zig install | none | 2, 3 | — |
| 2. Phase 1: zig-services C++ TUs | 1 | 4, 5 | 3 |
| 3. Phase 2: Langflow importer | none | 5, 6 | 2 |
| 4. Phase 3: Desktop Zig default | 2 | 6, 7 | — |
| 5. Phase 4: Android Zig JNI | 4 | 7 | — |
| 6. Phase 5: ArenaAllocator | 4 | 7 | — |
| 7. Phase 6: Diagrams + docs | 3, 4, 5 | none | — |

## Todos
> Implementation + Test = ONE todo. Never separate.
<!-- APPEND TASK BATCHES BELOW THIS LINE WITH edit/apply_patch - never rewrite the headers above. -->
- [x] 1. Phase 0: Zig 0.14.x bootstrap via Zig-built setup tool (no shell scripts)
  What to do / Must NOT do: Create misc/client-setup/zig/bootstrap.zig — single Zig source file that:
    - Downloads/verifies Zig 0.14.0 tarball for current platform (Linux/macOS/Windows) from ziglang.org/builds
    - Installs to ~/.local/zig-0.14.0/ (Linux/macOS) or %LOCALAPPDATA%\zig-0.14.0\ (Windows)
    - Writes env.sh / env.bat with ZIG_VERSION=0.14.0, ZIG_HOME, PATH exports
    - Optionally detects/installs JDK 26 via zig fetch + Foojay API (replaces distro-specific shell scripts)
    - Self-contained: `zig run bootstrap.zig` works on any platform with Zig 0.11+ (bootstraps itself)
  Create misc/client-setup/zig/build.zig — builds bootstrap tool as standalone executable (zig-bootstrap).
  Create misc/client-setup/setup.zig — entry point users run: `zig run misc/client-setup/setup.zig` (delegates to bootstrap).
  **No .sh/.bat files** — cross-platform Zig code only.
  Update AGENTS.md First-run: `zig run misc/client-setup/setup.zig` → `source misc/client-setup/env.sh` → `./gradlew :app:run`.
  MUST NOT change Gradle JDK 26 toolchain.
  Parallelization: Wave 1 | Blocked by: none | Blocks: 2, 3
  References: zig-patching.md:92-124, zig-patching.md:67-88, AGENTS.md:13-16, misc/client-setup/README.md:1-30
  Acceptance criteria (agent-executable): Fresh machine: `zig run misc/client-setup/setup.zig` → `source misc/client-setup/env.sh && zig version` prints 0.14.0; env.sh documents ANDROID_NDK optional export; AGENTS.md First run mentions Zig bootstrap
  QA scenarios (name the exact tool + invocation): happy: `zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh && zig version`; failure: no network, wrong Zig version already installed, Windows PATH limits
  Commit: Y | feat(client-setup): add Zig 0.14.x bootstrap via Zig (no shell scripts)

- [x] 2. Phase 1: zig-services scaffold - link real desktop C++ TUs via zig c++
  What to do / Must NOT do: Extend template/desktop-app/zig-services/build.zig to compile at least one real C++ TU from ../src/ (e.g., NexusTheme.cpp or AppModel.cpp) and ../shared/runtime/. Add build.zig.zon with pinned deps matching CMake GIT_TAGs (SDL3 release-3.2.4, imgui 1.91.8). Create zig-services/scripts/fetch-deps.sh mirroring FetchContent. Ensure `zig build` succeeds on Linux x86_64 and `zig build -Dtarget=x86_64-windows` cross-compiles (documented). Add nxs_config.json build.nativeBackend: "cmake" | "zig" and useCmakeFallback flag. ProjectGenerator copies zig-services/ when nativeBackend is zig or dual. CMake coexistence: keep cmake --preset debug as default; add NXS_USE_ZIG_SIDECAR=ON option that prints message only. MUST NOT invoke Zig from CMake in Phase 1.
  Parallelization: Wave 2 | Blocked by: 1 | Blocks: 4, 5
  References: zig-patching.md:127-184, template/desktop-app/zig-services/build.zig:1-52, template/desktop-app/zig-services/README.md:1-69, template/desktop-app/CMakeLists.txt:22-86
  Acceptance criteria (agent-executable): `cd template/desktop-app/zig-services && zig build` succeeds; at least one desktop C++ TU links; `zig build -Dtarget=x86_64-windows` documented; CMake path still builds unchanged app; misc/scripts/test-gen/ smoke note added
  QA scenarios (name the exact tool + invocation): happy: `zig build && ./zig-out/bin/hello_cpp`; failure: missing SDL3 dep, wrong C++ standard, cross-compile target unsupported
  Commit: Y | feat(zig-services): compile real C++ TUs via zig c++ + build.zig.zon deps

- [x] 3. Phase 2: LangflowTransformationEngine (parallel with Phase 1)
  What to do / Must NOT do: Add LangflowTransformationEngine.kt in misc/core/src/main/kotlin/nexus/opensource/core/service/ with LangflowExport DTOs in model/langflow/. Map ReactFlow nodes/edges → FlowsFile with ALL imported flows enabled: false. Implement trigger inference (ChatInput→manual, Webhook→event, Schedule→interval). Wire into FlowsEditorView.kt "Import Langflow JSON" button and FlowsEditorController.importLangflow(). Unit tests with 3+ fixtures (minimal-chatflow.json, agent-with-tools.json, schedule-trigger.json). Update docs/templates/flows-schema.md importer behavior + enabled: false default. README § Importing Langflow updates. MUST NOT bundle Langflow runtime; import is codegen only.
  Parallelization: Wave 2 | Blocked by: none | Blocks: 5, 6
  References: zig-patching.md:187-331, misc/core/src/main/kotlin/nexus/opensource/core/service/FlowsValidator.kt, app/src/main/kotlin/nexus/opensource/view/FlowsEditorView.kt
  Acceptance criteria (agent-executable): LangflowTransformationEngineTest green with 3+ fixtures; imported flows always have enabled: false; FlowsValidator passes on all fixture outputs; Flows Editor import button loads file and updates JSON preview; generation writes flows/flows.json with disabled flags
  QA scenarios (name the exact tool + invocation): happy: import valid Langflow JSON → flows disabled; failure: cycle in edges → error/warning + partial import policy; invalid JSON → graceful error
  Commit: Y | feat(core): LangflowTransformationEngine + Flows Editor import

- [x] 4. Phase 3: Desktop Zig as primary native backend (default)
  What to do / Must NOT do: Change nxs_config.json default to "nativeBackend": "zig" for desktop template. Complete build.zig graph replicating CMakeLists.txt targets: imgui_bundle equivalent, pack_archive for lua.dat/python.dat, post-build asset staging (scripts/, ui/, flows/, blueprint.json). Commit build.zig.zon.lock. Rename CMake presets to legacy-cmake-debug/release with README escape hatch. ProjectGenerator emits zig-services/ always for desktop; honors useCmakeFallback. Add Zig-first C++ deps section to docs/guides/adding-dependencies.md. Parity build: Zig binary runs hello + ImGui window (same as CMake debug). CI: zig build on template/desktop-app + generated smoke in builds/framework/. MUST NOT delete CMakeLists.txt.
  Parallelization: Wave 3 | Blocked by: 2 | Blocks: 6, 7
  References: zig-patching.md:334-367, template/desktop-app/CMakeLists.txt:1-200, template/desktop-app/zig-services/build.zig:1-52
  Acceptance criteria (agent-executable): `zig build` produces runnable desktop binary equivalent to CMake debug; lua.dat/python.dat packed and staged; -Duse_cmake=true or nativeBackend: cmake still works; misc/scripts/test-gen/desktop-smoke.sh passes against Zig-built artifact
  QA scenarios (name the exact tool + invocation): happy: generate desktop app → zig build → runs; failure: missing dep in build.zig.zon, lua.dat not staged, CMake fallback broken
  Commit: Y | feat(desktop): Zig primary backend + CMake fallback

- [ ] 5. Phase 4: Android Zig JNI bridges + retire Djinni path
  What to do / Must NOT do: Create template/android-app/zig-services/ with build.zig, build.zig.zon, jni/python_bridge.zig, jni/lua_bridge.zig exporting Java_com_nexus_* JNI functions. Replace Djinni-generated JNI for plotter.djinni (evaluate, install_python_bridge). app/build.gradle.kts adds zigBuildRelease Exec task copying .so to jniLibs. Keep Chaquopy + Python.start boot order documented. Archive regen-djinni.sh to docs/guides/legacy-djinni.md. Targets: aarch64-linux-android, arm-linux-androideabi (optional x86_64-android). Prereq: Phase 3 stable, ANDROID_NDK installed, API ≥ 29. Zig NDK cross-compile research: zig-android-sdk or libc.conf for Bionic.
  Parallelization: Wave 4 | Blocked by: 4 | Blocks: 7
  References: zig-patching.md:370-420, template/android-app/djinni/plotter.djinni, template/android-app/app/build.gradle.kts:83
  Acceptance criteria (agent-executable): Android APK builds with Zig-produced .so on API 29+ emulator/device; PlotterCore.install_python_bridge + evaluate round-trip matches Djinni behavior; no Djinni codegen in default generate path; CMake Android preset documented as fallback only
  QA scenarios (name the exact tool + invocation): happy: ./gradlew :app:assembleDebug → APK with Zig .so; failure: NDK sysroot missing, Bionic libc mismatch, JNI signature mismatch
  Commit: Y | feat(android): Zig JNI bridges replace Djinni for plotter

- [x] 6. Phase 5: Opt-in ArenaAllocator C-ABI (AppModel hotspots)
  What to do / Must NOT do: Add zig-services/src/allocator.zig exporting nxs_alloc, nxs_free, nxs_reset_arena. Add template/shared/runtime/ZigAllocator.hpp with static alloc/free. Guard with nxs_config.json "allocator": { "zigArena": false } default off. Apply to hotspots: FunctionRegistry.cpp sample cache vectors, PlotController.cpp dirty-flag resample buffers, optionally AppModel. Document surgical rules (do/don't table). zigArena: false default → byte-identical behavior; zigArena: true → plotter smoke passes, no leak across nxs_reset_arena frames. ASan/valgrind note in docs.
  Parallelization: Wave 5 | Blocked by: 4 | Blocks: 7
  References: zig-patching.md:423-469, template/desktop-app/examples/plotter/src/model/FunctionRegistry.cpp, template/desktop-app/src/controller/PlotController.cpp
  Acceptance criteria (agent-executable): zigArena: false → byte-identical vs baseline; zigArena: true → plotter smoke test passes; no leak across nxs_reset_arena frames; ASan/valgrind note in docs
  QA scenarios (name the exact tool + invocation): happy: zigArena=true + plotter runs; failure: pointer escapes frame scope → UAF detected by ASan
  Commit: Y | feat(allocator): opt-in Zig ArenaAllocator C-ABI for hotspots

- [ ] 7. Phase 6: Docs, diagrams, translations, README updates — BEAUTIFUL RENDERED SVGs ONLY
  What to do / Must NOT do:
    **Zero ASCII diagrams** — every diagram MUST be a rendered SVG generated via `misc/scripts/generate-diagrams/generate-styled-diagrams.py`. No ASCII art in docs, README, plan artifacts, or any committed file.
    **Diagram quality bar**: attractive, visually clear, easy to understand for readers. Use the existing style system: JetBrainsMono font, layered colored boxes with subtle shadows, consistent color palette (blues/greens/oranges/purples), professional curved arrows with arrowheads, legend boxes explaining color coding, proper padding and spacing, high-contrast text on colored backgrounds.
    Generate 3 NEW SVGs with new generator functions in generate-styled-diagrams.py:
      - `zig-orchestration-layer.svg` — layered architecture diagram showing Zig build orchestration from client-setup through zig-services to output binary; gradient boxes, clear hierarchy arrows
      - `cmake-to-zig-migration.svg` — phased timeline/roadmap showing Phase 0→6 progression with CMake fading, Zig emerging; color-coded by phase with status indicators
      - `langflow-import-pipeline.svg` — flow diagram showing Langflow JSON → LangflowTransformationEngine → FlowsFile → nxs_config.json; pipeline boxes with data flowing arrows
    Update 3 EXISTING SVGs (regenerate with updated generator functions):
      - `full-stack-architecture.svg` — add zig-services box in template layer, update labels
      - `desktop-vs-android-runtime.svg` — Desktop: Zig+pybind11; Android: Zig JNI+Chaquopy (Phase 4)
      - `langflow-adoption-workflow.svg` — replace "Manual translate (v1)" with "Import button → LangflowTransformationEngine"
    Update: README.md (Zig vs CMake section, MVP checklist), AGENTS.md (Zig build commands, nativeBackend config), template/desktop-app/AGENTS.md (zig build primary), template/android-app/AGENTS.md (Zig JNI, Djinni deprecated), misc/client-setup/README.md (Zig + NDK rows), docs/guides/adding-dependencies.md (Zig build.zig.zon deps), docs/architecture/risk-analysis.md (stale findings + Z1-Z5 risks). Sync 6 translation files.
    MUST NOT use ASCII art anywhere. MUST NOT weaken UX for Lighthouse 100 compliance.
  Parallelization: Wave 6 | Blocked by: 3, 4, 5 | Blocks: none
  References: zig-patching.md:472-510, misc/scripts/generate-diagrams/generate-styled-diagrams.py:1-1165, docs/assets/diagrams/*.svg (8 existing)
  Acceptance criteria (agent-executable): All 3 new SVG diagrams generated and committed; 3 existing SVGs regenerated and updated; zero ASCII diagrams in any file; all SVGs linked from README; translation files mention Zig install + Langflow import; docs/README.md hub links to this plan; SVGs are visually attractive with layered boxes, professional arrows, legends, consistent color palette; Lighthouse 100 on any web preview
  QA scenarios (name the exact tool + invocation): happy: python3 generate-styled-diagrams.py → 10 valid SVGs rendered; visual: inspect each SVG for clarity, no text overflow, consistent styling; failure: diagram generation script error, missing color palette, text overflow, ASCII art found in committed files
  Commit: Y | docs: beautiful SVG diagrams + risk-analysis refresh + translations

## Final verification wave
> Runs in parallel after ALL todos. ALL must APPROVE. Surface results and wait for the user's explicit okay before declaring complete.
- [ ] F1. Plan compliance audit
- [ ] F2. Code quality review
- [ ] F3. Real manual QA
- [ ] F4. Scope fidelity

## Commit strategy
- Each phase = 1-2 commits (feats + docs) with conventional commit messages
- Phase 0: `feat(client-setup): add Zig 0.14.x install + env exports`
- Phase 1: `feat(zig-services): compile real C++ TUs via zig c++ + build.zig.zon deps`
- Phase 2: `feat(core): LangflowTransformationEngine + Flows Editor import`
- Phase 3: `feat(desktop): Zig primary backend + CMake fallback`
- Phase 4: `feat(android): Zig JNI bridges replace Djinni for plotter`
- Phase 5: `feat(allocator): opt-in Zig ArenaAllocator C-ABI for hotspots`
- Phase 6: `docs: Zig diagrams + risk-analysis refresh + translations`
- No WIP commits; squash fixups before final commit per phase

## Success criteria
- [ ] Fresh machine: platform setup.sh → `zig version` prints 0.14.x
- [ ] `cd template/desktop-app/zig-services && zig build` links real C++ TU
- [ ] Langflow import → flows.json with all `enabled: false`; FlowsValidator passes
- [ ] Generated desktop app: `zig build` binary runs identically to CMake debug build
- [ ] Android APK builds with Zig `.so`; plotter evaluate round-trip matches Djinni
- [ ] zigArena: false → byte-identical; zigArena: true → no leaks across frames
- [ ] 6 SVGs generated/updated; all linked from README; translations synced
- [ ] Risk analysis updated (Z1-Z5 added, stale findings corrected)
