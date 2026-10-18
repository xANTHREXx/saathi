package app.saathi.trigger

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import app.saathi.core.RideStatus
import app.saathi.core.readRide
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.ui.PlateCheckActivity

/** Picks up rides booked on Uber, Ola or Rapido from their notifications. No action from her. */
class RideNotificationListener : NotificationListenerService() {
    companion object {
        const val DEMO_CHANNEL = "demo_ride"
        const val PLATE_CHECK_ID = 40
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val n = sbn.notification ?: return
        if (sbn.packageName == packageName && n.channelId != DEMO_CHANNEL) return
        val extras = n.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT) ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString()
        val ride = readRide(sbn.packageName, title, text) ?: return
        Log.i("Saathi", "ride ${ride.app} ${ride.status} ${ride.plate}")
        val onTrip = TripState.now.onTrip
        when (ride.status) {
            RideStatus.ASSIGNED, RideStatus.STARTED -> if (!onTrip) start(ride.app, ride.plate, ride.mode.name)
            RideStatus.ARRIVED -> {
                if (!onTrip) start(ride.app, ride.plate, ride.mode.name)
                askForPlate(ride.plate ?: TripState.now.plate)
            }
            RideStatus.ENDED -> if (onTrip) startForegroundService(TripService.intent(this, TripService.ACTION_STOP_TRIP))
        }
    }

    private fun start(app: String, plate: String?, mode: String) {
        startForegroundService(TripService.intent(this, TripService.ACTION_START_TRIP)
            .putExtra(TripService.EXTRA_MODE, mode).putExtra(TripService.EXTRA_PLATE, plate).putExtra(TripService.EXTRA_APP, app))
    }

    private fun askForPlate(booked: String?) {
        val open = PendingIntent.getActivity(this, 41,
            Intent(this, PlateCheckActivity::class.java).putExtra(PlateCheckActivity.EXTRA_BOOKED, booked)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = Notification.Builder(this, "alert")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("Your ride is here. Check the plate")
            .setContentText("Snap the number plate and Saathi will compare it with ${booked ?: "your booking"}.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .addAction(Notification.Action.Builder(null, "Check the plate", open).build())
            .build()
        getSystemService(NotificationManager::class.java).notify(PLATE_CHECK_ID, n)
    }
}
