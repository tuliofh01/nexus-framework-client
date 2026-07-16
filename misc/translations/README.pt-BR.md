<!--
  description: O Nexus Framework gera aplicativos nativos desktop e Android em C++/Lua/Python a partir de blueprints visuais. Sem Electron, sem nuvem, sem navegador — apenas um binário compilado que você controla. SDL3 + ImGui + Zig 0.14.
  keywords: gerador de aplicativos nativos, framework C++, módulos C++20, scripting Lua, Python embarcado, SDL3, ImGui, Zig build, Zig JNI, Compose Desktop, desenvolvimento orientado a blueprint, desktop multiplataforma, app Android nativo, sem Electron, gerador de projetos, arquitetura baseada em grafo, arena allocator, pybind11, sol2, Chaquopy
-->
# Nexus Framework — Gerador de Apps Nativos: C++ + Lua + Python a partir de Blueprints Visuais

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework — Native C++ Lua Python Project Generator" width="240" />
</p>

<p align="center"><strong>🧩 Desenhe seu app como um grafo. Obtenha um binário nativo compilado. Sem navegador. Sem Electron. Sem nuvem.</strong></p>

<p align="center"><em>Simples quando você quer. Poderoso quando você precisa.</em></p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache License 2.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.14-orange?style=flat-square&logo=zig" alt="Zig 0.14" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.1-blueviolet?style=flat-square" alt="Versão 1.0.1" /></a>
</p>

> **🚀 Do zero ao binário em cinco minutos**
> `zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh && ./gradlew :app:run`
> Sem Chrome. Sem Docker. Sem npm install. Apenas um binário SDL3 nativo.

---

## Índice

- [O que é o Nexus?](#o-que-é-o-nexus)
- [Por que nativo é importante](#por-que-nativo-é-importante)
- [O que você pode construir](#o-que-você-pode-construir)
- [C++ no centro — todo o resto é ferramenta](#c-no-centro--todo-o-resto-é-ferramenta)
- [Arquitetura](#arquitetura)
- [Pipeline de geração](#pipeline-de-geração)
- [Blueprint & flows](#blueprint--flows)
- [Nexus vs as alternativas](#nexus-vs-as-alternativas)
- [Performance](#performance)
- [Início rápido](#início-rápido)
- [C++20 moderno](#c20-moderno)
- [A história do Zig](#a-história-do-zig)
- [Contribuindo](#contribuindo)
- [Licença](#licença)

---

## O que é o Nexus?

**Nexus transforma um diagrama visual em um binário nativo compilado.** Você desenha a arquitetura do seu app como um grafo — adicione uma página UI, insira um módulo Python, conecte um script Lua — e o Nexus gera uma árvore de projeto completa e compilável. Sem CMake manual. Sem pontes de linguagem. Sem Electron.

- **3–20 MB** de binário — sem Chromium, sem Node.js
- **Inicialização em < 200 ms** direto para um frame ImGui interativo
- **15–40 MB de RAM em idle**
- **Offline por padrão** — sem telemetria, sem dependência de nuvem
- **Cross-compilável** do Linux para Windows em um comando Zig

---

## Por que nativo é importante

| O que importa | Electron / Tauri / browser shell | Nexus nativo |
|:--------------|:----------------------------------|:-------------|
| **Tamanho da instalação** | 120–200 MB (Electron) / 5–15 MB (Tauri) | **3–20 MB** |
| **Inicialização** | 2–8 s (Electron) / 1–2 s (Tauri) | **< 200 ms** |
| **RAM em idle** | 150–500 MB (Electron) / 50–100 MB (Tauri) | **15–40 MB** |
| **Acesso GPU** | WebGL limitado / WebView mediado | **Vulkan / GLES / Metal** |
| **Sistema de arquivos** | Sandboxado, assíncrono | **POSIX / Win32 direto** |
| **Offline** | Cache-manifest | **Sempre offline** |
| **Determinismo de build** | npm / cargo roulette | **Toolchain fixa** |

---

## O que você pode construir

| Seu objetivo... | O que você obtém |
|:----------------|:-----------------|
| **Plotar gráficos com Lua scripting** | Modelo C++20 + ImPlot + console Lua + FFT Python — um binário |
| **App Android com ML offline** | SDL3/GLES + Chaquopy + Zig JNI para sensores nativos |
| **Dashboard hot-reload** | Nós blueprint por painel + `flows.json` + Lua em runtime |
| **App desktop com UI web-style (nativa)** | XHTML + TypeScript que viram chamadas ImGui |
| **C++ + Python no mesmo processo** | pybind11 — sem IPC, sem serialização, sem cópia numpy |
| **Cross-compile Linux para Windows no CI** | `zig build -Dtarget=x86_64-windows` |

> 📦 **Veja você mesmo:** [`builds/framework/PlotterApp/`](../../builds/framework/PlotterApp/) é um projeto real gerado por este repositório. Abra, leia o README, execute `zig build`.

---

## 🧱 C++ no centro — todo o resto é ferramenta

O app gerado é um **programa C++**. Não é um programa Lua com bindings C, nem um script Python com módulos nativos. O loop principal, o modelo, o dispatch do controlador — tudo em C++20.

### Por que C++ domina o MVC

| Preocupação MVC | O que C++ fornece | Por que importa |
|:---------------|:------------------|:----------------|
| **Model** — estado do domínio | `class`, RAII, `constexpr`, `[[nodiscard]]` | Lifetime determinístico. Sem pausas GC. Tipos copiáveis que cabem no cache. |
| **Controller** — dispatch de comandos | `std::function`, módulos, `std::variant` | Roteamento type-safe. Cada caminho de comando é visível em tempo de compilação. |
| **View** — renderização | SDL3 + ImGui direto, `noexcept` | Redesenho em < 0.5 ms. Sem engine de layout. Sem DOM diffing. |

### O toolchain ao redor do C++

| Ferramenta | Papel | Como serve o C++ |
|:-----------|:------|:-----------------|
| **Zig** | Orquestrador de build | `zig c++` compila módulos C++20. `build.zig.zon` fixa dependências. CMake removido. |
| **Lua** | Scripting ao vivo | Métodos `sol2` em objetos C++. Edite `panels.lua`, recarregue — sem recompilar. |
| **Python** | Análise de dados | Módulos pybind11 expõem buffers C++ ao NumPy sem cópia. |
| **TypeScript/XHTML** | UI declarativa | Marcação XML + bindings TS viram chamadas `ImGui` em tempo de build. |

---

## Arquitetura

Três camadas: você **autor** um grafo no cliente Compose Desktop, o **gerador** cria uma árvore de projeto nativa, e o **runtime** executa.

![Arquitetura Nexus — contexto do sistema com atores e componentes](../../docs/assets/diagrams/full-stack-architecture.svg)

### Pilha tecnológica — o que cada camada possui

![Pilha Nexus — Application, Framework, Build, Target](../../docs/assets/diagrams/cmake-to-zig-migration.svg)

### Ponte entre linguagens

Todas as linguagens se comunicam em processo pelo NexusBridge — sem IPC, sem serialização:

![Ponte multi-linguagem — C++, Lua, Python, Zig, TS no mesmo processo](../../docs/assets/diagrams/cross-language-bridge.svg)

---

## Pipeline de geração

O `ProjectGenerator` lê um `blueprint.json`, valida contra o schema v2, e materializa os arquivos do template em uma árvore de projeto completa. Cada execução é determinística.

![Pipeline de geração — casos de uso do design ao build](../../docs/assets/diagrams/generation-builds-flow.svg)

### Estrutura do projeto gerado — desktop vs android

Ambos os templates compartilham o mesmo núcleo C++20 MVC, mas diferem no embedding Python e nas ferramentas de build:

![Árvores de template — desktop-app vs android-app vs shared](../../docs/assets/diagrams/langflow-adoption-workflow.svg)

---

## Início rápido

```bash
# 1. Bootstrap (uma vez) — instala JDK 26 + Zig 0.14.0
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh

# 2. Inicie o cliente Compose Desktop
./gradlew :app:run

# 3. Ou gere via CLI
./gradlew :cli:run --args="generate --type desktop --name MeuApp"

# 4. Compile o app gerado
cd builds/framework/MeuApp && zig build
```

---

## C++20 moderno

O código gerado usa módulos C++20 (`.cppm`) — sem headers, sem macros vazando. Cada módulo é um arquivo autossuficiente com sua própria documentação. **22 módulos** no total, entre runtime compartilhado e templates.

Recursos: `[[nodiscard]]`, `constexpr`, `noexcept`, retornos trailing, `std::unique_ptr` com deleters personalizados para recursos SDL, `std::ranges` para transformações de dados.

---

## A história do Zig

O Zig substituiu completamente CMake + Ninja + NDK + Djinni. Um único `zig build` compila, linka e empacota. O Android JNI foi reescrito de 8 arquivos C++ e Djinni para 1 arquivo Zig puro com 5 exports C ABI.

---

## Nexus vs as alternativas

### vs Electron, Tauri, Flutter

| | Electron | Tauri | Flutter | **Nexus** |
|:--|:---------|:------|:--------|:----------|
| **Runtime** | Chromium + Node.js | WebView + Rust | Dart + Skia | **C++20 + SDL3** |
| **Tamanho** | 120–200 MB | 5–15 MB | 15–50 MB | **3–20 MB** |
| **RAM parado** | 150–500 MB | 50–100 MB | 50–100 MB | **15–40 MB** |
| **Inicialização** | 2–8 s | 1–2 s | 1–2 s | **< 200 ms** |
| **Scripting** | Node.js | Rust commands | Dart isolates | **Lua + Python** |
| **Codegen** | — | — | — | **Blueprint-driven** |
| **Offline** | Parcial | Parcial | Completo | **Completo** |

### vs n8n, Langflow

Nexus **não** é um motor de workflow. n8n conecta APIs. Langflow conecta LLMs. Nexus conecta modelos C++, módulos Python e scripts Lua em um **aplicativo compilado**.

---

## Performance

| Métrica | O que esperar |
|:--------|:--------------|
| **Tamanho do binário** | 3–20 MB |
| **RAM em idle** | 15–40 MB |
| **RAM sob carga** | 50–150 MB (com Python + NumPy) |
| **Inicialização** | < 200 ms |
| **Redesenho UI** | < 0.5 ms por frame |

---

## Contribuindo

| Área | Envolve | Nível |
|:-----|:--------|:------|
| `:core` pipeline | Kotlin — generator, template engine | Kotlin intermediário |
| `:app` client | Compose Desktop — editor de blueprint, flows | Compose Desktop UI |
| Geração C++ | Módulos C++20 — modelos, runtime | C++20 intermediário |
| Zig services | Build, alocador, bridge JNI | Zig iniciante |

---

## Licença

Apache 2.0 — veja o arquivo [LICENSE](../../LICENSE).

*Desenhe seu app como um grafo, gere a árvore, entregue o binário — então itere em camadas de código reais.*
