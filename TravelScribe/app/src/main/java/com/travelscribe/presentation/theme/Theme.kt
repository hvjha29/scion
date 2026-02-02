package com.travelscribe.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * TravelScribe brand colors matching the design system.
 * Warm orange primary with cream/paper backgrounds.
 */
object TravelScribeColors {
    // Primary brand color - warm orange
    val Primary = Color(0xFFEE652B)
    val PrimaryDark = Color(0xFFD6521B)
    
    // Light theme
    val BackgroundLight = Color(0xFFFCF9F8)
    val CardLight = Color(0xFFF4EFE9)
    val PaperLight = Color(0xFFFFFFFF)
    val TextMain = Color(0xFF1B120D)
    val TextMuted = Color(0xFF9A634C)
    
    // Dark theme
    val BackgroundDark = Color(0xFF221510)
    val CardDark = Color(0xFF362924)
    val PaperDark = Color(0xFF2D1B15)
    val TextMainDark = Color(0xFFFCF9F8)
    val TextMutedDark = Color(0xFFB8977F)
}

// Light theme colors - Warm paper/cream aesthetic
private val LightColorScheme = lightColorScheme(
    primary = TravelScribeColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBD0),
    onPrimaryContainer = Color(0xFF3A0B00),
    secondary = TravelScribeColors.TextMuted,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF4EFE9),
    onSecondaryContainer = TravelScribeColors.TextMain,
    tertiary = Color(0xFF26A69A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00201E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = TravelScribeColors.BackgroundLight,
    onBackground = TravelScribeColors.TextMain,
    surface = TravelScribeColors.PaperLight,
    onSurface = TravelScribeColors.TextMain,
    surfaceVariant = TravelScribeColors.CardLight,
    onSurfaceVariant = TravelScribeColors.TextMuted,
    outline = Color(0xFFD4C4BC),
    outlineVariant = Color(0xFFE8DDD6)
)

// Dark theme colors - Warm dark brown aesthetic
private val DarkColorScheme = darkColorScheme(
    primary = TravelScribeColors.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF862200),
    onPrimaryContainer = Color(0xFFFFDBD0),
    secondary = TravelScribeColors.TextMutedDark,
    onSecondary = Color(0xFF3A0B00),
    secondaryContainer = TravelScribeColors.CardDark,
    onSecondaryContainer = TravelScribeColors.TextMainDark,
    tertiary = Color(0xFF80CBC4),
    onTertiary = Color(0xFF003733),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFFB2DFDB),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = TravelScribeColors.BackgroundDark,
    onBackground = TravelScribeColors.TextMainDark,
    surface = TravelScribeColors.PaperDark,
    onSurface = TravelScribeColors.TextMainDark,
    surfaceVariant = TravelScribeColors.CardDark,
    onSurfaceVariant = TravelScribeColors.TextMutedDark,
    outline = Color(0xFF5C4A42),
    outlineVariant = Color(0xFF3D2E28)
)

/**
 * Extended color scheme for custom TravelScribe colors not in Material3.
 */
data class ExtendedColors(
    val card: Color,
    val textMuted: Color,
    val paperTexture: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        card = TravelScribeColors.CardLight,
        textMuted = TravelScribeColors.TextMuted,
        paperTexture = TravelScribeColors.PaperLight
    )
}

/**
 * TravelScribe application theme.
 * Warm paper/cream aesthetic with orange accent.
 * Disables dynamic colors to maintain brand consistency.
 */
@Composable
fun TravelScribeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to keep brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            card = TravelScribeColors.CardDark,
            textMuted = TravelScribeColors.TextMutedDark,
            paperTexture = TravelScribeColors.PaperDark
        )
    } else {
        ExtendedColors(
            card = TravelScribeColors.CardLight,
            textMuted = TravelScribeColors.TextMuted,
            paperTexture = TravelScribeColors.PaperLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Access extended colors from the current theme.
 */
object TravelScribeTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
