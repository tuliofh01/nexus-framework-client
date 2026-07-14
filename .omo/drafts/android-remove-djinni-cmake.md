---
slug: android-remove-djinni-cmake
status: awaiting-approval
intent: clear
pending-action: write .omo/plans/android-remove-djinni-cmake.md
approach: Remove all Djinni-generated files (Kotlin/JNI/C++), legacy CMake build, and regen scripts from android-app template. Replace with hand-authored C++ bridge files in zig-services/jni/ and clean Kotlin bridge files in app/src/main/java/. Update build.zig, build.gradle.kts, and all docs. Chaquopy Python runtime stays (required for Android Python). SDL Java glue stays (upstream, required). ALSO: Convert all C++ code across desktop-app, android-app, and shared/runtime to C++20 modules (import/export instead of #include). External libraries (SDL, ImGui, sol2, pybind11, Chaquopy) remain as traditional headers; only project-owned C++ is modularized.
---

# Draft: android-remove-djinni-cmake

## Components (topology ledger)
<!-- Lock the SHAPE before depth. One row per top-level component that can succeed or fail independently. -->
<!-- id | outcome (one line) | status: active|deferred | evidence path -->

| id | outcome | status | evidence path |
|----|---------|--------|---------------|
| C1 | C++ bridge headers (app_core.hpp, python_bridge.hpp, eval_result.hpp, NativePythonBridge.hpp) exist in zig-services/jni/ with no Djinni provenance | active | template/android-app/zig-services/jni/*.hpp |
| C2 | C++ bridge impls (app_core.cpp, NativePythonBridge.cpp) compile via zig c++ and delegate JNI to Kotlin | active | template/android-app/zig-services/jni/*.cpp |
| C3 | Kotlin bridge files (PythonBridge.kt, EvalResult.kt, AppCore.kt) in app/src/main/java/ compile and match JNI signatures | active | template/android-app/app/src/main/java/com/nexus/{{packageName}}/ |
| C4 | build.zig references only zig-services/jni/ sources, no djinni-generated/ | active | template/android-app/zig-services/build.zig |
| C5 | build.gradle.kts has no Djinni kotlin source dir, no CMake externalNativeBuild | active | template/android-app/app/build.gradle.kts |
| C6 | All Djinni/CMake files removed (djinni-generated/, djinni/, CMakeLists.txt, CMakePresets.json, regen-djinni.sh) | active | template/android-app/ |
| C7 | Docs updated (AGENTS.md, READMEs, zig-services/README.md) | active | template/android-app/AGENTS.md, template/android-app/README.md, template/android-app/zig-services/README.md |
| C8 | Shared runtime C++ (NexusTheme, FontConfig, ScriptArchive, ScriptCrypto) converted to C++20 module interface units (.cppm) | active | template/shared/runtime/ |
| C9 | Desktop app C++ (AppModel, AppController, PythonEngine, AppView, LuaPanels, FlowRunner) converted to C++20 modules | active | template/desktop-app/src/ |
| C10 | Android app C++ (AppModel, AppController, PythonEngine, AppView, LuaPanels, FlowRunner) converted to C++20 modules | active | template/android-app/src/ |
| C11 | JNI bridge files (app_core, NativePythonBridge, jni_bridge) remain as traditional .cpp (C linkage required for JNI) | active | template/android-app/zig-services/jni/ |
| C12 | build.zig updated to handle C++20 module dependencies (module map, import paths) | active | template/android-app/zig-services/build.zig, template/desktop-app/zig-services/build.zig |

## Open assumptions (announced defaults)
<!-- Record any default you adopt instead of asking, so the user can veto it at the gate. -->
<!-- assumption | adopted default | rationale | reversible? -->

| assumption | adopted default | rationale | reversible? |
|------------|----------------|-----------|-------------|
| Chaquopy retention | KEEP Chaquopy | Android Python needs JVM runtime; Zig JNI bridge calls Kotlin PythonBridge which uses Chaquopy | Yes - removing Chaquopy is a larger architectural change |
| Lua JNI bridge | Leave as stubs | Phase 4b future work; stubs are harmless | Yes - can implement later |
| SDL Java files | KEEP all 11 files in org/libsdl/app/ | Upstream SDL3 Java glue, required for any SDL Android app | No - removing breaks SDL |
| Premature file writes | 5 files already in zig-services/jni/ | Worker should verify match plan specs; NativePythonBridge.cpp not yet written | N/A |
| External libraries | Keep as traditional #include | SDL, ImGui, sol2, pybind11, Chaquopy don't support modules; wrap in module interface units | Yes - can modularize later if vendors add support |
| JNI bridge files | Keep as traditional .cpp | JNI requires C linkage and specific naming; C++20 modules can't guarantee ABI compatibility | Yes - can revisit if Zig adds module-aware JNI support |
| Module naming convention | export module nexus.<component>; e.g. export module nexus.app_model | Consistent namespace across desktop/android; shared modules use nexus.shared.* | Yes - can change before first release |

## Findings (cited - path:lines)

### Djinni-generated files to remove (9 files)
- `djinni-generated/kotlin/com/nexus/{{packageName}}/AppCore.kt:1-12` - JNI bridge loader + external fun
- `djinni-generated/kotlin/com/nexus/{{packageName}}/PythonBridge.kt:1-15` - Abstract base class
- `djinni-generated/kotlin/com/nexus/{{packageName}}/EvalResult.kt` - Data class
- `djinni-generated/jni/NativePythonBridge.hpp:1-30` - JNI glue header
- `djinni-generated/jni/NativePythonBridge.cpp:1-99` - JNI glue impl + extern JNI export
- `djinni-generated/cpp/app_core.hpp:1-16` - C++ AppCore header
- `djinni-generated/cpp/python_bridge.hpp:1-22` - C++ PythonBridge interface
- `djinni-generated/cpp/eval_result.hpp:1-21` - C++ EvalResult struct
- `djinni-generated/README.md` - Deprecation notice

### Djinni IDL + regen script (3 files)
- `djinni/app.djinni` - IDL for PythonBridge
- `djinni/plotter.djinni` - IDL for plotter bridge
- `scripts/regen-djinni.sh` - Regen script

### CMake files to remove (2 files)
- `CMakeLists.txt:1-103` - Legacy fallback build
- `CMakePresets.json` - CMake presets

### Files to update
- `zig-services/jni/jni_bridge.cpp:1-17` - Currently includes Djinni headers; needs local includes
- `zig-services/build.zig:10,45-48,113-116,125-126` - References djinni_root and djinni sources
- `app/build.gradle.kts:42-46,61-63` - CMake externalNativeBuild + Djinni kotlin source dir
- `zig-services/README.md` - References Djinni deprecation
- `AGENTS.md` - References Djinni
- `README.md` - References Djinni/CMake

### Files that MUST STAY (not ours)
- `app/src/main/java/org/libsdl/app/*.java` (11 files) - Upstream SDL Java glue
- `app/src/main/java/com/nexus/{{packageName}}/MainActivity.kt` - App entry point
- `app/src/main/java/com/nexus/{{packageName}}/NexusApplication.kt` - Chaquopy startup
- `app/src/main/java/com/nexus/{{packageName}}/ChaquopyPythonBridge.kt` - Python bridge impl
- `app/src/main/java/com/nexus/{{packageName}}/FlowRunner.kt` - Flow management
- `app/src/main/java/com/nexus/{{packageName}}/AssetExtractor.kt` - Asset extraction

### Premature file writes (5 files exist in zig-services/jni/)
- `zig-services/jni/app_core.hpp` - Written during planning, correct target state
- `zig-services/jni/app_core.cpp` - Written during planning, correct target state
- `zig-services/jni/python_bridge.hpp` - Written during planning, correct target state
- `zig-services/jni/eval_result.hpp` - Written during planning, correct target state
- `zig-services/jni/NativePythonBridge.hpp` - Written during planning, correct target state
- `zig-services/jni/NativePythonBridge.cpp` - NOT written (blocked), must be created

## Decisions (with rationale)

| decision | rationale |
|----------|-----------|
| Replace Djinni with hand-authored C++ + Kotlin | Zig JNI export fn is the JNI entry point; Djinni codegen is unnecessary overhead |
| Keep Chaquopy | Android Python requires JVM runtime; removing Chaquopy would break Python support entirely |
| Remove CMakeLists.txt | User explicitly requested; Zig is now the primary build path |
| Remove Djinni IDL files | No longer needed; hand-authored files replace generated code |
| Keep SDL Java files | Upstream SDL3 requirement; not part of the Djinni/JNI bridge |

## Scope IN

- Remove all Djinni-generated files (Kotlin/JNI/C++)
- Remove Djinni IDL files and regen script
- Remove CMakeLists.txt and CMakePresets.json
- Create hand-authored C++ bridge files in zig-services/jni/
- Create clean Kotlin bridge files in app/src/main/java/
- Update build.zig to remove Djinni references
- Update build.gradle.kts to remove Djinni source dir and CMake externalNativeBuild
- Update all READMEs and AGENTS.md files
- Verify JNI export function works (Zig export fn → C++ → Kotlin)

## Scope OUT (Must NOT have)

- DO NOT remove Chaquopy or Python support from Android
- DO NOT remove SDL Java files (org/libsdl/app/)
- DO NOT modify app logic files (MainActivity.kt, NexusApplication.kt, FlowRunner.kt, AssetExtractor.kt)
- DO NOT implement Lua JNI bridge (Phase 4b future work, keep stubs)
- DO NOT modify desktop-app template
- DO NOT modify misc/core, misc/cli, or :app modules

## Open questions

None. Chaquopy retention and Lua JNI stubs are adopted as defaults (see Open assumptions).

## Approval gate
status: awaiting-approval
<!-- When exploration is exhausted and unknowns are answered, set status: awaiting-approval. -->
<!-- That durable record is the loop guard: on a later turn read it and resume at the gate instead of re-running exploration. -->
