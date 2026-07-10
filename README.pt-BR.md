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
| Pipeline de geração (`:core`, `:cli`) — copia/emite templates em `builds/framework/<nome>/` | Editor blueprint imnodes ligado à geração |
| Templates incluídos — plotter desktop, plotter Android, DSL/temas compartilhados | Catálogo remoto, pack `python.dat`, template iOS |

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

## Casos de uso

O Nexus se encaixa em **ferramentas nativas, com uso intenso de dados ou implantadas em campo**, onde throughput e tamanho de binário importam mais que layout HTML.

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

## Curva de aprendizado

| Habilidade | Obrigatória? | Papel |
|------------|--------------|-------|
| C++ / CMake | Sim | Lógica de domínio, MVC, build |
| SDL3 / ImGui | Conceitual | UI immediate-mode — sem DOM |
| Lua / sol2 | Opcional → recomendado | Painéis em runtime, atalhos |
| TypeScript + XHTML | Opcional | Autoria de UI familiar a web devs |
| Python | Opcional | pybind11 (desktop) · Chaquopy (Android) |
| Android / Djinni | Só Android | Ponte sem JNI, APK |

Progressão: executar template → ajustar MVC → adicionar Python → scriptar Lua → estender TS/XHTML → editar `blueprint.json`. [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md)

## Arquitetura

![Pilha completa do Nexus — TS/XHTML + blueprint.json, Lua/sol2, MVC C++ no SDL3/ImGui/ImPlot, pontes Python, cliente Compose](docs/assets/diagrams/full-stack-architecture.svg)

![Fluxo do assistente — cliente Compose de 6 passos planejado (roadmap v1)](docs/assets/diagrams/app-creation-wizard-flow.svg)

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

**Ainda não:** UI completa do wizard, integração do editor imnodes, catálogo remoto, template iOS, polimento do runner SDL3 Android.

**Limitações (v1):** apenas scaffolder Compose Desktop; estética ImGui utilitária; Chaquopy aumenta o APK no Android; sem iOS nesta toolchain hoje.

**Branch:** desenvolvimento ativo em **`master`** (`origin/master`). Se seu clone usa `main` como padrão, execute `git checkout master`.
