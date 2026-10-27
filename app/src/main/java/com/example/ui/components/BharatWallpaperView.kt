package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.model.BHARAT_WALLPAPERS

@Composable
fun BharatWallpaperView(
    selectedWallpaperId: String,
    customWallpaperUri: String?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (!customWallpaperUri.isNullOrEmpty()) {
            AsyncImage(
                model = Uri.parse(customWallpaperUri),
                contentDescription = "Custom Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Subtle scrim for icon contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )
        } else {
            val wallpaper = BHARAT_WALLPAPERS.find { it.id == selectedWallpaperId }
                ?: BHARAT_WALLPAPERS[0]

            val primary = Color(wallpaper.primaryColorHex)
            val secondary = Color(wallpaper.secondaryColorHex)
            val accent = Color(wallpaper.accentColorHex)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Diagonal elegant gradient
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(primary, secondary),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    )
                )

                // Subtle radial glow near top right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.2f),
                        radius = w * 0.75f
                    ),
                    radius = w * 0.75f,
                    center = Offset(w * 0.85f, h * 0.2f)
                )

                // Atmospheric secondary glow near bottom left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(w * 0.15f, h * 0.85f),
                        radius = w * 0.65f
                    ),
                    radius = w * 0.65f,
                    center = Offset(w * 0.15f, h * 0.85f)
                )

                // Fine watermark ring (Ashoka Chakra motif homage)
                drawCircle(
                    color = accent.copy(alpha = 0.06f),
                    radius = w * 0.45f,
                    center = Offset(w * 0.5f, h * 0.35f),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = accent.copy(alpha = 0.04f),
                    radius = w * 0.40f,
                    center = Offset(w * 0.5f, h * 0.35f),
                    style = Stroke(width = 1.5f)
                )
            }
        }
    }
}
