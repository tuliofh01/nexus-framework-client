# IntelliJ IDEA kit for Nexus Framework

Shareable IntelliJ configuration that gives this Kotlin / Compose Desktop repo a **Python-like editor UX**: strong on-save cleanup, clear linting, hover docs, snippets, and rich project wisdom for humans and AI assistants (Junie / AI Assistant).

## Quick start

1. Open this repository root in **IntelliJ IDEA** (Community or Ultimate) or **Android Studio is not required** — this is a Compose **Desktop** JVM project, not an Android app IDE target.
2. Install recommended plugins (IDEA → Settings → Plugins):
   - **Kotlin** (bundled)
   - **Compose Multiplatform IDE Support** (JetBrains)
   - **Ktlint** (`com.nbadal.ktlint`) — already referenced by this kit
   - Optional: **Rainbow Brackets**, **String Manipulation**
3. Apply this kit:

```bash
./intellij/apply-to-idea.sh
```

4. Restart IntelliJ (or File → Invalidate Caches if inspections look stale).
5. Confirm **Settings → Tools → Actions on Save**:
   - Reformat code
   - Optimize imports
   - Rearrange code
   - Run code cleanup
   - (Ktlint) Format / lint if the plugin shows its own save actions

## What you get

| Goal | How this kit delivers it |
|------|--------------------------|
| Auto-indent | `.editorconfig` + Kotlin official code style under `codeStyles/` |
| Syntax highlighting | Kotlin + Compose plugins; dictionary for domain terms |
| Code snippets | Live templates in `templates/Nexus.xml` |
| Hover definitions | Quick Documentation + Parameter Info enabled in `options/` |
| Lint on save | Inspection profile + Ktlint DISTRACT_FREE + cleanup on save |
| Organize imports on save | Actions on Save + Kotlin import layout |
| Drop unused imports/vars | Inspections set to ERROR/WARNING + code cleanup |
| Better suggestions | Completion settings + wisdom docs for AI context |
| Context wisdom | `AGENTS.md`, `wisdom/*.md` |

## Layout

```
intellij/
├── README.md                 ← you are here
├── AGENTS.md                 ← AI / Junie project brain
├── apply-to-idea.sh          ← copies kit → .idea/
├── codeStyles/               ← Kotlin/Compose formatting
├── inspectionProfiles/       ← unused import/var, Compose tips
├── options/                  ← editor + actions-on-save + completion
├── templates/                ← live templates / snippets
├── dictionaries/             ← spellcheck domain words
├── runConfigurations/        ← :app run, :cli generate, tests
└── wisdom/                   ← deep architecture & coding tips
```

## Manual apply (if you skip the script)

Copy contents into `.idea/`:

- `codeStyles/` → `.idea/codeStyles/`
- `inspectionProfiles/` → `.idea/inspectionProfiles/`
- `runConfigurations/` → `.idea/runConfigurations/`
- `dictionaries/` → `.idea/dictionaries/`
- `templates/Nexus.xml` → live templates via Settings → Editor → Live Templates → Import, or place under IDE config
- `options/*.xml` → merge into `.idea/` workspace options (script handles known files)
- Keep `ktlint-plugin.xml` from this kit

## Not Android Studio

If IntelliJ ever offers an **Android** project system for this root, decline it. Generated Android apps live under `template/android-app/` and `builds/framework/`. The Framework client itself is **Compose Multiplatform Desktop** (`:app`).
