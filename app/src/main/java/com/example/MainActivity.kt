package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LauncherTheme
import com.example.ui.components.AppContextMenuBottomSheet
import com.example.ui.components.BharatWallpaperView
import com.example.ui.components.FolderDialog
import com.example.ui.components.FontStudioSheet
import com.example.ui.components.GlassStudioSheet
import com.example.ui.components.GridDockStudioSheet
import com.example.ui.components.IconStudioSheet
import com.example.ui.components.WidgetPickerSheet
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AppDrawerScreen
import com.example.ui.screens.BugCenterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WallpapersScreen
import com.example.ui.theme.BharatOSTheme
import com.example.ui.theme.TypeHelper
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.StudioSheetType
import com.example.util.DiagnosticsTracker
import com.example.util.LauncherManager
import com.example.util.MediaSessionTracker

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    private val roleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkDefaultLauncherStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DiagnosticsTracker.markActivityCreateStart()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                DiagnosticsTracker.markFirstFrameDrawn()
            }

            val isDarkTheme = when (uiState.settings.theme) {
                LauncherTheme.LIGHT -> false
                LauncherTheme.DARK, LauncherTheme.SAFFRON_GLOW -> true
                LauncherTheme.FROST_CLEAR -> false
                LauncherTheme.SYSTEM -> isSystemInDarkTheme()
            }

            // Toast trigger
            LaunchedEffect(uiState.toastMessage) {
                uiState.toastMessage?.let { msg ->
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    viewModel.clearToast()
                }
            }

            BharatOSTheme(darkTheme = isDarkTheme) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Wallpaper layer rendered underneath Home, Drawer, and Onboarding
                    if (uiState.currentScreen == LauncherScreen.HOME ||
                        uiState.currentScreen == LauncherScreen.DRAWER ||
                        uiState.currentScreen == LauncherScreen.ONBOARDING
                    ) {
                        BharatWallpaperView(
                            selectedWallpaperId = uiState.settings.selectedWallpaperId,
                            customWallpaperUri = uiState.settings.customWallpaperUri
                        )
                    }

                    // Screen Navigation with smooth fades
                    AnimatedContent(
                        targetState = uiState.currentScreen,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "launcher_screen_transition"
                    ) { screen ->
                        when (screen) {
                            LauncherScreen.ONBOARDING -> {
                                OnboardingScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onRequestDefaultLauncher = {
                                        LauncherManager.requestDefaultLauncherRole(
                                            this@MainActivity,
                                            roleLauncher
                                        )
                                    }
                                )
                            }
                            LauncherScreen.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onRequestDefaultLauncher = {
                                        LauncherManager.requestDefaultLauncherRole(
                                            this@MainActivity,
                                            roleLauncher
                                        )
                                    }
                                )
                            }
                            LauncherScreen.DRAWER -> {
                                AppDrawerScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                            LauncherScreen.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    onRequestDefaultLauncher = {
                                        LauncherManager.requestDefaultLauncherRole(
                                            this@MainActivity,
                                            roleLauncher
                                        )
                                    }
                                )
                            }
                            LauncherScreen.WALLPAPERS -> {
                                WallpapersScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                            LauncherScreen.PRIVACY -> {
                                PrivacyScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                            LauncherScreen.ABOUT -> {
                                AboutScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                            LauncherScreen.BUG_CENTER -> {
                                BugCenterScreen(
                                    uiState = uiState,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }

                    // Context Menu Bottom Sheet
                    uiState.contextMenuApp?.let { app ->
                        val isHomeShortcut = uiState.settings.homeShortcutPackages.contains(app.packageName)
                        val isDockApp = uiState.settings.dockPackages.contains(app.packageName)

                        AppContextMenuBottomSheet(
                            app = app,
                            isHomeShortcut = isHomeShortcut,
                            isDockApp = isDockApp,
                            language = uiState.settings.language,
                            onDismiss = { viewModel.setContextMenuApp(null) },
                            onToggleHome = {
                                if (isHomeShortcut) {
                                    viewModel.removeFromHome(app.packageName)
                                } else {
                                    viewModel.addToHome(app.packageName)
                                }
                            },
                            onToggleDock = {
                                viewModel.toggleDock(app.packageName)
                            },
                            onToggleFavorite = {
                                viewModel.toggleFavorite(app.packageName)
                            },
                            onOpenDetails = {
                                viewModel.openAppDetails(app.packageName)
                            },
                            onUninstall = {
                                viewModel.uninstallApp(app.packageName)
                            }
                        )
                    }

                    // Studio Bottom Sheets triggered from Edit Mode or Settings
                    when (uiState.activeStudioSheet) {
                        StudioSheetType.ICON -> {
                            IconStudioSheet(
                                settings = uiState.settings,
                                onUpdateShape = { viewModel.updateIconShape(it) },
                                onUpdateSize = { viewModel.updateIconSize(it) },
                                onUpdateLabels = { viewModel.updateShowLabels(it) },
                                onUpdateGlassContainers = {
                                    viewModel.updateGlassSettings(
                                        uiState.settings.glassSettings.opacity,
                                        uiState.settings.glassSettings.borderOpacity,
                                        it
                                    )
                                },
                                onDismiss = { viewModel.openStudioSheet(null) }
                            )
                        }
                        StudioSheetType.FONT -> {
                            FontStudioSheet(
                                settings = uiState.settings,
                                onUpdateFont = { viewModel.updateFontChoice(it) },
                                onDismiss = { viewModel.openStudioSheet(null) }
                            )
                        }
                        StudioSheetType.GLASS -> {
                            GlassStudioSheet(
                                settings = uiState.settings,
                                onUpdateTheme = { viewModel.updateTheme(it) },
                                onUpdateOpacity = { opacity ->
                                    viewModel.updateGlassSettings(
                                        opacity,
                                        uiState.settings.glassSettings.borderOpacity,
                                        uiState.settings.glassSettings.enableGlassIconContainers
                                    )
                                },
                                onUpdateBorderOpacity = { borderOpacity ->
                                    viewModel.updateGlassSettings(
                                        uiState.settings.glassSettings.opacity,
                                        borderOpacity,
                                        uiState.settings.glassSettings.enableGlassIconContainers
                                    )
                                },
                                onUpdateReducedEffects = { reduced ->
                                    viewModel.updateReducedEffects(reduced)
                                },
                                onDismiss = { viewModel.openStudioSheet(null) }
                            )
                        }
                        StudioSheetType.WIDGET -> {
                            WidgetPickerSheet(
                                settings = uiState.settings,
                                onToggleWidget = { viewModel.toggleWidget(it) },
                                onUpdateClockStyle = { viewModel.updateClockStyle(it) },
                                onUpdateGreeting = { viewModel.updateCulturalGreeting(it) },
                                onDismiss = { viewModel.openStudioSheet(null) }
                            )
                        }
                        StudioSheetType.GRID -> {
                            GridDockStudioSheet(
                                settings = uiState.settings,
                                onUpdateGridSize = { viewModel.updateGridSize(it) },
                                onUpdateDockSlots = { slots ->
                                    viewModel.updateDockSlots(slots)
                                },
                                onUpdatePagesCount = { viewModel.updateHomePagesCount(it) },
                                onDismiss = { viewModel.openStudioSheet(null) }
                            )
                        }
                        null -> {}
                    }

                    // Folder Dialog
                    uiState.selectedFolder?.let { folder ->
                        val appsInFolder = uiState.allApps.filter { folder.packageNames.contains(it.packageName) }
                        FolderDialog(
                            folder = folder,
                            appsInFolder = appsInFolder,
                            iconSize = uiState.settings.iconSize,
                            iconShape = uiState.settings.iconShape,
                            glassSettings = uiState.settings.glassSettings,
                            theme = uiState.settings.theme,
                            fontFamily = TypeHelper.getFontFamily(uiState.settings.fontChoice),
                            onDismiss = { viewModel.openFolder(null) },
                            onAppClick = { viewModel.openApp(it) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DiagnosticsTracker.attachFrameMetrics(this)
        viewModel.checkDefaultLauncherStatus()
        MediaSessionTracker.refreshSessions(this)
    }

    override fun onPause() {
        DiagnosticsTracker.detachFrameMetrics(this)
        super.onPause()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
    override fun onBackPressed() {
        val state = viewModel.uiState.value
        when {
            state.activeStudioSheet != null -> {
                viewModel.openStudioSheet(null)
            }
            state.isEditMode -> {
                viewModel.setEditMode(false)
            }
            state.selectedFolder != null -> {
                viewModel.openFolder(null)
            }
            state.currentScreen != LauncherScreen.HOME && state.currentScreen != LauncherScreen.ONBOARDING -> {
                viewModel.navigateTo(LauncherScreen.HOME)
            }
        }
    }
}
