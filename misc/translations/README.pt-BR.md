<!--
  description: Nexus Framework 1.0.2 — gerador nativo C++/Lua/Python. Home = dashboard. Langflow → flows.json (não blueprint). Licença Nexus-1.0.
-->
# Nexus Framework — Gerador de Apps Nativos

<p align="center">
  <img src="../../docs/assets/nexus-logo.png" alt="Nexus Framework" width="240" />
</p>

<p align="center"><strong>Desenhe seu app como um grafo. Obtenha um binário nativo. Sem navegador. Sem Electron. Sem nuvem.</strong></p>

<p align="center">
  <a href="../../LICENSE"><img src="https://img.shields.io/badge/license-Nexus--License-blue?style=flat-square" alt="Nexus-1.0" /></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin-2.4-purple?style=flat-square&logo=kotlin" alt="Kotlin 2.4" /></a>
  <a href="https://ziglang.org/"><img src="https://img.shields.io/badge/Zig-0.16.0-orange?style=flat-square&logo=zig" alt="Zig 0.16.0" /></a>
  <a href="#"><img src="https://img.shields.io/badge/version-1.0.2-blueviolet?style=flat-square" alt="Versão 1.0.2" /></a>
</p>

> **Do zero ao binário**
> ```bash
> zig run misc/client-setup/setup.zig && source misc/client-setup/env.sh
> ./misc/build_client.sh && ./gradlew :app:run
> ```

O README completo em inglês é a fonte autoritativa: [../../README.md](../../README.md).

## O que é

Gerador que transforma um blueprint visual em projeto nativo (C++20, Lua, Python, Zig). Módulos Gradle na raiz (`:core`, `:cli`, `:app`); um único `build.gradle.kts`. `misc/` é só tooling — não contém mais o código de core/cli.

## Início rápido

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./misc/build_client.sh          # aceita a Licença Nexus (uma vez); CI: --accept-license
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MeuApp"
# Opcional: export Langflow → stubs flows.json (não blueprint.json)
./gradlew :cli:run --args="import-langflow --file export.json --output builds/framework/MeuApp/flows/flows.json"
cd builds/framework/MeuApp && ./build_app.sh
```

## Cliente Compose (v1.0.2)

**Home** é o dashboard inicial (flamingo, projetos recentes, criar/analisar). Editores Blueprint e Flows (esqueletos), Debugger, Test Runner, Loading / What's New.

## Langflow → flows

Importação opcional de JSON de exportação compatível com Langflow para stubs `flows.json` (`enabled: false`) via `import-langflow`. **Não** gera `blueprint.json`. Formato compatível; Nexus **não é afiliado** ao projeto Langflow.

## Licença (Nexus License)

- Uso **não comercial** do Toolkit e de apps gerados (pessoal / hobby / institucional não comercial): permitido **com atribuição**.
- Até **2041-07-21**, autorização prévia de [Túlio Horta (@tuliofh01)](https://github.com/tuliofh01) é necessária para: uso **comercial do Toolkit**; apps gerados que **geram receita**; apps usados em **instituição comercial**.
- Depois de 2041-07-21 essas restrições de autorização expiram (salvo renovação); a atribuição continua.
- **Sem garantia.** O autor não se responsabiliza por atividades ilícitas de apps derivados.

Texto completo: [../../LICENSE](../../LICENSE).

## Docs

| Doc | Conteúdo |
|:----|:---------|
| [docs/hub.md](../../docs/hub.md) | Hub |
| [docs/architecture/overview.md](../../docs/architecture/overview.md) | Arquitetura |
| [docs/assets/diagrams/activity-generate-pipeline.svg](../../docs/assets/diagrams/activity-generate-pipeline.svg) | Diagrama de geração |
| [docs/guides/coding-with-nexus.md](../../docs/guides/coding-with-nexus.md) | Guia de código |

*Desenhe o grafo, gere a árvore, entregue o binário.*
