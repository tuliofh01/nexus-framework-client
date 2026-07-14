# Plan: README Refactor + Docs Audit + Repo Cleanse

## TL;DR (For humans)

Rewrite the README to be **fun, SEO-optimized, pragmatic, educational, and comparative** — matching the project's complexity. Clean up outdated docs (Djinni references, stale HTML files), fix broken links, and remove obsolete content. The repo gets a professional, engaging face that respects readers' time while showcasing what makes Nexus unique.

**Estimated effort**: Medium (single session)
**Files touched**: ~10-12 files

---

## What's happening and why

The README is 731 lines of dense technical content that buries the lede. It reads like documentation, not a pitch. Meanwhile, several docs reference deprecated Djinni codegen, and there are orphaned HTML files in `docs/architecture/`. This plan gives Nexus the polished, engaging entrance it deserves — then cleans the attic.

---

## Architecture / Integration Notes

- **No code changes** — documentation and markdown only
- **Diagram SVGs stay** — all 14 are current and working
- **Template source untouched** — only docs/README files
- **Build directories ignored** — .gitignore handles `*/build/`

---

## Execution Strategy

### Wave 1: Quick Wins + Deletions (parallel)

| Task | Description | Depends On |
|------|-------------|------------|
| **T1** | Delete `docs/architecture/risk-analysis.html` (orphaned, not linked) | None |
| **T2** | Delete `docs/architecture/zig-surgical-integration-risk-analysis.html` (orphaned) | None |
| **T3** | Fix README.md line 56: `[Road to MVP](#road-to-mvp)` → `[Roadmap](misc/ROADMAP.md)` | None |
| **T4** | Add ROADMAP.md row to README.md See also > Documentation table (after line 715) | None |

**Acceptance (T1-T4)**:
```bash
# T1-T2: Verify HTML files deleted
! test -f docs/architecture/risk-analysis.html && ! test -f docs/architecture/zig-surgical-integration-risk-analysis.html

# T3: Verify dead link removed
! grep -q "#road-to-mvp" README.md

# T4: Verify ROADMAP referenced
grep "misc/ROADMAP.md" README.md
```

---

### Wave 2: Docs Audit (parallel)

| Task | Description | Depends On |
|------|-------------|------------|
| **T5** | Fix `docs/architecture/overview.md` line 42: Change "Djinni" → "Zig JNI" in Android bridge row | None |
| **T6** | Audit `docs/guides/legacy-djinni.md` — Keep as archival, but move to `docs/archives/legacy-djinni.md` and update internal references | None |
| **T7** | Scan all docs for active Djinni references that should say "Zig JNI" or "deprecated" | T6 |
| **T8** | Scan docs for CMake-as-primary references — add notes where Zig is now default | T7 |

**Acceptance (T5-T8)**:
```bash
# T5: Verify overview.md updated
grep "Zig JNI" docs/architecture/overview.md

# T6: Verify legacy doc moved
test -f docs/archives/legacy-djinni.md

# T7-T8: Spot check
grep -c "Djinni" docs/architecture/overview.md  # Should be 0 or marked deprecated
grep "Zig" docs/guides/generation-pipeline.md | head -3  # Should mention Zig as default
```

---

### Wave 3: README Rewrite (sequential)

| Task | Description | Depends On |
|------|-------------|------------|
| **T9** | Rewrite README.md with new structure (see below) | T3, T4 complete |

**New README structure (~400-500 lines target)**:

```
# The Nexus Framework — [PUNCHY TAGLINE]

<p align="center">logo + badges</p>

> [!TIP]  
> **One-liner value prop** + fastest path to running

## What is Nexus? (2 paragraphs max)
## Quick Start (5 steps, code blocks)
## What Makes Nexus Different
  - vs Electron/Tauri (comparison table)
  - vs n8n/Power Automate (comparison table)  
  - vs Langflow (comparison table)
  - vs raw C++ (comparison table)
## Architecture (condensed, 2-3 diagrams)
## Blueprint & Flows (two layers)
## Building Your App
## The misc/ Folder
## Zig Patching (gains table, condensed)
## Docs & Resources
## License
```

**Tone guidelines**:
- Confident, slightly irreverent ("not another Electron shell")
- Technical but scannable (headers, tables, code blocks)
- Respect the reader's time — front-load value
- Keep SEO keywords: Kotlin, C++20, Zig, SDL3, ImGui, Lua, Python, blueprint, native

**Acceptance (T9)**:
```bash
# Verify README under 550 lines
wc -l README.md | awk '{print $1}' | xargs test 550 -ge

# Verify key sections exist
grep -q "Quick Start" README.md
grep -q "What Makes Nexus Different" README.md
grep -q "Blueprint" README.md

# Verify no dead internal links (spot check)
! grep -q "#road-to-mvp" README.md
```

---

### Wave 4: Docs Hub Update + Final Polish

| Task | Description | Depends On |
|------|-------------|------------|
| **T10** | Update `docs/README.md` to match new README structure and links | T9 |
| **T11** | Add `docs/archives/` directory if T6 created it, add README explaining archival | T6 |
| **T12** | Final grep for stale references across all docs | T8, T9 |

**Acceptance (T10-T12)**:
```bash
# T10: Verify docs hub updated
grep "architecture" docs/README.md | head -3

# T12: Final audit
grep -r "Djinni" docs/ --include="*.md" | grep -v "legacy\|archiv\|deprecated" | wc -l  # Should be 0
```

---

## Commit Strategy

Single commit with descriptive message:
```
docs: refactor README for engagement + clean obsolete files

- Rewrite README: fun, SEO-optimized, pragmatic, educational, comparative
- Fix dead TOC link (#road-to-mvp → misc/ROADMAP.md)
- Delete orphaned HTML files from docs/architecture/
- Update docs/architecture/overview.md: Djinni → Zig JNI
- Move legacy-djinni.md to docs/archives/
- Scan and update stale Djinni/CMake references across docs
- Update docs/README.md hub

README reduced from 731 → ~450 lines while preserving all technical content.
```

---

## Success Criteria

- [ ] README is fun, engaging, and under 550 lines
- [ ] All internal links work (no dead anchors)
- [ ] No active Djinni references remain (only archival mentions)
- [ ] Zig recognized as default build system in docs
- [ ] HTML files removed from docs/architecture/
- [ ] ROADMAP.md referenced in README See also
- [ ] docs/README.md hub matches new structure
- [ ] `git status` shows only docs/*.md changes (no code)

---

## Questions for you

**None** — all forks resolved with defaults. Ready to execute when you approve.

---

## File Changes Summary

| File | Action |
|------|--------|
| README.md | Major rewrite (731 → ~450 lines) |
| misc/ROADMAP.md | Already created, add reference |
| docs/README.md | Update hub structure |
| docs/architecture/overview.md | Fix Djinni → Zig JNI (line 42) |
| docs/architecture/risk-analysis.html | DELETE |
| docs/architecture/zig-surgical-integration-risk-analysis.html | DELETE |
| docs/guides/legacy-djinni.md | MOVE to docs/archives/ |
| docs/archives/legacy-djinni.md | NEW (moved from above) |
| docs/archives/README.md | NEW (explains archival) |

---

## Approval

**Status**: `awaiting-approval`

Reply with:
- **`approved`** — I execute this plan
- **`scope change: [details]`** — adjust scope
- **`question: [question]`** — ask before proceeding
