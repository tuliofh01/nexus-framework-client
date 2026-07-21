<!--
  description: Nexus Framework 1.0.2 — apps nativas C++/Lua/Python. Home = dashboard. Langflow → flows.json (no blueprint). Licencia Nexus-1.0.
-->
# Nexus Framework — Generador de Apps Nativas

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Dibuja tu app como un grafo. Obtén un binario nativo. Sin navegador. Sin Electron. Sin nube.</strong></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Versión 1.0.2" /></a>
</p>

<p align="center"><em>El README en inglés es la fuente autoritativa.</em></p>

## Inicio rápido

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./misc/build_client.sh          # acepta la Licencia Nexus (una vez); CI: --accept-license
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MiApp"
# Opcional: export Langflow → stubs flows.json (no blueprint.json)
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MiApp/flows/flows.json"
cd builds/framework/MiApp && ./build_app.sh
```

## Layout (v1.0.2)

Módulos Gradle en la raíz: `:core`, `:cli`, `:app`. Un solo `build.gradle.kts`. Herramientas en `misc/` (ya no incluye el código de core/cli).

**Home** es el dashboard de inicio. Editores Blueprint / Flows (esqueletos).

## Langflow → flows

Importación opcional de JSON de exportación compatible con Langflow a stubs `flows.json` vía `import-langflow`. **No** genera `blueprint.json`. Formato compatible; Nexus **no está afiliado** con Langflow.

## Licencia (Nexus License)

- Uso **no comercial** del Toolkit y de apps generadas (personal / hobby / institucional no comercial): permitido **con atribución**.
- Hasta **2041-07-21**, se requiere autorización previa de [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) para: uso **comercial del Toolkit**; apps que **generan ingresos**; apps en una **institución comercial**.
- Después de 2041-07-21 esas restricciones caducan (salvo renovación); la atribución continúa.
- **Sin garantía.** El autor no responde por actividades ilícitas de apps derivadas.

Ver [LICENSE](../../LICENSE) · [README en inglés](../../README.md).

## Docs

| Doc | Contenido |
|:----|:----------|
| [docs/hub.md](../../docs/hub.md) | Hub |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | Arquitectura |
| [docs/assets/diagrams/activity-generate-pipeline.svg](../../docs/assets/diagrams/activity-generate-pipeline.svg) | Diagrama de generación |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | Guía |

*Dibuja el grafo, genera el árbol, entrega el binario.*
