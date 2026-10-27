package com.example.ui.components.widgets

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClockStyle
import com.example.data.model.FontChoice
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherTheme
import com.example.ui.components.GlassCard
import com.example.ui.theme.TypeHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BharatClockWidget(
    clockStyle: ClockStyle,
    fontChoice: FontChoice,
    is24Hour: Boolean,
    showSeconds: Boolean,
    language: LauncherLanguage,
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    colSpan: Int = 4,
    rowSpan: Int = 2,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(showSeconds, clockStyle) {
        val interval = if (showSeconds || clockStyle == ClockStyle.ANALOG_GLASS) 1000L else 15000L
        while (true) {
            currentTime = Date()
            delay(interval)
        }
    }

    val locale = remember(language) {
        when (language) {
            LauncherLanguage.HINDI -> Locale("hi", "IN")
            LauncherLanguage.BENGALI -> Locale("bn", "IN")
            LauncherLanguage.ENGLISH -> Locale.ENGLISH
        }
    }

    val isCompactWidth = colSpan <= 2
    val isExpandedHeight = rowSpan >= 3

    val timePattern = if (is24Hour) {
        if (showSeconds && !isCompactWidth) "HH:mm:ss" else "HH:mm"
    } else {
        if (showSeconds && !isCompactWidth) "hh:mm:ss a" else if (isCompactWidth) "hh:mm" else "hh:mm a"
    }

    val timeFormat = remember(locale, timePattern) { SimpleDateFormat(timePattern, locale) }
    val dateFormat = remember(locale, isCompactWidth) {
        SimpleDateFormat(if (isCompactWidth) "EEE, d MMM" else "EEEE, d MMMM yyyy", locale)
    }

    val formattedTime = timeFormat.format(currentTime)
    val formattedDate = dateFormat.format(currentTime)
    val fontFamily = remember(fontChoice) { TypeHelper.getFontFamily(fontChoice) }

    val verticalPadding = when {
        rowSpan <= 1 -> 10.dp
        rowSpan == 2 -> 16.dp
        else -> 24.dp
    }

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        onClick = { openClockApp(context) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("bharat_clock_widget")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isCompactWidth) 12.dp else 20.dp, vertical = verticalPadding)
        ) {
            Column {
                when (clockStyle) {
                    ClockStyle.ANALOG_GLASS -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AnalogClockCanvas(
                                calendar = Calendar.getInstance().apply { time = currentTime },
                                modifier = Modifier.size(if (rowSpan <= 1) 64.dp else if (isExpandedHeight) 110.dp else 88.dp)
                            )
                            if (!isCompactWidth) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formattedTime,
                                        style = TextStyle(
                                            fontSize = if (isExpandedHeight) 28.sp else 22.sp,
                                            fontFamily = fontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formattedDate,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontFamily = fontFamily,
                                            color = Color.White.copy(alpha = 0.9f)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                    ClockStyle.DIGITAL_MINIMAL -> {
                        Column {
                            Text(
                                text = formattedTime,
                                style = TextStyle(
                                    fontSize = when {
                                        isCompactWidth -> 24.sp
                                        rowSpan <= 1 -> 30.sp
                                        isExpandedHeight -> 44.sp
                                        else -> 36.sp
                                    },
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    letterSpacing = (-0.5).sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = formattedDate,
                                style = TextStyle(
                                    fontSize = if (isCompactWidth) 11.sp else 13.sp,
                                    fontFamily = fontFamily,
                                    color = Color.White.copy(alpha = 0.85f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    ClockStyle.SPLIT_TYPOGRAPHY -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = formattedTime,
                                style = TextStyle(
                                    fontSize = if (isCompactWidth) 24.sp else if (rowSpan <= 1) 32.sp else 40.sp,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFB74D)
                                ),
                                maxLines = 1
                            )
                            if (!isCompactWidth) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = when (language) {
                                            LauncherLanguage.HINDI -> "समय"
                                            LauncherLanguage.BENGALI -> "সময়"
                                            LauncherLanguage.ENGLISH -> "IST • India"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(0.65f)
                                    )
                                    Text(
                                        text = formattedDate,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            fontFamily = fontFamily,
                                            color = Color.White.copy(alpha = 0.9f)
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    ClockStyle.DIGITAL_LARGE -> {
                        Column {
                            Text(
                                text = formattedTime,
                                style = TextStyle(
                                    fontSize = when {
                                        isCompactWidth -> 26.sp
                                        rowSpan <= 1 -> 34.sp
                                        isExpandedHeight -> 54.sp
                                        else -> 46.sp
                                    },
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Light,
                                    color = Color.White,
                                    letterSpacing = (-1).sp,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        offset = Offset(2f, 2f),
                                        blurRadius = 6f
                                    )
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formattedDate,
                                style = TextStyle(
                                    fontSize = if (isCompactWidth) 11.sp else 14.sp,
                                    fontFamily = fontFamily,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.95f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // When expanded vertically (rowSpan >= 3), show dual Indian / UTC World Time row
                if (isExpandedHeight && !isCompactWidth) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val istFormat = remember {
                        SimpleDateFormat("HH:mm", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                        }
                    }
                    val utcFormat = remember {
                        SimpleDateFormat("HH:mm", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "IST (New Delhi): ${istFormat.format(currentTime)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFCC80)
                        )
                        Text(
                            text = "UTC: ${utcFormat.format(currentTime)}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalogClockCanvas(calendar: Calendar, modifier: Modifier = Modifier) {
    val seconds = calendar.get(Calendar.SECOND)
    val minutes = calendar.get(Calendar.MINUTE)
    val hours = calendar.get(Calendar.HOUR)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2f, h / 2f)
        val radius = w * 0.46f

        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
        )

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30.0) - 90.0)
            val isMajor = i % 3 == 0
            val tickLen = if (isMajor) 7f else 4f
            val startX = (center.x + (radius - tickLen) * cos(angle)).toFloat()
            val startY = (center.y + (radius - tickLen) * sin(angle)).toFloat()
            val endX = (center.x + radius * cos(angle)).toFloat()
            val endY = (center.y + radius * sin(angle)).toFloat()

            drawLine(
                color = if (isMajor) Color(0xFFFFB74D) else Color.White.copy(0.6f),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (isMajor) 3f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        val hourAngle = Math.toRadians(((hours + minutes / 60.0) * 30.0) - 90.0)
        val hourLen = radius * 0.52f
        drawLine(
            color = Color.White,
            start = center,
            end = Offset((center.x + hourLen * cos(hourAngle)).toFloat(), (center.y + hourLen * sin(hourAngle)).toFloat()),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        val minAngle = Math.toRadians((minutes * 6.0) - 90.0)
        val minLen = radius * 0.72f
        drawLine(
            color = Color(0xFFFFB74D),
            start = center,
            end = Offset((center.x + minLen * cos(minAngle)).toFloat(), (center.y + minLen * sin(minAngle)).toFloat()),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        val secAngle = Math.toRadians((seconds * 6.0) - 90.0)
        val secLen = radius * 0.82f
        drawLine(
            color = Color(0xFFE65100),
            start = center,
            end = Offset((center.x + secLen * cos(secAngle)).toFloat(), (center.y + secLen * sin(secAngle)).toFloat()),
            strokeWidth = 1.2f,
            cap = StrokeCap.Round
        )

        drawCircle(color = Color(0xFFE65100), radius = 3.5f, center = center)
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
