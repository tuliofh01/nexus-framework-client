package nexus.opensource.framework.core.service

import nexus.opensource.framework.core.model.AppType
import nexus.opensource.framework.core.model.ProjectSpec
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
}
