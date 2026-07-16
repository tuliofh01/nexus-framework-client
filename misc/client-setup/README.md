# Client setup — first run

Run **one** bootstrap **before** your first `./gradlew :app:run`. Installs **Zig 0.14.0** and pins a known-good JDK 26 for Gradle.

## Quick start

**Cross-platform (recommended):**

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
./gradlew :app:run
```

**Linux / macOS:**

```bash
zig run misc/client-setup/setup.zig
source misc/client-setup/env.sh
```

**Windows:**

```
zig run misc/client-setup/setup.zig
call misc\client-setup\env.bat
```

## What gets installed

| Component | Required | Notes |
|-----------|----------|-------|
| **Zig 0.14.0** | Yes | Builds generated native apps |
| **JDK 26** | Yes | Via Foojay Toolchains Gradle plugin |
| **Git** | Yes | Clone, templates, version control |
| **gcc/g++/make** (Linux) | Recommended | System libraries for Zig |

Source `misc/client-setup/env.sh` (or `env.bat`) in each new shell, or add to `~/.bashrc` / `~/.zshrc`.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Dependency requires at least JVM runtime version 26` | Re-run setup or set `JAVA_HOME` to JDK 26 |
| Linux: `openjdk-26-jdk` not found | Use backports or [Eclipse Temurin 26](https://adoptium.net/) |
| macOS: `openjdk@26` missing | `brew install --cask temurin@26` |
| Windows: setup exits with error 1 | Install JDK 26 + Git via printed commands; new terminal |
| Wrong branch | Active branch is **`main`** — `git checkout main` |

Related: [../README.md](../README.md) · [../AGENTS.md](../AGENTS.md)
