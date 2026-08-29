package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.ShieldStats
import com.example.ui.components.BrowserHomeDashboard
import com.example.ui.theme.AegisBrowserTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun aegis_home_screenshot() {
    composeTestRule.setContent {
      AegisBrowserTheme {
        BrowserHomeDashboard(
          isIncognito = false,
          shieldStats = ShieldStats(adsBlockedTotal = 142, trackersBlockedTotal = 89, bandwidthSavedMb = 12.4f),
          bookmarks = emptyList(),
          onNavigate = {},
          onSimulateMediaStream = { _, _, _ -> },
          onOpenAiAssistant = {},
          onOpenShields = {},
          onOpenDownloads = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
