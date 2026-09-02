package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DownloadItem
import com.example.data.model.DownloadStatus
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary

@Composable
fun ActiveDownloadBottomBar(
    activeDownloads: List<DownloadItem>,
    onPauseDownload: (String) -> Unit,
    onResumeDownload: (String) -> Unit,
    onCancelDownload: (String) -> Unit,
    onOpenDownloadCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeItem = activeDownloads.firstOrNull {
        it.status == DownloadStatus.DOWNLOADING ||
                it.status == DownloadStatus.CONVERTING_AUDIO ||
                it.status == DownloadStatus.QUEUED ||
                it.status == DownloadStatus.PAUSED
    }

    AnimatedVisibility(
        visible = activeItem != null,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
        modifier = modifier
    ) {
        if (activeItem != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenDownloadCenter() }
                    .testTag("active_download_progress_bar")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val iconVector = when {
                                activeItem.status == DownloadStatus.CONVERTING_AUDIO -> Icons.Default.Sync
                                activeItem.format.isAudioOnly -> Icons.Default.Audiotrack
                                else -> Icons.Default.Download
                            }
                            val iconColor = if (activeItem.format.isAudioOnly) AegisAmberSecondary else AegisCyanPrimary

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(iconColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = iconVector,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeItem.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (activeDownloads.size > 1) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(+${activeDownloads.size - 1} more)",
                                            fontSize = 11.sp,
                                            color = AegisCyanPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                val statusSubtitle = when (activeItem.status) {
                                    DownloadStatus.CONVERTING_AUDIO -> "Extracting & converting HQ audio stream..."
                                    DownloadStatus.PAUSED -> "Paused"
                                    DownloadStatus.QUEUED -> "Queued for sniffer extraction..."
                                    else -> {
                                        val speedText = if (activeItem.speedBps > 0) {
                                            " • ${(activeItem.speedBps / (1024f * 1024f)).formatTwoDecimals()} MB/s"
                                        } else ""
                                        "${activeItem.format.qualityLabel} ${activeItem.format.container.uppercase()}$speedText"
                                    }
                                }

                                Text(
                                    text = statusSubtitle,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Quick Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeItem.status == DownloadStatus.DOWNLOADING) {
                                IconButton(
                                    onClick = { onPauseDownload(activeItem.id) },
                                    modifier = Modifier.size(32.dp).testTag("pause_active_download")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else if (activeItem.status == DownloadStatus.PAUSED) {
                                IconButton(
                                    onClick = { onResumeDownload(activeItem.id) },
                                    modifier = Modifier.size(32.dp).testTag("resume_active_download")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = AegisCyanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onCancelDownload(activeItem.id) },
                                modifier = Modifier.size(32.dp).testTag("cancel_active_download")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onOpenDownloadCenter,
                                modifier = Modifier.size(32.dp).testTag("expand_download_center")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open Downloads",
                                    tint = AegisCyanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Visual Progress Bar
                    if (activeItem.status == DownloadStatus.CONVERTING_AUDIO) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AegisAmberSecondary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { activeItem.progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AegisCyanPrimary,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            drawStopIndicator = {}
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val downloadedMb = (activeItem.downloadedBytes / (1024f * 1024f)).formatTwoDecimals()
                        val totalMb = (activeItem.totalBytes / (1024f * 1024f)).formatTwoDecimals()
                        Text(
                            text = if (activeItem.totalBytes > 0) "$downloadedMb MB / $totalMb MB" else "$downloadedMb MB",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(activeItem.progressPercent * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AegisCyanPrimary
                        )
                    }
                }
            }
        }
    }
}

private fun Float.formatTwoDecimals(): String {
    return String.format(java.util.Locale.US, "%.1f", this)
}
