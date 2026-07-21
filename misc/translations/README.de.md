<!--
  description: Nexus Framework 1.0.2 — native C++/Lua/Python Apps. Home = Dashboard. Langflow → flows.json (nicht Blueprint). Lizenz Nexus-1.0.
-->
# Nexus Framework — Nativer App-Generator

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Zeichne deine App als Graph. Erhalte eine native Binärdatei. Kein Browser. Kein Electron. Keine Cloud.</strong></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Version 1.0.2" /></a>
</p>

<p align="center"><em>Die englische README ist maßgeblich.</em></p>

## Schnellstart

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./misc/build_client.sh          # Nexus-Lizenz einmal akzeptieren; CI: --accept-license
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MeineApp"
# Optional: Langflow-Export → flows.json-Stubs (nicht blueprint.json)
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MeineApp/flows/flows.json"
cd builds/framework/MeineApp && ./build_app.sh
```

## Layout (v1.0.2)

Gradle-Module im Repo-Root: `:core`, `:cli`, `:app`. Eine zentrale `build.gradle.kts`. Tooling unter `misc/`.

**Home** ist das Home-Dashboard. Blueprint-/Flows-Editoren (Skeletons).

## Langflow → flows

Optionaler Import von Langflow-kompatiblem Export-JSON zu `flows.json`-Stubs via `import-langflow`. **Kein** `blueprint.json`. Kompatibles Format; Nexus ist **nicht affiliert** mit Langflow.

## Lizenz (Nexus-1.0)

- **Nicht-kommerziell** (Toolkit und generierte Apps: privat / Hobby / nicht-kommerzielle Institution): erlaubt **mit Namensnennung**.
- Bis **2041-07-21** braucht es vorherige Genehmigung von [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) für: **kommerzielles Toolkit**; Apps mit **Umsatz**; Einsatz in einer **kommerziellen Institution**.
- Nach 2041-07-21 entfallen diese Genehmigungspflichten (sofern nicht erneuert); die Namensnennung bleibt.
- **Keine Gewährleistung.** Der Autor haftet nicht für rechtswidrige Nutzung abgeleiteter Apps.

Siehe [LICENSE](../../LICENSE) · [englische README](../../README.md).

## Dokumentation

| Dokument | Inhalt |
|:---------|:-------|
| [docs/hub.md](../../docs/hub.md) | Hub |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | Architektur |
| [docs/assets/diagrams/activity-generate-pipeline.svg](../../docs/assets/diagrams/activity-generate-pipeline.svg) | Generierungsdiagramm |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | Leitfaden |

*Zeichne den Graphen, generiere den Baum, liefere die Binärdatei.*
