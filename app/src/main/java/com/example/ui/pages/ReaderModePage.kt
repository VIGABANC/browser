package com.example.ui.pages

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReaderArticle
import com.example.ui.theme.AegisAmberSecondary
import com.example.ui.theme.AegisCyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderModePage(
    article: ReaderArticle,
    onOpenAiSummary: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSize by remember { mutableFloatStateOf(16f) }
    var selectedTheme by remember { mutableIntStateOf(0) } // 0 = Dark Slate, 1 = Warm Sepia, 2 = Pitch Black

    val bgColor = when (selectedTheme) {
        1 -> Color(0xFF2C241E)
        2 -> Color(0xFF000000)
        else -> Color(0xFF0F172A)
    }

    val textColor = when (selectedTheme) {
        1 -> Color(0xFFF7EBD9)
        2 -> Color(0xFFE2E8F0)
        else -> Color(0xFFF1F5F9)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AegisCyanPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = AegisCyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Mode Lecture Épuré",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { if (fontSize > 12f) fontSize -= 2f }
                    ) {
                        Icon(Icons.Default.TextDecrease, contentDescription = "Diminuer", tint = Color.White)
                    }
                    IconButton(
                        onClick = { if (fontSize < 28f) fontSize += 2f }
                    ) {
                        Icon(Icons.Default.TextIncrease, contentDescription = "Agrandir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = bgColor,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // AI Summary Banner
            Button(
                onClick = onOpenAiSummary,
                colors = ButtonDefaults.buttonColors(containerColor = AegisCyanPrimary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Générer un résumé avec Gemini 3.1 Pro", color = Color.Black, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Article Header
            Text(
                text = article.title,
                fontSize = (fontSize + 6).sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = (fontSize + 12).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${article.siteName ?: "Article Web"} • ${article.estimatedReadingTimeMinutes} min de lecture",
                fontSize = 12.sp,
                color = AegisAmberSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Article Paragraphs or Raw Text
            if (article.paragraphs.isNotEmpty()) {
                article.paragraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        fontSize = fontSize.sp,
                        color = textColor.copy(alpha = 0.9f),
                        lineHeight = (fontSize * 1.6f).sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                Text(
                    text = article.rawText.ifBlank { "Aucun contenu textuel extrait sur cette page." },
                    fontSize = fontSize.sp,
                    color = textColor.copy(alpha = 0.9f),
                    lineHeight = (fontSize * 1.6f).sp,
                    fontFamily = FontFamily.Serif
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
