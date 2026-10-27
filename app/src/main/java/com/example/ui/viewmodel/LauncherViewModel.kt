package com.example.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.data.model.ClockStyle
import com.example.data.model.CulturalGreeting
import com.example.data.model.FolderItem
import com.example.data.model.FontChoice
import com.example.data.model.GridSize
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherSettings
import com.example.data.model.LauncherTheme
import com.example.data.model.WidgetType
import com.example.data.repository.AppRepository
import com.example.data.repository.LauncherPreferencesRepository
import com.example.util.AppSearchRanker
import com.example.util.DiagnosticsTracker
import com.example.util.LauncherManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LauncherScreen {
    HOME,
    DRAWER,
    SETTINGS,
    ONBOARDING,
    WALLPAPERS,
    PRIVACY,
    ABOUT,
    BUG_CENTER
}

enum class StudioSheetType {
    ICON,
    FONT,
    GLASS,
    WIDGET,
    GRID
}

@Immutable
data class LauncherUiState(
    val isLoading: Boolean = true,
    val allApps: List<AppInfo> = emptyList(),
    val homeApps: List<AppInfo> = emptyList(),
    val dockApps: List<AppInfo> = emptyList(),
    val searchQuery: String = "",
    val filteredApps: List<AppInfo> = emptyList(),
    val selectedCategory: AppCategory = AppCategory.ALL,
    val currentScreen: LauncherScreen = LauncherScreen.HOME,
    val settings: LauncherSettings = LauncherSettings(),
    val isDefaultLauncher: Boolean = false,
    val contextMenuApp: AppInfo? = null,
    val toastMessage: String? = null,
    val isEditMode: Boolean = false,
    val selectedWidgetIdForEdit: String? = null,
    val activeStudioSheet: StudioSheetType? = null,
    val selectedFolder: FolderItem? = null,
    val drawerScrollIndex: Int = 0,
    val drawerScrollOffset: Int = 0
)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appRepository = AppRepository(application)
    private val preferencesRepository = LauncherPreferencesRepository(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    private var searchFilterJob: Job? = null

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            appRepository.invalidatePackageCache(pkg)
            refreshInstalledApps()
        }
    }

    init {
        viewModelScope.launch {
            preferencesRepository.settings.collectLatest { settings ->
                _uiState.update { current ->
                    val updatedAllApps = if (current.allApps.isNotEmpty()) {
                        current.allApps.map { app ->
                            val fav = settings.favoritePackages.contains(app.packageName)
                            if (app.isFavorite != fav) app.copy(isFavorite = fav) else app
                        }
                    } else {
                        current.allApps
                    }
                    current.copy(settings = settings, allApps = updatedAllApps)
                }
                syncAppLists()
            }
        }
        registerPackageReceiver()
        checkDefaultLauncherStatus()
        refreshInstalledApps()
    }

    private fun registerPackageReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                app.registerReceiver(packageChangeReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                app.registerReceiver(packageChangeReceiver, filter)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(packageChangeReceiver)
        } catch (_: Exception) {}
    }

    fun checkDefaultLauncherStatus() {
        val isDefault = LauncherManager.isDefaultLauncher(getApplication())
        _uiState.update { current ->
            val screen = if (!current.settings.hasCompletedOnboarding && !isDefault) {
                LauncherScreen.ONBOARDING
            } else {
                current.currentScreen
            }
            current.copy(isDefaultLauncher = isDefault, currentScreen = screen)
        }
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            if (_uiState.value.allApps.isEmpty()) {
                _uiState.update { it.copy(isLoading = true) }
            }
            val apps = appRepository.getInstalledApps(_uiState.value.settings.favoritePackages)
            _uiState.update { it.copy(allApps = apps, isLoading = false) }

            // If home shortcuts are empty and onboarding was never done, seed default shortcuts
            if (_uiState.value.settings.homeShortcutPackages.isEmpty() && apps.isNotEmpty()) {
                val seedHome = apps.take(12).map { it.packageName }
                val seedDock = apps.filter { it.category == AppCategory.COMMUNICATION || it.category == AppCategory.TOOLS }
                    .take(4)
                    .map { it.packageName }
                    .ifEmpty { apps.take(4).map { it.packageName } }

                preferencesRepository.setHomeShortcuts(seedHome)
                preferencesRepository.setDockPackages(seedDock)
            } else {
                syncAppLists()
            }
        }
    }

    private fun syncAppLists() {
        val state = _uiState.value
        val appMap = state.allApps.associateBy { it.packageName }

        val homeList = state.settings.homeShortcutPackages.mapNotNull { appMap[it] }
        val dockList = state.settings.dockPackages.mapNotNull { appMap[it] }

        _uiState.update { it.copy(homeApps = homeList, dockApps = dockList) }
        filterAppsAsync()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        filterAppsAsync()
    }

    fun selectCategory(category: AppCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        filterAppsAsync()
    }

    private fun filterAppsAsync() {
        val snapshot = _uiState.value
        val apps = snapshot.allApps
        val query = snapshot.searchQuery
        val category = snapshot.selectedCategory
        val favorites = snapshot.settings.favoritePackages

        searchFilterJob?.cancel()
        searchFilterJob = viewModelScope.launch {
            val ranked = withContext(Dispatchers.Default) {
                AppSearchRanker.filterAndRank(
                    apps = apps,
                    rawQuery = query,
                    selectedCategory = category,
                    favoritePackages = favorites
                )
            }
            _uiState.update { it.copy(filteredApps = ranked) }
        }
    }

    fun saveDrawerScrollPosition(index: Int, offset: Int) {
        _uiState.update {
            if (it.drawerScrollIndex == index && it.drawerScrollOffset == offset) it
            else it.copy(drawerScrollIndex = index, drawerScrollOffset = offset)
        }
    }

    fun navigateTo(screen: LauncherScreen) {
        _uiState.update {
            it.copy(
                currentScreen = screen,
                isEditMode = false,
                selectedWidgetIdForEdit = null
            )
        }
    }

    fun setEditMode(enabled: Boolean, selectedWidgetId: String? = null) {
        _uiState.update {
            it.copy(
                isEditMode = enabled,
                selectedWidgetIdForEdit = if (enabled) selectedWidgetId else null
            )
        }
    }

    fun selectWidgetForEdit(widgetId: String?) {
        _uiState.update {
            it.copy(
                isEditMode = widgetId != null || it.isEditMode,
                selectedWidgetIdForEdit = widgetId
            )
        }
    }

    fun openStudioSheet(type: StudioSheetType?) {
        _uiState.update { it.copy(activeStudioSheet = type) }
    }

    fun openFolder(folder: FolderItem?) {
        _uiState.update { it.copy(selectedFolder = folder) }
    }

    fun openApp(app: AppInfo) {
        val launched = appRepository.launchApp(app.packageName, app.activityName)
        if (!launched) {
            _uiState.update { it.copy(toastMessage = "Unable to launch ${app.label}") }
        }
    }

    fun setContextMenuApp(app: AppInfo?) {
        _uiState.update { it.copy(contextMenuApp = app) }
    }

    fun toggleFavorite(packageName: String) {
        preferencesRepository.toggleFavorite(packageName)
        // Preferences collector updates in-memory allApps & filteredApps without re-querying PackageManager
    }

    fun addToHome(packageName: String) {
        preferencesRepository.addHomeShortcut(packageName)
    }

    fun removeFromHome(packageName: String) {
        preferencesRepository.removeHomeShortcut(packageName)
    }

    fun toggleDock(packageName: String) {
        preferencesRepository.toggleDockPackage(packageName)
    }

    fun openAppDetails(packageName: String) {
        appRepository.openAppDetails(packageName)
    }

    fun uninstallApp(packageName: String) {
        appRepository.uninstallApp(packageName)
    }

    fun updateGridSize(gridSize: GridSize) {
        preferencesRepository.updateGridSize(gridSize)
    }

    fun updateIconSize(iconSize: IconSize) {
        preferencesRepository.updateIconSize(iconSize)
    }

    fun updateIconShape(shape: IconShape) {
        preferencesRepository.updateIconShape(shape)
    }

    fun updateFontChoice(font: FontChoice) {
        preferencesRepository.updateFontChoice(font)
    }

    fun updateClockStyle(clockStyle: ClockStyle) {
        preferencesRepository.updateClockStyle(clockStyle)
    }

    fun updateCulturalGreeting(greeting: CulturalGreeting) {
        preferencesRepository.updateCulturalGreeting(greeting)
    }

    fun updateShowLabels(show: Boolean) {
        preferencesRepository.updateShowLabels(show)
    }

    fun updateTheme(theme: LauncherTheme) {
        preferencesRepository.updateTheme(theme)
    }

    fun updateLanguage(lang: LauncherLanguage) {
        preferencesRepository.updateLanguage(lang)
    }

    fun setWallpaper(wallpaperId: String, customUri: String? = null) {
        preferencesRepository.updateWallpaper(wallpaperId, customUri)
    }

    fun updateGlassSettings(
        opacity: Float,
        borderOpacity: Float,
        enableContainers: Boolean,
        reducedEffects: Boolean = _uiState.value.settings.glassSettings.reducedEffects
    ) {
        preferencesRepository.updateGlassSettings(opacity, borderOpacity, enableContainers, reducedEffects)
    }

    fun updateReducedEffects(reduced: Boolean) {
        preferencesRepository.updateReducedEffects(reduced)
    }

    fun updateHomePagesCount(count: Int) {
        preferencesRepository.updateHomePagesCount(count)
    }

    fun updateDockSlots(slots: Int) {
        preferencesRepository.updateDockSlots(slots)
    }

    fun toggleWidget(type: WidgetType, targetPageIndex: Int? = null) {
        preferencesRepository.toggleWidget(type, targetPageIndex)
    }

    fun removeWidget(widgetId: String) {
        val removed = preferencesRepository.removeWidgetById(widgetId)
        if (removed) {
            _uiState.update { state ->
                state.copy(
                    selectedWidgetIdForEdit = if (state.selectedWidgetIdForEdit == widgetId) null else state.selectedWidgetIdForEdit,
                    toastMessage = "Widget removed from Home"
                )
            }
        }
    }

    fun resizeWidget(widgetId: String, colSpan: Int, rowSpan: Int) {
        preferencesRepository.resizeWidget(widgetId, colSpan, rowSpan)
    }

    fun moveWidgetToPage(widgetId: String, targetPage: Int) {
        preferencesRepository.moveWidgetToPage(widgetId, targetPage)
    }

    fun reorderWidget(widgetId: String, moveUp: Boolean) {
        preferencesRepository.reorderWidget(widgetId, moveUp)
    }

    fun completeOnboarding() {
        preferencesRepository.setOnboardingCompleted(true)
        _uiState.update { it.copy(currentScreen = LauncherScreen.HOME) }
    }

    fun resetLayout() {
        val apps = _uiState.value.allApps
        val defaultHome = apps.take(12).map { it.packageName }
        val defaultDock = apps.filter { it.category == AppCategory.COMMUNICATION || it.category == AppCategory.TOOLS }
            .take(4)
            .map { it.packageName }
            .ifEmpty { apps.take(4).map { it.packageName } }
        preferencesRepository.resetLayout(defaultHome, defaultDock)
        DiagnosticsTracker.recordEvent("Layout", "Home layout and widgets reset to defaults")
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
