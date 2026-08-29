package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.BrowserTab
import com.example.data.model.ChipType
import com.example.data.model.StripType
import com.example.data.model.TabChip
import com.example.data.model.TabStripState

@Composable
fun OpenTabsStrip(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onTabClick: (BrowserTab) -> Unit,
    onTabClose: (BrowserTab) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chips = tabs.map { tab ->
        val displayTitle = when {
            tab.title.isNotBlank() && tab.title != "Loading..." -> tab.title
            tab.url == "about:home" -> if (tab.isIncognito) "Onglet Privé" else "Nouvel onglet"
            else -> tab.url.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(24).ifBlank { "Onglet" }
        }
        TabChip(
            id = tab.id,
            title = displayTitle,
            url = tab.url,
            faviconUrl = null,
            isActive = tab.id == activeTabId,
            isCloseable = true,
            type = ChipType.TAB
        )
    }

    val state = TabStripState(
        chips = chips,
        activeChipId = activeTabId,
        isVisible = true,
        stripType = StripType.TABS
    )

    HorizontalTabStrip(
        state = state,
        onChipClick = { chip ->
            val tab = tabs.firstOrNull { it.id == chip.id }
            tab?.let { onTabClick(it) }
        },
        onChipClose = { chip ->
            val tab = tabs.firstOrNull { it.id == chip.id }
            tab?.let { onTabClose(it) }
        },
        onStripClose = onDismiss,
        modifier = modifier.testTag("open_tabs_strip"),
        leadingIcon = {
            IconButton(
                onClick = onNewTab,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("tab_strip_new_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nouvel onglet",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
