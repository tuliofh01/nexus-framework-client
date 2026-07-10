# O Framework da Nexus Company para Desenvolvimento de Aplicações Nativas

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Logo do The Nexus Framework" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

**The Nexus Framework** gera aplicações nativas em C++/Lua/Python para **Desktop** (Windows, macOS, Linux) e **Android** — janela SDL3, scripts sol2, autoria de UI TypeScript + XHTML e Python embarcado (pybind11 no desktop, Chaquopy + Djinni no Android). ImGui + ImPlot renderizam UI immediate-mode sem motor de browser.

Se você está avaliando stacks **web-shell** — **Electron** (Chromium + JavaScript) ou **Tauri** (WebView do SO + Rust) — o Nexus aposta em outro caminho: runtime C++ nativo, widgets immediate-mode e Lua/Python in-process em vez de motores de layout HTML. Essas ferramentas brilham quando DOM/CSS é a superfície do produto; o Nexus brilha quando throughput, tamanho de binário e uma pilha SDL3 compartilhada entre desktop e tablets Android de campo importam mais.

## Blueprint nodes: Langflow-style vs n8n

O Nexus inclui um **grafo de app estilo Langflow** na raiz do projeto. Nós declaram módulos (`python.module`, `cpp.model`, `ui.page`, …); arestas ligam fluxo de dados e comandos dentro do app MVC gerado. A **geração em `:core`** valida e consome o grafo ao materializar `builds/framework/<nome>/`.

![Langflow vs n8n vs blueprint Nexus — DAG tipado vs automação vs codegen em design-time](docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

| | **Langflow** | **n8n** | **Nexus `blueprint.json`** |
|---|-------------|---------|---------------------------|
| **Propósito** | Autoria de fluxos ML/LLM | Automação (gatilhos, HTTP, integrações) | Estrutura de app nativo em **design-time** |
| **Modelo de nó** | Componentes tipados (modelo, ferramenta, memória) | Passos + gatilhos (webhook, cron, Slack, …) | Módulos tipados (`python.module`, `cpp.model`, …) |
| **Arestas** | Dados entre componentes | Roteamento de evento / payload | Portas MVC (`evaluate`, `sampleCache`, `commands`, …) |
| **Execução** | **Runtime** — usuário executa o fluxo | **Runtime** — agenda ou webhook dispara | **Design-time** — `ProjectGenerator` valida e emite |


| Tipo de nó | Papel |
|------------|-------|
| `python.module` | Amostragem / analytics Python (`python/functions.py`) |
| `cpp.model` | Estado de domínio C++ (`FunctionRegistry`, caches) |
| `cpp.controller` | Comandos + orquestração (`PlotController`) |
| `ui.page` | Página TS/XHTML (`ui/ui.ts`, `ui/ui.xhtml`) |
| `lua.script` | Painéis Lua em runtime (`scripts/panels.lua`) |

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit blueprint** (canvas Compose + inspetor JSON na v1; painel nativo **imnodes** previsto na v1.1 — mesmo schema). Amostras: [template/desktop-app/blueprint.json](template/desktop-app/blueprint.json) · [template/android-app/blueprint.json](template/android-app/blueprint.json). Schema: [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md).

### Nós estilo Langflow vs n8n

Ambas as ferramentas usam o **modelo mental de nós e arestas**, mas resolvem camadas diferentes. O `blueprint.json` do Nexus é mais próximo do **Langflow** (grafo tipado dentro do app) do que do **n8n** (automação de workflows externos). O Nexus **não substitui** o n8n; os dois podem coexistir. Para quando um fluxo deve virar software nativo entregue — não só cola de ops — veja [Além da automação rápida](#além-da-automação-rápida-de-fluxos-para-aplicações-reais) abaixo.

| | **Nexus `blueprint.json`** (estilo Langflow) | **n8n** |
|---|---------------------------------------------|---------|
| **Propósito** | Autorar a **estrutura interna do app** — quais módulos C++/Python/Lua/UI se conectam e como os dados fluem | Automatizar **workflows externos** — webhooks, APIs REST, agendamentos, integrações SaaS |
| **Tipos de nó** | `python.module`, `cpp.model`, `cpp.controller`, `ui.page`, `lua.script` | HTTP Request, Webhook, Cron, Slack, Postgres, … |
| **Modelo de execução** | O grafo é consumido na **geração**; o runtime é C++/Lua/Python compilado no SDL3 | Motor de workflow no servidor executa passos por gatilhos ou cron |
| **Onde roda** | Dentro do binário desktop ou APK Android gerado | Instância n8n (cloud ou self-hosted) |
| **Quando usar** | Reconfigurar MVC do plotter, adicionar telas, mapear amostras Python → controller → UI | Automação de ops, ETL, alertas, cola entre serviços de terceiros |

**Coexistência:** um app Nexus gerado pode chamar um webhook n8n a partir de Python ou Lua (ex.: enviar telemetria, disparar pipeline downstream) enquanto o `blueprint.json` cuida apenas da **fiação interna** do app — a mesma separação que o Langflow usa para cadeias LLM vs. o que o n8n usa para fluxos de integração. **Ganchos estilo n8n** no schema do blueprint são roadmap; os tipos v1 são todos estilo Langflow (`editor.paradigm: langflow`).

**Mapeamento no cliente:** **Edit blueprint** no `:app` espelha o canvas do Langflow — arrastar nós, conectar portas, pré-visualizar JSON. A v1.1 embute **imnodes** nativamente com o mesmo arquivo; sem migração de schema prevista.

**Exemplos visuais:** Veja [chatbot RAG](docs/assets/examples/langflow-rag-chatbot.svg), [agente com ferramentas](docs/assets/examples/langflow-agent-tools.svg) e [estrutura de app no blueprint Nexus](docs/assets/examples/nexus-blueprint-app-structure.svg) para grafos estilo Langflow e como o Nexus traduz o mesmo modelo mental para `blueprint.json` em design-time.

## Fluxos de runtime opcionais (serviços)

O Nexus separa **estrutura em design-time** de **automação em runtime**:

| Camada | Arquivo | Propósito |
|--------|---------|-----------|
| Estrutura do app | `blueprint.json` | Fiação MVC estilo Langflow (módulos, portas, telas) |
| Fluxos de runtime | `flows/flows.json` | Serviços opcionais in-app — loops em background, gatilhos por evento, agendamentos |

**Editar no cliente:** `./gradlew :app:run` → **Generate Project** → **Edit flows** — listar fluxos, habilitar/desabilitar, pré-visualizar JSON (editor visual na v1.1). Schema: [docs/templates/flows-schema.md](docs/templates/flows-schema.md).

### Caminhos de adoção

| Caminho | O que você entrega |
|---------|-------------------|
| **Sem fluxos** | Omitir ou desabilitar — app totalmente customizado; o plotter funciona sem FlowRunner |
| **Fluxos como helpers** | Pequenos serviços de automação (timers, hooks de evento) dentro de um app maior |
| **Híbrido** | MVC via blueprint + fluxos background/triggered no mesmo binário |

### Background vs triggered

| Modo | Quando executa | Exemplo de gatilho |
|------|----------------|-------------------|
| `background` | Enquanto o app está vivo | `interval` a cada 5000 ms |
| `triggered` | Só na condição | `event` `curve.added`, `startup`, `manual` |

Adicione vários fluxos no array `flows` de `flows.json` (cada um com `id` único). Desabilite globalmente com `nxs_config.json` → `"flows": { "enabled": false }` ou por fluxo com `"enabled": false`.

### Usando o Langflow para autoria de fluxos

O [Langflow](https://github.com/langflow-ai/langflow) é um editor visual de DAG para cadeias LLM, ferramentas, retrievers e agentes. O Nexus **não** embute o runtime do Langflow na v1 — você pode usar o Langflow como **ferramenta externa opcional de autoria**, exportar o JSON do fluxo e **adotar** o grafo como serviços nativos em `flows.json` dentro do app gerado. Para como o Langflow se relaciona com `blueprint.json` (estrutura) vs n8n (automação de ops), veja [Nós estilo Langflow vs n8n](#nós-estilo-langflow-vs-n8n) acima.

#### Passo A — Projetar no Langflow (ferramenta externa opcional)

1. Monte um DAG visual no Langflow: nós (LLM, Prompt, Tool, Retriever, Agent, …), arestas e parâmetros por nó.
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

**`blueprint.json` vs `flows.json`:** grafos Langflow que ligam **estrutura do app** (telas, controllers, módulos Python) mapeiam melhor para [`blueprint.json`](docs/templates/blueprint-schema.md). Grafos de **automação em runtime** (timers, hooks de evento, tarefas em background) mapeiam para **`flows.json`**. Um único canvas Langflow pode ser dividido nos dois arquivos após a tradução.

#### Passo C — Fluxo de adoção

1. **Exportar** JSON do Langflow.
2. **Traduzir** nomes de campo para o schema Nexus `flows.json` ([docs/templates/flows-schema.md](docs/templates/flows-schema.md)) — manual na v1; importador Langflow previsto para v1.1.
3. **Colocar** o resultado em `flows/flows.json` no projeto gerado, ou colar na tela **Edit flows** do cliente (`./gradlew :app:run` → **Generate Project** → **Edit flows**).
4. **Habilitar** em `nxs_config.json` → `"flows": { "enabled": true }`.
5. Na inicialização do app, o **FlowRunner** carrega `flows.json` e registra gatilhos (intervalos, eventos, hooks de startup).
6. **Handlers customizados** — você pode escrever `flows.json` à mão ou a partir de Python/Lua sem abrir o Langflow.

#### Limites honestos (v1)

- **Sem importador automático do Langflow** — exportação → mapeamento manual hoje; importador alvo da v1.1.
- **Sem orquestração de webhooks SaaS na cloud** — diferente de fluxos estilo n8n; chame APIs externas via alvos `invoke` em Python/Lua quando precisar.
- **Nós LLM viram stubs** — componentes LLM/Agent do Langflow mapeiam para passos `invoke`; a chamada real ao modelo deve ficar em um `python.module` (ou tipo de passo dedicado futuro).
- **Runtime Langflow não incluído** — o Nexus roda C++/Lua/Python nativo no SDL3; não executa o motor de grafo server-side do Langflow.

**Não é n8n na v1:** fluxos são serviços **locais, in-process** dentro do app nativo. Não substituem orquestração de webhooks na nuvem (n8n, Power Automate). Chame webhooks externos via Python/Lua quando precisar; use `flows.json` para timers e eventos do app. Passos HTTP/webhook estão previstos para v1.1.

Amostra: [template/desktop-app/flows/flows.json](template/desktop-app/flows/flows.json).

## O que é este repositório

| Camada | Hoje | Roadmap (v1.1+) |
|--------|------|-----------------|
| **Cliente (`:app`)** | Compose Desktop — demo MVC + **Generate Project** + **Blueprint Editor** + **Flows Editor** | Assistente em 6 passos, painel imnodes nativo, canvas visual de fluxos (v1.1) |
| **Geração ([`misc/core`, `misc/cli`](#o-diretório-misc))** | `ProjectGenerator` + `BlueprintValidator` + `FlowsValidator` → `builds/framework/<nome>/` | Catálogo remoto, packs `python.dat` / `lua.dat` |
| **Templates** | Plotter desktop + Android com `blueprint.json` compartilhado | Template iOS |
| **Autoria** | JSON de nós estilo Langflow + TS/XHTML + Lua | Packs de script criptografados |

Este é o monorepo **Framework** (`:app`, `:core`, `:cli`). Não é o repositório separado [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (wizard `:client-desktop` lá).

## Primeira execução

Execute um script de setup da plataforma, carregue o arquivo de ambiente e use o Gradle:

| Plataforma | Setup | Ambiente |
|------------|-------|----------|
| Linux | `./misc/client-setup/linux/setup.sh` | `source misc/client-setup/env.sh` |
| macOS | `./misc/client-setup/macos/setup.sh` | `source misc/client-setup/env.sh` |
| Windows | `misc\client-setup\windows\setup.bat` | `call misc\client-setup\env.bat` |

Exige **JDK 26** e Git — veja [misc/client-setup/README.md](misc/client-setup/README.md).

## Início rápido

```bash
source misc/client-setup/env.sh          # após setup na primeira execução
./gradlew :app:run                  # cliente Compose
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./gradlew :cli:run --args="generate --type desktop --name MyApp"
```

Compilar e testar: `./gradlew :core:compileKotlin :cli:compileKotlin :app:compileKotlin :app:test`

Deploy do cliente: `./gradlew :app:deployToBuildsClient` → [builds/client/app/](builds/client/app/)

Build do template desktop: `cd template/desktop-app && cmake --preset debug && cmake --build --preset debug`

Layout de saída: [builds/README.md](builds/README.md) · Templates: [template/README.md](template/README.md)

## Estrutura do repositório

```
Framework/
├── app/                 Cliente Compose Desktop (`:app`) — MVC em `nexus.opensource/`
├── misc/                Tooling + pipeline de geração — veja [O diretório `misc/`](#o-diretório-misc)
├── builds/              Cliente → builds/client/ · apps → builds/framework/<nome>/
├── template/            desktop-app · android-app · shared
├── docs/                Hub de documentação → docs/README.md
└── Jenkinsfile          Entrada de pipeline opcional (→ misc/jenkins/)
```

## O diretório `misc/`

A pasta `misc/` consolida **tooling do repositório Framework** — módulos Gradle, plugins de convenção, setup na primeira execução, imagens de container, notas de CI e scripts auxiliares. Nada disso vai para apps nativos gerados em `builds/framework/<nome>/`; serve apenas para construir e executar o scaffolder.

| Caminho | Gradle / papel |
|---------|----------------|
| [misc/core/](misc/core/) | `:core` — `ProjectGenerator`, `TemplateEngine`, schema `nxs_config.json` (v2) |
| [misc/cli/](misc/cli/) | `:cli` — comando headless `generate` |
| [misc/build-logic/](misc/build-logic/) | Included build (antes `buildSrc` na raiz) — toolchain JVM 26, plugins de convenção |
| [misc/client-setup/](misc/client-setup/) | Instaladores na primeira execução (Linux/macOS/Windows) para JDK 26 + Git; `env.sh` / `env.bat` definem `JAVA_HOME` antes do Gradle |
| [misc/scripts/](misc/scripts/) | Automação do repo — [dev/](misc/scripts/dev/) (build/validação/execução do client), [test-gen/](misc/scripts/test-gen/) (stubs smoke/instrumentados em `builds/framework/`), [generate-diagrams/](misc/scripts/generate-diagrams/) (SVGs de docs) |
| [misc/docker/](misc/docker/) | `Dockerfile` + compose para geração containerizada |
| [misc/jenkins/](misc/jenkins/) | Setup Jenkins opcional — veja [misc/jenkins/README.md](misc/jenkins/README.md) |

O Gradle expõe `:core` e `:cli` na raiz do repo enquanto os fontes ficam em `misc/` — veja [settings.gradle.kts](settings.gradle.kts):

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
| [app/](app/) | Scaffolder Compose Desktop (`:app`) — Generate Project, editores de blueprint/fluxos |
| [template/](template/) | Templates-fonte copiados para `builds/framework/<nome>/` |
| [builds/](builds/) | Saídas de deploy — cliente em `builds/client/`, apps gerados em `builds/framework/` |
| [docs/](docs/) | Hub de documentação |

**Comandos rápidos** (caminhos em `misc/`):

```bash
./misc/client-setup/linux/setup.sh && source misc/client-setup/env.sh
./gradlew :core:compileKotlin
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
./misc/scripts/dev/nexus-dev.sh compile
./misc/scripts/dev/generate-in-docker.sh desktop MyApp builds/framework/MyApp
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture
```

Hub: [misc/README.md](misc/README.md) · pipeline: [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md)

### Scripts de setup inicial (`misc/client-setup/`)

Execute **uma vez** antes do primeiro `./gradlew :app:run`. Os instaladores em [misc/client-setup/](misc/client-setup/) provisionam **JDK 26** e **Git** (mais ferramentas de build recomendadas no Linux). Depois, carregue o arquivo de ambiente gerado para o Gradle encontrar `JAVA_HOME`:

```bash
./misc/client-setup/linux/setup.sh   # ou macos/setup.sh / windows/setup.bat
source misc/client-setup/env.sh      # Windows: call misc\client-setup\env.bat
./gradlew :app:run
```

Helpers Linux por distro (`setup-arch.sh`, `setup-debian.sh`, `setup-fedora.sh`) e troubleshooting: [misc/client-setup/README.md](misc/client-setup/README.md).

### Testes automatizados e geração de testes (`misc/scripts/test-gen/`)

[test-gen/](misc/scripts/test-gen/) gera stubs de smoke e testes instrumentados para **apps já gerados** em `builds/framework/<projeto>/` — não para o scaffolder em si. Lê `nxs_config.json`, detecta desktop vs Android e grava arquivos idempotentes (smoke C++ desktop via CTest; stubs Kotlin `androidTest`). A geração é idempotente; use `--force` para sobrescrever.

Os entry points por plataforma envolvem o núcleo compartilhado em [test-gen/common/generate-tests.sh](misc/scripts/test-gen/common/generate-tests.sh):

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
./gradlew :core:test   # testes Gradle do scaffolder (:core, :cli, :app)
```

Para build/validação/execução local do client Compose, use [misc/scripts/dev/nexus-dev.sh](misc/scripts/dev/nexus-dev.sh) (`compile`, `generate`, `docker`, …). Uso completo: [misc/scripts/test-gen/README.md](misc/scripts/test-gen/README.md).

### Outros scripts do repo

| Família | Entry point | Propósito |
|---------|-------------|-----------|
| `dev/` | `./misc/scripts/dev/nexus-dev.sh compile` | Workflow Gradle local + geração Docker |
| `generate-diagrams/` | `python3 misc/scripts/generate-diagrams/generate-styled-diagrams.py` | Regenerar SVGs de docs |

## Casos de uso — para que o Nexus foi feito

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

Exemplo principal: **plotter estilo Desmos** — Python amostra curvas, C++ possui o modelo, ImGui desenha. [docs/templates/desktop-app.md](docs/templates/desktop-app.md) · [docs/templates/android-app.md](docs/templates/android-app.md)

---

## Por que o Nexus performa melhor

O Nexus foi feito para **throughput, footprint e implantação em campo** — não para renderizar sites de marketing. Onde o Electron embute Chromium e o Tauri delega ao WebView do SO, apps Nexus gerados permanecem em endereço nativo de ponta a ponta: lógica de domínio em C++, superfícies GPU SDL3, widgets ImGui/ImPlot e camadas opcionais Lua/Python sem processo de browser.

| Vantagem | O que significa na prática | Contexto Electron / Tauri |
|----------|----------------------------|---------------------------|
| **Tamanho de binário** | ~3–20 MB binário nativo + assets (cresce com `libs/` vendored) | Instaladores Electron comuns **85–250 MB** (Chromium embutido); Tauri tipicamente **3–15 MB**, mas ainda leva WebView + bundle frontend |
| **Sem Chromium / WebView** | UI é ImGui + SDL3/OpenGL — sem subprocesso renderer, sem layout/paint DOM | Electron = stack browser completo; Tauri = WebView do SO + runtime JS |
| **Memória nativa para arrays** | Malhas, order books e buffers numpy ficam no heap C++; Python via pybind11/Chaquopy sem marshaling via JS | Web shells copiam ou serializam dados através de fronteiras JS |
| **SDL3 multiplataforma** | Mesma camada de janela/input no Windows, macOS, Linux e Android GLES | Mobile é secundário ou toolchain separada na maioria das stacks web-shell |
| **sol2 + hot-reload Lua** | Edite `panels.lua`, reempacote `lua.dat` opcional — painéis UI em runtime sem recompilar C++ | HMR de frontend ajuda, mas ainda é round-trip HTML/CSS/JS |
| **Proteção `python.dat` / `lua.dat`** | Packs criptografados v2 opcionais distribuem lógica sem `.py`/`.lua` soltos em disco (desktop `misc/`; Android `lua.dat` em assets do APK) | Não é preocupação de primeira classe em modelos de asset típicos Electron/Tauri |
| **Refresh ImGui sub-ms** | UI immediate-mode mira **<1 ms** por orientação Dear ImGui; sem layout thrash | Ciclos de layout + paint do WebView dominam CPU em steady state |
| **APK tablet de campo** | Template Android: ImGui SDL3/GLES full-screen + Python Chaquopy em devices rugged — sem WebView | Electron Android não é foco; Tauri Mobile continua WebView-based |
| **Mesmo blueprint, desktop + Android** | Um fluxo `blueprint.json` / imnodes liga MVC nos dois templates | Pipelines web + mobile separados são comuns |
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
| Kotlin Compose | Só cliente scaffold | Wizard `:app` — não é o app gerado |

### Caminho de progressão (por persona)

| Passo | Todos | Game dev | Eng. C++ | Dev web | Analista Python | Dev Android |
|-------|-------|----------|----------|---------|-----------------|-------------|
| 1 | Rodar template gerado | ✓ plotter + painéis Lua | ✓ build CMake | ✓ abrir arquivos UI | ✓ rodar + editar Python | ✓ Gradle/APK |
| 2 | Ajustar um comportamento visível | Hotkey em `panels.lua` | Novo campo no model | Botão em `ui.xhtml` | Nova função em `functions.py` | Rastrear ponte Djinni |
| 3 | Ligar MVC ponta a ponta | Lua → chamada controller | Comando no controller | Handler TS → C++ | Refresh C++ a partir de Python | Kotlin ↔ eval C++ |
| 4 | Estender autoria | Misturar Lua + XHTML | Nova série ImPlot | Painel lateral completo | Caminho numpy → ImPlot | API Lua `android.*` |
| 5 | Fluxo blueprint | **Edit blueprint** no `:app` | Religar nós do `blueprint.json` | Conectar arestas no editor | Mesmo grafo, nós Python | Mesmo MVC compartilhado |

Guia completo: [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

<details>
<summary><strong>Quando outra stack pode encaixar melhor</strong> (ressalva honesta)</summary>

| Situação | Considere |
|----------|-----------|
| Equipe só web, sem apetite por C++/CMake | Electron ou Tauri — rampa mais rápida para times HTML/CSS |
| UI de marketing pixel-perfect ou design system | Web ou toolkit nativo com motor de layout |
| iOS a partir deste repo hoje | Ainda não entregue — aguarde template iOS v1 ou Swift nativo |
| Greenfield safety-critical com provas de memória em compile-time | Rust — veja [C++ moderno no Nexus](#c-moderno-no-nexus) abaixo |

**Vale a rampa do Nexus quando:** você precisa de throughput nativo, binários pequenos, paridade SDL3 entre desktop e tablets Android de campo, Python/numpy in-process ou rewiring via blueprint sem motor de browser.

</details>

---

## Python: Desktop vs Android

O mesmo nó `python.module` no [`blueprint.json`](docs/templates/blueprint-schema.md) liga amostragem de curvas nos **dois** templates — só mudam o embed, o empacotamento e a fronteira C++↔Python. Guias dos projetos gerados: [template/desktop-app/AGENTS.md](template/desktop-app/AGENTS.md) · [template/android-app/AGENTS.md](template/android-app/AGENTS.md).

| | **Desktop** | **Android** |
|---|-------------|-------------|
| **Embedding** | pybind11 — CPython dentro do processo nativo | Chaquopy na JVM; Djinni `ChaquopyPythonBridge` |
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
        │        │  PythonEngine (embed pybind11)             │
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

Ambos os caminhos respeitam a mesma aresta do blueprint: Python `evaluate` → controller `sampleCache` → caches do model → desenho ImPlot. Desktop favorece binário nativo único com packs de script criptografados opcionais; Android favorece Python gerenciado pela JVM com JNI type-safe via Djinni. Veja [Runtime Desktop vs Android](docs/assets/diagrams/desktop-vs-android-runtime.svg).

---

## TypeScript + DSL XHTML

O Nexus expõe **duas camadas de autoria de UI** que descem para a mesma API ImGui/Lua — Lua imperativo para painéis rápidos, TS/XHTML declarativo quando o modelo mental de componentes encaixa melhor. Nenhum caminho usa motor de browser.

### Lua imperativo (`panels.lua`)

Painéis em runtime registram via sol2: `nxs.register_panel(...)` com `ui.button`, `ui.text`, `ui.separator` e `nxs.register_hotkey`. É a camada mais baixa — edite `scripts/panels.lua`, reempacote `lua.dat` opcionalmente, hot-reload sem recompilar C++.

```lua
nxs.register_panel("Quick add", function()
    if ui.button("sin(x)") then nxs.add_function("sine") end
end)
```

### TS/XHTML declarativo (`ui/`)

[`ui/ui.xhtml`](template/desktop-app/ui/ui.xhtml) + [`ui/ui.ts`](template/desktop-app/ui/ui.ts) descrevem a mesma sidebar e o gráfico em markup e TypeScript. A toolchain desce para definições de painel Lua equivalentes a `panels.lua` — **não** Node nem WebView.

| Mecanismo | TS/XHTML | Desce para |
|-----------|----------|------------|
| `state()` em `ui.ts` | `bind="sampleCount"` em `<slider>` | Estado two-way do widget ImGui |
| `native()` em `ui.ts` | `items-source="activeCurves"` | Projeção read-only do model C++ (`FunctionRegistry`) |
| `invoke("nxs.add_function", …)` | `on-click="addPending"` | Mesmos comandos `nxs.*` que Lua chama diretamente |

### `ComponentTag` → widgets nativos

[`template/shared/dsl/tags.ts`](template/shared/dsl/tags.ts) mapeia cada tag XHTML para chamada Dear ImGui, ImPlot ou imnodes. [`components.ts`](template/shared/dsl/components.ts) fornece classes tipadas por tag; [`core.ts`](template/shared/dsl/core.ts) define a base `Component`, props de estilo e callbacks de evento percorridos pelo runtime nativo a cada frame.

| Tag (exemplos) | API nativa |
|----------------|------------|
| `window`, `panel`, `button`, `slider`, `checkbox` | Dear ImGui |
| `plot`, `plot-line`, `plot-scatter`, `plot-bars` | ImPlot |
| `node-editor` | imnodes (`BeginNodeEditor`) |

Widgets-chave do plotter: **Window**, **Panel**, **Button**, **Slider**, **Plot**, **PlotLine**. O futuro painel **imnodes** do blueprint (v1.1) reutiliza a mesma tag `NodeEditor` no mesmo schema.

**Por onde começar:** [template/shared/dsl/](template/shared/dsl/) · markup de exemplo [template/desktop-app/ui/ui.xhtml](template/desktop-app/ui/ui.xhtml) · [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

---

## C++ moderno no Nexus

Projetos gerados usam **C++20** com convenções que endereçam dores clássicas de C++ legado. Rust ainda vence em garantias estáticas de segurança — trade-off honesto, não guerra de linguagens.

| Tópico | Templates Nexus (C++20) | Rust (contexto) |
|--------|-------------------------|-------------------|
| **Memória** | `shared_ptr` / RAII; sem ponteiros brutos de posse no template; `.clang-format` em todo projeto | Ownership + borrow checker — garantias estáticas mais fortes |
| **Concorrência** | `std::mutex`, atomics; `std::jthread` onde há threads | Concorrência fearless por padrão |
| **Tooling** | Presets CMake (debug/release), Ninja, `compile_commands.json`, clang-format | `cargo` — excelente, ecossistema diferente |
| **Stack UI / mídia** | ImGui, SDL3, sol2, pybind11, ImPlot — maduros e testados em produção | Sem equivalente ImGui-first direto; egui/wgpu são caminhos distintos |
| **Android NDK** | Djinni + SDL3 GLES — C++ comprovado no device | Possível via FFI, menos turnkey para esta stack |

**Rust costuma ser o default melhor** para serviços greenfield safety-critical, backends web async ou times já padronizados em `cargo` e `#![deny(unsafe_code)]`.

**C++ moderno + Nexus encaixa** quando você já depende de libs C++ (kernels CAD, codecs, APIs de exchange), precisa de UX ImGui immediate-mode, quer Python pybind11/Chaquopy in-process ou deve entregar a mesma pilha SDL3 no desktop e Android sem reescrever em outra linguagem.

---

## Arquitetura

### Arquitetura full-stack do Nexus
*Cliente, pipeline de geração, templates e runtimes nativos*

O mapa ponta a ponta do scaffolder Compose pela geração em `:core` até apps C++/Lua/Python no SDL3. Consulte primeiro no onboarding ou ao explicar como autoria de blueprint, emissão de template e camadas de runtime se conectam. Observe `:app`, `:core`/`:cli`, `blueprint.json` e as pontes Python/Djinni.

![Arquitetura full-stack do Nexus](docs/assets/diagrams/full-stack-architecture.svg)

### Fluxo de geração e builds
*De client-setup e módulos Gradle até `builds/framework/<nome>/`*

Rastreia o caminho do setup inicial de JDK pela geração em `:app`/`:cli` até templates emitidos em `builds/framework/`. Use ao depurar falhas de geração ou explicar o layout de deploy. Paradas-chave: `client-setup`, `ProjectGenerator` e o passo de build nativo.

![Fluxo de geração e builds](docs/assets/diagrams/generation-builds-flow.svg)

### Runtime Desktop vs Android
*MVC compartilhado no SDL3/ImGui; pybind11 vs Chaquopy + Djinni*

Mostra como o mesmo grafo de nós do blueprint compila para desktop (pybind11, `python.dat`) vs Android (Chaquopy, Djinni). Consulte ao escolher template ou ligar `python.module` entre plataformas. Observe o caminho compartilhado controller/model e onde a ponte Python diverge.

![Runtime Desktop vs Android](docs/assets/diagrams/desktop-vs-android-runtime.svg)

### Langflow vs n8n vs blueprint Nexus
*DAG tipado in-app vs automação de workflow em runtime vs codegen em design-time*

Contrasta Langflow (fluxos LLM em runtime), n8n (automação externa) e `blueprint.json` do Nexus (estrutura em build-time). Leia antes de editar blueprints ou explicar por que o Nexus não substitui o n8n. Tipos como `python.module` e `ui.page` mapeiam para artefatos gerados, não passos de webhook.

![Langflow vs n8n vs blueprint Nexus](docs/assets/diagrams/langflow-vs-n8n-blueprint.svg)

Referência de camadas: [docs/architecture/overview.md](docs/architecture/overview.md) · Tooling do scaffold: [O diretório `misc/`](#o-diretório-misc) · Divisão Python: [Python: Desktop vs Android](#python-desktop-vs-android) · Autoria UI: [TypeScript + DSL XHTML](#typescript--dsl-xhtml)

## Documentação

| Doc | Descrição |
|-----|-----------|
| [docs/README.md](docs/README.md) | Hub de documentação |
| [docs/templates/blueprint-schema.md](docs/templates/blueprint-schema.md) | Tipos de nó, arestas e validação do `blueprint.json` |
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas |
| [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/architecture/overview.md](docs/architecture/overview.md) | Diagramas de camadas + autoria blueprint |
| [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md) | Onboarding de agentes IA |
| [docs/architecture/risk-analysis.md](docs/architecture/risk-analysis.md) | Riscos de arquitetura |
| [AGENTS.md](AGENTS.md) | Comandos de build para assistentes de código |
| [template/desktop-app/AGENTS.md](template/desktop-app/AGENTS.md) | Plotter desktop gerado — pybind11, Lua, TS/XHTML |
| [template/android-app/AGENTS.md](template/android-app/AGENTS.md) | Plotter Android gerado — Chaquopy, Djinni |

## Status de desenvolvimento e limitações

**Entregue:** `:app` (Counter + Generate Project + Blueprint Editor), `:core` / `:cli` (emissão + `BlueprintValidator`), `template/*`, packs de script (`lua.dat` / `python.dat` no desktop, `lua.dat` no APK Android), `builds/`, `misc/client-setup/`, `docs/`.

**Ainda não:** wizard completo em 6 passos, painel **imnodes nativo** (v1 usa editor Compose JSON), catálogo remoto, template iOS, polimento SDL3 Android.

**Limitações (v1):** apenas scaffolder Compose Desktop; estética ImGui utilitária; Chaquopy aumenta o APK no Android; sem iOS nesta toolchain hoje.

**Branch:** desenvolvimento ativo em **`main`** (`origin/main`).

---

## Além da automação rápida: de fluxos para aplicações reais

**Power Automate**, **n8n** e ferramentas similares ocupam um sweet spot claro: scripts de cola, cadeias de webhook, integrações SaaS e “consertos rápidos” que mantêm as operações rodando. Elas brilham em automação de ops — conectar Slack ao Postgres, espalhar webhooks, ETL agendado — sem ninguém entregar um binário de produto.

Essa força vira limitação quando o conserto rápido deveria *ser* o produto. Canvases de fluxo não oferecem UI/runtime nativo além do shell do vendor, empacotamento fraco para uso offline ou em campo, e dependência de cloud que faz de binário desktop ou mobile um detalhe secundário. Crescer um fluxo até virar app com várias funções costuma significar remendar nós para sempre: frágil, difícil de testar e caro de versionar como software de verdade.

O **Nexus** mantém o mesmo modelo mental em **design-time** — nós e arestas no [`blueprint.json`](docs/templates/blueprint-schema.md) (veja [Nós estilo Langflow vs n8n](#nós-estilo-langflow-vs-n8n) acima) — mas a geração em `:core` emite uma **aplicação nativa real**: janelas C++/SDL3, camadas Lua e Python, páginas ImGui + DSL TS/XHTML, packs de script criptografados (`lua.dat` / `python.dat`) e binários Android/desktop a partir de um único scaffold. O grafo é autoral como no Langflow; o artefato é MVC compilado no SDL3, não um motor de workflow no servidor.

**Caminho de migração:** comece onde você já pensa — ligue módulos no editor de blueprint → gere com `:cli` ou **Generate Project** → itere nas camadas de código normais (`cpp.model`, `python.module`, `ui.page`, painéis Lua) em vez de empilhar remendos no fluxo. Um webhook n8n ou Power Automate pode continuar na borda para cola de ops enquanto o app detém estado, UI e comportamento offline in-process.

**Capacidades além de fluxos:**

| Área | Ferramentas de fluxo (típico) | Saída Nexus |
|------|-------------------------------|-------------|
| **Runtime** | Motor de passos no servidor, UI admin no browser | Binário desktop nativo ou APK Android |
| **Offline / campo** | Exige conectividade com o host do workflow | App SDL3 offline-first; packs de script no bundle |
| **Performance** | Round-trips HTTP entre passos | C++ amigável a game loop; Python/numpy in-process |
| **Superfície de UI** | Dashboard do vendor ou nenhuma | ImGui + páginas DSL; amostra [plotter estilo Desmos](docs/templates/desktop-app.md) |
| **Cross-platform** | Integrações separadas por alvo | Um [`blueprint.json`](docs/templates/blueprint-schema.md) liga [desktop + Android](docs/assets/diagrams/desktop-vs-android-runtime.svg) |
| **UX de autoria** | Canvas n8n / Power Automate | Editor Compose de blueprint hoje; painel **imnodes** nativo (v1.1) no mesmo schema |

Veja a [arquitetura completa](docs/assets/diagrams/full-stack-architecture.svg) e o [fluxo geração → builds](docs/assets/diagrams/generation-builds-flow.svg) para entender como o grafo de design-time vira código de runtime.

**Ressalva honesta:** o Nexus **não** substitui Power Automate ou n8n quando o problema é puramente **orquestração de webhooks na cloud** entre APIs SaaS. Use essas ferramentas para cola de integração; use o Nexus quando o fluxo-conserto-rápido deve virar software entregue — superfície nativa, módulos testáveis e espaço para crescer além do próximo remendo de nó.

---

## Rumo ao MVP

Quando todas as caixas abaixo estiverem marcadas, o Nexus Framework estará **pronto para MVP**: scaffold de apps nativos, edição de blueprints/fluxos, geração e entrega de um projeto desktop/Android documentado.

### Cliente / scaffolder

- [ ] Assistente Compose em 6 passos *(v1 entrega Generate em 2 telas + editores — suficiente para MVP)*
- [x] Gerar desktop + android a partir dos templates
- [x] Editor de blueprint (Compose)
- [x] Editor de fluxos (lista, ativar/desativar, preview JSON)
- [x] ProjectGenerator + validadores

### Templates

- [x] Templates desktop + android de propósito geral
- [ ] Build binário desktop ponta a ponta verificado no CI
- [ ] Build APK Android ponta a ponta verificado no CI
- [x] Estrutura `blueprint.json` + `flows.json` opcional
- [x] Stubs TS/XHTML DSL, caminhos Lua e Python

### Runtime / apps gerados

- [ ] pybind11 desktop totalmente integrado no app gerado (Fase 2 — codegen guiado por blueprint)
- [x] Paridade de packs `python.dat` / `lua.dat`
- [ ] Ponte Chaquopy Android testada E2E em dispositivo
- [ ] Compilador TS/XHTML → Lua *(caminho manual via `panels.lua` documentado hoje)*

### Docs / DX

- [x] Seções de arquitetura e comparação no README
- [x] Guias `AGENTS.md` dos templates
- [ ] CLI `debug validate --all` ou validação equivalente de templates no CI
- [x] Scripts `client-setup` (JDK 26)

### Release

- [ ] Pipeline CI verde em `main`
- [ ] Binário do cliente publicado (`builds/client/`)
- [ ] Tag de versão `v1.0.0`

### Pós-MVP (v1.1+)

Não obrigatório para MVP — acompanhar separadamente:

- Painel imnodes nativo de blueprint (mesmo schema `blueprint.json`)
- Canvas visual do editor de fluxos
- Catálogo remoto de templates · template iOS
- Tipos de passo HTTP/webhook em `flows.json`
- Polimento do runner SDL3 no Android
