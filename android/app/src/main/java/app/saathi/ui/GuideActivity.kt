package app.saathi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
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
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.data.Api
import app.saathi.data.Store
import app.saathi.trip.TripState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Options for reporting and a draft complaint, with sources. She edits and files it herself. */
class GuideActivity : ComponentActivity() {
    companion object { const val EXTRA_CASE = "case"; const val EXTRA_KIND = "kind" }

    // Shown when the server or the model can't answer. No numbers or legal claims that nobody reviewed.
    private val offline = """This is information, not legal advice.

YOUR OPTIONS
• Tell someone close what happened. You don't have to decide anything else today.
• If it was a ride-app trip, report it in the app's Help or Safety section. Keep your trip record.
• The police: in India you can ask any police station to register a Zero FIR, even if it happened elsewhere. You can bring someone you trust.
• A women's helpline in your state can advise you on the next step.

Your trip record below has fingerprints for each piece of evidence, so you can show it wasn't changed."""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val caseId = intent.getStringExtra(EXTRA_CASE)
        val kind = intent.getStringExtra(EXTRA_KIND) ?: "something happened"
        val record = Store.records(this).firstOrNull { it.caseId == caseId }
        setContent {
            var text by remember { mutableStateOf<String?>(null) }
            var sources by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            LaunchedEffect(Unit) {
                val g = record?.let { r ->
                    withContext(Dispatchers.IO) { Api.guide(Json.encodeToString(r), kind, TripState.now.place.state) }
                }
                text = g?.markdown ?: offline
                sources = g?.sources ?: emptyList()
            }
            Column(Modifier.fillMaxSize().background(Color(0xFFF3F4F1)).verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What you can do next", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1D21))
                Text(text ?: "Preparing your options…", fontSize = 16.sp, color = Color(0xFF1A1D21), lineHeight = 23.sp)
                if (sources.isNotEmpty()) {
                    Text("Sources", fontWeight = FontWeight.Bold)
                    sources.forEach { (t, u) -> Text("$t\n$u", fontSize = 13.sp, color = Color(0xFF1F6F4A)) }
                }
                record?.let { r ->
                    Text("Trip record ${r.caseId}", fontWeight = FontWeight.Bold)
                    r.evidence.forEach { Text("${it.name}: ${it.sha256}", fontSize = 12.sp, color = Color(0xFF5B6168)) }
                }
                Button(onClick = {
                    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Saathi draft", text ?: ""))
                }, Modifier.fillMaxWidth()) { Text("Copy, to edit and send yourself") }
            }
        }
    }
}
