# Architecture (1.0.3)

Single Gradle module. Sources live under `src/main/kotlin/com/nexus/framework/`.

## Packages

- `core/` — generation brain (`RepoRoot`, schemas, `ProjectGenerator`). No Compose.
- `cli/` — headless entry (`FrameworkCliKt`) via `./gradlew runCli`.
- `shared/` — `RecentProjectsStore`, `DebuggerService`, `TestRunner`, `NativeFileDialogs`.
- `ui/theme` — `NexusTheme`.
- `ui/chrome` — Flamingo mascot, Whats New, window icon.
- `ui/<feature>/` — co-located controller + screen (`generate`, `blueprint`, `flows`, `home`, `loading`, `debugger`).
- `App.kt` — Compose Desktop entry (`./gradlew run`).

Never nest a second Gradle module under `app/src`. Never pull Android SDK types into this Desktop client.

## Outputs

- Generated apps: `builds/framework/<name>/`
- Client distributable: `builds/client/` via `deployToBuildsClient`
