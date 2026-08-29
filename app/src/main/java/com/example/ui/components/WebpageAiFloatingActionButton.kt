package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiTaskType
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.utils.rememberReducedMotion

@Composable
fun WebpageAiFloatingActionButton(
    pageTitle: String,
    hasDetectedMedia: Boolean,
    onTriggerAiTask: (AiTaskType, String) -> Unit,
    onOpenMediaGrabber: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isReducedMotion = rememberReducedMotion()

    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_pulse"
    )

    Column(
        modifier = modifier.padding(end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded quick actions
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Summarize Page
                AiQuickActionItem(
                    title = "Summarize Webpage",
                    subtitle = "Gemini 3.1 Pro High-Thinking summary",
                    icon = Icons.Default.Summarize,
                    iconColor = AegisCyanPrimary,
                    testTag = "fab_action_summarize",
                    onClick = {
                        isExpanded = false
                        onTriggerAiTask(
                            AiTaskType.PAGE_SUMMARY,
                            "Please provide a comprehensive, structured executive summary of this webpage: '$pageTitle'. Highlight key takeaways, main facts, and actionable insights using deep reasoning."
                        )
                    }
                )

                // Option 2: Explain Page Concepts
                AiQuickActionItem(
                    title = "Explain Concepts",
                    subtitle = "Break down complex topics simply",
                    icon = Icons.Default.Lightbulb,
                    iconColor = Color(0xFFFFD54F),
                    testTag = "fab_action_explain",
                    onClick = {
                        isExpanded = false
                        onTriggerAiTask(
                            AiTaskType.DEEP_REASONING,
                            "Explain the core arguments, context, and key technical or informational concepts presented on this webpage: '$pageTitle'."
                        )
                    }
                )

                // Option 3: Deep Thinking Audit
                AiQuickActionItem(
                    title = "Deep Thinking Analysis",
                    subtitle = "Step-by-step analytical reasoning",
                    icon = Icons.Default.Psychology,
                    iconColor = Color(0xFFC084FC),
                    testTag = "fab_action_deep_reasoning",
                    onClick = {
                        isExpanded = false
                        onTriggerAiTask(
                            AiTaskType.DEEP_REASONING,
                            "Perform an in-depth analytical reasoning breakdown of the claims, arguments, and content structure on: '$pageTitle'."
                        )
                    }
                )

                // Option 4: Media Sniffer Download
                if (hasDetectedMedia) {
                    AiQuickActionItem(
                        title = "Extract Media (MP4/MP3)",
                        subtitle = "Sniffed stream formats ready",
                        icon = Icons.Default.Download,
                        iconColor = AegisAmberSecondary,
                        testTag = "fab_action_extract_media",
                        onClick = {
                            isExpanded = false
                            onOpenMediaGrabber()
                        }
                    )
                }
            }
        }

        // Primary Floating Action Button
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            shape = CircleShape,
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .testTag("gemini_ai_fab")
                .scale(if (!isExpanded && !isReducedMotion) pulseScale else 1f)
                .size(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (isExpanded) {
                                listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                            } else {
                                listOf(AegisCyanPrimary, Color(0xFF00B0FF), Color(0xFF7C4DFF))
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                    contentDescription = if (isExpanded) "Close AI Menu" else "Gemini 3.1 Pro AI Assistant",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AiQuickActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
