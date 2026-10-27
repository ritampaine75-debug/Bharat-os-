package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherTheme

@Composable
fun EditModeBar(
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    onAddWidget: () -> Unit,
    onOpenWallpapers: () -> Unit,
    onOpenIconStudio: () -> Unit,
    onOpenFontStudio: () -> Unit,
    onOpenGridStudio: () -> Unit,
    onOpenGlassStudio: () -> Unit,
    onAddPage: () -> Unit,
    onRemovePage: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        cornerRadius = 24.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("edit_mode_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Title + Done Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Customize Home",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap tools below to edit layout & appearance",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    modifier = Modifier.height(38.dp).testTag("edit_mode_done_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Done", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Items Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EditActionPill(
                    icon = Icons.Default.Widgets,
                    label = "Widgets",
                    onClick = onAddWidget,
                    tag = "edit_action_widgets"
                )
                EditActionPill(
                    icon = Icons.Default.Palette,
                    label = "Wallpapers",
                    onClick = onOpenWallpapers,
                    tag = "edit_action_wallpapers"
                )
                EditActionPill(
                    icon = Icons.Default.Tune,
                    label = "Icon Studio",
                    onClick = onOpenIconStudio,
                    tag = "edit_action_icons"
                )
                EditActionPill(
                    icon = Icons.Default.FontDownload,
                    label = "Typography",
                    onClick = onOpenFontStudio,
                    tag = "edit_action_fonts"
                )
                EditActionPill(
                    icon = Icons.Default.GridOn,
                    label = "Grid & Dock",
                    onClick = onOpenGridStudio,
                    tag = "edit_action_grid"
                )
                EditActionPill(
                    icon = Icons.Default.Palette,
                    label = "Glass Theme",
                    onClick = onOpenGlassStudio,
                    tag = "edit_action_glass"
                )
                EditActionPill(
                    icon = Icons.Default.Add,
                    label = "+ Page",
                    onClick = onAddPage,
                    tag = "edit_action_add_page"
                )
                EditActionPill(
                    icon = Icons.Default.Remove,
                    label = "- Page",
                    onClick = onRemovePage,
                    tag = "edit_action_remove_page"
                )
            }
        }
    }
}

@Composable
private fun EditActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.16f),
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
