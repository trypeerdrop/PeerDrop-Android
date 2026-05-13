package to.foss.peerdrop.android.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Semantic color tokens ─────────────────────────────────────────────────────

data class PeerDropColorScheme(
    val background:   Color,
    val surface:      Color,
    val surfaceVar:   Color,
    val primary:      Color,
    val primaryDim:   Color,
    val onBackground: Color,
    val subtle:       Color,
    val verySubtle:   Color,
    val online:       Color,
    val warning:      Color,
    val danger:       Color,
    val sendBlue:     Color,
    val border:       Color,
)

val DarkColorScheme = PeerDropColorScheme(
    background   = Color(0xFF0F0F14),
    surface      = Color(0xFF13131A),
    surfaceVar   = Color(0xFF1E1E2E),
    primary      = Color(0xFF7C3AED),
    primaryDim   = Color(0xFF1A0A3E),
    onBackground = Color(0xFFCDD6F4),
    subtle       = Color(0xFF555570),
    verySubtle   = Color(0xFF333350),
    online       = Color(0xFF3ECF6E),
    warning      = Color(0xFFF59E0B),
    danger       = Color(0xFFF38BA8),
    sendBlue     = Color(0xFF89B4FA),
    border       = Color(0xFF2E2E4E),
)

val LightColorScheme = PeerDropColorScheme(
    background   = Color(0xFFF5F5F8),
    surface      = Color(0xFFFFFFFF),
    surfaceVar   = Color(0xFFEEEEF5),
    primary      = Color(0xFF6D28D9),
    primaryDim   = Color(0xFFEDE9FE),
    onBackground = Color(0xFF0F0F14),
    subtle       = Color(0xFF6B7280),
    verySubtle   = Color(0xFF9CA3AF),
    online       = Color(0xFF16A34A),
    warning      = Color(0xFFD97706),
    danger       = Color(0xFFDC2626),
    sendBlue     = Color(0xFF2563EB),
    border       = Color(0xFFE2E2EF),
)

// ── CompositionLocal ──────────────────────────────────────────────────────────

val LocalPeerDropColors = staticCompositionLocalOf { DarkColorScheme }

// Composable accessor — use in @Composable functions
val peerDropColors: PeerDropColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalPeerDropColors.current

// ── Material3 schemes ─────────────────────────────────────────────────────────

private val M3Dark = darkColorScheme(
    primary        = DarkColorScheme.primary,
    secondary      = Color(0xFF6D28D9),
    background     = DarkColorScheme.background,
    surface        = DarkColorScheme.surface,
    surfaceVariant = DarkColorScheme.surfaceVar,
    onBackground   = DarkColorScheme.onBackground,
    onSurface      = DarkColorScheme.onBackground,
    onPrimary      = Color.White,
    outline        = DarkColorScheme.border,
)

private val M3Light = lightColorScheme(
    primary        = LightColorScheme.primary,
    secondary      = Color(0xFF7C3AED),
    background     = LightColorScheme.background,
    surface        = LightColorScheme.surface,
    surfaceVariant = LightColorScheme.surfaceVar,
    onBackground   = LightColorScheme.onBackground,
    onSurface      = LightColorScheme.onBackground,
    onPrimary      = Color.White,
    outline        = LightColorScheme.border,
)

// ── Theme ─────────────────────────────────────────────────────────────────────

@Composable
fun PeerDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val colors   = if (darkTheme) DarkColorScheme else LightColorScheme
    val m3Colors = if (darkTheme) M3Dark          else M3Light

    CompositionLocalProvider(LocalPeerDropColors provides colors) {
        MaterialTheme(
            colorScheme = m3Colors,
            content     = content
        )
    }
}