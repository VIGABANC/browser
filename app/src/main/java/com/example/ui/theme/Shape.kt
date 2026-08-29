package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Aegis Browser Shapes
 *
 * Restrained geometry optimized for browser chrome density:
 * - Omnibox: 22dp pill radius
 * - Menu Surface: 16-20dp rounded bottom sheet
 * - Chips & Badges: 8-16dp
 * - Cards & Dialogs: 12-16dp
 */
val AegisShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

val OmniboxShape = RoundedCornerShape(22.dp)
val MenuSheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
val ChipPillShape = RoundedCornerShape(16.dp)
val DialogShape = RoundedCornerShape(16.dp)
val CardShape = RoundedCornerShape(12.dp)
