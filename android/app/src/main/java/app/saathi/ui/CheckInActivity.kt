package app.saathi.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.alert.AlertManager
import app.saathi.trip.TripState
import kotlinx.coroutines.delay

/** Full screen over the lock screen: the check-in, and after an alert, who was told. */
class CheckInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        AlertManager.init(this)
        setContent { CheckIn(onOk = { AlertManager.imOkay(); finish() }, onClose = { finish() }) }
    }
}

@Composable
private fun CheckIn(onOk: () -> Unit, onClose: () -> Unit) {
    val s by TripState.ui.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { now = System.currentTimeMillis(); delay(200) } }
    val ends = s.checkInEndsAtMs
    val bg = if (s.alertActive) Color(0xFF7A1A12) else Color(0xFF1A1D21)
    Column(
        Modifier.fillMaxSize().background(bg).padding(28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (ends != null) {
            val left = ((ends - now) / 1000).coerceAtLeast(0)
            Text("Are you okay?", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text(s.events.lastOrNull()?.detail?.replaceFirstChar { it.uppercase() } ?: "", color = Color(0xFFE6E6E6), fontSize = 20.sp)
            Text("Your contacts will be told in $left s", color = Color(0xFFF2B705), fontSize = 22.sp)
        } else if (s.alertActive) {
            Text("Your contacts were told", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text(if (s.told.isEmpty()) "Sending…" else s.told.joinToString(), color = Color.White, fontSize = 24.sp)
            if (s.smsFailed.isNotEmpty()) Text("Couldn't send to ${s.smsFailed.joinToString()}", color = Color(0xFFFFC2B8), fontSize = 18.sp)
            if (s.recording) Text("Recording on this phone", color = Color(0xFFE6E6E6), fontSize = 16.sp)
            Text("Emergency step: simulated", color = Color(0xFFF2B705), fontSize = 16.sp)
        } else {
            Text("No check-in right now", color = Color.White, fontSize = 24.sp)
        }
        Button(
            onClick = onOk,
            modifier = Modifier.fillMaxWidth().height(88.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E8A5C)),
        ) { Text("I'm okay", fontSize = 28.sp, fontWeight = FontWeight.Bold) }
        if (ends == null) Button(onClick = onClose, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F45))) {
            Text("Close")
        }
    }
}
