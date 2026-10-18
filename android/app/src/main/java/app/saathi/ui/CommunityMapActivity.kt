package app.saathi.ui

import android.graphics.Color as AColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import app.saathi.core.Fix
import app.saathi.core.Report
import app.saathi.core.areaNote
import app.saathi.data.Api
import app.saathi.trip.TripState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon

/** Where women have reported trouble, as a heatmap. Counts only; no names, no exact places. */
class CommunityMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName
        setContent { app.saathi.ui.theme.SaathiTheme(dark = false) { CommunityMap() } }
    }

    @Composable
    private fun CommunityMap() {
        var data by remember { mutableStateOf<Pair<Boolean, List<Api.Pt>>?>(null) }
        var failed by remember { mutableStateOf(false) }
        var nightOnly by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            data = withContext(Dispatchers.IO) { Api.communityPoints() }
            failed = data == null
        }
        val here = TripState.now.lastFix ?: Fix(12.9250, 77.6200, 0)
        val pts = data?.second?.filter { !nightOnly || it.band == 7 || it.band == 0 } ?: emptyList()
        val note = data?.second?.let { all -> areaNote(all.map { Report(it.lat, it.lng, it.band) }, here, nightOnly) }

        Column(Modifier.fillMaxSize().background(Color(0xFFF5F6FA)).systemBarsPadding()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Community map", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D1222))
                    if (data?.first == true) Text("Sample data", color = Color(0xFF0D1222), fontSize = 12.sp,
                        modifier = Modifier.background(Color(0xFF8FA2E8), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Text("Where women reported something, even small things, in the last 90 days. Never names, never exact places.",
                    color = Color(0xFF4D5675), fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(nightOnly, { nightOnly = true }, label = { Text("After 9 pm") })
                    FilterChip(!nightOnly, { nightOnly = false }, label = { Text("All day") })
                }
                when {
                    failed -> Text("No connection. The map needs data.", color = Color(0xFFB42A23))
                    note != null -> Text(note, fontWeight = FontWeight.Bold, color = Color(0xFFB42A23))
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(14.0)
                            controller.setCenter(GeoPoint(here.lat, here.lng))
                        }
                    },
                    update = { map ->
                        map.overlays.removeAll { it is Polygon }
                        pts.forEach { p ->
                            map.overlays += Polygon(map).apply {
                                points = Polygon.pointsAsCircle(GeoPoint(p.lat, p.lng), 140.0)
                                fillPaint.color = AColor.argb(70, 200, 53, 42)
                                outlinePaint.color = AColor.TRANSPARENT
                                setOnClickListener { _, _, _ -> true }
                            }
                        }
                        map.invalidate()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
