package app.saathi.core

import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Plain rules that decide when to check in on her. No AI, no network.
 * One event per kind until [reset] (she said "I'm okay").
 */
class Watcher(
    private val mode: Mode,
    private val route: List<Fix>,
    private val durationS: Long?,
    private val startedAt: Long,
) {
    private val stopAfterS = if (mode == Mode.BUS || mode == Mode.TRAIN) 600L else 300L
    private val stopRadiusM = 50.0
    private val offRouteM = 300.0
    private val offRouteForS = 60L

    private val fired = mutableSetOf<EventKind>()
    private var anchor: Fix? = null
    private var offSince: Long? = null

    fun add(fix: Fix): TripEvent? {
        longStop(fix)?.let { return it }
        offRoute(fix)?.let { return it }
        return late(fix)
    }

    fun reset() {
        fired.clear()
        anchor = null
        offSince = null
    }

    private fun longStop(fix: Fix): TripEvent? {
        val a = anchor
        if (a == null || Geo.distanceM(a, fix) > stopRadiusM) {
            anchor = fix
            return null
        }
        val stoppedS = fix.t - a.t
        if (stoppedS < stopAfterS || EventKind.LONG_STOP in fired) return null
        fired += EventKind.LONG_STOP
        return TripEvent(EventKind.LONG_STOP, "stopped ${stoppedS / 60} min", fix.t)
    }

    private fun offRoute(fix: Fix): TripEvent? {
        if (route.isEmpty()) return null
        val d = Geo.distanceToRouteM(fix, route)
        if (d <= offRouteM) {
            offSince = null
            return null
        }
        val since = offSince ?: fix.t.also { offSince = it }
        if (fix.t - since < offRouteForS || EventKind.OFF_ROUTE in fired) return null
        fired += EventKind.OFF_ROUTE
        val rounded = (d / 50).roundToInt() * 50
        return TripEvent(EventKind.OFF_ROUTE, "$rounded m off route", fix.t)
    }

    private fun late(fix: Fix): TripEvent? {
        val dur = durationS ?: return null
        val due = startedAt + dur
        val grace = max(600L, dur / 4)
        if (fix.t <= due + grace || EventKind.LATE in fired) return null
        fired += EventKind.LATE
        return TripEvent(EventKind.LATE, "${(fix.t - due) / 60} min past arrival time", fix.t)
    }
}
