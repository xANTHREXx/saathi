package app.saathi.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.saathi.alert.AlertManager
import app.saathi.core.Plate
import app.saathi.core.Reason
import app.saathi.data.Api
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Compares the plate on the vehicle in front of her with the one the ride app booked. */
class PlateCheckActivity : ComponentActivity() {
    companion object { const val EXTRA_BOOKED = "booked" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AlertManager.init(this)
        val booked = intent.getStringExtra(EXTRA_BOOKED) ?: TripState.now.plate
        setContent { PlateCheck(booked) { finish() } }
    }

    @Composable
    private fun PlateCheck(booked: String?, done: () -> Unit) {
        val scope = rememberCoroutineScope()
        var seen by remember { mutableStateOf<String?>(null) }
        var typed by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }
        var note by remember { mutableStateOf<String?>(null) }
        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
            if (bmp == null) return@rememberLauncherForActivityResult
            busy = true
            scope.launch {
                val jpeg = ByteArrayOutputStream().also { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }.toByteArray()
                val plate = withContext(Dispatchers.IO) { Api.readPlate(jpeg) }
                busy = false
                if (plate != null) seen = plate else note = "Couldn't read the plate. Type it below."
            }
        }
        val match = seen?.let { s -> booked?.let { Plate.matches(it, s) } }
        val bg = when (match) { true -> Color(0xFFE4F3EA); false -> Color(0xFFFDE7E4); null -> Color(0xFFF3F4F1) }

        Column(Modifier.fillMaxSize().background(bg).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Check the plate", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1D21))
            Text("Your booking says: ${booked ?: "no plate shown"}", fontSize = 18.sp, color = Color(0xFF1A1D21))
            when (match) {
                null -> {
                    Button(onClick = { camera.launch(null) }, Modifier.fillMaxWidth().height(64.dp), enabled = !busy) {
                        Text(if (busy) "Reading the plate…" else "Snap the plate", fontSize = 20.sp)
                    }
                    note?.let { Text(it, color = Color(0xFF7A1A12)) }
                    OutlinedTextField(typed, { typed = it.uppercase() }, Modifier.fillMaxWidth(), singleLine = true,
                        label = { Text("Or type the plate you see") })
                    OutlinedButton(onClick = { if (typed.isNotBlank()) seen = Plate.normalise(typed) }) { Text("Compare") }
                }
                true -> {
                    Text("✓ It matches: $seen", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F6F4A))
                    TripState.update { it.copy(plateCheck = "match: $seen") }
                    Button(onClick = done, Modifier.fillMaxWidth()) { Text("Done") }
                }
                false -> {
                    Text("This plate is $seen. Your ride app says $booked.", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7A1A12))
                    TripState.update { it.copy(plateCheck = "mismatch: saw $seen, booked $booked") }
                    Button(onClick = {
                        startForegroundService(TripService.intent(this@PlateCheckActivity, TripService.ACTION_STOP_TRIP)); done()
                    }, Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC8352A))) {
                        Text("Don't get in", fontSize = 18.sp)
                    }
                    Button(onClick = { AlertManager.raise(Reason.PLATE_MISMATCH); done() }, Modifier.fillMaxWidth()) {
                        Text("Tell my contacts")
                    }
                    OutlinedButton(onClick = done, Modifier.fillMaxWidth()) { Text("It's fine") }
                }
            }
        }
    }
}
