package com.example.ui.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherTheme
import com.example.ui.components.GlassCard
import com.example.util.SystemControlsManager

@Composable
fun BharatBatteryWidget(
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    colSpan: Int = 4,
    rowSpan: Int = 1,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val batteryStatus = remember(context) { SystemControlsManager.getBatteryStatus(context) }
    val batteryPct = batteryStatus.levelPercent
    val isCharging = batteryStatus.isCharging

    val maxMemMb = remember { Runtime.getRuntime().maxMemory() / (1024 * 1024) }
    val usedMemMb = remember {
        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    }

    val isCompact = colSpan <= 2
    val isExpanded = rowSpan >= 2

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        onClick = { SystemControlsManager.openBatterySettings(context) },
        modifier = modifier
            .fillMaxWidth()
            .testTag("bharat_battery_widget")
    ) {
        Column(modifier = Modifier.padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = if (isExpanded) 18.dp else 14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = if (isCharging) Color(0xFF34D399) else Color(0xFFFFB74D),
                        modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isCompact) "$batteryPct%" else "Battery: $batteryPct%",
                        fontSize = if (isCompact) 14.sp else 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (!isCompact) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Heap ${usedMemMb}MB / ${maxMemMb}MB",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (batteryPct / 100f).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                batteryPct > 50 -> Color(0xFF34D399)
                                batteryPct > 20 -> Color(0xFFFFB300)
                                else -> Color(0xFFEF4444)
                            }
                        )
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isCharging) "Status: Charging Active" else "Status: Discharging",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Temp: ${batteryStatus.temperatureCelsius}°C",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFCC80)
                    )
                }
            }
        }
    }
}
