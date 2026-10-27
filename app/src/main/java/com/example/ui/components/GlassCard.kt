package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.GlassSettings
import com.example.data.model.LauncherTheme

/**
 * Bharat OS 2.0 Liquid Glass Surface.
 * Renders a layered refractive translucent surface with top-left specular rim highlight,
 * inner liquid refraction sheen, and adaptive scroll/reduced-effects optimization.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glassSettings: GlassSettings = GlassSettings(),
    theme: LauncherTheme = LauncherTheme.LIGHT,
    cornerRadius: Dp = glassSettings.cornerRadiusDp.dp,
    borderWidth: Dp = glassSettings.borderWidthDp.dp,
    isScrolling: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val lightweightMode = isScrolling || glassSettings.reducedEffects

    // Refractive Liquid Glass multi-stop surface fill cached per theme/opacity
    val liquidSurfaceBrush = remember(theme, glassSettings.opacity, lightweightMode) {
        val alpha = glassSettings.opacity.coerceIn(0.12f, 0.70f)
        when (theme) {
            LauncherTheme.DARK -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E293B).copy(alpha = (alpha + 0.42f).coerceAtMost(0.88f)),
                    Color(0xFF0F172A).copy(alpha = (alpha + 0.34f).coerceAtMost(0.82f)),
                    Color(0xFF172554).copy(alpha = (alpha + 0.38f).coerceAtMost(0.85f))
                )
            )
            LauncherTheme.SAFFRON_GLOW -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF431407).copy(alpha = (alpha + 0.35f).coerceAtMost(0.84f)),
                    Color(0xFF2A1205).copy(alpha = (alpha + 0.28f).coerceAtMost(0.78f)),
                    Color(0xFF7C2D12).copy(alpha = (alpha + 0.24f).coerceAtMost(0.74f))
                )
            )
            LauncherTheme.FROST_CLEAR -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = (alpha + 0.06f).coerceIn(0.16f, 0.48f)),
                    Color(0xFFE0F2FE).copy(alpha = (alpha * 0.75f).coerceIn(0.12f, 0.38f)),
                    Color.White.copy(alpha = (alpha * 0.90f).coerceIn(0.14f, 0.42f))
                )
            )
            LauncherTheme.LIGHT -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFFBEB).copy(alpha = (alpha + 0.08f).coerceIn(0.18f, 0.52f)),
                    Color.White.copy(alpha = alpha.coerceIn(0.14f, 0.44f)),
                    Color(0xFFFFEDD5).copy(alpha = (alpha * 0.85f).coerceIn(0.12f, 0.40f))
                )
            )
            LauncherTheme.SYSTEM -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFF1E293B).copy(alpha = (alpha + 0.26f).coerceAtMost(0.78f)),
                    Color(0xFF0F172A).copy(alpha = (alpha + 0.20f).coerceAtMost(0.72f))
                )
            )
        }
    }

    // Specular rim border gradient (light hits top-left, refracts bottom-right)
    val specularBorderBrush = remember(theme, glassSettings.borderOpacity) {
        val bAlpha = glassSettings.borderOpacity.coerceIn(0.15f, 0.90f)
        when (theme) {
            LauncherTheme.DARK -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = bAlpha),
                    Color(0xFF38BDF8).copy(alpha = bAlpha * 0.38f),
                    Color.White.copy(alpha = bAlpha * 0.16f),
                    Color(0xFF818CF8).copy(alpha = bAlpha * 0.32f)
                )
            )
            LauncherTheme.SAFFRON_GLOW -> Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFCC80).copy(alpha = bAlpha),
                    Color(0xFFFF9800).copy(alpha = bAlpha * 0.45f),
                    Color.White.copy(alpha = bAlpha * 0.20f),
                    Color(0xFFE65100).copy(alpha = bAlpha * 0.50f)
                )
            )
            else -> Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = (bAlpha + 0.15f).coerceAtMost(0.95f)),
                    Color.White.copy(alpha = bAlpha * 0.28f),
                    Color(0xFFFFE0B2).copy(alpha = bAlpha * 0.42f)
                )
            )
        }
    }

    val topSheenAlpha = if (theme == LauncherTheme.DARK || theme == LauncherTheme.SAFFRON_GLOW) 0.16f else 0.28f

    Box(
        modifier = modifier
            .then(
                if (!lightweightMode) {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.32f),
                        spotColor = Color.Black.copy(alpha = 0.24f)
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(brush = liquidSurfaceBrush, shape = shape)
            .drawWithCache {
                val cornerPx = cornerRadius.toPx()
                val sheenBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (lightweightMode) topSheenAlpha * 0.5f else topSheenAlpha),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = (size.height * 0.48f).coerceAtLeast(36f)
                )
                val innerRimStroke = Stroke(width = 1.dp.toPx())
                val innerRimColor = Color.White.copy(alpha = if (lightweightMode) 0.08f else 0.14f)

                onDrawBehind {
                    // Top liquid refraction sheen
                    drawRoundRect(
                        brush = sheenBrush,
                        cornerRadius = CornerRadius(cornerPx, cornerPx)
                    )
                    // Subtle inner liquid rim highlight
                    if (!lightweightMode && glassSettings.showHighlights) {
                        drawRoundRect(
                            color = innerRimColor,
                            topLeft = Offset(1f, 1f),
                            size = size.copy(
                                width = (size.width - 2f).coerceAtLeast(0f),
                                height = (size.height - 2f).coerceAtLeast(0f)
                            ),
                            cornerRadius = CornerRadius(cornerPx, cornerPx),
                            style = innerRimStroke
                        )
                    }
                }
            }
            .border(width = borderWidth, brush = specularBorderBrush, shape = shape)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        content()
    }
}

@Composable
fun GlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    theme: LauncherTheme = LauncherTheme.LIGHT,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    content: @Composable () -> Unit
) {
    val borderBrush = remember {
        Brush.linearGradient(
            listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.12f))
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                when (theme) {
                    LauncherTheme.DARK -> Color.White.copy(alpha = 0.14f)
                    LauncherTheme.SAFFRON_GLOW -> Color(0xFFFF9800).copy(alpha = 0.22f)
                    else -> Color.White.copy(alpha = 0.24f)
                }
            )
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        content()
    }
}
