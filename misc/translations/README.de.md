<!--
  description: Das Nexus Framework generiert native Desktop- und Android-Apps in C++/Lua/Python aus visuellen Blueprints. Kein Electron, keine Cloud, kein Browser — nur eine kompilierte Binärdatei, die du kontrollierst. SDL3 + ImGui + Zig 0.14.
  keywords: nativer App-Generator, C++ Framework, C++20 Module, Lua Scripting, Python eingebettet, SDL3, ImGui, Zig Build, Zig JNI, Compose Desktop, Blueprint-getriebene Entwicklung, plattformübergreifender Desktop, Android Native App, kein Electron, Projektgenerator, Graph-basierte Architektur, Arena-Allokator, pybind11, sol2, Chaquopy
-->
# Nexus Framework — Nativer App-Generator: C++ + Lua + Python aus visuellen Blueprints

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>🧩 Zeichne deine App als Graph. Erhalte eine kompilierte native Binärdatei. Kein Browser. Kein Electron. Keine Cloud.</strong></p>

<p align="center"><em>Einfach, wenn du willst. Leistungsstark, wenn du es brauchst.</em></p>

---

## Schnellstart

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MeineApp"
cd builds/framework/MeineApp && zig build
```

## Dokumentation

| Dokument | Beschreibung |
|---------:|:-------------|
| [docs/hub.md](../docs/hub.md) | Dokumentations-Hub |
| [docs/architecture/overview.md](../docs/architecture/overview.md) | Architektur, Sprachen, Pipeline, Templates |
| [docs/guides/coding-with-nexus.md](../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, Themes |
| [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md) | Referenz für `blueprint.json` + `flows.json` |

## Lizenz

Apache 2.0 — siehe [LICENSE](../LICENSE).

*Zeichne deine App als Graph, generiere den Baum, liefere die Binärdatei aus — dann iteriere in echten Code-Ebenen.*
