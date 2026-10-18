package app.saathi.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import app.saathi.trip.UiState
import app.saathi.ui.theme.Saathi
import app.saathi.ui.theme.ajrakhStar
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay

/**
 * The calm map above the trip sheet: her path so far and where she is. At night the tiles are
 * inverted and dimmed into the indigo ground. With no location yet, an empty indigo field.
 */
@Composable
fun TripMap(s: UiState, modifier: Modifier = Modifier) {
    val c = Saathi.colors
    val scheme = MaterialTheme.colorScheme
    val here = s.lastFix
    Box(modifier.clipToBounds().background(scheme.surfaceContainerHigh)) {
        if (here == null) {
            Canvas(Modifier.size(64.dp).align(Alignment.Center).alpha(0.18f)) { ajrakhStar(c.starOn) }
        } else {
            val line = scheme.primary.toArgb()
            val walked = c.fg3.toArgb()
            val me = scheme.primary.toArgb()
            AndroidView(
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = ctx.packageName
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(false)
                        isClickable = false
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(15.5)
                        overlayManager.tilesOverlay.setColorFilter(if (c.isDark) indigoNight() else indigoDay())
                    }
                },
                update = { map ->
                    map.overlays.clear()
                    val pts = s.trail.map { GeoPoint(it.lat, it.lng) }
                    if (pts.size > 1) {
                        // A hairline under the route, with the watched stretch stamped in stars on top.
                        map.overlays += Polyline(map).apply {
                            setPoints(pts)
                            outlinePaint.apply { color = (walked and 0x00FFFFFF) or 0x99000000.toInt(); strokeWidth = 5f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND }
                        }
                        map.overlays += StarTrail(pts, if (s.onTrip) line else walked, gapPx = 34f, radiusPx = 13f)
                    }
                    val p = GeoPoint(here.lat, here.lng)
                    map.overlays += Polygon(map).apply {
                        points = Polygon.pointsAsCircle(p, 140.0)
                        fillPaint.color = (me and 0x00FFFFFF) or 0x33000000
                        outlinePaint.color = android.graphics.Color.TRANSPARENT
                    }
                    map.overlays += Polygon(map).apply {
                        points = Polygon.pointsAsCircle(p, 45.0)
                        fillPaint.color = me
                        outlinePaint.apply { color = android.graphics.Color.WHITE; strokeWidth = 6f }
                    }
                    // Keep her in the upper half, clear of the sheet.
                    map.controller.setCenter(GeoPoint(here.lat - 0.0016, here.lng))
                    map.invalidate()
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Let the map fall away into the sheet's ground.
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to scheme.background.copy(alpha = 0.55f), 0.18f to scheme.background.copy(alpha = 0f),
            0.7f to scheme.background.copy(alpha = 0f), 1f to scheme.background,
        )))
    }
}

/**
 * Map tiles recoloured into the palette: luminance only, inverted for night, mapped onto the
 * indigo ramp, so streets read as quiet structure under her line instead of a coloured map.
 */
private fun indigoRamp(lo: FloatArray, hi: FloatArray, invert: Boolean): android.graphics.ColorMatrixColorFilter {
    val (wr, wg, wb) = Triple(0.30f, 0.59f, 0.11f)
    val sign = if (invert) -1f else 1f
    fun row(i: Int): FloatArray {
        val span = (hi[i] - lo[i]) / 255f
        val base = if (invert) hi[i] else lo[i]
        return floatArrayOf(sign * wr * span, sign * wg * span, sign * wb * span, 0f, base)
    }
    return android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(row(0) + row(1) + row(2) + floatArrayOf(0f, 0f, 0f, 1f, 0f)))
}

private fun indigoNight() = indigoRamp(floatArrayOf(10f, 14f, 30f), floatArrayOf(52f, 64f, 110f), invert = true)
private fun indigoDay() = indigoRamp(floatArrayOf(190f, 196f, 214f), floatArrayOf(250f, 251f, 253f), invert = false)

/** Ajrakh stars stamped along her path at a fixed screen spacing, so the map carries the same thread as the sheet. */
private class StarTrail(private val pts: List<GeoPoint>, color: Int, private val gapPx: Float, private val radiusPx: Float) :
    org.osmdroid.views.overlay.Overlay() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    private val square = android.graphics.Path()

    override fun draw(canvas: android.graphics.Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow || pts.size < 2) return
        val proj = mapView.projection
        val px = pts.map { proj.toPixels(it, null) }
        var carry = 0f
        for (i in 1 until px.size) {
            val a = px[i - 1]; val b = px[i]
            val dx = (b.x - a.x).toFloat(); val dy = (b.y - a.y).toFloat()
            val len = kotlin.math.hypot(dx, dy)
            var d = gapPx - carry
            while (d <= len) {
                star(canvas, a.x + dx * d / len, a.y + dy * d / len)
                d += gapPx
            }
            carry = len - (d - gapPx)
        }
    }

    private fun star(c: android.graphics.Canvas, x: Float, y: Float) {
        val s = radiusPx * 0.72f
        square.reset(); square.addRect(x - s, y - s, x + s, y + s, android.graphics.Path.Direction.CW)
        c.drawPath(square, paint)
        c.save(); c.rotate(45f, x, y); c.drawPath(square, paint); c.restore()
    }
}
