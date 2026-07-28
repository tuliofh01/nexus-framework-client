# Nexus Framework — IntelliJ / AI agent context

Use this file as the primary brain for Junie, AI Assistant, and any agent working inside IntelliJ on this repo.

## What this project is

Nexus Framework is a **multi-module Kotlin JVM** toolchain that:

1. **Generates** native desktop/Android app scaffolds from blueprints + flows (`:core`)
2. Exposes a **CLI** for headless generate / Langflow import (`:cli`)
3. Ships a **Compose Desktop** GUI client (`:app`)

It is **not** an Android Studio application project at the root.

## Package layout (canonical)

All first-party Kotlin code lives under **`com.nexus.framework.*`**:

| Module | Package root | Path |
|--------|--------------|------|
| `:app` | `com.nexus.framework` | `app/src/main/kotlin/com/nexus/framework/` |
| `:app` UI | `com.nexus.framework.view` | `.../view/` |
| `:app` logic | `com.nexus.framework.controller` | `.../controller/` |
| `:app` state | `com.nexus.framework.model` | `.../model/` |
| `:app` helpers | `com.nexus.framework.util` | `.../util/` |
| `:core` | `com.nexus.framework.core` | `core/src/main/kotlin/com/nexus/framework/core/` |
| `:core` schemas | `com.nexus.framework.core.model` | `.../core/model/` |
| `:core` engine | `com.nexus.framework.core.service` | `.../core/service/` |
| `:cli` | `com.nexus.framework.cli` | `cli/src/main/kotlin/com/nexus/framework/cli/` |

### Import rules (keep these easy and clear)

- **Project code:** `import com.nexus.framework...`
- **Compose / Jetpack (Multiplatform Desktop):** `import androidx.compose...`
- **Kotlinx:** `import kotlinx.serialization...`, `import kotlinx.coroutines...`
- **JDK:** `import java.nio.file.Path`, `import java.nio.file.Files` (never `nio.file.*`, never Gradle plugin types)

**Never** import:

- `org.jetbrains.kotlin.gradle.*` into app/core/cli sources
- `android.*` Android SDK types into `:app` (Desktop client)
- Broken truncated imports (`import foo.bar.`)

Entry points:

- Desktop UI: `com.nexus.framework.AppKt`
- CLI: `com.nexus.framework.cli.FrameworkCliKt`

## Gradle modules

```
settings.gradle.kts → include(":core", ":cli", ":app")
core/build.gradle.kts   — kotlin.jvm + serialization
cli/build.gradle.kts    — kotlin.jvm + application (depends on :core)
app/build.gradle.kts    — kotlin.jvm + compose desktop (depends on :core)
```

JDK toolchain: **26**. Wrapper: Gradle **9.6.x**. Catalog: `gradle/libs.versions.toml`.

## Architecture tips for better suggestions

- Prefer **MVC** boundaries already in `:app` — do not put Compose UI into `:core`.
- Generation / validation / Langflow mapping belongs in `:core` services.
- Templates for generated apps: `template/desktop-app`, `template/android-app`, `template/shared`.
- Output of generation: `builds/framework/<name>/` (gitignored contents).
- Client distributable: `builds/client/` via `:app:deployToBuildsClient`.

## Coding preferences

- Explicit imports over wildcards unless a file already uses deliberate `@file:Suppress` for view barrels.
- Optimize imports + remove unused parameters/variables on every edit.
- Match `.editorconfig` (4 spaces Kotlin).
- Keep `CUSTOMIZE` comments as extension points — do not delete them casually.
- When renaming packages, update `mainClass` in `app/build.gradle.kts` and `cli/build.gradle.kts`.

## Known junk (do not “fix” by nesting modules again)

If you see `app/src/main/kotlin/com/nexus/framework/framework/` with root-owned `build/` artifacts, that is **stale nested-module debris**. Exclude it from sources (already done in `app/build.gradle.kts`) and remove with:

```bash
sudo rm -rf app/src/main/kotlin/com/nexus/framework/framework
```

Do **not** move `:core` or `:cli` back under `app/`.

## Read next

- `intellij/wisdom/architecture.md`
- `intellij/wisdom/compose-desktop.md`
- `intellij/wisdom/imports-and-style.md`
- `docs/guides/` (repo docs hub)
