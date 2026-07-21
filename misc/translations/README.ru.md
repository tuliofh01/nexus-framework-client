<!--
  description: Nexus Framework 1.0.2 — нативные C++/Lua/Python приложения. Home = dashboard. Langflow → flows.json (не blueprint). Лицензия Nexus-1.0.
-->
# Nexus Framework — Генератор Нативных Приложений

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Нарисуйте приложение как граф. Получите нативный бинарник. Без браузера. Без Electron. Без облака.</strong></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Версия 1.0.2" /></a>
</p>

<p align="center"><em>Авторитетный источник — английский README.</em></p>

## Быстрый старт

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./misc/build_client.sh          # принять лицензию Nexus (один раз); CI: --accept-license
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MyApp"
# Опционально: экспорт Langflow → stubs flows.json (не blueprint.json)
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MyApp/flows/flows.json"
cd builds/framework/MyApp && ./build_app.sh
```

## Структура (v1.0.2)

Модули Gradle в корне: `:core`, `:cli`, `:app`. Один корневой `build.gradle.kts`. Инструменты в `misc/` (исходники core/cli больше не там).

**Home** — это домашний dashboard. Редакторы Blueprint / Flows (скелеты).

## Langflow → flows

Опциональный импорт JSON экспорта, совместимого с Langflow, в stubs `flows.json` через `import-langflow`. **Не** создаёт `blueprint.json`. Совместимый формат; Nexus **не аффилирован** с Langflow.

## Лицензия (Nexus-1.0)

- **Некоммерческое** использование Toolkit и сгенерированных приложений (личное / хобби / некоммерческое учреждение) — разрешено **с указанием** репозитория.
- До **2041-07-21** нужно предварительное разрешение [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) для: **коммерческого Toolkit**; приложений, **приносящих доход**; использования в **коммерческой организации**.
- После 2041-07-21 эти требования истекают (если не продлены); указание автора остаётся обязательным.
- **Без гарантий.** Автор не несёт ответственности за противоправные действия производных приложений.

См. [LICENSE](../../LICENSE) · [английский README](../../README.md).

## Документация

| Документ | Описание |
|:---------|:---------|
| [docs/hub.md](../../docs/hub.md) | Хаб |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | Архитектура |
| [docs/assets/diagrams/activity-generate-pipeline.svg](../../docs/assets/diagrams/activity-generate-pipeline.svg) | Диаграмма генерации |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | Руководство |

*Нарисуйте граф, сгенерируйте дерево, поставьте бинарник.*
