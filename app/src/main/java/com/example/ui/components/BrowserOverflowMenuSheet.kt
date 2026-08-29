package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisIndigoIncognito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserOverflowMenuSheet(
    isDesktopMode: Boolean,
    onDismiss: () -> Unit,
    onGoForward: () -> Unit,
    onReload: () -> Unit,
    onAddBookmark: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenShields: () -> Unit,
    onNewTab: (Boolean) -> Unit,
    onOpenTabs: () -> Unit,
    onOpenBookmarksHistory: () -> Unit,
    onOpenClearData: () -> Unit,
    onOpenAutoFill: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenReaderMode: () -> Unit,
    onToggleDesktopMode: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(scrollState)
        ) {
            // Top Quick Action Row (Circles - as seen in Screenshots 1, 2 & 3)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Forward
                    QuickActionCircle(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Suivant",
                        onClick = {
                            onDismiss()
                            onGoForward()
                        }
                    )

                    // Bookmark
                    QuickActionCircle(
                        icon = Icons.Default.Star,
                        contentDescription = "Favoris",
                        iconTint = AegisAmberSecondary,
                        onClick = {
                            onDismiss()
                            onAddBookmark()
                        }
                    )

                    // Downloads
                    QuickActionCircle(
                        icon = Icons.Default.Download,
                        contentDescription = "Téléchargements",
                        iconTint = AegisCyanPrimary,
                        onClick = {
                            onDismiss()
                            onOpenDownloads()
                        }
                    )

                    // Page Info & Shields
                    QuickActionCircle(
                        icon = Icons.Default.Shield,
                        contentDescription = "Boucliers",
                        iconTint = AegisCyanPrimary,
                        onClick = {
                            onDismiss()
                            onOpenShields()
                        }
                    )

                    // Reload
                    QuickActionCircle(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Actualiser",
                        onClick = {
                            onDismiss()
                            onReload()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Navigation Actions (Exact matching list from Screenshots 1, 2, 3)
            OverflowMenuItem(
                icon = Icons.Default.Add,
                title = "Nouvel onglet",
                onClick = {
                    onDismiss()
                    onNewTab(false)
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Security,
                title = "Nouvel onglet de navigation privée",
                iconTint = AegisIndigoIncognito,
                onClick = {
                    onDismiss()
                    onNewTab(true)
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Grid3x3,
                title = "Gestionnaire d'onglets",
                onClick = {
                    onDismiss()
                    onOpenTabs()
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            OverflowMenuItem(
                icon = Icons.Default.History,
                title = "Historique",
                onClick = {
                    onDismiss()
                    onOpenBookmarksHistory()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.DeleteSweep,
                title = "Supprimer les données de navigation...",
                iconTint = Color(0xFFFF6B6B),
                onClick = {
                    onDismiss()
                    onOpenClearData()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Download,
                title = "Téléchargements",
                iconTint = AegisCyanPrimary,
                onClick = {
                    onDismiss()
                    onOpenDownloads()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Key,
                title = "Portefeuille & Mots de passe",
                iconTint = AegisAmberSecondary,
                onClick = {
                    onDismiss()
                    onOpenAutoFill()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Bookmark,
                title = "Favoris",
                iconTint = AegisAmberSecondary,
                onClick = {
                    onDismiss()
                    onOpenBookmarksHistory()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.AutoAwesome,
                title = "Assistant IA Gemini (Mode IA)",
                iconTint = AegisCyanPrimary,
                badge = "PRO",
                onClick = {
                    onDismiss()
                    onOpenAiAssistant()
                }
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            OverflowMenuItem(
                icon = Icons.Default.FindInPage,
                title = "Trouver sur la page",
                onClick = {
                    onDismiss()
                    onOpenFindInPage()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.MenuBook,
                title = "Mode Lecture",
                onClick = {
                    onDismiss()
                    onOpenReaderMode()
                }
            )

            // Desktop Site Checkbox Item (from Screenshot 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onToggleDesktopMode()
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isDesktopMode) Icons.Default.DesktopWindows else Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Version pour ordinateur",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = isDesktopMode,
                    onCheckedChange = { onToggleDesktopMode() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AegisCyanPrimary,
                        checkmarkColor = Color.Black
                    )
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            OverflowMenuItem(
                icon = Icons.Default.Shield,
                title = "Boucliers Aegis & Confidentialité",
                iconTint = AegisCyanPrimary,
                onClick = {
                    onDismiss()
                    onOpenShields()
                }
            )

            OverflowMenuItem(
                icon = Icons.Default.Settings,
                title = "Paramètres",
                onClick = {
                    onDismiss()
                    onOpenSettings()
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickActionCircle(
    icon: ImageVector,
    contentDescription: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun OverflowMenuItem(
    icon: ImageVector,
    title: String,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AegisAmberSecondary.copy(alpha = 0.25f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AegisAmberSecondary
                )
            }
        }
    }
}
