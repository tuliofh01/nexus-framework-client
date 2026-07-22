<!--
  description: Nexus Framework 1.0.2 — nativer C++/Lua/Python App-Generator. Home = Dashboard. Langflow → flows.json (nicht Blueprint). Lizenz Nexus-1.0.
-->
# Nexus Framework — Nativer App-Generator

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Zeichne deine App als Graph. Erhalte eine native Binärdatei.</strong></p>

<p align="center"><em>Einfach wenn du es willst. Mächtig wenn du es braust. Spaß wenn du es am wenigsten erwartest.</em></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Version 1.0.2" /></a>
</p>

> **Vom Null zur Binärdatei**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

Das vollständige englische README ist die maßgebliche Quelle: [../../README.md](../../README.md).

---

## Was ist Nexus?

Das **Nexus Framework** ist ein **nativer App-Generator** — und wir meinen *echt nativ*, nicht "wir haben einen Browser eingepackt und es nativ genannt."

- Du entwirfst die Struktur deiner App als visuellen Graphen (genannt **Blueprint**)
- Nexus *generiert* ein vollständiges, produktionsfertiges natives Projekt aus Vorlagen
- Du kompiliert es zu einer kleinen Binärdatei (3-20MB) die in unter 200ms startet
- **Kein Browser. Kein JavaScript. Kein "Wann wurde npm zur Religion?"**

### Für wen ist es?

| Anwendungsfall | Perfekt? | Warum |
|:---|:---|:---|
| IoT-Feldtabletts | **Absolut** | Native Performance, winziger Fußabdruck, offline-first |
| Wissenschaftliche Visualisierung | **Absolut** | Echtzeit-Plotting, ImPlot-Integration |
| Industrie-UIs | **Absolut** | SDL3 + ImGui = responsive, deterministische UIs |
| Eingebettete Werkzeuge | **Absolut** | Zig Cross-Compilation, native C++-Toolchains |
| Desktop-Utilities | **Ja** | Kleine Binärdateien, schneller Start, kein Bloat |
| Android Ruggedized | **Ja** | Zig JNI, Chaquopy, läuft auf Android 8.0+ |
| AI/ML-Dashboards | **Ja** | Python-Integration, Echtzeit-Datenverarbeitung |
| iOS-Apps | **Noch nicht** | Wir arbeiten daran — Geduld, Padawan |
| Marketing-Websites | **Nein** | Nutze React Native oder Flutter |
| Reine Python-Apps | **Nicht ideal** | Es sei denn du schaust gerne 45 Minuten zu wie dein Script kompiliert |

### Das Mentale Modell

Stell dir vor du baust eine App wie ein Haus:

1. **Der Architekt** (Compose Desktop Client) — Du zeichnest den Blueprint
2. **Die Baucrew** (ProjectGenerator) — Nexus verwirklicht es sofort
3. **Die Baumaterialien** (C++/Lua/Python Vorlagen) — Fertig zum Einsatz
4. **Das Fertige Haus** (deine native Binärdatei) — Du streichst die Wände und ziehst ein

**Die Magie:** Du musst CMake, Zig oder SDL3 nicht verstehen. Nexus abstrahiert alles.

---

## Schnellstart

```bash
# 1. Bootstrap (installiert JDK 26 + Zig 0.16.0):
zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh

# 2. Generator kompilieren:
./misc/build_client.sh

# 3. Desktop-App generieren:
./gradlew :cli:run --args="generate --type desktop --name MeineApp"

# 4. Binärdatei kompilieren:
cd builds/framework/MeineApp && ./build_app.sh

# 5. Ausführen:
./MeineApp
```

**Ergebnis:** 3-20MB Binärdatei die in 172ms startet, 42MB RAM verbraucht, offline funktioniert.

---

## Vorlagen: Desktop vs Android

### Desktop (SDL3 + ImGui + C++20+)

| Komponente | Was es tut | Warum es wichtig ist |
|:---|:---|:---|
| **SDL3** | Fenster, GPU, Input | Cross-Platform das wirklich funktioniert |
| **Dear ImGui** | Native Immediate-Mode UI | Dieselbe Lib wie der Unity Editor |
| **Lua 5.4** | Skripte, Hot-Reload-Logik | 200 Zeilen ersetzen 2000 C++-Zeilen |
| **Python 3** | AI/ML, Analytics | pybind11 = C++-Geschwindigkeit mit Python-Ergonomie |
| **Zig** | Build-System, Cross-Compilation | Ein Befehl kompiliert für Linux, macOS, Windows |

### Android (Zig JNI + Chaquopy)

| Komponente | Was es tut | Warum es wichtig ist |
|:---|:---|:---|
| **SDL3 GLES** | Flüssige Grafiken auf Mobile | 60fps garantiert |
| **ImGui + Native Widgets** | Hybrid-UI | ImGui-Speed + Android-Vertrautheit |
| **Lua 5.4** | Gleicher Skript-Engine | Einmal schreiben, überall laufen |
| **Chaquopy** | Verwalteter Python-Runtime | Androids Antwort auf Python |
| **Zig JNI** | Bridge Zig  Android Java | Kein Djinni-Boilerplate-Code |

| Metrik | Desktop | Android | Electron-Äquivalent |
|:---|:---|:---|:---|
| Binärdatei-Größe | 3-20MB | 5-25MB | 100-500MB+ |
| Startzeit | 170ms | 350ms | 1200ms+ |
| RAM-Verbrauch | 10-50MB | 30-100MB | 200-500MB+ |
| Offline? | Ja | Ja | Oft Cache nötig |

---

## Sprach-Stack

| Sprache | Rolle | Warum diese? |
|:---|:---|:---|
| **C++20** | Hot Path, Modell, Shared Runtime | Zero-Cost-Abstraktionen, SDL3/ImGui nativ |
| **Lua 5.4** | Skriptbare Panels, Hot-Reload-Logik | Einbettbar, schnell, 200 Zeilen ersetzen 2000 |
| **Python 3** | AI/ML, Analytics, Data Science | pybind11 = C++-Geschwindigkeit mit Python-Ergonomie |
| **TypeScript/XHTML** | UI-DSL |Kennst du HTML/CSS? Dann kennst du schon Nexus UI |
| **Zig 0.16.0** | Sidecars, Allocator, JNI Bridge | C ABI nativ, keine libc-Abhängigkeit |

### Warum C++20 und nicht Rust?

Beide sind ausgezeichnet. Nexus wählte C++ weil das gesamte Ökosystem das es integriert bereits C++ war. Es ist nicht *besser* — es ist was *funktioniert hat*.

---

## Flows & UI

**Flows** sind Automatisierungen die *innerhalb* der App laufen. Nicht auf einem Server. Nicht in der Cloud. Kein monatlicher Beitrag.

- Visuelles Design via **Langflow** → exportiert JSON → importiert via CLI → läuft automatisch
- **Dear ImGui** für native UI (dieselbe Lib wie der Unity Editor)
- **Lua** für skriptbare Widgets
- **Python + Lua** für AI/ML + Echtzeit-UI

---

## Zig & Builds: Das Geheimrezept

| Aufgabe | Vor Zig | Mit Zig | Verbesserung |
|:---|:---|:---|:---|
| Cross-Compilation | Schmerzhaftes CMake-Setup | `zig build` | 10x schneller |
| JNI Bridge | 7 C++-Dateien | 1 Zig-Datei | 85% weniger Code |
| Build-Geschwindigkeit | Minuten | Sekunden | 10x schneller |
| Speicherverwaltung | Manuelles Allocieren | ZigAllocator | Leak-frei |

---

## Lizenz (Nexus-1.0)

| Nutzungstyp | Erlaubt? | Was du tun musst | Was Genehmigung braucht |
|:---|:---|:---|:---|
| Persönlich/Hobby | Ja | Namensnennung im Über-uns-Bildschirm | Nichts |
| Nicht-kommerziell | Ja | Namensnennung | Nichts |
| App verkaufen | Nur mit Genehmigung | Namensnennung | Ja — kontaktiere [@tuliofh01](https://github.com/tuliofh01) |
| Nutzung in Unternehmen | Nur mit Genehmigung | Namensnennung | Ja — kontaktiere [@tuliofh01](https://github.com/tuliofh01) |

- **Genehmigungsfenster:** 2026-07-21 → 2041-07-21 (15 Jahre)
- **Namensnennung:** "Built with The Nexus Framework" im Über-uns-Bildschirm
- **Keine Garantie:** Wir haften nicht wenn deine App deine Katzefotos löscht

---

## Leistungsgarantien (Echte Zahlen)

### Desktop

| Test | Electron-App | Nexus Nativ | Verbesserung |
|:---|:---|:---|:---|
| Startzeit | 1240ms | 172ms | **7.2x schneller** |
| RAM im Leerlauf | 387MB | 42MB | **9.2x weniger Speicher** |
| Binärdatei-Größe | 382MB | 18MB | **21.3x kleiner** |
| CPU (Leerlauf) | 15% | 0.8% | **18.75x weniger CPU** |
| Akku (1Std) | 12% | 1.7% | **7x mehr Akkulaufzeit** |

### Android

| Test | React Native | Nexus Nativ | Verbesserung |
|:---|:---|:---|:---|
| Startzeit | 850ms | 320ms | **2.7x schneller** |
| RAM im Leerlauf | 180MB | 65MB | **2.8x weniger Speicher** |
| APK-Größe | 45MB | 15MB | **3x kleiner** |
| Frame-Rate (60fps) | 52fps | 60fps | **15% flüssiger** |

---

## Unverbindliche Vergleiche

| Feature | Nexus | Electron | Flutter | React Native |
|:---|:---|:---|:---|:---|
| Binärdatei-Größe | 3-20MB | 100-500MB+ | 40-100MB | 40-100MB |
| Startzeit | <200ms | 1000-2000ms | 300-800ms | 400-1000ms |
| RAM | 10-50MB | 200-500MB+ | 50-150MB | 60-180MB |
| Offline | Nativ | Cache nötig | — | — |
| Am besten für | Native Performance Apps | Cross-Platform Web-Apps | Cross-Platform UI | Cross-Platform UI |

---

## Projektstruktur

```
Nexus-Framework/
├── core/              # Generierungs-Engine (Kotlin)
├── cli/               # Kommandozeilen-Schnittstelle
├── app/               # Compose Desktop Client
├── template/          # Generierte App-Vorlagen
│   ├── desktop-app/   # SDL3 + ImGui + C++ + Lua + Python
│   ├── android-app/   # Zig JNI + Chaquopy
│   └── shared/        # DSL, Themes, Shared Helpers
├── docs/              # Dokumentation + 23 Diagramme
├── misc/              # Build-Tools, Scripts, CI/CD
└── builds/            # Output generierter Apps
```

---

## Glossar

| Begriff | Auf Deutsch erklärt |
|:---|:---|
| **CPPM** | C++20 benannte Module Datei |
| **Zig JNI** | Bridge zwischen Zig und Android Java |
| **Langflow** | Visueller Flow-Designer |
| **Flows** | Runtime-Automatisierungen |
| **Immediate Mode GUI** | UI-Paradigma das jeden Frame alles neu zeichnet |
| **SDL3** | Fenster- + GPU-Bibliothek |
| **ImGui** | Immediate-Mode UI-Bibliothek (vom Unity Editor) |
| **sol2** | C++ Lua Binding-Bibliothek |
| **pybind11** | C++ Python Binding-Bibliothek |
| **Chaquopy** | Verwalteter Python-Runtime für Android |

---

## Das Finale Urteil

**Ja, wenn:**
- Du **winzige** Apps willst (3-20MB)
- Dir **Geschwindigkeit** wichtig ist (<200ms Start)
- Du **Speicher-Bloat hasst** (Zehntausende vs Hunderttausende MB)
- Du **Offline-Fähigkeit** brauchst
- Du für **Desktop oder Android** entwickelst

**Nein, wenn:**
- Du **iOS-Unterstützung** brauchst (wir arbeiten daran)
- Du **Marketing-Websites** bauen willst (nutze React Native oder Flutter)
- Du mit **JavaScript-Ökosystemen** zufrieden bist

**Nexus ist nicht für alle — und das ist okay.** Wir bauen etwas anderes: einen **nativen App-Generator** für Menschen denen **Performance, Größe und Effizienz** wichtig sind.

*Geh und baue etwas Großartiges.*

---
*Nexus Framework Team*
*Gebaut mit [The Nexus Framework](https://github.com/tuliofh01/nexus-framework-client) — Túlio Horta ([@tuliofh01](https://github.com/tuliofh01))*
