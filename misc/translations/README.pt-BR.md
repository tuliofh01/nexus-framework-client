# O Framework da Nexus Company para Desenvolvimento de Aplicações Nativas

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — logo do gerador de projetos nativo C++/Lua/Python" width="240" />
</p>

<p align="center"><strong>🧩 Apps nativos, não abas de browser</strong> — entregue binários SDL3 a partir de um grafo blueprint.</p>

<p align="center">
  🌐 <strong>Traduções:</strong>
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
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Licença open source Apache 2.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 gerador de projetos Compose Desktop" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-multiplataforma-green?style=flat-square" alt="SDL3 desktop e Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui UI immediate-mode" /></a>
</p>

> [!TIP]
> **Bem-vindo(a).** Rode o [setup inicial](#início-rápido), depois `./gradlew :app:run` — em minutos você tem o cliente Compose, o editor de blueprint e o caminho até `builds/framework/<nome>/`. Sem download de Chromium.

## Índice

- [O que é o Nexus?](#o-que-é-o-nexus)
- [Como o Nexus se compara](#como-o-nexus-se-compara)
- [O que há neste repositório](#o-que-há-neste-repositório)
- [Início rápido](#início-rápido)
- [Arquitetura](#arquitetura)
- [Blueprint e fluxos — duas camadas](#blueprint-e-fluxos--duas-camadas)
- [Construindo seu app](#construindo-seu-app)
- [A pasta `misc/`](#a-pasta-misc)
- [Adicionando dependências](#adicionando-dependências)
- [C++ moderno e crescimento sem reescrever](#c-moderno-e-crescimento-sem-reescrever)
- [Zig patching (builds nativos)](#zig-patching-builds-nativos)
- [Além da automação rápida](#além-da-automação-rápida)
- [Status de desenvolvimento](#status-de-desenvolvimento)
- [Copyright e licença](#copyright-e-licença)
- [Veja também](#veja-também)
- [Rumo ao MVP](#rumo-ao-mvp)

---

## O que é o Nexus?

**The Nexus Framework** é um **construtor de apps nativos open source**. Você descreve o app como um grafo visual — [`blueprint.json`](../../docs/templates/blueprint-schema.md) para estrutura, [`flows.json`](../../docs/templates/blueprint-schema.md) opcional para automações in-app — e o Nexus gera uma aplicação real em **C++**, **Lua** e **Python** para **desktop** (Windows, macOS, Linux) e **Android**. O cliente Kotlin Compose (`:app`) autora esses grafos; [`misc/core`](#a-pasta-misc) valida e gera projetos em [`template/`](#construindo-seu-app) com janela SDL3, widgets Dear ImGui, scripts sol2, autoria de UI TypeScript + XHTML e Python integrado (pybind11 no desktop, Chaquopy + Djinni no Android).

Isto **não** é um web-shell nem um runtime de fluxos hospedado. O Nexus entrega binários compilados — SDL3 + ImGui + ImPlot — com Lua e Python in-process. Você itera nas camadas de código normais (`cpp.model`, `python.module`, `ui.page`, painéis Lua) após a geração. Para diferenças em relação a Electron, n8n, Langflow ou começar do zero, veja [Como o Nexus se compara](#como-o-nexus-se-compara).

---

## Como o Nexus se compara

O Nexus empresta o **modelo mental de nós e arestas** de ferramentas visuais de fluxo, mas a saída é um **programa nativo** — não uma aba Chromium, não um host de workflow na nuvem e não um servidor Langflow embutido no seu app.

### vs Electron e Tauri

| Ferramenta | Ponto forte | Diferença do Nexus |
|------------|-------------|-------------------|
| [Electron](https://www.electronjs.org/) | Apps desktop web-first; DOM/CSS/React como superfície do produto | Runtime C++ nativo, binários ~3–20 MB, sem subprocesso renderer |
| [Tauri](https://tauri.app/) | UI web leve no WebView do SO + backend Rust | UI immediate-mode ImGui, superfícies GPU SDL3, pilha compartilhada desktop + Android |
| **Nexus** | Ferramentas com dados intensos, campo, sensíveis a throughput | Codegen guiado por blueprint; Python/numpy in-process; UX estilo game engine |

**Quando web shells vencem:** equipe HTML/CSS-first ou necessidade de iOS via toolchain web hoje. **Quando o Nexus vence:** refresh de UI sub-ms, binários pequenos, paridade SDL3 da mesa de trading ao tablet Android de campo — veja [Construindo seu app](#construindo-seu-app).

### vs n8n e Power Automate

| Ferramenta | Ponto forte | Diferença do Nexus |
|------------|-------------|-------------------|
| [n8n](https://n8n.io/) | Cola de ops — webhooks, cron, integrações SaaS | Gera um **app entregue** com UI nativa, comportamento offline e estado in-process |
| [Power Automate](https://www.microsoft.com/power-automate) | Automação de processos na nuvem Microsoft | Mesma UX de grafo para **fiação MVC interna**, não motores de passos externos |
| **Nexus** | Quando o fluxo rápido *é* o produto | `blueprint.json` = estrutura em tempo de build; `flows.json` opcional = serviços locais in-process |

> [!WARNING]
> **O Nexus não é n8n nem Power Automate.** Use essas ferramentas para integração SaaS na cloud. Um app gerado ainda pode chamar webhooks n8n via Python/Lua na borda.

### vs Langflow

| Ferramenta | Ponto forte | Diferença do Nexus |
|------------|-------------|-------------------|
| [Langflow](https://github.com/langflow-ai/langflow) | Autoria visual de fluxos LLM/AI em runtime | **Importar/adotar** JSON exportado em `blueprint.json` e `flows.json` — sem runtime Langflow embutido na v1 |
| **Nexus `blueprint.json`** | — | Grafo MVC em tempo de build (`python.module`, `cpp.model`, `ui.page`, …) consumido uma vez pelo `ProjectGenerator` |
| **Nexus `flows.json`** | — | Automações opcionais in-app (timers, eventos, loops em background) carregadas pelo FlowRunner na inicialização |

Grafos de estrutura → [`blueprint.json`](#estrutura-do-app-blueprintjson); automação → [`flows.json`](#automações-in-app-flowsjson). Fluxo completo de importação: [Importando procedimentos Langflow](#importando-procedimentos-langflow).

<!-- Diagrama: comparação Langflow vs n8n vs blueprint Nexus -->
![📊 Langflow vs n8n vs blueprint Nexus — passos conectados vs automação vs geração em tempo de build](../../docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

*Fluxos runtime estilo Langflow vs automação ops n8n vs codegen Nexus em tempo de build — mesmo padrão visual, modelo de execução diferente.*

### vs C++ puro / do zero

| Abordagem | Ponto forte | Diferença do Nexus |
|-----------|-------------|-------------------|
| C++/CMake manual | Controle total; SDKs de fornecedor e cores legados | Geradores + grafo blueprint; camadas TS/XHTML e Lua sem reescrever do zero |
| Rewrite greenfield (Rust, Go, …) | Segurança em compile-time ou ecossistema novo | **Crescer incrementalmente** — manter C++ crítico de performance, adicionar nós e flows ao lado do código antigo |
| **Nexus** | Times presos entre overhead de web-shell e rewrite completo | Terceiro caminho: modernizar autoria passo a passo, fazer profile antes de migrar linguagens |

---

## O que há neste repositório

| Caminho | Papel |
|---------|-------|
| [`app/`](../../app/) | Cliente Compose Desktop (`:app`) — Generate Project, editores de blueprint/fluxos |
| [`misc/`](../../misc/) | Gerador `:core`, `:cli`, client-setup, scripts, Docker — veja [A pasta `misc/`](#a-pasta-misc) |
| [`template/`](../../template/) | desktop-app · android-app · shared — copiados para `builds/framework/<nome>/` |
| [`builds/`](../../builds/) | Artefatos do cliente → `builds/client/` · apps gerados → `builds/framework/` |
| [`docs/`](../../docs/) | Hub de documentação → [docs/README.md](../../docs/README.md) |

Este é o monorepo **Framework** (`:app`, `:core`, `:cli`). Não é o repositório separado [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (wizard `:client-desktop` lá).

---

## Início rápido

**1. Setup na primeira execução** — instale JDK 26 + Git (uma vez por máquina):

| Plataforma | Setup | Ambiente |
|------------|-------|----------|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Detalhes: [misc/client-setup/README.md](../client-setup/README.md).

**2. Rodar o cliente**

```bash
source misc/client-setup/env.sh
./gradlew :app:run
```

**3. Gerar um projeto**

```bash
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Ou use **Generate Project** na UI Compose → **Edit blueprint** / **Edit flows**.

**4. Compilar o app gerado**

```bash
cd template/desktop-app && cmake --preset debug && cmake --build --preset debug
# a saída também vai para builds/framework/<nome>/ após a geração
```

**5. Ler a documentação** — [docs/README.md](../../docs/README.md) · [coding-with-nexus](../../docs/guides/coding-with-nexus.md) · [generation-pipeline](../../docs/guides/generation-pipeline.md)

Compilar e testar o gerador: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy do cliente: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](../../builds/client/app/)

---

## Arquitetura

### Arquitetura full-stack
*Cliente Compose → fluxo de geração `:core` → runtimes SDL3 (seu app, não uma aba de browser)*

![Arquitetura full-stack do Nexus](../../docs/assets/diagrams/full-stack-architecture.svg)

O cliente `:app` autora JSON de blueprint e fluxos; `:core` valida e materializa templates em `builds/framework/<nome>/`. Apps gerados rodam como binários SDL3 nativos com ImGui, Lua e Python opcional.

### Fluxo de geração e builds
*De client-setup e módulos Gradle até `builds/framework/<nome>/`*

![Fluxo de geração e builds](../../docs/assets/diagrams/generation-builds-flow.svg)

Setup JDK 26 → Gradle `:core` / `:cli` / `:app` → `ProjectGenerator` escreve árvores CMake/Gradle em `builds/`.

### Runtime Desktop vs Android
*MVC compartilhado no SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

![Runtime Desktop vs Android](../../docs/assets/diagrams/desktop-vs-android-runtime.svg)

Um `blueprint.json` liga MVC nos dois templates; só a ponte Python e o empacotamento mudam por plataforma.

Referência de camadas: [docs/architecture/overview.md](../../docs/architecture/overview.md) · Blueprint/fluxos: [Blueprint e fluxos](#blueprint-e-fluxos--duas-camadas) · Python: [Python no desktop vs Android](#python-no-desktop-vs-android)

---

## Blueprint e fluxos — duas camadas

O Nexus separa **estrutura do app em tempo de build** de **automações que rodam dentro do app**. Um único canvas Langflow pode ser dividido nos dois arquivos após a tradução.

### Blueprint vs fluxos — duas camadas
*Estrutura em tempo de build vs automações opcionais no app*

![blueprint.json vs flows.json — modelo de duas camadas](../../docs/assets/diagrams/blueprint-vs-flows-layers.svg)

O `blueprint.json` define a fiação MVC consumida uma vez pelo `:core`; o `flows.json` registra gatilhos in-process carregados pelo FlowRunner na inicialização.

### Estrutura do app (`blueprint.json`)

Grafo em tempo de build na raiz do projeto. Nós declaram módulos; arestas ligam fluxo de dados e comandos no app MVC gerado.

| Tipo de nó | Papel |
|------------|-------|
| `python.module` | Amostragem / analytics Python (`python/functions.py`) |
| `cpp.model` | Estado de domínio C++ (`FunctionRegistry`, caches) |
| `cpp.controller` | Comandos + ligação (`PlotController`) |
| `ui.page` | Página TS/XHTML (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Painéis Lua em runtime (`scripts/panels.lua`) |

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (canvas Compose + inspetor JSON na v1; painel nativo **imnodes** previsto v1.1 — mesmo schema).

Amostras: [template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](../../template/android-app/blueprint.json) · Schema: [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md)

#### Exemplos estilo Langflow

Diagramas de referência para o padrão visual que o Nexus espelha em tempo de build (não em runtime):

- [Fluxo RAG chatbot](../../docs/assets/examples/langflow-rag-chatbot.svg) — runtime Langflow; mapear módulos para tipos de nó blueprint
- [Agente com ferramentas](../../docs/assets/examples/langflow-agent-tools.svg) — loop de agente → `python.module`, `cpp.controller`, …
- [Estrutura blueprint Nexus](../../docs/assets/examples/nexus-blueprint-app-structure.svg) — codegen MVC em tempo de build

### Automações in-app (`flows.json`)

Serviços opcionais em runtime — loops em background, gatilhos por evento, agendamentos.

| Modo | Quando executa | Exemplo de gatilho |
|------|----------------|-------------------|
| `background` | Enquanto o app está vivo | `interval` a cada 5000 ms |
| `triggered` | Só na condição | `event` `curve.added`, `startup`, `manual` |

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — listar fluxos, habilitar/desabilitar, pré-visualizar JSON (editor visual v1.1). Schema: [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md).

Adicione vários fluxos no array `flows` (cada um com `id` único). Desabilite globalmente com `nxs_config.json` → `"flows": { "enabled": false }` ou por fluxo com `"enabled": false`.

Amostra: [template/desktop-app/flows/flows.json](../../template/desktop-app/flows/flows.json)

### Importando procedimentos Langflow

[Langflow](https://github.com/langflow-ai/langflow) é ferramenta **externa opcional** de autoria. Exporte JSON do fluxo e adote como serviços nativos Nexus — **não** rodando Langflow dentro do app entregue.

**Passo 1 — Exportar do Langflow**

1. Monte um fluxo visual no Langflow (LLM, Prompt, Tool, Retriever, Agent, …).
2. Exporte como JSON via **Export flow** ou API Langflow (`/api/v1/flows/{id}`). Nomes de campo e aninhamento **diferem** dos schemas Nexus; trate a exportação como artefato de design, não arquivo plug-and-play.

**Passo 2 — Mapear para o Nexus**

| Conceito Langflow | Alvo Nexus |
|-------------------|------------|
| Componentes de estrutura do app | Nós e portas MVC em [`blueprint.json`](#estrutura-do-app-blueprintjson) |
| Componentes de automação (LLM, Tool, Agent, …) | `flows.json` → `steps[]` com `type: invoke` → `nxs.*`, `python.*`, `lua.*` |
| Arestas / ordem de execução | Array `steps` ordenado; ramificações via `condition` (v1.1) |
| Gatilho (chat, webhook, agendamento) | `trigger.type`: `event`, `interval`, `startup`, `manual`, `hotkey` |
| Loop contínuo | `mode: background` |
| Execução sob demanda | `mode: triggered` |

**Passo 3 — Entregar no projeto**

![Fluxo de adoção Langflow → flows.json](../../docs/assets/diagrams/langflow-adoption-workflow.svg)

1. **Traduzir** exportação para [schema flows](../../docs/templates/blueprint-schema.md) (manual v1; importador v1.1).
2. **Colocar** em `flows/flows.json` ou colar em **Edit flows** no cliente.
3. **Habilitar** em `nxs_config.json` → `"flows": { "enabled": true }`. FlowRunner registra gatilhos na inicialização.

> [!NOTE]
> **Limites honestos v1:** sem importador Langflow automático; sem runtime Langflow embutido; nós LLM viram stubs `invoke` (chamada real em `python.module`). Fluxos são **locais, in-process** — não integração de webhooks na nuvem. Passos HTTP/webhook previstos v1.1.

### Caminhos de adoção para fluxos

Três formas de adotar fluxos de runtime:

1. 🚫 **Sem fluxos** — Omitir ou desabilitar; o starter funciona sem FlowRunner
2. 🔧 **Fluxos como helpers** — Pequenos serviços de automação (timers, hooks de evento) dentro de um app maior
3. 🔀 **Híbrido** — MVC via blueprint + fluxos background/triggered no mesmo binário

---

## Construindo seu app

O Nexus mira **ferramentas nativas, com uso intenso de dados ou implantadas em campo** — mesas de trading, visualizadores CAD, viz científica, utilitários de game dev, bancadas de áudio/DSP, painéis de robótica e tablets Android de campo. Template padrão: starter de propósito geral (hello + counter). **Plotter estilo Desmos** opcional em `examples/plotter/`.

### Templates (desktop e Android)

| Template | Stack | Guia |
|----------|-------|------|
| `desktop-app` | SDL3 + ImGui + pybind11 + sol2 | [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md) |
| `android-app` | SDL3/GLES + Chaquopy + Djinni | [docs/templates/android-app.md](../../docs/templates/android-app.md) |

Saída: `builds/framework/<nome>/` · Layout: [builds/README.md](../../builds/README.md) · [template/README.md](../../template/README.md)

### Python no desktop vs Android

O mesmo nó `python.module` no `blueprint.json` liga amostragem de curvas nos **dois** templates — só mudam setup Python, empacotamento e fronteira C++↔Python.

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Python integrado** | pybind11 — CPython dentro do processo nativo | Chaquopy na JVM; Djinni `ChaquopyPythonBridge` |
| **Árvore de fontes** | `python/` (ex.: `functions.py`) | `app/src/main/python/` |
| **Arquivo** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **Nenhum** — Gradle/Chaquopy empacota `.py` no APK |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Rebuild típico** | `cmake --build` (atualiza `python.dat`) | `./gradlew :app:assembleDebug` |

![Fluxo de embedding Python desktop vs Android](../../docs/assets/diagrams/python-desktop-vs-android-flow.svg)

*Mesma porta evaluate do `python.module` — empacotamento e ponte diferentes por plataforma.*

Guias: [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md)

### UI TypeScript + XHTML

Duas camadas de autoria de UI descem para a mesma API ImGui/Lua — nenhuma usa motor de browser.

**Lua imperativo** (`panels.lua`) — camada mais baixa; `nxs.register_panel(...)` com `ui.button`, hotkeys; hot-reload opcional via `lua.dat`.

**TS/XHTML declarativo** (`ui/ui.xhtml` + `ui/ui.ts`) — markup e TypeScript descem para definições de painel Lua. [`template/shared/dsl/`](../../template/shared/dsl/) mapeia tags (`window`, `panel`, `plot`, `node-editor`, …) para chamadas Dear ImGui, ImPlot e imnodes.

| Mecanismo | TS/XHTML | Desce para |
|-----------|----------|------------|
| `state()` em `ui.ts` | `bind="sampleCount"` em `<slider>` | Estado two-way do widget ImGui |
| `native()` em `ui.ts` | `items-source="activeCurves"` | Projeção read-only do model C++ |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Mesmos comandos `nxs.*` que Lua chama diretamente |

Comece aqui: [template/desktop-app/ui/ui.xhtml](../../template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

### Scripts Lua e fluxos opcionais

- **Lua** — painéis e hotkeys em runtime via sol2; edite `scripts/panels.lua`, rebuild empacota `lua.dat`
- **Fluxos** — serviços opcionais `flows.json`; veja [Automações in-app](#automações-in-app-flowsjson) e [Importando procedimentos Langflow](#importando-procedimentos-langflow)

### Quem aprende mais rápido

| Persona | Comece por |
|---------|------------|
| Game devs (overlays ImGui) | `scripts/panels.lua` → hotkeys e botões rápidos |
| Engenheiros C++ | `src/model/` + `src/controller/` → estender `FunctionRegistry` |
| Devs web | `ui/ui.xhtml` + `ui/ui.ts` → adicionar painel e handler |
| Analistas Python | `python/functions.py` → nova amostragem de curva |
| Devs Android | Gerar `android-app` → rastrear ponte Djinni |

Guia completo: [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

<details>
<summary><strong>Quando outra stack pode encaixar melhor</strong></summary>

| Situação | Considere |
|----------|-----------|
| Equipe só web, sem apetite por C++/CMake | Electron ou Tauri |
| UI de marketing pixel-perfect | Web ou toolkit nativo com motor de layout |
| iOS a partir deste repo hoje | Ainda não entregue — aguarde template iOS v1 |
| Projeto novo safety-critical | Rust — veja [C++ moderno](#c-moderno-e-crescimento-sem-reescrever) |

</details>

---

## A pasta `misc/`

A pasta `misc/` consolida **ferramentas do repositório Framework** — módulos Gradle, plugins de convenção, setup na primeira execução, imagens de container, notas de CI e scripts auxiliares. Nada disso vai para apps nativos gerados; serve apenas para construir e executar o gerador de projetos.

| Caminho | Papel |
|---------|-------|
| [misc/core/](../core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, schema `nxs_config.json` (v2) |
| [misc/cli/](../cli/) | `:cli` — comando headless `generate` |
| [misc/build-logic/](../build-logic/) | Included build — toolchain JVM 26, plugins de convenção |
| [misc/client-setup/](../client-setup/) | Instaladores na primeira execução (JDK 26 + Git) |
| [misc/scripts/](../scripts/) | [dev/](../scripts/dev/) · [test-gen/](../scripts/test-gen/) · [generate-diagrams/](../scripts/generate-diagrams/) |
| [misc/docker/](../docker/) | Geração containerizada |
| [misc/jenkins/](../jenkins/) | CI Jenkins opcional |
| [misc/translations/](README.md) | READMEs localizados — [pt-BR](README.pt-BR.md) · [es](README.es.md) · [de](README.de.md) · [ru](README.ru.md) · [zh-CN](README.zh-CN.md) |

O Gradle mapeia `:core` e `:cli` a partir de `misc/` via [settings.gradle.kts](../../settings.gradle.kts). Hub: [misc/README.md](../README.md) · Pipeline: [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md)

**test-gen** grava stubs de smoke/instrumentados para apps em `builds/framework/<projeto>/` (não o gerador em si). Entrada: `./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp` — veja [misc/scripts/test-gen/README.md](../scripts/test-gen/README.md).

---

## Adicionando dependências

Depois do [client-setup](../client-setup/README.md) e **Generate Project**, adicione dependências nativas no **app gerado** em `builds/framework/<ProjectName>/` — não nos módulos do gerador Compose.

- **C++** — estenda `CMakeLists.txt` com `FetchContent` ou vcpkg; recompile com `cmake --build --preset debug`
- **Python** — desktop: `pip install`, edite `python/`, recompile; Android: Chaquopy `pip { install(...) }` em `app/build.gradle.kts`
- **Lua** — coloque `.lua` em `scripts/`, `require` a partir de `panels.lua`; rebuild empacota `lua.dat`

Guia completo: **[docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)**

---

## C++ moderno e crescimento sem reescrever

Projetos gerados usam **C++20** com padrões RAII, presets CMake e clang-format. Rust ainda vence em garantias estáticas de segurança — trade-off honesto para times com libs C++ existentes (kernels CAD, codecs, APIs de exchange) e dependências ImGui/SDL3.

**Cresça passo a passo, não reescreva do zero.** Novos nós de blueprint, flows em runtime e telas autoria em XHTML podem conviver com scripts Lua antigos e módulos C++ sob medida no mesmo processo. Times presos em Electron ou Tauri costumam enfrentar um garfo: aceitar overhead de web-shell ou apostar em rewrite completo. O Nexus oferece um terceiro caminho — manter o C++ crítico de performance que você já pagou, modernizar autoria incrementalmente e fazer profile antes de reescrever em outra linguagem.

> *"Faça funcionar, faça certo, faça rápido — nessa ordem."* — frequentemente atribuído a Kent Beck

---

## Zig patching (builds nativos)

**Zig** é uma camada opcional de **orquestração** para apps nativos gerados — não uma reescrita do gerador Kotlin `:app` / `:core`. O Gradle continua sendo o sistema de build do cliente Compose e do pipeline de geração.

### Por que adotar Zig (ganhos)

O Zig não substitui sua stack C++20 MVC — substitui **atrito de build**: menos toolchains no host, uma história única de cross-compile, glue JNI mais fino e um lock de dependências em vez de sete clones FetchContent a cada configure frio. Os apps gerados mantêm as mesmas fontes; só muda o backend nativo.

| Fase | Foco | Status |
|------|------|--------|
| 0 | Instalar Zig **0.14.x** em `misc/client-setup` | ⬜ Planejado |
| 1 | Sidecar `zig-services/` ao lado do CMake | ⬜ Planejado |
| 2 | Importador Langflow → `flows.json` (`enabled: false` na importação) | ⬜ Planejado |
| 3 | Zig como backend nativo padrão no desktop | ⬜ Planejado |
| 4 | Zig JNI no Android (aposentar Djinni) | ⬜ Planejado |
| 5 | ArenaAllocator opt-in nos hotspots do AppModel | ⬜ Planejado |

Rollout em fases: Zig ao lado do CMake → Zig padrão no desktop → JNI Android → ArenaAllocator opt-in. Fixar Zig **0.14.x**; builds Android precisam do NDK (API ≥ 29) — Zig não inclui Bionic. O importador Langflow é uma trilha Kotlin paralela em `:core` e não bloqueia o scaffold Zig.

### Ganhos projetados / ilustrativos (metas Fase 1–4)

**Não medidos em produção hoje** — Zig ainda é um plano cirúrgico em andamento ([plano completo](../../docs/architecture/zig-patching.md)). Linhas com **†** usam baselines medidos neste repositório em 2026-07-13. As demais combinam claims do toolchain Zig com metas das Fases 1–4.

| Métrica | Anterior (baseline CMake / Djinni) | Com Zig (meta) | Ganho |
|---------|-------------------------------------|----------------|-------|
| Tempo de configure nativo frio † | ~174 s (`cmake --preset debug`, clones FetchContent) | ~20–30 s (`zig build`, cache `build.zig.zon` quente) | **~83–88% mais rápido** |
| Compiladores host (3 desktop + 2 ABIs Android) | 5–7 (MSVC, g++, clang, NDK por ABI) | **1** (`zig c++`) | **~83% menos** instalações |
| Espaço em disco (ferramentas nativas) | ~10–12 GB (MSVC + NDK + clang) | ~80 MB (tarball Zig 0.14.x) | **~99% menor** |
| Cross-compile Linux → Windows sem MSVC | Não suportado | Suportado (`-Dtarget=x86_64-windows`) | **Nova capacidade** |
| Passos de build matriz Android † | 2 presets CMake + Gradle NDK | 1 `zig build` com targets Android | **~50% menos** invocações |
| LOC gerado Djinni † | 228 linhas / 8 arquivos | ~120 linhas / 2 módulos `.zig` | **~47% menos** glue |
| Arquivos ponte Python † | 10 | 3 | **~70% menos** arquivos |
| Arquivos ponte Lua † | 8 | 2 | **~75% menos** arquivos |
| Ferramentas do grafo de build | CMake + Ninja + compilador + NDK + Djinni | **Zig** apenas | **4 → 1** ferramentas |
| Hash reprodutível cross-machine | Tags FetchContent variam com cache | Lock `build.zig.zon.json` | **Pins determinísticos** |
| Rebuild incremental (1 TU C++) | ~6–10 s | ~4–6 s (projetado) | **~30–40% mais rápido** |
| Hotspots ArenaAllocator opt-in | 0 | 3 planejados | **Cobertura** onde perf mostrar ganho |
| Jobs CI smoke nativo | 5–7 runners por OS | 2 (cross-compile Linux + macOS) | **~65–70% menos** slots |
| Langflow: flows habilitados por padrão | Risco manual | **`enabled: false`** em todo import | **Default mais seguro** |
| Dependências de rede no configure † | **7** clones FetchContent | **0** pós-vendor | **100% offline** |
| Docs onboarding toolchain nativo † | ~10 páginas | ~3 páginas | **~70% menos** leitura |
| Allocator C-ABI unificado | Nenhum | `nxs_alloc` / `nxs_free` (opt-in) | **C-ABI único** |
| Tamanho binário release desktop | Baseline CMake + LTO | Mesmas fontes, LTO Zig | **~3–8% menor** (projetado) |
| Tempo de link release | ~40–60 s | ~25–40 s (projetado) | **~30–35% mais rápido** |
| Caminho de artefatos previsível | `_build/`, `build/`, depende do preset | `zig-out/bin/` fixo | **Layout fixo** para CI |

[Plano completo](../../docs/architecture/zig-patching.md)

---

## Além da automação rápida

**Power Automate**, **n8n** e ferramentas similares brilham em cola de ops. Isso quebra quando o conserto rápido *é* o produto: sem UI nativa, empacotamento offline fraco, dependência de cloud.

O Nexus mantém o modelo mental de nós e arestas mas gera uma **aplicação nativa real** — C++/SDL3, Lua/Python, ImGui + TS/XHTML, packs de script, apps desktop/Android. Veja [Como o Nexus se compara](#como-o-nexus-se-compara) para contexto ferramenta a ferramenta.

**Caminho de migração:** ligue módulos no editor de blueprint → gere com `:cli` ou **Generate Project** → itere nas camadas de código em vez de empilhar remendos no fluxo. Um webhook n8n pode continuar na borda para cola de ops enquanto o app detém estado, UI e comportamento offline in-process.

| Área | Ferramentas de fluxo (típico) | Saída Nexus |
|------|-------------------------------|-------------|
| **Runtime** | Motor de passos no servidor | App desktop/mobile ou APK Android |
| **Offline / campo** | Exige conectividade com host do workflow | App SDL3 offline-first |
| **Performance** | Round-trips HTTP entre passos | C++ in-process; Python/numpy |
| **Superfície de UI** | Dashboard do vendor ou nenhuma | ImGui + páginas DSL |
| **Cross-platform** | Integrações separadas por alvo | Um `blueprint.json` liga desktop + Android |

---

## Status de desenvolvimento

**Entregue hoje:**

- `:app` — Counter + Generate Project + Blueprint Editor + Flows Editor
- `:core` / `:cli` — geração de templates + `BlueprintValidator` + `FlowsValidator`
- `template/*` — desktop + Android com `blueprint.json` + `flows.json` opcional
- Packs de script — `lua.dat` / `python.dat` (desktop), `lua.dat` no APK Android
- `builds/`, `misc/client-setup/`, `docs/`

**Limitações (v1):** apenas gerador Compose Desktop; estética ImGui utilitária; Chaquopy aumenta o APK; sem iOS nesta toolchain hoje.

**Branch:** desenvolvimento ativo em **`main`** (`origin/main`).

Trabalho restante antes do MVP: [Rumo ao MVP](#rumo-ao-mvp).

---

## Copyright e licença

> [!IMPORTANT]
> **Licença Apache 2.0** — uso comercial, modificação e distribuição são permitidos. Mantenha avisos de copyright e o arquivo [LICENSE](../../LICENSE) ao redistribuir. O código do app gerado é seu; trechos copiados dos templates devem preservar os avisos Apache.

- © 2026 Nexus Framework contributors — Nexus Framework Client e templates/docs incluídos
- **Projetos gerados:** você é dono do código que o gerador escreve; trechos copiados dos templates Nexus devem manter o aviso Apache 2.0 onde aparecerem

Texto completo: [Apache License 2.0](../../LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

---

## Veja também

*Desenhe o blueprint, gere a árvore, entregue o binário — depois itere nas camadas de código de verdade.*

### Documentação

| Doc | Descrição |
|-----|-----------|
| [docs/README.md](../../docs/README.md) | Hub de documentação |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas |
| [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | Schema `blueprint.json` |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | Schema `flows.json` |
| [AGENTS.md](../../AGENTS.md) | Comandos de build para assistentes de código |

### Ecossistema

| Tecnologia | Papel |
|------------|------|
| [SDL3](https://www.libsdl.org/) | Janela, input, superfícies GPU |
| [Dear ImGui](https://github.com/ocornut/imgui) / [ImPlot](https://github.com/epezent/implot) | UI immediate-mode e gráficos |
| [sol2](https://github.com/ThePhD/sol2) / [pybind11](https://pybind11.readthedocs.io/) | Lua e Python em C++ |
| [Chaquopy](https://chaquo.com/chaquopy/) / [Djinni](https://github.com/dropbox/djinni) | Python e ponte Kotlin no Android |
| [Langflow](https://github.com/langflow-ai/langflow) / [n8n](https://n8n.io/) | Autoria externa opcional (importar no Nexus) |

| Repositório | Papel |
|-------------|------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | Distribuição separada do wizard `:client-desktop` |

---

## Rumo ao MVP

Quando cada linha estiver ✅, o Nexus Framework estará **pronto para MVP**: gerar apps nativos, editar blueprints/fluxos, escrever projetos e entregar um build desktop/Android documentado.

### Cliente e gerador de projetos

| Item | Status |
|------|--------|
| Gerar desktop + Android a partir dos templates | ✅ |
| Editor de blueprint (Compose) | ✅ |
| Editor de fluxos (lista, ativar/desativar, preview JSON) | ✅ |
| ProjectGenerator + validadores | ✅ |
| Assistente Compose em 6 passos *(v1 entrega Generate em 2 telas + editores)* | ⬜ |

### Templates

| Item | Status |
|------|--------|
| Templates desktop + Android de propósito geral | ✅ |
| Estrutura `blueprint.json` + `flows.json` opcional | ✅ |
| Stubs TS/XHTML DSL, caminhos Lua e Python | ✅ |
| Build de app desktop ponta a ponta verificado no CI | ⬜ |
| Build APK Android ponta a ponta verificado no CI | ⬜ |

### Runtime / apps gerados

| Item | Status |
|------|--------|
| Paridade de packs `python.dat` / `lua.dat` | ✅ |
| pybind11 desktop totalmente ligado no app gerado (Fase 2) | ⬜ |
| Ponte Chaquopy Android testada E2E em dispositivo | ⬜ |
| Compilador TS/XHTML → Lua *(caminho manual via `panels.lua` documentado)* | ⬜ |

### Docs e experiência do desenvolvedor

| Item | Status |
|------|--------|
| Seções de arquitetura e comparação no README | ✅ |
| Guias `AGENTS.md` dos templates | ✅ |
| Scripts `client-setup` (JDK 26) | ✅ |
| CLI `debug validate --all` ou equivalente no CI | ⬜ |

### Release

| Item | Status |
|------|--------|
| CI verde em `main` | ⬜ |
| Binário do cliente publicado (`builds/client/`) | ⬜ |
| Tag de versão `v1.0.0` | ⬜ |

<details>
<summary><strong>Roadmap pós-MVP (v1.1+) — clique para expandir</strong></summary>

| Item | Notas |
|------|-------|
| Painel imnodes nativo de blueprint | Mesmo schema `blueprint.json` |
| Canvas visual do editor de fluxos | — |
| Importador JSON Langflow | Tradução manual na v1 |
| Catálogo remoto de templates · template iOS | — |
| Tipos de passo HTTP/webhook em `flows.json` | — |

</details>
