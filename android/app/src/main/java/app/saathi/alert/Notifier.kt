package app.saathi.alert

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import app.saathi.trip.TripService
import app.saathi.trip.UiState
import app.saathi.ui.CheckInActivity
import app.saathi.ui.MainActivity
import app.saathi.ui.SpeakForMeActivity

object Notifier {
    const val GUARD_ID = 1
    const val CHECKIN_ID = 2
    const val ALERT_ID = 3
    private const val GUARD = "guard"
    private const val CHECKIN = "checkin"
    private const val ALERT = "alert"

    fun channels(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(GUARD, "Saathi is ready", NotificationManager.IMPORTANCE_LOW).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        })
        nm.createNotificationChannel(NotificationChannel(CHECKIN, "Check-ins", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null); lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        })
        nm.createNotificationChannel(NotificationChannel(ALERT, "Alerts sent", NotificationManager.IMPORTANCE_HIGH).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        })
    }

    /** The persistent notification: usable from the lock screen. */
    fun guard(ctx: Context, s: UiState): Notification {
        val title = when {
            s.alertActive -> "Alert sent to ${s.told.joinToString()}"
            s.onTrip -> "Watching your ${s.app ?: s.mode?.name?.lowercase() ?: "trip"}" + (s.plate?.let { ", $it" } ?: "")
            else -> "Saathi is ready"
        }
        val b = Notification.Builder(ctx, GUARD)
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentTitle(title)
            .setContentText(if (s.onTrip) "Help and Speak for me work from the lock screen" else "Press the power button to get help")
            .setOngoing(true)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(activity(ctx, MainActivity::class.java, 10))
            .addAction(action(ctx, "Help", service(ctx, TripService.ACTION_HELP, 11)))
            .addAction(action(ctx, "Speak for me", activity(ctx, SpeakForMeActivity::class.java, 12)))
        if (s.onTrip) b.addAction(action(ctx, "Stop watching", service(ctx, TripService.ACTION_STOP_TRIP, 13)))
        return b.build()
    }

    fun showCheckIn(ctx: Context, detail: String) {
        val full = activity(ctx, CheckInActivity::class.java, 20)
        val n = Notification.Builder(ctx, CHECKIN)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Are you okay?")
            .setContentText("Your trip: $detail. Your contacts will be told in 10 seconds.")
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setFullScreenIntent(full, true)
            .setContentIntent(full)
            .addAction(action(ctx, "I'm okay", service(ctx, TripService.ACTION_OK, 21)))
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(CHECKIN_ID, n)
    }

    fun showAlerted(ctx: Context, told: List<String>, failed: List<String>) {
        val text = buildString {
            append(if (told.isEmpty()) "No SMS went out." else "Told: ${told.joinToString()}.")
            if (failed.isNotEmpty()) append(" Failed: ${failed.joinToString()}.")
            append(" Emergency step: simulated.")
        }
        val n = Notification.Builder(ctx, ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Your contacts were told")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setContentIntent(activity(ctx, CheckInActivity::class.java, 30))
            .addAction(action(ctx, "I'm okay", service(ctx, TripService.ACTION_OK, 31)))
            .build()
        ctx.getSystemService(NotificationManager::class.java).notify(ALERT_ID, n)
    }

    fun cancel(ctx: Context, id: Int) = ctx.getSystemService(NotificationManager::class.java).cancel(id)

    private fun action(ctx: Context, title: String, pi: PendingIntent) =
        Notification.Action.Builder(null, title, pi).build()

    private fun activity(ctx: Context, cls: Class<*>, code: Int): PendingIntent =
        PendingIntent.getActivity(ctx, code, Intent(ctx, cls).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    private fun service(ctx: Context, act: String, code: Int): PendingIntent =
        PendingIntent.getForegroundService(ctx, code, Intent(ctx, TripService::class.java).setAction(act),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
}
