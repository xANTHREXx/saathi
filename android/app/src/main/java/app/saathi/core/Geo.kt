package app.saathi.core

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object Geo {
    private const val EARTH_M = 6_371_000.0

    fun distanceM(a: Fix, b: Fix): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val h = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(a.lat)) * cos(Math.toRadians(b.lat)) * sin(dLng / 2).pow(2)
        return 2 * EARTH_M * asin(sqrt(h))
    }

    /** Shortest distance from [p] to the polyline [route], in metres. Infinite for an empty route. */
    fun distanceToRouteM(p: Fix, route: List<Fix>): Double {
        if (route.isEmpty()) return Double.POSITIVE_INFINITY
        if (route.size == 1) return distanceM(p, route[0])
        var best = Double.POSITIVE_INFINITY
        for (i in 0 until route.size - 1) best = min(best, toSegmentM(p, route[i], route[i + 1]))
        return best
    }

    // Flat projection around p; accurate enough over a city.
    private fun toSegmentM(p: Fix, a: Fix, b: Fix): Double {
        val kx = 111_320.0 * cos(Math.toRadians(p.lat))
        val ky = 110_540.0
        val ax = (a.lng - p.lng) * kx; val ay = (a.lat - p.lat) * ky
        val bx = (b.lng - p.lng) * kx; val by = (b.lat - p.lat) * ky
        val dx = bx - ax; val dy = by - ay
        val len2 = dx * dx + dy * dy
        val t = if (len2 == 0.0) 0.0 else (-(ax * dx + ay * dy) / len2).coerceIn(0.0, 1.0)
        val cx = ax + t * dx; val cy = ay + t * dy
        return sqrt(cx * cx + cy * cy)
    }
}
