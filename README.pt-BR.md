# O Framework da Nexus Company para Desenvolvimento de Aplicações Nativas

<p align="center">
  <img src="docs/assets/nexus-logo.png" alt="Logo do The Nexus Framework" width="220" />
</p>

<p align="center">
  <a href="README.md"><img src="https://img.shields.io/badge/lang-English-blue?style=for-the-badge" alt="English" /></a>
  <a href="README.pt-BR.md"><img src="https://img.shields.io/badge/lang-Portugu%C3%AAs%20(BR)-green?style=for-the-badge" alt="Português (BR)" /></a>
</p>

Este programa é o Cliente do Nexus Framework. Ele gera aplicações nativas modernas em C++ para **Desktop** (Windows, macOS, Linux) e **Android**, usando **SDL3** para janela multiplataforma, **sol2** para scripts Lua, **TypeScript + XHTML** para autoria de UI familiar a desenvolvedores web e **Python** para análises embarcadas. Projetos Android incluem ponte **Djinni** C++↔Kotlin e **Chaquopy** no lado JVM.

## Para que o Nexus foi feito

O Nexus se encaixa em **ferramentas nativas, com uso intenso de dados ou implantadas em campo**, onde você precisa de desempenho e pegada pequena sem enviar Chromium ou WebView.

| Caso de uso | Por que Nexus | Template |
|-------------|---------------|----------|
| **Mesa de trading / dados de mercado** | UI em sub-ms; parsers C++; Python in-process | Desktop |
| **Visualizador CAD / malhas / nuvens de pontos** | Viewport GPU SDL3 + chrome ImGui; geometria em C++ | Desktop |
| **Visualização científica** | numpy via pybind11; arrays na memória nativa; ImPlot | Desktop |
| **Ferramentas de game dev** (editores, profilers) | UI immediate-mode como overlays de debug; hot-reload Lua | Desktop |
| **Bancada de áudio / DSP** | Caminho de sinal C++ de baixa latência | Desktop |
| **Monitor de DevOps / infra** | Dashboard leve; binário nativo único | Desktop |
| **Tablet Android de campo** (inspeção, quiosque) | ImGui SDL3/GLES; ponte Djinni; Python Chaquopy | Android |
| **Painel de robótica / teleoperação** | ImGui amigável ao toque; bindings Lua `android.*` | Android |
| **HMI embarcado / painel industrial** | Mesma pilha SDL3 no desktop e Android | Ambos |

Templates: [docs/templates/desktop-app.md](docs/templates/desktop-app.md) · [docs/templates/android-app.md](docs/templates/android-app.md)

O exemplo principal é um **plotter estilo Desmos** — Python (numpy) amostra curvas, C++ possui o modelo, ImGui + ImPlot desenham a página.

## Curva de aprendizado

O Nexus exige mais habilidade inicial que um shell web, mas os projetos gerados rodam no primeiro dia.

| Habilidade | Obrigatória? | Papel |
|------------|--------------|-------|
| **C++ / CMake** | Sim | Lógica de domínio, MVC, build |
| **SDL3 / ImGui** | Conceitual | UI immediate-mode — redesenho a cada frame, sem DOM |
| **Lua / sol2** | Opcional → recomendado | Painéis em runtime, atalhos |
| **TypeScript + XHTML** | Opcional | Autoria de UI familiar a web devs |
| **Python** | Opcional | Desktop: pybind11 · Android: Chaquopy |
| **Android / Djinni** | Só Android | Ponte sem JNI, empacotamento APK |

**Progressão recomendada:** executar o template → ajustar MVC → adicionar funções Python → scriptar painéis Lua → estender o DSL TS/XHTML → reconfigurar `blueprint.json` no editor imnodes.

**Ressalva honesta:** Electron e Tauri são mais suaves para equipes exclusivamente web. O Nexus compensa o esforço quando throughput nativo, tamanho de binário ou implantação Android em campo importam mais que flexibilidade de layout HTML. Veja [docs/guides/coding-with-nexus.md](docs/guides/coding-with-nexus.md).

## Arquitetura

### Pilha completa

Um diagrama cobre o cliente scaffold, camadas de autoria, MVC C++ no SDL3, pontes Python e alvos Desktop vs Android:

![Arquitetura completa do Nexus — autoria TS/XHTML + blueprint.json, scripting Lua/sol2, MVC C++ no SDL3/ImGui/ImPlot, Python pybind11 ou Chaquopy+Djinni, cliente wizard Compose](docs/assets/diagrams/full-stack-architecture.svg)

| Camada | Tecnologia |
|--------|------------|
| **Autoria** | Componentes TypeScript + XHTML, **imnodes** / `blueprint.json` (estilo Langflow) |
| **Scripting** | DSL Lua ImGui via **sol2** |
| **Domínio** | **MVC** C++20 — `model/`, `controller/`, `view/` |
| **Renderização** | Dear ImGui + ImPlot sobre **SDL3** (OpenGL desktop, GLES Android) |
| **Python** | **pybind11** (desktop) · **Chaquopy** + **Djinni** (Android) |
| **Cliente scaffold** | **Compose Desktop** Kotlin MVC (`app/`) |

### Assistente de criação (roadmap v1)

Fluxo planejado — ainda não implementado no cliente:

![Fluxo do assistente — cliente Compose, wizard de 6 passos, editor blueprint imnodes, emissão do template, build e execução](docs/assets/diagrams/app-creation-wizard-flow.svg)

Mais detalhes: [docs/architecture/overview.md](docs/architecture/overview.md) · [docs/README.md](docs/README.md)

## Quando escolher outra coisa

| Você precisa de… | Melhor opção |
|------------------|--------------|
| HTML/CSS rico ou grande base React/Vue existente | Electron ou Tauri |
| **iOS** com a mesma toolchain hoje | Tauri 2 Mobile ou Swift/Kotlin nativo |
| Widgets nativos do sistema (menus HIG, seletores de arquivo) | Qt, .NET MAUI, UI nativa |
| UI guiada por designers sem código | Pipelines Figma → web; ImGui é code-first |

**Limitações do Nexus (v1):** apenas scaffolder Compose Desktop; sem template iOS; estética ImGui utilitária; Chaquopy aumenta o APK no Android.

## O que ele faz

| Tipo de app | Pilha | Guia |
|-------------|-------|------|
| **Desktop App** | C++ + Lua (sol2) + TS/XHTML + Python + ImGui sobre **SDL3** | [docs/templates/desktop-app.md](docs/templates/desktop-app.md) |
| **Android App** | C++ + Lua + ImGui sobre **SDL3** + **Djinni** + **Chaquopy** | [docs/templates/android-app.md](docs/templates/android-app.md) |

Cada projeto inclui `nxs_config.json`, `blueprint.json`, **temas** compartilhados (`nexus-dark`, `nexus-light`, `nexus-field`), configuração opcional de **Nerd Font** e o logo Nexus em `assets/`.

## Estrutura do repositório

```
Framework/
├── app/                 Cliente Compose Desktop (`:app`) — MVC em `nexus.opensource/`
├── buildSrc/            Plugins Gradle de convenção (toolchain JVM 26)
├── template/
│   ├── desktop-app/     Template de saída Desktop (plotter C++/CMake)
│   ├── android-app/     Template de saída Android (Gradle/Djinni/Chaquopy)
│   └── shared/          DSL, assets, temas, runtime (NexusTheme, FontConfig)
└── docs/                Hub de documentação → docs/README.md
```

Cliente Gradle de módulo único — não usa o layout multi-módulo `:core` / `:cli` / `:client-desktop` de outros repositórios.

## Pré-requisitos

- **JDK 26** (exigido pelo `buildSrc`; o resolver Foojay pode baixá-lo automaticamente)
- Git
- Apps **Desktop** gerados: CMake 3.24+, Ninja, C++20, Python 3.10+
- Apps **Android** gerados: Android SDK, NDK, JDK 17+

## Início rápido

Executar o cliente Compose Desktop (demo de contador MVC hoje):

```bash
./gradlew :app:run
```

Compilar e testar:

```bash
./gradlew :app:compileKotlin :app:test
```

Build do **template desktop** diretamente (templates ainda não são emitidos pelo cliente):

```bash
cd template/desktop-app
cmake --preset debug && cmake --build --preset debug
```

Veja [template/README.md](template/README.md) para notas de build Android.

## Status de desenvolvimento

**Neste repositório hoje**

- `:app` — demo MVC de contador Compose Desktop (`model/`, `view/`, `controller/`)
- `template/desktop-app`, `template/android-app`, `template/shared` — amostras plotter, temas, DSL, stubs de runtime
- `docs/` — hub, páginas de template, guias, dois SVGs de arquitetura

**Roadmap v1 (cliente)**

- Tela inicial e assistente de criação em 6 passos (ver diagrama acima)
- Cópia / emissão de templates a partir do cliente
- Editor blueprint imnodes ligado à geração

**Adiado**

- Catálogo remoto de templates, ofuscação `python.dat`, polimento do runner SDL3 Android, template iOS
