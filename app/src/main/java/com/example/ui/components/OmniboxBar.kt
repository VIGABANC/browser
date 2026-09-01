package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BrowserTab
import com.example.ui.theme.AegisBrowserChrome
import com.example.ui.theme.AegisCyanPrimary
import com.example.ui.theme.AegisMenuSurface
import com.example.ui.theme.AegisOmniboxBg
import com.example.ui.theme.AegisTextPrimary
import com.example.ui.theme.AegisTextSecondary
import com.example.ui.utils.VoiceRecognitionManager

@Composable
fun OmniboxBar(
    tab: BrowserTab? = null,
    blockedCount: Int = 0,
    suggestions: List<String> = emptyList(),
    onQueryChange: (String) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onReload: () -> Unit = {},
    onShieldClick: () -> Unit = {},
    onMediaSnifferClick: () -> Unit = {},
    onAiClick: () -> Unit = {},
    onReaderModeClick: () -> Unit = {},
    onFindInPageClick: () -> Unit = {},
    onAutoFillClick: () -> Unit = {},
    onToggleMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        VoiceRecognitionManager.handleSpeechResult(result) { spokenText ->
            textInput = spokenText
            onQueryChange(spokenText)
            onNavigate(spokenText)
        }
    }

    Surface(
        color = AegisBrowserChrome,
        modifier = modifier.fillMaxWidth() // removed height(56.dp) constraint to allow dropdown to not clip, actually DropdownMenu floats. Let's keep height(56.dp) on surface and put DropdownMenu inside.
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Aegis Shield Privacy Control Button (Left)
            IconButton(
                onClick = onShieldClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("shield_button")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_aegis_shield),
                    contentDescription = "Aegis Shields Protection",
                    tint = AegisCyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 2. Large Pill-Shaped Omnibox Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(AegisOmniboxBg)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Rechercher",
                        tint = AegisTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    if (isEditing) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = {
                                textInput = it
                                onQueryChange(it)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = AegisTextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(AegisCyanPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    focusManager.clearFocus()
                                    isEditing = false
                                    if (textInput.isNotBlank()) {
                                        onNavigate(textInput)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("omnibox_input")
                        )
                    } else {
                        Text(
                            text = "Rechercher ou saisir une adresse",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = AegisTextSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isEditing = true }
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            speechLauncher.launch(VoiceRecognitionManager.getSpeechIntent())
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Recherche vocale",
                            tint = AegisTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. 3-Dots Vertical Overflow Button (Top Right)
            IconButton(
                onClick = onToggleMenu,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("omnibox_more_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu Options",
                    tint = AegisTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        androidx.compose.material3.DropdownMenu(
            expanded = isEditing && suggestions.isNotEmpty(),
            onDismissRequest = { /* Don't dismiss on outside tap unless focus lost */ },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(AegisMenuSurface)
        ) {
            suggestions.take(5).forEach { suggestion ->
                androidx.compose.material3.DropdownMenuItem(
                    text = {
                        Text(
                            text = suggestion,
                            color = AegisTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        textInput = suggestion
                        isEditing = false
                        focusManager.clearFocus()
                        onNavigate(suggestion)
                    }
                )
            }
        }
    }
}
}
