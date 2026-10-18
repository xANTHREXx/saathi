package app.saathi.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import app.saathi.alert.AlertManager
import app.saathi.core.Reason
import app.saathi.core.languagesFor
import app.saathi.data.Api
import app.saathi.trip.TripState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * The companion call: she talks out loud, Saathi answers like an elder sister, names landmarks,
 * gives one next step, and raises the alert when she asks for help. Push-to-talk, or type.
 */
class CompanionActivity : ComponentActivity() {
    private val lines = mutableStateListOf<Pair<String, String>>() // role to text
    private val listening = mutableStateOf(false)
    private val offline = mutableStateOf(false)
    private val typed = mutableStateOf("")
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private lateinit var lang: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        AlertManager.init(this)
        val p = TripState.now.place
        lang = languagesFor(p.countryCode ?: "IN", p.state).let { if (it.first() == "hi-IN") "hi-IN" else it.first() }
        tts = TextToSpeech(this) { ok ->
            if (ok == TextToSpeech.SUCCESS) say("I'm here. Talk to me normally, out loud.")
        }
        if (SpeechRecognizer.isRecognitionAvailable(this)) recognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onResults(results: Bundle) {
                    listening.value = false
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { send(it) }
                }
                override fun onError(error: Int) { listening.value = false }
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        setContent {
            Column(Modifier.fillMaxSize().background(Color(0xFF1A1D21)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Saathi is on the call", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(if (offline.value) "No connection. Use Help or Speak for me." else "Speak out loud, so anyone near you hears you're on a call.",
                    color = if (offline.value) Color(0xFFF2B705) else Color(0xFFA3A9AF))
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(lines) { (role, text) ->
                        Text(text, color = if (role == "saathi") Color(0xFFF2B705) else Color.White, fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth().padding(start = if (role == "saathi") 0.dp else 40.dp))
                    }
                }
                if (TripState.now.alertActive) Text("Your contacts were told: ${TripState.now.told.joinToString()}", color = Color(0xFFFF8A7A))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(typed.value, { typed.value = it }, Modifier.weight(1f), singleLine = true,
                        label = { Text("Or type", color = Color(0xFFA3A9AF)) })
                    Button(onClick = { if (typed.value.isNotBlank()) { send(typed.value); typed.value = "" } }) { Text("Send") }
                }
                Box(
                    Modifier.fillMaxWidth().height(96.dp).clip(RoundedCornerShape(20.dp))
                        .background(if (listening.value) Color(0xFF2E8A5C) else Color(0xFF1F6F4A))
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = { startListening(); tryAwaitRelease(); recognizer?.stopListening() })
                        },
                    contentAlignment = Alignment.Center,
                ) { Text(if (listening.value) "Listening…" else "Hold to talk", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { AlertManager.raise(Reason.COMPANION) }, Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8352A))) { Text("Help") }
                    Button(onClick = { startActivity(Intent(this@CompanionActivity, SpeakForMeActivity::class.java)) }, Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B705), contentColor = Color(0xFF111111))) { Text("Speak for me") }
                    Button(onClick = { finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3F45))) { Text("End call") }
                }
            }
        }
    }

    private fun startListening() {
        val r = recognizer ?: return
        listening.value = true
        r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang))
    }

    private fun send(text: String) {
        lines += "her" to text
        val fix = TripState.now.lastFix
        val history = lines.toList()
        lifecycleScope.launch {
            val r = withContext(Dispatchers.IO) { Api.companion(text, lang, fix?.lat, fix?.lng, history) }
            if (r == null) { offline.value = true; return@launch }
            offline.value = false
            lines += "saathi" to r.reply
            say(r.reply)
            if (r.raiseAlert) AlertManager.raise(Reason.COMPANION)
        }
    }

    private fun say(text: String) {
        val t = tts ?: return
        t.setLanguage(Locale.forLanguageTag(lang))
        t.speak(text, TextToSpeech.QUEUE_FLUSH, null, "reply")
    }

    override fun onDestroy() {
        tts?.shutdown(); recognizer?.destroy()
        super.onDestroy()
    }
}
