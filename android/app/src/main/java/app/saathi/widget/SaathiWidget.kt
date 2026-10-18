package app.saathi.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceModifier as GM
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.ui.SpeakForMeActivity

private val SLID = booleanPreferencesKey("slid")
private val ink = ColorProvider(Color(0xFF111111))
private val white = ColorProvider(Color.White)

/** Main state: one big SOS. Slide across for Walk, Bus, Train. */
class SaathiWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val slid = currentState(SLID) ?: false
            val onTrip = TripState.now.onTrip
            Column(GlanceModifier.fillMaxSize().background(Color(0xFF1A1D21)).cornerRadius(20.dp).padding(8.dp)) {
                if (!slid) {
                    Box(
                        GlanceModifier.fillMaxWidth().defaultWeight().background(Color(0xFFC8352A)).cornerRadius(16.dp)
                            .clickable(actionStartActivity<SpeakForMeActivity>()),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SOS", style = TextStyle(color = white, fontSize = 34.sp, fontWeight = FontWeight.Bold))
                            Text(if (onTrip) "Watching your trip" else "Speak for me and alert", style = TextStyle(color = white, fontSize = 12.sp))
                        }
                    }
                    Spacer(GlanceModifier.height(6.dp))
                    Text("Start a trip  ›  ›", modifier = GlanceModifier.fillMaxWidth().padding(6.dp)
                        .clickable(actionRunCallback<Slide>()),
                        style = TextStyle(color = ColorProvider(Color(0xFFF2B705)), fontSize = 14.sp, fontWeight = FontWeight.Medium))
                } else {
                    Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
                        listOf("WALK" to "Walk", "BUS" to "Bus", "TRAIN" to "Train").forEach { (mode, label) ->
                            Box(
                                GM.defaultWeight().fillMaxSize().background(Color(0xFFF2B705)).cornerRadius(14.dp)
                                    .clickable(actionStartService(
                                        Intent(context, TripService::class.java).setAction(TripService.ACTION_START_TRIP)
                                            .putExtra(TripService.EXTRA_MODE, mode), isForegroundService = true)),
                                contentAlignment = Alignment.Center,
                            ) { Text(label, style = TextStyle(color = ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)) }
                            Spacer(GM.width(6.dp))
                        }
                    }
                    Spacer(GlanceModifier.height(6.dp))
                    Text("‹  ‹  Back to SOS", modifier = GlanceModifier.fillMaxWidth().padding(6.dp)
                        .clickable(actionRunCallback<Slide>()),
                        style = TextStyle(color = ColorProvider(Color(0xFFE6E6E6)), fontSize = 14.sp))
                }
            }
        }
    }
}

class Slide : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, glanceId) { it[SLID] = !(it[SLID] ?: false) }
        SaathiWidget().update(context, glanceId)
    }
}

class SaathiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = SaathiWidget()
}
