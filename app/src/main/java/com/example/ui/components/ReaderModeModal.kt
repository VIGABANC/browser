package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ReaderArticle
import com.example.data.model.ReaderTheme
import com.example.ui.theme.AegisCyanPrimary

@Composable
fun ReaderModeModal(
    article: ReaderArticle,
    onDismiss: () -> Unit,
    onOpenAiSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTheme by remember { mutableStateOf(ReaderTheme.NIGHT) }
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Serif) }
    var isControlsOpen by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val currentBgColor = Color(selectedTheme.bgHex)
    val currentTextColor = Color(selectedTheme.textHex)

    Surface(
        color = currentBgColor,
        modifier = modifier
            .fillMaxSize()
            .testTag("reader_mode_modal")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(currentBgColor.copy(alpha = 0.95f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AegisCyanPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = AegisCyanPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reader Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AegisCyanPrimary
                                )
                            }
                        }

                        article.siteName?.let { site ->
                            Text(
                                text = site,
                                fontSize = 12.sp,
                                color = currentTextColor.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Reader Settings Drawer
                        IconButton(
                            onClick = { isControlsOpen = !isControlsOpen },
                            modifier = Modifier.testTag("reader_settings_toggle")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Reader Settings",
                                tint = if (isControlsOpen) AegisCyanPrimary else currentTextColor
                            )
                        }

                        // Copy article text
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("${article.title}\n\n${article.rawText}"))
                            },
                            modifier = Modifier.testTag("copy_article_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                tint = currentTextColor
                            )
                        }

                        // Close Reader Mode
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_reader_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = currentTextColor
                            )
                        }
                    }
                }

                HorizontalDivider(color = currentTextColor.copy(alpha = 0.1f))

                // Article Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Article Title
                        Text(
                            text = article.title,
                            fontSize = (fontSizeSp + 8f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = selectedFontFamily,
                            lineHeight = (fontSizeSp + 14f).sp,
                            color = currentTextColor
                        )
                    }

                    item {
                        // Metadata byline, reading time, word count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (!article.byline.isNullOrBlank()) {
                                Text(
                                    text = "By ${article.byline}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = currentTextColor.copy(alpha = 0.8f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = currentTextColor.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${article.estimatedReadingTimeMinutes} min read (${article.wordCount} words)",
                                    fontSize = 11.sp,
                                    color = currentTextColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = currentTextColor.copy(alpha = 0.1f))
                    }

                    // Lead image if extracted
                    if (!article.leadImageUrl.isNullOrBlank()) {
                        item {
                            AsyncImage(
                                model = article.leadImageUrl,
                                contentDescription = article.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    // Clean Body Paragraphs
                    items(article.paragraphs) { paragraph ->
                        Text(
                            text = paragraph,
                            fontSize = fontSizeSp.sp,
                            fontFamily = selectedFontFamily,
                            lineHeight = (fontSizeSp * 1.6f).sp,
                            textAlign = TextAlign.Start,
                            color = currentTextColor.copy(alpha = 0.95f)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }

            // Reader Customization Panel (Floating Card)
            AnimatedVisibility(
                visible = isControlsOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 56.dp, end = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier
                        .width(280.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Reader Appearance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Font size A- / A+
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Font Size", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (fontSizeSp > 12f) fontSizeSp -= 2f },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("A-", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${fontSizeSp.toInt()}sp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { if (fontSizeSp < 26f) fontSizeSp += 2f },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("A+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Typeface selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val fonts = listOf(
                                "Serif" to FontFamily.Serif,
                                "Sans" to FontFamily.Default,
                                "Mono" to FontFamily.Monospace
                            )
                            fonts.forEach { (label, family) ->
                                val isSelected = selectedFontFamily == family
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) AegisCyanPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { selectedFontFamily = family }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Theme choices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReaderTheme.entries.forEach { theme ->
                                val isSelected = selectedTheme == theme
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(theme.bgHex))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) AegisCyanPrimary else Color.Gray.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedTheme = theme }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = theme.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(theme.textHex)
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
