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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.ElectricRickshaw
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.saathi.alert.AlertManager
import app.saathi.alert.Notifier
import app.saathi.core.ANYWHERE
import app.saathi.core.Contact
import app.saathi.core.Reason
import app.saathi.data.Settings
import app.saathi.data.Store
import app.saathi.data.TripRecord
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.trip.UiState
import app.saathi.ui.theme.PlateText
import app.saathi.ui.theme.Saathi
import app.saathi.ui.theme.SaathiMark
import app.saathi.ui.theme.SaathiTheme
import app.saathi.ui.theme.Star
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AlertManager.init(this)
        setContent {
            SaathiTheme {
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

private val clock = SimpleDateFormat("h:mm a", Locale.ENGLISH)
private fun time(ms: Long) = clock.format(Date(ms)).lowercase()

// ---------------------------------------------------------------- onboarding (flow 1)

@Composable
private fun Onboarding(initial: Settings, onDone: (Settings) -> Unit) {
    val ctx = LocalContext.current
    val c = Saathi.colors
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

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())
            .statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SaathiMark(28.dp); Text("Saathi", style = MaterialTheme.typography.titleLarge)
        }
        Text("Set up Saathi", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(top = 8.dp))
        Text("About a minute. Everything here is asked once, so nothing interrupts you on a trip.",
            style = MaterialTheme.typography.bodyLarge, color = c.fg2)

        Section("Your first name")
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Shown in alerts") })

        Section("Emergency contacts")
        Hint("Tag each with a city, or \"anywhere\". Alerts go to people in the city you're in, plus everyone tagged anywhere.")
        contacts.forEachIndexed { i, ct ->
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(ct.name, style = MaterialTheme.typography.titleSmall)
                    Text("${ct.phone} · ${ct.city}", style = MaterialTheme.typography.bodySmall, color = c.fg2)
                }
                OutlinedButton(onClick = { contacts.removeAt(i) }) { Text("Remove") }
            }
        }
        OutlinedTextField(cName, { cName = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Name") })
        OutlinedTextField(cPhone, { cPhone = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Phone, with country code (+91…)") })
        OutlinedTextField(cCity, { cCity = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("City, or anywhere") })
        error?.let { Text(it, color = c.help, style = MaterialTheme.typography.bodyMedium) }
        FilledTonalButton(onClick = {
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
            FilterChip(selected = phrase == k, onClick = { phrase = k }, label = { Text(label, Modifier.padding(vertical = 8.dp)) })
        }

        Section("Power button")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3 to "3 presses", 4 to "4", 6 to "6", 0 to "Off").forEach { (n, l) ->
                FilterChip(selected = presses == n, onClick = { presses = n }, label = { Text(l) })
            }
        }
        Hint("Many phones open the camera on a double press, and Android's own Emergency SOS uses 5 presses. Pick a count that doesn't clash with yours.")

        Section("Community")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Add anonymous points to the community map when I report something", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Switch(consent, { consent = it })
        }

        Section("Permissions")
        Hint("Location (all the time) to watch trips · SMS to text your contacts from your SIM · Microphone to record when help is raised · Notifications for check-ins.")
        Button(onClick = {
            perms.launch(buildList {
                add(Manifest.permission.ACCESS_FINE_LOCATION); add(Manifest.permission.SEND_SMS)
                add(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            }.toTypedArray())
        }, Modifier.fillMaxWidth().height(52.dp)) { Text("Allow location, SMS, microphone, notifications") }
        SetupButton("Location: choose \"Allow all the time\"") {
            ctx.startActivity(Intent(SysSettings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${ctx.packageName}")))
        }
        if (Build.VERSION.SDK_INT >= 34) SetupButton("Allow check-ins over the lock screen") {
            ctx.startActivity(Intent(SysSettings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${ctx.packageName}")))
        }
        SetupButton("Keep Saathi running in the background") {
            ctx.startActivity(Intent(SysSettings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${ctx.packageName}")))
        }
        SetupButton("Notice Uber, Ola and Rapido rides for me") {
            ctx.startActivity(Intent(SysSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        SetupCheck()

        Button(
            onClick = {
                if (name.isBlank() || contacts.isEmpty()) error = "Add your name and at least one contact."
                else onDone(Settings(name.trim(), contacts.toList(), initial.homes, phrase, presses, consent, onboarded = true))
            },
            Modifier.fillMaxWidth().padding(top = 8.dp).height(56.dp),
        ) { Text("Done", style = MaterialTheme.typography.titleMedium) }
    }
}

@Composable
private fun SetupButton(label: String, onClick: () -> Unit) =
    OutlinedButton(onClick = onClick, Modifier.fillMaxWidth().height(48.dp)) { Text(label) }

@Composable
private fun SetupCheck() {
    val ctx = LocalContext.current
    val c = Saathi.colors
    fun has(p: String) = ContextCompat.checkSelfPermission(ctx, p) == PackageManager.PERMISSION_GRANTED
    val items = listOf(
        "Location" to has(Manifest.permission.ACCESS_FINE_LOCATION),
        "Location all the time" to has(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
        "SMS" to has(Manifest.permission.SEND_SMS),
        "Microphone" to has(Manifest.permission.RECORD_AUDIO),
        "Check-ins over the lock screen" to (Build.VERSION.SDK_INT < 34 || ctx.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()),
        "Runs in the background" to ctx.getSystemService(PowerManager::class.java).isIgnoringBatteryOptimizations(ctx.packageName),
    )
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Check my setup", style = MaterialTheme.typography.titleSmall)
        items.forEach { (label, ok) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(8.dp).background(if (ok) c.ok else c.help, CircleShape))
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text(if (ok) "Done" else "Missing", style = MaterialTheme.typography.labelMedium, color = if (ok) c.ok else c.help)
            }
        }
    }
}

@Composable
private fun Section(t: String) = Text(t, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp))

@Composable
private fun Hint(t: String) = Text(t, style = MaterialTheme.typography.bodyMedium, color = Saathi.colors.fg2)

// ---------------------------------------------------------------- home (flows 3, 4, 5)

private enum class Tab { TRIP, TRIPS }

@Composable
private fun Home(settings: Settings, onEdit: () -> Unit) {
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(Tab.TRIP) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                NavigationBarItem(tab == Tab.TRIP, { tab = Tab.TRIP }, { Icon(Icons.Rounded.Route, null) }, label = { Text("Trip") })
                NavigationBarItem(tab == Tab.TRIPS, { tab = Tab.TRIPS }, { Icon(Icons.Rounded.History, null) }, label = { Text("My trips") })
                NavigationBarItem(false, { ctx.startActivity(Intent(ctx, CommunityMapActivity::class.java)) },
                    { Icon(Icons.Rounded.Map, null) }, label = { Text("Map") })
                NavigationBarItem(false, onEdit, { Icon(Icons.Rounded.Tune, null) }, label = { Text("Settings") })
            }
        },
    ) { pad ->
        Box(Modifier.padding(bottom = pad.calculateBottomPadding())) {
            when (tab) {
                Tab.TRIP -> TripTab(settings)
                Tab.TRIPS -> TripsTab()
            }
        }
    }
}

@Composable
private fun TripTab(settings: Settings) {
    val s by TripState.ui.collectAsState()
    val ctx = LocalContext.current
    var showDemo by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
    Box(Modifier.fillMaxWidth().weight(1f)) {
        TripMap(s, Modifier.fillMaxWidth().height(340.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MapChip(Modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { showDemo = !showDemo }) }) {
                    SaathiMark(20.dp); Text("Saathi", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.weight(1f))
                // On a trip the plate matters more than the city, and two chips beside it crowd at large text sizes.
                if (s.plate == null) (TripState.city() ?: s.place.city)?.let { city ->
                    MapChip { Icon(Icons.Rounded.NearMe, null, Modifier.size(16.dp)); Text(city, style = MaterialTheme.typography.labelMedium) }
                }
                s.plate?.let { p -> MapChip { PlateText(p) } }
            }
            Spacer(Modifier.height(236.dp))
            Surface(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.background,
            ) {
                Column(Modifier.padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.padding(top = 10.dp).align(Alignment.CenterHorizontally).size(36.dp, 4.dp)
                        .background(Saathi.colors.fg3.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                    if (s.alertActive) ToldBanner(s)
                    if (s.onTrip) OnTrip(s) else Ready()
                    AfterTrip()
                    if (showDemo) DemoPanel()
                }
            }
        }
        // Keep the clock readable when the sheet scrolls up under it.
        Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f)))
    }
    // Help never scrolls away: always one hold from her thumb.
    Box(Modifier.fillMaxWidth().height(1.dp).background(Saathi.colors.hair))
    Box(Modifier.background(MaterialTheme.colorScheme.background).padding(vertical = 12.dp)) { HelpRow(settings, quiet = s.alertActive) }
    }
}

@Composable
private fun MapChip(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, Saathi.colors.hair, RoundedCornerShape(18.dp)).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) { content() }
}

/** After an alert: who was told, and the one full-contrast control, I'm okay. */
@Composable
private fun ToldBanner(s: UiState) {
    val c = Saathi.colors
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(c.helpContainer).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(c.help, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(if (s.told.isEmpty()) "Telling your contacts…" else "${names(s.told)} ${if (s.told.size == 1) "was" else "were"} told",
                    style = MaterialTheme.typography.titleSmall, color = c.onHelpContainer)
                Text(buildString {
                    append("Live location link sent")
                    if (s.recording) append(" · recording on this phone")
                    append(" · emergency step simulated")
                }, style = MaterialTheme.typography.bodySmall, color = c.onHelpContainer.copy(alpha = 0.8f))
                if (s.smsFailed.isNotEmpty()) Text("Couldn't send to ${names(s.smsFailed)}. Retrying.",
                    style = MaterialTheme.typography.bodySmall, color = c.onHelpContainer)
            }
        }
        OkayPlate { AlertManager.imOkay() }
    }
}

/** The reversed plate: full contrast, the one thing to press. */
@Composable
fun OkayPlate(height: Int = 88, onClick: () -> Unit) {
    Button(
        onClick, Modifier.fillMaxWidth().height(height.dp), shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface),
    ) {
        Icon(Icons.Rounded.Check, null, Modifier.size(28.dp)); Spacer(Modifier.width(10.dp))
        Text("I'm okay", style = MaterialTheme.typography.headlineMedium)
    }
}

private fun names(list: List<String>) = when (list.size) {
    0 -> ""; 1 -> list[0]; else -> list.dropLast(1).joinToString(", ") + " and " + list.last()
}

private fun watchedMinutes(s: UiState): Int {
    val first = s.trail.firstOrNull(); val last = s.trail.lastOrNull()
    // Replays run faster than the wall clock; their fixes carry the trip's own time.
    if (first != null && last != null && last.t > first.t) return ((last.t - first.t) / 60).toInt()
    return if (s.startedAtMs > 0) ((System.currentTimeMillis() - s.startedAtMs) / 60_000).toInt() else 0
}

private fun modeLabel(s: UiState) = when (s.mode?.name) {
    "AUTO" -> "auto"; "CAB" -> "cab"; "BIKE" -> "bike ride"; "BUS" -> "bus"; "TRAIN" -> "train"; "WALK" -> "walk"; else -> "trip"
}

@Composable
private fun OnTrip(s: UiState) {
    val ctx = LocalContext.current
    val c = Saathi.colors
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { while (true) { delay(15_000); tick++ } }
    val minutes = remember(s.trail.size, s.startedAtMs, tick) { watchedMinutes(s) }

    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(s.driver?.let { "With $it" } ?: s.place.label ?: "On your way", style = MaterialTheme.typography.titleLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).background(c.ok, CircleShape))
                    Text("Watching your ${s.app?.let { "$it " } ?: ""}${modeLabel(s)}", style = MaterialTheme.typography.bodyMedium, color = c.fg2)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$minutes min", style = MaterialTheme.typography.titleLarge)
                Text("watched", style = MaterialTheme.typography.labelSmall, color = c.fg2)
            }
        }
        LineDiagram(s, minutes)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (s.plate != null) FilledTonalButton(onClick = {
                ctx.startActivity(Intent(ctx, PlateCheckActivity::class.java).putExtra(PlateCheckActivity.EXTRA_BOOKED, s.plate))
            }) { Icon(Icons.Rounded.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Check plate") }
            FilledTonalButton(onClick = { ctx.startForegroundService(TripService.intent(ctx, TripService.ACTION_STOP_TRIP)) }) {
                Icon(Icons.Rounded.StopCircle, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("End trip")
            }
        }
    }
}

/**
 * Her trip as a line: where it started, anything the watcher noticed, and her, with the watched
 * stretch stamped in ajrakh stars, one per minute (the newest few, once a trip runs long).
 */
@Composable
private fun LineDiagram(s: UiState, minutes: Int) {
    val c = Saathi.colors
    // When the trip stalls, the stop and her own station matter more than history: show fewer stars.
    val shown = minutes.coerceIn(0, if (s.checkInEndsAtMs != null || s.alertActive) 3 else 5)
    val firstShown = remember { shown }
    val stalled = s.checkInEndsAtMs != null || s.alertActive

    Column {
        // The trail carries the trip's own clock (replays run fast), so start and stops agree.
        val startMs = s.trail.firstOrNull()?.t?.times(1000) ?: s.startedAtMs
        // During an alert only the stop and her own station matter; the history steps aside.
        if (!s.alertActive) {
        Station(ring = c.fg3, title = "Started ${if (startMs > 0) time(startMs) else ""}",
            sub = listOfNotNull(s.app, s.plateCheck?.let { if (it.startsWith("match")) "plate matched" else "plate didn't match" }).joinToString(" · ").ifBlank { null },
            dim = true)
        Row {
            // The stars sit on a hairline rail, so they read as one thread, not loose beads.
            val rail = c.starOff
            Column(Modifier.width(26.dp).drawBehind {
                drawLine(rail, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 2.dp.toPx())
            }.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                // A long trip shows its newest few; two faint stars say the thread runs further back.
                if (minutes > shown) { Star(c.starOff, 8.dp); Star(c.starOff, 10.dp) }
                repeat(shown) { i ->
                    val newest = i == shown - 1
                    // A slight, fixed tilt per star, like a hand-placed block.
                    Star(if (newest && stalled) c.alum else c.starOn, 14.dp, stamp = i >= firstShown, tilt = ((i * 37) % 9 - 4).toFloat())
                }
                if (shown == 0) Box(Modifier.width(3.dp).height(18.dp).background(c.starOff, RoundedCornerShape(2.dp)))
            }
        }
        }
        s.events.forEach { e ->
            Station(ring = c.alum, title = e.detail.replaceFirstChar { it.uppercase() }, sub = time(e.at * 1000), dim = false, filled = true)
        }
        Station(ring = MaterialTheme.colorScheme.primary, title = "You" + (s.place.label?.let { " · $it" } ?: ""),
            sub = when {
                s.alertActive -> "Your contacts were told"
                s.checkInEndsAtMs != null -> "Checking in"
                s.events.isEmpty() -> "$minutes min watched, all quiet"
                else -> "$minutes min watched"
            }, dim = false, filled = true, big = true)
        if (s.app != null) Text("Ends on its own when the ${s.app} ride ends.", style = MaterialTheme.typography.bodySmall,
            color = c.fg2, modifier = Modifier.padding(start = 40.dp, top = 6.dp))
    }
}

@Composable
private fun Station(ring: Color, title: String, sub: String?, dim: Boolean, filled: Boolean = false, big: Boolean = false) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.width(26.dp).padding(top = 3.dp), contentAlignment = Alignment.Center) {
            val d = if (big) 18.dp else 14.dp
            Box(Modifier.size(d).clip(CircleShape).background(if (filled) ring else MaterialTheme.colorScheme.background)
                .border(3.dp, ring, CircleShape))
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = if (dim) Saathi.colors.fg2 else MaterialTheme.colorScheme.onBackground)
            sub?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Saathi.colors.fg2) }
        }
    }
}

@Composable
private fun Ready() {
    val ctx = LocalContext.current
    val c = Saathi.colors
    var autoOpen by remember { mutableStateOf(false) }
    var plate by remember { mutableStateOf("") }
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Uber, Ola and Rapido rides start on their own", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModeChip(Icons.AutoMirrored.Rounded.DirectionsWalk, "Walk", Modifier.weight(1f)) { startTrip(ctx, "WALK") }
            ModeChip(Icons.Rounded.DirectionsBus, "Bus", Modifier.weight(1f)) { startTrip(ctx, "BUS") }
            ModeChip(Icons.Rounded.Train, "Train", Modifier.weight(1f)) { startTrip(ctx, "TRAIN") }
            ModeChip(Icons.Rounded.ElectricRickshaw, "Auto", Modifier.weight(1f), selected = autoOpen) { autoOpen = !autoOpen }
        }
        if (autoOpen) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(plate, { plate = it.uppercase() }, Modifier.weight(1f), singleLine = true, label = { Text("Auto plate, if you can see it") })
            Button(onClick = { startTrip(ctx, "AUTO", plate.ifBlank { null }); autoOpen = false }, Modifier.height(56.dp)) { Text("Start") }
        }
    }
}

@Composable
private fun ModeChip(icon: ImageVector, label: String, modifier: Modifier, selected: Boolean = false, onClick: () -> Unit) {
    Column(
        modifier.height(72.dp).clip(MaterialTheme.shapes.medium)
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
    }
}

/** Help on the left, larger; Speak for me and the companion beside it. */
@Composable
private fun HelpRow(settings: Settings, quiet: Boolean = false) {
    val ctx = LocalContext.current
    // Grows with the system font size instead of clipping her contacts' names.
    Row(Modifier.padding(horizontal = 16.dp).heightIn(min = 140.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HoldForHelp(settings, Modifier.weight(1.15f).fillMaxHeight(), quiet)
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Tile(Icons.Rounded.RecordVoiceOver, "Speak for me", Modifier.weight(1f)) { ctx.startActivity(Intent(ctx, SpeakForMeActivity::class.java)) }
            Tile(Icons.Rounded.Call, "Call Saathi", Modifier.weight(1f)) { ctx.startActivity(Intent(ctx, CompanionActivity::class.java)) }
        }
    }
}

@Composable
private fun Tile(icon: ImageVector, label: String, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

/** Hold for 2 seconds to raise help, so a brush against the screen doesn't. The plate fills as you hold. */
@Composable
// quiet: help was already raised, so I'm okay owns the screen and this steps back to a tonal plate.
private fun HoldForHelp(settings: Settings, modifier: Modifier, quiet: Boolean = false) {
    val c = Saathi.colors
    val scope = rememberCoroutineScope()
    val fill = remember { Animatable(0f) }
    var holding by remember { mutableStateOf(false) }
    val who = names(settings.contacts.map { it.name }.take(2))
    Box(
        modifier.clip(RoundedCornerShape(32.dp)).background(if (quiet) c.helpContainer else c.help)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    holding = true
                    var job: Job? = null
                    job = scope.launch {
                        fill.snapTo(0f); fill.animateTo(1f, tween(2000, easing = LinearEasing))
                        AlertManager.raise(Reason.HOLD)
                    }
                    tryAwaitRelease()
                    job.cancel(); holding = false
                    scope.launch { fill.animateTo(0f, tween(250)) }
                })
            },
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fill.value).background(Color.Black.copy(alpha = 0.22f)))
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.Bottom) {
            Column {
                Text(if (holding) "Keep holding…" else if (quiet) "Hold to alert again" else "Hold for help", style = if (quiet) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium, color = if (quiet) c.onHelpContainer else Color.White)
                Text(if (who.isNotBlank()) "2 seconds · texts $who" else "2 seconds · texts your contacts",
                    style = MaterialTheme.typography.bodySmall, color = if (quiet) c.onHelpContainer.copy(alpha = 0.8f) else c.onHelpContainer.let { if (c.isDark) it else Color(0xFFFFE3DF) },
                    maxLines = 2, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/** After a trip: two taps to log a small thing, so "nothing major" still leaves a trace (RE-7). */
@Composable
private fun AfterTrip() {
    val ctx = LocalContext.current
    val s by TripState.ui.collectAsState()
    val scope = rememberCoroutineScope()
    var refresh by remember { mutableIntStateOf(0) }
    val records = remember(s.onTrip, refresh) { Store.records(ctx) }
    records.lastOrNull()?.takeIf { it.feltOff == null && !s.onTrip }?.let { last ->
        FeltOff { kind ->
            Store.saveRecord(ctx, last.copy(feltOff = kind))
            val consent = Store.settings(ctx).communityConsent
            if (kind != "nothing" && consent && last.lat != null && last.lng != null) {
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    app.saathi.data.Api.point(ctx, last.lat, last.lng, last.endedAt ?: last.createdAt, kind, last.plate)
                }
            }
            if (kind == "something_happened") ctx.startActivity(Intent(ctx, GuideActivity::class.java)
                .putExtra(GuideActivity.EXTRA_CASE, last.caseId).putExtra(GuideActivity.EXTRA_KIND, "something happened"))
            refresh++
        }
    }
}

@Composable
private fun FeltOff(onPick: (String) -> Unit) {
    val c = Saathi.colors
    Column(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow).border(1.dp, c.hair, MaterialTheme.shapes.large).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Anything felt off on that trip?", style = MaterialTheme.typography.titleMedium)
        Text("You don't need to say what happened.", style = MaterialTheme.typography.bodyMedium, color = c.fg2)
        Spacer(Modifier.height(4.dp))
        FilledTonalButton(onClick = { onPick("nothing") }, Modifier.fillMaxWidth().height(48.dp)) { Text("Nothing") }
        FilledTonalButton(onClick = { onPick("felt_off") }, Modifier.fillMaxWidth().height(48.dp)) { Text("Something felt off, nothing major") }
        Button(onClick = { onPick("something_happened") }, Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = c.help, contentColor = Color.White)) { Text("Something happened") }
    }
}

@Composable
private fun TripsTab() {
    val ctx = LocalContext.current
    val c = Saathi.colors
    val records = remember { Store.records(ctx) }
    val day = remember { SimpleDateFormat("EEE d MMM, h:mm a", Locale.ENGLISH) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My trips", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp))
        if (records.isEmpty()) Text("Trips Saathi watched will appear here, each with its sealed record.",
            style = MaterialTheme.typography.bodyLarge, color = c.fg2, modifier = Modifier.padding(horizontal = 6.dp))
        records.reversed().forEach { r -> TripCard(r, day.format(Date(r.createdAt * 1000))) {
            ctx.startActivity(Intent(ctx, GuideActivity::class.java).putExtra(GuideActivity.EXTRA_CASE, r.caseId))
        } }
    }
}

@Composable
private fun TripCard(r: TripRecord, whenText: String, onGuide: () -> Unit) {
    val c = Saathi.colors
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, c.hair, MaterialTheme.shapes.large).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(r.mode.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
                Text(whenText, style = MaterialTheme.typography.bodySmall, color = c.fg2)
            }
            r.plate?.let { PlateText(it) }
        }
        r.events.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        r.alerts.forEach { Text(it, style = MaterialTheme.typography.bodyMedium, color = c.help) }
        r.plateCheck?.let { Text("Plate check: $it", style = MaterialTheme.typography.bodyMedium, color = c.fg2) }
        if (r.evidence.isNotEmpty()) Column(Modifier.padding(top = 4.dp)) {
            Text("Sealed · fingerprints", style = MaterialTheme.typography.labelSmall, color = c.fg2)
            r.evidence.forEach {
                Text("${it.name}  ${it.sha256.take(16)}…", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = c.fg2)
            }
        }
        FilledTonalButton(onClick = onGuide, Modifier.padding(top = 6.dp)) { Text("Reporting options and draft") }
    }
}

@Composable
private fun DemoPanel() {
    val ctx = LocalContext.current
    var cityOverride by remember { mutableStateOf("") }
    Column(
        Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Demo panel (sample data)", style = MaterialTheme.typography.titleSmall)
        OutlinedButton(onClick = { startTrip(ctx, "AUTO", "KA05AB1234", "Uber", replay = "blr-auto") }) { Text("Replay: Bengaluru Uber auto, 30×") }
        OutlinedButton(onClick = { startTrip(ctx, "BUS", replay = "pune-bus") }) { Text("Replay: Pune bus, 30×") }
        Text("Fake ride-app notifications", style = MaterialTheme.typography.labelMedium)
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

private fun stateFor(city: String) = when (city.trim().lowercase()) {
    "bengaluru", "bangalore" -> "Karnataka"
    "pune", "mumbai" -> "Maharashtra"
    "delhi" -> "Delhi"
    else -> null
}
