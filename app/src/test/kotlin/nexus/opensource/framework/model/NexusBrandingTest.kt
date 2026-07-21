package nexus.opensource.framework.model

import kotlin.test.Test
import kotlin.test.assertEquals
import nexus.opensource.framework.core.model.NexusBranding

class NexusBrandingTest {

    @Test
    fun `window title follows the framework convention`() {
        assertEquals(
            "MyApp - built with The Nexus Framework",
            NexusBranding.windowTitle("MyApp"),
        )
    }

    @Test
    fun `version label includes framework version`() {
        assertEquals(
            "v${NexusBranding.FRAMEWORK_VERSION} — ${NexusBranding.SUBTITLE}",
            NexusBranding.versionLabel(),
        )
    }
}
