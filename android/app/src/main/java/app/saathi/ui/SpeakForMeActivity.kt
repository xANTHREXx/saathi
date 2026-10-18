package app.saathi.ui

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.alert.AlertManager
import app.saathi.core.Reason
import app.saathi.core.languagesFor
import app.saathi.data.Store
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Locale

/** Says her phrase for her, loud, in the local language, then Hindi, then English. Works offline. */
class SpeakForMeActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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

        setContent {
            Column(
                Modifier.fillMaxSize().background(Color(0xFFF2B705)).padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                lines.forEachIndexed { i, (_, text) ->
                    Text(text, color = Color(0xFF111111), textAlign = TextAlign.Center,
                        fontSize = if (i == 0) 38.sp else 26.sp, fontWeight = if (i == 0) FontWeight.Black else FontWeight.SemiBold,
                        lineHeight = if (i == 0) 44.sp else 32.sp)
                }
                Button(onClick = { speak(lines) }, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111111))) {
                    Text("Say it again", fontSize = 20.sp)
                }
                Button(onClick = { finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0x33111111))) {
                    Text("Close", color = Color(0xFF111111))
                }
            }
        }

        tts = TextToSpeech(this) { ok -> if (ok == TextToSpeech.SUCCESS) speak(lines) }
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
