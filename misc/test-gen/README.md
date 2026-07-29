# test-gen — generate tests for built apps

Emit smoke-test stubs for projects scaffolded under `builds/framework/<name>/`. Detects template type from `nxs_config.json` (desktop vs android) and writes idempotent test files.

## Prerequisites

- Bash 4+ (Linux/macOS) or PowerShell 5+ (Windows)
- `python3` for JSON parsing (stdlib only)
- Optional: `gtest` / `catch2` for C++ unit tests (documented only — smoke tests run without them)

For Android instrumented tests, source `misc/client-setup/env.sh` so `JAVA_HOME` and Gradle are on `PATH`.

## Usage

```bash
# By project name (under builds/framework/)
./misc/scripts/test-gen/linux/generic.sh --project MyApp

# By path to generated app root
./misc/scripts/test-gen/linux/debian.sh builds/framework/MyApp

# Preview without writing files
./misc/scripts/test-gen/linux/generic.sh --dry-run --project _fixture

# Overwrite previously generated stubs
./misc/scripts/test-gen/linux/generic.sh --force --project MyApp
```

### Platform entry points

| Platform | Script |
|----------|--------|
| Linux (Arch) | [linux/arch.sh](linux/arch.sh) |
| Linux (Debian/Ubuntu) | [linux/debian.sh](linux/debian.sh) |
| Linux (Fedora/RHEL) | [linux/fedora.sh](linux/fedora.sh) |
| Linux (POSIX fallback) | [linux/generic.sh](linux/generic.sh) |
| macOS | [macos/darwin.sh](macos/darwin.sh) |
| Windows | [windows/win32.ps1](windows/win32.ps1) |

Platform scripts are thin wrappers; all logic lives in [common/generate-tests.sh](common/generate-tests.sh).

## What gets generated

### Desktop (`nexus.template` = `desktop-app`)

| File | Purpose |
|------|---------|
| `tests/smoke_test.cpp` | Minimal CTest-compatible smoke stub |
| `tests/run_smoke.sh` | Build preset + run binary if present |
| `tests/nexus_generated/zig_build_snippet.txt` | Paste-into `build.zig` CTest-like smoke block |
| `tests/nexus_generated/README.fragment.md` | Integration notes |
| `tests/nexus_generated/metadata.json` | Manifest of generated artifacts |

### Android (`nexus.template` = `android-app`)

| File | Purpose |
|------|---------|
| `app/src/androidTest/java/com/nexus/<app>/SmokeInstrumentedTest.kt` | Gradle `androidTest` stub |
| `tests/nexus_generated/README.fragment.md` | `./gradlew connectedAndroidTest` notes |
| `tests/nexus_generated/metadata.json` | Manifest of generated artifacts |

## Idempotency

Generated files include a `nexus-test-gen: generated` marker. Re-running skips existing files unless `--force` is passed.

## Optional C++ test frameworks

Smoke tests do not require Google Test or Catch2. To add unit tests later:

| Distro | Install (optional) |
|--------|-------------------|
| Arch | `pacman -S gtest` |
| Debian/Ubuntu | `apt install libgtest-dev` |
| Fedora | `dnf install gtest-devel` |
| macOS | `brew install googletest` |

## Fixture

`builds/framework/_fixture/` is a minimal desktop project used for `--dry-run` validation in CI and local checks.
