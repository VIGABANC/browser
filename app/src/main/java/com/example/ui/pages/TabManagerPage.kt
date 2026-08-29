package com.example.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisIndigoIncognito
import com.example.ui.theme.AegisIndigoIncognitoBorderDark
import com.example.ui.theme.AegisIndigoIncognitoBorderLight
import com.example.ui.theme.AegisIndigoIncognitoContainerDark
import com.example.ui.theme.AegisIndigoIncognitoContainerLight
import com.example.ui.theme.AegisIndigoIncognitoDark
import com.example.ui.theme.AegisIndigoIncognitoLight
import com.example.ui.theme.AegisRedDanger


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabManagerPage(
    tabs: List<BrowserTab>,
    activeTabId: String,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: (Boolean) -> Unit,
    onCloseAll: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSessionTab by remember { mutableIntStateOf(0) } // 0 = Standard, 1 = Privé
    var searchQuery by remember { mutableStateOf("") }

    val standardTabs = remember(tabs, searchQuery) {
        tabs.filter { !it.isIncognito && (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.url.contains(searchQuery, true)) }
    }
    val incognitoTabs = remember(tabs, searchQuery) {
        tabs.filter { it.isIncognito && (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.url.contains(searchQuery, true)) }
    }

    val currentTabList = if (selectedSessionTab == 0) standardTabs else incognitoTabs
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedSessionTab == 1) AegisIndigoIncognito.copy(alpha = 0.2f)
                                    else AegisCyanPrimary.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedSessionTab == 1) Icons.Default.Security else Icons.Default.Tab,
                                contentDescription = null,
                                tint = if (selectedSessionTab == 1) AegisIndigoIncognito else AegisCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gestionnaire d'Onglets",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tabs.size} onglets ouverts",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("tabs_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onNewTab(selectedSessionTab == 1)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("add_new_tab_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nouvel onglet",
                            tint = if (selectedSessionTab == 1) AegisIndigoIncognito else AegisCyanPrimary
                        )
                    }
                    if (tabs.isNotEmpty()) {
                        IconButton(
                            onClick = onCloseAll,
                            modifier = Modifier.testTag("close_all_tabs_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Fermer tout",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Rechercher dans les onglets...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = if (selectedSessionTab == 1) AegisIndigoIncognito else AegisCyanPrimary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (selectedSessionTab == 1) AegisIndigoIncognito else AegisCyanPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Standard vs Incognito Segment
            TabRow(
                selectedTabIndex = selectedSessionTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSessionTab]),
                        color = if (selectedSessionTab == 1) AegisIndigoIncognito else AegisCyanPrimary,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedSessionTab == 0,
                    onClick = { selectedSessionTab = 0 },
                    text = {
                        Text(
                            "Standard (${tabs.count { !it.isIncognito }})",
                            fontWeight = if (selectedSessionTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSessionTab == 0) (if (isDark) AegisCyanPrimary else MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedSessionTab == 1,
                    onClick = { selectedSessionTab = 1 },
                    text = {
                        Text(
                            "Privé (${tabs.count { it.isIncognito }})",
                            fontWeight = if (selectedSessionTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedSessionTab == 1) AegisIndigoIncognito else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentTabList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedSessionTab == 1) Icons.Default.Security else Icons.Default.Tab,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedSessionTab == 1) "Aucun onglet privé ouvert" else "Aucun onglet standard",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onNewTab(selectedSessionTab == 1)
                                onNavigateBack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedSessionTab == 1) AegisIndigoIncognito else (if (isDark) AegisCyanPrimary else MaterialTheme.colorScheme.primary)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (selectedSessionTab == 1) "Ouvrir onglet privé" else "Ouvrir un onglet",
                                color = if (selectedSessionTab == 1 || isDark) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(currentTabList, key = { it.id }) { tab ->
                        val isActive = tab.id == activeTabId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (tab.isIncognito) {
                                    if (isDark) AegisIndigoIncognitoDark else AegisIndigoIncognitoContainerLight
                                } else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isActive) 2.dp else 1.dp,
                                if (isActive) (if (tab.isIncognito) AegisIndigoIncognito else (if (isDark) AegisCyanPrimary else MaterialTheme.colorScheme.primary))
                                else if (tab.isIncognito) {
                                    if (isDark) AegisIndigoIncognitoBorderDark else AegisIndigoIncognitoBorderLight
                                } else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectTab(tab.id)
                                    onNavigateBack()
                                }
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Header bar of card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (tab.isIncognito) {
                                                if (isDark) AegisIndigoIncognito.copy(alpha = 0.25f) else AegisIndigoIncognitoLight
                                            } else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (tab.isIncognito) Icons.Default.Lock else Icons.Default.Public,
                                            contentDescription = null,
                                            tint = if (tab.isIncognito) AegisIndigoIncognito else (if (isDark) AegisCyanPrimary else MaterialTheme.colorScheme.primary),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { onCloseTab(tab.id) },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Fermer",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                // Thumbnail preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(
                                            if (tab.isIncognito) {
                                                if (isDark) AegisIndigoIncognitoContainerDark else AegisIndigoIncognitoContainerLight
                                            } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (tab.isIncognito) Icons.Default.Security else Icons.Default.Public,
                                            contentDescription = null,
                                            tint = if (tab.isIncognito) AegisIndigoIncognito.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = tab.url.removePrefix("https://").removePrefix("http://"),
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
