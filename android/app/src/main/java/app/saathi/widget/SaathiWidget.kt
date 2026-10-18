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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
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
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.ui.SpeakForMeActivity

private val SLID = booleanPreferencesKey("slid")
private val ground = Color(0xFF141A2E)
private val tile = Color(0xFF252F52)
private val madder = Color(0xFFC7372F)
private val white = ColorProvider(Color.White)
private val mist = ColorProvider(Color(0xFFC8CEE4))
private val madderMist = ColorProvider(Color(0xFFFFDAD5))

/** Main state: one big SOS. Slide across for Walk, Bus, Train. Night indigo; madder only for help. */
class SaathiWidget : GlanceAppWidget() {
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val slid = currentState(SLID) ?: false
            val onTrip = TripState.now.onTrip
            Column(GlanceModifier.fillMaxSize().background(ground).cornerRadius(28.dp).padding(8.dp)) {
                if (!slid) {
                    Box(
                        GlanceModifier.fillMaxWidth().defaultWeight().background(madder).cornerRadius(22.dp)
                            .clickable(actionStartActivity<SpeakForMeActivity>()).padding(16.dp),
                        contentAlignment = Alignment.BottomStart,
                    ) {
                        Column {
                            Text("Help", style = TextStyle(color = white, fontSize = 30.sp, fontWeight = FontWeight.Bold))
                            Text(if (onTrip) "Watching your trip · one tap speaks for you and texts your contacts" else "One tap speaks for you and texts your contacts",
                                style = TextStyle(color = madderMist, fontSize = 12.sp))
                        }
                    }
                    Text("Slide for Walk, Bus, Train", modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp)
                        .clickable(actionRunCallback<Slide>()),
                        style = TextStyle(color = mist, fontSize = 13.sp, fontWeight = FontWeight.Medium))
                } else {
                    Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
                        listOf("WALK" to "Walk", "BUS" to "Bus", "TRAIN" to "Train").forEachIndexed { i, (mode, label) ->
                            if (i > 0) Spacer(GlanceModifier.width(8.dp))
                            Box(
                                GlanceModifier.defaultWeight().fillMaxSize().background(tile).cornerRadius(20.dp)
                                    .clickable(actionStartService(
                                        Intent(context, TripService::class.java).setAction(TripService.ACTION_START_TRIP)
                                            .putExtra(TripService.EXTRA_MODE, mode), isForegroundService = true)),
                                contentAlignment = Alignment.Center,
                            ) { Text(label, style = TextStyle(color = white, fontSize = 15.sp, fontWeight = FontWeight.Bold)) }
                        }
                    }
                    Text("Back to SOS", modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp)
                        .clickable(actionRunCallback<Slide>()),
                        style = TextStyle(color = mist, fontSize = 13.sp))
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
