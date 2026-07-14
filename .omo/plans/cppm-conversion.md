# cppm-conversion - Work Plan

## TL;DR (For humans)
<!-- Fill this LAST, after the detailed plan below is written, so it summarizes the REAL plan. -->
<!-- Plain English for a non-engineer: NO file paths, NO todo numbers, NO wave/agent/tool names. -->

**What you'll get:** Every C++ class in the desktop and Android app templates becomes a modern C++20 module (one file per class, with teaching comments), the old header/source pairs are removed, the dead Djinni bridge is deleted, both apps' entry points switch to importing the new modules, fresh diagrams and UI mockups are generated, and the README is rewritten to read like a fun, search-friendly pitch that shows off how complex this framework really is. Each step is committed and pushed on its own.

**Why this approach:** We split the work into 23 tiny, independent tasks (one class = one task) so no single agent is overloaded and everything runs in parallel waves; a single shared brief + one exemplar file keep every conversion identical in style. Build-system changes are deliberately deferred — this is a source-only migration.

**What it will NOT do:** It will not change how the apps behave at runtime, add new dependencies, run a full native build, touch agent config, or delete any module file or the Zig services. It will not write iOS code.

**Effort:** Large
**Risk:** Medium - the main risk is a missed `#include`→`import` translation or an accidentally deleted still-referenced file; both are caught by the per-task self-review and the two verification tasks.
**Decisions to sanity-check:** (1) Desktop PythonEngine keeps pybind11, Android keeps Chaquopy/JVM + Zig JNI — confirmed by the brief. (2) Obsolete deletions are scoped to known dupes + the Djinni tree only. (3) Per-task git push is required by your instruction.

Your next move: approve this plan, then start work (execution follows the waves exactly). Full execution detail follows below.

---

> TL;DR (machine): Large/Medium — 23 atomic tasks converting 14 C++ classes to C++20 modules + main rewrites + obsolete deletion + verify + images + README/docs; per-task tidy/commit/push; 3 waves + final verification.

## Scope
### Must have
- Convert all 8 desktop-app C++ classes (AppModel, FunctionRegistry, AppController, PythonEngine, AppView, LuaPanels, FlowRunner, PlotController) to C++20 modules (`.cppm`) with educational comments, following the `NexusBridge.cppm` exemplar.
- Convert all 6 android-app C++ classes (AppModel, AppController, PythonEngine, AppView, LuaPanels, FlowRunner) to C++20 modules.
- Rewrite both `main.cpp` files to `import` the new modules.
- Delete obsolete files: shared/runtime `.hpp` dupes + `Paths.hpp`; android `djinni-generated/`, `djinni/`, `scripts/regen-djinni.sh`.
- Verify converted modules (clang -fsyntax-only best-effort or structured self-review).
- Generate architecture/UI images (dependency graph, import map, 2 mockups).
- Rewrite README (fun/pragmatic/SEO, complexity highlight, expanded comparisons).
- Audit `docs/` for accuracy vs the new module architecture.
- **Per-task git workflow:** after each task, TIDY the projects folder (remove temp/stray files), `git add -A && git commit` with a conventional message, then `git push`.

### Must NOT have (guardrails, anti-slop, scope boundaries)
- NO ASCII-art box diagrams in comments (plain `// ===` banners OK).
- NO behavior changes — restructure/modernize style + comments only.
- NO new dependencies.
- NO full module build (build-system update is DEFERRED — source-only migration).
- NO editing of `opencode.jsonc` or other agent config.
- NO deletion of `.cppm` files, `zig-services/`, or files still `#include`d by non-converted code.
- NO iOS template work (out of scope per README).
- Do NOT skip the per-task tidy + commit + push step.

## Verification strategy
> Zero human intervention - all verification is agent-executed.
- Test decision: tests-after (self-review + clang -fsyntax-only spot-check) + framework
- Evidence: `.omo/evidence/task-<N>-cppm-conversion.<ext>` for every task
- Each conversion task self-verifies: correct `export module` name, correct `import` edges, no leftover project `#include`, originals deleted.
- Verification tasks 19-20 run `clang -fsyntax-only -std=c++20` best-effort; if the toolchain lacks module support, a documented structured self-review substitutes (noted in evidence).

## Execution strategy
### Parallel execution waves
> Target 5-8 todos per wave. Fewer than 3 (except the final) means you under-split.
- **Wave 1 (20 parallel tasks):** tasks 1-8 (desktop classes), 10-15 (android classes), 17-18 (obsolete deletions), 21-23 (images, README, docs-audit). All independent.
- **Wave 2 (4 parallel tasks):** tasks 9, 16 (main rewrites) + 19, 20 (verification). Mains depend on Wave-1 class conversions; verification can run alongside mains once modules exist.
- **Wave 3 (Final verification wave):** F1-F4 run in parallel after ALL todos.

### Dependency matrix
| Todo | Depends on | Blocks | Can parallelize with |
| --- | --- | --- | --- |
| 1 desktop AppModel | — | 9 | 2-8, 10-15, 17-23 |
| 2 desktop FunctionRegistry | — | 9 | 1, 3-8, 10-15, 17-23 |
| 3 desktop AppController | — | 9 | 1-2, 4-8, 10-15, 17-23 |
| 4 desktop PythonEngine | — | 9 | 1-3, 5-8, 10-15, 17-23 |
| 5 desktop AppView | — | 9 | 1-4, 6-8, 10-15, 17-23 |
| 6 desktop LuaPanels | — | 9 | 1-5, 7-8, 10-15, 17-23 |
| 7 desktop FlowRunner | — | 9 | 1-6, 8, 10-15, 17-23 |
| 8 desktop PlotController | — | 9 | 1-7, 10-15, 17-23 |
| 9 desktop main | 1-8 | 19 | 16, 19-20 |
| 10 android AppModel | — | 16 | 1-8, 11-15, 17-23 |
| 11 android AppController | — | 16 | 1-8, 10, 12-15, 17-23 |
| 12 android PythonEngine | — | 16 | 1-8, 10-11, 13-15, 17-23 |
| 13 android AppView | — | 16 | 1-8, 10-12, 14-15, 17-23 |
| 14 android LuaPanels | — | 16 | 1-8, 10-13, 15, 17-23 |
| 15 android FlowRunner | — | 16 | 1-8, 10-14, 17-23 |
| 16 android main | 10-15 | 20 | 9, 19-20 |
| 17 delete shared/runtime dupes | — | 19 | 1-8, 10-16, 18, 21-23 |
| 18 delete android djinni tree | — | 20 | 1-8, 10-17, 21-23 |
| 19 verify desktop | 1-9 | F1-F4 | 16, 20 |
| 20 verify android | 10-18 | F1-F4 | 9, 19 |
| 21 images | — | F1-F4 | 1-20, 22-23 |
| 22 README rewrite | — | F1-F4 | 1-21, 23 |
| 23 docs audit | — | F1-F4 | 1-22 |

## Todos
> Implementation + Test = ONE todo. Never separate.
<!-- APPEND TASK BATCHES BELOW THIS LINE WITH edit/apply_patch - never rewrite the headers above. -->
- [x] 0. Conversion brief (DONE — reference artifact)
  What to do / Must NOT do: Brief already written at `/tmp/opencode/cppm-conversion-brief.md`. Use it as the single source of truth for module names, style rules, and verification approach. Do NOT recreate or modify it.
  Parallelization: Wave 0 (prerequisite, already complete) | Blocked by: none | Blocks: all conversion tasks
  References: `/tmp/opencode/cppm-conversion-brief.md`; exemplar `template/desktop-app/src/bridge/NexusBridge.cppm`
  Acceptance criteria: Brief exists and is read by every downstream task.
  QA scenarios: n/a (already complete)
  Commit: N

- [ ] 1. Convert desktop AppModel → `nxs.desktop.model`
  What to do / Must NOT do: Read `template/desktop-app/src/model/AppModel.hpp` + `AppModel.cpp`. Create `template/desktop-app/src/model/AppModel.cppm` with `module;` global fragment, `export module nxs.desktop.model;`, all method defs inlined, educational comments (NO ASCII-art). Delete the original `.hpp`/`.cpp` after verified. Must NOT change behavior, add deps, or use box-drawing in comments.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9 (desktop main)
  References (executor has NO interview context): `/tmp/opencode/cppm-conversion-brief.md` (rules 1-7, module map); exemplar `template/desktop-app/src/bridge/NexusBridge.cppm`; source `template/desktop-app/src/model/AppModel.hpp`, `template/desktop-app/src/model/AppModel.cpp`
  Acceptance criteria: `AppModel.cppm` exists; contains `export module nxs.desktop.model;`; original `.hpp`+`.cpp` deleted; no `#include "*.hpp"` of project headers remains (translated to `import`); self-review checklist passed.
  QA scenarios: happy — `grep -l "export module nxs.desktop.model" template/desktop-app/src/model/AppModel.cppm` succeeds AND `ls template/desktop-app/src/model/AppModel.{hpp,cpp}` both fail. failure — module name mismatch or original files still present → reject. Evidence `.omo/evidence/task-1-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop AppModel to C++20 module — then TIDY projects folder (remove temp/stray files) and `git add -A && git commit && git push`

- [ ] 2. Convert desktop FunctionRegistry → `nxs.desktop.func`
  What to do / Must NOT do: Read `template/desktop-app/src/model/FunctionRegistry.hpp` + `.cpp`. Create `template/desktop-app/src/model/FunctionRegistry.cppm` (`export module nxs.desktop.func;`), inline defs, educational comments. Delete originals after verified. Preserve exact behavior.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief (rules, map); exemplar `NexusBridge.cppm`; `template/desktop-app/src/model/FunctionRegistry.hpp`, `.cpp`
  Acceptance criteria: `FunctionRegistry.cppm` exists with correct module name; originals deleted; no project `#include` left; self-review passed.
  QA scenarios: happy — module name correct + originals gone. failure — leftover `.hpp` include → reject. Evidence `.omo/evidence/task-2-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop FunctionRegistry to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 3. Convert desktop AppController → `nxs.desktop.controller`
  What to do / Must NOT do: Read `template/desktop-app/src/controller/AppController.hpp` + `.cpp`. Create `template/desktop-app/src/controller/AppController.cppm` (`export module nxs.desktop.controller;`). Inline defs, educational comments. Delete originals. Preserve behavior.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief; exemplar; `template/desktop-app/src/controller/AppController.hpp`, `.cpp`
  Acceptance criteria: `AppController.cppm` exists, correct module name, originals deleted, no project `#include`, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — behavior change suspected → diff against original. Evidence `.omo/evidence/task-3-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop AppController to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 4. Convert desktop PythonEngine → `nxs.desktop.python`
  What to do / Must NOT do: Read `template/desktop-app/src/controller/PythonEngine.hpp` + `.cpp`. Create `template/desktop-app/src/controller/PythonEngine.cppm` (`export module nxs.desktop.python;`). Desktop uses pybind11 — keep as-is. Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief (desktop = pybind11, NOT Chaquopy); exemplar; `template/desktop-app/src/controller/PythonEngine.hpp`, `.cpp`
  Acceptance criteria: `PythonEngine.cppm` exists, correct module name, originals deleted, pybind11 includes in `module;` fragment, self-review passed.
  QA scenarios: happy — pybind11 include in global fragment + name correct. failure — pybind11 include left outside `module;` → reject. Evidence `.omo/evidence/task-4-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop PythonEngine to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 5. Convert desktop AppView → `nxs.desktop.view`
  What to do / Must NOT do: Read `template/desktop-app/src/view/AppView.hpp` + `.cpp`. Create `template/desktop-app/src/view/AppView.cppm` (`export module nxs.desktop.view;`). Inline defs, educational comments (ImGui usage). Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief; exemplar; `template/desktop-app/src/view/AppView.hpp`, `.cpp`
  Acceptance criteria: `AppView.cppm` exists, correct module name, originals deleted, ImGui includes in `module;` fragment, self-review passed.
  QA scenarios: happy — ImGui include in global fragment. failure — missing method defs → reject. Evidence `.omo/evidence/task-5-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop AppView to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 6. Convert desktop LuaPanels → `nxs.desktop.lua`
  What to do / Must NOT do: Read `template/desktop-app/src/view/LuaPanels.hpp` + `.cpp`. Create `template/desktop-app/src/view/LuaPanels.cppm` (`export module nxs.desktop.lua;`). sol2 includes in `module;` fragment. Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief; exemplar; `template/desktop-app/src/view/LuaPanels.hpp`, `.cpp`
  Acceptance criteria: `LuaPanels.cppm` exists, correct module name, originals deleted, sol2 include in global fragment, self-review passed.
  QA scenarios: happy — sol2 include in fragment. failure — leftover `.hpp` include → reject. Evidence `.omo/evidence/task-6-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop LuaPanels to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 7. Convert desktop FlowRunner → `nxs.desktop.flow`
  What to do / Must NOT do: Read `template/desktop-app/src/service/FlowRunner.hpp` + `.cpp`. Create `template/desktop-app/src/service/FlowRunner.cppm` (`export module nxs.desktop.flow;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief; exemplar; `template/desktop-app/src/service/FlowRunner.hpp`, `.cpp`
  Acceptance criteria: `FlowRunner.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — behavior change → diff. Evidence `.omo/evidence/task-7-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop FlowRunner to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 8. Convert desktop PlotController → `nxs.desktop.plot`
  What to do / Must NOT do: Read `template/desktop-app/src/controller/PlotController.hpp` + `.cpp`. Create `template/desktop-app/src/controller/PlotController.cppm` (`export module nxs.desktop.plot;`). ImPlot includes in `module;` fragment. Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 9
  References: brief; exemplar; `template/desktop-app/src/controller/PlotController.hpp`, `.cpp`
  Acceptance criteria: `PlotController.cppm` exists, correct module name, originals deleted, ImPlot include in global fragment, self-review passed.
  QA scenarios: happy — ImPlot in fragment. failure — missing defs → reject. Evidence `.omo/evidence/task-8-cppm-conversion.md`
  Commit: Y | feat(template): convert desktop PlotController to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 9. Rewrite desktop `main.cpp` to import `nxs.desktop.*`
  What to do / Must NOT do: Read `template/desktop-app/src/main.cpp`. Rewrite to `import nxs.desktop.model; import nxs.desktop.func; import nxs.desktop.controller; import nxs.desktop.python; import nxs.desktop.view; import nxs.desktop.lua; import nxs.desktop.flow; import nxs.desktop.plot;` and call the same entry logic. Must NOT alter program behavior; keep `int main()` signature.
  Parallelization: Wave 2 | Blocked by: tasks 1-8 | Blocks: task 19 (verify-desktop)
  References: brief (rule 4); exemplar `NexusBridge.cppm`; `template/desktop-app/src/main.cpp`; all 8 desktop `.cppm` files from tasks 1-8
  Acceptance criteria: `main.cpp` uses `import nxs.desktop.*;` (8 imports), no `#include` of converted project headers, entry behavior preserved.
  QA scenarios: happy — 8 import statements present, no project `#include`. failure — missing import or behavior change → reject. Evidence `.omo/evidence/task-9-cppm-conversion.md`
  Commit: Y | feat(template): rewrite desktop main.cpp to import C++20 modules — TIDY + `git add -A && git commit && git push`

- [ ] 10. Convert android AppModel → `nxs.android.model`
  What to do / Must NOT do: Read `template/android-app/src/model/AppModel.hpp` + `.cpp`. Create `template/android-app/src/model/AppModel.cppm` (`export module nxs.android.model;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16 (android main)
  References: brief (android module map); exemplar; `template/android-app/src/model/AppModel.hpp`, `.cpp`
  Acceptance criteria: `AppModel.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — leftover `.hpp` include → reject. Evidence `.omo/evidence/task-10-cppm-conversion.md`
  Commit: Y | feat(template): convert android AppModel to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 11. Convert android AppController → `nxs.android.controller`
  What to do / Must NOT do: Read `template/android-app/src/controller/AppController.hpp` + `.cpp`. Create `template/android-app/src/controller/AppController.cppm` (`export module nxs.android.controller;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16
  References: brief; exemplar; `template/android-app/src/controller/AppController.hpp`, `.cpp`
  Acceptance criteria: `AppController.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — behavior change → diff. Evidence `.omo/evidence/task-11-cppm-conversion.md`
  Commit: Y | feat(template): convert android AppController to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 12. Convert android PythonEngine → `nxs.android.python`
  What to do / Must NOT do: Read `template/android-app/src/controller/PythonEngine.hpp` + `.cpp`. Create `template/android-app/src/controller/PythonEngine.cppm` (`export module nxs.android.python;`). Android = Chaquopy/JVM + Zig JNI (NOT pybind11) — keep as-is. Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16
  References: brief (android PythonEngine = Chaquopy/JVM + Zig JNI); exemplar; `template/android-app/src/controller/PythonEngine.hpp`, `.cpp`
  Acceptance criteria: `PythonEngine.cppm` exists, correct module name, originals deleted, Chaquopy/JNI includes in `module;` fragment, self-review passed.
  QA scenarios: happy — Chaquopy/JNI include in global fragment, no pybind11. failure — pybind11 present → reject. Evidence `.omo/evidence/task-12-cppm-conversion.md`
  Commit: Y | feat(template): convert android PythonEngine to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 13. Convert android AppView → `nxs.android.view`
  What to do / Must NOT do: Read `template/android-app/src/view/AppView.hpp` + `.cpp`. Create `template/android-app/src/view/AppView.cppm` (`export module nxs.android.view;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16
  References: brief; exemplar; `template/android-app/src/view/AppView.hpp`, `.cpp`
  Acceptance criteria: `AppView.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — missing defs → reject. Evidence `.omo/evidence/task-13-cppm-conversion.md`
  Commit: Y | feat(template): convert android AppView to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 14. Convert android LuaPanels → `nxs.android.lua`
  What to do / Must NOT do: Read `template/android-app/src/view/LuaPanels.hpp` + `.cpp`. Create `template/android-app/src/view/LuaPanels.cppm` (`export module nxs.android.lua;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16
  References: brief; exemplar; `template/android-app/src/view/LuaPanels.hpp`, `.cpp`
  Acceptance criteria: `LuaPanels.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — leftover `.hpp` include → reject. Evidence `.omo/evidence/task-14-cppm-conversion.md`
  Commit: Y | feat(template): convert android LuaPanels to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 15. Convert android FlowRunner → `nxs.android.flow`
  What to do / Must NOT do: Read `template/android-app/src/service/FlowRunner.hpp` + `.cpp`. Create `template/android-app/src/service/FlowRunner.cppm` (`export module nxs.android.flow;`). Inline defs, educational comments. Delete originals.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 16
  References: brief; exemplar; `template/android-app/src/service/FlowRunner.hpp`, `.cpp`
  Acceptance criteria: `FlowRunner.cppm` exists, correct module name, originals deleted, self-review passed.
  QA scenarios: happy — name + deletion correct. failure — behavior change → diff. Evidence `.omo/evidence/task-15-cppm-conversion.md`
  Commit: Y | feat(template): convert android FlowRunner to C++20 module — TIDY + `git add -A && git commit && git push`

- [ ] 16. Rewrite android `main.cpp` to import `nxs.android.*`
  What to do / Must NOT do: Read `template/android-app/src/main.cpp`. Rewrite to `import nxs.android.model; import nxs.android.controller; import nxs.android.python; import nxs.android.view; import nxs.android.lua; import nxs.android.flow;` and call the same entry logic. Must NOT alter behavior; keep `int main()` signature.
  Parallelization: Wave 2 | Blocked by: tasks 10-15 | Blocks: task 20 (verify-android)
  References: brief (rule 4); exemplar; `template/android-app/src/main.cpp`; all 6 android `.cppm` files from tasks 10-15
  Acceptance criteria: `main.cpp` uses `import nxs.android.*;` (6 imports), no `#include` of converted project headers, entry behavior preserved.
  QA scenarios: happy — 6 import statements present, no project `#include`. failure — missing import → reject. Evidence `.omo/evidence/task-16-cppm-conversion.md`
  Commit: Y | feat(template): rewrite android main.cpp to import C++20 modules — TIDY + `git add -A && git commit && git push`

- [ ] 17. Delete obsolete shared/runtime dupes + Paths.hpp
  What to do / Must NOT do: Delete `template/shared/runtime/` duplicate headers that are now superseded by `.cppm` modules (verify which `.hpp` have a `.cppm` twin and remove the `.hpp` only if a `.cppm` provides the same API). Also remove `Paths.hpp` if it is a legacy dupe of `paths.cppm`. Must NOT delete any `.cppm` or files still `#include`d by non-converted code.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 19 (verify-desktop)
  References: brief; `template/shared/runtime/*.cppm` (7 exist: nexus_theme, font_config, paths, script_crypto, script_archive, script_protection, zig_allocator); `template/shared/runtime/*.hpp` (inspect for dupes)
  Acceptance criteria: Obsolete `.hpp` dupes removed; no `.cppm` deleted; `grep -r "Paths.hpp"` returns no references before deletion.
  QA scenarios: happy — dupe `.hpp` gone, `.cppm` intact. failure — deleted a still-referenced file → restore. Evidence `.omo/evidence/task-17-cppm-conversion.md`
  Commit: Y | chore(template): remove obsolete shared/runtime header dupes — TIDY + `git add -A && git commit && git push`

- [ ] 18. Delete android djinni tree
  What to do / Must NOT do: Delete `template/android-app/djinni-generated/`, `template/android-app/djinni/`, and `template/android-app/scripts/regen-djinni.sh`. These are superseded by the Zig JNI bridge (Phase 4). Must NOT delete `zig-services/` or any `.cppm`.
  Parallelization: Wave 1 | Blocked by: none | Blocks: task 20 (verify-android)
  References: brief; `template/android-app/djinni-generated/`, `template/android-app/djinni/`, `template/android-app/scripts/regen-djinni.sh`; README "Android Zig JNI (retire Djinni)" note
  Acceptance criteria: Three paths deleted; `grep -r "djinni" template/android-app/src/` returns no `#include` of djinni headers (those were in deleted tree); `zig-services/` untouched.
  QA scenarios: happy — djinni paths gone, zig-services intact. failure — accidentally removed zig-services → restore. Evidence `.omo/evidence/task-18-cppm-conversion.md`
  Commit: Y | chore(template): remove android djinni tree (superseded by Zig JNI) — TIDY + `git add -A && git commit && git push`

- [ ] 19. Verify desktop converted modules (clang -fsyntax-only spot-check)
  What to do / Must NOT do: Run `clang -fsyntax-only -std=c++20` on each desktop `.cppm` (best-effort; if clang lacks module support, do a structured self-review instead and note it). Confirm all 8 modules + main.cpp are internally consistent (correct `import` edges, no missing symbols). Must NOT run a full build (deferred).
  Parallelization: Wave 2 | Blocked by: tasks 1-9 | Blocks: Final wave
  References: brief (verification section); all desktop `.cppm` + `main.cpp` from tasks 1-9
  Acceptance criteria: Each `.cppm` passes `clang -fsyntax-only` OR has a documented self-review note; no leftover project `#include`; import names match module names.
  QA scenarios: happy — all modules consistent. failure — undefined symbol or wrong import → fix at source task. Evidence `.omo/evidence/task-19-cppm-conversion.md`
  Commit: Y | test(template): verify desktop C++20 modules — TIDY + `git add -A && git commit && git push`

- [ ] 20. Verify android converted modules (clang -fsyntax-only spot-check)
  What to do / Must NOT do: Run `clang -fsyntax-only -std=c++20` on each android `.cppm` (best-effort). Confirm all 6 modules + main.cpp consistent. Must NOT run a full build.
  Parallelization: Wave 2 | Blocked by: tasks 10-18 | Blocks: Final wave
  References: brief (verification); all android `.cppm` + `main.cpp` from tasks 10-16; djinni removed in task 18
  Acceptance criteria: Each `.cppm` passes or has self-review note; no djinni `#include` remains; import names match.
  QA scenarios: happy — all modules consistent, no djinni refs. failure — djinni include left → fix. Evidence `.omo/evidence/task-20-cppm-conversion.md`
  Commit: Y | test(template): verify android C++20 modules — TIDY + `git add -A && git commit && git push`

- [ ] 21. Generate architecture + UI images
  What to do / Must NOT do: Produce diagrams: (a) desktop vs android module dependency graph, (b) C++20 module import map, (c) 2 UI mockups (desktop + android) reflecting the new architecture. Use vision-capable free model for visual quality. Output to `docs/assets/diagrams/` and `docs/assets/mockups/`. Must NOT use ASCII-art; produce real raster/SVG images.
  Parallelization: Wave 1 | Blocked by: none | Blocks: Final wave
  References: README "Engineering showcase" section; `docs/assets/diagrams/` existing SVGs for style; brief
  Acceptance criteria: ≥3 image files created (dependency graph, import map, 2 mockups); referenced from docs or committed alongside.
  QA scenarios: happy — images render, non-blank. failure — blank/garbled → regenerate. Evidence `.omo/evidence/task-21-cppm-conversion.md`
  Commit: Y | docs(assets): add architecture + UI diagrams for C++20 modules — TIDY + `git add -A && git commit && git push`

- [ ] 22. Rewrite README (fun pragmatic voice, SEO, complexity, comparisons)
  What to do / Must NOT do: Rewrite the top "What is Nexus?" + "How Nexus compares" + "Engineering showcase" sections with a fun, pragmatic tone; optimize for SEO (keywords: native app framework, C++20 modules, SDL3, ImGui, Lua, Python); explicitly highlight the 7-language / 3-build-system / 2-platform complexity; expand comparisons vs Electron, Tauri, Flutter, Compose Multiplatform, WebAssembly. Must NOT remove factual accuracy or existing translation links.
  Parallelization: Wave 1 | Blocked by: none | Blocks: Final wave
  References: current `README.md`; brief; `docs/architecture/runtime-stack.md`
  Acceptance criteria: README sections rewritten; SEO keywords present; comparison table expanded; tone is fun/pragmatic; translation header intact.
  QA scenarios: happy — keywords found, comparisons expanded, links intact. failure — broken markdown or removed translations → fix. Evidence `.omo/evidence/task-22-cppm-conversion.md`
  Commit: Y | docs(readme): rewrite with fun pragmatic voice + SEO + expanded comparisons — TIDY + `git add -A && git commit && git push`

- [ ] 23. Audit docs/ for accuracy vs new module architecture
  What to do / Must NOT do: Review `docs/` (architecture, guides, templates) for references to the old `.hpp`/`.cpp` class layout or Djinni; update or flag them. Must NOT invent new docs; only reconcile existing ones with the module migration.
  Parallelization: Wave 1 | Blocked by: none | Blocks: Final wave
  References: `docs/architecture/`, `docs/guides/`, `docs/templates/`; brief; converted `.cppm` names
  Acceptance criteria: No doc references the deleted `.hpp`/`.cpp` class files or djinni without noting the migration; flagged items listed in evidence.
  QA scenarios: happy — grep for old class filenames in docs returns only migration notes. failure — contradictory doc → update. Evidence `.omo/evidence/task-23-cppm-conversion.md`
  Commit: Y | docs(audit): reconcile docs with C++20 module migration — TIDY + `git add -A && git commit && git push`

## Final verification wave
> Runs in parallel after ALL todos. ALL must APPROVE. Surface results and wait for the user's explicit okay before declaring complete.
- [ ] F1. Plan compliance audit
- [ ] F2. Code quality review
- [ ] F3. Real manual QA
- [ ] F4. Scope fidelity

## Commit strategy
- Every task commits its changes atomically: `git add -A && git commit -m "<type>(<scope>): <summary>" && git push`.
- Conventional commit types: `feat(template):` for conversions, `chore(template):` for deletions, `test(template):` for verification, `docs(assets):` for images, `docs(readme):` for README, `docs(audit):` for docs audit.
- Scope is always `template` (or `template/desktop-app`, `template/android-app` where precise).
- Summary line is the task title; body may note the module name or files deleted.
- **Required per your instruction:** after each task, TIDY the projects folder (remove temp/stray files), then commit + push. No batched commits.

## Success criteria
- All 14 C++ classes converted to `.cppm` with correct `export module nxs.<target>.<area>;` names.
- All 8 desktop + 6 android original `.hpp`/`.cpp` pairs deleted.
- Both `main.cpp` files rewritten to `import` their respective modules (8 desktop, 6 android imports).
- Obsolete paths removed: shared/runtime `.hpp` dupes + `Paths.hpp`; android `djinni-generated/`, `djinni/`, `scripts/regen-djinni.sh`.
- Verification tasks 19-20 pass (clang -fsyntax-only or documented self-review) with zero leftover project `#include` or djinni references.
- ≥3 image files created in `docs/assets/diagrams/` and/or `docs/assets/mockups/`.
- README rewritten: fun/pragmatic tone, SEO keywords present, comparison table expanded, translation links intact.
- `docs/` audit complete: no references to deleted class files or djinni without migration note.
- 23 conventional commits pushed (one per task), no stray files left in the repo.
- Final verification wave (F1-F4) all APPROVE.
