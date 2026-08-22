package com.nexus.framework.core

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Locates the Framework repository root (contains `template/desktop-app/`).
 *
 * Search order:
 *  1. `NXS_REPO_ROOT` environment variable (explicit override).
 *  2. Walk upward from [start] (CWD) looking for marker directories.
 *  3. Walk upward from `user.dir` (may differ from CWD under Gradle).
 *  4. Walk upward from the running JAR/classes location (packaged Compose client).
 *
 * The marker check accepts both the full repository (has `settings.gradle.kts`)
 * and a **packaged client bundle** that ships `template/shared/` alongside the
 * desktop-app template but does not carry the Gradle project files.
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

        // Collect all candidate starting directories.
        val candidates = linkedSetOf(
            start,
            Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize(),
        ).apply {
            codeSourceDir()?.let { add(it) }
        }

        for (candidate in candidates) {
            var dir: Path? = candidate
            while (dir != null) {
                if (isRepoRoot(dir)) return dir
                dir = dir.parent
            }
        }

        error(
            "Could not find Framework repo root ($MARKER_DIR). " +
                "Run from the repository, or set $ENV_VAR.",
        )
    }

    private fun isRepoRoot(path: Path): Boolean {
        if (!Files.isDirectory(path.resolve(MARKER_DIR))) return false
        // Full repo carries settings.gradle.kts; packaged client bundles
        // template/shared/ but not the Gradle project files.
        return Files.exists(path.resolve("settings.gradle.kts")) ||
            Files.isDirectory(path.resolve("template").resolve("shared"))
    }

    /** Directory containing the running classes / JAR (for packaged Compose clients). */
    private fun codeSourceDir(): Path? = runCatching {
        val loc = RepoRoot::class.java.protectionDomain.codeSource?.location?.toURI()
            ?: return null
        Paths.get(loc).toAbsolutePath().normalize().parent
    }.getOrNull()

    private fun defaultStartDir(): Path =
        Paths.get("").toAbsolutePath().normalize()
}
