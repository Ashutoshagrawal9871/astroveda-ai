package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CosmicDarkColorScheme = darkColorScheme(
    primary = MysticGold,
    onPrimary = DeepCosmic,
    primaryContainer = DarkCosmicCard,
    onPrimaryContainer = MysticGoldBright,
    secondary = SacredSaffron,
    onSecondary = DeepCosmic,
    secondaryContainer = DarkCosmicSurfaceVariant,
    onSecondaryContainer = MysticGold,
    tertiary = CelestialViolet,
    onTertiary = DeepCosmic,
    background = DeepCosmic,
    onBackground = TextGold,
    surface = DarkCosmicSurface,
    onSurface = TextGold,
    surfaceVariant = DarkCosmicSurfaceVariant,
    onSurfaceVariant = TextSilver,
    outline = BorderSubtle,
    outlineVariant = BorderGold
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = CosmicDarkColorScheme,
        typography = Typography,
        content = content
    )
}
