package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary

@Composable
fun BottomToolbar(
    tabCount: Int,
    activeDownloadCount: Int,
    isDesktopMode: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarksHistory: () -> Unit,
    onOpenClearData: () -> Unit,
    onOpenFindInPage: () -> Unit,
    onOpenReaderMode: () -> Unit,
    onOpenAutoFill: () -> Unit,
    onOpenAiAssistant: () -> Unit,
    onOpenShields: () -> Unit,
    onNewTab: (Boolean) -> Unit,
    onToggleDesktopMode: () -> Unit,
    onAddBookmark: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuSheetOpen by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Home (Accueil - Screenshot 4)
            IconButton(
                onClick = onGoHome,
                modifier = Modifier.testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Accueil",
                    tint = AegisCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bookmarks (Favoris - Screenshot 4)
            IconButton(
                onClick = onOpenBookmarksHistory,
                modifier = Modifier.testTag("nav_bookmarks_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Favoris",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Search (Rechercher - Screenshot 4)
            IconButton(
                onClick = onGoHome,
                modifier = Modifier.testTag("nav_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Recherche",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Tab Switcher with Count Badge (7 in square - Screenshot 4)
            IconButton(
                onClick = onOpenTabs,
                modifier = Modifier.testTag("nav_tabs_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (tabCount > 99) "99+" else "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 3-Dots Overflow Menu (⋮ - Screenshot 1, 2, 3, 4)
            IconButton(
                onClick = { isMenuSheetOpen = true },
                modifier = Modifier.testTag("nav_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu Options",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    if (isMenuSheetOpen) {
        BrowserOverflowMenuSheet(
            isDesktopMode = isDesktopMode,
            onDismiss = { isMenuSheetOpen = false },
            onGoForward = onGoForward,
            onReload = onGoHome,
            onAddBookmark = onAddBookmark,
            onOpenDownloads = onOpenDownloads,
            onOpenShields = onOpenShields,
            onNewTab = onNewTab,
            onOpenTabs = onOpenTabs,
            onOpenBookmarksHistory = onOpenBookmarksHistory,
            onOpenClearData = onOpenClearData,
            onOpenAutoFill = onOpenAutoFill,
            onOpenAiAssistant = onOpenAiAssistant,
            onOpenFindInPage = onOpenFindInPage,
            onOpenReaderMode = onOpenReaderMode,
            onToggleDesktopMode = onToggleDesktopMode,
            onOpenSettings = onOpenSettings
        )
    }
}
