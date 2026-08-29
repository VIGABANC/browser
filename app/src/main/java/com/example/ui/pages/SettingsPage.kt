package com.example.ui.pages

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SearchEngine
import com.example.data.model.UserAgentMode
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisEmeraldSafe
import com.example.ui.theme.AegisIndigoIncognito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    searchEngine: SearchEngine,
    userAgentMode: UserAgentMode,
    isDarkTheme: Boolean,
    isSafeMode: Boolean,
    isIncognito: Boolean,
    onSelectSearchEngine: (SearchEngine) -> Unit,
    onSelectUserAgentMode: (UserAgentMode) -> Unit,
    onToggleDarkTheme: () -> Unit,
    onToggleSafeMode: (Boolean) -> Unit,
    onToggleIncognito: (Boolean) -> Unit,
    onClearAllData: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToShields: () -> Unit,
    onNavigateToAutoFill: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isClearDataDialogOpen by remember { mutableStateOf(false) }
    var isSearchEngineDialogOpen by remember { mutableStateOf(false) }
    var isUserAgentDialogOpen by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Paramètres du Navigateur",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
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
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Privacy & Shields Section
            SettingsSectionHeader(title = "Boucliers & Confidentialité", color = AegisEmeraldSafe)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsNavigationItem(
                        icon = Icons.Default.Shield,
                        iconTint = AegisEmeraldSafe,
                        title = "Tableau de bord des Boucliers",
                        subtitle = "Ad-blocker, anti-tracking, empreinte numérique",
                        onClick = onNavigateToShields
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsNavigationItem(
                        icon = Icons.Default.VpnKey,
                        iconTint = AegisAmberSecondary,
                        title = "Coffre-fort Mots de passe & AutoFill",
                        subtitle = "Identifiants chiffrés en local avec AES",
                        onClick = onNavigateToAutoFill
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsToggleItem(
                        icon = Icons.Default.Security,
                        iconTint = AegisCyanPrimary,
                        title = "Mode Téléchargement Sécurisé (Safe Mode)",
                        subtitle = "Vérification des licences et conformité des flux ouverts",
                        checked = isSafeMode,
                        onCheckedChange = onToggleSafeMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Navigation & Engine Section
            SettingsSectionHeader(title = "Recherche & Identité Réseau", color = AegisCyanPrimary)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsNavigationItem(
                        icon = Icons.Default.Search,
                        iconTint = AegisCyanPrimary,
                        title = "Moteur de recherche par défaut",
                        subtitle = searchEngine.displayName,
                        onClick = { isSearchEngineDialogOpen = true }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    SettingsNavigationItem(
                        icon = Icons.Default.Devices,
                        iconTint = AegisAmberSecondary,
                        title = "User-Agent Spoofing & Empreinte",
                        subtitle = userAgentMode.displayName,
                        onClick = { isUserAgentDialogOpen = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App Appearance & Theme
            SettingsSectionHeader(title = "Apparence & Thème", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Mode Sombre Cyberpunk",
                        subtitle = "Palette optimisée OLED avec noir pur et néon",
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Wipe & Danger Zone
            SettingsSectionHeader(title = "Données & Stockage Local", color = MaterialTheme.colorScheme.error)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    SettingsNavigationItem(
                        icon = Icons.Default.DeleteSweep,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Effacer toutes les données locales",
                        subtitle = "Cache WebView, historique Room, cookies & mots de passe",
                        onClick = { isClearDataDialogOpen = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // About Aegis
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AegisCyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = AegisCyanPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Aegis Browser", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Version 2.4.0 • Gemini 3.1 Pro Integrated", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Architecture Zero-Telemetry & Stockage Local Room", fontSize = 11.sp, color = AegisEmeraldSafe)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (isClearDataDialogOpen) {
        AlertDialog(
            onDismissRequest = { isClearDataDialogOpen = false },
            title = { Text("Effacer toutes les données ?", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Text(
                    "Cette action réinitialise le cache, efface la base de données locale Room (historique, favoris), les mots de passe enregistrés et l'historique IA.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        isClearDataDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Tout effacer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isClearDataDialogOpen = false }) {
                    Text("Annuler", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Search Engine Selector Dialog
    if (isSearchEngineDialogOpen) {
        AlertDialog(
            onDismissRequest = { isSearchEngineDialogOpen = false },
            title = { Text("Choisir le moteur de recherche", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    SearchEngine.entries.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSearchEngine(engine)
                                    isSearchEngineDialogOpen = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = searchEngine == engine,
                                onClick = {
                                    onSelectSearchEngine(engine)
                                    isSearchEngineDialogOpen = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AegisCyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(engine.displayName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(engine.searchUrl, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                TextButton(onClick = { isSearchEngineDialogOpen = false }) {
                    Text("Fermer", color = AegisCyanPrimary)
                }
            }
        )
    }

    // User-Agent Selector Dialog
    if (isUserAgentDialogOpen) {
        AlertDialog(
            onDismissRequest = { isUserAgentDialogOpen = false },
            title = { Text("Sélectionner l'identité User-Agent", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    UserAgentMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectUserAgentMode(mode)
                                    isUserAgentDialogOpen = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = userAgentMode == mode,
                                onClick = {
                                    onSelectUserAgentMode(mode)
                                    isUserAgentDialogOpen = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = AegisCyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(mode.displayName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(mode.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            confirmButton = {
                TextButton(onClick = { isUserAgentDialogOpen = false }) {
                    Text("Fermer", color = AegisCyanPrimary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, color: Color) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AegisCyanPrimary)
        )
    }
}
