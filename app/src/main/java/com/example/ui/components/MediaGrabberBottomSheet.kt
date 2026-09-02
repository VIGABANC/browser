package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DetectedMedia
import com.example.data.model.MediaType
import com.example.ui.theme.AegisCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGrabberBottomSheet(
    detectedMediaList: List<DetectedMedia>,
    onDismiss: () -> Unit,
    onDownloadSelected: (List<DetectedMedia>) -> Unit,
    onPreviewMedia: (DetectedMedia) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // State for selected media items (batch download)
    var selectedMediaIds by remember { mutableStateOf(detectedMediaList.map { it.id }.toSet()) }
    
    // State for showing the download options dialog
    var showOptionsDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Movie,
                        contentDescription = null,
                        tint = AegisCyanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${detectedMediaList.size} Media Items Found",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Select All Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedMediaIds = if (selectedMediaIds.size == detectedMediaList.size) {
                            emptySet()
                        } else {
                            detectedMediaList.map { it.id }.toSet()
                        }
                    }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedMediaIds.size == detectedMediaList.size && detectedMediaList.isNotEmpty(),
                    onCheckedChange = { checked ->
                        selectedMediaIds = if (checked) detectedMediaList.map { it.id }.toSet() else emptySet()
                    }
                )
                Text(
                    text = "Select All",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Media List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detectedMediaList) { media ->
                    val isSelected = selectedMediaIds.contains(media.id)
                    
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMediaIds = if (isSelected) {
                                    selectedMediaIds - media.id
                                } else {
                                    selectedMediaIds + media.id
                                }
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) AegisCyanPrimary.copy(alpha = 0.05f) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) AegisCyanPrimary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedMediaIds = if (checked) {
                                            selectedMediaIds + media.id
                                        } else {
                                            selectedMediaIds - media.id
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val icon = when (media.type) {
                                        MediaType.AUDIO -> Icons.Default.LibraryMusic
                                        MediaType.HLS, MediaType.DASH -> Icons.Default.Movie
                                        else -> Icons.Default.Movie
                                    }
                                    val typeText = when (media.type) {
                                        MediaType.AUDIO -> "Audio"
                                        MediaType.HLS -> "HLS Stream"
                                        MediaType.DASH -> "DASH Stream"
                                        MediaType.BLOB -> "Blob Video"
                                        else -> "Video"
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${typeText} • ${media.mimeType ?: "Unknown"}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = media.title ?: media.url.take(50),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = media.domain,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val formatsList = media.formats
                                    if (formatsList.isNotEmpty()) {
                                        Text(
                                            text = formatsList.joinToString(", ") { "${it.qualityLabel} (${it.container})" },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AegisCyanPrimary
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { onPreviewMedia(media) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onDownloadSelected(listOf(media)) },
                                    modifier = Modifier.height(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Download", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Batch Download Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showOptionsDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Options")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { 
                        val selectedMedia = detectedMediaList.filter { selectedMediaIds.contains(it.id) }
                        onDownloadSelected(selectedMedia) 
                    },
                    enabled = selectedMediaIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AegisCyanPrimary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(2f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download Selected (${selectedMediaIds.size})",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
