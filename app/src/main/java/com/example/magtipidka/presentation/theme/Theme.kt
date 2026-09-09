package com.example.magtipidka.presentation.theme

import android.graphics.Color.parseColor
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.magtipidka.domain.model.ThemeColorPalette
import com.example.magtipidka.domain.model.ThemeMode

@Composable
fun MagTipidKaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themeColorPalette: ThemeColorPalette = ThemeColorPalette.EMERALD,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val basePrimaryColor = try {
        Color(parseColor(themeColorPalette.hexColor))
    } catch (e: Exception) {
        EmeraldPrimary
    }

    val darkPrimaryColor = basePrimaryColor.lightenForDarkMode(factor = 0.35f)

    val lightColorScheme = lightColorScheme(
        primary = basePrimaryColor,
        onPrimary = Color.White,
        primaryContainer = basePrimaryColor.copy(alpha = 0.10f),
        onPrimaryContainer = basePrimaryColor,
        secondary = SecondaryTeal,
        onSecondary = Color.White,
        secondaryContainer = SecondaryTealContainer,
        onSecondaryContainer = OnSecondaryTealContainer,
        background = BackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceContainerLight,
        onSurfaceVariant = OnSurfaceMutedLight,
        outline = OutlineLight
    )

    val darkColorScheme = darkColorScheme(
        primary = darkPrimaryColor,
        onPrimary = OnSurfaceLight,
        primaryContainer = darkPrimaryColor.copy(alpha = 0.18f),
        onPrimaryContainer = darkPrimaryColor,
        secondary = SecondaryTealContainer,
        onSecondary = OnSecondaryTealContainer,
        secondaryContainer = SecondaryTeal,
        onSecondaryContainer = SecondaryTealContainer,
        background = BackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceContainerDark,
        onSurfaceVariant = OnSurfaceMutedDark,
        outline = OutlineDark
    )

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

private fun Color.lightenForDarkMode(factor: Float = 0.35f): Color {
    return Color(
        red = red + (1f - red) * factor,
        green = green + (1f - green) * factor,
        blue = blue + (1f - blue) * factor,
        alpha = alpha
    )
}
