package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlateTest {
    @Test
    fun normaliseStripsSpacesAndDashes() {
        assertEquals("KA05AB1234", Plate.normalise(" ka-05 ab 1234 "))
    }

    @Test
    fun plateVariantsMatch() {
        assertTrue(Plate.matches("KA05AB1234", "KA 05 AB 1234"))
        assertTrue(Plate.matches("KA05AB1234", "ka-05-ab-1234"))
    }

    @Test
    fun plateOcrConfusionsMatch() {
        assertTrue(Plate.matches("KA05AB1234", "KAO5AB1234"))
        assertTrue(Plate.matches("KA05AB1234", "KA05AB I234"))
    }

    @Test
    fun plateDifferentCharacterDoesNotMatch() {
        assertFalse(Plate.matches("KA05AB1234", "KA05AB1Z34"))
    }

    @Test
    fun plateDifferentNumber() {
        assertFalse(Plate.matches("KA05AB1234", "KA05XY9876"))
    }

    @Test
    fun findsPlateInNotificationText() {
        assertEquals("KA05AB1234", Plate.find("Ravi is arriving in your Auto KA 05 AB 1234 · 3 min"))
        assertEquals(null, Plate.find("Your driver is on the way"))
    }
}
