# Agent Readiness — Nexus Framework (Framework repo)

**Scan date:** July 10, 2026  
**Target:** `/Framework/` — Kotlin Compose scaffold + C++/Lua/Python/Android **templates** (not a REST API)  
**Verdict:** **44/100 — NOT AGENT-READY** (target ≥70 with zero critical doc/code mismatches)

This report captures an [API Readiness Analyzer](https://github.com/tuliofh01/nexus-framework-client)–style audit adapted for a multi-language scaffold repo. Use it when onboarding AI agents or contributors who must build, extend, or generate Nexus projects without guessing.

---

## Pillar breakdown

| Pillar | Score | Notes |
|--------|-------|-------|
| Orientation (AGENTS/README) | 55% | `AGENTS.md` exists; README previously oversold wizard capabilities — partially fixed in `c3f316c` |
| Build & run | 40% | Works with JDK **26**; older README said **21** → build failed on typical setups |
| Architecture docs | 75% | Solid `docs/` hub, diagrams, guides |
| Template discoverability | 82% | `template/` well-structured, `nxs_config.json`, CMake presets |
| Doc/code fidelity | 25% | **Critical** — wizard/generation documented in places but not implemented in Kotlin |
| Tooling & validation | 15% | No CLI, no `nexus-dev.sh`, no CI |
| IDE/agent setup | 20% | No `.vscode/`, no Cursor rules |
| Extension paths | 60% | MVC layout clear in `app/` and templates |

---

## Critical failures (blockers)

1. **Wizard UI does not exist** — README, `docs/architecture/overview.md`, and older Quick Start text described:
   - Home screen → "Create Desktop App" / "Create Android App"
   - 6-step wizard + imnodes blueprint editor  
   **Reality:** `app/` is a **Counter demo** only (`App.kt` → `CounterScreen`). No generation, wizard, or template-copy code in Kotlin.

2. **JDK prerequisite was wrong in README** — README + pt-BR previously said **JDK 21**; `buildSrc` uses `jvmToolchain(26)`. Build fails on JDK 21:
   > `Dependency requires at least JVM runtime version 26`  
   `AGENTS.md` and README (post-`c3f316c`) now correctly say Java 26.

3. **No generation/CLI path** — Templates exist under `template/` but there is no `ProjectGenerator`, `:cli`, or headless `generate` command. An agent following older README text cannot scaffold a project from the client.

4. **Phantom `utils/` module** — Older README layout listed `utils/` (NexusBranding); it lives in `app/src/.../model/NexusBranding.kt`. Fixed in `c3f316c`.

---

## What works well

- **`AGENTS.md`** — build commands, MVC map, edit locations (for the Counter client)
- **`docs/README.md`** hub — architecture, templates, coding guide linked
- **Templates** — desktop/android plotter samples, `blueprint.json`, `nxs_config.json`, `CMakePresets.json`, shared themes/DSL/runtime
- **`template/README.md`** — clear index of desktop/android/shared
- **Bilingual README** (EN + pt-BR)
- **Build succeeds** with `JAVA_HOME=/usr/lib/jvm/java-26-openjdk` + `./gradlew :app:compileKotlin :app:test`
- **Post-`c3f316c` README** — honest Development status, JDK 26 prerequisites, manual template workflow

---

## Top 5 fixes (by impact)

| # | Fix | Why agents fail without it | Est. impact |
|---|-----|---------------------------|-------------|
| 1 | Keep **Implementation status** prominent in README + `AGENTS.md` ("Counter stub; wizard planned") | Agent runs `./gradlew :app:run` expecting wizard, gets counter | +12 |
| 2 | Align **JDK docs** everywhere — README/pt-BR → Java 26 (done in `c3f316c`) | First build fails on typical JDK 21 setups | +10 |
| 3 | Document **manual template workflow** until wizard ships: `cd template/desktop-app && cmake --preset debug` (done in `c3f316c`) | No in-app path to generated projects | +8 |
| 4 | Remove/fix **`utils/`** in README; point NexusBranding to `app/.../model/` (done in `c3f316c`) | Wrong file search targets | +4 |
| 5 | Add **`.vscode/settings.json`** (`java.configuration.runtimes` → 26) | Repeatable agent onboarding | +6 |

**Sibling reference:** [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (separate repo) is ahead — `:core`, `:cli`, `scripts/nexus-dev.sh`, `debug validate`, fuller `AGENTS.md`.

---

## Agent onboarding reality check

| Task | Can agent do it today? |
|------|------------------------|
| Clone & build client | ⚠️ Only with JDK 26 (README now documents this) |
| Run wizard / generate app | ❌ Not implemented |
| Build template directly | ✅ `cmake --preset debug` in `template/desktop-app/` |
| Extend Compose client MVC | ✅ Clear `model/view/controller` layout |
| Extend generated C++ app | ✅ Well-documented template MVC |
| Validate templates automatically | ❌ No CLI/CI |

---

## Remaining doc gaps

Even after `c3f316c`, agents should watch for:

- `docs/architecture/overview.md` — wizard/imnodes described in present tense in the scaffold-client row; treat as roadmap until Kotlin code ships
- `docs/guides/coding-with-nexus.md` — step 6 may still reference the imnodes editor; edit `blueprint.json` manually until the wizard ships
- No machine-readable project manifest (`nxs_config.json` exists in templates, not in the client repo root)

---

## Related

- [Architecture overview](overview.md)
- [Documentation hub](../README.md)
- [AGENTS.md](../../AGENTS.md)
