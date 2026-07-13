# Фреймворк Nexus Company для разработки нативных приложений

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — логотип генератора нативных проектов C++/Lua/Python" width="240" />
</p>

<p align="center"><strong>🧩 Нативные приложения, а не вкладки браузера</strong> — поставляйте SDL3-бинарники из графа blueprint.</p>

<p align="center">
  🌐 <strong>Переводы:</strong>
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
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Лицензия Apache 2.0 open source" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 генератор проектов Compose Desktop" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-кроссплатформенный-green?style=flat-square" alt="SDL3 desktop и Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui UI immediate-mode" /></a>
</p>

> [!TIP]
> **Добро пожаловать.** Запустите [первоначальную настройку](#быстрый-старт), затем `./gradlew :app:run` — через несколько минут у вас будет Compose-клиент, редактор blueprint и путь к `builds/framework/<имя>/`. Без загрузки Chromium.

## Содержание

- [Что такое Nexus?](#что-такое-nexus)
- [Как Nexus сравнивается](#как-nexus-сравнивается)
- [Что находится в репозитории](#что-находится-в-репозитории)
- [Быстрый старт](#быстрый-старт)
- [Архитектура](#архитектура)
- [Blueprint и flows — два слоя](#blueprint-и-flows--два-слоя)
- [Создание приложения](#создание-приложения)
- [Папка `misc/`](#папка-misc)
- [Добавление зависимостей](#добавление-зависимостей)
- [Современный C++ и рост без переписывания](#современный-c-и-рост-без-переписывания)
- [Zig patching (нативные сборки)](#zig-patching-нативные-сборки)
- [За пределами быстрой автоматизации](#за-пределами-быстрой-автоматизации)
- [Статус разработки](#статус-разработки)
- [Copyright и лицензия](#copyright-и-лицензия)
- [Смотрите также](#смотрите-также)
- [Дорога к MVP](#дорога-к-mvp)

---

## Что такое Nexus?

**The Nexus Framework** — **open source конструктор нативных приложений**. Вы описываете приложение визуальным графом — [`blueprint.json`](../../docs/templates/blueprint-schema.md) для структуры, опциональный [`flows.json`](../../docs/templates/flows-schema.md) для in-app автоматизаций — и Nexus генерирует реальное приложение на **C++**, **Lua** и **Python** для **desktop** (Windows, macOS, Linux) и **Android**. Клиент Kotlin Compose (`:app`) создаёт эти графы; [`misc/core`](#папка-misc) валидирует и записывает проекты из [`template/`](#создание-приложения) с окнами SDL3, виджетами Dear ImGui, скриптингом sol2, авторингом UI TypeScript + XHTML и встроенным Python (pybind11 на desktop, Chaquopy + Djinni на Android).

Это **не** web-shell и не hosted flow runtime. Nexus поставляет скомпилированные бинарники — SDL3 + ImGui + ImPlot — с in-process Lua и Python. После генерации вы итерируете в обычных слоях кода (`cpp.model`, `python.module`, `ui.page`, Lua-панели). Отличия от Electron, n8n, Langflow или старта с нуля — в [Как Nexus сравнивается](#как-nexus-сравнивается).

---

## Как Nexus сравнивается

Nexus заимствует **ментальную модель узлов и рёбер** у визуальных flow-инструментов, но результат — **нативная программа**, а не вкладка Chromium, не cloud workflow host и не встроенный сервер Langflow в вашем приложении.

### vs Electron и Tauri

| Инструмент | Сильная сторона | Отличие Nexus |
|------------|-----------------|---------------|
| [Electron](https://www.electronjs.org/) | Web-first desktop; DOM/CSS/React как поверхность продукта | Нативный C++ runtime, бинарники ~3–20 MB, без renderer subprocess |
| [Tauri](https://tauri.app/) | Лёгкий web UI в OS WebView + Rust backend | ImGui immediate-mode UI, GPU-поверхности SDL3, общий stack desktop + Android |
| **Nexus** | Data-heavy, полевые, throughput-sensitive инструменты | Codegen по blueprint; in-process Python/numpy; UX в стиле game engine |

**Когда web shells выигрывают:** команда HTML/CSS-first или нужен iOS из web toolchain сегодня. **Когда выигрывает Nexus:** sub-ms обновление UI, маленькие бинарники, SDL3-паритет от trading desk до полевого Android-планшета — см. [Создание приложения](#создание-приложения).

### vs n8n и Power Automate

| Инструмент | Сильная сторона | Отличие Nexus |
|------------|-----------------|---------------|
| [n8n](https://n8n.io/) | Ops glue — webhooks, cron, SaaS-интеграции | Генерирует **поставляемое приложение** с нативным UI, offline-поведением и in-process state |
| [Power Automate](https://www.microsoft.com/power-automate) | Автоматизация бизнес-процессов в облаке Microsoft | Та же graph UX для **внутренней MVC-проводки**, а не внешних step engines |
| **Nexus** | Когда quick-fix flow *и есть* продукт | `blueprint.json` = структура на этапе build; опциональный `flows.json` = локальные in-process сервисы |

> [!WARNING]
> **Nexus — это не n8n и не Power Automate.** Используйте их для cloud SaaS wiring. Сгенерированное приложение всё ещё может вызывать n8n webhooks из Python/Lua на периферии.

### vs Langflow

| Инструмент | Сильная сторона | Отличие Nexus |
|------------|-----------------|---------------|
| [Langflow](https://github.com/langflow-ai/langflow) | Визуальное создание LLM/AI runtime flows | **Импорт/адаптация** экспортированного JSON в `blueprint.json` и `flows.json` — без bundled Langflow runtime в v1 |
| **Nexus `blueprint.json`** | — | MVC-граф на этапе build (`python.module`, `cpp.model`, `ui.page`, …), один раз потребляемый `ProjectGenerator` |
| **Nexus `flows.json`** | — | Опциональные in-app автоматизации (таймеры, события, background loops), загружаемые FlowRunner при старте |

Графы структуры → [`blueprint.json`](#структура-приложения-blueprintjson); автоматизация → [`flows.json`](#in-app-автоматизации-flowsjson). Полный workflow импорта: [Импорт процедур Langflow](#импорт-процедур-langflow).

<!-- Диаграмма: Langflow vs n8n vs Nexus blueprint -->
![📊 Langflow vs n8n vs Nexus blueprint — связанные шаги vs workflow automation vs build-time codegen](../../docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

*Runtime flows в стиле Langflow vs ops automation n8n vs build-time codegen Nexus — тот же визуальный паттерн, другая модель выполнения.*

### vs чистый C++ / с нуля

| Подход | Сильная сторона | Отличие Nexus |
|--------|-----------------|---------------|
| Ручной C++/CMake | Полный контроль; vendor SDK и legacy cores | Генераторы + blueprint graph; слои TS/XHTML и Lua без переписывания с нуля |
| Greenfield rewrite (Rust, Go, …) | Compile-time safety или новая экосистема | **Расти инкрементально** — сохранить performance-critical C++, добавлять blueprint nodes и flows рядом со старым кодом |
| **Nexus** | Команды между web-shell overhead и полным rewrite | Третий путь: модернизировать authoring по шагам, профилировать перед миграцией языков |

---

## Что находится в репозитории

| Путь | Роль |
|------|------|
| [`app/`](../../app/) | Compose Desktop client (`:app`) — Generate Project, редакторы blueprint/flows |
| [`misc/`](../../misc/) | Генератор `:core`, `:cli`, client-setup, scripts, Docker — см. [Папка `misc/`](#папка-misc) |
| [`template/`](../../template/) | desktop-app · android-app · shared — копируются в `builds/framework/<имя>/` |
| [`builds/`](../../builds/) | Артефакты клиента → `builds/client/` · сгенерированные apps → `builds/framework/` |
| [`docs/`](../../docs/) | Хаб документации → [docs/README.md](../../docs/README.md) |

Это monorepo **Framework** (`:app`, `:core`, `:cli`). Не отдельный репозиторий [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (wizard `:client-desktop` там).

---

## Быстрый старт

**1. Первоначальная настройка** — установите JDK 26 + Git (один раз на машину):

| Платформа | Setup | Env |
|-----------|-------|-----|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Подробности: [misc/client-setup/README.md](../client-setup/README.md).

**2. Запуск клиента**

```bash
source misc/client-setup/env.sh
./gradlew :app:run
```

**3. Генерация проекта**

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Или **Generate Project** в Compose UI → **Edit blueprint** / **Edit flows**.

**4. Сборка сгенерированного приложения**

```bash
cd template/desktop-app && cmake --preset debug && cmake --build --preset debug
# вывод также попадает в builds/framework/<имя>/ после генерации
```

**5. Читайте docs** — [docs/README.md](../../docs/README.md) · [coding-with-nexus](../../docs/guides/coding-with-nexus.md) · [generation-pipeline](../../docs/guides/generation-pipeline.md)

Компиляция и тест генератора: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy клиента: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](../../builds/client/app/)

---

## Архитектура

### Full-stack архитектура
*Compose client → flow генерации `:core` → SDL3 runtimes (ваше приложение, не вкладка браузера)*

![Full-stack архитектура Nexus](../../docs/assets/diagrams/full-stack-architecture.svg)

Клиент `:app` создаёт JSON blueprint и flows; `:core` валидирует и материализует templates в `builds/framework/<имя>/`. Сгенерированные apps работают как нативные SDL3-бинарники с ImGui, Lua и опциональным Python.

### Flow генерации и builds
*От client-setup и Gradle modules до `builds/framework/<имя>/`*

![Flow генерации и builds](../../docs/assets/diagrams/generation-builds-flow.svg)

Setup JDK 26 → Gradle `:core` / `:cli` / `:app` → `ProjectGenerator` пишет CMake/Gradle деревья в `builds/`.

### Desktop vs Android runtime
*Общий MVC на SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

![Desktop vs Android runtime](../../docs/assets/diagrams/desktop-vs-android-runtime.svg)

Один `blueprint.json` проводит MVC на обоих templates; меняются только Python bridge и packaging по платформе.

Справочник слоёв: [docs/architecture/overview.md](../../docs/architecture/overview.md) · Blueprint/flows: [Blueprint и flows](#blueprint-и-flows--два-слоя) · Python: [Python на desktop vs Android](#python-на-desktop-vs-android)

---

## Blueprint и flows — два слоя

Nexus разделяет **структуру приложения на этапе build** и **автоматизации, работающие внутри приложения**. Один canvas Langflow может быть разделён на оба файла после перевода.

### Blueprint vs flows — два слоя
*Структура на этапе build vs опциональные in-app автоматизации*

![blueprint.json vs flows.json — двухслойная модель](../../docs/assets/diagrams/blueprint-vs-flows-layers.svg)

`blueprint.json` проводит MVC-структуру, один раз потребляемую `:core`; `flows.json` регистрирует in-process triggers, загружаемые FlowRunner при старте.

### Структура приложения (`blueprint.json`)

Build-time граф в корне проекта. Узлы объявляют модули; рёбра соединяют поток данных и команд в сгенерированном MVC app.

| Тип узла | Роль |
|----------|------|
| `python.module` | Python sampling / analytics (`python/functions.py`) |
| `cpp.model` | C++ domain state (`FunctionRegistry`, caches) |
| `cpp.controller` | Команды + wiring (`PlotController`) |
| `ui.page` | TS/XHTML страница (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Runtime Lua panels (`scripts/panels.lua`) |

**Редактировать в клиенте:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (Compose canvas + JSON inspector в v1; нативная панель **imnodes** запланирована v1.1 — тот же schema).

Примеры: [template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](../../template/android-app/blueprint.json) · Schema: [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)

#### Примеры в стиле Langflow

Справочные диаграммы визуального паттерна, который Nexus отражает на этапе build (не runtime):

- [RAG chatbot flow](../../docs/assets/examples/langflow-rag-chatbot.svg) — Langflow runtime; маппинг модулей на типы blueprint nodes
- [Agent with tools](../../docs/assets/examples/langflow-agent-tools.svg) — agent loop → `python.module`, `cpp.controller`, …
- [Nexus blueprint app structure](../../docs/assets/examples/nexus-blueprint-app-structure.svg) — build-time MVC codegen

### In-app автоматизации (`flows.json`)

Опциональные runtime services — background loops, event triggers, schedules.

| Режим | Когда выполняется | Пример trigger |
|-------|-------------------|----------------|
| `background` | Пока app жива | `interval` каждые 5000 ms |
| `triggered` | Только по условию | `event` `curve.added`, `startup`, `manual` |

**Редактировать в клиенте:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — список flows, enable/disable, JSON preview (визуальный редактор v1.1). Schema: [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md).

Добавляйте несколько flows в массив `flows` (каждый с уникальным `id`). Отключайте глобально через `nxs_config.json` → `"flows": { "enabled": false }` или по flow с `"enabled": false`.

Пример: [template/desktop-app/flows/flows.json](../../template/desktop-app/flows/flows.json)

### Импорт процедур Langflow

[Langflow](https://github.com/langflow-ai/langflow) — опциональный **внешний инструмент** авторинга. Экспортируйте flow JSON и адаптируйте как нативные Nexus services — **не** запуская Langflow внутри поставляемого app.

**Шаг 1 — Экспорт из Langflow**

1. Постройте визуальный flow в Langflow (LLM, Prompt, Tool, Retriever, Agent, …).
2. Экспортируйте как JSON через **Export flow** или Langflow API (`/api/v1/flows/{id}`). Имена полей и вложенность **отличаются** от Nexus schemas; относитесь к экспорту как к design artifact, не drop-in файлу.

**Шаг 2 — Маппинг на Nexus**

| Концепция Langflow | Цель Nexus |
|--------------------|------------|
| Компоненты структуры app | Узлы и MVC ports в [`blueprint.json`](#структура-приложения-blueprintjson) |
| Компоненты автоматизации (LLM, Tool, Agent, …) | `flows.json` → `steps[]` с `type: invoke` → `nxs.*`, `python.*`, `lua.*` |
| Рёбра / порядок выполнения | Упорядоченный массив `steps`; ветвления через `condition` (v1.1) |
| Trigger (chat, webhook, schedule) | `trigger.type`: `event`, `interval`, `startup`, `manual`, `hotkey` |
| Долгий loop | `mode: background` |
| Выполнение по запросу | `mode: triggered` |

**Шаг 3 — Поставка в проект**

![Langflow export → flows.json adoption workflow](../../docs/assets/diagrams/langflow-adoption-workflow.svg)

1. **Перевести** экспорт в [flows schema](../../docs/templates/flows-schema.md) (вручную v1; importer v1.1).
2. **Разместить** в `flows/flows.json` или вставить в **Edit flows** в клиенте.
3. **Включить** в `nxs_config.json` → `"flows": { "enabled": true }`. FlowRunner регистрирует triggers при старте.

> [!NOTE]
> **Честные лимиты v1:** нет автоматического Langflow importer; нет bundled Langflow runtime; LLM nodes становятся `invoke` stubs (вызов модели живёт в `python.module`). Flows **локальные, in-process** — не cloud webhook wiring. HTTP/webhook step types запланированы v1.1.

### Пути адаптации flows

Три способа адаптировать runtime flows — выберите вес, подходящий вашему app:

1. 🚫 **Без flows** — Пропустить или отключить; starter работает без FlowRunner
2. 🔧 **Flows как helpers** — Небольшие automation services (timers, event hooks) внутри большего app
3. 🔀 **Гибрид** — Blueprint MVC + background/triggered flows в одном binary

---

## Создание приложения

Nexus ориентирован на **нативные инструменты с интенсивной работой с данными и полевым развёртыванием** — trading desks, CAD viewers, научная визуализация, game dev utilities, audio/DSP benches, robotics panels и полевые Android-планшеты. Шаблон по умолчанию: general-purpose starter (hello + counter). Опциональный **plotter в стиле Desmos** в `examples/plotter/`.

### Templates (desktop и Android)

| Template | Stack | Guide |
|----------|-------|-------|
| `desktop-app` | SDL3 + ImGui + pybind11 + sol2 | [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md) |
| `android-app` | SDL3/GLES + Chaquopy + Djinni | [docs/templates/android-app.md](../../docs/templates/android-app.md) |

Output: `builds/framework/<имя>/` · Layout: [builds/README.md](../../builds/README.md) · [template/README.md](../../template/README.md)

### Python на desktop vs Android

Тот же узел `python.module` в `blueprint.json` проводит sampling кривых на **обоих** templates — меняются только Python setup, packaging и граница C++↔Python.

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Встроенный Python** | pybind11 — CPython внутри native process | Chaquopy на JVM; Djinni `ChaquopyPythonBridge` |
| **Дерево исходников** | `python/` (напр. `functions.py`) | `app/src/main/python/` |
| **Архив** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **Нет** — Gradle/Chaquopy упаковывает `.py` в APK |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Типичный rebuild** | `cmake --build` (обновляет `python.dat`) | `./gradlew :app:assembleDebug` |

![Python desktop vs Android embedding flow](../../docs/assets/diagrams/python-desktop-vs-android-flow.svg)

*Тот же evaluate port `python.module` — разный pack и bridge по платформе.*

Guides: [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md)

### TypeScript + XHTML UI

Два слоя UI authoring опускаются к той же ImGui/Lua API — ни один не использует browser engine.

**Императивный Lua** (`panels.lua`) — нижний слой; `nxs.register_panel(...)` с `ui.button`, hotkeys; опциональный hot-reload через `lua.dat`.

**Декларативный TS/XHTML** (`ui/ui.xhtml` + `ui/ui.ts`) — markup и TypeScript опускаются к Lua panel definitions. [`template/shared/dsl/`](../../template/shared/dsl/) мапит tags (`window`, `panel`, `plot`, `node-editor`, …) на вызовы Dear ImGui, ImPlot и imnodes.

| Механизм | TS/XHTML | Опускается к |
|----------|----------|--------------|
| `state()` в `ui.ts` | `bind="sampleCount"` на `<slider>` | Two-way ImGui widget state |
| `native()` в `ui.ts` | `items-source="activeCurves"` | Read-only проекция C++ model |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Те же `nxs.*` commands, что Lua вызывает напрямую |

Начните здесь: [template/desktop-app/ui/ui.xhtml](../../template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

### Lua scripts и опциональные flows

- **Lua** — runtime panels и hotkeys via sol2; редактируйте `scripts/panels.lua`, rebuild упаковывает `lua.dat`
- **Flows** — опциональные `flows.json` services; см. [In-app автоматизации](#in-app-автоматизации-flowsjson) и [Импорт процедур Langflow](#импорт-процедур-langflow)

### Кто учится быстрее

| Persona | Начните с |
|---------|-----------|
| Game devs (ImGui overlays) | `scripts/panels.lua` → hotkeys и quick-add buttons |
| C++ engineers | `src/model/` + `src/controller/` → расширить `FunctionRegistry` |
| Web devs | `ui/ui.xhtml` + `ui/ui.ts` → добавить panel и handler |
| Python analysts | `python/functions.py` → новый curve sampling |
| Android devs | Сгенерировать `android-app` → проследить Djinni bridge |

Полный guide: [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

<details>
<summary><strong>Когда другой stack может подойти лучше</strong></summary>

| Ситуация | Рассмотрите |
|----------|-------------|
| Web-only команда, без аппетита к C++/CMake | Electron или Tauri |
| Pixel-perfect marketing UI | Web или native UI toolkit с layout engines |
| iOS из этого repo сегодня | Ещё не поставляется — ждите v1 iOS template |
| Новый safety-critical проект | Rust — см. [Современный C++](#современный-c-и-рост-без-переписывания) |

</details>

---

## Папка `misc/`

Папка `misc/` объединяет **инструменты Framework repo** — Gradle modules, convention plugins, first-run setup, container images, CI notes и helper scripts. Ничего из этого не поставляется в сгенерированные native apps; это только сборка и запуск project generator.

| Путь | Роль |
|------|------|
| [misc/core/](../core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, schema `nxs_config.json` (v2) |
| [misc/cli/](../cli/) | `:cli` — headless `generate` command |
| [misc/build-logic/](../build-logic/) | Included build — JVM toolchain 26, convention plugins |
| [misc/client-setup/](../client-setup/) | First-run installers (JDK 26 + Git) |
| [misc/scripts/](../scripts/) | [dev/](../scripts/dev/) · [test-gen/](../scripts/test-gen/) · [generate-diagrams/](../scripts/generate-diagrams/) |
| [misc/docker/](../docker/) | Containerized generation |
| [misc/jenkins/](../jenkins/) | Optional Jenkins CI |
| [misc/translations/](README.md) | Локализованные README — [pt-BR](README.pt-BR.md) · [es](README.es.md) · [de](README.de.md) · [ru](README.ru.md) · [zh-CN](README.zh-CN.md) |

Gradle мапит `:core` и `:cli` из `misc/` via [settings.gradle.kts](../../settings.gradle.kts). Hub: [misc/README.md](../README.md) · Pipeline: [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md)

**test-gen** пишет smoke/instrumented test stubs для apps под `builds/framework/<project>/` (не сам generator). Entry: `./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp` — см. [misc/scripts/test-gen/README.md](../scripts/test-gen/README.md).

---

## Добавление зависимостей

После [client-setup](../client-setup/README.md) и **Generate Project** добавляйте native dependencies в **сгенерированном app** под `builds/framework/<ProjectName>/` — не в модулях Compose generator.

- **C++** — расширьте `CMakeLists.txt` с `FetchContent` или vcpkg; пересоберите с `cmake --build --preset debug`
- **Python** — desktop: `pip install`, редактируйте `python/`, rebuild; Android: Chaquopy `pip { install(...) }` в `app/build.gradle.kts`
- **Lua** — положите `.lua` в `scripts/`, `require` из `panels.lua`; rebuild упаковывает `lua.dat`

Полный walkthrough: **[docs/guides/adding-dependencies.md](../../docs/guides/adding-dependencies.md)**

---

## Современный C++ и рост без переписывания

Сгенерированные проекты используют **C++20** с RAII patterns, CMake presets и clang-format. Rust по-прежнему выигрывает в compile-time safety guarantees — честный trade-off для команд с existing C++ libraries (CAD kernels, codecs, exchange APIs) и ImGui/SDL3 dependencies.

**Растите шаг за шагом, не переписывайте с нуля.** Новые blueprint nodes, runtime flows и XHTML-authored screens могут сосуществовать со старыми Lua scripts и bespoke C++ modules в одном process. Команды на Electron или Tauri часто стоят перед выбором: принять web-shell overhead или полный stack rewrite. Nexus предлагает третий путь — сохранить performance-critical C++, модернизировать authoring incrementally и профилировать перед rewrite на другом языке.

> *"Make it work, make it right, make it fast — in that order."* — часто приписывают Kent Beck

---

## Zig patching (нативные сборки)

**Zig** — опциональный слой **оркестрации** для сгенерированных нативных приложений; это не переписывание Kotlin-генератора `:app` / `:core`. Gradle остаётся системой сборки Compose-клиента и pipeline генерации.

| Фаза | Фокус | Статус |
|------|-------|--------|
| 0 | Установка Zig **0.14.x** в `misc/client-setup` | ⬜ Запланировано |
| 1 | Sidecar `zig-services/` рядом с CMake | ⬜ Запланировано |
| 2 | Импортёр Langflow → `flows.json` (`enabled: false` при импорте) | ⬜ Запланировано |
| 3 | Zig как native backend по умолчанию (desktop) | ⬜ Запланировано |
| 4 | Zig JNI на Android (замена Djinni) | ⬜ Запланировано |
| 5 | Opt-in ArenaAllocator в hotspots AppModel | ⬜ Запланировано |

Поэтапно: Zig рядом с CMake → desktop Zig default → Android JNI → opt-in ArenaAllocator. Зафиксировать Zig **0.14.x**; для Android нужен NDK (API ≥ 29) — Zig не поставляет Bionic.

[Полный план (англ.)](../../docs/architecture/zig-patching.md)

---

## За пределами быстрой автоматизации

**Power Automate**, **n8n** и подобные tools отлично справляются с ops glue. Это ломается, когда quick fix *и есть* продукт: нет native UI, слабое offline packaging, cloud dependency.

Nexus сохраняет node-and-edge mental model, но генерирует **реальное native app** — C++/SDL3, Lua/Python, ImGui + TS/XHTML, script packs, desktop/Android programs. См. [Как Nexus сравнивается](#как-nexus-сравнивается) для tool-by-tool context.

**Migration path:** подключите modules в blueprint editor → generate с `:cli` или **Generate Project** → итерируйте в code layers вместо stacking flow patches. n8n webhook может остаться на edge для ops glue, пока app владеет state, UI и offline behavior in-process.

| Область | Flow tools (типично) | Nexus output |
|---------|----------------------|--------------|
| **Runtime** | Server-side step engine | Desktop/mobile app или Android APK |
| **Offline / field** | Требует connectivity к workflow host | Offline-first SDL3 app |
| **Performance** | HTTP round-trips между steps | In-process C++; Python/numpy |
| **UI surface** | Vendor dashboard или none | ImGui + DSL pages |
| **Cross-platform** | Separate integrations per target | Один `blueprint.json` проводит desktop + Android |

---

## Статус разработки

**Поставлено сегодня:**

- `:app` — Counter + Generate Project + Blueprint Editor + Flows Editor
- `:core` / `:cli` — template generation + `BlueprintValidator` + `FlowsValidator`
- `template/*` — desktop + Android с `blueprint.json` + optional `flows.json`
- Script archive packs — `lua.dat` / `python.dat` (desktop), `lua.dat` в Android APK
- `builds/`, `misc/client-setup/`, `docs/`

**Ограничения (v1):** только Compose Desktop generator; ImGui aesthetics utilitarian; Chaquopy увеличивает APK; нет iOS из этой toolchain сегодня.

**Branch:** активная разработка на **`main`** (`origin/main`).

Оставшаяся работа до MVP: [Дорога к MVP](#дорога-к-mvp).

---

## Copyright и лицензия

> [!IMPORTANT]
> **Apache License 2.0** — commercial use, modification и distribution разрешены. Сохраняйте copyright notices и файл [LICENSE](../../LICENSE) при redistribution. Generated app code — ваш; copied template snippets должны сохранять Apache notices.

- © 2026 Nexus Framework contributors — Nexus Framework Client и bundled templates/docs
- **Generated projects:** вы владеете application code, который пишет generator; portions copied from Nexus templates должны сохранять Apache 2.0 notice

Полный текст лицензии: [Apache License 2.0](../../LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

## Смотрите также

*Blueprint your app, generate the tree, ship the binary — затем итерируйте в real code layers.*

### Документация

| Doc | Описание |
|-----|----------|
| [docs/README.md](../../docs/README.md) | Documentation hub |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, themes |
| [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | Schema `blueprint.json` |
| [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md) | Schema `flows.json` |
| [AGENTS.md](../../AGENTS.md) | Build commands для coding assistants |

### Экосистема

| Technology | Role |
|------------|------|
| [SDL3](https://www.libsdl.org/) | Windowing, input, GPU surfaces |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | Immediate-mode UI и charts |
| [sol2](https://github.com/ThePhD/sol2) / [pybind11](https://pybind11.readthedocs.io/) | Lua и Python в C++ |
| [Chaquopy](https://chaquo.com/chaquopy/) / [Djinni](https://github.com/dropbox/djinni) | Python и Kotlin bridge на Android |
| [Langflow](https://github.com/langflow-ai/langflow) / [n8n](https://n8n.io/) | Optional external authoring (import в Nexus) |

| Repo | Role |
|------|------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | Separate `:client-desktop` wizard distribution |

---

## Дорога к MVP

Когда каждая строка ✅, Nexus Framework **MVP-ready**: generate native apps, edit blueprints/flows, write out projects и ship documented desktop/Android build.

### Client & project generator

| Item | Status |
|------|--------|
| Generate desktop + Android from templates | ✅ |
| Blueprint editor (Compose) | ✅ |
| Flows editor UI (list, enable/disable, JSON preview) | ✅ |
| ProjectGenerator + validators | ✅ |
| Compose 6-step wizard *(v1 ships 2-screen Generate + editors)* | ⬜ |

### Templates

| Item | Status |
|------|--------|
| General-purpose desktop + Android templates | ✅ |
| `blueprint.json` + optional `flows.json` structure | ✅ |
| TS/XHTML DSL stubs, Lua, Python paths | ✅ |
| End-to-end desktop app build verified in CI | ⬜ |
| End-to-end Android APK build verified in CI | ⬜ |

### Runtime / generated apps

| Item | Status |
|------|--------|
| `python.dat` / `lua.dat` pack parity | ✅ |
| Desktop pybind11 fully wired in generated app (Phase 2) | ⬜ |
| Android Chaquopy bridge E2E tested on device | ⬜ |
| TS/XHTML → Lua lowering compiler *(manual `panels.lua` documented)* | ⬜ |

### Docs & developer experience

| Item | Status |
|------|--------|
| README architecture + comparison sections | ✅ |
| Template `AGENTS.md` guides | ✅ |
| `client-setup` scripts (JDK 26) | ✅ |
| CLI `debug validate --all` or equivalent in CI | ⬜ |

### Release

| Item | Status |
|------|--------|
| CI build green on `main` | ⬜ |
| Published client binary (`builds/client/`) | ⬜ |
| Version tag `v1.0.0` | ⬜ |

<details>
<summary><strong>Post-MVP roadmap (v1.1+) — нажмите, чтобы развернуть</strong></summary>

| Item | Notes |
|------|-------|
| imnodes native blueprint panel | Same `blueprint.json` schema |
| Visual flows canvas editor | — |
| Langflow JSON importer | Manual translation in v1 |
| Remote template catalog · iOS template | — |
| HTTP/webhook step types in `flows.json` | — |

</details>

