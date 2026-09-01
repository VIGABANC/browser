package com.example.ui.utils

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object AccessibilityUtils {

    /**
     * Check if the user has enabled "Remove Animations" or reduced motion in system settings.
     * Checks if animator/transition/window duration scales are set to 0.
     */
    fun isReducedMotionEnabled(context: Context): Boolean {
        // Check system animator duration scale (0f indicates animations disabled)
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

        return animatorDurationScale == 0f || 
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


