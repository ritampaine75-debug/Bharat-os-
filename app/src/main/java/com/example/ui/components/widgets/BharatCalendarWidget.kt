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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BharatCalendarWidget(
    language: LauncherLanguage,
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    colSpan: Int = 4,
    rowSpan: Int = 1,
    modifier: Modifier = Modifier
) {
    val locale = remember(language) {
        when (language) {
            LauncherLanguage.HINDI -> Locale("hi", "IN")
            LauncherLanguage.BENGALI -> Locale("bn", "IN")
            LauncherLanguage.ENGLISH -> Locale.ENGLISH
        }
    }

    val isCompact = colSpan <= 2
    val isExpanded = rowSpan >= 2

    val dayFormat = remember(locale) { SimpleDateFormat("d", locale) }
    val monthFormat = remember(locale) { SimpleDateFormat("MMMM yyyy", locale) }
    val dayNameFormat = remember(locale) { SimpleDateFormat("EEEE", locale) }

    val now = Date()
    val dayNumber = dayFormat.format(now)
    val monthName = monthFormat.format(now)
    val dayName = dayNameFormat.format(now)

    val panchangText = when (language) {
        LauncherLanguage.HINDI -> "विक्रम संवत् २०८३ • कार्तिक शुक्ल"
        LauncherLanguage.BENGALI -> "বিক্রম সংবৎ ২০৮৩ • কার্তিক শুক্ল"
        LauncherLanguage.ENGLISH -> "Vikram Samvat 2083 • Kartik Shukla"
    }

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        modifier = modifier
            .fillMaxWidth()
            .testTag("bharat_calendar_widget")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompact) 12.dp else 18.dp, vertical = if (isExpanded) 18.dp else 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(if (isCompact) 24.dp else 32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isCompact) "$dayNumber $monthName" else "$dayName, $dayNumber $monthName",
                            fontSize = if (isCompact) 13.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = panchangText,
                            fontSize = 11.sp,
                            color = Color(0xFFFFD54F),
                            maxLines = 1
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shubh Muhurat • Abhijit: 11:48 AM – 12:34 PM",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.82f)
                )
            }
        }
    }
}
