package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AreaNoteTest {
    private val here = Fix(12.9200, 77.6200, 0)
    private fun near(i: Int, band: Int) = Report(12.9200 + i * 0.0002, 77.6200, band)
    private val far = Report(12.9500, 77.6200, 7)

    @Test
    fun fewerThanThreeShowsNothing() {
        assertNull(areaNote(listOf(near(0, 7), near(1, 7), far), here, nightOnly = true))
    }

    @Test
    fun nightNoteCountsOnlyNightBandsNearby() {
        val pts = listOf(near(0, 7), near(1, 0), near(2, 7), near(3, 4), far)
        assertEquals("3 reports after 9 pm within 300 m of here in the last 90 days.", areaNote(pts, here, nightOnly = true))
    }

    @Test
    fun allDayNote() {
        val pts = listOf(near(0, 7), near(1, 3), near(2, 4))
        assertEquals("3 reports within 300 m of here in the last 90 days.", areaNote(pts, here, nightOnly = false))
    }

    @Test
    fun neverSaysSafe() {
        assertNull(areaNote(emptyList(), here, nightOnly = false))
    }
}
