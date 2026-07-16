<!--
  description: Nexus Framework 从可视化蓝图中生成本地 C++/Lua/Python 桌面和 Android 应用。无需 Electron，无需云服务，无需浏览器——只有一个你掌控的编译二进制文件。SDL3 + ImGui + Zig 0.14 构建系统。
  keywords: 原生应用生成器, C++ 框架, C++20 模块, Lua 脚本, 嵌入式 Python, SDL3, ImGui, Zig 构建, Zig JNI, Compose Desktop, 蓝图驱动开发, 跨平台桌面, Android 原生应用, 无 Electron, 项目生成器, 基于图的架构, arena 分配器, pybind11, sol2, Chaquopy
-->
# Nexus Framework — 原生应用生成器：从可视化蓝图生成 C++ + Lua + Python 应用

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>🧩 像画图一样设计应用。获得一个编译好的原生二进制文件。无需浏览器。无需 Electron。无需云服务。</strong></p>

---

## 快速开始

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name 我的应用"
cd builds/framework/我的应用 && zig build
```

## 文档

| 文档 | 内容 |
|----:|:-----|
| [docs/hub.md](../docs/hub.md) | 文档中心 |
| [docs/architecture/overview.md](../docs/architecture/overview.md) | 架构、语言栈、流水线、模板 |
| [docs/guides/coding-with-nexus.md](../docs/guides/coding-with-nexus.md) | UI、MVC、Python、Lua、主题 |
| [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md) | `blueprint.json` + `flows.json` 参考 |

## 许可证

Apache 2.0 — 参见 [LICENSE](../LICENSE)。

*设计应用为图，生成项目树，交付二进制文件——然后在真实代码层中迭代。*
