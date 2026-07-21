<!--
  description: Nexus Framework 1.0.2 — 原生 C++/Lua/Python 应用。Home = 仪表盘。Langflow → flows.json（非 blueprint）。许可证 Nexus-1.0。
-->
# Nexus Framework — 原生应用生成器

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>像画图一样设计应用。获得原生二进制。无需浏览器、Electron 或云。</strong></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="版本 1.0.2" /></a>
</p>

<p align="center"><em>以英文 README 为准。</em></p>

## 快速开始

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./misc/build_client.sh          # 首次接受 Nexus License；CI 用 --accept-license
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name 我的应用"
# 可选：Langflow 导出 → flows.json 存根（不是 blueprint.json）
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/我的应用/flows/flows.json"
cd builds/framework/我的应用 && ./build_app.sh
```

## 仓库布局（v1.0.2）

Gradle 模块在仓库根目录：`:core`、`:cli`、`:app`。单一根 `build.gradle.kts`。工具位于 `misc/`（core/cli 源码已不在 misc 下）。

**Home** 即首页仪表盘。Blueprint / Flows 编辑器（骨架）。

## Langflow → flows

可选将兼容 Langflow 的导出 JSON 导入为 `flows.json` 存根（CLI `import-langflow`）。**不会**生成 `blueprint.json`。兼容格式；Nexus **与 Langflow 无关**。

## 许可证（Nexus-1.0）

- **非商业**使用 Toolkit 与生成的应用（个人 / 爱好 / 非商业机构）：允许，但须**注明**本仓库。
- 至 **2041-07-21**，以下情况须经 [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) **事先书面授权**：Toolkit **商业**使用；生成应用**产生收入**；在**商业机构**中使用生成应用。
- 2041-07-21 之后，上述授权限制自动到期（除非续期）；署名要求继续有效。
- **无担保。** 作者不对衍生应用的违法滥用负责。

详见 [LICENSE](../../LICENSE) · [英文 README](../../README.md)。

## 文档

| 文档 | 内容 |
|:----|:-----|
| [docs/hub.md](../../docs/hub.md) | 文档中心 |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | 架构 |
| [docs/assets/diagrams/activity-generate-pipeline.svg](../../docs/assets/diagrams/activity-generate-pipeline.svg) | 生成流水线示意图 |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | 编码指南 |

*设计为图，生成项目树，交付二进制。*
