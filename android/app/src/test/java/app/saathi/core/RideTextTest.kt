package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RideTextTest {
    @Test
    fun assignedFromUber() {
        val r = readRide("com.ubercab", "Ravi is on the way", "Auto KA 05 AB 1234 · arriving in 4 min")
        assertEquals(RideStatus.ASSIGNED, r?.status)
        assertEquals("KA05AB1234", r?.plate)
        assertEquals(Mode.AUTO, r?.mode)
        assertEquals("Uber", r?.app)
    }

    @Test
    fun arrivedFromOla() {
        val r = readRide("com.olacabs.customer", "Your cab has arrived", "KA 01 CD 5678 is waiting at pickup")
        assertEquals(RideStatus.ARRIVED, r?.status)
        assertEquals(Mode.CAB, r?.mode)
        assertEquals("Ola", r?.app)
    }

    @Test
    fun startedAndEnded() {
        assertEquals(RideStatus.STARTED, readRide("com.rapido.passenger", "Ride started", "Enjoy your ride")?.status)
        assertEquals(RideStatus.ENDED, readRide("com.ubercab", "Trip completed", "Rate your driver")?.status)
    }

    @Test
    fun otherAppsIgnored() {
        assertEquals(null, readRide("com.whatsapp", "Ravi is on the way", "KA 05 AB 1234"))
    }

    @Test
    fun unrelatedRideAppNotificationIgnored() {
        assertEquals(null, readRide("com.ubercab", "50% off your next ride", "Use code SAVE50"))
    }
}
