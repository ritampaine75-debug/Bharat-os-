package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherLanguage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BharatClockWidget(
    language: LauncherLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000)
        }
    }

    val locale = when (language) {
        LauncherLanguage.HINDI -> Locale("hi", "IN")
        LauncherLanguage.BENGALI -> Locale("bn", "IN")
        LauncherLanguage.ENGLISH -> Locale.ENGLISH
    }

    val timeFormat = remember(locale) { SimpleDateFormat("HH:mm", locale) }
    val dateFormat = remember(locale) { SimpleDateFormat("EEEE, d MMMM", locale) }

    val formattedTime = timeFormat.format(currentTime)
    val formattedDate = dateFormat.format(currentTime)

    val shadow = Shadow(
        color = Color.Black.copy(alpha = 0.45f),
        offset = Offset(2f, 2f),
        blurRadius = 8f
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                openClockApp(context)
            }
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .testTag("bharat_clock_widget"),
        horizontalAlignment = Alignment.Start
    ) {
        // Time
        Text(
            text = formattedTime,
            style = TextStyle(
                fontSize = 52.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = (-1).sp,
                shadow = shadow
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Date
        Text(
            text = formattedDate,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.95f),
                shadow = shadow
            )
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Cultural Panchang / Samvat homage
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = when (language) {
                    LauncherLanguage.HINDI -> "विक्रम संवत् २०८३ • भारत"
                    LauncherLanguage.BENGALI -> "বিক্রম সংবৎ ২০৮৩ • ভারত"
                    LauncherLanguage.ENGLISH -> "Vikram Samvat 2083 • Bharat"
                },
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFFFD54F),
                    shadow = shadow
                )
            )
        }
    }
}

private fun openClockApp(context: Context) {
    try {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_DATE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
