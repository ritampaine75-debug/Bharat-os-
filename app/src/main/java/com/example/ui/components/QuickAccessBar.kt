package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherTheme
import com.example.util.SystemControlsManager

@Composable
fun QuickAccessBar(
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    onOpenBugCenter: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isTorchActive by remember { mutableStateOf(SystemControlsManager.isTorchActive()) }
    val scrollState = rememberScrollState()

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        cornerRadius = 24.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("quick_access_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 10.dp)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Real Flashlight Toggle
            QuickAccessItem(
                icon = if (isTorchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                label = if (isTorchActive) "Torch ON" else "Torch",
                active = isTorchActive,
                activeColor = Color(0xFFF59E0B),
                tag = "quick_torch_button",
                onClick = {
                    isTorchActive = SystemControlsManager.toggleTorch(context)
                }
            )

            // 2. Wi-Fi Panel
            QuickAccessItem(
                icon = Icons.Default.Wifi,
                label = "Wi-Fi",
                tag = "quick_wifi_button",
                onClick = { SystemControlsManager.openWifiSettings(context) }
            )

            // 3. Bluetooth Settings
            QuickAccessItem(
                icon = Icons.Default.Bluetooth,
                label = "Bluetooth",
                tag = "quick_bluetooth_button",
                onClick = { SystemControlsManager.openBluetoothSettings(context) }
            )

            // 4. Sound Settings
            QuickAccessItem(
                icon = Icons.Default.VolumeUp,
                label = "Sound",
                tag = "quick_sound_button",
                onClick = { SystemControlsManager.openSoundSettings(context) }
            )

            // 5. Display / Brightness
            QuickAccessItem(
                icon = Icons.Default.BrightnessMedium,
                label = "Display",
                tag = "quick_display_button",
                onClick = { SystemControlsManager.openDisplaySettings(context) }
            )

            // 6. Battery Saver
            QuickAccessItem(
                icon = Icons.Default.BatterySaver,
                label = "Battery",
                tag = "quick_battery_button",
                onClick = { SystemControlsManager.openBatterySettings(context) }
            )

            // 7. Bug & Performance Center
            QuickAccessItem(
                icon = Icons.Default.BugReport,
                label = "Audit",
                active = true,
                activeColor = Color(0xFF15803D),
                tag = "quick_bug_center_button",
                onClick = onOpenBugCenter
            )

            // 8. Launcher Settings
            QuickAccessItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                tag = "quick_settings_button",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun QuickAccessItem(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    activeColor: Color = Color(0xFFE65100),
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Surface(
            shape = CircleShape,
            color = if (active) activeColor else Color.White.copy(alpha = 0.12f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) Color.White else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}
