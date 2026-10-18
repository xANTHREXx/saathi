package app.saathi.trip

import android.annotation.SuppressLint
import android.content.Context
import app.saathi.core.Fix
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** One location reading for an alert raised outside a trip. Falls back to the last known fix. */
object LocationOnce {
    @SuppressLint("MissingPermission")
    suspend fun get(ctx: Context, timeoutMs: Long = 4000): Fix? {
        val client = LocationServices.getFusedLocationProviderClient(ctx)
        val fresh = withTimeoutOrNull(timeoutMs) {
            suspendCancellableCoroutine<android.location.Location?> { cont ->
                val cts = CancellationTokenSource()
                client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(null) }
                cont.invokeOnCancellation { cts.cancel() }
            }
        }
        val loc = fresh ?: suspendCancellableCoroutine<android.location.Location?> { cont ->
            client.lastLocation.addOnSuccessListener { cont.resume(it) }.addOnFailureListener { cont.resume(null) }
        }
        return loc?.let { Fix(it.latitude, it.longitude, it.time / 1000) }
    }
}
