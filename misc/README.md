# misc/

Supporting tooling for the Framework scaffold client (single Gradle module at repo root).

**Layout rule:** at most **two directory levels** under `misc/` (`misc/<category>/<item>`). Files may live in those leaves. Exception: `misc/build-logic/src/main/kotlin/` — required by Gradle precompiled convention plugins.

## Tree

```text
misc/
├── README.md
├── build_client.sh          # license + compile / --deploy (tests + distributable)
├── build-logic/             # includeBuild convention plugins (JDK 26)
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src/main/kotlin/…    # Gradle-required source set
├── client-setup/            # Zig + env bootstrap
├── cli/                     # man page
├── docker/
├── jenkins/
├── scripts/                 # flat: nexus-dev, diagrams, docker generate
├── test-gen/                # flat: smoke-test stub generator
└── translations/            # localized README landing pages
```

| Path | Role |
|------|------|
| [build_client.sh](build_client.sh) | License gate + compile; `--deploy` runs **test** then packages client |
| [../build_client.sh](../build_client.sh) | **Preferred cold-clone:** source env + `--deploy` → `builds/clients/NexusFrameworkClient-<ver>/` |
| [build-logic/](build-logic/) | Gradle included build — `includeBuild("misc/build-logic")` |
| [client-setup/](client-setup/) | `setup.zig`, `env.sh` / `env.bat` |
| [scripts/](scripts/) | `nexus-dev.sh`, `generate-diagrams.py`, `generate-in-docker.sh` |
| [test-gen/](test-gen/) | Smoke stubs for generated apps |
| [docker/](docker/) | Containerized generation |
| [jenkins/](jenkins/) | Optional CI — Script Path `misc/jenkins/Jenkinsfile` |
| [translations/](translations/) | Non-English landing READMEs |
| [cli/](cli/) | `nexus.1` man page |

```kotlin
pluginManagement {
    includeBuild("misc/build-logic")
}
```

## Common commands

```bash
./build_client.sh --accept-license     # test + deploy → builds/clients/NexusFrameworkClient-1.1.0/
./misc/build_client.sh --accept-license
./misc/build_client.sh --test
./gradlew test
./misc/scripts/nexus-dev.sh compile
./misc/test-gen/linux-generic.sh --dry-run --project _fixture
```

License stamp: `misc/.license-accepted` (gitignored).

Docs: [../README.md](../README.md) · [../CONTRIBUTING.md](../CONTRIBUTING.md) · [../docs/hub.md](../docs/hub.md)
