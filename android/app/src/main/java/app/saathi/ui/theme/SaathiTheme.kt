package app.saathi.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.saathi.R

// ---------------------------------------------------------------- colour

/** Night: indigo cloth. Day: resist white. Madder is spent only on help and alerts. */
private object Night {
    val bg = Color(0xFF0D1222); val s1 = Color(0xFF141A2E); val s2 = Color(0xFF1B2340); val s3 = Color(0xFF252F52)
    val fg = Color(0xFFEEF0F7); val fg2 = Color(0xFFAAB2CC); val fg3 = Color(0xFF6F7899)
    val brand = Color(0xFF8FA2E8); val brandC = Color(0xFF25336B); val onBrandC = Color(0xFFDCE2FF)
    val help = Color(0xFFC7372F); val helpC = Color(0xFF5A1714); val onHelpC = Color(0xFFFFDAD5)
    val alum = Color(0xFFE7B94A); val ok = Color(0xFF7BD3A4); val hair = Color(0x14FFFFFF)
}

private object Day {
    val bg = Color(0xFFF5F6FA); val s1 = Color(0xFFFFFFFF); val s2 = Color(0xFFEEF0F7); val s3 = Color(0xFFE2E6F2)
    val fg = Color(0xFF11162A); val fg2 = Color(0xFF4D5675); val fg3 = Color(0xFF8790AD)
    val brand = Color(0xFF2E3F86); val brandC = Color(0xFFDCE2FF); val onBrandC = Color(0xFF18245A)
    val help = Color(0xFFB42A23); val helpC = Color(0xFFFFDAD5); val onHelpC = Color(0xFF5A1714)
    val alum = Color(0xFF8A5F00); val ok = Color(0xFF1E7A4C); val hair = Color(0x1A0D1222)
}

/** Roles Material has no slot for. */
@Immutable
data class SaathiColors(
    val help: Color, val onHelp: Color, val helpContainer: Color, val onHelpContainer: Color,
    val alum: Color, val ok: Color, val hair: Color, val starOn: Color, val starOff: Color,
    val fg2: Color, val fg3: Color, val isDark: Boolean,
)

val LocalSaathi = staticCompositionLocalOf {
    SaathiColors(Night.help, Color.White, Night.helpC, Night.onHelpC, Night.alum, Night.ok, Night.hair,
        Night.fg, Color(0x22FFFFFF), Night.fg2, Night.fg3, true)
}

private val darkScheme: ColorScheme = darkColorScheme(
    primary = Night.brand, onPrimary = Night.bg, primaryContainer = Night.brandC, onPrimaryContainer = Night.onBrandC,
    secondary = Night.alum, onSecondary = Night.bg, secondaryContainer = Night.s3, onSecondaryContainer = Night.fg,
    background = Night.bg, onBackground = Night.fg, surface = Night.bg, onSurface = Night.fg,
    surfaceVariant = Night.s2, onSurfaceVariant = Night.fg2,
    surfaceContainerLowest = Night.bg, surfaceContainerLow = Night.s1, surfaceContainer = Night.s1,
    surfaceContainerHigh = Night.s2, surfaceContainerHighest = Night.s3,
    outline = Night.fg3, outlineVariant = Night.s3,
    error = Night.help, onError = Color.White, errorContainer = Night.helpC, onErrorContainer = Night.onHelpC,
    inverseSurface = Night.fg, inverseOnSurface = Night.bg,
)

private val lightScheme: ColorScheme = lightColorScheme(
    primary = Day.brand, onPrimary = Color.White, primaryContainer = Day.brandC, onPrimaryContainer = Day.onBrandC,
    secondary = Day.alum, onSecondary = Color.White, secondaryContainer = Day.s3, onSecondaryContainer = Day.fg,
    background = Day.bg, onBackground = Day.fg, surface = Day.bg, onSurface = Day.fg,
    surfaceVariant = Day.s2, onSurfaceVariant = Day.fg2,
    surfaceContainerLowest = Day.s1, surfaceContainerLow = Day.s1, surfaceContainer = Day.s1,
    surfaceContainerHigh = Day.s2, surfaceContainerHighest = Day.s3,
    outline = Day.fg3, outlineVariant = Day.s3,
    error = Day.help, onError = Color.White, errorContainer = Day.helpC, onErrorContainer = Day.onHelpC,
    inverseSurface = Day.fg, inverseOnSurface = Day.bg,
)

// ---------------------------------------------------------------- type

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun figtree(w: Int) = Font(R.font.figtree, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))

/** Figtree for Latin; Android's Noto fallback renders Kannada and Devanagari. */
val Figtree = FontFamily(figtree(400), figtree(500), figtree(600), figtree(700))

private fun t(size: Int, line: Int, w: Int, track: Double = 0.0) =
    TextStyle(fontFamily = Figtree, fontSize = size.sp, lineHeight = line.sp, fontWeight = FontWeight(w), letterSpacing = track.em)

private val type = Typography(
    displayLarge = t(46, 48, 600, -0.03), displayMedium = t(40, 44, 600, -0.03), displaySmall = t(34, 38, 600, -0.025),
    headlineLarge = t(30, 34, 600, -0.02), headlineMedium = t(28, 32, 600, -0.02), headlineSmall = t(24, 28, 600, -0.015),
    titleLarge = t(22, 28, 600, -0.015), titleMedium = t(17, 22, 600, -0.005), titleSmall = t(15, 20, 600),
    bodyLarge = t(16, 23, 400), bodyMedium = t(15, 21, 400), bodySmall = t(13, 18, 400),
    labelLarge = t(15, 20, 600), labelMedium = t(13, 18, 500), labelSmall = t(12, 16, 500),
)

private val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun SaathiTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val extra = if (dark) SaathiColors(Night.help, Color.White, Night.helpC, Night.onHelpC, Night.alum, Night.ok, Night.hair,
        Night.fg, Color(0x24FFFFFF), Night.fg2, Night.fg3, true)
    else SaathiColors(Day.help, Color.White, Day.helpC, Day.onHelpC, Day.alum, Day.ok, Day.hair,
        Day.brand, Color(0x262E3F86), Day.fg2, Day.fg3, false)
    CompositionLocalProvider(LocalSaathi provides extra) {
        MaterialTheme(colorScheme = if (dark) darkScheme else lightScheme, typography = type, shapes = shapes) {
            // Screens that draw their own ground (no Surface) still get readable text.
            CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onBackground, content = content)
        }
    }
}

object Saathi {
    val colors: SaathiColors @Composable get() = LocalSaathi.current
}

// ---------------------------------------------------------------- the ajrakh star

/** Eight-point star: two squares, one turned 45 degrees. The trip thread and the app mark. */
fun DrawScope.ajrakhStar(color: Color, center: Offset = this.center, radius: Float = size.minDimension / 2f) {
    val s = radius * 0.72f
    val sq = Path().apply {
        moveTo(center.x - s, center.y - s); lineTo(center.x + s, center.y - s)
        lineTo(center.x + s, center.y + s); lineTo(center.x - s, center.y + s); close()
    }
    drawPath(sq, color)
    rotate(45f, center) { drawPath(sq, color) }
}

private val Stamp = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** True when the system's Remove animations setting is on. */
@Composable
fun reduceMotion(): Boolean {
    val cr = LocalContext.current.contentResolver
    return remember { android.provider.Settings.Global.getFloat(cr, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }
}

/**
 * One star of the thread. [stamp] presses it in like a block on cloth: it lands large and light,
 * then settles to size and full ink. [tilt] is a small fixed turn so a row looks hand-placed.
 */
@Composable
fun Star(color: Color, size: Dp = 12.dp, stamp: Boolean = false, tilt: Float = 0f, modifier: Modifier = Modifier) {
    val animate = stamp && !reduceMotion()
    val s = remember { Animatable(if (animate) 1.6f else 1f) }
    val ink = remember { Animatable(if (animate) 0.35f else 1f) }
    if (animate) LaunchedEffect(Unit) {
        launch { ink.animateTo(1f, tween(260, easing = Stamp)) }
        s.animateTo(1f, tween(200, easing = Stamp))
    }
    Canvas(modifier.size(size).scale(s.value).rotate(tilt).alpha(ink.value)) { ajrakhStar(color) }
}

/** The in-app mark: an indigo tile with a resist-white star. Madder stays reserved for help. */
@Composable
fun SaathiMark(size: Dp = 24.dp) {
    Box(Modifier.size(size).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(size * 0.3f)).padding(size * 0.2f)) {
        Canvas(Modifier.size(size * 0.6f)) { ajrakhStar(Color.White) }
    }
}

/** A number plate drawn like the real thing: white, black border, spaced capitals. */
@Composable
fun PlateText(plate: String, ring: Color? = null, big: Boolean = false, modifier: Modifier = Modifier) {
    val pretty = prettyPlate(plate)
    val inner = Modifier
        .background(Color(0xFFFAFAF7), RoundedCornerShape(6.dp))
        .then(Modifier.padding(1.5.dp))
    // Big plates are compared in a column, so every big plate reserves the ring's room and their edges line up.
    val ringSpace = ring != null || big
    Box(
        modifier
            .then(if (ringSpace) Modifier.background(ring ?: Color.Transparent, RoundedCornerShape(10.dp)).padding(2.5.dp)
                .background(if (ring != null) MaterialTheme.colorScheme.background else Color.Transparent, RoundedCornerShape(8.dp)).padding(2.dp) else Modifier)
            .background(Color(0xFF121212), RoundedCornerShape(6.dp)).padding(1.5.dp)
    ) {
        Text(
            pretty, inner.padding(horizontal = if (big) 12.dp else 8.dp, vertical = if (big) 4.dp else 1.dp),
            color = Color(0xFF121212), fontFamily = Figtree, fontWeight = FontWeight.Bold,
            fontSize = if (big) 22.sp else 13.sp, letterSpacing = 0.06.em,
        )
    }
}

/** KA05AB1234 -> KA 05 AB 1234; anything else is left as typed. */
fun prettyPlate(p: String): String {
    val m = Regex("""^([A-Z]{2})\s*(\d{1,2})\s*([A-Z]{0,3})\s*(\d{1,4})$""").find(p.uppercase().replace(" ", ""))
    return m?.destructured?.let { (a, b, c, d) -> listOf(a, b, c, d).filter { it.isNotEmpty() }.joinToString(" ") } ?: p
}
