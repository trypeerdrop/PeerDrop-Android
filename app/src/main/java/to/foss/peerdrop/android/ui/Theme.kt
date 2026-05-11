package to.foss.peerdrop.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object PeerDropColors {
    val Background    = Color(0xFF0F0F14)
    val Surface       = Color(0xFF13131A)
    val SurfaceVar    = Color(0xFF1E1E2E)
    val Primary       = Color(0xFF7C3AED)
    val PrimaryDim    = Color(0xFF1A0A3E)
    val OnBackground  = Color(0xFFCDD6F4)
    val Subtle        = Color(0xFF555570)
    val VerySubtle    = Color(0xFF333350)
    val Online        = Color(0xFF3ECF6E)
    val Warning       = Color(0xFFF59E0B)
    val Danger        = Color(0xFFF38BA8)
    val SendBlue      = Color(0xFF89B4FA)
    val Border        = Color(0xFF2E2E4E)
}

private val ColorScheme = darkColorScheme(
    primary        = PeerDropColors.Primary,
    secondary      = Color(0xFF6D28D9),
    background     = PeerDropColors.Background,
    surface        = PeerDropColors.Surface,
    surfaceVariant = PeerDropColors.SurfaceVar,
    onBackground   = PeerDropColors.OnBackground,
    onSurface      = PeerDropColors.OnBackground,
    onPrimary      = Color.White,
    outline        = PeerDropColors.Border,
)

@Composable
fun PeerDropTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        content     = content
    )
}
