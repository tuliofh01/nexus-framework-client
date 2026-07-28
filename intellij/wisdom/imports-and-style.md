# Imports and style

## Package root

Everything first-party: **`com.nexus.framework.*`**

| Kind | Prefix |
|------|--------|
| Project | `com.nexus.framework…` |
| Compose | `androidx.compose…` |
| Kotlinx | `kotlinx…` |
| JDK | `java…` / `javax…` |

## Forbidden in app/core/cli sources

- `org.jetbrains.kotlin.gradle.*`
- Truncated imports (`import foo.bar.`)
- `import nio.file.*` (must be `java.nio.file`)
- Re-nesting modules under another module’s `src/`

## On-save expectations (IntelliJ kit)

1. Optimize imports
2. Remove unused imports (ERROR in Nexus inspection profile)
3. Reformat with official Kotlin style (4 spaces)
4. Ktlint DISTRACT_FREE mode

## Snippets

Live templates group **Nexus**: `nxcomp`, `nxfield`, `nximp` — see `intellij/templates/Nexus.xml`.
