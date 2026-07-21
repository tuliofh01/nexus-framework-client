# Nexus Framework — Documentation Hub

Start here when you need to know **what to read** and **when**. English docs are authoritative; translations under `misc/translations/` are landing pages.

---

## Core reading

### [Project README](../README.md)
High-level pitch, **v1.0.2 repository layout** (`:core` / `:cli` / `:app` at root, single `build.gradle.kts`), Compose client screens, quick start, and the **Nexus License** summary. Read first if you are new to the repo.

### [Nexus License](../LICENSE)
Full legal text (**Nexus License** / `Nexus-1.0`). Non-commercial use OK with attribution. Through **2041-07-21**, owner authorization is required for Toolkit commercial use, **revenue-producing** derived apps, and derived apps used in a **commercial institution**. After that date those authorization restrictions expire unless renewed; attribution continues. No warranty; no liability for misuse.

### [AGENTS.md](../AGENTS.md)
Condensed build/run commands and repo map for AI coding assistants (and humans who want the short version of “how to compile this”).

### [misc/README.md](../misc/README.md)
What lives under `misc/` (build-logic, client-setup, scripts, Docker, Jenkins, translations) — **not** the Kotlin sources (those are at repo root).

---

## Architecture & diagrams

### [architecture/overview.md](architecture/overview.md)
Full-stack narrative: authoring client → generation pipeline → native runtime, language roles, templates, and risk notes. Read when you need the big picture beyond the README.

### [assets/diagrams/activity-diagrams.md](assets/diagrams/activity-diagrams.md)
**UML activity diagrams** (SVG) for:
- Framework: bootstrap, Compose navigation, `ProjectGenerator`, `build_app.sh`
- Derived apps: SDL frame loop, counter starter, flows automation, **Langflow → `flows.json` import** (not blueprint), Android tablet  

Use these when explaining or implementing control flow. Canonical location: **`docs/assets/diagrams/`** (alongside the structural SVGs). Regenerate with `python3 misc/scripts/generate-diagrams.py`.

### [assets/diagrams/](assets/diagrams/)
Static **SVG** structure diagrams (full-stack, generation flow, desktop vs Android, Langflow comparisons, etc.) plus the UML **activity** SVGs indexed above.

### Compose UI mockups — [assets/examples/](assets/examples/)
Dark-theme SVG wireframes for the Desktop client. **Home is the main dashboard** (`mockup-welcome.svg`); other screens have their own mockups. Full table and regenerate commands: [misc/scripts/README.md](../misc/scripts/README.md#compose-ui-mockups).

```bash
python3 misc/scripts/generate-diagrams.py --mockups
```

---

## Guides & schemas

### [guides/coding-with-nexus.md](guides/coding-with-nexus.md)
How to work inside a **generated** app: MVC layout, Lua/Python/TS-XHTML, themes, adding dependencies. Read after you have run `generate` once.

### [templates/blueprint-schema.md](templates/blueprint-schema.md)
Reference for `blueprint.json` and `flows.json` — nodes, edges, validation, runtime automations. Read when authoring or debugging graphs.

### [../template/README.md](../template/README.md)
Index of `desktop-app` and `android-app` scaffolds the generator copies.

### [../builds/LAYOUT.md](../builds/LAYOUT.md)
Where generated apps (`builds/framework/`) and packaged client binaries (`builds/client/`) land.

---

## Setup & tooling

### [../misc/client-setup/README.md](../misc/client-setup/README.md)
First-run bootstrap: JDK **26** + Zig **0.16.0** via `setup.zig` / `env.sh`. Required before the first `./gradlew :app:run`.

### [../misc/build_client.sh](../misc/build_client.sh)
One-shot script to compile `:core`, `:cli`, and `:app`. Shows a **Nexus License** accept dialog (zenity/kdialog/yad or TTY) before Gradle; stamp in `misc/.license-accepted`. Use `--accept-license` for CI and `--show-license` to re-display.

### Translations — [../misc/translations/README.md](../misc/translations/README.md)
Localized landing pages (pt-BR, es, de, ru, zh-CN). Keep license and layout wording aligned with English.

---

## Quick commands

```bash
source misc/client-setup/env.sh
./misc/build_client.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MyApp/flows/flows.json"
```

---

## License (one-liner)

**[Nexus License](../LICENSE)** (`Nexus-1.0`): non-commercial OK with attribution; through **2041-07-21** ask [@tuliofh01](https://github.com/tuliofh01) for Toolkit commercial use, revenue-producing apps, or commercial-institution deployment.
