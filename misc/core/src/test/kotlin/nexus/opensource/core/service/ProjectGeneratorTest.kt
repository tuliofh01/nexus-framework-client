package nexus.opensource.core.service

import nexus.opensource.core.model.AppType
import nexus.opensource.core.model.ProjectSpec
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
    fun defaultOutputPathIncludesProjectName() {
        assertEquals("builds/framework/DemoApp", ProjectGenerator.defaultOutputPath("DemoApp"))
    }
}
