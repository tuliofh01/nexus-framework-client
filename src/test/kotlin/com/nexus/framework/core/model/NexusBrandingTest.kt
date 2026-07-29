package com.nexus.framework.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NexusBrandingTest {

    @Test
    fun windowTitleFollowsFrameworkConvention() {
        assertEquals(
            "MyApp - built with The Nexus Framework",
            NexusBranding.windowTitle("MyApp"),
        )
    }

    @Test
    fun frameworkVersionIs110ForThisRelease() {
        assertEquals("1.1.0", NexusBranding.FRAMEWORK_VERSION)
        assertTrue(NexusBranding.versionLabel().startsWith("v1.1.0"))
    }

    @Test
    fun frameworkNameIsStable() {
        assertEquals("The Nexus Framework", NexusBranding.FRAMEWORK_NAME)
    }
}
