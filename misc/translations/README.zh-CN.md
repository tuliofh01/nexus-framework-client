# Nexus Company 原生应用开发框架

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — 原生 C++/Lua/Python 项目生成器 logo" width="240" />
</p>

<p align="center"><strong>🧩 原生应用，而非浏览器标签页</strong> — 从 blueprint 图生成 SDL3 二进制程序。</p>

<p align="center">
  🌐 <strong>翻译：</strong>
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
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache 2.0 开源许可证" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 Compose Desktop 项目生成器" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-跨平台-green?style=flat-square" alt="SDL3 桌面与 Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui 即时模式 UI" /></a>
</p>

> [!TIP]
> **欢迎加入。** 运行[首次设置](#快速开始)，然后执行 `./gradlew :app:run` — 几分钟内即可获得 Compose 客户端、blueprint 编辑器，以及通往 `builds/framework/<名称>/` 的路径。无需下载 Chromium。

## 目录

- [什么是 Nexus？](#什么是-nexus)
- [Nexus 如何比较](#nexus-如何比较)
- [本仓库包含什么](#本仓库包含什么)
- [快速开始](#快速开始)
- [架构](#架构)
- [Blueprint 与 flows — 两层](#blueprint-与-flows--两层)
- [构建你的应用](#构建你的应用)
- [`misc/` 文件夹](#misc-文件夹)
- [添加依赖](#添加依赖)
- [现代 C++ 与无需重写即可成长](#现代-c-与无需重写即可成长)
- [超越快速修复自动化](#超越快速修复自动化)
- [开发状态](#开发状态)
- [版权与许可](#版权与许可)
- [参见](#参见)
- [通往 MVP](#通往-mvp)

---

## 什么是 Nexus？

**The Nexus Framework** 是一个**开源原生应用构建器**。你将应用描述为可视化图 — 结构用 [`blueprint.json`](../../docs/templates/blueprint-schema.md)，可选 [`flows.json`](../../docs/templates/flows-schema.md) 用于应用内自动化 — Nexus 生成真实的 **C++**、**Lua** 和 **Python** 应用，支持**桌面**（Windows、macOS、Linux）和 **Android**。Kotlin Compose 客户端（`:app`）编写这些图；[`misc/core`](#misc-文件夹) 验证并写入 [`template/`](#构建你的应用) 项目，包含 SDL3 窗口、Dear ImGui 控件、sol2 脚本、TypeScript + XHTML UI 创作，以及内置 Python（桌面 pybind11，Android Chaquopy + Djinni）。

这**不是**浏览器壳或托管 flow runtime。Nexus 交付编译后的二进制 — SDL3 + ImGui + ImPlot — 进程内 Lua 与 Python。生成后，你在常规代码层（`cpp.model`、`python.module`、`ui.page`、Lua 面板）中迭代。与 Electron、n8n、Langflow 或从零开始的区别见 [Nexus 如何比较](#nexus-如何比较)。

---

## Nexus 如何比较

Nexus 借鉴可视化 flow 工具的**节点-边思维模型**，但产出是**原生程序** — 不是 Chromium 标签页，不是云端 workflow 主机，也不是嵌入应用内的 Langflow 服务器。

### vs Electron 与 Tauri

| 工具 | 优势 | Nexus 差异 |
|------|------|------------|
| [Electron](https://www.electronjs.org/) | Web 优先桌面应用；DOM/CSS/React 作为产品界面 | 原生 C++ runtime，约 3–20 MB 二进制，无 renderer 子进程 |
| [Tauri](https://tauri.app/) | OS WebView 中的轻量 Web UI + Rust 后端 | ImGui 即时模式 UI，SDL3 GPU 表面，共享桌面 + Android 栈 |
| **Nexus** | 数据密集、现场部署、吞吐敏感的工具 | Blueprint 驱动 codegen；进程内 Python/numpy；游戏引擎式 UX |

**Web shell 何时胜出：** 团队 HTML/CSS 优先，或今天就需要从 Web 工具链出 iOS。**Nexus 何时胜出：** 亚毫秒 UI 刷新、小二进制、从交易台到 Android 现场平板的 SDL3 一致性 — 见 [构建你的应用](#构建你的应用)。

### vs n8n 与 Power Automate

| 工具 | 优势 | Nexus 差异 |
|------|------|------------|
| [n8n](https://n8n.io/) | Ops 胶水 — webhooks、cron、SaaS 集成 | 生成**可交付应用**，含原生 UI、离线行为与进程内状态 |
| [Power Automate](https://www.microsoft.com/power-automate) | Microsoft 云中的业务流程自动化 | 相同图 UX 用于**内部 MVC 连线**，而非外部 step engine |
| **Nexus** | 当快速修复 flow *就是*产品时 | `blueprint.json` = 构建时结构；可选 `flows.json` = 本地进程内服务 |

> [!WARNING]
> **Nexus 不是 n8n 或 Power Automate。** 请用它们做云端 SaaS 集成。生成的应用仍可从 Python/Lua 在边缘调用 n8n webhooks。

### vs Langflow

| 工具 | 优势 | Nexus 差异 |
|------|------|------------|
| [Langflow](https://github.com/langflow-ai/langflow) | LLM/AI runtime flow 的可视化创作 | **导入/采用**导出 JSON 到 `blueprint.json` 与 `flows.json` — v1 不捆绑 Langflow runtime |
| **Nexus `blueprint.json`** | — | 构建时 MVC 图（`python.module`、`cpp.model`、`ui.page` 等），由 `ProjectGenerator` 一次性消费 |
| **Nexus `flows.json`** | — | 可选应用内自动化（定时器、事件、后台循环），启动时由 FlowRunner 加载 |

结构图 → [`blueprint.json`](#应用结构-blueprintjson)；自动化图 → [`flows.json`](#应用内自动化-flowsjson)。完整导入流程：[导入 Langflow 流程](#导入-langflow-流程)。

<!-- 图：Langflow vs n8n vs Nexus blueprint -->
![📊 Langflow vs n8n vs Nexus blueprint — 连接步骤 vs 工作流自动化 vs 构建时代码生成](../../docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

*Langflow 式 runtime flow vs n8n ops 自动化 vs Nexus 构建时代码生成 — 相同视觉模式，不同执行模型。*

### vs 纯 C++ / 从零开始

| 方式 | 优势 | Nexus 差异 |
|------|------|------------|
| 手写 C++/CMake | 完全控制；现有 vendor SDK 与遗留核心 | 生成器 + blueprint 图；TS/XHTML 与 Lua 层无需从零重写 |
| Greenfield 重写（Rust、Go 等） | 编译期安全或新生态 | **增量成长** — 保留性能关键 C++，在旧代码旁添加 blueprint 节点与 flows |
| **Nexus** | 困在 web shell 开销与全面重写之间的团队 | 第三条路：逐步现代化创作，迁移语言前先 profiling |

---

## 本仓库包含什么

| 路径 | 角色 |
|------|------|
| [`app/`](../../app/) | Compose Desktop 客户端（`:app`）— Generate Project、blueprint/flows 编辑器 |
| [`misc/`](../../misc/) | `:core` 生成器、`:cli`、client-setup、scripts、Docker — 见 [`misc/` 文件夹](#misc-文件夹) |
| [`template/`](../../template/) | desktop-app · android-app · shared — 复制到 `builds/framework/<名称>/` |
| [`builds/`](../../builds/) | 客户端产物 → `builds/client/` · 生成应用 → `builds/framework/` |
| [`docs/`](../../docs/) | 文档中心 → [docs/README.md](../../docs/README.md) |

这是 **Framework** monorepo（`:app`、`:core`、`:cli`）。不是独立的 [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) 仓库（那里的 `:client-desktop` 向导）。

---

## 快速开始

**1. 首次设置** — 安装 JDK 26 + Git（每台机器一次）：

| 平台 | 设置 | 环境 |
|------|------|------|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

详情：[misc/client-setup/README.md](../client-setup/README.md)。

**2. 运行客户端**

```bash
source misc/client-setup/env.sh
./gradlew :app:run
```

**3. 生成项目**

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

或在 Compose UI 中使用 **Generate Project** → **Edit blueprint** / **Edit flows**。

**4. 构建生成的应用**

```bash
cd template/desktop-app && cmake --preset debug && cmake --build --preset debug
# 生成后输出也会进入 builds/framework/<名称>/
```

**5. 阅读文档** — [docs/README.md](../../docs/README.md) · [coding-with-nexus](../../docs/guides/coding-with-nexus.md) · [generation-pipeline](../../docs/guides/generation-pipeline.md)

编译并测试生成器：`./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

部署客户端：`./gradlew :app:deployToBuildsClient` → [builds/client/app/](../../builds/client/app/)

---

## 架构

### 全栈架构
*Compose 客户端 → `:core` 生成流程 → SDL3 runtime（你的应用，而非浏览器标签页）*

![Nexus 全栈架构](../../docs/assets/diagrams/full-stack-architecture.svg)

`:app` 客户端编写 blueprint 与 flows JSON；`:core` 验证并将 template 物化到 `builds/framework/<名称>/`。生成的应用以原生 SDL3 二进制运行，含 ImGui、Lua 与可选 Python。

### 生成与构建流程
*从 client-setup 与 Gradle 模块到 `builds/framework/<名称>/`*

![生成与构建流程](../../docs/assets/diagrams/generation-builds-flow.svg)

JDK 26 设置 → Gradle `:core` / `:cli` / `:app` → `ProjectGenerator` 在 `builds/` 下写入 CMake/Gradle 树。

### 桌面 vs Android runtime
*SDL3/ImGui 上共享 MVC；pybind11 vs Chaquopy + Djinni*

![桌面 vs Android runtime](../../docs/assets/diagrams/desktop-vs-android-runtime.svg)

一份 `blueprint.json` 在两个 template 上连线 MVC；仅 Python 桥接与打包因平台而异。

层级参考：[docs/architecture/overview.md](../../docs/architecture/overview.md) · Blueprint/flows：[Blueprint 与 flows](#blueprint-与-flows--两层) · Python：[桌面 vs Android 上的 Python](#桌面-vs-android-上的-python)

---

## Blueprint 与 flows — 两层

Nexus 将**构建时应用结构**与**应用内运行的自动化**分开。单个 Langflow 画布可在翻译后拆成两个文件。

### Blueprint vs flows — 两层
*构建时结构 vs 可选应用内自动化*

![blueprint.json vs flows.json 两层模型](../../docs/assets/diagrams/blueprint-vs-flows-layers.svg)

`blueprint.json` 连线 MVC 结构，由 `:core` 一次性消费；`flows.json` 注册 in-process 触发器，启动时由 FlowRunner 加载。

### 应用结构（`blueprint.json`）

项目根部的构建时图。节点声明模块；边连接生成 MVC 应用中的数据与命令流。

| 节点类型 | 角色 |
|----------|------|
| `python.module` | Python 采样 / 分析（`python/functions.py`） |
| `cpp.model` | C++ 领域状态（`FunctionRegistry`、缓存） |
| `cpp.controller` | 命令 + 连线（`PlotController`） |
| `ui.page` | TS/XHTML 页面（`ui/ui.ts`、`ui/ui.xhtml`） |
| `lua.script` | 运行时 Lua 面板（`scripts/panels.lua`） |

**在客户端编辑：** `./gradlew :app:run` → **Generate Project** → **Edit blueprint**（v1 为 Compose 画布 + JSON 检查器；v1.1 计划原生 **imnodes** 面板 — 同一 schema）。

示例：[template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](../../template/android-app/blueprint.json) · Schema：[docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)

#### Langflow 风格示例

Nexus 在构建时镜像的视觉模式参考图（非 runtime）：

- [RAG chatbot flow](../../docs/assets/examples/langflow-rag-chatbot.svg) — Langflow runtime；将模块映射到 blueprint 节点类型
- [Agent with tools](../../docs/assets/examples/langflow-agent-tools.svg) — agent 循环 → `python.module`、`cpp.controller` 等
- [Nexus blueprint app structure](../../docs/assets/examples/nexus-blueprint-app-structure.svg) — 构建时 MVC codegen

### 应用内自动化（`flows.json`）

可选 runtime 服务 — 后台循环、事件触发、计划任务。

| 模式 | 何时运行 | 触发示例 |
|------|----------|----------|
| `background` | 应用存活期间 | 每 5000 ms 的 `interval` |
| `triggered` | 仅满足条件时 | `event` `curve.added`、`startup`、`manual` |

**在客户端编辑：** `./gradlew :app:run` → **Generate Project** → **Edit flows** — 列出 flows、启用/禁用、JSON 预览（v1.1 可视化编辑器）。Schema：[docs/templates/flows-schema.md](../../docs/templates/flows-schema.md)

在 `flows` 数组中追加多个 flow（每个需唯一 `id`）。全局禁用：`nxs_config.json` → `"flows": { "enabled": false }`；单个 flow：`"enabled": false`。

示例：[template/desktop-app/flows/flows.json](../../template/desktop-app/flows/flows.json)

### 导入 Langflow 流程

[Langflow](https://github.com/langflow-ai/langflow) 是可选的**外部创作工具**。导出 flow JSON 并采用为原生 Nexus 服务 — **不是**在交付的应用内运行 Langflow。

**步骤 1 — 从 Langflow 导出**

1. 在 Langflow 中构建可视化 flow（LLM、Prompt、Tool、Retriever、Agent 等）。
2. 通过 **Export flow** 或 Langflow API（`/api/v1/flows/{id}`）导出 JSON。字段名与嵌套**与 Nexus schema 不同**；将导出视为设计产物，而非即插即用文件。

**步骤 2 — 映射到 Nexus**

| Langflow 概念 | Nexus 目标 |
|---------------|------------|
| 应用结构组件 | [`blueprint.json`](#应用结构-blueprintjson) 节点与 MVC 端口 |
| 自动化组件（LLM、Tool、Agent 等） | `flows.json` → `steps[]`，`type: invoke` → `nxs.*`、`python.*`、`lua.*` |
| 边 / 执行顺序 | 有序 `steps` 数组；v1.1 通过 `condition` 分支 |
| 触发（chat、webhook、计划） | `trigger.type`：`event`、`interval`、`startup`、`manual`、`hotkey` |
| 长期循环 | `mode: background` |
| 按需运行 | `mode: triggered` |

**步骤 3 — 交付到项目**

![Langflow 导出到 flows.json 采用流程](../../docs/assets/diagrams/langflow-adoption-workflow.svg)

1. **翻译**导出为 [flows schema](../../docs/templates/flows-schema.md)（v1 手动；v1.1 importer）。
2. **放置**于 `flows/flows.json` 或在客户端 **Edit flows** 中粘贴。
3. **启用**于 `nxs_config.json` → `"flows": { "enabled": true }`。FlowRunner 在启动时注册触发器。

> [!NOTE]
> **v1 诚实限制：** 无自动 Langflow importer；无捆绑 Langflow runtime；LLM 节点变为 `invoke` stub（模型调用在 `python.module`）。Flows 为**本地 in-process** — 非云端 webhook 集成。HTTP/webhook 步骤类型计划 v1.1。

### flows 采用路径

三种采用 runtime flows 的方式 — 选择适合你应用的权重：

1. 🚫 **无 flows** — 省略或禁用；starter 无需 FlowRunner 即可工作
2. 🔧 **flows 作辅助** — 较大应用内的小型自动化服务（定时器、事件钩子）
3. 🔀 **混合** — Blueprint MVC + 同一二进制中的 background/triggered flows

---

## 构建你的应用

Nexus 面向**原生、数据密集、现场部署的工具** — 交易台、CAD 查看器、科学可视化、游戏开发工具、音频/DSP 工作台、机器人面板与 Android 现场平板。默认 template：通用 starter（hello + counter）。`examples/plotter/` 中可选 **Desmos 风格 plotter**。

### Templates（桌面与 Android）

| Template | Stack | 指南 |
|----------|-------|------|
| `desktop-app` | SDL3 + ImGui + pybind11 + sol2 | [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md) |
| `android-app` | SDL3/GLES + Chaquopy + Djinni | [docs/templates/android-app.md](../../docs/templates/android-app.md) |

输出：`builds/framework/<名称>/` · 布局：[builds/README.md](../../builds/README.md) · [template/README.md](../../template/README.md)

### 桌面 vs Android 上的 Python

`blueprint.json` 中相同的 `python.module` 节点在**两个** template 上连线曲线采样 — 仅 Python 设置、打包与 C++↔Python 边界不同。

| | **桌面** | **Android** |
|---|----------|-------------|
| **内置 Python** | pybind11 — 原生进程内 CPython | JVM 上 Chaquopy；Djinni `ChaquopyPythonBridge` |
| **源码树** | `python/`（如 `functions.py`） | `app/src/main/python/` |
| **归档** | `misc/python.dat`（PYAC），CMake `pack_python_dat` | **无** — Gradle/Chaquopy 将 `.py` 打入 APK |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **典型重建** | `cmake --build`（刷新 `python.dat`） | `./gradlew :app:assembleDebug` |

![Python 桌面 vs Android 嵌入流程](../../docs/assets/diagrams/python-desktop-vs-android-flow.svg)

*相同 `python.module` evaluate 端口 — 各平台不同 pack 与 bridge。*

指南：[template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md)

### TypeScript + XHTML UI

两层 UI 创作均降至同一 ImGui/Lua API — 均不使用浏览器引擎。

**命令式 Lua**（`panels.lua`）— 最底层；`nxs.register_panel(...)` 与 `ui.button`、热键；可选 `lua.dat` 热重载。

**声明式 TS/XHTML**（`ui/ui.xhtml` + `ui/ui.ts`）— markup 与 TypeScript 降至 Lua 面板定义。[`template/shared/dsl/`](../../template/shared/dsl/) 将标签（`window`、`panel`、`plot`、`node-editor` 等）映射为 Dear ImGui、ImPlot、imnodes 调用。

| 机制 | TS/XHTML | 降至 |
|------|----------|------|
| `ui.ts` 中 `state()` | `<slider>` 上 `bind="sampleCount"` | ImGui 控件双向状态 |
| `ui.ts` 中 `native()` | `items-source="activeCurves"` | C++ model 只读投影 |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | 与 Lua 直接调用的相同 `nxs.*` 命令 |

从这里开始：[template/desktop-app/ui/ui.xhtml](../../template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

### Lua 脚本与可选 flows

- **Lua** — 通过 sol2 的运行时面板与热键；编辑 `scripts/panels.lua`，重建打包 `lua.dat`
- **Flows** — 可选 `flows.json` 服务；见 [应用内自动化](#应用内自动化-flowsjson) 与 [导入 Langflow 流程](#导入-langflow-流程)

### 谁学得最快

| 角色 | 从这里开始 |
|------|------------|
| 游戏开发者（ImGui overlay） | `scripts/panels.lua` → 热键与快速添加按钮 |
| C++ 工程师 | `src/model/` + `src/controller/` → 扩展 `FunctionRegistry` |
| Web 开发者 | `ui/ui.xhtml` + `ui/ui.ts` → 添加面板与 handler |
| Python 分析师 | `python/functions.py` → 新曲线采样 |
| Android 开发者 | 生成 `android-app` → 追踪 Djinni bridge |

完整指南：[docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

<details>
<summary><strong>何时其他技术栈可能更合适</strong></summary>

| 情况 | 可考虑 |
|------|--------|
| 纯 Web 团队，不愿碰 C++/CMake | Electron 或 Tauri |
| 像素级营销 UI | Web 或带布局引擎的原生 UI 工具包 |
| 今天从此 repo 出 iOS | 尚未交付 — 等待 v1 iOS template |
| 全新安全关键项目 | Rust — 见 [现代 C++](#现代-c-与无需重写即可成长) |

</details>

---

## `misc/` 文件夹

`misc/` 汇总 **Framework 仓库工具** — Gradle 模块、约定插件、首次运行设置、容器镜像、CI 说明与辅助脚本。这些均不随生成的原生应用交付；仅用于构建与运行项目生成器。

| 路径 | 角色 |
|------|------|
| [misc/core/](../core/) | `:core` — `ProjectGenerator`、`TemplateEngine`、`nxs_config.json` schema（v2） |
| [misc/cli/](../cli/) | `:cli` — 无头 `generate` 命令 |
| [misc/build-logic/](../build-logic/) | Included build — JVM toolchain 26、约定插件 |
| [misc/client-setup/](../client-setup/) | 首次运行安装器（JDK 26 + Git） |
| [misc/scripts/](../scripts/) | [dev/](../scripts/dev/) · [test-gen/](../scripts/test-gen/) · [generate-diagrams/](../scripts/generate-diagrams/) |
| [misc/docker/](../docker/) | 容器化生成 |
| [misc/jenkins/](../jenkins/) | 可选 Jenkins CI |
| [misc/translations/](README.md) | 本地化 README — [pt-BR](README.pt-BR.md) · [es](README.es.md) · [de](README.de.md) · [ru](README.ru.md) · [zh-CN](README.zh-CN.md) |

Gradle 通过 [settings.gradle.kts](../../settings.gradle.kts) 将 `:core` 与 `:cli` 映射自 `misc/`。Hub：[misc/README.md](../README.md) · Pipeline：[docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md)

**test-gen** 为 `builds/framework/<project>/` 下的应用写入 smoke/instrumented 测试 stub（非生成器本身）。入口：`./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp` — 见 [misc/scripts/test-gen/README.md](../scripts/test-gen/README.md)。

---

## 添加依赖

在 [client-setup](../client-setup/README.md) 与 **Generate Project** 之后，在 `builds/framework/<ProjectName>/` 下的**生成应用**中添加原生依赖 — 而非 Compose 生成器模块。

- **C++** — 用 `FetchContent` 或 vcpkg 扩展 `CMakeLists.txt`；`cmake --build --preset debug` 重建
- **Python** — 桌面：`pip install`，编辑 `python/`，重建；Android：在 `app/build.gradle.kts` 中 Chaquopy `pip { install(...) }`
- **Lua** — 将 `.lua` 放入 `scripts/`，从 `panels.lua` `require`；重建打包 `lua.dat`

完整 walkthrough：**[docs/guides/adding-dependencies.md](../../docs/guides/adding-dependencies.md)**

---

## 现代 C++ 与无需重写即可成长

生成的项目使用 **C++20**、RAII 模式、CMake presets 与 clang-format。Rust 在编译期安全保证上仍占优 — 对已有 C++ 库（CAD 内核、编解码器、交易所 API）与 ImGui/SDL3 依赖的团队是诚实权衡。

**逐步成长，而非从零重写。** 新 blueprint 节点、runtime flows 与 XHTML 创作屏幕可与旧 Lua 脚本、定制 C++ 模块共存于同一进程。困在 Electron 或 Tauri 的团队常面临分叉：接受 web shell 开销或全面重写。Nexus 提供第三条路 — 保留已投入的性能关键 C++，逐步现代化创作，迁移语言前先 profiling。

> *"Make it work, make it right, make it fast — in that order."* — 常归于 Kent Beck

---

## 超越快速修复自动化

**Power Automate**、**n8n** 及类似工具擅长 ops 胶水。当快速修复*就是*产品时便失效：无原生 UI、弱离线打包、云依赖。

Nexus 保留节点-边思维模型，但生成**真实原生应用** — C++/SDL3、Lua/Python、ImGui + TS/XHTML、脚本包、桌面/Android 程序。逐工具对比见 [Nexus 如何比较](#nexus-如何比较)。

**迁移路径：** 在 blueprint 编辑器中连线模块 → 用 `:cli` 或 **Generate Project** 生成 → 在代码层迭代，而非堆叠 flow 补丁。n8n webhook 可留在边缘做 ops 胶水，应用 in-process 拥有状态、UI 与离线行为。

| 领域 | Flow 工具（典型） | Nexus 产出 |
|------|-------------------|------------|
| **Runtime** | 服务端 step engine | 桌面/移动应用或 Android APK |
| **Offline / 现场** | 需连接 workflow 主机 | Offline-first SDL3 应用 |
| **Performance** | 步骤间 HTTP 往返 | In-process C++；Python/numpy |
| **UI 表面** | 厂商 dashboard 或无 | ImGui + DSL 页面 |
| **Cross-platform** | 各目标单独集成 | 一份 `blueprint.json` 连线 desktop + Android |

---

## 开发状态

**今日已交付：**

- `:app` — Counter + Generate Project + Blueprint Editor + Flows Editor
- `:core` / `:cli` — template 生成 + `BlueprintValidator` + `FlowsValidator`
- `template/*` — desktop + Android，含 `blueprint.json` + 可选 `flows.json`
- 脚本归档包 — `lua.dat` / `python.dat`（桌面），Android APK 中 `lua.dat`
- `builds/`、`misc/client-setup/`、`docs/`

**限制（v1）：** 仅 Compose Desktop 生成器；ImGui 美学偏实用；Chaquopy 增大 APK；此工具链今天尚无 iOS。

**Branch：** 在 **`main`**（`origin/main`）上活跃开发。

MVP 前剩余工作：[通往 MVP](#通往-mvp)。

---

## 版权与许可

> [!IMPORTANT]
> **Apache License 2.0** — 允许商业使用、修改与分发。再分发时保留版权声明与 [LICENSE](../../LICENSE) 文件。生成的应用代码归你；从 template 复制的片段应保留 Apache 声明。

- © 2026 Nexus Framework contributors — Nexus Framework Client 与捆绑 templates/docs
- **生成的项目：** 你拥有生成器写出的应用代码；从 Nexus template 复制的部分应在出现处保留 Apache 2.0 声明

完整许可文本：[Apache License 2.0](../../LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

## 参见

*Blueprint 你的应用，生成目录树，交付二进制 — 然后在真实代码层迭代。*

### 文档

| 文档 | 说明 |
|------|------|
| [docs/README.md](../../docs/README.md) | 文档中心 |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | UI、MVC、Python、Lua、主题 |
| [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md) | ProjectGenerator、CLI、Docker |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | `blueprint.json` schema |
| [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md) | `flows.json` schema |
| [AGENTS.md](../../AGENTS.md) | 编码助手构建命令 |

### 生态

| 技术 | 角色 |
|------|------|
| [SDL3](https://www.libsdl.org/) | 窗口、输入、GPU 表面 |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | 即时模式 UI 与图表 |
| [sol2](https://github.com/ThePhD/sol2) / [pybind11](https://pybind11.readthedocs.io/) | C++ 中的 Lua 与 Python |
| [Chaquopy](https://chaquo.com/chaquopy/) / [Djinni](https://github.com/dropbox/djinni) | Android 上 Python 与 Kotlin bridge |
| [Langflow](https://github.com/langflow-ai/langflow) / [n8n](https://n8n.io/) | 可选外部创作（导入 Nexus） |

| 仓库 | 角色 |
|------|------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | 独立的 `:client-desktop` 向导分发 |

---

## 通往 MVP

当每行均为 ✅ 时，Nexus Framework **MVP 就绪**：生成原生应用、编辑 blueprints/flows、写出项目并交付有文档的 desktop/Android build。

### 客户端与项目生成器

| 项 | 状态 |
|----|------|
| 从 templates 生成 desktop + Android | ✅ |
| Blueprint 编辑器（Compose） | ✅ |
| Flows 编辑器 UI（列表、启用/禁用、JSON 预览） | ✅ |
| ProjectGenerator + validators | ✅ |
| Compose 六步向导*（v1 交付两屏 Generate + 编辑器）* | ⬜ |

### Templates

| 项 | 状态 |
|----|------|
| 通用 desktop + Android templates | ✅ |
| `blueprint.json` + 可选 `flows.json` 结构 | ✅ |
| TS/XHTML DSL stubs、Lua、Python 路径 | ✅ |
| CI 中验证端到端 desktop 应用 build | ⬜ |
| CI 中验证端到端 Android APK build | ⬜ |

### Runtime / 生成的应用

| 项 | 状态 |
|----|------|
| `python.dat` / `lua.dat` pack 一致性 | ✅ |
| 生成应用中 desktop pybind11 完全连线（Phase 2） | ⬜ |
| Android Chaquopy bridge 在设备上 E2E 测试 | ⬜ |
| TS/XHTML → Lua lowering 编译器*（手动 `panels.lua` 路径已文档化）* | ⬜ |

### 文档与开发者体验

| 项 | 状态 |
|----|------|
| README 架构与对比章节 | ✅ |
| Template `AGENTS.md` 指南 | ✅ |
| `client-setup` 脚本（JDK 26） | ✅ |
| CI 中 CLI `debug validate --all` 或等价物 | ⬜ |

### Release

| 项 | 状态 |
|----|------|
| `main` 上 CI 通过 | ⬜ |
| 发布客户端二进制（`builds/client/`） | ⬜ |
| 版本标签 `v1.0.0` | ⬜ |

<details>
<summary><strong>MVP 后路线图（v1.1+）— 点击展开</strong></summary>

| 项 | 说明 |
|----|------|
| imnodes 原生 blueprint 面板 | 相同 `blueprint.json` schema |
| 可视化 flows 画布编辑器 | — |
| Langflow JSON importer | v1 手动翻译 |
| 远程 template 目录 · iOS template | — |
| `flows.json` 中 HTTP/webhook 步骤类型 | — |

</details>

