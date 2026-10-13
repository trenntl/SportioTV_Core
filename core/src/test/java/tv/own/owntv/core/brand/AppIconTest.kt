package tv.own.owntv.core.brand

import org.junit.Assert.assertEquals
import org.junit.Test

class AppIconTest {

    @Test
    fun `every stored name resolves to the single sportio mark`() {
        AppIcon.entries.forEach { assertEquals(AppIcon.SPORTIO, AppIcon.fromStored(it.name)) }
    }

    @Test
    fun `nothing stored or an unknown name falls back to sportio`() {
        assertEquals(AppIcon.SPORTIO, AppIcon.fromStored(null))
        assertEquals(AppIcon.SPORTIO, AppIcon.fromStored("EGGSHELL"))
        assertEquals(AppIcon.SPORTIO, AppIcon.fromStored("VIOLET"))
        assertEquals(AppIcon.SPORTIO, AppIcon.DEFAULT)
    }

    @Test
    fun `sportio is MainActivity itself`() {
        // Upgrading users' home-screen icons point at MainActivity, so the default must keep that name.
        assertEquals("", AppIcon.SPORTIO.activitySuffix)
        assertEquals(AppIcon.entries.size, AppIcon.entries.map { it.activitySuffix }.toSet().size)
    }
}
