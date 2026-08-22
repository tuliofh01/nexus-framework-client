package com.nexus.framework.core.service

import com.nexus.framework.core.model.AppType
import com.nexus.framework.core.model.ProjectSpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectGeneratorTest {
    @Test
    fun templateVarsIncludeProjectNameAndWindowTitle() {
        val spec = ProjectSpec(
            projectName = "MyApp",
            outputPath = "builds/framework",
            appType = AppType.DESKTOP,
            windowTitle = "MyApp - built with The Nexus Framework",
        )
        val vars = ProjectGenerator(java.nio.file.Paths.get(".")).templateVars(spec)
        assertEquals("MyApp", vars["projectName"])
        assertEquals("MyApp - built with The Nexus Framework", vars["windowTitle"])
        assertEquals("desktop", vars["appType"])
    }

    @Test
    fun templateVarsIncludeScriptProtectionFields() {
        val spec = ProjectSpec(
            projectName = "MyApp",
            outputPath = "builds/framework",
            appType = AppType.DESKTOP,
            scriptProtectionEnabled = true,
        )
        val vars = ProjectGenerator(java.nio.file.Paths.get(".")).templateVars(spec)
        assertEquals("true", vars["scriptProtectionEnabled"])
        assertTrue(vars["scriptProtectionSalt"]!!.isNotEmpty())
        assertTrue(vars["createdAt"]!!.isNotEmpty())
    }

    @Test
    fun defaultOutputPathIncludesProjectName() {
        assertEquals("builds/framework/DemoApp", ProjectGenerator.defaultOutputPath("DemoApp"))
    }

    @Test
    fun templateVarsSanitizePackageNameForAndroid() {
        val spec = ProjectSpec(
            projectName = "My Cool-App",
            outputPath = "builds/framework",
            appType = AppType.ANDROID,
            windowTitle = "title",
        )
        val vars = ProjectGenerator(java.nio.file.Paths.get(".")).templateVars(spec)
        assertEquals("mycoolapp", vars["packageName"])
        assertEquals("android", vars["appType"])
        assertEquals("my cool-app", vars["project_name"])
    }

    @Test
    fun templateVarsFallbackPackageNameWhenNameHasNoLetters() {
        val spec = ProjectSpec(
            projectName = "---",
            outputPath = "builds/framework",
            appType = AppType.DESKTOP,
        )
        val vars = ProjectGenerator(java.nio.file.Paths.get(".")).templateVars(spec)
        assertEquals("app", vars["packageName"])
    }

    @Test
    fun defaultScriptProtectionIsDisabled() {
        val spec = ProjectSpec(
            projectName = "MyApp",
            outputPath = "builds/framework",
            appType = AppType.DESKTOP,
        )
        assertEquals(false, spec.scriptProtectionEnabled)
        val vars = ProjectGenerator(java.nio.file.Paths.get(".")).templateVars(spec)
        assertEquals("false", vars["scriptProtectionEnabled"])
        assertEquals("", vars["scriptProtectionSalt"])
    }
}
