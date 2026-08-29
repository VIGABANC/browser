package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = AegisCyanPrimary,
    onPrimary = Color(0xFF002029),
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = AegisCyanLight,
    secondary = AegisAmberSecondary,
    onSecondary = Color(0xFF331F00),
    secondaryContainer = Color(0xFF5E3A00),
    onSecondaryContainer = AegisAmberLight,
    tertiary = AegisEmeraldSafe,
    onTertiary = Color(0xFF003822),
    background = AegisDarkBackground,
    onBackground = AegisTextPrimary,
    surface = AegisDarkSurface,
    onSurface = AegisTextPrimary,
    surfaceVariant = AegisDarkSurfaceVariant,
    onSurfaceVariant = AegisTextSecondary,
    outline = AegisDarkBorder,
    error = AegisRedDanger,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AegisLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = AegisLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFDE68A),
    onSecondaryContainer = Color(0xFFB45309),
    tertiary = AegisEmeraldDark,
    onTertiary = Color.White,
    background = AegisLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = AegisLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = AegisLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = AegisRedDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to deep privacy dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            var context = view.context
            while (context is android.content.ContextWrapper) {
                if (context is android.app.Activity) break
                context = context.baseContext
            }
            val window = (context as? android.app.Activity)?.window
            if (window != null) {
                androidx.core.view.WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun AegisBrowserTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

