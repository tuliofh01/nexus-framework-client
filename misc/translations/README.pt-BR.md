# O Framework da Nexus Company para Desenvolvimento de Aplicações Nativas

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="🧩 Nexus Framework — logo do gerador de projetos nativo C++/Lua/Python" width="240" />
</p>

<p align="center"><strong>🧩 Apps nativos, não abas de browser</strong> — entregue binários SDL3 a partir de um grafo blueprint.</p>

<p align="center">🇧🇷 Versão em português · <a href="../../README.md">English README</a></p>

<p align="center">
  <a href="../../README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache--2.0-blue?style=flat-square" alt="Licença open source Apache 2.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.4 gerador de projetos Compose Desktop" /></a>
  <a href="https://www.libsdl.org/"><img src="https://img.shields.io/badge/SDL3-multiplataforma-green?style=flat-square" alt="SDL3 desktop e Android" /></a>
  <a href="https://github.com/ocornut/imgui"><img src="https://img.shields.io/badge/Dear%20ImGui-immediate--mode-orange?style=flat-square" alt="Dear ImGui UI immediate-mode" /></a>
</p>

> [!TIP]
> **Bem-vindo(a).** Rode o [setup inicial](#primeira-execução), depois `./gradlew :app:run` — em minutos você tem o cliente Compose, o editor de blueprint e o caminho até `builds/framework/<nome>/`. Sem download de Chromium.

**The Nexus Framework** é um **construtor de apps nativos open source**: gera aplicações em **C++**, **Lua** e **Python** para **desktop** (Windows, macOS, Linux) e **Android** a partir de um [`blueprint.json`](#blueprint-nodes-langflow-style-vs-n8n) estilo [Langflow](https://github.com/langflow-ai/langflow) e [`flows.json`](#fluxos-de-runtime-opcionais-serviços) opcional. O cliente **Kotlin Compose** Desktop (`:app`) autora grafos; [`misc/core`](#o-diretório-misc) valida e gera projetos em [`template/`](#estrutura-do-repositório) com janela **SDL3**, widgets **Dear ImGui**, scripts sol2, autoria de UI TypeScript + XHTML e **Python integrado** (pybind11 no desktop, Chaquopy + Djinni no Android). Gráficos ImPlot renderizam sem motor de browser.

*Pense nele como um fluxo de geração de código com opiniões — e um plotter de amostra que realmente roda.*

Se você está avaliando stacks **web-shell** — **Electron** (Chromium + JavaScript) ou **Tauri** (WebView do SO + Rust) — o Nexus aposta em outro caminho: runtime **C++** nativo, widgets immediate-mode e Lua/Python in-process em vez de motores de layout HTML. Essas ferramentas brilham quando DOM/CSS é a superfície do produto; o Nexus brilha quando throughput, tamanho de binário e uma pilha SDL3 compartilhada entre desktop e tablets Android de campo importam mais.

## Índice

- [Blueprint nodes: Langflow-style vs n8n](#blueprint-nodes-langflow-style-vs-n8n)
- [Fluxos de runtime opcionais (serviços)](#fluxos-de-runtime-opcionais-serviços)
- [O que é este repositório](#o-que-é-este-repositório)
- [Primeira execução](#primeira-execução)
- [Início rápido](#início-rápido)
- [Estrutura do repositório](#estrutura-do-repositório)
- [O diretório `misc/`](#o-diretório-misc)
- [Casos de uso — para que o Nexus foi feito](#casos-de-uso--para-que-o-nexus-foi-feito)
- [Por que o Nexus performa melhor](#por-que-o-nexus-performa-melhor)
- [Curva de aprendizado](#curva-de-aprendizado)
- [Python: Desktop vs Android](#python-desktop-vs-android)
- [TypeScript + DSL XHTML](#typescript--dsl-xhtml)
- [C++ moderno no Nexus](#c-moderno-no-nexus)
- [Arquitetura](#arquitetura)
- [Documentação](#documentação)
- [Adicionando dependências após o setup](#adicionando-dependências-após-o-setup)
- [Status de desenvolvimento e limitações](#status-de-desenvolvimento-e-limitações)
- [Além da automação rápida: de fluxos para aplicações reais](#além-da-automação-rápida-de-fluxos-para-aplicações-reais)
- [Copyright e licença](#copyright-e-licença)
- [Veja também](#veja-também)
- [Rumo ao MVP](#rumo-ao-mvp)

---

## 🧩 Blueprint nodes: Langflow-style vs n8n

O Nexus inclui um **grafo de app estilo Langflow** na raiz do projeto via [`blueprint.json`](../../docs/templates/blueprint-schema.md). Nós declaram módulos (`python.module`, `cpp.model`, `ui.page`, …); arestas ligam fluxo de dados e comandos dentro do app MVC gerado. A **geração em [`:core`](#o-diretório-misc)** valida e consome o grafo ao materializar `builds/framework/<nome>/`.

<!-- Diagrama: comparação Langflow vs n8n vs blueprint Nexus -->
![📊 Langflow vs n8n vs blueprint Nexus — passos conectados vs automação vs geração em tempo de build](../../docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

| | **[Langflow](https://github.com/langflow-ai/langflow)** | **[n8n](https://n8n.io/)** | **Nexus [`blueprint.json`](../../docs/templates/blueprint-schema.md)** |
|---|-------------|---------|---------------------------|
| **Propósito** | Autoria de fluxos ML/LLM | Automação (gatilhos, HTTP, integrações) | Estrutura de app nativo **em tempo de build** |
| **Modelo de nó** | Componentes tipados (modelo, ferramenta, memória) | Passos + gatilhos (webhook, cron, Slack, …) | Módulos tipados (`python.module`, `cpp.model`, …) |
| **Arestas** | Dados entre componentes | Roteamento de evento / payload | Portas MVC (`evaluate`, `sampleCache`, `commands`, …) |
| **Execução** | **Runtime** — usuário executa o fluxo | **Runtime** — agenda ou webhook dispara | **Em tempo de build** — `ProjectGenerator` valida e gera |


| Tipo de nó | Papel |
|------------|-------|
| `python.module` | Amostragem / analytics Python (`python/functions.py`) |
| `cpp.model` | Estado de domínio C++ (`FunctionRegistry`, caches) |
| `cpp.controller` | Comandos + ligação (`PlotController`) |
| `ui.page` | Página TS/XHTML (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Painéis Lua em runtime (`scripts/panels.lua`) |

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (canvas Compose + inspetor JSON na v1; painel nativo **imnodes** previsto na v1.1 — mesmo schema). Amostras: [template/desktop-app/blueprint.json](../../template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](../../template/android-app/blueprint.json). Schema: [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md).

### Nós estilo Langflow vs n8n

O [`blueprint.json`](../../docs/templates/blueprint-schema.md) do Nexus é mais próximo do **Langflow** (grafo tipado in-app) do que do **n8n** (automação externa) — veja a tabela acima. O Nexus **não substitui** o n8n; um app gerado pode chamar webhooks n8n via Python/Lua enquanto o blueprint cuida da fiação **interna**. Para quando um fluxo deve virar software nativo entregue, veja [Além da automação rápida](#além-da-automação-rápida-de-fluxos-para-aplicações-reais).

**Exemplos visuais:** [chatbot RAG](../../docs/assets/examples/langflow-rag-chatbot.svg) · [agente com ferramentas](../../docs/assets/examples/langflow-agent-tools.svg) · [estrutura blueprint Nexus](../../docs/assets/examples/nexus-blueprint-app-structure.svg)

---

## ⚡ Fluxos de runtime opcionais (serviços)

O Nexus separa **estrutura do app em tempo de build** de **automações que rodam dentro do app**:

| Camada | Arquivo | Propósito |
|--------|---------|-----------|
| Estrutura do app | [`blueprint.json`](../../docs/templates/blueprint-schema.md) | Fiação MVC estilo Langflow (módulos, portas, telas) |
| Fluxos de runtime | [`flows/flows.json`](../../docs/templates/flows-schema.md) | Serviços opcionais in-app — loops em background, gatilhos por evento, agendamentos |

### Blueprint vs fluxos — duas camadas
*Estrutura em tempo de build vs automações opcionais no app*

O `blueprint.json` define a fiação MVC consumida uma vez pelo `:core`; o `flows.json` registra gatilhos in-process carregados pelo FlowRunner na inicialização. Um único canvas Langflow pode ser dividido nos dois arquivos após a tradução.

![blueprint.json vs flows.json — modelo de duas camadas](../../docs/assets/diagrams/blueprint-vs-flows-layers.svg)

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — listar fluxos, habilitar/desabilitar, pré-visualizar JSON (editor visual na v1.1). Schema: [docs/templates/flows-schema.md](../../docs/templates/flows-schema.md).

### Caminhos de adoção

Três formas de adotar fluxos de runtime — escolha o peso certo para o seu app:

1. 🚫 **Sem fluxos** — Omitir ou desabilitar; app totalmente customizado; o plotter funciona sem FlowRunner
2. 🔧 **Fluxos como helpers** — Pequenos serviços de automação (timers, hooks de evento) dentro de um app maior
3. 🔀 **Híbrido** — MVC via blueprint + fluxos background/triggered no mesmo binário

### Background vs triggered

| Modo | Quando executa | Exemplo de gatilho |
|------|----------------|-------------------|
| `background` | Enquanto o app está vivo | `interval` a cada 5000 ms |
| `triggered` | Só na condição | `event` `curve.added`, `startup`, `manual` |

Adicione vários fluxos no array `flows` de [`flows.json`](../../docs/templates/flows-schema.md) (cada um com `id` único). Desabilite globalmente com `nxs_config.json` → `"flows": { "enabled": false }` ou por fluxo com `"enabled": false`.

### Usando o Langflow para autoria de fluxos

[Langflow](https://github.com/langflow-ai/langflow) é ferramenta **externa opcional** — exporte JSON e adote como serviços nativos em `flows.json`. Grafos de estrutura → [`blueprint.json`](../../docs/templates/blueprint-schema.md); automação → **`flows.json`** (veja [Blueprint vs fluxos](#blueprint-vs-fluxos--duas-camadas)). Langflow/n8n: [Blueprint nodes](#blueprint-nodes-langflow-style-vs-n8n).

#### Passo A — Projetar no Langflow (ferramenta externa opcional)

1. Monte um fluxo visual no Langflow: nós (LLM, Prompt, Tool, Retriever, Agent, …), arestas e parâmetros por nó.
2. Exporte o fluxo como JSON — em geral via **Export flow** ou pela API do Langflow (`/api/v1/flows/{id}`). A exportação é um documento de definição de fluxo (nós, arestas, payloads `data`, posições). Nomes de campo e aninhamento **diferem** do `flows.json` do Nexus; trate a exportação como artefato de design, não como arquivo plug-and-play.

#### Passo B — Mapear para `flows.json` do Nexus

O Nexus adota o grafo como **automação in-process** — não como servidor Langflow hospedado.

| Conceito Langflow | Nexus `flows.json` |
|-------------------|-------------------|
| Componente Langflow (LLM, Tool, Agent, …) | `steps[]` com `type: invoke` apontando para `nxs.*`, `python.*` ou `lua.*` |
| Arestas / ordem de execução | Array `steps` ordenado; ramificações via `condition` (v1.1) |
| Gatilho (chat, webhook, agendamento) | `trigger.type`: `event`, `interval`, `startup`, `manual`, `hotkey` |
| Loop contínuo / polling | `mode: background` |
| Execução sob demanda / iniciada pelo usuário | `mode: triggered` |

#### Passo C — Fluxo de adoção

1. **Exportar** JSON do Langflow → **traduzir** para [schema flows](../../docs/templates/flows-schema.md) (manual v1; importador v1.1).
2. **Colocar** em `flows/flows.json` ou colar em **Edit flows** (`./gradlew :app:run` → **Generate Project** → **Edit flows**).
3. **Habilitar** em `nxs_config.json` → `"flows": { "enabled": true }`. Handlers customizados funcionam sem Langflow.

#### Limites honestos (v1)

- Sem importador Langflow automático; sem runtime Langflow embutido; nós LLM viram stubs `invoke` (chamada real em `python.module`).
- Fluxos são **locais, in-process** — não integração de webhooks na nuvem ([n8n](#nós-estilo-langflow-vs-n8n)). Passos HTTP/webhook previstos v1.1.

Amostra: [template/desktop-app/flows/flows.json](../../template/desktop-app/flows/flows.json).

---

> *"Dê-me seis horas para cortar uma árvore e passarei as quatro primeiras afiando o machado."* — Abraham Lincoln

## 📦 O que é este repositório

| Camada | Hoje | Roadmap (v1.1+) |
|--------|------|-----------------|
| **Cliente (`:app`)** | Compose Desktop — demo MVC + **Generate Project** + **Blueprint Editor** + **Flows Editor** | Assistente em 6 passos, painel imnodes nativo, canvas visual de fluxos (v1.1) |
| **Geração ([`misc/core`, `misc/cli`](#o-diretório-misc))** | `ProjectGenerator` + `BlueprintValidator` + `FlowsValidator` → `builds/framework/<nome>/` | Catálogo remoto, packs `python.dat` / `lua.dat` |
| **Templates** | Plotter desktop + Android com `blueprint.json` compartilhado | Template iOS |
| **Autoria** | JSON de nós estilo Langflow + TS/XHTML + Lua | Packs de script criptografados |

Este é o monorepo **Framework** (`:app`, `:core`, `:cli`). Não é o repositório separado [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (wizard `:client-desktop` lá).

## 🚀 Primeira execução

Execute um script de setup da plataforma, carregue o arquivo de ambiente e use o Gradle:

| Plataforma | Setup | Ambiente |
|------------|-------|----------|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Exige **JDK 26** e Git — veja [misc/client-setup/README.md](../client-setup/README.md).

## Início rápido

```bash
source misc/client-setup/env.sh          # após setup na primeira execução
./gradlew :app:run                  # cliente Compose
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Compilar e testar: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy do cliente: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](../../builds/client/app/)

Build do template desktop: `cd template/desktop-app && cmake --preset debug && cmake --build --preset debug`

Layout de saída: [builds/README.md](../../builds/README.md) · Templates: [template/README.md](../../template/README.md) · Progresso MVP: [Rumo ao MVP](#rumo-ao-mvp)

## 📁 Estrutura do repositório

```
Framework/
├── app/                 Cliente Compose Desktop (`:app`) — MVC em `nexus.opensource/`
├── misc/                Ferramentas + fluxo de geração — veja [O diretório `misc/`](#o-diretório-misc)
├── builds/              Cliente → builds/client/ · apps → builds/framework/<nome>/
├── template/            desktop-app · android-app · shared
└── docs/                Hub de documentação → ../../docs/README.md
```

## 🧰 O diretório `misc/`

A pasta `misc/` consolida **ferramentas do repositório Framework** — módulos Gradle, plugins de convenção, setup na primeira execução, imagens de container, notas de CI e scripts auxiliares. Nada disso vai para apps nativos gerados em `builds/framework/<nome>/`; serve apenas para construir e executar o gerador de projetos.

| Caminho | Gradle / papel |
|---------|----------------|
| [misc/core/](../core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, schema `nxs_config.json` (v2) |
| [misc/cli/](../cli/) | `:cli` — comando headless `generate` |
| [misc/build-logic/](../build-logic/) | Included build (antes `buildSrc` na raiz) — toolchain JVM 26, plugins de convenção |
| [misc/client-setup/](../client-setup/) | Instaladores na primeira execução (Linux/macOS/Windows) para JDK 26 + Git; `env.sh` / `env.bat` definem `JAVA_HOME` antes do Gradle |
| [misc/scripts/](../scripts/) | Automação do repo — [dev/](../scripts/dev/) (build/validação/execução do client), [test-gen/](../scripts/test-gen/) (stubs smoke/instrumentados em `builds/framework/`), [generate-diagrams/](../scripts/generate-diagrams/) (SVGs de docs) |
| [misc/docker/](../docker/) | `Dockerfile` + compose para geração containerizada |
| [misc/jenkins/](../jenkins/) | CI Jenkins opcional — [Jenkinsfile](../jenkins/Jenkinsfile) · [setup](../jenkins/README.md) |
| [misc/translations/](README.pt-BR.md) | READMEs traduzidos — [pt-BR](README.pt-BR.md) |

O Gradle expõe `:core` e `:cli` na raiz do repo enquanto os fontes ficam em `misc/` — veja [settings.gradle.kts](../../settings.gradle.kts):

```kotlin
includeBuild("misc/build-logic")
include(":core", ":cli", ":app")
project(":core").projectDir = file("misc/core")
project(":cli").projectDir = file("misc/cli")
```

O included build em `misc/build-logic/` substitui um diretório `buildSrc/` na raiz (o Gradle só descobre `buildSrc/` automaticamente na raiz do repositório).

**Fora de `misc/`** — estes caminhos na raiz têm papéis distintos:

| Caminho | Papel |
|---------|-------|
| [app/](../../app/) | Gerador de projetos Compose Desktop (`:app`) — Generate Project, editores de blueprint/fluxos |
| [template/](../../template/) | Templates-fonte copiados para `builds/framework/<nome>/` |
| [builds/](../../builds/) | Saídas de deploy — cliente em `builds/client/`, apps gerados em `builds/framework/` |
| [docs/](../../docs/) | Hub de documentação |

**Comandos rápidos** (caminhos em `misc/`):

```bash
./misc/client-setup/linux/setup.sh && source misc/client-setup/env.sh
./gradlew :core:compileKotlin
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./misc/scripts/dev/nexus-dev.sh compile
./misc/scripts/dev/generate-in-docker.sh desktop MyApp builds/framework/MyApp
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture
```

Hub: [misc/README.md](../README.md) · fluxo de geração: [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md)

### Scripts de setup inicial (`misc/client-setup/`)

Execute **uma vez** antes do primeiro `./gradlew :app:run` — mesmo fluxo de [Primeira execução](#primeira-execução). Helpers por distro e troubleshooting: [misc/client-setup/README.md](../client-setup/README.md).

### Testes automatizados e geração de testes (`misc/scripts/test-gen/`)

[test-gen/](../scripts/test-gen/) gera stubs de smoke e testes instrumentados para **apps já gerados** em `builds/framework/<projeto>/` — não para o gerador de projetos em si. Lê `nxs_config.json`, detecta desktop vs Android e grava arquivos idempotentes (smoke C++ desktop via CTest; stubs Kotlin `androidTest`). A geração é idempotente; use `--force` para sobrescrever.

Os entry points por plataforma envolvem o núcleo compartilhado em [test-gen/common/generate-tests.sh](../scripts/test-gen/common/generate-tests.sh):

| Plataforma | Script |
|------------|--------|
| Linux (Arch) | `linux/arch.sh` |
| Linux (Debian/Ubuntu) | `linux/debian.sh` |
| Linux (Fedora/RHEL) | `linux/fedora.sh` |
| Linux (fallback POSIX) | `linux/generic.sh` |
| macOS | `macos/darwin.sh` |
| Windows | `windows/win32.ps1` |

```bash
./misc/scripts/test-gen/linux/generic.sh --dry-run --project MyApp
./misc/scripts/test-gen/linux/debian.sh builds/framework/MyApp
./gradlew :core:test   # testes Gradle do gerador de projetos (:core, :cli, :app)
```

Para build/validação/execução local do client Compose, use [misc/scripts/dev/nexus-dev.sh](../scripts/dev/nexus-dev.sh) (`compile`, `generate`, `docker`, …). Uso completo: [misc/scripts/test-gen/README.md](../scripts/test-gen/README.md).

### Outros scripts do repo

| Família | Entry point | Propósito |
|---------|-------------|-----------|
| `dev/` | `./misc/scripts/dev/nexus-dev.sh compile` | Workflow Gradle local + geração Docker |
| `generate-diagrams/` | `python3 misc/scripts/generate-diagrams/generate-styled-diagrams.py` | Regenerar SVGs de docs |

## 🎯 Casos de uso — para que o Nexus foi feito

O Nexus mira **ferramentas nativas, com uso intenso de dados ou implantadas em campo** — mesas de trading, visualizadores CAD, viz científica, utilitários de game dev, bancadas de áudio/DSP, painéis de robótica e tablets Android de campo. Throughput, tamanho de binário e uma pilha SDL3 compartilhada importam mais que layout HTML.

| Caso de uso | Por que Nexus | Template |
|-------------|---------------|----------|
| Mesa de trading / dados de mercado | UI em sub-ms; parsers C++; Python in-process | Desktop |
| Visualizador CAD / malhas / nuvens | Viewport GPU SDL3; geometria em C++ | Desktop |
| Visualização científica | numpy via pybind11; gráficos ImPlot | Desktop |
| Ferramentas de game dev | UI immediate-mode; hot-reload Lua | Desktop |
| Bancada de áudio / DSP | Caminho de sinal C++ de baixa latência | Desktop |
| Monitor DevOps / infra | Binário nativo leve | Desktop |
| Tablet Android de campo | ImGui SDL3/GLES; Djinni; Chaquopy | Android |
| Painel de robótica / teleoperação | ImGui touch; bindings Lua `android.*` | Android |
| HMI embarcado | Mesma pilha SDL3 no desktop e Android | Ambos |

Exemplo principal: **plotter estilo Desmos** — Python amostra curvas, C++ possui o modelo, ImGui desenha. [docs/templates/desktop-app.md](../../docs/templates/desktop-app.md) · [docs/templates/android-app.md](../../docs/templates/android-app.md)

---

## 🏎️ Por que o Nexus performa melhor

O Nexus foi feito para **throughput, footprint e implantação em campo** — não para renderizar sites de marketing. Onde o Electron embute Chromium e o Tauri delega ao WebView do SO, apps Nexus gerados permanecem em endereço nativo de ponta a ponta: lógica de domínio em C++, superfícies GPU SDL3, widgets ImGui/ImPlot e camadas opcionais Lua/Python sem processo de browser.

| Vantagem | O que significa na prática | Contexto Electron / Tauri |
|----------|----------------------------|---------------------------|
| **Tamanho de binário** | ~3–20 MB de app desktop/mobile + assets (cresce com `libs/` vendored) | Instaladores Electron comuns **85–250 MB** (Chromium embutido); Tauri tipicamente **3–15 MB**, mas ainda leva WebView + bundle frontend |
| **Sem Chromium / WebView** | UI é ImGui + SDL3/OpenGL — sem subprocesso renderer, sem layout/paint DOM | Electron = stack browser completo; Tauri = WebView do SO + runtime JS |
| **Memória nativa para arrays** | Malhas, order books e buffers numpy ficam no heap C++; Python via pybind11/Chaquopy sem marshaling via JS | Web shells copiam ou serializam dados através de fronteiras JS |
| **SDL3 multiplataforma** | Mesma camada de janela/input no Windows, macOS, Linux e Android GLES | Mobile é secundário ou toolchain separada na maioria das stacks web-shell |
| **sol2 + hot-reload Lua** | Edite `panels.lua`, reempacote `lua.dat` opcional — painéis UI em runtime sem recompilar C++ | HMR de frontend ajuda, mas ainda é round-trip HTML/CSS/JS |
| **Proteção `python.dat` / `lua.dat`** | Packs criptografados v2 opcionais distribuem lógica sem `.py`/`.lua` soltos em disco (desktop `misc/`; Android `lua.dat` em assets do APK) | Não é preocupação de primeira classe em modelos de asset típicos Electron/Tauri |
| **Refresh ImGui sub-ms** | UI immediate-mode mira **<1 ms** por orientação Dear ImGui; sem layout thrash | Ciclos de layout + paint do WebView dominam CPU em steady state |
| **APK tablet de campo** | Template Android: ImGui SDL3/GLES full-screen + Python Chaquopy em devices rugged — sem WebView | Electron Android não é foco; Tauri Mobile continua WebView-based |
| **Mesmo blueprint, desktop + Android** | Um fluxo `blueprint.json` / imnodes liga MVC nos dois templates | Fluxos de build web + mobile separados são comuns |
| **Djinni vs JNI manual** | Ponte type-safe C++ ↔ Kotlin gerada no Android | N/A em web shells desktop; boilerplate JNI manual em híbridos Android nativos |

**Quando o Nexus vence:** throughput nativo, binários pequenos, paridade SDL3 da mesa de trading ao tablet Android de campo, Python/numpy in-process, rewiring via blueprint e UX immediate-mode estilo game engine — sem pagar por um motor de browser que você não precisa.

**Quando Electron ou Tauri vencem:** sua equipe é web-first, a UI é HTML/CSS/React, ou você precisa de iOS via toolchain web-shell hoje. Trade-off justo — não é modo de falha.

> **Ressalva honesta:** benchmarks entre frameworks variam por complexidade do app, SO e método de medição. Sempre profile *sua* carga antes de escolher só por tamanho ou RAM.

---

## Curva de aprendizado

O Nexus tem rampa real — CMake, C++20 e UI immediate-mode fazem parte — mas o plotter gerado entrega um app funcional no primeiro dia. A seção abaixo foi pensada para leitura rápida.

### Quem aprende Nexus mais rápido

| Persona | Por que encaixa | Comece por |
|---------|-----------------|------------|
| **Game devs** (overlays ImGui de debug) | Já pensam em painéis immediate-mode e hotkeys | `scripts/panels.lua` → ajustar hotkeys e botões rápidos |
| **Engenheiros C++** (CAD, científico, trading) | Dominam o caminho crítico de performance; Python/Lua são camadas opcionais | `src/model/` + `src/controller/` → novo tipo em `FunctionRegistry` |
| **Devs web** (modelo mental de componentes) | DSL TS/XHTML mapeia tags e `on-click` para padrões familiares — sem DOM, widgets ImGui nativos | `ui/ui.xhtml` + `ui/ui.ts` → adicionar painel e handler |
| **Analistas Python-first** | Mantêm numpy/matemática em Python; C++ cuida de render e input | `python/functions.py` → nova amostragem de curva sem reescrever math em JS |
| **Devs Android** (Kotlin, curiosos em NDK) | Djinni gera ponte sem JNI manual; SDL3 hospeda UI nativa full-screen | Gerar template `android-app` → rastrear `MainActivity` → Djinni → core C++ |

**Encaixe mais difícil:** equipes só de design ou só React que esperam layout CSS e não querem tocar CMake/C++. Nexus é ImGui utilitário, não design system — disposição para ler C++ e rodar build nativo importa.

### Matriz de habilidades

| Habilidade | Obrigatória? | Papel no Nexus |
|------------|--------------|----------------|
| C++20 / CMake | **Sim** | Lógica de domínio, MVC, build nativo |
| SDL3 / ImGui | Conceitual | UI immediate-mode — widgets, não HTML |
| Lua / sol2 | Opcional → recomendado | Painéis em runtime, hotkeys, experimentos |
| TypeScript + XHTML | Opcional | Autoria de UI familiar → widgets nativos |
| Python | Opcional | pybind11 (desktop) · Chaquopy (Android) |
| Android / Djinni | Só Android | Ponte sem JNI, empacotamento APK |
| Kotlin Compose | Só cliente do gerador de projetos | Wizard `:app` — não é o app gerado |

### Caminho de progressão (por persona)

| Passo | Todos | Game dev | Eng. C++ | Dev web | Analista Python | Dev Android |
|-------|-------|----------|----------|---------|-----------------|-------------|
| 1 | Rodar template gerado | ✓ plotter + painéis Lua | ✓ build CMake | ✓ abrir arquivos UI | ✓ rodar + editar Python | ✓ Gradle/APK |
| 2 | Ajustar um comportamento visível | Hotkey em `panels.lua` | Novo campo no model | Botão em `ui.xhtml` | Nova função em `functions.py` | Rastrear ponte Djinni |
| 3 | Ligar MVC ponta a ponta | Lua → chamada controller | Comando no controller | Handler TS → C++ | Refresh C++ a partir de Python | Kotlin ↔ eval C++ |
| 4 | Estender autoria | Misturar Lua + XHTML | Nova série ImPlot | Painel lateral completo | Caminho numpy → ImPlot | API Lua `android.*` |
| 5 | Fluxo blueprint | **Edit blueprint** no `:app` | Religar nós do `blueprint.json` | Conectar arestas no editor | Mesmo grafo, nós Python | Mesmo MVC compartilhado |

Guia completo: [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

<details>
<summary><strong>Quando outra stack pode encaixar melhor</strong> (ressalva honesta)</summary>

| Situação | Considere |
|----------|-----------|
| Equipe só web, sem apetite por C++/CMake | Electron ou Tauri — rampa mais rápida para times HTML/CSS |
| UI de marketing pixel-perfect ou design system | Web ou toolkit nativo com motor de layout |
| iOS a partir deste repo hoje | Ainda não entregue — aguarde template iOS v1 ou Swift nativo |
| Projeto novo safety-critical com provas de memória em compile-time | Rust — veja [C++ moderno no Nexus](#c-moderno-no-nexus) abaixo |

**Vale a rampa do Nexus quando:** você precisa de throughput nativo, binários pequenos, paridade SDL3 entre desktop e tablets Android de campo, Python/numpy in-process ou rewiring via blueprint — veja [Por que o Nexus performa melhor](#por-que-o-nexus-performa-melhor). Para crescer passo a passo vs reconstruir do zero, veja [C++ moderno no Nexus](#c-moderno-no-nexus).

</details>

---

## 🐍 Python: Desktop vs Android

O mesmo nó `python.module` no [`blueprint.json`](../../docs/templates/blueprint-schema.md) liga amostragem de curvas nos **dois** templates — só mudam a forma de integrar Python, o empacotamento e a fronteira C++↔Python. Guias dos projetos gerados: [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md).

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Python integrado** | pybind11 — CPython dentro do processo nativo | Chaquopy na JVM; Djinni `ChaquopyPythonBridge` |
| **Árvore de fontes** | `python/` (ex.: `functions.py`) | `app/src/main/python/` |
| **Arquivo** | `misc/python.dat` (PYAC) via CMake `pack_python_dat` | **Nenhum** — Gradle/Chaquopy empacota `.py` no APK |
| **Loader em runtime** | `PythonEngine` em `src/controller/` | `PlotterCore` → Djinni → Kotlin `ChaquopyPythonBridge` |
| **`nxs_config.json`** | `features.python.embedding = "pybind11"` | `features.python.embedding = "chaquopy"` |
| **Rebuild típico** | `cmake --build` (atualiza `python.dat`) | `./gradlew :app:assembleDebug` |

```
blueprint.json  (python.module  →  porta "evaluate")
        │
        ├─ Desktop ───────────────────────────────────────────┐
        │   python/functions.py                               │
        │        │  CMake: pack_python_dat                    │
        │        ▼                                            │
        │   misc/python.dat (PYAC)                            │
        │        │  PythonEngine (pybind11)                    │
        │        ▼                                            │
        │   PlotController → FunctionRegistry → ImPlot        │
        │                                                     │
        └─ Android ───────────────────────────────────────────┤
            app/src/main/python/functions.py                  │
                 │  Gradle + Chaquopy (sem python.dat)       │
                 ▼                                            │
            ChaquopyPythonBridge (Djinni)                     │
                 ▼                                            │
            PlotController → FunctionRegistry → ImPlot  ◄─────┘
```

Ambos os caminhos respeitam a mesma aresta do blueprint: Python `evaluate` → controller `sampleCache` → ImPlot. Veja também [Runtime Desktop vs Android](#arquitetura).

---

## 📝 TypeScript + DSL XHTML

O Nexus expõe **duas camadas de autoria de UI** que descem para a mesma API ImGui/Lua — Lua imperativo para painéis rápidos, TS/XHTML declarativo quando o modelo mental de componentes encaixa melhor. Nenhum caminho usa motor de browser.

### Lua imperativo (`panels.lua`)

Painéis em runtime registram via sol2: `nxs.register_panel(...)` com `ui.button`, `ui.text`, `ui.separator` e `nxs.register_hotkey`. É a camada mais baixa — edite `scripts/panels.lua`, reempacote `lua.dat` opcionalmente, hot-reload sem recompilar C++.

```lua
nxs.register_panel("Quick add", function()
    if ui.button("sin(x)") then nxs.add_function("sine") end
end)
```

### TS/XHTML declarativo (`ui/`)

[`ui/ui.xhtml`](../../template/desktop-app/ui/ui.xhtml) + [`ui/ui.ts`](../../template/desktop-app/ui/ui.ts) descrevem a mesma sidebar e o gráfico em markup e TypeScript. A toolchain desce para definições de painel Lua equivalentes a `panels.lua` — **não** Node nem WebView.

### Fluxo de lowering TS/XHTML
*Markup declarativo e TypeScript → painéis Lua → widgets nativos*

O DSL compartilhado em `template/shared/dsl/` mapeia cada tag XHTML para chamadas Dear ImGui, ImPlot ou imnodes. A saída equivale a `nxs.register_panel` escrito à mão — sem motor de browser.

![Pipeline de lowering TS/XHTML → Lua → ImGui](../../docs/assets/diagrams/tsxhtml-lowering-pipeline.svg)

| Mecanismo | TS/XHTML | Desce para |
|-----------|----------|------------|
| `state()` em `ui.ts` | `bind="sampleCount"` em `<slider>` | Estado two-way do widget ImGui |
| `native()` em `ui.ts` | `items-source="activeCurves"` | Projeção read-only do model C++ (`FunctionRegistry`) |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Mesmos comandos `nxs.*` que Lua chama diretamente |

### `ComponentTag` → widgets nativos

[`template/shared/dsl/tags.ts`](../../template/shared/dsl/tags.ts) mapeia cada tag XHTML para chamada Dear ImGui, ImPlot ou imnodes. [`components.ts`](../../template/shared/dsl/components.ts) fornece classes tipadas por tag; [`core.ts`](../../template/shared/dsl/core.ts) define a base `Component`, props de estilo e callbacks de evento percorridos pelo runtime nativo a cada frame.

| Tag (exemplos) | API nativa |
|----------------|------------|
| `window`, `panel`, `button`, `slider`, `checkbox` | Dear ImGui |
| `plot`, `plot-line`, `plot-scatter`, `plot-bars` | ImPlot |
| `node-editor` | imnodes (`BeginNodeEditor`) |

Widgets-chave do plotter: **Window**, **Panel**, **Button**, **Slider**, **Plot**, **PlotLine**. O futuro painel **imnodes** do blueprint (v1.1) reutiliza a mesma tag `NodeEditor` no mesmo schema.

**Por onde começar:** [template/shared/dsl/](../../template/shared/dsl/) · markup de exemplo [template/desktop-app/ui/ui.xhtml](../../template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md)

---

> *"A perfeição é alcançada não quando não há mais nada a acrescentar, mas quando não há mais nada a retirar."* — Antoine de Saint-Exupéry

## ⚙️ C++ moderno no Nexus

Projetos gerados usam **C++20** com convenções que endereçam dores clássicas de C++ legado. Rust ainda vence em garantias estáticas de segurança — trade-off honesto, não guerra de linguagens.

| Tópico | Templates Nexus (C++20) | Rust (contexto) |
|--------|-------------------------|-------------------|
| **Memória** | `shared_ptr` / RAII; sem ponteiros brutos de posse no template; `.clang-format` em todo projeto | Ownership + borrow checker — garantias estáticas mais fortes |
| **Concorrência** | `std::mutex`, atomics; `std::jthread` onde há threads | Concorrência fearless por padrão |
| **Tooling** | Presets CMake (debug/release), Ninja, `compile_commands.json`, clang-format | `cargo` — excelente, ecossistema diferente |
| **Stack UI / mídia** | ImGui, SDL3, sol2, pybind11, ImPlot — maduros e testados em produção | Sem equivalente ImGui-first direto; egui/wgpu são caminhos distintos |
| **Android NDK** | Djinni + SDL3 GLES — C++ comprovado no device | Possível via FFI, menos turnkey para esta stack |

**Rust costuma ser o default melhor** para serviços safety-critical novos, backends web async ou times já padronizados em `cargo` e `#![deny(unsafe_code)]`.

**C++ moderno + Nexus encaixa** quando você já depende de libs C++ (kernels CAD, codecs, APIs de exchange), precisa de UX ImGui immediate-mode, quer Python pybind11/Chaquopy in-process ou deve entregar a mesma pilha SDL3 no desktop e Android sem reescrever em outra linguagem.

### Crescer passo a passo — não é reconstruir do zero

O Nexus gera apps **C++/SDL3** pensados para crescer camada a camada. Você não precisa descartar um core binário existente e redesenhar toda a infraestrutura em Rust, Go ou outra linguagem só para perseguir performance nativa. Suas libs C/C++, presets CMake, SDKs de fornecedores e cola **Lua**/**Python** in-process continuam de primeira classe: troque superfícies de UI (páginas TS/XHTML, novos painéis ImGui), ligue serviços [`flows.json`](../../docs/templates/flows-schema.md) ou estenda o grafo [`blueprint.json`](../../docs/templates/blueprint-schema.md) sem jogar fora `src/` escrito à mão ou `panels.lua` legado.

Dá para acrescentar peças novas sem jogar o código antigo fora — não é mágica de ABI. Novos nós de blueprint, flows em runtime e telas autoria em XHTML podem conviver com scripts Lua antigos e módulos C++ sob medida no mesmo processo. Times presos em Electron ou Tauri costumam enfrentar um garfo: aceitar overhead de web-shell ou apostar em rewrite completo da stack. O Nexus oferece um terceiro caminho — manter o C++ crítico de performance que você já pagou, modernizar a autoria passo a passo e fazer profile antes de reescrever qualquer coisa em outra linguagem.

> *"Faça funcionar, faça certo, faça rápido — nessa ordem."* — frequentemente atribuído a Kent Beck

**Ressalva honesta:** você ainda mantém código C++, persegue warnings do compilador e assume trade-offs de threading/memória que Rust pegaria em compile-time. O ganho é estratégico: você não é forçado a migrar a stack inteira para escapar de um web shell lento.

---

## 🏗️ Arquitetura

### 📊 Arquitetura full-stack do Nexus
*Cliente, fluxo de geração, templates e runtimes nativos*

![📊 Arquitetura full-stack — cliente Compose → fluxo de geração :core → runtimes SDL3 (seu app, não uma aba de browser)](../../docs/assets/diagrams/full-stack-architecture.svg)

### 📊 Fluxo de geração e builds
*De client-setup e módulos Gradle até `builds/framework/<nome>/`*

![📊 Fluxo de geração e builds — setup JDK → Gradle → ProjectGenerator → builds/framework/<nome>/](../../docs/assets/diagrams/generation-builds-flow.svg)

### 📊 Runtime Desktop vs Android
*MVC compartilhado no SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

![📊 Runtime Desktop vs Android — MVC compartilhado; pybind11 ↔ Chaquopy + Djinni](../../docs/assets/diagrams/desktop-vs-android-runtime.svg)

Blueprint vs Langflow vs n8n: [Blueprint nodes](#blueprint-nodes-langflow-style-vs-n8n) (diagrama lá). Referência de camadas: [docs/architecture/overview.md](../../docs/architecture/overview.md) · Ferramentas do gerador: [O diretório `misc/`](#o-diretório-misc) · Python: [Python: Desktop vs Android](#python-desktop-vs-android) · UI: [TypeScript + DSL XHTML](#typescript--dsl-xhtml)

## 📖 Documentação

| Doc | Descrição |
|-----|-----------|
| [docs/README.md](../../docs/README.md) | Hub de documentação |
| [docs/templates/blueprint-schema.md](../../docs/templates/blueprint-schema.md) | Tipos de nó, arestas e validação do `blueprint.json` |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas |
| [docs/guides/generation-pipeline.md](../../docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | Diagramas de camadas + autoria blueprint |
| [docs/architecture/agent-readiness.md](../../docs/architecture/agent-readiness.md) | Onboarding de agentes IA |
| [docs/architecture/risk-analysis.md](../../docs/architecture/risk-analysis.md) | Riscos de arquitetura |
| [AGENTS.md](AGENTS.md) | Comandos de build para assistentes de código |
| [template/desktop-app/AGENTS.md](../../template/desktop-app/AGENTS.md) | Plotter desktop gerado — pybind11, Lua, TS/XHTML |
| [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md) | Plotter Android gerado — Chaquopy, Djinni |
| [docs/guides/adding-dependencies.md](../../docs/guides/adding-dependencies.md) | Pacotes C++, Lua e Python após client-setup |

## 📎 Adicionando dependências após o setup

Depois do [client-setup](../client-setup/README.md) (JDK 26 + Git) e **Generate Project**, dependências nativas entram no **app gerado** em `builds/framework/<ProjectName>/` — não nos módulos do gerador de projetos Compose. O gerador só escreve templates; CMake, Gradle, pip e `scripts/` são editados nessa árvore de saída.

**C++ (desktop):** instale CMake, Ninja e libs SDL3 do sistema se necessário; estenda o `CMakeLists.txt` do projeto com `FetchContent` (padrão Nexus para SDL3, ImGui, sol2, pybind11) ou vcpkg opcional. Ligue novos alvos em `src/` e recompile com `cmake --build --preset debug`. **C++ (Android):** o mesmo `CMakeLists.txt` é acionado por `externalNativeBuild` em `app/build.gradle.kts`; veja [template/android-app/AGENTS.md](../../template/android-app/AGENTS.md).

**Python:** no desktop, pybind11 — `pip install -r requirements.txt`, edite `python/`, recompile (CMake roda `pack_python_dat`). No Android, Chaquopy — adicione wheels em `chaquopy { pip { install("numpy") } }` em `app/build.gradle.kts` e fontes em `app/src/main/python/`, depois `./gradlew :app:assembleDebug`. Espelhe pacotes em `nxs_config.json` → `features.python.packages` no Android.

**Lua:** coloque `.lua` em `scripts/` e `require` a partir de `panels.lua`; o rebuild empacota `lua.dat`. Sem gerenciador de pacotes — sol2 carrega do arquivo em runtime.

Guia completo, exemplos apt/dnf/pacman por distro e tabela desktop vs Android: **[docs/guides/adding-dependencies.md](../../docs/guides/adding-dependencies.md)**. Stubs de smoke test opcionais: [misc/scripts/test-gen/](../scripts/test-gen/README.md).

## 🚧 Status de desenvolvimento e limitações

**Entregue:** `:app` (Counter + Generate Project + Blueprint Editor), `:core` / `:cli` (geração de templates + `BlueprintValidator`), `template/*`, packs de script (`lua.dat` / `python.dat` no desktop, `lua.dat` no APK Android), `builds/`, `misc/client-setup/`, `docs/`.

**Ainda não:** veja [Rumo ao MVP](#rumo-ao-mvp) para a lista completa (v1 entrega Generate em 2 telas + editores Compose).

**Limitações (v1):** apenas gerador de projetos Compose Desktop; estética ImGui utilitária; Chaquopy aumenta o APK no Android; sem iOS nesta toolchain hoje.

**Branch:** desenvolvimento ativo em **`main`** (`origin/main`).

> *"A primeira regra de qualquer tecnologia usada num negócio é que a automação aplicada a uma operação eficiente amplificará a eficiência."* — Bill Gates

## 🔄 Além da automação rápida: de fluxos para aplicações reais

**Power Automate**, **n8n** e ferramentas similares brilham em cola de ops — webhooks, integrações SaaS, ETL agendado. Isso quebra quando o conserto rápido *é* o produto: sem UI nativa, empacotamento offline fraco, dependência de cloud.

O **Nexus** mantém o modelo mental de nós e arestas no [`blueprint.json`](../../docs/templates/blueprint-schema.md) mas gera uma **aplicação nativa real** — C++/SDL3, Lua/Python, ImGui + TS/XHTML, packs de script, apps desktop/Android. Veja [Blueprint nodes](#blueprint-nodes-langflow-style-vs-n8n) para como isso difere do n8n.

**Caminho de migração:** comece onde você já pensa — ligue módulos no editor de blueprint → gere com `:cli` ou **Generate Project** → itere nas camadas de código normais (`cpp.model`, `python.module`, `ui.page`, painéis Lua) em vez de empilhar remendos no fluxo. Um webhook n8n ou Power Automate pode continuar na borda para cola de ops enquanto o app detém estado, UI e comportamento offline in-process.

**Capacidades além de fluxos:**

| Área | Ferramentas de fluxo (típico) | Saída Nexus |
|------|-------------------------------|-------------|
| **Runtime** | Motor de passos no servidor, UI admin no browser | App desktop/mobile (programa de verdade) ou APK Android |
| **Offline / campo** | Exige conectividade com o host do workflow | App SDL3 offline-first; packs de script no bundle |
| **Performance** | Round-trips HTTP entre passos | C++ amigável a game loop; Python/numpy in-process |
| **Superfície de UI** | Dashboard do vendor ou nenhuma | ImGui + páginas DSL; amostra [plotter estilo Desmos](../../docs/templates/desktop-app.md) |
| **Cross-platform** | Integrações separadas por alvo | Um [`blueprint.json`](../../docs/templates/blueprint-schema.md) liga [desktop + Android](../../docs/assets/diagrams/desktop-vs-android-runtime.svg) |
| **UX de autoria** | Canvas n8n / Power Automate | Editor Compose de blueprint hoje; painel **imnodes** nativo (v1.1) no mesmo schema |

Diagramas: [arquitetura completa](../../docs/assets/diagrams/full-stack-architecture.svg) · [geração → builds](../../docs/assets/diagrams/generation-builds-flow.svg)

> [!WARNING]
> **O Nexus não é n8n nem Power Automate.** Use essas ferramentas para integração SaaS na cloud; use o Nexus quando o fluxo deve virar software entregue.

## 🏁 Rumo ao MVP

Quando cada linha estiver ✅, o Nexus Framework estará **pronto para MVP**: gerar apps nativos, editar blueprints/fluxos, escrever projetos e entregar um build desktop/Android documentado.

> *"Se você não está envergonhado da primeira versão do seu produto, lançou tarde demais."* — Reid Hoffman

| Área | Item | Status |
|------|------|--------|
| Cliente / gerador de projetos | Assistente Compose em 6 passos *(v1 entrega Generate em 2 telas + editores — suficiente para MVP)* | ⬜ |
| Cliente / gerador de projetos | Gerar desktop + android a partir dos templates | ✅ |
| Cliente / gerador de projetos | Editor de blueprint (Compose) | ✅ |
| Cliente / gerador de projetos | Editor de fluxos (lista, ativar/desativar, preview JSON) | ✅ |
| Cliente / gerador de projetos | ProjectGenerator + validadores | ✅ |
| Templates | Templates desktop + android de propósito geral | ✅ |
| Templates | Build de app desktop ponta a ponta verificado no CI | ⬜ |
| Templates | Build APK Android ponta a ponta verificado no CI | ⬜ |
| Templates | Estrutura `blueprint.json` + `flows.json` opcional | ✅ |
| Templates | Stubs TS/XHTML DSL, caminhos Lua e Python | ✅ |
| Runtime | Python integrado via pybind11 no app gerado (Fase 2 — codegen guiado por blueprint) | ⬜ |
| Runtime | Paridade de packs `python.dat` / `lua.dat` | ✅ |
| Runtime | Ponte Chaquopy Android testada E2E em dispositivo | ⬜ |
| Runtime | Compilador TS/XHTML → Lua *(caminho manual via `panels.lua` documentado hoje)* | ⬜ |
| Docs / DX | Seções de arquitetura e comparação no README | ✅ |
| Docs / DX | Guias `AGENTS.md` dos templates | ✅ |
| Docs / DX | CLI `debug validate --all` ou validação equivalente de templates no CI | ⬜ |
| Docs / DX | Scripts `client-setup` (JDK 26) | ✅ |
| Release | CI verde em `main` | ⬜ |
| Release | Binário do cliente publicado (`builds/client/`) | ⬜ |
| Release | Tag de versão `v1.0.0` | ⬜ |

<details>
<summary><strong>Roadmap pós-MVP (v1.1+) — clique para expandir</strong></summary>

Não obrigatório para MVP — acompanhar separadamente:

| Item |
|------|
| Painel imnodes nativo de blueprint (mesmo schema `blueprint.json`) |
| Canvas visual do editor de fluxos |
| Catálogo remoto de templates · template iOS |
| Tipos de passo HTTP/webhook em `flows.json` |
| Polimento do runner SDL3 no Android |

</details>

## 📜 Copyright e licença

> [!IMPORTANT]
> **Licença Apache 2.0** — uso comercial, modificação e distribuição são permitidos. Mantenha avisos de copyright e o arquivo [LICENSE](../../LICENSE) ao redistribuir. O código do app gerado é seu; trechos copiados dos templates devem preservar os avisos Apache.

### Copyright

- © 2026 Nexus Framework contributors — Nexus Framework Client e templates/docs incluídos
- **Projetos gerados:** você é dono do código que o gerador de projetos escreve; trechos copiados dos templates Nexus devem manter o aviso Apache 2.0 onde aparecerem

### Apache License 2.0 — o que significa (linguagem simples)

*Este é um resumo prático, não aconselhamento jurídico.*

- **Uso permissivo:** uso comercial e privado, modificação e distribuição são permitidos
- **Concessão de patentes:** contribuidores concedem direitos de patente necessários para usar o software
- **Atribuição:** mantenha o aviso de copyright, inclua o arquivo [LICENSE](../../LICENSE) e note alterações ao redistribuir
- **Sem garantia:** o software é fornecido "no estado em que se encontra"
- **Marca:** a licença não concede permissão para usar nomes ou marcas do projeto
- **Saída de templates:** o gerador de projetos escreve seu app; você pode licenciar o código gerado como quiser; trechos copiados dos templates Nexus devem manter avisos conforme os termos Apache

Texto completo: [Apache License 2.0](../../LICENSE) · [https://www.apache.org/licenses/LICENSE-2.0](https://www.apache.org/licenses/LICENSE-2.0)

## 🔗 Veja também

*Desenhe o blueprint, gere a árvore, entregue o binário — depois itere nas camadas de código de verdade. Esse é o ciclo Nexus.*

### Ecossistema e dependências

| Tecnologia | Site oficial |
|------------|--------------|
| [SDL3](https://www.libsdl.org/) | Janela, input e superfícies GPU multiplataforma |
| [Dear ImGui](https://github.com/ocornut/imgui) | Widgets de UI immediate-mode |
| [ImPlot](https://github.com/epezent/implot) | Extensão de gráficos para ImGui |
| [sol2](https://github.com/ThePhD/sol2) | Bindings C++ ↔ Lua |
| [pybind11](https://pybind11.readthedocs.io/) | Rodar Python dentro de apps C++ (desktop) |
| [Chaquopy](https://chaquo.com/chaquopy/) | Python no Android (JVM) |
| [Djinni](https://github.com/dropbox/djinni) | Ponte type-safe C++ ↔ Kotlin/Java |
| [Langflow](https://github.com/langflow-ai/langflow) | Editor visual de fluxos para LLM (autoria opcional) |
| [n8n](https://n8n.io/) | Automação de workflows (cola de ops externa) |
| [Kotlin](https://kotlinlang.org/) | Cliente gerador de projetos Compose Desktop |
| [Kotlin Compose](https://www.jetbrains.com/compose-multiplatform/) | UI multiplataforma para `:app` |

<!-- Mantenedor: considere tópicos do repositório GitHub — native-app, scaffolder, sdl3, imgui, kotlin-compose, cpp, lua, python, android, blueprint, langflow, open-source -->

### Repositórios relacionados

| Repositório | Papel |
|-------------|-------|
| [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) | Distribuição separada do wizard `:client-desktop` |
