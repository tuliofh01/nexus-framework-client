<!--
  description: O Nexus Framework gera aplicativos nativos desktop e Android em C++/Lua/Python a partir de blueprints visuais. Sem Electron, sem nuvem, sem navegador — apenas um binário compilado que você controla. SDL3 + ImGui + Zig 0.14.
  keywords: gerador de aplicativos nativos, framework C++, módulos C++20, scripting Lua, Python embarcado, SDL3, ImGui, Zig build, Zig JNI, Compose Desktop, desenvolvimento orientado a blueprint, desktop multiplataforma, app Android nativo, sem Electron, gerador de projetos, arquitetura baseada em grafo, arena allocator, pybind11, sol2, Chaquopy
-->
# Nexus Framework — Gerador de Apps Nativos: C++ + Lua + Python a partir de Blueprints Visuais

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Nexus Framework — Native C++ Lua Python Project Generator" width="240" />
</p>

<p align="center"><strong>🧩 Desenhe seu app como um grafo. Obtenha um binário nativo compilado. Sem navegador. Sem Electron. Sem nuvem.</strong></p>

<p align="center"><em>Simples quando você quer. Poderoso quando você precisa.</em></p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Apache License 2.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-cross--platform-green?style=flat-square" alt="SDL3 Multiplataforma" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.14-orange?style=flat-square&logo=zig" alt="Zig 0.14 Builds Nativos" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/ImGui-native_UI-green?style=flat-square" alt="Dear ImGui UI Nativa" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.1-blueviolet?style=flat-square" alt="Versão 1.0.1" /></a>
</p>

> **🚀 Do zero ao binário em cinco minutos**
> `zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh && ./gradlew :app:run`
> Sem Chrome. Sem Docker. Sem npm install. Sem dependência de nuvem.
> Apenas um binário SDL3 nativo que você控制a.

---

## Índice

- [O que é o Nexus?](#o-que-é-o-nexus)
- [Por que nativo é importante](#por-que-nativo-é-importante)
- [O que você pode construir?](#o-que-você-pode-construir)
- [Visão geral da arquitetura](#visão-geral-da-arquitetura)
- [Pipeline de geração](#pipeline-de-geração)
- [Blueprint & flows](#blueprint--flows)
- [Construindo seu app](#construindo-seu-app)
- [Nexus vs as alternativas](#nexus-vs-as-alternativas)
- [Como o progressive enhancement funciona](#como-o-progressive-enhancement-funciona)
- [Performance & footprint](#performance--footprint)
- [Início rápido](#início-rápido)
- [O workflow completo](#o-workflow-completo)
- [Para quem é isso?](#para-quem-é-isso)
- [O que o torna especial](#o-que-o-torna-especial)
- [A história do Zig](#a-história-do-zig)
- [Comunidade & contribuições](#comunidade--contribuições)
- [A pasta misc/](#a-pasta-misc)
- [Roadmap](#roadmap)
- [Docs & recursos](#docs--recursos)
- [Copyright & modelo de propriedade](#copyright--modelo-de-propriedade)
- [Veja também](#veja-também)

---

## O que é o Nexus?

**Nexus torna o desenvolvimento de apps nativos tão simples quanto desenhar um diagrama.** Você desenha a arquitetura do seu app como um grafo — adicione uma página de UI, insira um módulo Python, conecte um script Lua — e o Nexus gera uma árvore de projeto completa e compilável. Sem sistemas de build manuais. Sem pontes de linguagem. Sem lutar com CMake.

**Funciona para qualquer app, simples ou complexo:**

- **App simples** — Um contador C++ com ImGui? Gere em um comando. O template fornece janelamento SDL3, manipulação de entrada e um loop de UI funcional. Adicione sua lógica, compile, entregue. Você não precisa entender metade da stack para obter um binário funcional.
- **App complexo** — Precisa de análise Python em processo? Painéis Lua que recarregam em tempo de execução? UI TypeScript que se traduz em widgets nativos? Adicione nós ao seu blueprint, gere novamente. Cada camada se empilha sem quebrar o que veio antes. Quando você precisa da performance, ela está lá — binário de 3 MB, inicialização em 200 ms, interoperabilidade Python com zero cópia.

**O resultado é um binário nativo real:**
- **3–20 MB** — sem Chromium, sem Node.js, sem taxa de VM
- **Inicializa em menos de 200 ms** direto para um frame ImGui interativo
- **15–40 MB de RAM em idle** — deixando espaço para seus dados
- **Funciona completamente offline** — sem telemetria, sem dependência de nuvem
- **Cross-compilável** do Linux para Windows em um comando Zig

---

## Por que nativo é importante

| O que importa           | Taxa do web shell              | Nexus nativo                     |
|----------------------:|:------------------------------|:---------------------------------|
| **Tamanho da instalação** | 120–200 MB (Chromium)        | **3–20 MB** (SDL3 + seu código)  |
| **Inicialização**     | 2–8 segundos (renderizador)  | **< 200 ms** (sem sandbox)       |
| **RAM em idle**       | 150–500 MB                    | **15–40 MB**                     |
| **Acesso GPU**        | WebGL (limitado)              | **Vulkan / GLES / Metal nativo** |
| **Sistema de arquivos** | Sandbox, assíncrono, mediado | **POSIX / Win32 direto**         |
| **Offline**           | Cache-manifest dance          | **Sempre offline por padrão**    |
| **Determinismo de build** | npm roulette                | **Toolchain fixa, offline**      |

---

## Início rápido

```bash
# 1. Bootstrap (uma vez por máquina) — instala JDK 26 + Zig 0.14.0
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh

# 2. Inicie o cliente Compose Desktop
./gradlew :app:run

# 3. Gere um projeto via CLI
./gradlew :cli:run --args="generate --type desktop --name MeuApp"

# 4. Compile o app gerado
cd builds/framework/MeuApp && zig build

# 5. Edite o blueprint, edite os flows, entregue o binário
```

---

## docs & recursos

| Doc | Cobre |
|----:|:-------|
| [docs/hub.md](../docs/hub.md) | Hub de documentação |
| [docs/architecture/overview.md](../docs/architecture/overview.md) | Arquitetura, linguagens, pipeline, templates, riscos |
| [docs/guides/coding-with-nexus.md](../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas, dependências |
| [docs/templates/blueprint-schema.md](../docs/templates/blueprint-schema.md) | Referência de `blueprint.json` + `flows.json` |

---

## Licença

Apache 2.0 — veja o arquivo [LICENSE](../LICENSE).

*Desenhe seu app como um grafo, gere a árvore, entregue o binário — então itere em camadas de código reais.*
