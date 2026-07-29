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
| root | `com.nexus.framework` | `src/main/kotlin/com/nexus/framework/` |
| UI screens | `com.nexus.framework.ui.*` | `.../ui/{generate,blueprint,...}/` |
| UI controllers | co-located in `ui/<feature>/` | feature folders |
| shared | `com.nexus.framework.shared` | `.../shared/` |
| shared helpers | `com.nexus.framework.shared` | `.../shared/` |
| core | `com.nexus.framework.core` | `.../core/` |
| core schemas | `com.nexus.framework.core.model` | `.../core/model/` |
| core engine | `com.nexus.framework.core.service` | `.../core/service/` |
| cli | `com.nexus.framework.cli` | `.../cli/` |

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
settings.gradle.kts → single root project (no :core/:cli/:app)
build.gradle.kts      — kotlin.jvm + serialization + Compose Desktop + runCli
src/main/kotlin/com/nexus/framework/
  App.kt, cli/, core/, shared/, ui/{theme,chrome,generate,blueprint,flows,home,loading,debugger}/


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

If you see `src/main/kotlin/com/nexus/framework/framework/` with root-owned `build/` artifacts, that is **stale nested-module debris**. Exclude it from sources (already done in root `build.gradle.kts`) and remove with:

```bash
sudo rm -rf src/main/kotlin/com/nexus/framework/framework
```

Do **not** move `:core` or `:cli` back under `app/`.

## Read next

- `intellij/wisdom/architecture.md`
- `intellij/wisdom/compose-desktop.md`
- `intellij/wisdom/imports-and-style.md`
- `docs/guides/` (repo docs hub)
