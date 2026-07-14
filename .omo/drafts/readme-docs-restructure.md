---
slug: readme-docs-restructure
status: approved
intent: clear
pending-action: execute .omo/plans/readme-docs-restructure.md
approach: Comprehensive README rewrite (fun, SEO, pragmatic, educational, comparative) + docs audit + repo cleansing of obsolete files
---

# Draft: readme-docs-restructure

## Components (topology ledger)

| id | outcome | status | evidence |
|----|---------|--------|----------|
| README.md | Rewrite: fun, SEO, pragmatic, educational, comparative | active | 731 lines, needs restructuring |
| docs/README.md | Update hub to match new README structure | active | 49 lines, minimal |
| docs/architecture/overview.md | Fix outdated Djinni reference (line 42) | active | Still says "Android bridge | Djinni" |
| docs/guides/legacy-djinni.md | Archive or delete (Djinni deprecated) | active | 48 lines, explicitly archived |
| docs/architecture/*.html | Delete HTML files (should be SVG/MD) | active | risk-analysis.html, zig-surgical-integration-risk-analysis.html |
| misc/ROADMAP.md | Already created, add to README See also | active | v0.4.0 roadmap |
| Dead TOC link | Fix #road-to-mvp → misc/ROADMAP.md | active | README.md line 56 |

## Open assumptions (announced defaults)

| assumption | adopted default | rationale | reversible? |
|------------|-----------------|-----------|-------------|
| Keep Djinni legacy doc as archive | Yes, but move to misc/ or mark archival | Historical reference value | Yes |
| HTML files in docs/ are obsolete | Delete them | SVG/Md preferred, HTML not linked | Yes |
| Build dirs (app/build, misc/*/build) | Do NOT delete in this plan | .gitignore should handle; user may need them | Yes |
| README length target | ~400-500 lines (down from 731) | Balance detail with scannability | Yes |

## Findings (cited - path:lines)

### README.md issues
- Line 56: Dead link `[Road to MVP](#road-to-mvp)` — section removed, needs → `misc/ROADMAP.md`
- Line 715: No reference to new ROADMAP.md in See also table
- 731 lines total — dense, needs restructuring for engagement
- Missing: fun tone, clear value proposition hook, scannable sections

### Outdated docs
- `docs/architecture/overview.md:42` — Still lists "Android bridge | Djinni" (should be "Zig JNI")
- `docs/guides/legacy-djinni.md` — 48 lines of deprecated content, archived but still linked
- `docs/architecture/risk-analysis.html` — HTML file, not linked from anywhere
- `docs/architecture/zig-surgical-integration-risk-analysis.html` — HTML file, not linked from anywhere
- Multiple docs reference CMake as primary (should note Zig as default now)

### Repo clutter
- `docs/architecture/*.html` — 2 HTML files that should be removed
- Build directories exist but are .gitignore'd (leave alone)
- `template/android-app/djinni-generated/` — Check if still exists (shouldn't)

## Decisions (with rationale)

1. **README structure**: Hero → Quick Start → "What makes Nexus different" → Architecture (condensed) → Building your app → Compare → Docs → License
2. **Tone**: Confident, slightly irreverent ("not another Electron app"), technical but not dry
3. **SEO**: Keep technology names (Kotlin, C++20, Zig, SDL3, ImGui, Lua, Python), comparison keywords
4. **Legacy Djinni**: Move to `docs/archives/legacy-djinni.md` or delete entirely
5. **HTML files**: Delete both `.html` files from docs/architecture/

## Scope IN

- Rewrite README.md (fun, SEO, pragmatic, educational, comparative)
- Update docs/README.md hub
- Fix docs/architecture/overview.md Djinni → Zig JNI
- Delete docs/architecture/*.html (2 files)
- Handle legacy-djinni.md (archive or delete)
- Fix dead TOC link and add ROADMAP.md reference
- Clean any other obsolete references found during execution

## Scope OUT (Must NOT have)

- No code changes (no Kotlin, C++, Zig, Python edits)
- No build directory cleanup (.gitignore handles this)
- No diagram regeneration (14 SVGs work fine)
- No mockup_studio.py fixes (separate plan)
- No changes to template/ source files

## Open questions

None — defaults adopted for all forks.

## Approval gate
status: awaiting-approval
