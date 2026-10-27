package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherWidgetItem

private val EditFrameBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF9800),
        Color(0xFFFFCC80),
        Color(0xFFE65100)
    )
)

/**
 * Interactive Liquid Glass Widget wrapper that supports:
 * - Long-press to enter Edit Mode and select widget
 * - Real-time horizontal (colSpan) and vertical (rowSpan) resizing via drag handles & step controls
 * - Immediate widget deletion ("Remove") without affecting installed applications
 * - Reordering and page migration
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResizableWidgetContainer(
    widget: LauncherWidgetItem,
    gridColumns: Int,
    homePagesCount: Int,
    isEditMode: Boolean,
    isSelectedForEdit: Boolean,
    onEnterEditMode: () -> Unit,
    onRemoveWidget: () -> Unit,
    onResizeWidget: (newColSpan: Int, newRowSpan: Int) -> Unit,
    onReorderWidget: (moveUp: Boolean) -> Unit,
    onMoveToNextPage: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (colSpan: Int, rowSpan: Int) -> Unit
) {
    val safeMaxCols = gridColumns.coerceAtLeast(widget.type.minColSpan)
    val effectiveColSpan = widget.colSpan.coerceIn(widget.type.minColSpan, safeMaxCols)
    val effectiveRowSpan = widget.rowSpan.coerceIn(widget.type.minRowSpan, widget.type.maxRowSpan)

    val widthFraction = (effectiveColSpan.toFloat() / safeMaxCols.toFloat()).coerceIn(0.48f, 1.0f)
    val minHeightDp = when (effectiveRowSpan) {
        1 -> 74.dp
        2 -> 132.dp
        else -> 188.dp
    }

    var dragAccumX by remember(widget.id, effectiveColSpan) { mutableFloatStateOf(0f) }
    var dragAccumY by remember(widget.id, effectiveRowSpan) { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .then(
                    if (isEditMode) {
                        Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = if (isSelectedForEdit) 2.dp else 1.2.dp,
                                brush = EditFrameBorderBrush,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(Color(0xFF0F172A).copy(alpha = 0.28f))
                            .padding(6.dp)
                    } else {
                        Modifier
                    }
                )
                .combinedClickable(
                    onClick = {
                        if (isEditMode) {
                            onEnterEditMode()
                        }
                    },
                    onLongClick = onEnterEditMode
                )
                .testTag("widget_container_${widget.id}")
        ) {
            // Edit Mode Header Bar (Remove button + Grid Span indicator)
            if (isEditMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE65100).copy(alpha = 0.85f)
                        ) {
                            Text(
                                text = "${effectiveColSpan}×${effectiveRowSpan}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("widget_size_badge_${widget.id}")
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = widget.type.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    // Remove Widget Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.90f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onRemoveWidget)
                            .testTag("remove_widget_${widget.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Remove Widget",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Remove",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Widget Content Area with Corner Drag Resize Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeightDp)
            ) {
                content(effectiveColSpan, effectiveRowSpan)

                // Interactive Corner Drag Handle for fluid finger resizing in Edit Mode
                if (isEditMode) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFF9800),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(26.dp)
                            .pointerInput(widget.id, effectiveColSpan, effectiveRowSpan, safeMaxCols) {
                                detectDragGestures(
                                    onDragEnd = {
                                        dragAccumX = 0f
                                        dragAccumY = 0f
                                    },
                                    onDragCancel = {
                                        dragAccumX = 0f
                                        dragAccumY = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    dragAccumX += dragAmount.x
                                    dragAccumY += dragAmount.y
                                    val stepThresholdPx = 56f

                                    var targetCol = effectiveColSpan
                                    var targetRow = effectiveRowSpan

                                    if (dragAccumX >= stepThresholdPx) {
                                        targetCol = (effectiveColSpan + 1).coerceAtMost(safeMaxCols)
                                        dragAccumX = 0f
                                    } else if (dragAccumX <= -stepThresholdPx) {
                                        targetCol = (effectiveColSpan - 1).coerceAtLeast(widget.type.minColSpan)
                                        dragAccumX = 0f
                                    }

                                    if (dragAccumY >= stepThresholdPx) {
                                        targetRow = (effectiveRowSpan + 1).coerceAtMost(widget.type.maxRowSpan)
                                        dragAccumY = 0f
                                    } else if (dragAccumY <= -stepThresholdPx) {
                                        targetRow = (effectiveRowSpan - 1).coerceAtLeast(widget.type.minRowSpan)
                                        dragAccumY = 0f
                                    }

                                    if (targetCol != effectiveColSpan || targetRow != effectiveRowSpan) {
                                        onResizeWidget(targetCol, targetRow)
                                    }
                                }
                            }
                            .testTag("resize_handle_${widget.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = "Drag to Resize Widget",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Precision Grid Resize & Position Controls in Edit Mode
            if (isEditMode) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    // Width (Columns) Step Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ResizeChipButton(
                            label = "W-",
                            enabled = effectiveColSpan > widget.type.minColSpan,
                            tag = "widget_col_dec_${widget.id}",
                            onClick = { onResizeWidget(effectiveColSpan - 1, effectiveRowSpan) }
                        )
                        ResizeChipButton(
                            label = "W+",
                            enabled = effectiveColSpan < safeMaxCols.coerceAtMost(widget.type.maxColSpan),
                            tag = "widget_col_inc_${widget.id}",
                            onClick = { onResizeWidget(effectiveColSpan + 1, effectiveRowSpan) }
                        )

                        if (widget.type.maxRowSpan > widget.type.minRowSpan) {
                            ResizeChipButton(
                                label = "H-",
                                enabled = effectiveRowSpan > widget.type.minRowSpan,
                                tag = "widget_row_dec_${widget.id}",
                                onClick = { onResizeWidget(effectiveColSpan, effectiveRowSpan - 1) }
                            )
                            ResizeChipButton(
                                label = "H+",
                                enabled = effectiveRowSpan < widget.type.maxRowSpan,
                                tag = "widget_row_inc_${widget.id}",
                                onClick = { onResizeWidget(effectiveColSpan, effectiveRowSpan + 1) }
                            )
                        }
                    }

                    // Order & Page Move Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReorderWidget(true) }
                                .testTag("widget_move_up_${widget.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(14.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReorderWidget(false) }
                                .testTag("widget_move_down_${widget.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(14.dp)
                            )
                        }

                        if (homePagesCount > 1) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onMoveToNextPage() }
                                    .testTag("widget_move_page_${widget.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = "Move Page",
                                        tint = Color(0xFFFFCC80),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "P${widget.pageIndex + 1}",
                                        fontSize = 10.sp,
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

@Composable
private fun ResizeChipButton(
    label: String,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) Color(0xFFE65100).copy(alpha = 0.80f) else Color.White.copy(alpha = 0.08f),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.38f),
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}
