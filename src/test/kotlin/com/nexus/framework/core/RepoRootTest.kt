package com.nexus.framework.core

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RepoRootTest {
    @Test
    fun resolvesFromWorkingDirectory() {
        val root = RepoRoot.resolve()
        assertTrue(root.resolve("template/desktop-app").let {
            java.nio.file.Files.isDirectory(it)
        }, "resolved root must contain template/desktop-app/")
        assertTrue(root.resolve("settings.gradle.kts").let {
            java.nio.file.Files.isRegularFile(it)
        }, "resolved root must contain settings.gradle.kts")
    }

    @Test
    fun resolvesFromUserDir() {
        val root = RepoRoot.resolve()
        assertTrue(root.toAbsolutePath().toString().contains("nexus_framework"),
            "root should be inside the nexus_framework project")
    }

    @Test
    fun respectsNxSRepoRootEnvVar() {
        val original = System.getenv("NXS_REPO_ROOT")
        try {
            val root = RepoRoot.resolve()
            System.setProperty("NXS_REPO_ROOT", root.toString())
            val resolved = RepoRoot.resolve()
            assertEquals(root.toAbsolutePath().normalize(), resolved.toAbsolutePath().normalize())
        } finally {
            if (original != null) {
                System.setProperty("NXS_REPO_ROOT", original)
            } else {
                System.clearProperty("NXS_REPO_ROOT")
            }
        }
    }

    @Test
    fun acceptClientBundleRoot() {
        val root = RepoRoot.resolve()
        assertTrue(
            root.resolve("template/shared").let { java.nio.file.Files.isDirectory(it) },
            "repo root must contain template/shared/ (required for client bundle detection)"
        )
    }
}
