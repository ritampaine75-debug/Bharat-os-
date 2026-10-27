package com.example.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CulturalGreeting
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherTheme
import com.example.ui.components.ChakraEmblem
import com.example.ui.components.GlassCard
import java.util.Calendar

@Composable
fun BharatGreetingWidget(
    greetingChoice: CulturalGreeting,
    language: LauncherLanguage,
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    colSpan: Int = 4,
    rowSpan: Int = 1,
    modifier: Modifier = Modifier
) {
    val greetingText = when (language) {
        LauncherLanguage.HINDI -> greetingChoice.hindiText
        LauncherLanguage.BENGALI -> greetingChoice.bengaliText
        LauncherLanguage.ENGLISH -> greetingChoice.englishText
    }

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val periodDesc = when (hour) {
        in 4..11 -> "शुभ प्रभात • Auspicious Dawn"
        in 12..16 -> "शुभ दोपहर • Radiant Midday"
        in 17..20 -> "संध्या वेला • Peaceful Twilight"
        else -> "शुभ रात्रि • Silent Night"
    }

    val isCompact = colSpan <= 2
    val isExpanded = rowSpan >= 2

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bharat_greeting_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 12.dp else 18.dp, vertical = if (isExpanded) 18.dp else 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingText,
                        fontSize = if (isCompact) 16.sp else 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB74D),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = periodDesc,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1
                    )
                }

                if (!isCompact) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(Color(0xFFFFB300).copy(0.4f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ChakraEmblem(modifier = Modifier.size(34.dp))
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "सत्यमेव जयते • Harmony, Focus & Mindful Productivity",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.78f)
                )
            }
        }
    }
}
