package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WatcherTest {
    // A straight route north, about 2 km long.
    private val route = listOf(Fix(12.9000, 77.6000, 0), Fix(12.9180, 77.6000, 0))
    private val spot = Fix(12.9050, 77.6000, 0)

    private fun stationary(fromT: Long, count: Int): List<Fix> =
        (0 until count).map { i ->
            // jitter of a few metres, as GPS does
            val j = if (i % 2 == 0) 0.00005 else -0.00005
            Fix(spot.lat + j, spot.lng, fromT + i * 10L)
        }

    private fun feed(w: Watcher, fixes: List<Fix>): List<TripEvent> = fixes.mapNotNull { w.add(it) }

    @Test
    fun longStopAutoFiresAt300s() {
        val w = Watcher(Mode.AUTO, route, durationS = null, startedAt = 0)
        val events = feed(w, stationary(0, 30))
        assertEquals(0, events.size)
        val last = w.add(Fix(spot.lat, spot.lng, 300))
        assertEquals(EventKind.LONG_STOP, last?.kind)
    }

    @Test
    fun longStopBusNeeds600s() {
        val w = Watcher(Mode.BUS, route, durationS = null, startedAt = 0)
        assertEquals(0, feed(w, stationary(0, 31)).size)
        val events = feed(w, stationary(310, 30))
        assertEquals(listOf(EventKind.LONG_STOP), events.map { it.kind })
    }

    @Test
    fun offRouteAfter60s() {
        val eastOffset = 400.0 / (111_320.0 * Math.cos(Math.toRadians(12.9)))
        val far = { t: Long, i: Int -> Fix(12.9010 + i * 0.0005, 77.6000 + eastOffset, t) }

        val w1 = Watcher(Mode.CAB, route, durationS = null, startedAt = 0)
        val shortRun = (0..4).map { far(it * 10L, it) }
        assertEquals(0, feed(w1, shortRun).size)

        val w2 = Watcher(Mode.CAB, route, durationS = null, startedAt = 0)
        val longRun = (0..7).map { far(it * 10L, it) }
        assertEquals(listOf(EventKind.OFF_ROUTE), feed(w2, longRun).map { it.kind })
    }

    @Test
    fun lateFiresOnce() {
        val w = Watcher(Mode.AUTO, route, durationS = 600, startedAt = 0)
        assertNull(w.add(Fix(12.9170, 77.6000, 1199)))
        assertEquals(EventKind.LATE, w.add(Fix(12.9171, 77.6000, 1201))?.kind)
        assertNull(w.add(Fix(12.9172, 77.6000, 1300)))
    }

    @Test
    fun noRepeatUntilReset() {
        val w = Watcher(Mode.AUTO, route, durationS = null, startedAt = 0)
        assertEquals(1, feed(w, stationary(0, 31)).size)
        assertEquals(0, feed(w, stationary(310, 20)).size)
        w.reset()
        assertEquals(0, feed(w, stationary(510, 30)).size)
        assertEquals(EventKind.LONG_STOP, w.add(Fix(spot.lat, spot.lng, 810))?.kind)
    }

    @Test
    fun noRouteStillDetectsLongStop() {
        val w = Watcher(Mode.WALK, emptyList(), durationS = null, startedAt = 0)
        val events = feed(w, stationary(0, 31))
        assertEquals(listOf(EventKind.LONG_STOP), events.map { it.kind })
    }

    @Test
    fun detailIsPlainEnglish() {
        val w = Watcher(Mode.AUTO, route, durationS = null, startedAt = 0)
        val e = feed(w, stationary(0, 31)).single()
        assertEquals("stopped 5 min", e.detail)
    }
}
