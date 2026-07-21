# misc/scripts/

Repo automation — all scripts live flat in this directory or under `test-gen/` for platform-specific test runners.

## Layout

| Path | Purpose |
|------|---------|
| [nexus-dev.sh](nexus-dev.sh) | Local dev workflow — compile, test, generate, run client, Docker |
| [generate-in-docker.sh](generate-in-docker.sh) | Run generation inside a Docker container |
| [generate-diagrams.py](generate-diagrams.py) | Regenerate docs SVG architecture, UML activity, and Compose UI mockups |
| [test-gen/](test-gen/) | Smoke test generation for built apps under `builds/framework/` |

**Client first-run setup** stays at [../client-setup/](../client-setup/).

## Quick reference

```bash
# Dev workflow
./misc/scripts/nexus-dev.sh compile
./misc/scripts/generate-in-docker.sh desktop MyApp builds/framework/MyApp

# Test generation (dry-run against fixture)
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture

# Regenerate all SVGs (architecture + activity + mockups)
python3 misc/scripts/generate-diagrams.py

# UI mockups only → docs/assets/examples/mockup-*.svg
# (skips architecture / activity diagrams — safer when another agent owns those)
python3 misc/scripts/generate-diagrams.py --mockups

# Architecture + activity diagrams only (skip mockups)
python3 misc/scripts/generate-diagrams.py --diagrams
```

### Compose UI mockups

Home hub is **Home** (`HomeScreen.kt`). `AppScreen.Welcome` / `AppScreen.Dashboard` are aliases of Home — there is **no** separate competing dashboard mockup.

| Screen | Description | SVG | Compose |
|--------|-------------|-----|---------|
| Home (hub) | Main dashboard — branding, generated apps, create / open / Langflow / analyze | [mockup-welcome.svg](../../docs/assets/examples/mockup-welcome.svg) | `HomeScreen.kt` |
| Loading | Splash + init progress steps | [mockup-loading.svg](../../docs/assets/examples/mockup-loading.svg) | `LoadingScreen.kt` |
| Generate | Project name, app type, output path | [mockup-generate-project.svg](../../docs/assets/examples/mockup-generate-project.svg) | `GenerateProjectView.kt` |
| Blueprint Editor | Palette / canvas / inspector node graph | [mockup-blueprint-editor.svg](../../docs/assets/examples/mockup-blueprint-editor.svg) | `BlueprintEditorView.kt` |
| Flows Editor | Runtime automation toggles | [mockup-flows-editor.svg](../../docs/assets/examples/mockup-flows-editor.svg) | `FlowsEditorView.kt` |
| Debugger | Log paste, scan, severity matches | [mockup-debugger-v102.svg](../../docs/assets/examples/mockup-debugger-v102.svg) | `DebuggerPanel.kt` |
| Test Runner | In-memory unitary pass/fail list | [mockup-test-runner.svg](../../docs/assets/examples/mockup-test-runner.svg) | `TestRunnerPanel.kt` |
| What's New | Version modal overlay (optional) | [mockup-whats-new.svg](../../docs/assets/examples/mockup-whats-new.svg) | `WhatsNewDialog.kt` |

Notes:

- Palette tokens match `NexusTheme` / `NexusBranding` (dark bg `#1A1A2E`, flamingo, brand purple, cyan).
- `mockup-debugger-v102.svg` is used instead of legacy root-owned `mockup-debugger.svg` (PermissionError on overwrite).
- Deprecated `DashboardView.kt` / `WelcomeScreen.kt` wrap `HomeScreen`; prefer `AppScreen.Home`.

See [../README.md](../README.md) and [../../AGENTS.md](../../AGENTS.md) for full repo context.
