package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.BHARAT_WALLPAPERS
import com.example.data.model.LauncherLanguage
import com.example.data.model.PresetWallpaper
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.Localization

@Composable
fun WallpapersScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(LauncherScreen.SETTINGS)
    }

    val settings = uiState.settings
    val language = settings.language

    // Zero-permission Android Photo Picker for picking user wallpaper
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setWallpaper("custom", uri.toString())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("wallpapers_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                    modifier = Modifier.testTag("wallpapers_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getText("wallpapers", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Custom Wallpaper Picker Action Button
            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp)
                    .testTag("pick_custom_wallpaper_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = Localization.getText("custom_wallpaper", language),
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Preset Wallpapers Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // If custom wallpaper exists, show it as first tile
                if (!settings.customWallpaperUri.isNullOrEmpty()) {
                    item {
                        val isSelected = settings.selectedWallpaperId == "custom"
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    viewModel.setWallpaper("custom", settings.customWallpaperUri)
                                }
                                .then(
                                    if (isSelected) Modifier.border(3.dp, Color(0xFFE65100), RoundedCornerShape(18.dp))
                                    else Modifier
                                )
                                .testTag("wallpaper_tile_custom")
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = Uri.parse(settings.customWallpaperUri),
                                    contentDescription = "Custom photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    SelectedBadge(modifier = Modifier.align(Alignment.TopEnd))
                                }
                                Text(
                                    text = "Custom Photo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Curated Bharat Wallpapers
                items(BHARAT_WALLPAPERS) { wallpaper ->
                    val isSelected = settings.selectedWallpaperId == wallpaper.id
                    val title = when (language) {
                        LauncherLanguage.HINDI -> wallpaper.nameHi
                        LauncherLanguage.BENGALI -> wallpaper.nameBn
                        LauncherLanguage.ENGLISH -> wallpaper.nameEn
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                viewModel.setWallpaper(wallpaper.id, null)
                            }
                            .then(
                                if (isSelected) Modifier.border(3.dp, Color(0xFFE65100), RoundedCornerShape(18.dp))
                                else Modifier
                            )
                            .testTag("wallpaper_tile_${wallpaper.id}")
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            WallpaperThumbnail(wallpaper = wallpaper)

                            if (isSelected) {
                                SelectedBadge(modifier = Modifier.align(Alignment.TopEnd))
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperThumbnail(wallpaper: PresetWallpaper, modifier: Modifier = Modifier) {
    val primary = Color(wallpaper.primaryColorHex)
    val secondary = Color(wallpaper.secondaryColorHex)
    val accent = Color(wallpaper.accentColorHex)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(primary, secondary),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.8f, h * 0.25f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.8f, h * 0.25f)
        )
    }
}

@Composable
fun SelectedBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color(0xFFE65100),
        modifier = modifier
            .padding(10.dp)
            .size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
