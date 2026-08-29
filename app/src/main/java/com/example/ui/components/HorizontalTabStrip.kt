package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.TabChip
import com.example.data.model.TabStripState
import kotlinx.coroutines.launch

enum class ArrowDirection { LEFT, RIGHT }

@Composable
fun ScrollIndicatorArrow(
    direction: ArrowDirection,
    onClick: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(32.dp)
                .testTag(if (direction == ArrowDirection.LEFT) "tab_strip_scroll_left" else "tab_strip_scroll_right")
        ) {
            Icon(
                imageVector = when (direction) {
                    ArrowDirection.LEFT -> Icons.AutoMirrored.Filled.NavigateBefore
                    ArrowDirection.RIGHT -> Icons.AutoMirrored.Filled.NavigateNext
                },
                contentDescription = if (direction == ArrowDirection.LEFT) "Faire défiler vers la gauche" else "Faire défiler vers la droite",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HorizontalTabStrip(
    state: TabStripState,
    onChipClick: (TabChip) -> Unit,
    onChipClose: (TabChip) -> Unit,
    onStripClose: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        val scrollState = rememberScrollState()
        val scope = rememberCoroutineScope()

        // Track scroll position to show/hide arrows dynamically
        val showLeft by remember {
            derivedStateOf { scrollState.value > 10 }
        }
        val showRight by remember {
            derivedStateOf { scrollState.maxValue > 0 && scrollState.value < (scrollState.maxValue - 10) }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Leading icon (lightbulb, add tab button, etc.)
                leadingIcon?.invoke()

                // Left scroll arrow
                ScrollIndicatorArrow(
                    direction = ArrowDirection.LEFT,
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollBy(-220f)
                        }
                    },
                    visible = showLeft
                )

                // Scrollable chip row
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .testTag("tab_strip_chip_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.chips.forEach { chip ->
                        TabChipItem(
                            chip = chip,
                            onClick = { onChipClick(chip) },
                            onClose = { onChipClose(chip) },
                            modifier = Modifier.testTag("tab_chip_${chip.id}")
                        )
                    }
                }

                // Right scroll arrow
                ScrollIndicatorArrow(
                    direction = ArrowDirection.RIGHT,
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollBy(220f)
                        }
                    },
                    visible = showRight
                )

                // Close entire strip
                IconButton(
                    onClick = onStripClose,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("tab_strip_dismiss_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer la barre d'onglets",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hairline divider below
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        }
    }
}
