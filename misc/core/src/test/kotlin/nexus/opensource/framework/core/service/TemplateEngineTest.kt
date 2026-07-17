package nexus.opensource.framework.core.service

import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateEngineTest {
    @Test
    fun copyTreePreservesExecutableScripts() {
        val source = createTempDirectory("nexus-template-source")
        val destination = createTempDirectory("nexus-template-destination")
        val script = source.resolve("build_app.sh")
        Files.writeString(script, "#!/usr/bin/env bash\necho {{projectName}}\n")
        assertTrue(script.toFile().setExecutable(true, false))

        TemplateEngine().copyTree(
            sourceRoot = source,
            destRoot = destination,
            vars = mapOf("projectName" to "Demo"),
        )

        val generated = destination.resolve("build_app.sh")
        assertTrue(Files.isExecutable(generated))
        assertEquals("#!/usr/bin/env bash\necho Demo\n", Files.readString(generated))
    }
}
