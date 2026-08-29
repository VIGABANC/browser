package com.example.ui.utils

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object AccessibilityUtils {

    /**
     * Check if the user has enabled "Remove Animations" in accessibility settings.
     * Also checks if animator duration scale is set to 0 (developer option or accessibility).
     */
    fun isReducedMotionEnabled(context: Context): Boolean {
        // Check accessibility service (TalkBack, etc. often imply reduced motion preference)
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        val isAccessibilityEnabled = accessibilityManager?.isEnabled == true

        // Check system animator duration scale
        val animatorDurationScale = try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
        } catch (e: Exception) { 1.0f }

        // Check transition animation scale
        val transitionScale = try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                1.0f
            )
        } catch (e: Exception) { 1.0f }

        // Check window animation scale
        val windowScale = try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.WINDOW_ANIMATION_SCALE,
                1.0f
            )
        } catch (e: Exception) { 1.0f }

        return isAccessibilityEnabled || 
               animatorDurationScale == 0f || 
               transitionScale == 0f ||
               windowScale == 0f
    }
}

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        AccessibilityUtils.isReducedMotionEnabled(context)
    }
}


