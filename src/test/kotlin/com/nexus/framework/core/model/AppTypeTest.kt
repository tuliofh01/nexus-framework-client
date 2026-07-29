package com.nexus.framework.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class AppTypeTest {
    @Test
    fun desktopAndAndroidMapToTemplateFolders() {
        assertEquals("desktop-app", AppType.DESKTOP.templateFolder)
        assertEquals("android-app", AppType.ANDROID.templateFolder)
        assertEquals("desktop", AppType.DESKTOP.id)
        assertEquals("android", AppType.ANDROID.id)
    }

    @Test
    fun fromCliArgAcceptsAliases() {
        assertEquals(AppType.DESKTOP, AppType.fromCliArg("desktop"))
        assertEquals(AppType.DESKTOP, AppType.fromCliArg("desktop-app"))
        assertEquals(AppType.DESKTOP, AppType.fromCliArg("simple"))
        assertEquals(AppType.ANDROID, AppType.fromCliArg("android"))
        assertEquals(AppType.ANDROID, AppType.fromCliArg("android-app"))
    }

    @Test
    fun fromIdReturnsNullForUnknown() {
        assertNull(AppType.fromId("ios"))
    }

    @Test
    fun fromCliArgRejectsUnknown() {
        assertFailsWith<IllegalStateException> {
            AppType.fromCliArg("wasm")
        }
    }
}
