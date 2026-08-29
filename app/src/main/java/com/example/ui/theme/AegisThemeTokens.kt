package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Aegis Browser Semantic Design Tokens
 *
 * Dedicated tokens for browser chrome, overflow menu, shields/privacy,
 * download workflows, and incognito states.
 */
@Immutable
data class AegisThemeTokens(
    // Browser Chrome
    val browserBackground: Color,
    val browserChrome: Color,
    val omniboxBackground: Color,
    val divider: Color,
    val iconPrimary: Color,
    val iconSecondary: Color,
    val iconDisabled: Color,

    // Overflow Menu Surface
    val menuSurface: Color,
    val menuSurfaceElevated: Color,
    val menuTextPrimary: Color,
    val menuTextSecondary: Color,
    val menuDivider: Color,

    // Privacy & Protection
    val shieldActive: Color,
    val shieldInactive: Color,
    val safeModeActive: Color,
    val warning: Color,
    val danger: Color,

    // Downloads
    val downloadActive: Color,
    val downloadPaused: Color,
    val downloadCompleted: Color,
    val downloadFailed: Color,

    // Incognito / Private Mode
    val privateBackground: Color,
    val privateChrome: Color,
    val privateBorder: Color,
    val privateAccent: Color,
    val privateContainer: Color
)

val DarkAegisTokens = AegisThemeTokens(
    browserBackground = AegisMainBackground,
    browserChrome = AegisBrowserChrome,
    omniboxBackground = AegisOmniboxBg,
    divider = AegisSeparator,
    iconPrimary = AegisTextPrimary,
    iconSecondary = AegisTextSecondary,
    iconDisabled = AegisTextMuted,

    menuSurface = AegisMenuSurface,
    menuSurfaceElevated = AegisMenuSurfaceElevated,
    menuTextPrimary = AegisTextPrimary,
    menuTextSecondary = AegisTextSecondary,
    menuDivider = AegisOutline,

    shieldActive = AegisCyanPrimary,
    shieldInactive = AegisTextSecondary,
    safeModeActive = AegisEmeraldSafe,
    warning = AegisAmberSecondary,
    danger = AegisRedDanger,

    downloadActive = AegisCyanPrimary,
    downloadPaused = AegisAmberSecondary,
    downloadCompleted = AegisEmeraldSafe,
    downloadFailed = AegisRedDanger,

    privateBackground = AegisIndigoIncognitoDark,
    privateChrome = Color(0xFF1B1833),
    privateBorder = AegisIndigoIncognitoBorderDark,
    privateAccent = AegisIndigoIncognito,
    privateContainer = AegisIndigoIncognitoContainerDark
)

val LightAegisTokens = AegisThemeTokens(
    browserBackground = AegisLightBackground,
    browserChrome = AegisLightSurface,
    omniboxBackground = AegisLightOmniboxBg,
    divider = AegisLightBorder,
    iconPrimary = AegisLightTextPrimary,
    iconSecondary = AegisLightTextSecondary,
    iconDisabled = AegisLightTextMuted,

    menuSurface = AegisLightMenuSurface,
    menuSurfaceElevated = AegisLightSurfaceElevated,
    menuTextPrimary = AegisLightMenuText,
    menuTextSecondary = AegisLightMenuSecondary,
    menuDivider = AegisLightMenuDivider,

    shieldActive = AegisLightPrimary,
    shieldInactive = AegisLightTextSecondary,
    safeModeActive = AegisEmeraldDark,
    warning = AegisLightSecondary,
    danger = AegisRedDanger,

    downloadActive = AegisLightPrimary,
    downloadPaused = AegisLightSecondary,
    downloadCompleted = AegisEmeraldDark,
    downloadFailed = AegisRedDanger,

    privateBackground = Color(0xFFFAF5FF),
    privateChrome = AegisIndigoIncognitoLight,
    privateBorder = AegisIndigoIncognitoBorderLight,
    privateAccent = AegisIndigoIncognito,
    privateContainer = AegisIndigoIncognitoContainerLight
)

val LocalAegisTokens = staticCompositionLocalOf { DarkAegisTokens }

/**
 * Accessor for semantic browser tokens
 */
object AegisTheme {
    val tokens: AegisThemeTokens
        @Composable
        get() = LocalAegisTokens.current
}
