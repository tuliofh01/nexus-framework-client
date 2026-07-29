# Contributing to Nexus Framework

Thanks for helping improve **The Nexus Framework** — a native app generator (Compose Desktop client + Kotlin CLI → C++20 / Lua / Python templates).

## Prerequisites

- **JDK 26** (Foojay toolchain / Temurin 26)
- **Zig 0.16.0** — `zig run misc/client-setup/setup.zig` then `source misc/client-setup/env.sh`
- Repo-root **Gradle wrapper** (`./gradlew`)

## Build & test

```bash
source misc/client-setup/env.sh
./build_client.sh --accept-license         # license + test + deploy client
./misc/build_client.sh --accept-license   # compile only (+ license)
./gradlew test                            # unit tests alone
./gradlew run                             # Compose Desktop client
./gradlew runCli --args="generate --type desktop --name MyApp --dry-run"
```

Package the client distributable:

```bash
./build_client.sh --accept-license
# → builds/clients/NexusFrameworkClient-1.1.0/
```

## Layout (1.1.0)

Single Gradle module. Sources: `src/main/kotlin/com/nexus/framework/` (`core`, `cli`, `shared`, `ui/*`). Tests: `src/test/…`. Templates: `template/`. Docs: `docs/hub.md`.

Do **not** reintroduce `:app` / `:core` / `:cli` modules or `nexus.opensource` packages.

## Diagrams

If you change architecture or build flow, regenerate SVGs:

```bash
python3 misc/scripts/generate-diagrams.py
./misc/test-gen/linux-generic.sh --dry-run --project _fixture
```

## License

By contributing you agree that your changes are under the **Nexus License (Nexus-1.0)** — see [LICENSE](LICENSE).

## Questions

Open an issue at the repository linked in `NexusBranding.REPO_URL`, or start from [docs/hub.md](docs/hub.md).
