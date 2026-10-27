package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppInfo
import com.example.data.model.IconShape
import com.example.data.model.IconSize

private val LiquidIconContainerShape = RoundedCornerShape(20.dp)
private val LiquidIconBorderBrush = Brush.linearGradient(
    listOf(
        Color.White.copy(alpha = 0.62f),
        Color(0xFFFFCC80).copy(alpha = 0.25f),
        Color.White.copy(alpha = 0.12f)
    )
)
private val LiquidIconFillBrush = Brush.verticalGradient(
    listOf(
        Color.White.copy(alpha = 0.24f),
        Color.White.copy(alpha = 0.10f)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItemView(
    app: AppInfo,
    iconSize: IconSize,
    showLabel: Boolean,
    iconShape: IconShape = IconShape.SQUIRCLE,
    labelColor: Color = Color.White,
    fontFamily: FontFamily = FontFamily.SansSerif,
    useGlassContainer: Boolean = false,
    isScrolling: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconDp = iconSize.dpSize.dp

    val shape: Shape = remember(iconShape) {
        when (iconShape) {
            IconShape.CIRCLE -> CircleShape
            IconShape.SQUIRCLE -> RoundedCornerShape(percent = 35)
            IconShape.ROUNDED_SQUARE -> RoundedCornerShape(percent = 22)
            IconShape.EXTRA_ROUNDED -> RoundedCornerShape(percent = 42)
            IconShape.ORIGINAL -> RoundedCornerShape(12.dp)
        }
    }

    // Use pre-decoded ImageBitmap from AppRepository; fallback remembered if AppInfo was constructed in tests
    val imageBitmap = app.iconImageBitmap ?: remember(app.compositeKey, app.iconBitmap) {
        app.iconBitmap?.asImageBitmap()
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .testTag("app_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(if (useGlassContainer) iconDp + 14.dp else iconDp)
                .then(
                    if (useGlassContainer) {
                        Modifier
                            .clip(LiquidIconContainerShape)
                            .background(brush = LiquidIconFillBrush, shape = LiquidIconContainerShape)
                            .border(1.dp, LiquidIconBorderBrush, LiquidIconContainerShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (imageBitmap != null) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Crop,
                    filterQuality = if (isScrolling) FilterQuality.Low else FilterQuality.Medium,
                    modifier = Modifier
                        .size(iconDp)
                        .then(
                            if (!isScrolling && !useGlassContainer) {
                                Modifier.shadow(2.dp, shape, clip = false)
                            } else {
                                Modifier
                            }
                        )
                        .clip(shape)
                )
            } else {
                Surface(
                    shape = shape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(iconDp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = app.label,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(iconDp * 0.58f)
                        )
                    }
                }
            }
        }

        if (showLabel) {
            Text(
                text = app.label,
                color = labelColor,
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
