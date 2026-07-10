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

## Grafo blueprint (`blueprint.json`)

O Nexus inclui um **grafo de app estilo Langflow** na raiz do projeto. Nós declaram módulos (`python.module`, `cpp.model`, `ui.page`, …); arestas ligam fluxo de dados e comandos dentro do app MVC gerado. A **geração em `:core`** valida e consome o grafo ao materializar `builds/framework/<nome>/`.

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

**Coexistência:** um app Nexus gerado pode chamar um webhook n8n a partir de Python ou Lua (ex.: enviar telemetria, disparar pipeline downstream) enquanto o `blueprint.json` cuida apenas da **fiação interna** do app — a mesma separação que o Langflow usa para cadeias LLM vs. o que o n8n usa para fluxos de integração.

**Mapeamento no cliente:** **Edit blueprint** no `:app` espelha o canvas do Langflow — arrastar nós, conectar portas, pré-visualizar JSON. A v1.1 embute **imnodes** nativamente com o mesmo arquivo; sem migração de schema prevista.

**Exemplos visuais:** Veja [chatbot RAG](docs/assets/examples/langflow-rag-chatbot.svg), [agente com ferramentas](docs/assets/examples/langflow-agent-tools.svg) e [estrutura de app no blueprint Nexus](docs/assets/examples/nexus-blueprint-app-structure.svg) para grafos estilo Langflow e como o Nexus traduz o mesmo modelo mental para `blueprint.json` em design-time.

## O que é este repositório

| Camada | Hoje | Roadmap (v1.1+) |
|--------|------|-----------------|
| **Cliente (`:app`)** | Compose Desktop — demo MVC + **Generate Project** + **Blueprint Editor** (grafo `blueprint.json`) | Assistente em 6 passos, painel imnodes nativo |
| **Geração (`misc/core`, `misc/cli`)** | `ProjectGenerator` + `BlueprintValidator` → `builds/framework/<nome>/` | Catálogo remoto, packs `python.dat` / `lua.dat` |
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
├── misc/
│   ├── build-logic/     Plugins Gradle de convenção (included build, toolchain JVM 26)
│   ├── core/            Pipeline de geração (`:core`) — ProjectGenerator, schema nxs_config
│   ├── cli/             Comando headless `generate` (`:cli`)
│   ├── client-setup/    Instaladores JDK 26 + Git na primeira execução
│   ├── docker/          Geração containerizada opcional
│   ├── jenkins/         Notas opcionais de setup Jenkins
│   └── scripts/         Automação do repositório (ex.: generate-in-docker.sh)
├── builds/              Cliente → builds/client/ · apps → builds/framework/<nome>/
├── template/
│   ├── desktop-app/     Saída Desktop (plotter C++/CMake)
│   ├── android-app/     Saída Android (Gradle/Djinni/Chaquopy)
│   └── shared/          DSL, assets, temas, runtime
├── docs/                Hub de documentação → docs/README.md
└── Jenkinsfile          Entrada de pipeline opcional
```

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

![Pilha completa do Nexus — cliente Compose, pipeline de geração, TS/XHTML + blueprint, Lua/sol2, MVC C++ no SDL3/ImGui/ImPlot, pontes Python, Djinni](docs/assets/diagrams/full-stack-architecture.svg)

![Fluxo de geração e builds — client-setup → :app/:cli → builds/framework/&lt;nome&gt; → app nativo](docs/assets/diagrams/generation-builds-flow.svg)

![Runtime Desktop vs Android — MVC/ImGui/SDL3 compartilhados, pybind11 vs Chaquopy+Djinni](docs/assets/diagrams/desktop-vs-android-runtime.svg)

Referência de camadas: [docs/architecture/overview.md](docs/architecture/overview.md)

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
