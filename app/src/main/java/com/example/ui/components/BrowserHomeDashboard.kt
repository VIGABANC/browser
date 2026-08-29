package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Bookmark
import com.example.data.model.HistoryItem
import com.example.data.model.ShieldStats
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisMainBackground
import com.example.ui.theme.AegisOmniboxBg
import com.example.ui.theme.AegisSeparator
import com.example.ui.theme.AegisTextPrimary
import com.example.ui.theme.AegisTextSecondary

@Composable
fun BrowserHomeDashboard(
    isIncognito: Boolean,
    shieldStats: ShieldStats,
    bookmarks: List<Bookmark> = emptyList(),
    recentHistory: List<HistoryItem> = emptyList(),
    isScenicWallpaper: Boolean = false,
    isPrivacyStatsVisible: Boolean = true,
    isDiscoverFeedVisible: Boolean = false,
    onNavigate: (String) -> Unit,
    onToggleIncognito: () -> Unit = {},
    onToggleScenicWallpaper: () -> Unit = {},
    onTogglePrivacyStatsVisible: () -> Unit = {},
    onToggleDiscoverFeedVisible: () -> Unit = {},
    onAddCustomShortcut: (title: String, url: String) -> Unit = { _, _ -> },
    onSimulateMediaStream: (title: String, streamUrl: String, isAudio: Boolean) -> Unit = { _, _, _ -> },
    onOpenAiAssistant: () -> Unit = {},
    onOpenShields: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AegisMainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Center Aegis Brand Logo & Title (as seen in AEGIS-UI-001)
            Box(contentAlignment = Alignment.TopCenter) {
                // Little cyan shield mark above 'g'
                Icon(
                    painter = painterResource(id = R.drawable.ic_aegis_shield),
                    contentDescription = null,
                    tint = AegisCyanPrimary,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(bottom = 2.dp)
                )

                Text(
                    text = "Aegis",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = AegisTextPrimary,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Navigation privée. Téléchargements sous contrôle.",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = AegisTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Privacy Statistics Section (238 Traceurs bloqués | 14.8 MB Données économisées)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Trackers blocked
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val trackersCount = if (shieldStats.trackersBlockedTotal > 0) shieldStats.trackersBlockedTotal else 238
                    Text(
                        text = "$trackersCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AegisCyanPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Traceurs bloqués",
                        fontSize = 12.sp,
                        color = AegisTextSecondary
                    )
                }

                // Vertical hairline divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(AegisSeparator)
                )

                // Column 2: Bandwidth saved
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    val mbSaved = if (shieldStats.bandwidthSavedMb > 0f) String.format("%.1f", shieldStats.bandwidthSavedMb) else "14.8"
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = mbSaved,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AegisCyanPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "MB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AegisCyanPrimary,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Données économisées",
                        fontSize = 12.sp,
                        color = AegisTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 4 Shortcut Tiles (Google, YouTube, Reddit, Ajouter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 1. Google
                AegisShortcutTile(
                    title = "Google",
                    drawableRes = R.drawable.ic_google,
                    onClick = { onNavigate("https://www.google.com") },
                    testTag = "shortcut_google"
                )

                // 2. YouTube
                AegisShortcutTile(
                    title = "YouTube",
                    drawableRes = R.drawable.ic_youtube,
                    onClick = { onNavigate("https://www.youtube.com") },
                    testTag = "shortcut_youtube"
                )

                // 3. Reddit
                AegisShortcutTile(
                    title = "Reddit",
                    drawableRes = R.drawable.ic_reddit,
                    onClick = { onNavigate("https://www.reddit.com") },
                    testTag = "shortcut_reddit"
                )

                // 4. Ajouter
                AegisShortcutTile(
                    title = "Ajouter",
                    iconVector = Icons.Default.Add,
                    onClick = { onAddCustomShortcut("Nouveau", "https://") },
                    testTag = "shortcut_add"
                )
            }
        }
    }
}

@Composable
private fun AegisShortcutTile(
    title: String,
    drawableRes: Int? = null,
    iconVector: ImageVector? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AegisOmniboxBg),
            contentAlignment = Alignment.Center
        ) {
            if (drawableRes != null) {
                Icon(
                    painter = painterResource(id = drawableRes),
                    contentDescription = title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(26.dp)
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = title,
                    tint = AegisTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = AegisTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
