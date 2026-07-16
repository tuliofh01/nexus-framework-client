# Nexus Framework — Documentation Hub

Documentation for the **Framework** scaffold client: Compose Desktop UI + Gradle generation pipeline for native C++/Lua/Python apps.

## Navigation

| Doc                                                 | What it covers                                                                               |
|----------------------------------------------------|---------------------------------------------------------------------------------------------|
| [Architecture](architecture/doc-index-guide.md)     | Full-stack layers, language stack, generation pipeline, templates, build orchestration, risk |
| [Guide](guides/doc-index-guide.md)                  | UI, MVC, Python, Lua, themes, XHTML DSL, coding styles, adding dependencies                  |
| [Templates & schemas](templates/doc-index-guide.md) | `blueprint.json` + `flows.json` — nodes, validation, runtime automations                     |
## Architecture diagrams

![Full stack architecture](assets/diagrams/full-stack-architecture.svg)

![Generation and builds flow](assets/diagrams/generation-builds-flow.svg)

![Desktop vs Android runtime](assets/diagrams/desktop-vs-android-runtime.svg)

## Related READMEs

| Path                                                                                                        | Purpose                                               |
|------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| [../README.md](../README.md) · [../misc/translations/README.pt-BR.md](../misc/translations/README.pt-BR.md) | Project overview (EN / pt-BR)                         |
| [../misc/client-setup/README.md](../misc/client-setup/README.md)                                            | First-run JDK 26 + Git setup                          |
| [../builds/LAYOUT.md](../builds/LAYOUT.md)                                                                  | `builds/client/` and `builds/framework/` layout       |
| [../template/README.md](../template/README.md)                                                              | Output templates index                                |
| [../misc/README.md](../misc/README.md)                                                                      | Generation pipeline modules, Docker, Jenkins, scripts |
| [../AGENTS.md](../AGENTS.md)                                                                                | Build commands for coding assistants                  |
## Quick commands

```bash
source ../misc/client-setup/env.sh
./gradlew :app:run
./gradlew :cli:run --args="generate --type desktop --name MyApp --dry-run"
```
