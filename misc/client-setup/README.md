# Client setup — first run

Run **one** platform script **before** your first `./gradlew :app:run`. Installs **JDK 26**, **Git**, and basic build tools.

| Platform | Script | Env file |
|----------|--------|----------|
| **Linux** | [`linux/setup.sh`](linux/setup.sh) | `misc/client-setup/env.sh` |
| **macOS** | [`macos/setup.sh`](macos/setup.sh) | `misc/client-setup/env.sh` |
| **Windows** | [`windows/setup.bat`](windows/setup.bat) | `misc/client-setup/env.bat` |

## Quick start

**Linux**

```bash
./misc/client-setup/linux/setup.sh
source misc/client-setup/env.sh
./gradlew :app:run
```

Per-distro helpers: [`linux/setup-arch.sh`](linux/setup-arch.sh) · [`linux/setup-debian.sh`](linux/setup-debian.sh) · [`linux/setup-fedora.sh`](linux/setup-fedora.sh)

**macOS** — requires [Homebrew](https://brew.sh); installs `git` and OpenJDK 26.

**Windows** — prints winget/Chocolatey hints if JDK 26 or Git is missing.

## What gets installed

| Component | Required for Compose client? | Notes |
|-----------|------------------------------|-------|
| **OpenJDK 26** | Yes | Matches `misc/build-logic` `jvmToolchain(26)` |
| **Git** | Yes | Clone, templates, version control |
| **gcc/g++/make** (Linux) | Recommended | Native helpers |
| **CMake / Ninja** | No | Only for **generated** C++ templates |

Source `misc/client-setup/env.sh` (or `env.bat`) in each new shell. Add exports to `~/.bashrc` or `~/.zshrc` to persist.

[Foojay Toolchains](https://github.com/gradle/foojay-toolchains-gradle-plugin) can download JDK 26 if missing, but a system JDK avoids slow first builds.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Dependency requires at least JVM runtime version 26` | Re-run setup or set `JAVA_HOME` to JDK 26 |
| Linux: `openjdk-26-jdk` not found | Use backports or [Eclipse Temurin 26](https://adoptium.net/) |
| macOS: `openjdk@26` missing | `brew install --cask temurin@26` |
| Windows: setup exits with error 1 | Install JDK 26 + Git via printed commands; new terminal |
| Template C++ build fails | Install CMake 3.24+ and Ninja separately |
| Wrong branch | Active branch is **`main`** — `git checkout main` |

Related: [../README.md](../README.md) · [../AGENTS.md](../AGENTS.md)
