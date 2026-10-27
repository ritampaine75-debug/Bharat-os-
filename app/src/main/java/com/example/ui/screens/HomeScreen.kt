package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AppInfo
import com.example.data.model.WidgetType
import com.example.ui.components.AppItemView
import com.example.ui.components.BharatSearchBar
import com.example.ui.components.BharatStatusHeader
import com.example.ui.components.EditModeBar
import com.example.ui.components.GlassCard
import com.example.ui.components.QuickAccessBar
import com.example.ui.components.ResizableWidgetContainer
import com.example.ui.components.SmartIsland
import com.example.ui.components.widgets.BharatBatteryWidget
import com.example.ui.components.widgets.BharatCalendarWidget
import com.example.ui.components.widgets.BharatClockWidget
import com.example.ui.components.widgets.BharatGreetingWidget
import com.example.ui.components.widgets.BharatWeatherWidget
import com.example.ui.theme.TypeHelper
import com.example.ui.viewmodel.LauncherScreen
import com.example.ui.viewmodel.LauncherUiState
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.viewmodel.StudioSheetType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    onRequestDefaultLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = uiState.isEditMode || uiState.searchQuery.isNotEmpty()) {
        if (uiState.isEditMode) {
            viewModel.setEditMode(false)
        } else if (uiState.searchQuery.isNotEmpty()) {
            viewModel.setSearchQuery("")
        }
    }

    val settings = uiState.settings
    val language = settings.language
    val pagerState = rememberPagerState(pageCount = { settings.homePagesCount })
    val fontFamily = remember(settings.fontChoice) { TypeHelper.getFontFamily(settings.fontChoice) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .combinedClickable(
                onClick = {
                    if (uiState.isEditMode && uiState.selectedWidgetIdForEdit != null) {
                        viewModel.selectWidgetForEdit(null)
                    }
                },
                onLongClick = {
                    viewModel.setEditMode(true)
                }
            )
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Status Header & Smart Island
            BharatStatusHeader(
                language = language,
                isDefaultLauncher = uiState.isDefaultLauncher,
                onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) },
                onOpenWallpapers = { viewModel.navigateTo(LauncherScreen.WALLPAPERS) },
                onSetDefaultLauncher = onRequestDefaultLauncher
            )

            SmartIsland(
                glassSettings = settings.glassSettings,
                theme = settings.theme,
                onOpenBugCenter = { viewModel.navigateTo(LauncherScreen.BUG_CENTER) }
            )

            // Center Horizontal Multi-Page Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageWidgets = remember(settings.activeWidgets, page) {
                    settings.activeWidgets
                        .filter { it.pageIndex == page }
                        .sortedBy { it.order }
                }

                val appsPerPage = settings.gridSize.columns * 2
                val pageApps = remember(uiState.homeApps, page, appsPerPage) {
                    val startIndex = page * appsPerPage
                    uiState.homeApps.drop(startIndex).take(appsPerPage)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Resizable, Deletable Liquid Glass Widgets for this Page
                    if (pageWidgets.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (uiState.isEditMode) {
                                        Modifier.verticalScroll(rememberScrollState())
                                    } else {
                                        Modifier
                                    }
                                ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pageWidgets.forEach { widgetItem ->
                                val isSelected = uiState.selectedWidgetIdForEdit == widgetItem.id

                                ResizableWidgetContainer(
                                    widget = widgetItem,
                                    gridColumns = settings.gridSize.columns,
                                    homePagesCount = settings.homePagesCount,
                                    isEditMode = uiState.isEditMode,
                                    isSelectedForEdit = isSelected,
                                    onEnterEditMode = {
                                        viewModel.setEditMode(true, selectedWidgetId = widgetItem.id)
                                    },
                                    onRemoveWidget = {
                                        viewModel.removeWidget(widgetItem.id)
                                    },
                                    onResizeWidget = { newCol, newRow ->
                                        viewModel.resizeWidget(widgetItem.id, newCol, newRow)
                                    },
                                    onReorderWidget = { moveUp ->
                                        viewModel.reorderWidget(widgetItem.id, moveUp)
                                    },
                                    onMoveToNextPage = {
                                        val nextPage = (widgetItem.pageIndex + 1) % settings.homePagesCount
                                        viewModel.moveWidgetToPage(widgetItem.id, nextPage)
                                    }
                                ) { colSpan, rowSpan ->
                                    when (widgetItem.type) {
                                        WidgetType.CLOCK -> {
                                            BharatClockWidget(
                                                clockStyle = settings.clockStyle,
                                                fontChoice = settings.fontChoice,
                                                is24Hour = settings.is24HourClock,
                                                showSeconds = settings.showClockSeconds,
                                                language = language,
                                                glassSettings = settings.glassSettings,
                                                theme = settings.theme,
                                                colSpan = colSpan,
                                                rowSpan = rowSpan
                                            )
                                        }
                                        WidgetType.GREETING -> {
                                            BharatGreetingWidget(
                                                greetingChoice = settings.greetingChoice,
                                                language = language,
                                                glassSettings = settings.glassSettings,
                                                theme = settings.theme,
                                                colSpan = colSpan,
                                                rowSpan = rowSpan
                                            )
                                        }
                                        WidgetType.WEATHER -> {
                                            BharatWeatherWidget(
                                                language = language,
                                                glassSettings = settings.glassSettings,
                                                theme = settings.theme,
                                                colSpan = colSpan,
                                                rowSpan = rowSpan
                                            )
                                        }
                                        WidgetType.BATTERY -> {
                                            BharatBatteryWidget(
                                                glassSettings = settings.glassSettings,
                                                theme = settings.theme,
                                                colSpan = colSpan,
                                                rowSpan = rowSpan
                                            )
                                        }
                                        WidgetType.CALENDAR -> {
                                            BharatCalendarWidget(
                                                language = language,
                                                glassSettings = settings.glassSettings,
                                                theme = settings.theme,
                                                colSpan = colSpan,
                                                rowSpan = rowSpan
                                            )
                                        }
                                        WidgetType.SEARCH -> {
                                            BharatSearchBar(
                                                query = uiState.searchQuery,
                                                onQueryChange = { q ->
                                                    viewModel.setSearchQuery(q)
                                                    if (q.isNotEmpty()) {
                                                        viewModel.navigateTo(LauncherScreen.DRAWER)
                                                    }
                                                },
                                                language = language,
                                                onSearchSubmit = {
                                                    uiState.filteredApps.firstOrNull()?.let { viewModel.openApp(it) }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Home Shortcuts Grid for this Page
                    if (pageApps.isNotEmpty() && !uiState.isEditMode) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(settings.gridSize.columns),
                            contentPadding = PaddingValues(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = pageApps,
                                key = { it.compositeKey },
                                contentType = { "home_app_item" }
                            ) { app ->
                                AppItemView(
                                    app = app,
                                    iconSize = settings.iconSize,
                                    showLabel = settings.showLabels,
                                    iconShape = settings.iconShape,
                                    labelColor = Color.White,
                                    fontFamily = fontFamily,
                                    useGlassContainer = settings.glassSettings.enableGlassIconContainers,
                                    isScrolling = settings.glassSettings.reducedEffects,
                                    onClick = { viewModel.openApp(app) },
                                    onLongClick = { viewModel.setContextMenuApp(app) }
                                )
                            }
                        }
                    }
                }
            }

            // Page Indicator Pills
            if (settings.homePagesCount > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(settings.homePagesCount) { index ->
                        val isCurrent = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (isCurrent) 18.dp else 6.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(if (isCurrent) Color(0xFFE65100) else Color.White.copy(alpha = 0.45f))
                        )
                    }
                }
            }

            // Bottom Section: Search & Dock OR Edit Mode Bar
            if (uiState.isEditMode) {
                EditModeBar(
                    glassSettings = settings.glassSettings,
                    theme = settings.theme,
                    onAddWidget = { viewModel.openStudioSheet(StudioSheetType.WIDGET) },
                    onOpenWallpapers = { viewModel.navigateTo(LauncherScreen.WALLPAPERS) },
                    onOpenIconStudio = { viewModel.openStudioSheet(StudioSheetType.ICON) },
                    onOpenFontStudio = { viewModel.openStudioSheet(StudioSheetType.FONT) },
                    onOpenGridStudio = { viewModel.openStudioSheet(StudioSheetType.GRID) },
                    onOpenGlassStudio = { viewModel.openStudioSheet(StudioSheetType.GLASS) },
                    onAddPage = {
                        if (settings.homePagesCount < 5) {
                            viewModel.updateHomePagesCount(settings.homePagesCount + 1)
                        }
                    },
                    onRemovePage = {
                        if (settings.homePagesCount > 1) {
                            viewModel.updateHomePagesCount(settings.homePagesCount - 1)
                        }
                    },
                    onDone = { viewModel.setEditMode(false) },
                    modifier = Modifier.navigationBarsPadding()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 6.dp)
                ) {
                    // Quick Access Floating Bar
                    QuickAccessBar(
                        glassSettings = settings.glassSettings,
                        theme = settings.theme,
                        onOpenBugCenter = { viewModel.navigateTo(LauncherScreen.BUG_CENTER) },
                        onOpenSettings = { viewModel.navigateTo(LauncherScreen.SETTINGS) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Search Bar
                    BharatSearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = { query ->
                            viewModel.setSearchQuery(query)
                            if (query.isNotEmpty() && uiState.currentScreen != LauncherScreen.DRAWER) {
                                viewModel.navigateTo(LauncherScreen.DRAWER)
                            }
                        },
                        language = language,
                        onSearchSubmit = {
                            val firstMatch = uiState.filteredApps.firstOrNull()
                            if (firstMatch != null) {
                                viewModel.openApp(firstMatch)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Liquid Glass Dock
                    GlassDockView(
                        dockApps = uiState.dockApps,
                        dockSlots = settings.dockSlots,
                        iconSize = settings.iconSize,
                        iconShape = settings.iconShape,
                        glassSettings = settings.glassSettings,
                        theme = settings.theme,
                        onAppClick = { viewModel.openApp(it) },
                        onAppLongClick = { viewModel.setContextMenuApp(it) },
                        onOpenDrawer = { viewModel.navigateTo(LauncherScreen.DRAWER) }
                    )
                }
            }
        }
    }
}

@Composable
fun GlassDockView(
    dockApps: List<AppInfo>,
    dockSlots: Int,
    iconSize: com.example.data.model.IconSize,
    iconShape: com.example.data.model.IconShape,
    glassSettings: com.example.data.model.GlassSettings,
    theme: com.example.data.model.LauncherTheme,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        glassSettings = glassSettings,
        theme = theme,
        cornerRadius = 28.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("launcher_dock")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dockApps.take(dockSlots).forEach { app ->
                AppItemView(
                    app = app,
                    iconSize = iconSize,
                    showLabel = false,
                    iconShape = iconShape,
                    labelColor = Color.White,
                    isScrolling = glassSettings.reducedEffects,
                    onClick = { onAppClick(app) },
                    onLongClick = { onAppLongClick(app) }
                )
            }

            // All Apps Drawer Toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onOpenDrawer)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("dock_drawer_toggle")
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE65100).copy(alpha = 0.95f),
                    modifier = Modifier.size(iconSize.dpSize.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "All Apps",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
