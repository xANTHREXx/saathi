package app.saathi.ui

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.saathi.alert.AlertManager
import app.saathi.core.Plate
import app.saathi.core.Reason
import app.saathi.data.Api
import app.saathi.trip.TripService
import app.saathi.trip.TripState
import app.saathi.ui.theme.PlateText
import app.saathi.ui.theme.Saathi
import app.saathi.ui.theme.SaathiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Compares the plate on the vehicle in front of her with the one the ride app booked. */
class PlateCheckActivity : ComponentActivity() {
    companion object { const val EXTRA_BOOKED = "booked" }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AlertManager.init(this)
        val booked = intent.getStringExtra(EXTRA_BOOKED) ?: TripState.now.plate
        setContent { SaathiTheme { PlateCheck(booked) { finish() } } }
    }

    @Composable
    private fun PlateCheck(booked: String?, done: () -> Unit) {
        val c = Saathi.colors
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
        LaunchedEffect(match, seen) {
            when (match) {
                true -> TripState.update { it.copy(plateCheck = "match: $seen") }
                false -> TripState.update { it.copy(plateCheck = "mismatch: saw $seen, booked $booked") }
                null -> {}
            }
        }

        Column(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(when (match) { null -> "Check the plate"; true -> "It matches your booking"; false -> "This isn't the plate you booked" },
                style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
            Text(when (match) {
                null -> "Snap the number plate of the vehicle in front of you. Saathi compares it with your booking."
                true -> "The plate in front of you is the one your ride app booked."
                false -> "The vehicle in front of you has a different number from your booking."
            }, style = MaterialTheme.typography.bodyLarge, color = c.fg2, modifier = Modifier.padding(horizontal = 8.dp))

            // The comparison sits in the optical middle, not stranded at the top.
            Spacer(Modifier.weight(0.8f))
            Column(
                Modifier.padding(top = 20.dp).fillMaxWidth().clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow).border(1.dp, c.hair, MaterialTheme.shapes.large),
            ) {
                seen?.let { PlateRow(Icons.Rounded.PhotoCamera, "In front of you", it, ring = if (match == false) c.help else null) }
                if (seen != null) HorizontalDivider(color = c.hair)
                PlateRow(Icons.Rounded.ReceiptLong, "Booked", booked, ring = null, ok = match == true)
            }

            Spacer(Modifier.weight(1f))
            when (match) {
                null -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    note?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = c.help, modifier = Modifier.padding(horizontal = 8.dp)) }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(typed, { typed = it.uppercase() }, Modifier.weight(1f), singleLine = true,
                            label = { Text("Or type the plate you see") })
                        FilledTonalButton(onClick = { if (typed.isNotBlank()) seen = Plate.normalise(typed) }, Modifier.height(56.dp),
                            shape = RoundedCornerShape(16.dp)) { Text("Compare") }
                    }
                    Button(onClick = { camera.launch(null) }, Modifier.fillMaxWidth().height(64.dp), enabled = !busy, shape = RoundedCornerShape(24.dp)) {
                        if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(Icons.Rounded.PhotoCamera, null)
                        Spacer(Modifier.width(10.dp))
                        Text(if (busy) "Reading the plate…" else "Snap the plate", style = MaterialTheme.typography.titleMedium)
                    }
                }
                true -> Button(onClick = done, Modifier.fillMaxWidth().height(60.dp), shape = RoundedCornerShape(24.dp)) { Text("Done") }
                false -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { startForegroundService(TripService.intent(this@PlateCheckActivity, TripService.ACTION_STOP_TRIP)); done() },
                        Modifier.fillMaxWidth().height(72.dp), shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = c.help, contentColor = Color.White),
                    ) { Icon(Icons.Rounded.Block, null); Spacer(Modifier.width(10.dp)); Text("Don't get in", style = MaterialTheme.typography.headlineSmall) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { AlertManager.raise(Reason.PLATE_MISMATCH); done() }, Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(20.dp)) { Icon(Icons.Rounded.Sms, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Tell my contacts") }
                        FilledTonalButton(onClick = done, Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(20.dp)) { Text("It's fine") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlateRow(icon: ImageVector, label: String, plate: String?, ring: Color?, ok: Boolean = false) {
    val c = Saathi.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.fg2, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.fg2, modifier = Modifier.padding(start = 10.dp).weight(1f))
        if (ok) Box(Modifier.padding(end = 10.dp).size(24.dp).background(c.ok, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        if (plate != null) PlateText(plate, ring = ring, big = true) else Text("No plate shown", style = MaterialTheme.typography.bodyMedium)
    }
}
