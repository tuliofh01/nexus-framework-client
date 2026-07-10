# O Framework da Nexus Company para Desenvolvimento de Aplicações Nativas

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Logo do The Nexus Framework" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

**The Nexus Framework** gera aplicações nativas em C++/Lua/Python para **Desktop** (Windows, macOS, Linux) e **Android** — janela SDL3, scripts sol2, autoria de UI TypeScript + XHTML e Python embarcado (pybind11 no desktop, Chaquopy + Djinni no Android). ImGui + ImPlot renderizam UI immediate-mode sem motor de browser.

## O que é este repositório

| Hoje | Roadmap (v1) |
|------|----------------|
| Cliente Compose Desktop (`:app`) — demo MVC de contador + tela **Generate Project** | Tela inicial e assistente de criação em 6 passos |
| Pipeline de geração (`:core`, `:cli` em `misc/`) — emite templates em `builds/framework/<nome>/` | Editor blueprint imnodes ligado à geração |
| Templates incluídos — plotter desktop, plotter Android, DSL/temas compartilhados | Catálogo remoto, packs `python.dat` / `lua.dat`, template iOS |

Este é o monorepo **Framework** (`:app`, `:core`, `:cli`). Não é o repositório separado [Nexus Framework Client](https://github.com/tuliofh01/nexus-framework-client) (wizard `:client-desktop` lá).

## Primeira execução

Execute um script de setup da plataforma, carregue o arquivo de ambiente e use o Gradle:

| Plataforma | Setup | Ambiente |
|------------|-------|----------|
| Linux | `./client-setup/linux/setup.sh` | `source client-setup/env.sh` |
| macOS | `./client-setup/macos/setup.sh` | `source client-setup/env.sh` |
| Windows | `client-setup\windows\setup.bat` | `call client-setup\env.bat` |

Exige **JDK 26** e Git — veja [client-setup/README.md](client-setup/README.md).

## Início rápido

```bash
source client-setup/env.sh          # após setup na primeira execução
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
├── buildSrc/            Plugins Gradle de convenção (toolchain JVM 26) — **deve ficar na raiz**
├── misc/
│   ├── core/            Pipeline de geração (`:core`) — ProjectGenerator, schema nxs_config
│   ├── cli/             Comando headless `generate` (`:cli`)
│   ├── docker/          Geração containerizada opcional
│   ├── jenkins/         Notas opcionais de setup Jenkins
│   └── scripts/         Automação do repositório (ex.: generate-in-docker.sh)
├── client-setup/        Instaladores JDK 26 + Git na primeira execução
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

**Pontos fortes do Nexus:** renderização SDL3 + ImGui immediate-mode (sem WebView), sol2 para painéis em runtime, fluxo blueprint/imnodes para ligar MVC sem recompilar CMake, pybind11/Chaquopy para manter analytics em Python, Djinni no Android sem JNI manual, e packs `python.dat` / `lua.dat` no roadmap para distribuir lógica sem scripts soltos em disco.

Exemplo principal: **plotter estilo Desmos** — Python amostra curvas, C++ possui o modelo, ImGui desenha. [docs/templates/desktop-app.md](docs/templates/desktop-app.md) · [docs/templates/android-app.md](docs/templates/android-app.md)

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
| 5 | Fluxo blueprint | Editar `blueprint.json` | Religar módulos | Reabrir no wizard (v1) | Proteger com `.dat` (roadmap) | Mesmo MVC compartilhado |

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
| [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md) | UI, MVC, Python, Lua, temas |
| [docs/guides/generation-pipeline.md](docs/guides/generation-pipeline.md) | ProjectGenerator, CLI, Docker |
| [docs/architecture/agent-readiness.md](docs/architecture/agent-readiness.md) | Onboarding de agentes IA |
| [docs/architecture/risk-analysis.md](docs/architecture/risk-analysis.md) | Riscos de arquitetura |
| [AGENTS.md](AGENTS.md) | Comandos de build para assistentes de código |

## Status de desenvolvimento e limitações

**Entregue:** `:app` (Counter + Generate Project), `:core` / `:cli` (emissão de templates), `template/*`, `builds/`, `client-setup/`, `docs/`.

**Ainda não:** UI completa do wizard, integração do editor imnodes, catálogo remoto, template iOS, polimento do runner SDL3 Android, packs `python.dat` / `lua.dat`.

**Limitações (v1):** apenas scaffolder Compose Desktop; estética ImGui utilitária; Chaquopy aumenta o APK no Android; sem iOS nesta toolchain hoje.

**Branch:** desenvolvimento ativo em **`master`** (`origin/master`). Se seu clone usa `main` como padrão, execute `git checkout master`.
