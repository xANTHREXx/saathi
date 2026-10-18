package app.saathi.core

import kotlinx.serialization.Serializable

/** One location reading. [t] is epoch seconds. */
@Serializable
data class Fix(val lat: Double, val lng: Double, val t: Long)

@Serializable
enum class Mode { AUTO, CAB, BIKE, WALK, BUS, TRAIN }

@Serializable
enum class EventKind { LONG_STOP, OFF_ROUTE, LATE }

@Serializable
data class TripEvent(val kind: EventKind, val detail: String, val at: Long)
