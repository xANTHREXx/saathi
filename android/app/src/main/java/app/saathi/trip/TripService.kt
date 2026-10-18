package app.saathi.trip

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.Geocoder
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import app.saathi.alert.AlertManager
import app.saathi.alert.Notifier
import app.saathi.core.Fingerprint
import app.saathi.core.Fix
import app.saathi.core.Mode
import app.saathi.core.PowerPress
import app.saathi.core.Reason
import app.saathi.core.Watcher
import app.saathi.data.Api
import app.saathi.data.Evidence
import app.saathi.data.Store
import app.saathi.data.TripRecord
import app.saathi.widget.SaathiWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

/**
 * Runs while Saathi is guarding: owns the persistent notification, the power-button
 * trigger and, during a trip, the location feed and the watcher. Screen off is fine.
 */
class TripService : Service() {
    companion object {
        const val ACTION_GUARD = "app.saathi.GUARD"
        const val ACTION_START_TRIP = "app.saathi.START_TRIP"
        const val ACTION_STOP_TRIP = "app.saathi.STOP_TRIP"
        const val ACTION_HELP = "app.saathi.HELP"
        const val ACTION_OK = "app.saathi.OK"
        const val EXTRA_MODE = "mode"
        const val EXTRA_PLATE = "plate"
        const val EXTRA_APP = "app"
        const val EXTRA_DRIVER = "driver"
        const val EXTRA_REPLAY = "replay"
        const val EXTRA_SPEED = "speed"

        fun intent(ctx: Context, action: String) = Intent(ctx, TripService::class.java).setAction(action)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tripJob: Job? = null
    private var watcher: Watcher? = null
    private val fixes = mutableListOf<Fix>()
    private var tripStartedAt = 0L
    private val power = PowerPress()
    private var lastGeocodeMs = 0L

    private val screen = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            Log.i("Saathi", "screen ${i.action} at ${SystemClock.elapsedRealtime()}")
            if (power.onToggle(SystemClock.elapsedRealtime())) {
                Log.i("Saathi", "power-button trigger")
                AlertManager.raise(Reason.POWER_BUTTON)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Notifier.channels(this)
        AlertManager.init(this)
        AlertManager.onOkay = { watcher?.reset() }
        registerReceiver(screen, IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON); addAction(Intent.ACTION_SCREEN_OFF)
        })
        // Keep the notification and widget in step with the state.
        scope.launch {
            TripState.ui.map { Triple(it.onTrip, it.alertActive, it.told) }.distinctUntilChanged().collect {
                getSystemService(NotificationManager::class.java).notify(Notifier.GUARD_ID, Notifier.guard(this@TripService, TripState.now))
                runCatching { SaathiWidget().updateAll(this@TripService) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        goForeground()
        power.needed = Store.settings(this).powerPresses
        TripState.update { it.copy(guarding = true) }
        when (intent?.action) {
            ACTION_START_TRIP -> startTrip(intent)
            ACTION_STOP_TRIP -> stopTrip()
            ACTION_HELP -> AlertManager.raise(Reason.NOTIFICATION)
            ACTION_OK -> AlertManager.imOkay()
        }
        return START_STICKY
    }

    private fun goForeground() {
        val n = Notifier.guard(this, TripState.now)
        try {
            startForeground(Notifier.GUARD_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } catch (e: Exception) {
            Log.w("Saathi", "location foreground not allowed yet: ${e.message}")
            startForeground(Notifier.GUARD_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }
    }

    private fun startTrip(i: Intent) {
        tripJob?.cancel()
        val mode = runCatching { Mode.valueOf(i.getStringExtra(EXTRA_MODE) ?: "WALK") }.getOrDefault(Mode.WALK)
        tripStartedAt = System.currentTimeMillis() / 1000
        fixes.clear()
        watcher = Watcher(mode, emptyList(), durationS = null, startedAt = tripStartedAt)
        TripState.update {
            it.copy(onTrip = true, mode = mode, plate = i.getStringExtra(EXTRA_PLATE), app = i.getStringExtra(EXTRA_APP),
                driver = i.getStringExtra(EXTRA_DRIVER), events = emptyList(), plateCheck = null,
                startedAtMs = tripStartedAt * 1000, trail = emptyList())
        }
        i.getStringExtra(EXTRA_PLATE)?.let { plate -> scope.launch(Dispatchers.IO) { warnIfFlagged(plate) } }
        val replay = i.getStringExtra(EXTRA_REPLAY)
        val source = if (replay != null) ReplaySource(this, replay, i.getIntExtra(EXTRA_SPEED, 30)) else FusedSource(this)
        tripJob = scope.launch {
            source.fixes().collect { fix -> onFix(fix) }
        }
    }

    /** ST-6: a count only, shown only to her, never a driver's name. */
    private fun warnIfFlagged(plate: String) {
        val f = Api.vehicle(plate) ?: return
        if (!f.flagged) return
        val text = "This vehicle has ${f.reports} reports from other women in the last 90 days. (Sample data)"
        val n = android.app.Notification.Builder(this, "alert")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Before you get in: $plate")
            .setContentText(text)
            .setStyle(android.app.Notification.BigTextStyle().bigText("$text You can continue, or cancel and rebook in your ride app."))
            .build()
        getSystemService(NotificationManager::class.java).notify(50, n)
        Log.i("Saathi", "flagged $plate: ${f.reports}")
    }

    private fun onFix(fix: Fix) {
        fixes += fix
        TripState.update { it.copy(lastFix = fix, lastFixAtMs = System.currentTimeMillis(), trail = if (it.onTrip) (it.trail + fix).takeLast(600) else it.trail) }
        maybeGeocode(fix)
        val event = watcher?.add(fix) ?: return
        Log.i("Saathi", "watcher: ${event.kind} ${event.detail}")
        TripState.update { it.copy(events = it.events + event) }
        AlertManager.startCheckIn(event)
    }

    private fun maybeGeocode(fix: Fix) {
        val now = System.currentTimeMillis()
        if (now - lastGeocodeMs < 60_000 || !Geocoder.isPresent()) return
        lastGeocodeMs = now
        scope.launch(Dispatchers.IO) {
            runCatching {
                @Suppress("DEPRECATION")
                val a = Geocoder(this@TripService, Locale.ENGLISH).getFromLocation(fix.lat, fix.lng, 1)?.firstOrNull()
                    ?: return@launch
                val label = listOfNotNull(a.featureName?.takeIf { f -> f.any { it.isLetter() } && !f.contains("+") }, a.thoroughfare ?: a.subLocality)
                    .distinct().joinToString(", ").ifBlank { null }
                TripState.update { it.copy(place = Place(a.locality ?: a.subAdminArea, a.adminArea, a.countryCode, label)) }
            }
        }
    }

    private fun stopTrip() {
        tripJob?.cancel(); tripJob = null
        val ui = TripState.now
        if (ui.onTrip) saveRecord(ui)
        watcher = null
        TripState.update { it.copy(onTrip = false, mode = null, plate = null, app = null, driver = null) }
    }

    private fun saveRecord(ui: UiState) {
        val now = System.currentTimeMillis() / 1000
        val log = fixes.joinToString("\n") { "${it.t},${it.lat},${it.lng}" }.toByteArray()
        val evidence = mutableListOf(Evidence("location log", Fingerprint.sha256(log), now))
        AlertManager.recordingFile()?.takeIf { it.exists() }?.let {
            evidence += Evidence("audio ${it.name}", Fingerprint.sha256(it.readBytes()), now)
        }
        Store.saveRecord(this, TripRecord(
            caseId = "SA-" + UUID.randomUUID().toString().take(8).uppercase(),
            createdAt = tripStartedAt, mode = ui.mode?.name ?: "WALK", app = ui.app, plate = ui.plate,
            events = ui.events.map { it.detail }, alerts = AlertManager.takeAlertLog(),
            plateCheck = ui.plateCheck, evidence = evidence,
            lat = fixes.lastOrNull()?.lat, lng = fixes.lastOrNull()?.lng, endedAt = now,
        ))
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screen) }
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
