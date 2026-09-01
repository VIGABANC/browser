package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShieldStats
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisEmeraldSafe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShieldDashboardModal(
    stats: ShieldStats,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AegisCyanPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Aegis Shields",
                            tint = AegisCyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aegis Shields Protection",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Zero Telemetry • Cosmetic Ad Block",
                            fontSize = 11.sp,
                            color = AegisCyanPrimary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShieldMetricCard(
                    title = "Ads Blocked",
                    value = "${stats.adsBlockedTotal}",
                    icon = Icons.Default.Shield,
                    iconTint = AegisCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                ShieldMetricCard(
                    title = "Trackers",
                    value = "${stats.trackersBlockedTotal}",
                    icon = Icons.Default.Speed,
                    iconTint = AegisAmberSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShieldMetricCard(
                    title = "HTTPS Upgrades",
                    value = "${stats.httpsUpgradesTotal}",
                    icon = Icons.Default.Https,
                    iconTint = AegisEmeraldSafe,
                    modifier = Modifier.weight(1f)
                )
                ShieldMetricCard(
                    title = "Est. Data Saved",
                    value = "${String.format("%.1f", stats.bandwidthSavedMb)} MB",
                    icon = Icons.Default.Public,
                    iconTint = AegisCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Shield Mode",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (stats.shieldMode == "Standard") AegisCyanPrimary else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Standard",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (stats.shieldMode == "Standard") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (stats.shieldMode == "Strict") AegisCyanPrimary else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Strict",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (stats.shieldMode == "Strict") Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Privacy & Script Controls",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            NetworkTrafficChart(
                trafficData = listOf(10f, 30f, 25f, 50f, 40f, 80f, 60f, 90f, 100f, 75f),
                lineColor = AegisCyanPrimary,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Toggles
            ShieldSettingRow(
                title = "Cosmetic Ad & Tracker Blocking",
                subtitle = "EasyList + AdBlock Rust trie matcher",
                icon = Icons.Default.Shield,
                checked = stats.isShieldEnabled
            )

            ShieldSettingRow(
                title = "Block Fingerprinting",
                subtitle = "Randomizes Canvas, WebGL and audio noise",
                icon = Icons.Default.Fingerprint,
                checked = stats.blockFingerprinting
            )

            ShieldSettingRow(
                title = "Block 3rd-Party Tracking Cookies",
                subtitle = "Isolates storage to first-party context",
                icon = Icons.Default.Cookie,
                checked = stats.blockThirdPartyCookies
            )
        }
    }
}

@Composable
private fun ShieldMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ShieldSettingRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AegisCyanPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = AegisCyanPrimary
            )
        )
    }
}
