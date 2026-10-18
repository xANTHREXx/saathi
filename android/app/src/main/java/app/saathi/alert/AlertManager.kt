package app.saathi.alert

import android.content.Context
import android.util.Log
import app.saathi.core.Reason
import app.saathi.core.TripEvent
import app.saathi.core.alertText
import app.saathi.core.okText
import app.saathi.core.recipients
import app.saathi.data.Store
import app.saathi.trip.LocationOnce
import app.saathi.trip.TripState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Check-ins and alerts. Every way of asking for help ends in [raise].
 * Decisions here are rules; the only network call is the optional live page.
 */
object AlertManager {
    const val CHECKIN_S = 10L

    private lateinit var app: Context
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var checkInJob: Job? = null
    private var liveJob: Job? = null
    private lateinit var alarm: Alarm
    private lateinit var recorder: Recorder
    private val alertLog = mutableListOf<String>()

    /** Called by the trip service when she says she's okay, so the watcher can start fresh. */
    var onOkay: (() -> Unit)? = null

    fun init(ctx: Context) {
        if (::app.isInitialized) return
        app = ctx.applicationContext
        alarm = Alarm(app)
        recorder = Recorder(app)
    }

    fun startCheckIn(event: TripEvent) {
        if (checkInJob?.isActive == true || TripState.now.alertActive) return
        val ends = System.currentTimeMillis() + CHECKIN_S * 1000
        TripState.update { it.copy(checkInEndsAtMs = ends) }
        alarm.start()
        Notifier.showCheckIn(app, event.detail)
        checkInJob = scope.launch {
            delay(CHECKIN_S * 1000)
            if (isActive && TripState.now.checkInEndsAtMs != null) {
                endCheckIn()
                raise(Reason.NO_RESPONSE, event)
            }
        }
    }

    fun raise(reason: Reason, event: TripEvent? = null) {
        scope.launch(Dispatchers.IO) {
            if (TripState.now.alertActive) return@launch
            TripState.update { it.copy(alertActive = true) }
            alarm.buzzOnce()
            val recording = recorder.start()
            val s = Store.settings(app)
            if (!TripState.now.onTrip || TripState.now.lastFix == null) {
                // Outside a trip there is no feed: take one reading so the SMS always has a map link.
                LocationOnce.get(app)?.let { f ->
                    TripState.update { it.copy(lastFix = f, lastFixAtMs = f.t * 1000) }
                }
            }
            val ui = TripState.now
            val to = recipients(s.contacts, TripState.city())
            val liveUrl = LiveShare.create(s.name.ifBlank { "She" }, ui.plate)
            val fixAge = if (ui.lastFixAtMs == 0L) 0 else (System.currentTimeMillis() - ui.lastFixAtMs) / 1000
            val text = alertText(s.name.ifBlank { "Your contact" }, reason, ui.plate, event ?: ui.events.lastOrNull(),
                ui.place.label, ui.lastFix?.copy(t = ui.lastFixAtMs / 1000), fixAge, liveUrl)
            val r = SmsSender.send(app, to, text)
            alertLog += "${reason.name} at ${System.currentTimeMillis() / 1000}: told ${r.sent.joinToString()}"
            Log.i("Saathi", "alert ${reason.name}: $text")
            TripState.update { it.copy(told = r.sent, smsFailed = r.failed, recording = recording) }
            Notifier.showAlerted(app, r.sent, r.failed)
            if (liveUrl != null) startLivePush()
        }
    }

    /** She's okay: stop everything, tell contacts only if an alert went out. */
    fun imOkay() {
        val wasAlert = TripState.now.alertActive
        endCheckIn()
        liveJob?.cancel(); LiveShare.stop()
        recorder.stop()
        Notifier.cancel(app, Notifier.ALERT_ID)
        TripState.update { it.copy(alertActive = false, told = emptyList(), smsFailed = emptyList(), recording = false) }
        onOkay?.invoke()
        if (wasAlert) scope.launch(Dispatchers.IO) {
            val s = Store.settings(app)
            SmsSender.send(app, recipients(s.contacts, TripState.city()), okText(s.name.ifBlank { "She" }))
        }
    }

    fun takeAlertLog(): List<String> = alertLog.toList().also { alertLog.clear() }
    fun recordingFile() = recorder.file

    private fun endCheckIn() {
        checkInJob?.cancel()
        alarm.stop()
        Notifier.cancel(app, Notifier.CHECKIN_ID)
        TripState.update { it.copy(checkInEndsAtMs = null) }
    }

    private fun startLivePush() {
        liveJob?.cancel()
        liveJob = scope.launch(Dispatchers.IO) {
            val until = System.currentTimeMillis() + 60 * 60 * 1000
            while (isActive && TripState.now.alertActive && System.currentTimeMillis() < until) {
                if (!TripState.now.onTrip) LocationOnce.get(app, 3000)?.let { f ->
                    TripState.update { it.copy(lastFix = f, lastFixAtMs = System.currentTimeMillis()) }
                }
                TripState.now.lastFix?.let { LiveShare.push(it, TripState.now.events.map { e -> e.detail }) }
                delay(10_000)
            }
        }
    }
}
