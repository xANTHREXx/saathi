package app.saathi.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.alert.AlertManager
import app.saathi.core.Reason
import app.saathi.core.languagesFor
import app.saathi.data.Store
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.ui.theme.Saathi
import app.saathi.ui.theme.SaathiTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale

/** Says her phrase for her, loud, in the local language, then Hindi, then English. Works offline. Always day: a bright sheet a stranger can read. */
class SpeakForMeActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private val speaking = mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT), androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        AlertManager.init(this)
        startForegroundService(TripService.intent(this, TripService.ACTION_GUARD))

        val p = TripState.now.place
        val langs = languagesFor(p.countryCode ?: "IN", p.state)
        val lines = phrases(Store.settings(this).phraseKey).let { all ->
            langs.map { it to (all[it] ?: all["hi-IN"] ?: "") }
        }

        setContent { SaathiTheme(dark = false) { Speak(lines, speaking.intValue, onAgain = { speak(lines) }, onClose = { finish() }) } }

        tts = TextToSpeech(this) { ok -> if (ok == TextToSpeech.SUCCESS) speak(lines) }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) { id?.removePrefix("say-")?.toIntOrNull()?.let { speaking.intValue = it } }
                override fun onDone(id: String?) { if (id == "gap-${lines.lastIndex}") speaking.intValue = -1 }
                @Deprecated("Deprecated in Java") override fun onError(id: String?) {}
            })
        }
        AlertManager.raise(Reason.SPEAK_FOR_ME)
    }

    private fun speak(lines: List<Pair<String, String>>) {
        val t = tts ?: return
        t.stop()
        lines.forEachIndexed { i, (lang, text) ->
            val res = t.setLanguage(Locale.forLanguageTag(lang))
            if (res >= TextToSpeech.LANG_AVAILABLE) {
                t.speak(text, TextToSpeech.QUEUE_ADD, null, "say-$i")
                t.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, "gap-$i")
            }
        }
    }

    private fun phrases(key: String): Map<String, String> = runCatching {
        val root = Json.parseToJsonElement(assets.open("phrases.json").bufferedReader().readText()).jsonObject
        (root[key] ?: root["touched"])!!.jsonObject.mapValues { it.value.jsonPrimitive.content }
    }.getOrDefault(mapOf("en-IN" to "Please help me."))

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}

private fun languageName(tag: String) = when (tag.substringBefore('-')) {
    "kn" -> "ಕನ್ನಡ"; "hi" -> "हिन्दी"; "mr" -> "मराठी"; "ta" -> "தமிழ்"; "te" -> "తెలుగు"; "ml" -> "മലയാളം"
    "bn" -> "বাংলা"; "gu" -> "ગુજરાતી"; "en" -> "English"; else -> tag
}

@Composable
private fun Speak(lines: List<Pair<String, String>>, speaking: Int, onAgain: () -> Unit, onClose: () -> Unit) {
    val s by TripState.ui.collectAsState()
    val c = Saathi.colors
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(c.helpContainer).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(36.dp).background(c.help, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(if (s.told.isEmpty()) "Telling your contacts…" else "${s.told.joinToString(" and ")} ${if (s.told.size == 1) "was" else "were"} told",
                    style = MaterialTheme.typography.titleSmall, color = c.onHelpContainer)
                Text("Live location sent · recording on this phone · emergency step simulated",
                    style = MaterialTheme.typography.bodySmall, color = c.onHelpContainer.copy(alpha = 0.8f))
            }
        }
        Column(
            Modifier.padding(top = 12.dp).weight(1f).fillMaxWidth().clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest).border(1.dp, c.hair, MaterialTheme.shapes.large)
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            lines.forEachIndexed { i, (lang, text) ->
                if (i > 0) HorizontalDivider(color = c.hair)
                Column(Modifier.padding(vertical = 18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)) {
                        if (i == speaking) Icon(Icons.Rounded.VolumeUp, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text(languageName(lang) + if (i == speaking) " · speaking" else "", style = MaterialTheme.typography.labelMedium,
                            color = if (i == speaking) MaterialTheme.colorScheme.primary else c.fg2)
                    }
                    Text(text, style = when (i) {
                        0 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 38.sp)
                        1 -> MaterialTheme.typography.headlineSmall.copy(lineHeight = 32.sp)
                        else -> MaterialTheme.typography.titleLarge
                    })
                }
            }
        }
        Row(Modifier.padding(top = 12.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onAgain, Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) {
                Icon(Icons.Rounded.Replay, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Say it again")
            }
            FilledTonalButton(onClose, Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(20.dp)) { Text("Close") }
        }
    }
}
