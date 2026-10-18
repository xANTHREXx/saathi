package app.saathi.core

enum class RideStatus { ASSIGNED, ARRIVED, STARTED, ENDED }

data class RideNote(val app: String, val status: RideStatus, val plate: String?, val mode: Mode)

val RIDE_APPS = mapOf(
    "com.ubercab" to "Uber",
    "com.olacabs.customer" to "Ola",
    "com.rapido.passenger" to "Rapido",
    "app.saathi" to "Uber", // demo notifications posted by the app itself
)

/**
 * Reads a ride-app notification on the phone, with no network: which app, what stage, the plate.
 * Gemini improves this when online; this keeps it working offline.
 */
fun readRide(pkg: String, title: String?, text: String?): RideNote? {
    val app = RIDE_APPS[pkg] ?: return null
    val all = listOfNotNull(title, text).joinToString(" ").lowercase()
    val status = when {
        listOf("trip completed", "ride completed", "trip ended", "rate your", "you have arrived at your destination").any { it in all } -> RideStatus.ENDED
        listOf("ride started", "trip started", "on trip", "enjoy your ride").any { it in all } -> RideStatus.STARTED
        listOf("has arrived", "is here", "waiting at pickup", "arrived at pickup").any { it in all } -> RideStatus.ARRIVED
        listOf("on the way", "is arriving", "is coming", "driver assigned", "captain assigned", "accepted your").any { it in all } -> RideStatus.ASSIGNED
        else -> return null
    }
    val mode = when {
        "auto" in all -> Mode.AUTO
        "bike" in all || "moto" in all -> Mode.BIKE
        else -> Mode.CAB
    }
    return RideNote(app, status, Plate.find(listOfNotNull(title, text).joinToString(" ")), mode)
}
