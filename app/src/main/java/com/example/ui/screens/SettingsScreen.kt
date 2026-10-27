package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.StudioSheetType
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onRequestDefaultLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateTo(LauncherScreen.HOME)
    }

    val settings = uiState.settings
    val language = settings.language
    val isDefault = uiState.isDefaultLauncher
    val scrollState = rememberScrollState()

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = Localization.getText("reset_layout", language),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = Localization.getText("reset_confirm", language))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetLayout()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("reset_confirm_button")
                ) {
                    Text(
                        text = Localization.getText("reset", language),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = Localization.getText("cancel", language))
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
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
                    onClick = { viewModel.navigateTo(LauncherScreen.HOME) },
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Localization.getText("settings", language),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Default Launcher Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefault) Color(0xFF064E3B).copy(alpha = 0.15f)
                        else Color(0xFFE65100).copy(alpha = 0.12f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onRequestDefaultLauncher)
                        .testTag("settings_default_launcher_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDefault) Color(0xFF15803D) else Color(0xFFE65100),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isDefault) Icons.Default.Check else Icons.Default.Home,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDefault) Localization.getText("default_active", language)
                                else Localization.getText("default_inactive", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDefault) "Tap to change or manage launcher"
                                else Localization.getText("tap_to_set_default", language),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 1. Appearance & Wallpapers Section
                SettingsSection(title = "1. Appearance & Theme") {
                    SettingsRow(
                        icon = Icons.Default.Palette,
                        title = Localization.getText("wallpapers", language),
                        subtitle = "Select curated Indian aesthetic or device photo",
                        tag = "settings_wallpapers_row",
                        onClick = { viewModel.navigateTo(LauncherScreen.WALLPAPERS) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsRow(
                        icon = Icons.Default.Tune,
                        title = "Glass Effects Studio",
                        subtitle = "Theme preset, glass opacity, specular border highlight",
                        tag = "settings_glass_studio_row",
                        onClick = { viewModel.openStudioSheet(StudioSheetType.GLASS) }
                    )
                }

                // 2. Icon Appearance & Shapes
                SettingsSection(title = "2. Icon Appearance Studio") {
                    SettingsRow(
                        icon = Icons.Default.Tune,
                        title = "Icon Shape & Containers",
                        subtitle = "${settings.iconShape.displayName} • ${settings.iconSize.displayName}",
                        tag = "settings_icon_studio_row",
                        onClick = { viewModel.openStudioSheet(StudioSheetType.ICON) }
                    )
                }

                // 3. Typography & Fonts
                SettingsSection(title = "3. Typography Studio") {
                    SettingsRow(
                        icon = Icons.Default.FontDownload,
                        title = "Bundled Font Library",
                        subtitle = "${settings.fontChoice.displayName} (Poppins, Cinzel, Roboto Mono)",
                        tag = "settings_font_studio_row",
                        onClick = { viewModel.openStudioSheet(StudioSheetType.FONT) }
                    )
                }

                // 4. Widgets & Clock Studio
                SettingsSection(title = "4. Widgets & Clock Studio") {
                    SettingsRow(
                        icon = Icons.Default.Widgets,
                        title = "Clock Design & Active Widgets",
                        subtitle = "${settings.clockStyle.displayName} • ${settings.activeWidgets.size} active widgets",
                        tag = "settings_widgets_row",
                        onClick = { viewModel.openStudioSheet(StudioSheetType.WIDGET) }
                    )
                }

                // 5. Grid, Pages & Dock
                SettingsSection(title = "5. Grid & Dock Layout") {
                    SettingsRow(
                        icon = Icons.Default.GridOn,
                        title = "Grid Density & Pages",
                        subtitle = "${settings.gridSize.displayName} • ${settings.homePagesCount} Home Pages • ${settings.dockSlots} Dock Apps",
                        tag = "settings_grid_dock_row",
                        onClick = { viewModel.openStudioSheet(StudioSheetType.GRID) }
                    )
                }

                // 6. Language & Regional
                SettingsSection(title = "6. Language & Regional") {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = Localization.getText("language", language),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            com.example.data.model.LauncherLanguage.values().forEach { lang ->
                                val selected = settings.language == lang
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) Color(0xFFE65100)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.updateLanguage(lang) }
                                        .testTag("lang_button_${lang.code}")
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = lang.displayName,
                                            fontSize = 11.sp,
                                            color = if (selected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. System, Privacy & Reset
                SettingsSection(title = "7. System & Maintenance") {
                    SettingsRow(
                        icon = Icons.Default.BugReport,
                        title = "Bug & Diagnostics Center",
                        subtitle = "Audited issues (P0-P3), heap memory, and live telemetry",
                        tint = Color(0xFF15803D),
                        tag = "settings_bug_center_row",
                        onClick = { viewModel.navigateTo(LauncherScreen.BUG_CENTER) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsRow(
                        icon = Icons.Default.Shield,
                        title = Localization.getText("privacy_title", language),
                        subtitle = "Audited permissions and zero-telemetry policy",
                        tag = "settings_privacy_row",
                        onClick = { viewModel.navigateTo(LauncherScreen.PRIVACY) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = Localization.getText("about", language),
                        subtitle = "Architecture, version, and philosophy",
                        tag = "settings_about_row",
                        onClick = { viewModel.navigateTo(LauncherScreen.ABOUT) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    SettingsRow(
                        icon = Icons.Default.Refresh,
                        title = Localization.getText("reset_layout", language),
                        subtitle = "Restore home shortcuts & dock to defaults",
                        tint = MaterialTheme.colorScheme.error,
                        tag = "settings_reset_row",
                        onClick = { showResetDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    tint: Color = Color(0xFFE65100),
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
    }
}
