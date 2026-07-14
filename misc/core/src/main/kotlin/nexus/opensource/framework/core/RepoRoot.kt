package nexus.opensource.framework.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Locates the Framework repository root (contains `template/desktop-app/`).
 */
object RepoRoot {
    private const val ENV_VAR = "NXS_REPO_ROOT"
    private const val MARKER_DIR = "template/desktop-app"

    fun resolve(start: Path = defaultStartDir()): Path {
        System.getenv(ENV_VAR)?.takeIf { it.isNotBlank() }?.let { env ->
            val candidate = Paths.get(env).toAbsolutePath().normalize()
            if (isRepoRoot(candidate)) return candidate
            error("$ENV_VAR is set but missing $MARKER_DIR: $candidate")
        }

        var dir: Path? = start
        while (dir != null) {
            if (isRepoRoot(dir)) return dir
            dir = dir.parent
        }

        // Gradle :cli:run may use a subproject working directory — walk from user.dir too.
        val userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize()
        if (userDir != start) {
            dir = userDir
            while (dir != null) {
                if (isRepoRoot(dir)) return dir
                dir = dir.parent
            }
        }

        error(
            "Could not find Framework repo root ($MARKER_DIR). " +
                "Run from the repository or set $ENV_VAR.",
        )
    }

    private fun isRepoRoot(path: Path): Boolean =
        Files.isDirectory(path.resolve(MARKER_DIR)) &&
            Files.exists(path.resolve("settings.gradle.kts"))

    private fun defaultStartDir(): Path =
        Paths.get("").toAbsolutePath().normalize()
}
