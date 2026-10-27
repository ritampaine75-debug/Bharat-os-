package com.example.ui.components.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherTheme
import com.example.ui.components.GlassCard
import java.util.Calendar

@Composable
fun BharatWeatherWidget(
    language: LauncherLanguage,
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    colSpan: Int = 4,
    rowSpan: Int = 1,
    modifier: Modifier = Modifier
) {
    val month = Calendar.getInstance().get(Calendar.MONTH)
    val season = when (month) {
        in 2..4 -> when (language) {
            LauncherLanguage.HINDI -> "ग्रीष्म ऋतु (Summer)"
            LauncherLanguage.BENGALI -> "গ্রীষ্ম কাল (Summer)"
            else -> "Grishma (Summer)"
        }
        in 5..8 -> when (language) {
            LauncherLanguage.HINDI -> "वर्षा ऋतु (Monsoon)"
            LauncherLanguage.BENGALI -> "বর্ষা কাল (Monsoon)"
            else -> "Varsha (Monsoon)"
        }
        in 9..10 -> when (language) {
            LauncherLanguage.HINDI -> "शरद ऋतु (Autumn)"
            LauncherLanguage.BENGALI -> "শরৎ কাল (Autumn)"
            else -> "Sharad (Autumn)"
        }
        else -> when (language) {
            LauncherLanguage.HINDI -> "शीत ऋतु (Winter)"
            LauncherLanguage.BENGALI -> "শীত কাল (Winter)"
            else -> "Shita (Winter)"
        }
    }

    val isCompact = colSpan <= 2
    val isExpanded = rowSpan >= 2

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bharat_weather_widget")
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(if (isCompact) 28.dp else 36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "28°C",
                            fontSize = if (isCompact) 22.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = season,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }

                if (!isCompact) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AQI 72 • Good",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF86EFAC)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Clear Sky • India",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Humidity: 54% • UV Index: Moderate",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "H: 31° L: 22°",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFCC80)
                    )
                }
            }
        }
    }
}
