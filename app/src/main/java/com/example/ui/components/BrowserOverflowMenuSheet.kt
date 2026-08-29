package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisMenuSurface
import com.example.ui.theme.AegisSeparator
import com.example.ui.theme.AegisTextPrimary
import com.example.ui.theme.AegisTextSecondary

@Composable
fun BrowserOverflowMenuSheet(
    isSafeModeEnabled: Boolean = true,
    isDesktopMode: Boolean = false,
    onDismiss: () -> Unit,
    onNewTab: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenDownloads: () -> Unit,
    onDownloadDetectedMedia: () -> Unit,
    onOpenDownloadQueue: () -> Unit,
    onToggleSafeMode: (Boolean) -> Unit,
    onOpenSitePermissions: () -> Unit,
    onOpenPrivacySettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    var safeModeState by remember { mutableStateOf(isSafeModeEnabled) }
    val scrollState = rememberScrollState()

    // Semi-transparent scrim background dismissible on tap
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        // Floating bottom-attached menu surface (aligned to bottom right / bottom toolbar)
        Surface(
            color = AegisMenuSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            tonalElevation = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(0.92f)
                .padding(end = 8.dp, bottom = 68.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Prevent click through */ }
                )
                .testTag("aegis_overflow_menu_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .verticalScroll(scrollState)
            ) {
                // Centered drag handle bar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF5F6368))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // SECTION 1: TAB & CORE ACTIONS
                AegisMenuRow(
                    title = "Nouvel onglet",
                    drawableRes = R.drawable.ic_shield_check,
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter",
                            tint = AegisTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        onDismiss()
                        onNewTab(false)
                    },
                    testTag = "menu_new_tab"
                )

                AegisMenuRow(
                    title = "Nouvel onglet privé",
                    drawableRes = R.drawable.ic_incognito_mask,
                    onClick = {
                        onDismiss()
                        onNewTab(true)
                    },
                    testTag = "menu_new_private_tab"
                )

                AegisMenuRow(
                    title = "Historique",
                    imageVector = Icons.Default.History,
                    onClick = {
                        onDismiss()
                        onOpenHistory()
                    },
                    testTag = "menu_history"
                )

                AegisMenuRow(
                    title = "Favoris",
                    imageVector = Icons.Default.BookmarkBorder,
                    onClick = {
                        onDismiss()
                        onOpenBookmarks()
                    },
                    testTag = "menu_bookmarks"
                )

                AegisMenuRow(
                    title = "Téléchargements",
                    imageVector = Icons.Default.Download,
                    onClick = {
                        onDismiss()
                        onOpenDownloads()
                    },
                    testTag = "menu_downloads"
                )

                // SECTION DIVIDER
                HorizontalDivider(
                    color = AegisSeparator,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // SECTION 2: MEDIA DOWNLOADER & SAFE MODE
                AegisMenuRow(
                    title = "Télécharger média détecté",
                    drawableRes = R.drawable.ic_media_detect,
                    onClick = {
                        onDismiss()
                        onDownloadDetectedMedia()
                    },
                    testTag = "menu_download_detected_media"
                )

                AegisMenuRow(
                    title = "File de téléchargements",
                    drawableRes = R.drawable.ic_download_queue,
                    onClick = {
                        onDismiss()
                        onOpenDownloadQueue()
                    },
                    testTag = "menu_download_queue"
                )

                AegisMenuRow(
                    title = "Safe Mode",
                    imageVector = Icons.Default.Shield,
                    trailingContent = {
                        Switch(
                            checked = safeModeState,
                            onCheckedChange = {
                                safeModeState = it
                                onToggleSafeMode(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF101318),
                                checkedTrackColor = AegisCyanPrimary,
                                uncheckedThumbColor = AegisTextSecondary,
                                uncheckedTrackColor = Color(0xFF2A2F37)
                            ),
                            modifier = Modifier.testTag("menu_safe_mode_switch")
                        )
                    },
                    onClick = {
                        safeModeState = !safeModeState
                        onToggleSafeMode(safeModeState)
                    },
                    testTag = "menu_safe_mode"
                )

                // SECTION DIVIDER
                HorizontalDivider(
                    color = AegisSeparator,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // SECTION 3: PRIVACY, PERMISSIONS & SETTINGS
                AegisMenuRow(
                    title = "Autorisations du site",
                    drawableRes = R.drawable.ic_shield_permissions,
                    onClick = {
                        onDismiss()
                        onOpenSitePermissions()
                    },
                    testTag = "menu_site_permissions"
                )

                AegisMenuRow(
                    title = "Protection et confidentialité",
                    drawableRes = R.drawable.ic_shield_check,
                    onClick = {
                        onDismiss()
                        onOpenPrivacySettings()
                    },
                    testTag = "menu_privacy_protection"
                )

                AegisMenuRow(
                    title = "Paramètres",
                    imageVector = Icons.Default.Settings,
                    onClick = {
                        onDismiss()
                        onOpenSettings()
                    },
                    testTag = "menu_settings"
                )

                AegisMenuRow(
                    title = "Aide et commentaires",
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    onClick = {
                        onDismiss()
                        onOpenHelp()
                    },
                    testTag = "menu_help_feedback"
                )
            }
        }
    }
}

@Composable
private fun AegisMenuRow(
    title: String,
    imageVector: ImageVector? = null,
    drawableRes: Int? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = AegisCyanPrimary),
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (drawableRes != null) {
                Icon(
                    painter = painterResource(id = drawableRes),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(22.dp)
                )
            } else if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = AegisTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Title Label
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = AegisTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        // Trailing Content (Plus icon, Switch toggle, etc.)
        if (trailingContent != null) {
            trailingContent()
        }
    }
}
