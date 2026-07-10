# Client setup — first run

Run **one** platform script **before** your first `./gradlew :app:run`. These installers put **JDK 26**, **Git**, and basic build tools on your machine. The Gradle wrapper in this repo needs no root after that.

| Platform | Script | Generated env file |
|----------|--------|-------------------|
| **Linux** | [`linux/setup.sh`](linux/setup.sh) | `client-setup/env.sh` |
| **macOS** | [`macos/setup.sh`](macos/setup.sh) | `client-setup/env.sh` |
| **Windows** | [`windows/setup.bat`](windows/setup.bat) | `client-setup/env.bat` |

## Quick start

### Linux

```bash
./client-setup/linux/setup.sh
source client-setup/env.sh
./gradlew :app:run
```

Auto-detects Arch, Debian/Ubuntu, or Fedora/RHEL and delegates to optional per-distro scripts:

- [`linux/setup-arch.sh`](linux/setup-arch.sh) — `pacman`
- [`linux/setup-debian.sh`](linux/setup-debian.sh) — `apt`
- [`linux/setup-fedora.sh`](linux/setup-fedora.sh) — `dnf` / `yum`

### macOS

```bash
./client-setup/macos/setup.sh
source client-setup/env.sh
./gradlew :app:run
```

Requires [Homebrew](https://brew.sh). Installs `git` and `openjdk@26` (or Temurin 26 cask as fallback).

### Windows

```cmd
client-setup\windows\setup.bat
call client-setup\env.bat
gradlew.bat :app:run
```

If JDK 26 or Git is missing, the script prints **winget** and **Chocolatey** install hints.

## What gets installed

| Component | Required for Compose client? | Notes |
|-----------|---------------------------|-------|
| **OpenJDK 26** | Yes | Matches `buildSrc` `jvmToolchain(26)` |
| **Git** | Yes | Clone, templates, version control |
| **gcc/g++/make** (Linux) | Recommended | Native helpers; Gradle does not need root |
| **CMake / Ninja** | No | Only when building **generated** C++ templates under `template/desktop-app/` |

## JAVA_HOME

Setup scripts write `client-setup/env.sh` (or `env.bat` on Windows) with detected `JAVA_HOME`. Source or call it in each new shell:

```bash
source client-setup/env.sh   # Linux / macOS
```

```cmd
call client-setup\env.bat    # Windows
```

To persist on Linux/macOS, add the `export` lines from `env.sh` to `~/.bashrc` or `~/.zshrc`.

The [Foojay Toolchains](https://github.com/gradle/foojay-toolchains-gradle-plugin) plugin can still download JDK 26 if missing, but installing system JDK 26 avoids slow first builds and matches documented prerequisites.

## Troubleshooting

### `Dependency requires at least JVM runtime version 26`

You are on JDK 21 or older. Re-run the platform setup script or set `JAVA_HOME` to JDK 26, then `source client-setup/env.sh`.

### Linux: package `openjdk-26-jdk` not found

Your distro may not ship JDK 26 yet. Options:

1. Enable your distro’s backports / testing repo, or
2. Install [Eclipse Temurin 26](https://adoptium.net/) manually and point `JAVA_HOME` at it in `client-setup/env.sh`.

### macOS: `openjdk@26` not in Homebrew

Try `brew install --cask temurin@26`, or install Temurin 26 from Adoptium and set `JAVA_HOME` manually.

### Windows: setup.bat exits with error 1

Install JDK 26 and Git using the printed winget/choco commands, open a **new** terminal, and re-run `setup.bat`.

### Gradle works but template C++ build fails

Install CMake 3.24+ and Ninja separately — they are **not** required for the Kotlin Compose client:

```bash
# Arch
sudo pacman -S cmake ninja

# Debian/Ubuntu
sudo apt-get install cmake ninja-build

# Fedora
sudo dnf install cmake ninja-build

# macOS
brew install cmake ninja
```

### Wrong branch checked out

The active development branch is **`master`** (`origin/master`). Some remotes still expose `origin/main` as default; use `git checkout master` if docs or scripts do not match your tree.

## Related docs

- [README.md](../README.md) — prerequisites and quick start
- [AGENTS.md](../AGENTS.md) — build commands for AI assistants
- [docs/architecture/agent-readiness.md](../docs/architecture/agent-readiness.md) — agent onboarding gaps
- [docs/architecture/risk-analysis.md](../docs/architecture/risk-analysis.md) — architecture risks
