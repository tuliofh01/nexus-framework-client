# Architecture wisdom (IntelliJ / Junie)

## Modules

- `:core` — pure generation brain (`com.nexus.framework.core`). No Compose.
- `:cli` — headless entry (`com.nexus.framework.cli.FrameworkCliKt`). Depends on `:core`.
- `:app` — Compose Desktop UI (`com.nexus.framework.AppKt`). Depends on `:core` only.

Never nest `:core` / `:cli` under `app/src`. That produced the broken `.../framework/core/src/main/kotlin/...` paths.

## MVC in `:app`

| Layer | Package | Responsibility |
|-------|---------|----------------|
| View | `com.nexus.framework.view` | Compose screens only |
| Controller | `com.nexus.framework.controller` | Mutable UI state + orchestration |
| Model | `com.nexus.framework.model` | Client-side stores (recents, debugger, tests) |

Shared schemas/services stay in `:core`.

## Templates vs client

- Editable templates: `template/desktop-app`, `template/android-app`, `template/shared`
- Generated output: `builds/framework/<name>/`
- Client binary deploy: `builds/client/` via `:app:deployToBuildsClient`
