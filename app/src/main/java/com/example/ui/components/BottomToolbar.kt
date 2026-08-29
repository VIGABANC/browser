package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AegisBrowserChrome
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisSeparator
import com.example.ui.theme.AegisTextPrimary
import com.example.ui.theme.AegisTextSecondary

@Composable
fun BottomToolbar(
    tabCount: Int = 3,
    canGoBack: Boolean = false,
    canGoForward: Boolean = false,
    isMenuOpen: Boolean = false,
    onGoBack: () -> Unit = {},
    onGoForward: () -> Unit = {},
    onGoHome: () -> Unit = {},
    onOpenTabs: () -> Unit = {},
    onToggleMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = AegisBrowserChrome,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Back (Disabled on NTP)
            IconButton(
                onClick = onGoBack,
                enabled = canGoBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("toolbar_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = if (canGoBack) AegisTextPrimary else AegisTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Forward (Disabled on NTP)
            IconButton(
                onClick = onGoForward,
                enabled = canGoForward,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("toolbar_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Suivant",
                    tint = if (canGoForward) AegisTextPrimary else AegisTextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            // 3. Home / New Tab
            IconButton(
                onClick = onGoHome,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("toolbar_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Accueil",
                    tint = AegisTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Tabs Counter (e.g. [ 3 ])
            IconButton(
                onClick = onOpenTabs,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("toolbar_tabs_counter_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(1.8.dp, AegisTextPrimary, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AegisTextPrimary
                    )
                }
            }

            // 5. 3-Dots Overflow Menu (with active pill highlight when open)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isMenuOpen) Color(0xFF262B33) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = AegisCyanPrimary),
                        onClick = onToggleMenu
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toolbar_overflow_menu_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu Aegis",
                    tint = AegisTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
