package app.saathi.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings as SysSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.saathi.alert.AlertManager
import app.saathi.alert.Notifier
import app.saathi.core.ANYWHERE
import app.saathi.core.Contact
import app.saathi.core.Reason
import app.saathi.data.Settings
import app.saathi.data.Store
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Ink = Color(0xFF1A1D21)
private val Green = Color(0xFF1F6F4A)
private val Yellow = Color(0xFFF2B705)
private val Red = Color(0xFFC8352A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertManager.init(this)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Yellow)) {
                var settings by remember { mutableStateOf(Store.settings(this)) }
                if (!settings.onboarded) Onboarding(settings) { s ->
                    Store.save(this, s); settings = s; guard(this)
                } else {
                    guard(this)
                    Home(settings, onEdit = { settings = settings.copy(onboarded = false) })
                }
            }
        }
    }
}

private fun guard(ctx: Context) = ctx.startForegroundService(TripService.intent(ctx, TripService.ACTION_GUARD))

private fun startTrip(ctx: Context, mode: String, plate: String? = null, app: String? = null, replay: String? = null) =
    ctx.startForegroundService(TripService.intent(ctx, TripService.ACTION_START_TRIP)
        .putExtra(TripService.EXTRA_MODE, mode).putExtra(TripService.EXTRA_PLATE, plate)
        .putExtra(TripService.EXTRA_APP, app).putExtra(TripService.EXTRA_REPLAY, replay).putExtra(TripService.EXTRA_SPEED, 30))

// ---------------------------------------------------------------- onboarding (flow 1)

@Composable
private fun Onboarding(initial: Settings, onDone: (Settings) -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(initial.name) }
    val contacts = remember { mutableStateListOf<Contact>().apply { addAll(initial.contacts) } }
    var cName by remember { mutableStateOf("") }
    var cPhone by remember { mutableStateOf("") }
    var cCity by remember { mutableStateOf("") }
    var phrase by remember { mutableStateOf(initial.phraseKey) }
    var presses by remember { mutableStateOf(initial.powerPresses) }
    var consent by remember { mutableStateOf(initial.communityConsent) }
    var error by remember { mutableStateOf<String?>(null) }

    val perms = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    Column(Modifier.fillMaxSize().background(Color(0xFFF3F4F1)).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Set up Saathi", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("About a minute. Everything here is asked once, so nothing interrupts you on a trip.", color = Color.DarkGray)

        Section("Your first name")
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Shown in alerts") })

        Section("Emergency contacts")
        Text("Tag each with a city, or \"anywhere\". Alerts go to people in the city you're in, plus everyone tagged anywhere.", color = Color.DarkGray, fontSize = 14.sp)
        contacts.forEachIndexed { i, c ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${c.name} · ${c.phone} · ${c.city}", Modifier.weight(1f))
                OutlinedButton(onClick = { contacts.removeAt(i) }) { Text("Remove") }
            }
        }
        OutlinedTextField(cName, { cName = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") })
        OutlinedTextField(cPhone, { cPhone = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Phone, with country code (+91…)") })
        OutlinedTextField(cCity, { cCity = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("City, or anywhere") })
        error?.let { Text(it, color = Red) }
        OutlinedButton(onClick = {
            val phone = cPhone.replace(" ", "")
            error = when {
                cName.isBlank() -> "Add a name."
                !Regex("""^\+\d{8,15}$""").matches(phone) && !Regex("""^\d{4}$""").matches(phone) ->
                    "Use the full number with country code, like +919800000000."
                else -> null
            }
            if (error == null) {
                contacts += Contact(cName.trim(), phone, cCity.trim().ifBlank { ANYWHERE })
                cName = ""; cPhone = ""; cCity = ""
            }
        }) { Text("Add contact") }

        Section("Speak for me")
        listOf("touched" to "\"This man touched me. He's still here. Please help me.\"",
            "following" to "\"This man is following me. Please help me.\"",
            "help" to "\"I need help. Please stay with me and call for help.\"").forEach { (k, label) ->
            FilterChip(selected = phrase == k, onClick = { phrase = k }, label = { Text(label) })
        }

        Section("Power button")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3 to "3 presses", 4 to "4", 6 to "6", 0 to "Off").forEach { (n, l) ->
                FilterChip(selected = presses == n, onClick = { presses = n }, label = { Text(l) })
            }
        }
        Text("Many phones open the camera on a double press, and Android's own Emergency SOS uses 5 presses. Pick a count that doesn't clash with yours.",
            color = Color.DarkGray, fontSize = 14.sp)

        Section("Community")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add anonymous points to the community map when I report something", Modifier.weight(1f))
            Switch(consent, { consent = it })
        }

        Section("Permissions")
        Text("Location (all the time) to watch trips · SMS to text your contacts from your SIM · Microphone to record when help is raised · Notifications for check-ins.",
            color = Color.DarkGray, fontSize = 14.sp)
        Button(onClick = {
            perms.launch(buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION); add(Manifest.permission.SEND_SMS)
                add(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            }.toTypedArray())
        }, Modifier.fillMaxWidth()) { Text("Allow location, SMS, microphone, notifications") }
        OutlinedButton(onClick = {
            ctx.startActivity(Intent(SysSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
        }, Modifier.fillMaxWidth()) { Text("Location: choose \"Allow all the time\"") }
        if (Build.VERSION.SDK_INT >= 34) OutlinedButton(onClick = {
            ctx.startActivity(Intent(SysSettings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}")))
        }, Modifier.fillMaxWidth()) { Text("Allow check-ins over the lock screen") }
        OutlinedButton(onClick = {
            ctx.startActivity(Intent(SysSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
        }, Modifier.fillMaxWidth()) { Text("Keep Saathi running in the background") }
        OutlinedButton(onClick = {
            ctx.startActivity(Intent(SysSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, Modifier.fillMaxWidth()) { Text("Notice Uber, Ola and Rapido rides for me") }
        SetupCheck()

        Button(
            onClick = {
                if (name.isBlank() || contacts.isEmpty()) error = "Add your name and at least one contact."
                else onDone(Settings(name.trim(), contacts.toList(), initial.homes, phrase, presses, consent, onboarded = true))
            },
            Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Green),
        ) { Text("Done", fontSize = 18.sp) }
    }
}

@Composable
private fun SetupCheck() {
    val ctx = LocalContext.current
    fun has(p: String) = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
    val items = listOf(
        "Location" to has(Manifest.permission.ACCESS_FINE_LOCATION),
        "Location all the time" to has(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
        "SMS" to has(Manifest.permission.SEND_SMS),
        "Microphone" to has(Manifest.permission.RECORD_AUDIO),
        "Check-ins over the lock screen" to (Build.VERSION.SDK_INT < 34 || ctx.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()),
        "Runs in the background" to ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName),
    )
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F1))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Check my setup", fontWeight = FontWeight.Bold)
            items.forEach { (label, ok) -> Text((if (ok) "✓ " else "✗ ") + label, color = if (ok) Green else Red) }
        }
    }
}

@Composable
private fun Section(t: String) = Text(t, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink, modifier = Modifier.padding(top = 8.dp))

// ---------------------------------------------------------------- home (flows 3, 4, 5)

@Composable
private fun Home(settings: Settings, onEdit: () -> Unit) {
    val ctx = LocalContext.current
    val s by TripState.ui.collectAsState()
    var plate by remember { mutableStateOf("") }
    var showDemo by remember { mutableStateOf(false) }
    var cityOverride by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Color(0xFFF3F4F1)).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Saathi", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Ink,
            modifier = Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showDemo = !showDemo }) })

        Card(colors = CardDefaults.cardColors(containerColor = if (s.alertActive) Color(0xFFFDE7E4) else Color(0xFFE4F3EA))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(when {
                    s.alertActive -> "Your contacts were told: ${s.told.joinToString().ifBlank { "sending…" }}"
                    s.onTrip -> "Watching your ${s.app ?: s.mode?.name?.lowercase()}" + (s.plate?.let { " · $it" } ?: "")
                    else -> "Ready. Press the power button ${if (settings.powerPresses > 0) "${settings.powerPresses} times" else "(off)"} for help."
                }, fontWeight = FontWeight.Bold, color = Ink)
                (TripState.city() ?: s.place.city)?.let { Text("You're in $it", color = Color.DarkGray) }
                s.events.forEach { Text("• ${it.detail}", color = Color.DarkGray) }
                if (s.alertActive) Text("Emergency step: simulated", color = Red)
            }
        }

        if (s.alertActive) Button(onClick = { AlertManager.imOkay() }, Modifier.fillMaxWidth().height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)) { Text("I'm okay", fontSize = 22.sp) }

        HoldForHelp()

        Button(onClick = { ctx.startActivity(Intent(ctx, SpeakForMeActivity::class.java)) }, Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Ink)) { Text("Speak for me", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

        if (!s.onTrip) {
            Text("Start a trip", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("WALK" to "Walk", "BUS" to "Bus", "TRAIN" to "Train").forEach { (m, l) ->
                    OutlinedButton(onClick = { startTrip(ctx, m) }) { Text(l) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(plate, { plate = it.uppercase() }, Modifier.weight(1f), singleLine = true, label = { Text("Auto plate") })
                OutlinedButton(onClick = { startTrip(ctx, "AUTO", plate.ifBlank { null }) }) { Text("Hailed auto") }
            }
        } else {
            OutlinedButton(onClick = { ctx.startForegroundService(TripService.intent(ctx, TripService.ACTION_STOP_TRIP)) },
                Modifier.fillMaxWidth()) { Text("End trip") }
        }

        val records = remember(s.onTrip) { Store.records(ctx) }
        if (records.isNotEmpty()) {
            Text("My trips", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            records.takeLast(5).reversed().forEach { r ->
                Card { Column(Modifier.padding(12.dp)) {
                    Text("${r.caseId} · ${r.mode.lowercase()}${r.plate?.let { " · $it" } ?: ""}", fontWeight = FontWeight.Bold)
                    r.events.forEach { Text("• $it") }
                    r.alerts.forEach { Text("• $it", color = Red) }
                    r.evidence.forEach { Text("${it.name}: ${it.sha256.take(12)}…", fontSize = 12.sp, color = Color.DarkGray) }
                } }
            }
        }

        OutlinedButton(onClick = onEdit) { Text("Settings and contacts") }

        if (showDemo) Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4CC))) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Demo panel (sample data)", fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { startTrip(ctx, "AUTO", "KA05AB1234", "Uber", replay = "blr-auto") }) {
                    Text("Replay: Bengaluru Uber auto, 30×")
                }
                OutlinedButton(onClick = { startTrip(ctx, "BUS", replay = "pune-bus") }) { Text("Replay: Pune bus, 30×") }
                Text("Fake ride-app notifications", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { Notifier.demoRide(ctx, "Ravi is on the way", "Auto KA 05 AB 1234 · arriving in 4 min") }) { Text("Booked") }
                    OutlinedButton(onClick = { Notifier.demoRide(ctx, "Your auto has arrived", "KA 05 AB 1234 is waiting at pickup") }) { Text("Arrived") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { Notifier.demoRide(ctx, "Ravi is on the way", "Auto KA 05 XY 9876 · arriving in 3 min") }) { Text("Flagged auto") }
                    OutlinedButton(onClick = { Notifier.demoRide(ctx, "Trip completed", "Rate your driver") }) { Text("Ended") }
                }
                OutlinedTextField(cityOverride, { cityOverride = it }, singleLine = true, label = { Text("Pretend I'm in (city)") })
                OutlinedButton(onClick = {
                    TripState.update { it.copy(cityOverride = cityOverride.ifBlank { null },
                        place = it.place.copy(state = stateFor(cityOverride) ?: it.place.state, countryCode = "IN")) }
                }) { Text("Apply city") }
            }
        }
    }
}

private fun stateFor(city: String) = when (city.trim().lowercase()) {
    "bengaluru", "bangalore" -> "Karnataka"
    "pune", "mumbai" -> "Maharashtra"
    "delhi" -> "Delhi"
    else -> null
}

/** Hold for 2 seconds to raise help, so a brush against the screen doesn't. */
@Composable
private fun HoldForHelp() {
    val scope = rememberCoroutineScope()
    var holding by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(20.dp)).background(if (holding) Color(0xFF8E1F16) else Red)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    holding = true
                    var job: Job? = null
                    job = scope.launch { delay(2000); AlertManager.raise(Reason.HOLD) }
                    tryAwaitRelease()
                    job.cancel(); holding = false
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(if (holding) "Keep holding…" else "Hold for help", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}
