package com.example.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

/**
 * User selectable theme mode preference.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    @Composable
    fun isDark(): Boolean = when (this) {
        SYSTEM -> isSystemInDarkTheme()
        LIGHT -> false
        DARK -> true
    }
}

/**
 * Safe context unwrapping helper to find the hosting Activity.
 */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

// ============================================================================
// MATERIAL 3 COLOR SCHEMES
// ============================================================================

private val AegisDarkColorScheme = darkColorScheme(
    primary = AegisCyanPrimary,
    onPrimary = AegisDarkSurfaceLowest,
    primaryContainer = AegisCyanContainerDark,
    onPrimaryContainer = AegisCyanOnContainerDark,

    secondary = AegisAmberSecondary,
    onSecondary = AegisDarkSurfaceLowest,
    secondaryContainer = AegisAmberContainerDark,
    onSecondaryContainer = AegisAmberOnContainerDark,

    tertiary = AegisEmeraldSafe,
    onTertiary = AegisDarkSurfaceLowest,
    tertiaryContainer = AegisEmeraldContainerDark,
    onTertiaryContainer = AegisEmeraldLight,

    background = AegisMainBackground,
    onBackground = AegisTextPrimary,

    surface = AegisDarkSurface,
    onSurface = AegisTextPrimary,
    surfaceVariant = AegisDarkSurfaceVariant,
    onSurfaceVariant = AegisTextSecondary,

    surfaceContainer = AegisOmniboxBg,
    surfaceContainerLow = AegisBrowserChrome,
    surfaceContainerHigh = AegisDarkSurfaceElevated,
    surfaceContainerHighest = AegisMenuSurfaceElevated,

    outline = AegisOutline,
    outlineVariant = AegisOutlineVariant,

    error = AegisRedDanger,
    onError = Color.White,
    errorContainer = AegisRedContainerDark,
    onErrorContainer = AegisRedLight
)

private val AegisLightColorScheme = lightColorScheme(
    primary = AegisLightPrimary,
    onPrimary = Color.White,
    primaryContainer = AegisCyanContainerLight,
    onPrimaryContainer = AegisCyanOnContainerLight,

    secondary = AegisLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = AegisAmberContainerLight,
    onSecondaryContainer = AegisAmberOnContainerLight,

    tertiary = AegisEmeraldDark,
    onTertiary = Color.White,
    tertiaryContainer = AegisEmeraldContainerLight,
    onTertiaryContainer = AegisEmeraldDark,

    background = AegisLightBackground,
    onBackground = AegisLightTextPrimary,

    surface = AegisLightSurface,
    onSurface = AegisLightTextPrimary,
    surfaceVariant = AegisLightSurfaceVariant,
    onSurfaceVariant = AegisLightTextSecondary,

    surfaceContainer = AegisLightOmniboxBg,
    surfaceContainerLow = AegisLightBackground,
    surfaceContainerHigh = AegisLightSurfaceElevated,
    surfaceContainerHighest = AegisLightSurfaceVariant,

    outline = AegisLightBorder,
    outlineVariant = AegisLightOutlineVariant,

    error = AegisRedDanger,
    onError = Color.White,
    errorContainer = AegisRedContainerLight,
    onErrorContainer = AegisRedDark
)

// ============================================================================
// PRIMARY THEME ENTRY POINT
// ============================================================================

/**
 * Primary theme entry point for Aegis Browser.
 *
 * @param darkTheme Whether to render in dark theme. Defaults to system setting.
 * @param dynamicColor Whether to use Android 12+ dynamic color. Defaults to false to preserve Aegis identity.
 * @param content Composable content tree.
 */
@Composable
fun AegisBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AegisDarkColorScheme
        else -> AegisLightColorScheme
    }

    val tokens = if (darkTheme) DarkAegisTokens else LightAegisTokens

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            val window = activity?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalAegisTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AegisShapes,
            content = content
        )
    }
}

/**
 * Overload accepting a [ThemeMode] enum.
 */
@Composable
fun AegisBrowserTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AegisBrowserTheme(
        darkTheme = themeMode.isDark(),
        dynamicColor = dynamicColor,
        content = content
    )
}

/**
 * Backward compatibility alias forwarding to [AegisBrowserTheme].
 */
@Deprecated(
    message = "Use AegisBrowserTheme instead",
    replaceWith = ReplaceWith("AegisBrowserTheme(darkTheme, dynamicColor, content)")
)
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AegisBrowserTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

// ============================================================================
// COMPOSE PREVIEWS
// ============================================================================

@Preview(name = "Aegis Dark Theme", showBackground = true)
@Composable
private fun AegisDarkThemePreview() {
    AegisBrowserTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aegis Privacy Browser",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dark theme palette with high-contrast accessibility",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Action Primary")
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("Safe Mode")
                    }
                }
            }
        }
    }
}

@Preview(name = "Aegis Light Theme", showBackground = true)
@Composable
private fun AegisLightThemePreview() {
    AegisBrowserTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Aegis Privacy Browser",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Light theme palette with crisp borders and clean contrast",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Action Primary")
                    }
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Text("Safe Mode")
                    }
                }
            }
        }
    }
}
