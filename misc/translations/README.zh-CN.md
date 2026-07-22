<!--
  description: Nexus Framework 1.0.2 — 原生 C++/Lua/Python 应用生成器。Home = 仪表盘。Langflow → flows.json（非 blueprint）。许可证 Nexus-1.0。
-->
# Nexus Framework — 原生应用生成器

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>像画图一样设计应用。获得原生二进制文件。</strong></p>

<p align="center"><em>简单如你所愿。强大如你所需。有趣出乎意料。</em></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="版本 1.0.2" /></a>
</p>

> **从零到二进制**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

完整英文 README 为权威来源：[../../README.md](../../README.md)。

---

## 什么是 Nexus？

**Nexus Framework** 是一个**原生应用生成器**——我们说的是*真正的原生*，不是"包了个浏览器就叫原生"。

- 你用可视化图（称为 **blueprint**）设计应用结构
- Nexus *生成*完整的、可投产的原生项目
- 编译成小巧的二进制文件（3-20MB），启动不到 200ms
- **没有浏览器引擎。没有 JavaScript。没有"npm 什么时候变成宗教了？"**

### 适用场景

| 使用场景 | 完美？ | 原因 |
|:---|:---|:---|
| IoT 现场平板 | **绝对** | 原生性能、极小体积、离线优先 |
| 科学可视化 | **绝对** | 实时绘图、ImPlot 集成 |
| 工业控制 UI | **绝对** | SDL3 + ImGui = 响应式、确定性 UI |
| 嵌入式工程工具 | **绝对** | Zig 交叉编译、原生 C++ 工具链 |
| 桌面工具 | **是** | 小二进制、快速启动、无臃肿 |
| Android 加固设备 | **是** | Zig JNI、Chaquopy、支持 Android 8.0+ |
| AI/ML 仪表板 | **是** | Python 集成、实时数据处理 |
| iOS 应用 | **尚未** | 我们正在开发中——耐心，绝地学徒 |
| 营销网站 | **否** | 请使用 React Native 或 Flutter |
| 纯 Python 应用 | **不理想** | 除非你喜欢看脚本编译 45 分钟 |

### 心智模型

把构建应用想象成建房子：

1. **建筑师**（Compose Desktop 客户端）——你画 blueprint
2. **施工队**（ProjectGenerator）——Nexus 立即实现
3. **建材**（C++/Lua/Python 模板）——随时可用
4. **成品房**（你的原生二进制）——刷墙入住

**魔法在于：** 你不需要理解 CMake、Zig 或 SDL3 如何工作。Nexus 抽象了所有这些。

---

## 快速开始

```bash
# 1. 引导安装（安装 JDK 26 + Zig 0.16.0）：
zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh

# 2. 编译生成器：
./misc/build_client.sh

# 3. 生成桌面应用：
./gradlew :cli:run --args="generate --type desktop --name MyApp"

# 4. 编译二进制：
cd builds/framework/MyApp && ./build_app.sh

# 5. 运行：
./MyApp
```

**结果：** 3-20MB 二进制文件，172ms 启动，42MB 内存，离线运行。

---

## 模板：桌面 vs Android

### 桌面（SDL3 + ImGui + C++20+）

| 组件 | 功能 | 重要性 |
|:---|:---|:---|
| **SDL3** | 窗口、GPU、输入 | 真正可用的跨平台 |
| **Dear ImGui** | 原生 immediate-mode UI | Unity Editor 同款库 |
| **Lua 5.4** | 脚本、热重载逻辑 | 200 行替代 2000 行 C++ |
| **Python 3** | AI/ML、分析 | pybind11 = C++ 速度 + Python 人体工学 |
| **Zig** | 构建系统、交叉编译 | 一条命令编译 Linux、macOS、Windows |

### Android（Zig JNI + Chaquopy）

| 组件 | 功能 | 重要性 |
|:---|:---|:---|
| **SDL3 GLES** | 移动端流畅图形 | 60fps 保证 |
| **ImGui + 原生组件** | 混合 UI | ImGui 速度 + Android 熟悉感 |
| **Lua 5.4** | 同款脚本引擎 | 写一次，到处运行 |
| **Chaquopy** | 托管 Python 运行时 | Android 对 Python 的回答 |
| **Zig JNI** | Zig  Android Java 桥接 | 无需 Djinni 样板代码 |

| 指标 | 桌面 | Android | Electron 等价物 |
|:---|:---|:---|:---|
| 二进制大小 | 3-20MB | 5-25MB | 100-500MB+ |
| 启动时间 | 170ms | 350ms | 1200ms+ |
| 内存使用 | 10-50MB | 30-100MB | 200-500MB+ |
| 离线？ | 是 | 是 | 通常需要缓存 |

---

## 语言栈

| 语言 | 角色 | 为什么选它 |
|:---|:---|:---|
| **C++20** | 热路径、模型、共享运行时 | 零开销抽象、SDL3/ImGui 原生 |
| **Lua 5.4** | 可脚本化面板、热重载逻辑 | 可嵌入、快速、200 行替代 2000 行 |
| **Python 3** | AI/ML、分析、数据科学 | pybind11 = C++ 速度 + Python 人体工学 |
| **TypeScript/XHTML** | UI DSL | 懂 HTML/CSS？你已经懂 Nexus UI |
| **Zig 0.16.0** | sidecar、分配器、JNI 桥接 | 原生 C ABI、无 libc 依赖 |

### 为什么选 C++20 而不是 Rust？

两者都很优秀。Nexus 选择 C++ 是因为整个集成生态系统本来就是 C++。不是*更好*——只是*能用*。

---

## Flows 和 UI

**Flows** 是在应用*内部*运行的自动化。不在服务器上。不在云端。没有月费。

- 通过 **Langflow** 可视化设计 → 导出 JSON → CLI 导入 → 自动运行
- **Dear ImGui** 原生 UI（Unity Editor 同款库）
- **Lua** 可脚本化组件
- **Python + Lua** AI/ML + 实时 UI

---

## Zig 和构建：秘密武器

| 任务 | 用 Zig 前 | 用 Zig 后 | 提升 |
|:---|:---|:---|:---|
| 交叉编译 | 痛苦的 CMake 配置 | `zig build` | 快 10 倍 |
| JNI 桥接 | 7 个 C++ 文件 | 1 个 Zig 文件 | 代码减少 85% |
| 构建速度 | 几分钟 | 几秒钟 | 快 10 倍 |
| 内存管理 | 手动分配器 | ZigAllocator | 无泄漏 |

---

## 许可证（Nexus-1.0）

| 使用类型 | 允许？ | 需要做什么 | 需要授权 |
|:---|:---|:---|:---|
| 个人/爱好 | 是 | 在关于页面署名 | 无 |
| 非商业 | 是 | 署名 | 无 |
| 出售你的应用 | 仅限授权后 | 署名 | 是——联系 [@tuliofh01](https://github.com/tuliofh01) |
| 企业使用 | 仅限授权后 | 署名 | 是——联系 [@tuliofh01](https://github.com/tuliofh01) |

- **授权窗口：** 2026-07-21 → 2041-07-21（15 年）
- **署名：** 在关于页面显示"Built with The Nexus Framework"
- **无担保：** 如果你的应用删除了猫的照片，我们不负责

---

## 性能保证（真实数据）

### 桌面

| 测试 | Electron 应用 | Nexus 原生 | 提升 |
|:---|:---|:---|:---|
| 启动时间 | 1240ms | 172ms | **快 7.2 倍** |
| 空闲内存 | 387MB | 42MB | **内存减少 9.2 倍** |
| 二进制大小 | 382MB | 18MB | **缩小 21.3 倍** |
| CPU（空闲） | 15% | 0.8% | **CPU 减少 18.75 倍** |
| 电池（1小时） | 12% | 1.7% | **续航延长 7 倍** |

### Android

| 测试 | React Native | Nexus 原生 | 提升 |
|:---|:---|:---|:---|
| 启动时间 | 850ms | 320ms | **快 2.7 倍** |
| 空闲内存 | 180MB | 65MB | **内存减少 2.8 倍** |
| APK 大小 | 45MB | 15MB | **缩小 3 倍** |
| 帧率（60fps） | 52fps | 60fps | **流畅 15%** |

---

## 随意对比

| 特性 | Nexus | Electron | Flutter | React Native |
|:---|:---|:---|:---|:---|
| 二进制大小 | 3-20MB | 100-500MB+ | 40-100MB | 40-100MB |
| 启动 | <200ms | 1000-2000ms | 300-800ms | 400-1000ms |
| 内存 | 10-50MB | 200-500MB+ | 50-150MB | 60-180MB |
| 离线 | 原生 | 需要缓存 | — | — |
| 最适合 | 原生性能应用 | 跨平台 Web 应用 | 跨平台 UI | 跨平台 UI |

---

## 项目结构

```
Nexus-Framework/
├── core/              # 生成引擎（Kotlin）
├── cli/               # 命令行界面
├── app/               # Compose Desktop 客户端
├── template/          # 生成的应用模板
│   ├── desktop-app/   # SDL3 + ImGui + C++ + Lua + Python
│   ├── android-app/   # Zig JNI + Chaquopy
│   └── shared/        # DSL、主题、共享工具
├── docs/              # 文档 + 23 张架构图
├── misc/              # 构建工具、脚本、CI/CD
└── builds/            # 生成的应用输出
```

---

## 术语表

| 术语 | 通俗解释 |
|:---|:---|
| **CPPM** | C++20 命名模块文件 |
| **Zig JNI** | Zig 与 Android Java 之间的桥接 |
| **Langflow** | 可视化流程设计器 |
| **Flows** | 运行时自动化 |
| **Immediate Mode GUI** | 每帧重绘一切的 UI 范式 |
| **SDL3** | 窗口 + GPU 库 |
| **ImGui** | Immediate-mode UI 库（Unity Editor 同款） |
| **sol2** | C++ Lua 绑定库 |
| **pybind11** | C++ Python 绑定库 |
| **Chaquopy** | Android 托管 Python 运行时 |

---

## 最终裁决

**是，如果：**
- 你想要**微小**的应用（3-20MB）
- 你在意**速度**（<200ms 启动）
- 你讨厌**内存膨胀**（几十 vs 几百 MB）
- 你需要**离线能力**
- 你为**桌面或 Android** 开发

**否，如果：**
- 你需要 **iOS 支持**（我们正在开发）
- 你想做**营销网站**（请用 React Native 或 Flutter）
- 你满足于 **JavaScript 生态**

**Nexus 不适合所有人——这没关系。** 我们在构建不同的东西：为关注**性能、体积和效率**的人打造的**原生应用生成器**。

*去构建些了不起的东西吧。*

---
*Nexus Framework 团队*
*使用 [The Nexus Framework](https://github.com/tuliofh01/nexus-framework-client) 构建 — Túlio Horta ([@tuliofh01](https://github.com/tuliofh01))*
