package nexus.opensource.model

import kotlin.test.Test
import kotlin.test.assertEquals

class NexusBrandingTest {

    @Test
    fun `window title follows the framework convention`() {
        assertEquals(
            "MyApp - built with The Nexus Framework",
            NexusBranding.windowTitle("MyApp"),
        )
    }
}
