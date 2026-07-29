package {{PACKAGE_NAME}}

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

// {{MARKER}}
@RunWith(AndroidJUnit4::class)
class SmokeInstrumentedTest {
    @Test
    fun appContext_hasPackageName() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("{{PACKAGE_NAME}}", appContext.packageName)
    }
}
