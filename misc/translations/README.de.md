# Das Nexus Company Framework für die Entwicklung nativer Anwendungen

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — Logo des nativen C++/Lua/Python-Projektgenerators" width="240" />
</p>

<p align="center"><strong>🧩 Native Apps, keine Browser-Tabs</strong> — SDL3-Binaries aus einem Blueprint-Graphen ausliefern.</p>

<p align="center">
  🌐 <strong>Übersetzungen:</strong>
  <a href="../../README.md">English</a> ·
  <a href="README.pt-BR.md">Português</a> ·
  <a href="README.es.md">Español</a> ·
  <a href="README.de.md">Deutsch</a> ·
  <a href="README.ru.md">Русский</a> ·
  <a href="README.zh-CN.md">简体中文</a>
</p>

<p align="center">
  <a href="../../README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
  <a href="README.es.md"><img src="https://img.shields.io/badge/lang-Espa%C3%B1ol-red?style=for-the-badge" alt="Español" /></a>
  <a href="README.de.md"><img src="https://img.shields.io/badge/lang-Deutsch-yellow?style=for-the-badge" alt="Deutsch" /></a>
  <a href="README.ru.md"><img src="https://img.shields.io/badge/lang-%D0%A0%D1%83%D1%81%D1%81%D0%BA%D0%B8%D0%B9-lightgrey?style=for-the-badge" alt="Русский" /></a>
  <a href="README.zh-CN.md"><img src="https://img.shields.io/badge/lang-%E7%AE%80%E4%BD%93%E4%B8%AD%E6%96%87-orange?style=for-the-badge" alt="简体中文" /></a>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache 2.0 Open-Source-Lizenz" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 Compose Desktop Projektgenerator" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-plattform%C3%BCbergreifend-green?style=flat-square" alt="SDL3 Desktop und Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui Immediate-Mode-UI" /></a>
</p>

> [!TIP]
> **Willkommen.** Führe das [Erst-Setup](#schnellstart) aus, dann `./gradlew :app:run` — in Minuten hast du den Compose-Client, den Blueprint-Editor und den Pfad zu `builds/framework/<name>/`. Kein Chromium-Download nötig.

## Inhaltsverzeichnis

- [Was ist Nexus?](#was-ist-nexus)
- [Wie sich Nexus vergleicht](#wie-sich-nexus-vergleicht)
- [Was in diesem Repository liegt](#was-in-diesem-repository-liegt)
- [Schnellstart](#schnellstart)
- [Architektur](#architektur)
- [Blueprint & Flows — zwei Ebenen](#blueprint--flows--zwei-ebenen)
- [Deine App bauen](#deine-app-bauen)
- [Der `misc/`-Ordner](#der-misc-ordner)
- [Abhängigkeiten hinzufügen](#abhängigkeiten-hinzufügen)
- [Modernes C++ & wachsen ohne Rewrite](#modernes-c--wachsen-ohne-rewrite)
- [Zig patching (native Builds)](#zig-patching-native-builds)
- [Jenseits schneller Automatisierung](#jenseits-schneller-automatisierung)
- [Entwicklungsstatus](#entwicklungsstatus)
- [Copyright und Lizenz](#copyright-und-lizenz)
- [Siehe auch](#siehe-auch)
- [Weg zum MVP](#weg-zum-mvp)

---

## Was ist Nexus?

**The Nexus Framework** ist ein **Open-Source-Builder für native Apps**. Du beschreibst deine App als visuellen Graphen — [`blueprint.json`](../../docs/templates/blueprint-schema.md) für die Struktur, optionales [`flows.json`](../../docs/templates/flows-schema.md) für In-App-Automatisierungen — und Nexus generiert eine echte Anwendung in **C++**, **Lua** und **Python** für **Desktop** (Windows, macOS, Linux) und **Android**. Der Kotlin-Compose-Client (`:app`) erstellt diese Graphen; [`misc/core`](#der-misc-ordner) validiert sie und schreibt Projekte aus [`template/`](#deine-app-bauen) mit SDL3-Fenstern, Dear-ImGui-Widgets, sol2-Scripting, TypeScript- + XHTML-UI-Autoring und eingebettetem Python (pybind11 auf Desktop, Chaquopy + Djinni auf Android).

Das ist **kein** Web-Shell und kein gehosteter Flow-Runtime. Nexus liefert kompilierte Binaries — SDL3 + ImGui + ImPlot — mit In-Process-Lua und Python. Du iterierst in normalen Code-Schichten (`cpp.model`, `python.module`, `ui.page`, Lua-Panels) nach der Generierung. Unterschiede zu Electron, n8n, Langflow oder Greenfield siehe [Wie sich Nexus vergleicht](#wie-sich-nexus-vergleicht).

---

## Wie sich Nexus vergleicht

Nexus übernimmt das **Knoten-Kanten-Denkmuster** visueller Flow-Tools, aber die Ausgabe ist ein **nativer Programm** — kein Chromium-Tab, kein Cloud-Workflow-Host und kein eingebetteter Langflow-Server in deiner App.

### vs Electron & Tauri

| Tool | Stärke | Nexus-Unterschied |
|------|--------|-------------------|
| [Electron](https://www.electronjs.org/) | Web-first Desktop-Apps; DOM/CSS/React als Produktoberfläche | Nativer C++-Runtime, ~3–20 MB Binaries, kein Renderer-Subprozess |
| [Tauri](https://tauri.app/) | Leichtes Web-UI im OS-WebView + Rust-Backend | ImGui Immediate-Mode-UI, SDL3-GPU-Oberflächen, gemeinsamer Desktop- + Android-Stack |
| **Nexus** | Datenintensive, feldbasierte, throughput-sensitive Tools | Blueprint-gesteuertes Codegen; In-Process-Python/numpy; Game-Engine-UX |

**Wann Web-Shells gewinnen:** Team ist HTML/CSS-first oder braucht iOS aus Web-Toolchain heute. **Wann Nexus gewinnt:** Sub-ms-UI-Refresh, kleine Binaries, SDL3-Parität vom Trading-Desk bis Android-Feldtablet — siehe [Deine App bauen](#deine-app-bauen).

### vs n8n & Power Automate

| Tool | Stärke | Nexus-Unterschied |
|------|--------|-------------------|
| [n8n](https://n8n.io/) | Ops-Kleber — Webhooks, Cron, SaaS-Integrationen | Generiert eine **ausgelieferte App** mit nativer UI, Offline-Verhalten und In-Process-State |
| [Power Automate](https://www.microsoft.com/power-automate) | Geschäftsprozess-Automatisierung in der Microsoft-Cloud | Gleiche Graph-UX für **internes MVC-Wiring**, keine externen Step-Engines |
| **Nexus** | Wenn der Quick-Fix-Flow *das Produkt ist* | `blueprint.json` = Build-Time-Struktur; optionales `flows.json` = lokale In-Process-Services |

> [!WARNING]
> **Nexus ist nicht n8n oder Power Automate.** Nutze diese für Cloud-SaaS-Wiring. Eine generierte App kann weiterhin n8n-Webhooks aus Python/Lua am Rand aufrufen.

### vs Langflow

| Tool | Stärke | Nexus-Unterschied |
|------|--------|-------------------|
| [Langflow](https://github.com/langflow-ai/langflow) | Visuelles Authoring von LLM/AI-Runtime-Flows | **Import/Adopt** exportiertes JSON in `blueprint.json` und `flows.json` — kein gebündelter Langflow-Runtime in v1 |
| **Nexus `blueprint.json`** | — | Build-Time-MVC-Graph (`python.module`, `cpp.model`, `ui.page`, …) einmal von `ProjectGenerator` konsumiert |
| **Nexus `flows.json`** | — | Optionale In-App-Automatisierungen (Timer, Events, Background-Loops) von FlowRunner beim Start geladen |

Strukturgraphen → [`blueprint.json`](#app-struktur-blueprintjson); Automatisierung → [`flows.json`](#in-app-automatisierungen-flowsjson). Vollständiger Import-Workflow: [Langflow-Prozeduren importieren](#langflow-prozeduren-importieren).

<!-- Diagramm: Langflow vs n8n vs Nexus Blueprint -->
![📊 Langflow vs n8n vs Nexus Blueprint — verbundene Schritte vs Workflow-Automatisierung vs Build-Time-Codegen](../../docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

*Langflow-Runtime-Flows vs n8n-Ops-Automatisierung vs Nexus Build-Time-Codegen — gleiches visuelles Muster, anderes Ausführungsmodell.*

### vs rohes C++ / von Grund auf

| Ansatz | Stärke | Nexus-Unterschied |
|--------|--------|-------------------|
| Handgemachtes C++/CMake | Volle Kontrolle; bestehende Vendor-SDKs und Legacy-Cores | Generatoren + Blueprint-Graph; TS/XHTML- und Lua-Schichten ohne Neustart von null |
| Greenfield-Rewrite (Rust, Go, …) | Compile-Time-Safety oder neues Ökosystem | **Inkrementell wachsen** — performance-kritisches C++ behalten, Blueprint-Knoten und Flows neben altem Code |
| **Nexus** | Teams zwischen Web-Shell-Overhead und vollem Rewrite | Dritter Weg: Authoring schrittweise modernisieren, profilen vor Sprachmigration |

---

## Was in diesem Repository liegt

| Pfad | Rolle |
|------|-------|
| [`app/`](../../app/) | Compose Desktop Client (`:app`) — Generate Project, Blueprint/Flows-Editoren |
| [`misc/`](../../misc/) | `:core`-Generator, `:cli`, client-setup, Scripts, Docker — siehe [Der `misc/`-Ordner](#der-misc-ordner) |
| [`template/`](../../template/) | desktop-app · android-app · shared — kopiert nach `builds/framework/<name>/` |
| [`builds/`](../../builds/) | Client-Artefakte → `builds/client/` · generierte Apps → `builds/framework/` |
| [`docs/`](../../docs/) | Dokumentations-Hub → [docs/README.md](../../docs/README.md) |

Dies ist das **Framework**-Monorepo (`:app`, `:core`, `:cli`). Nicht das separate [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client)-Repo (`:client-desktop`-Wizard dort).

---

## Schnellstart

**1. Erst-Setup** — JDK 26 + Git installieren (einmal pro Maschine):

| Plattform | Setup | Env |
|-----------|-------|-----|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Details: [misc/client-setup/README.md](../client-setup/README.md).

**2. Client starten**

```bash
source misc/client-setup/env.sh
./gradlew :app:run
```

**3. Projekt generieren**

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Oder **Generate Project** in der Compose-UI → **Edit blueprint** / **Edit flows**.

**4. Generierte App bauen**

```bash
cd template/desktop-app && cmake --preset debug && cmake --build --preset debug
# Ausgabe landet auch in builds/framework/<name>/ nach Generierung
```

**5. Docs lesen** — [docs/README.md](../../docs/README.md) · [coding-with-nexus](../../docs/guides/coding-with-nexus.md) · [generation-pipeline](../../docs/guides/generation-pipeline.md)

Generator kompilieren und testen: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Client deployen: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](../../builds/client/app/)

---

## Architektur

### Full-Stack-Architektur
*Compose-Client → `:core`-Generierungsflow → SDL3-Runtimes (deine App, kein Browser-Tab)*

![Nexus Full-Stack-Architektur](../../docs/assets/diagrams/full-stack-architecture.svg)

Der `:app`-Client erstellt Blueprint- und Flow-JSON; `:core` validiert und materialisiert Templates in `builds/framework/<name>/`. Generierte Apps laufen als native SDL3-Binaries mit ImGui, Lua und optionalem Python.

### Generierungs- und Build-Flow
*Von client-setup und Gradle-Modulen bis `builds/framework/<name>/`*

![Generierungs- und Build-Flow](../../docs/assets/diagrams/generation-builds-flow.svg)

JDK-26-Setup → Gradle `:core` / `:cli` / `:app` → `ProjectGenerator` schreibt CMake/Gradle-Bäume unter `builds/`.

### Desktop- vs Android-Runtime
*Gemeinsames MVC auf SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

![Desktop vs Android Runtime](../../docs/assets/diagrams/desktop-vs-android-runtime.svg)

Ein `blueprint.json` verdrahtet MVC auf beiden Templates; nur Python-Bridge und Packaging unterscheiden sich pro Plattform.

Layer-Referenz: [docs/architecture/overview.md](../../docs/architecture/overview.md) · Blueprint/Flows: [Blueprint & Flows](#blueprint--flows--zwei-ebenen) · Python: [Python auf Desktop vs Android](#python-auf-desktop-vs-android)

---

## Blueprint & Flows — zwei Ebenen

Nexus trennt **App-Struktur zur Build-Zeit** von **Automatisierungen, die in der App laufen**. Eine Langflow-Leinwand kann nach der Übersetzung auf beide Dateien aufgeteilt werden.

### Blueprint vs Flows — zwei Ebenen
*Build-Time-Struktur vs optionale In-App-Automatisierungen*

![blueprint.json vs flows.json — Zwei-Ebenen-Modell](../../docs/assets/diagrams/blueprint-vs-flows-layers.svg)

`blueprint.json` verdrahtet MVC-Struktur, einmal von `:core` konsumiert; `flows.json` registriert In-Process-Trigger, von FlowRunner beim Start geladen.

### App-Struktur (`blueprint.json`)

Build-Time-Graph im Projektroot. Knoten deklarieren Module; Kanten verdrahten Daten- und Befehlsfluss in der generierten MVC-App.

| Knotentyp | Rolle |
|-----------|-------|
| `python.module` | Python-Sampling / Analytics (`python/functions.py`) |
| `cpp.model` | C++-Domänenstatus (`FunctionRegistry`, Caches) |
| `cpp.controller` | Befehle + Wiring (`PlotController`) |
| `ui.page` | TS/XHTML-Seite (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Runtime-Lua-Panels (`scripts/panels.lua`) |

**Im Client bearbeiten:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (Compose-Canvas + JSON-Inspector in v1; natives **imnodes**-Panel geplant v1.1 — gleiches Schema).

Beispiele: [template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](../../template/android-app/blueprint.json) · Schema: [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)

#### Langflow-Beispiele

Referenzdiagramme für das visuelle Muster, das Nexus zur Build-Zeit spiegelt (nicht Runtime):

- [RAG-Chatbot-Flow](../../docs/assets/examples/langflow-rag-chatbot.svg) — Langflow-Runtime; Module auf Blueprint-Knotentypen mappen
- [Agent mit Tools](../../docs/assets/examples/langflow-agent-tools.svg) — Agent-Loop → `python.module`, `cpp.controller`, …
- [Nexus-Blueprint-App-Struktur](../../docs/assets/examples/nexus-blueprint-app-structure.svg) — Build-Time-MVC-Codegen

### In-App-Automatisierungen (`flows.json`)

Optionale Runtime-Services — Background-Loops, Event-Trigger, Zeitpläne.

| Modus | Wann ausgeführt | Trigger-Beispiel |
|-------|-----------------|------------------|
| `background` | Solange App lebt | `interval` alle 5000 ms |
| `triggered` | Nur bei Bedingung | `event` `curve.added`, `startup`, `manual` |

**Im Client bearbeiten:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — Flows listen, aktivieren/deaktivieren, JSON-Vorschau (visueller Editor v1.1). Schema: [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md).

Mehrere Flows im `flows`-Array anhängen (jeder mit eindeutiger `id`). Global deaktivieren via `nxs_config.json` → `"flows": { "enabled": false }` oder pro Flow mit `"enabled": false`.

Beispiel: [template/desktop-app/flows/flows.json](../../template/desktop-app/flows/flows.json)

### Langflow-Prozeduren importieren

[Langflow](https://github.com/langflow-ai/langflow) ist ein optionales **externes Authoring-Tool**. Exportiere Flow-JSON und adoptiere es als native Nexus-Services — **nicht** durch Langflow in der ausgelieferten App.

**Schritt 1 — Aus Langflow exportieren**

1. Visuellen Flow in Langflow bauen (LLM, Prompt, Tool, Retriever, Agent, …).
2. Als JSON via **Export flow** oder Langflow-API (`/api/v1/flows/{id}`) exportieren. Feldnamen und Verschachtelung **unterscheiden sich** von Nexus-Schemas; Export als Design-Artefakt behandeln, nicht als Drop-in-Datei.

**Schritt 2 — Auf Nexus mappen**

| Langflow-Konzept | Nexus-Ziel |
|------------------|------------|
| App-Struktur-Komponenten | [`blueprint.json`](#app-struktur-blueprintjson)-Knoten und MVC-Ports |
| Automatisierungs-Komponenten (LLM, Tool, Agent, …) | `flows.json` → `steps[]` mit `type: invoke` → `nxs.*`, `python.*`, `lua.*` |
| Kanten / Ausführungsreihenfolge | Geordnetes `steps`-Array; Verzweigungen via `condition` (v1.1) |
| Trigger (Chat, Webhook, Zeitplan) | `trigger.type`: `event`, `interval`, `startup`, `manual`, `hotkey` |
| Dauerloop | `mode: background` |
| On-Demand-Ausführung | `mode: triggered` |

**Schritt 3 — Im Projekt ausliefern**

![Langflow-Export zu flows.json Adoptions-Workflow](../../docs/assets/diagrams/langflow-adoption-workflow.svg)

1. **Übersetzen** Export in [Flows-Schema](../../docs/templates/flows-schema.md) (manuell v1; Importer v1.1).
2. **Platzieren** in `flows/flows.json` oder in **Edit flows** im Client einfügen.
3. **Aktivieren** in `nxs_config.json` → `"flows": { "enabled": true }`. FlowRunner registriert Trigger beim Start.

> [!NOTE]
> **Ehrliche v1-Limits:** kein automatischer Langflow-Importer; kein gebündelter Langflow-Runtime; LLM-Knoten werden `invoke`-Stubs (Modellaufruf lebt in `python.module`). Flows sind **lokal, in-process** — kein Cloud-Webhook-Wiring. HTTP/Webhook-Step-Typen geplant v1.1.

### Adoptionspfade für Flows

Drei Wege, Runtime-Flows zu adoptieren — wähle das Gewicht, das zu deiner App passt:

1. 🚫 **Keine Flows** — Weglassen oder deaktivieren; Starter funktioniert ohne FlowRunner
2. 🔧 **Flows als Helfer** — Kleine Automatisierungs-Services (Timer, Event-Hooks) in größerer App
3. 🔀 **Hybrid** — Blueprint-MVC + Background/Triggered-Flows im gleichen Binary

---

## Deine App bauen

Nexus zielt auf **native, datenintensive und feldbasierte Tools** — Trading-Desks, CAD-Viewer, wissenschaftliche Visualisierung, Game-Dev-Utilities, Audio/DSP-Benches, Robotik-Panels und Android-Feldtablets. Standard-Template: General-Purpose-Starter (Hello + Counter). Optionaler **Desmos-Plotter** unter `examples/plotter/`.

### Templates (Desktop & Android)

| Template | Stack | Guide |
|----------|-------|-------|
| `desktop-app` | SDL3 + ImGui + pybind11 + sol2 | [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md) |
| `android-app` | SDL3/GLES + Chaquopy + Djinni | [docs/templates/android-app.md](../../docs/templates/android-app.md) |

Output: `builds/framework/<name>/` · Layout: [builds/README.md](../../builds/README.md) · [template/README.md](../../template/README.md)

### Python auf Desktop vs Android

Derselbe `python.module`-Knoten in `blueprint.json` verdrahtet Kurven-Sampling auf **beiden** Templates — nur Python-Setup, Packaging und C++↔Python-Grenze unterscheiden sich.

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Eingebettetes Python** | pybind11 — CPython im nativen Prozess | Chaquopy auf der JVM; Djinni `ChaquopyPythonBridge` |
| **Quellbaum** | `python/` (z. B. `functions.py`) | `app/src/main/python/` |
| **Archiv** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **Keins** — Gradle/Chaquopy bündelt `.py` im APK |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Typischer Rebuild** | `cmake --build` (aktualisiert `python.dat`) | `./gradlew :app:assembleDebug` |

![Python Desktop vs Android Embedding-Flow](../../docs/assets/diagrams/python-desktop-vs-android-flow.svg)

*Gleicher `python.module`-Evaluate-Port — unterschiedliches Pack und Bridge pro Plattform.*

Guides: [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md)

### TypeScript + XHTML UI

Zwei UI-Authoring-Schichten werden auf dieselbe ImGui/Lua-API abgebildet — keine nutzt eine Browser-Engine.

**Imperatives Lua** (`panels.lua`) — unterste Schicht; `nxs.register_panel(...)` mit `ui.button`, Hotkeys; optionales `lua.dat` Hot-Reload.

**Deklaratives TS/XHTML** (`ui/ui.xhtml` + `ui/ui.ts`) — Markup und TypeScript werden zu Lua-Panel-Definitionen abgebildet. [`template/shared/dsl/`](../../template/shared/dsl/) mappt Tags (`window`, `panel`, `plot`, `node-editor`, …) auf Dear ImGui-, ImPlot- und imnodes-Aufrufe.

| Mechanismus | TS/XHTML | Wird zu |
|-------------|----------|---------|
| `state()` in `ui.ts` | `bind="sampleCount"` auf `<slider>` | Two-Way-ImGui-Widget-State |
| `native()` in `ui.ts` | `items-source="activeCurves"` | Read-only C++-Model-Projektion |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Dieselben `nxs.*`-Befehle wie Lua direkt |

Start: [template/desktop-app/ui/ui.xhtml](../../template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

### Lua-Skripte & optionale Flows

- **Lua** — Runtime-Panels und Hotkeys via sol2; `scripts/panels.lua` bearbeiten, Rebuild packt `lua.dat`
- **Flows** — optionale `flows.json`-Services; siehe [In-App-Automatisierungen](#in-app-automatisierungen-flowsjson) und [Langflow-Prozeduren importieren](#langflow-prozeduren-importieren)

### Wer am schnellsten lernt

| Persona | Start hier |
|---------|------------|
| Game Devs (ImGui-Overlays) | `scripts/panels.lua` → Hotkeys und Quick-Add-Buttons |
| C++-Ingenieure | `src/model/` + `src/controller/` → `FunctionRegistry` erweitern |
| Web Devs | `ui/ui.xhtml` + `ui/ui.ts` → Panel und Handler hinzufügen |
| Python-Analysten | `python/functions.py` → neues Kurven-Sampling |
| Android Devs | `android-app` generieren → Djinni-Bridge nachverfolgen |

Vollständiger Guide: [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

<details>
<summary><strong>Wann ein anderer Stack besser passt</strong></summary>

| Situation | Erwäge stattdessen |
|-----------|-------------------|
| Nur-Web-Team, kein Appetit auf C++/CMake | Electron oder Tauri |
| Pixel-perfekte Marketing-UI | Web oder natives UI-Toolkit mit Layout-Engines |
| iOS aus diesem Repo heute | Noch nicht geliefert — warte auf v1-iOS-Template |
| Neues safety-critical Projekt | Rust — siehe [Modernes C++](#modernes-c--wachsen-ohne-rewrite) |

</details>

---

## Der `misc/`-Ordner

Der `misc/`-Ordner bündelt **Framework-Repo-Tooling** — Gradle-Module, Convention Plugins, Erst-Setup, Container-Images, CI-Notizen und Hilfsskripte. Nichts davon wird in generierte native Apps ausgeliefert; es baut und startet nur den Projektgenerator.

| Pfad | Rolle |
|------|-------|
| [misc/core/](../core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, `nxs_config.json`-Schema (v2) |
| [misc/cli/](../cli/) | `:cli` — headless `generate`-Befehl |
| [misc/build-logic/](../build-logic/) | Included build — JVM-Toolchain 26, Convention Plugins |
| [misc/client-setup/](../client-setup/) | Erst-Installers (JDK 26 + Git) |
| [misc/scripts/](../scripts/) | [dev/](../scripts/dev/) · [test-gen/](../scripts/test-gen/) · [generate-diagrams/](../scripts/generate-diagrams/) |
| [misc/docker/](../docker/) | Containerisierte Generierung |
| [misc/jenkins/](../jenkins/) | Optionales Jenkins-CI |
| [misc/translations/](README.md) | Lokalisierte READMEs — [pt-BR](README.pt-BR.md) · [es](README.es.md) · [de](README.de.md) · [ru](README.ru.md) · [zh-CN](README.zh-CN.md) |

Gradle mappt `:core` und `:cli` aus `misc/` via [settings.gradle.kts](../../settings.gradle.kts). Hub: [misc/README.md](../README.md) · Pipeline: [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md)

**test-gen** schreibt Smoke/Instrumented-Test-Stubs für Apps unter `builds/framework/<project>/` (nicht den Generator selbst). Einstieg: `./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp` — siehe [misc/scripts/test-gen/README.md](../scripts/test-gen/README.md).

---

## Abhängigkeiten hinzufügen

Nach [client-setup](../client-setup/README.md) und **Generate Project** native Abhängigkeiten in der **generierten App** unter `builds/framework/<ProjectName>/` hinzufügen — nicht in den Compose-Generator-Modulen.

- **C++** — `CMakeLists.txt` mit `FetchContent` oder vcpkg erweitern; mit `cmake --build --preset debug` neu bauen
- **Python** — Desktop: `pip install`, `python/` bearbeiten, neu bauen; Android: Chaquopy `pip { install(...) }` in `app/build.gradle.kts`
- **Lua** — `.lua` in `scripts/` ablegen, aus `panels.lua` `require`; Rebuild packt `lua.dat`

Vollständiger Walkthrough: **[docs/guides/adding-dependencies.md](../../docs/guides/adding-dependencies.md)**

---

## Modernes C++ & wachsen ohne Rewrite

Generierte Projekte nutzen **C++20** mit RAII-Mustern, CMake-Presets und clang-format. Rust gewinnt weiterhin bei Compile-Time-Safety-Garantien — ehrlicher Trade-off für Teams mit bestehenden C++-Bibliotheken (CAD-Kerne, Codecs, Exchange-APIs) und ImGui/SDL3-Abhängigkeiten.

**Schrittweise wachsen, nicht von Grund auf neu schreiben.** Neue Blueprint-Knoten, Runtime-Flows und XHTML-autorierte Screens können neben älteren Lua-Skripten und maßgeschneiderten C++-Modulen im selben Prozess existieren. Teams auf Electron oder Tauri stehen oft vor der Wahl: Web-Shell-Overhead akzeptieren oder voller Stack-Rewrite. Nexus bietet einen dritten Weg — performance-kritisches C++ behalten, Authoring inkrementell modernisieren und profilen vor dem Umschreiben in einer anderen Sprache.

> *"Make it work, make it right, make it fast — in that order."* — oft Kent Beck zugeschrieben

---

## Zig patching (native Builds)

**Zig** ist eine optionale **Orchestrierungsschicht** für generierte native Apps — kein Rewrite des Kotlin-Generators `:app` / `:core`. Gradle bleibt Build-System für Compose-Client und Generierungspipeline.

### Warum Zig (Gewinne)

Zig ersetzt nicht den C++20-Stack — sondern **Build-Reibung**: weniger Toolchains, ein Cross-Compile-Pfad, dünneres JNI-Glue. **Nicht in Produktion gemessen** — chirurgischer Plan ([vollständiger Plan](../../docs/architecture/zig-patching.md)). **†** = Baseline im Repo gemessen (2026-07-13).

| Metrik | Vorher (CMake / Djinni) | Mit Zig (Ziel) | Gewinn |
|--------|-------------------------|----------------|--------|
| Kaltes Native-Configure † | ~174 s | ~20–30 s | **~83–88% schneller** |
| Host-Toolchains | 5–7 | **1** | **~83% weniger** |
| Festplatten-Footprint | ~10–12 GB | ~80 MB | **~99% kleiner** |
| Cross-Compile Linux → Windows | Nein | Ja | **Neue Fähigkeit** |
| Android-ABI-Build-Schritte † | 2 CMake-Presets | 1 `zig build` | **~50% weniger** |
| Djinni-LOC † | 228 / 8 Dateien | ~120 / 2 `.zig` | **~47% weniger** |
| Python-Glue-Dateien † | 10 | 3 | **~70% weniger** |
| Lua-Glue-Dateien † | 8 | 2 | **~75% weniger** |
| Build-Tools | CMake+Ninja+NDK+Djinni | **Zig** | **4 → 1** |
| Reproduzierbarer Hash | FetchContent variabel | `build.zig.zon.json` | **Deterministisch** |
| Inkrementeller Rebuild | ~6–10 s | ~4–6 s | **~30–40% schneller** |
| ArenaAllocator-Hotspots | 0 | 3 geplant | **Opt-in-Abdeckung** |
| CI-Smoke-Jobs | 5–7 Runner | 2 Runner | **~65–70% weniger** |
| Langflow: Flows enabled | Manuelles Risiko | **`enabled: false`** | **Sicherer Default** |
| Netzwerk-Deps Configure † | **7** FetchContent | **0** nach Vendor | **100% offline** |
| Onboarding-Docs † | ~10 Seiten | ~3 Seiten | **~70% weniger** |
| C-ABI-Allocator | Keiner | `nxs_alloc` opt-in | **Einheitliche C-ABI** |
| Release-Binary-Größe | CMake-Baseline | Zig LTO | **~3–8% kleiner** |
| Link-Zeit Release | ~40–60 s | ~25–40 s | **~30–35% schneller** |
| Artefakt-Pfade | Preset-abhängig | `zig-out/bin/` fix | **Vorhersagbares Layout** |

[Vollständiger Plan (Englisch)](../../docs/architecture/zig-patching.md)

---

## Jenseits schneller Automatisierung

**Power Automate**, **n8n** und ähnliche Tools glänzen bei Ops-Kleber. Das bricht, wenn der Quick-Fix *das Produkt ist*: keine native UI, schwaches Offline-Packaging, Cloud-Abhängigkeit.

Nexus behält das Knoten-Kanten-Denkmuster, generiert aber eine **echte native App** — C++/SDL3, Lua/Python, ImGui + TS/XHTML, Script-Packs, Desktop/Android-Programme. Siehe [Wie sich Nexus vergleicht](#wie-sich-nexus-vergleicht) für Tool-für-Tool-Kontext.

**Migrationspfad:** Module im Blueprint-Editor verdrahten → mit `:cli` oder **Generate Project** generieren → in Code-Schichten iterieren statt Flow-Patches stapeln. Ein n8n-Webhook kann am Rand für Ops-Kleber bleiben, während die App State, UI und Offline-Verhalten in-process besitzt.

| Bereich | Flow-Tools (typisch) | Nexus-Output |
|---------|----------------------|--------------|
| **Runtime** | Server-seitige Step-Engine | Desktop/Mobile-App oder Android-APK |
| **Offline / Feld** | Erfordert Konnektivität zum Workflow-Host | Offline-first SDL3-App |
| **Performance** | HTTP-Roundtrips zwischen Steps | In-Process-C++; Python/numpy |
| **UI-Oberfläche** | Vendor-Dashboard oder keine | ImGui + DSL-Seiten |
| **Cross-Platform** | Separate Integrationen pro Ziel | Ein `blueprint.json` verdrahtet Desktop + Android |

---

## Entwicklungsstatus

**Heute geliefert:**

- `:app` — Counter + Generate Project + Blueprint Editor + Flows Editor
- `:core` / `:cli` — Template-Generierung + `BlueprintValidator` + `FlowsValidator`
- `template/*` — Desktop + Android mit `blueprint.json` + optionalem `flows.json`
- Script-Archive — `lua.dat` / `python.dat` (Desktop), `lua.dat` im Android-APK
- `builds/`, `misc/client-setup/`, `docs/`

**Limitierungen (v1):** nur Compose-Desktop-Generator; ImGui-Ästhetik utilitarisch; Chaquopy vergrößert APK; kein iOS aus dieser Toolchain heute.

**Branch:** aktive Entwicklung auf **`main`** (`origin/main`).

Verbleibende Arbeit vor MVP: [Weg zum MVP](#weg-zum-mvp).

---

## Copyright und Lizenz

> [!IMPORTANT]
> **Apache License 2.0** — kommerzielle Nutzung, Modifikation und Distribution erlaubt. Copyright-Hinweise und [LICENSE](../../LICENSE)-Datei bei Weiterverteilung behalten. Generierter App-Code gehört dir; kopierte Template-Snippets sollten Apache-Hinweise behalten.

- © 2026 Nexus Framework contributors — Nexus Framework Client und gebündelte Templates/Docs
- **Generierte Projekte:** du besitzt den Anwendungscode, den der Generator schreibt; aus Nexus-Templates kopierte Teile sollten Apache-2.0-Hinweis behalten

Vollständiger Lizenztext: [Apache License 2.0](../../LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

## Siehe auch

*Blueprint deine App, generiere den Baum, liefere das Binary — dann in echten Code-Schichten iterieren.*

### Dokumentation

| Doc | Beschreibung |
|-----|--------------|
| [docs/README.md](../../docs/README.md) | Dokumentations-Hub |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, Themes |
| [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | `blueprint.json`-Schema |
| [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md) | `flows.json`-Schema |
| [AGENTS.md](../../AGENTS.md) | Build-Befehle für Coding-Assistenten |

### Ökosystem

| Technologie | Rolle |
|-------------|-------|
| [SDL3](https://www.libsdl.org/) | Fenster, Input, GPU-Oberflächen |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | Immediate-Mode-UI und Charts |
| [sol2](https://github.com/ThePhD/sol2) / [pybind11](https://pybind11.readthedocs.io/) | Lua und Python in C++ |
| [Chaquopy](https://chaquo.com/chaquopy/) / [Djinni](https://github.com/dropbox/djinni) | Python- und Kotlin-Bridge auf Android |
| [Langflow](https://github.com/langflow-ai/langflow) / [n8n](https://n8n.io/) | Optionales externes Authoring (Import in Nexus) |

| Repo | Rolle |
|------|-------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | Separate `:client-desktop`-Wizard-Distribution |

---

## Weg zum MVP

Wenn jede Zeile ✅ ist, ist Nexus Framework **MVP-ready**: native Apps generieren, Blueprints/Flows bearbeiten, Projekte schreiben und dokumentierten Desktop/Android-Build ausliefern.

### Client & Projektgenerator

| Item | Status |
|------|--------|
| Desktop + Android aus Templates generieren | ✅ |
| Blueprint-Editor (Compose) | ✅ |
| Flows-Editor-UI (Liste, aktivieren/deaktivieren, JSON-Vorschau) | ✅ |
| ProjectGenerator + Validatoren | ✅ |
| Compose-6-Schritt-Assistent *(v1 liefert 2-Screen Generate + Editoren)* | ⬜ |

### Templates

| Item | Status |
|------|--------|
| General-Purpose Desktop- + Android-Templates | ✅ |
| `blueprint.json` + optionale `flows.json`-Struktur | ✅ |
| TS/XHTML-DSL-Stubs, Lua-, Python-Pfade | ✅ |
| End-to-End-Desktop-App-Build in CI verifiziert | ⬜ |
| End-to-End-Android-APK-Build in CI verifiziert | ⬜ |

### Runtime / generierte Apps

| Item | Status |
|------|--------|
| `python.dat` / `lua.dat` Pack-Parität | ✅ |
| Desktop-pybind11 vollständig in generierter App verdrahtet (Phase 2) | ⬜ |
| Android-Chaquopy-Bridge E2E auf Gerät getestet | ⬜ |
| TS/XHTML → Lua Lowering-Compiler *(manueller `panels.lua`-Pfad dokumentiert)* | ⬜ |

### Docs & Developer Experience

| Item | Status |
|------|--------|
| README-Architektur- + Vergleichsabschnitte | ✅ |
| Template-`AGENTS.md`-Guides | ✅ |
| `client-setup`-Skripte (JDK 26) | ✅ |
| CLI `debug validate --all` oder Äquivalent in CI | ⬜ |

### Release

| Item | Status |
|------|--------|
| CI grün auf `main` | ⬜ |
| Veröffentlichtes Client-Binary (`builds/client/`) | ⬜ |
| Versions-Tag `v1.0.0` | ⬜ |

<details>
<summary><strong>Post-MVP-Roadmap (v1.1+) — klicken zum Erweitern</strong></summary>

| Item | Notizen |
|------|---------|
| imnodes natives Blueprint-Panel | Gleiches `blueprint.json`-Schema |
| Visueller Flows-Canvas-Editor | — |
| Langflow-JSON-Importer | Manuelle Übersetzung in v1 |
| Remote-Template-Katalog · iOS-Template | — |
| HTTP/Webhook-Step-Typen in `flows.json` | — |

</details>

