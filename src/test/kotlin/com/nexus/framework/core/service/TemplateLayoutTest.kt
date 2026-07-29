package com.nexus.framework.core.service

import com.nexus.framework.core.RepoRoot
import com.nexus.framework.core.model.AppType
import com.nexus.framework.core.model.BlueprintJson
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

/** Guards the 1.1.0 layout: template folders and sample blueprints stay shippable. */
class TemplateLayoutTest {
    private val repoRoot = RepoRoot.resolve()

    @Test
    fun templateFoldersExistForEachAppType() {
        for (appType in AppType.entries) {
            val dir = repoRoot.resolve("template").resolve(appType.templateFolder)
            assertTrue(Files.isDirectory(dir), "missing template dir: $dir")
            assertTrue(
                Files.isRegularFile(dir.resolve("nxs_config.json")),
                "${appType.id}: missing nxs_config.json under $dir",
            )
            assertTrue(
                Files.isRegularFile(dir.resolve("blueprint.json")),
                "${appType.id}: missing blueprint.json under $dir",
            )
        }
    }

    @Test
    fun sampleBlueprintsValidate() {
        val validator = BlueprintValidator()
        for (appType in AppType.entries) {
            val sample = BlueprintJson.sampleApp("LayoutCheck", appType)
            val result = validator.validate(sample)
            assertTrue(result.isValid, "${appType.id}: ${result.errors.joinToString()}")
        }
    }

    @Test
    fun clientsDeployDirIsDocumentedUnderBuilds() {
        val clientsReadme = repoRoot.resolve("builds/clients/README.txt")
        assertTrue(Files.isRegularFile(clientsReadme), "missing builds/clients/README.txt")
        val text = Files.readString(clientsReadme)
        assertTrue(text.contains("NexusFrameworkClient"), text)
        assertTrue(
            Files.isRegularFile(repoRoot.resolve("build_client.sh")),
            "missing root build_client.sh",
        )
    }
}
