package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BrowserTab
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisEmeraldSafe
import com.example.ui.theme.AegisIndigoIncognito
import com.example.ui.utils.rememberReducedMotion

@Composable
fun OmniboxBar(
    tab: BrowserTab,
    blockedCount: Int,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onShieldClick: () -> Unit,
    onMediaSnifferClick: () -> Unit,
    onAiClick: () -> Unit,
    onReaderModeClick: () -> Unit,
    onFindInPageClick: () -> Unit,
    onAutoFillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember(tab.url, isEditing) {
        mutableStateOf(if (tab.url == "about:home") "" else tab.url)
    }

    val isReducedMotion = rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "media_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Shield Badge Button
                IconButton(
                    onClick = onShieldClick,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("shield_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (blockedCount > 0) {
                                Badge(
                                    containerColor = AegisCyanPrimary,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = if (blockedCount > 99) "99+" else "$blockedCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Aegis Shields Dashboard",
                            tint = if (tab.isIncognito) AegisIndigoIncognito else AegisCyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // URL & Search Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = {
                                textInput = it
                                onQueryChange(it)
                            },
                            singleLine = true,
                            placeholder = {
                                Text(
                                    "Search Google or type web address",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    isEditing = false
                                    onNavigate(textInput)
                                }
                            ),
                            trailingIcon = {
                                if (textInput.isNotBlank()) {
                                    IconButton(
                                        onClick = {
                                            textInput = ""
                                            onQueryChange("")
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear Input",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("omnibox_input")
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isEditing = true
                                    onQueryChange(textInput)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (tab.sslSecure) Icons.Default.Lock else Icons.Default.Search,
                                contentDescription = if (tab.sslSecure) "SSL Encrypted" else "Search",
                                tint = if (tab.sslSecure) AegisEmeraldSafe else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tab.url == "about:home") "Aegis Search & Address" else formatDisplayUrl(tab.url),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (tab.url == "about:home") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Media Sniffer Grabber Icon
                if (tab.detectedMediaList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .scale(if (isReducedMotion) 1f else pulseScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AegisAmberSecondary)
                            .clickable { onMediaSnifferClick() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("media_grabber_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Media Grabber",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${tab.detectedMediaList.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Reader Mode Icon (if on active webpage)
                if (tab.url != "about:home" && tab.url != "about:blank") {
                    IconButton(
                        onClick = onReaderModeClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("omnibox_reader_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Reader Mode",
                            tint = AegisCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Gemini High Thinking AI Assistant Button
                IconButton(
                    onClick = onAiClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .testTag("ai_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini High-Thinking AI Assistant",
                        tint = AegisCyanPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reload or Stop
                IconButton(
                    onClick = onReload,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload Page",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Real-time Google Search Suggest Predictions Dropdown Overlay
            if (isEditing && suggestions.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("google_suggest_dropdown")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Google Predictions",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AegisCyanPrimary
                            )
                            Text(
                                text = "Tap to search",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        suggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        focusManager.clearFocus()
                                        isEditing = false
                                        onNavigate(suggestion)
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Tap to put prediction in box
                                IconButton(
                                    onClick = {
                                        textInput = suggestion
                                        onQueryChange(suggestion)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NorthWest,
                                        contentDescription = "Fill Query",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        }
                    }
                }
            }

            // Web Loading Progress Bar
            AnimatedVisibility(
                visible = tab.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { tab.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = AegisCyanPrimary,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

private fun formatDisplayUrl(url: String): String {
    return try {
        val clean = url.removePrefix("https://").removePrefix("http://").removePrefix("www.")
        clean.take(45)
    } catch (e: Exception) {
        url
    }
}
