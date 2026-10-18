package app.saathi.trip

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import app.saathi.core.Fix
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface LocationSource { fun fixes(): Flow<Fix> }

/** Real GPS, every 10 s. */
class FusedSource(private val ctx: Context) : LocationSource {
    @SuppressLint("MissingPermission")
    override fun fixes(): Flow<Fix> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()
        val cb = object : LocationCallback() {
            override fun onLocationResult(r: LocationResult) {
                r.lastLocation?.let { trySend(Fix(it.latitude, it.longitude, it.time / 1000)) }
            }
        }
        client.requestLocationUpdates(req, cb, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(cb) }
    }
}

/**
 * Replays a recorded track from assets for the demo. Each point keeps its own track time,
 * so the watcher's thresholds hold while the replay runs faster than real time.
 */
class ReplaySource(private val ctx: Context, private val asset: String, private val speed: Int) : LocationSource {
    override fun fixes(): Flow<Fix> = flow {
        val text = ctx.assets.open("tracks/$asset.json").bufferedReader().readText()
        val pts = Json.parseToJsonElement(text).jsonArray.map {
            val o = it.jsonObject
            Fix(o["lat"]!!.jsonPrimitive.content.toDouble(), o["lng"]!!.jsonPrimitive.content.toDouble(),
                o["t"]!!.jsonPrimitive.content.toLong())
        }
        val base = System.currentTimeMillis() / 1000 - pts.first().t
        var prev = pts.first().t
        for (p in pts) {
            val gapMs = (p.t - prev) * 1000 / speed.coerceAtLeast(1)
            if (gapMs > 0) delay(gapMs)
            prev = p.t
            emit(p.copy(t = p.t + base))
        }
    }
}
