package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.StripType
import com.example.data.model.TabChip
import com.example.data.model.TabStripState

@Composable
fun FeatureNavigationStrip(
    features: List<TabChip>,
    activeFeatureId: String?,
    onFeatureClick: (TabChip) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = TabStripState(
        chips = features.map { chip ->
            chip.copy(isActive = chip.id == activeFeatureId)
        },
        isVisible = true,
        stripType = StripType.FEATURES
    )

    HorizontalTabStrip(
        state = state,
        onChipClick = onFeatureClick,
        onChipClose = { /* Features are not individually closed */ },
        onStripClose = onDismiss,
        modifier = modifier.testTag("feature_navigation_strip"),
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = "Fonctionnalités suggérées",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}
