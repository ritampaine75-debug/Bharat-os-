package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherTheme
import com.example.util.BatteryStatus
import com.example.util.MediaSessionTracker
import com.example.util.SystemControlsManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmartIsland(
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    onOpenBugCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }
    var batteryStatus by remember { mutableStateOf(BatteryStatus()) }
    var isTorchActive by remember { mutableStateOf(SystemControlsManager.isTorchActive()) }

    val mediaState by MediaSessionTracker.mediaState.collectAsStateWithLifecycle()

    // Live clock, battery, torch, and real MediaSession polling
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = formatter.format(Date())
            batteryStatus = SystemControlsManager.getBatteryStatus(context)
            isTorchActive = SystemControlsManager.isTorchActive()
            MediaSessionTracker.refreshSessions(context)
            delay(2000L)
        }
    }

    val liquidBorder = remember(glassSettings.borderOpacity) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = glassSettings.borderOpacity.coerceAtLeast(0.38f)),
                Color(0xFFFF9933).copy(alpha = 0.52f),
                Color.White.copy(alpha = 0.16f)
            )
        )
    }

    val surfaceColor = remember(theme) {
        when (theme) {
            LauncherTheme.SAFFRON_GLOW -> Color(0xFF1E1004).copy(alpha = 0.90f)
            LauncherTheme.LIGHT, LauncherTheme.FROST_CLEAR -> Color(0xFF0F172A).copy(alpha = 0.86f)
            LauncherTheme.DARK, LauncherTheme.SYSTEM -> Color(0xFF0A0F1D).copy(alpha = 0.92f)
        }
    }

    val cornerShape = RoundedCornerShape(if (isExpanded) 24.dp else 20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = cornerShape,
            color = surfaceColor,
            modifier = Modifier
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .then(
                    if (!glassSettings.reducedEffects) {
                        Modifier.shadow(elevation = 10.dp, shape = cornerShape)
                    } else {
                        Modifier
                    }
                )
                .border(width = 1.dp, brush = liquidBorder, shape = cornerShape)
                .clip(cornerShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        MediaSessionTracker.refreshSessions(context)
                        isExpanded = !isExpanded
                    }
                )
                .testTag("smart_island_container")
        ) {
            AnimatedContent(
                targetState = isExpanded,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "smart_island_content"
            ) { expanded ->
                if (!expanded) {
                    // Collapsed Liquid Glass Island Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .height(38.dp)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        // Left: Saffron Pip & Live Time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (mediaState.isPlaying) Color(0xFF34D399) else Color(0xFFFF9933)
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentTime.ifEmpty { "Bharat" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center: Real Active Media Track or Idle Equalizer
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.widthIn(max = 150.dp)
                        ) {
                            Icon(
                                imageVector = if (mediaState.isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                contentDescription = "Media Status",
                                tint = if (mediaState.isPlaying) Color(0xFF86EFAC) else Color(0xFFFF9933).copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                            if (mediaState.hasActiveSession && mediaState.trackTitle.isNotBlank()) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = mediaState.trackTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.92f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Right: Battery Percent & Charging Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (batteryStatus.isCharging) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Charging",
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                            Text(
                                text = "${batteryStatus.levelPercent}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (batteryStatus.isCharging) Color(0xFFFACC15) else Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                } else {
                    // Expanded Liquid Glass Island Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.96f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header: Title & Close Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9933))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Bharat Smart Island 2.0",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (batteryStatus.isCharging) {
                                            "Charging (${batteryStatus.levelPercent}%) • ${batteryStatus.temperatureCelsius}°C"
                                        } else {
                                            "Battery ${batteryStatus.levelPercent}% • ${batteryStatus.temperatureCelsius}°C"
                                        },
                                        fontSize = 11.sp,
                                        color = if (batteryStatus.isCharging) Color(0xFFFACC15) else Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (mediaState.activeSessionsCount > 1) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White.copy(alpha = 0.14f),
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { MediaSessionTracker.cycleActiveSession() }
                                            .testTag("island_switch_session_button")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SwapHoriz,
                                                contentDescription = "Switch Media App",
                                                tint = Color(0xFFFFCC80),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${mediaState.selectedSessionIndex + 1}/${mediaState.activeSessionsCount}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { isExpanded = false },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("smart_island_close")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Collapse Island",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Real Media Session Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.09f))
                                .clickable(enabled = mediaState.hasActiveSession) {
                                    MediaSessionTracker.openSourceMediaApp(context)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("island_media_card"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Artwork or Equalizer Emblem
                                if (mediaState.artworkBitmap != null) {
                                    Image(
                                        bitmap = mediaState.artworkBitmap!!,
                                        contentDescription = mediaState.trackTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (mediaState.isPlaying) Color(0xFF15803D).copy(alpha = 0.85f)
                                        else Color(0xFFE65100).copy(alpha = 0.80f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (mediaState.isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    when {
                                        mediaState.hasActiveSession -> {
                                            Text(
                                                text = mediaState.trackTitle.ifBlank { "Active Media Session" },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            val subtitle = buildString {
                                                if (mediaState.artistName.isNotBlank()) append(mediaState.artistName)
                                                if (mediaState.sourceAppLabel.isNotBlank()) {
                                                    if (isNotEmpty()) append(" • ")
                                                    append(mediaState.sourceAppLabel)
                                                }
                                            }.ifBlank { if (mediaState.isPlaying) "Playing" else "Paused" }
                                            Text(
                                                text = subtitle,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.68f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        !mediaState.hasNotificationPermission -> {
                                            Text(
                                                text = "No Active Media Session",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Tap Connect to sync Spotify/YouTube track info",
                                                fontSize = 10.sp,
                                                color = Color(0xFFFFCC80)
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "No Active Media Session",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Start playback in any music or podcast app",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Transport Controls or Permission Action
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (!mediaState.hasNotificationPermission && !mediaState.hasActiveSession) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFE65100),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                MediaSessionTracker.openNotificationListenerSettings(context)
                                            }
                                            .testTag("island_enable_media_access")
                                    ) {
                                        Text(
                                            text = "Connect",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                if (mediaState.hasActiveSession && mediaState.canSkipPrevious) {
                                    IconButton(
                                        onClick = { MediaSessionTracker.skipToPrevious(context) },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.10f))
                                            .testTag("island_media_prev")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipPrevious,
                                            contentDescription = "Previous Track",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { MediaSessionTracker.togglePlayPause(context) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .testTag("island_media_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (mediaState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play/Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                if (mediaState.hasActiveSession && mediaState.canSkipNext) {
                                    IconButton(
                                        onClick = { MediaSessionTracker.skipToNext(context) },
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.10f))
                                            .testTag("island_media_next")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SkipNext,
                                            contentDescription = "Next Track",
                                            tint = Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Control Capsules inside Island
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Flashlight Toggle
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTorchActive) Color(0xFFF59E0B).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isTorchActive = SystemControlsManager.toggleTorch(context)
                                    }
                                    .testTag("island_torch_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isTorchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                        contentDescription = "Torch",
                                        tint = if (isTorchActive) Color(0xFFFDE047) else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isTorchActive) "Torch ON" else "Torch",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            // Wi-Fi
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { SystemControlsManager.openWifiSettings(context) }
                                    .testTag("island_wifi_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wifi,
                                        contentDescription = "Wi-Fi",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Wi-Fi",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            // Diagnostics
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF15803D).copy(alpha = 0.35f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        isExpanded = false
                                        onOpenBugCenter()
                                    }
                                    .testTag("island_bug_center_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport,
                                        contentDescription = "Bug Center",
                                        tint = Color(0xFF86EFAC),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Audit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
