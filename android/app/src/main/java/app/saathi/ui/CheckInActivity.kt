package app.saathi.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.alert.AlertManager
import app.saathi.trip.TripState
import app.saathi.ui.theme.Saathi
import app.saathi.ui.theme.SaathiTheme
import app.saathi.ui.theme.Star
import kotlinx.coroutines.delay

/** Full screen over the lock screen: the check-in, and after an alert, who was told. Always night. */
class CheckInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        AlertManager.init(this)
        setContent { SaathiTheme(dark = true) { CheckIn(onOk = { AlertManager.imOkay(); finish() }, onClose = { finish() }) } }
    }
}

private const val WINDOW_S = 10f

@Composable
private fun CheckIn(onOk: () -> Unit, onClose: () -> Unit) {
    val s by TripState.ui.collectAsState()
    val c = Saathi.colors
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(100) } }
    val ends = s.checkInEndsAtMs

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        when {
            ends != null -> {
                val leftMs = (ends - now).coerceAtLeast(0)
                val reason = s.events.lastOrNull()?.detail?.replaceFirstChar { it.uppercase() }
                Column(Modifier.padding(horizontal = 6.dp)) {
                    reason?.let { Pill(Icons.Rounded.PauseCircle, it, c.alum) }
                    Text("Are you okay?", style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = 18.dp))
                    Text("If you don't answer, Saathi will text your contacts with your location.",
                        style = MaterialTheme.typography.bodyLarge, color = c.fg2, modifier = Modifier.padding(top = 12.dp))
                    StalledThread(Modifier.padding(top = 24.dp))
                }
                // The countdown owns the middle of the screen: this is the one moment that asks loudly.
                Spacer(Modifier.weight(1f))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Countdown(leftMs / 1000f / WINDOW_S, ((leftMs + 999) / 1000).toInt())
                    Text("seconds before your contacts get a text", style = MaterialTheme.typography.bodyLarge, color = c.fg2,
                        modifier = Modifier.padding(top = 14.dp))
                }
                Spacer(Modifier.weight(1f))
                OkayPlate(height = 108, onClick = onOk)
                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)) {
                    Fact(Icons.Rounded.VolumeUp, "Alarm on"); Fact(Icons.Rounded.Vibration, "Vibrating"); Fact(Icons.Rounded.Lock, "Over lock screen")
                }
            }
            s.alertActive -> {
                Column(Modifier.padding(horizontal = 6.dp)) {
                    Box(Modifier.size(56.dp).background(c.help, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Text(if (s.told.isEmpty()) "Telling your contacts…" else "Your contacts were told",
                        style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 20.dp))
                    if (s.told.isNotEmpty()) Text(s.told.joinToString(", "), style = MaterialTheme.typography.headlineSmall,
                        color = c.fg2, modifier = Modifier.padding(top = 6.dp))
                    Column(Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Fact(Icons.Rounded.Check, "Live location link sent", big = true)
                        if (s.recording) Fact(Icons.Rounded.Mic, "Recording on this phone", big = true)
                        if (s.smsFailed.isNotEmpty()) Text("Couldn't send to ${s.smsFailed.joinToString()}. Retrying.",
                            style = MaterialTheme.typography.bodyMedium, color = c.onHelpContainer)
                        Text("Emergency step: simulated", style = MaterialTheme.typography.labelMedium, color = c.alum)
                    }
                }
                Spacer(Modifier.weight(1f))
                OkayPlate(height = 96, onClick = onOk)
                FilledTonalButton(onClose, Modifier.fillMaxWidth().padding(top = 10.dp).height(52.dp), shape = RoundedCornerShape(20.dp)) { Text("Close") }
            }
            else -> {
                Spacer(Modifier.weight(1f))
                Text("No check-in right now", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 6.dp))
                Spacer(Modifier.weight(1f))
                FilledTonalButton(onClose, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(20.dp)) { Text("Close") }
            }
        }
    }
}

/** The trip thread, stopped: stamped stars, then the newest ones fading in alum. */
@Composable
private fun StalledThread(modifier: Modifier = Modifier) {
    val c = Saathi.colors
    Column(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(10) { Star(c.starOn, 12.dp) }
            Star(c.alum, 12.dp); Star(c.alum.copy(alpha = 0.55f), 12.dp); Star(c.alum.copy(alpha = 0.25f), 12.dp)
        }
        Text("The thread has stopped moving.", style = MaterialTheme.typography.bodySmall, color = c.fg2, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun Countdown(fraction: Float, seconds: Int) {
    val c = Saathi.colors
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    Box(Modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = 12.dp.toPx()
            drawArc(track, 0f, 360f, false, style = Stroke(w))
            drawArc(c.alum, -90f, 360f * fraction.coerceIn(0f, 1f), false, style = Stroke(w, cap = StrokeCap.Round))
        }
        Text("$seconds", style = MaterialTheme.typography.displayLarge.copy(fontSize = 72.sp, lineHeight = 76.sp))
    }
}

@Composable
private fun Pill(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier.background(color.copy(alpha = 0.16f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

@Composable
private fun Fact(icon: ImageVector, text: String, big: Boolean = false) {
    val c = Saathi.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (big) 10.dp else 4.dp)) {
        Icon(icon, null, tint = if (big) MaterialTheme.colorScheme.primary else c.fg3, modifier = Modifier.size(if (big) 20.dp else 16.dp))
        Text(text, style = if (big) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.labelSmall, color = if (big) MaterialTheme.colorScheme.onBackground else c.fg2)
    }
}
