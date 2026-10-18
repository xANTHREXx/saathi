package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PowerPressTest {
    private fun run(p: PowerPress, vararg times: Long) = times.map { p.onToggle(it) }

    @Test
    fun powerThreePressesFire() {
        assertEquals(listOf(false, false, true), run(PowerPress(3), 0, 400, 800))
    }

    @Test
    fun twoPressesNever() {
        assertEquals(listOf(false, false), run(PowerPress(3), 0, 400))
    }

    @Test
    fun slowPressesNever() {
        assertEquals(listOf(false, false, false), run(PowerPress(3), 0, 1600, 3200))
    }

    @Test
    fun sixNeededIgnoresThree() {
        val r = run(PowerPress(6), 0, 300, 600, 900, 1200, 1500)
        assertEquals(listOf(false, false, false, false, false, true), r)
    }

    @Test
    fun offNeverFires() {
        assertFalse(run(PowerPress(0), 0, 100, 200, 300).any { it })
    }

    @Test
    fun firesOnceThenNeedsFreshPresses() {
        assertEquals(listOf(false, false, true, false), run(PowerPress(3), 0, 300, 600, 900))
    }

    @Test
    fun screenEventsSpreadOverNearlyThreeSecondsFire() {
        // Screen on/off broadcasts arrive late on real phones; three presses can land 2.8 s apart.
        assertEquals(listOf(false, false, true), run(PowerPress(3), 0, 1400, 2800))
    }
}
