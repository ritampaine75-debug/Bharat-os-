package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppInfo
import com.example.data.model.FolderItem
import com.example.data.model.GlassSettings
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherTheme

@Composable
fun FolderItemView(
    folder: FolderItem,
    appsInFolder: List<AppInfo>,
    iconSize: IconSize,
    showLabel: Boolean,
    fontFamily: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconDp = iconSize.dpSize.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("folder_item_${folder.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 2x2 Mini Preview Glass Container
        GlassCard(
            modifier = Modifier.size(iconDp + 4.dp),
            cornerRadius = 20.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                contentAlignment = Alignment.Center
            ) {
                if (appsInFolder.isEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(iconDp * 0.5f)
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            appsInFolder.take(2).forEach { app ->
                                MiniAppIcon(app = app, size = (iconDp * 0.36f))
                            }
                        }
                        if (appsInFolder.size > 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                appsInFolder.drop(2).take(2).forEach { app ->
                                    MiniAppIcon(app = app, size = (iconDp * 0.36f))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLabel) {
            Text(
                text = folder.name,
                color = Color.White,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = if (iconSize == IconSize.COMPACT) 11.sp else 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(iconDp + 26.dp)
            )
        }
    }
}

@Composable
private fun MiniAppIcon(app: AppInfo, size: androidx.compose.ui.unit.Dp) {
    if (app.iconBitmap != null) {
        Image(
            bitmap = app.iconBitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE65100))
        )
    }
}

@Composable
fun FolderDialog(
    folder: FolderItem,
    appsInFolder: List<AppInfo>,
    iconSize: IconSize,
    iconShape: IconShape,
    glassSettings: GlassSettings,
    theme: LauncherTheme,
    fontFamily: FontFamily,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            glassSettings = glassSettings,
            theme = theme,
            cornerRadius = 28.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("folder_dialog_${folder.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Folder Name + Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of apps in folder
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(appsInFolder, key = { it.packageName }) { app ->
                        AppItemView(
                            app = app,
                            iconSize = iconSize,
                            showLabel = true,
                            iconShape = iconShape,
                            labelColor = Color.White,
                            fontFamily = fontFamily,
                            onClick = {
                                onAppClick(app)
                                onDismiss()
                            },
                            onLongClick = {}
                        )
                    }
                }
            }
        }
    }
}
