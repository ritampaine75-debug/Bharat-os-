package com.example.data.repository

import android.appwidget.AppWidgetHost
import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ClockStyle
import com.example.data.model.CulturalGreeting
import com.example.data.model.DEFAULT_WIDGET_ITEMS
import com.example.data.model.FontChoice
import com.example.data.model.GlassSettings
import com.example.data.model.GridSize
import com.example.data.model.IconShape
import com.example.data.model.IconSize
import com.example.data.model.LauncherLanguage
import com.example.data.model.LauncherSettings
import com.example.data.model.LauncherTheme
import com.example.data.model.LauncherWidgetItem
import com.example.data.model.WidgetType
import com.example.util.DiagnosticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LauncherPreferencesRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("bharat_launcher_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    companion object {
        private const val KEY_ACTIVE_WIDGETS_V2 = "active_widgets_v2"
        private const val KEY_ACTIVE_WIDGETS_LEGACY = "active_widgets"
        private const val APPWIDGET_HOST_ID = 1024
    }

    private fun loadSettings(): LauncherSettings {
        val gridSizeStr = prefs.getString("grid_size", GridSize.FOUR_BY_FIVE.name)
        val gridSize = try { GridSize.valueOf(gridSizeStr ?: GridSize.FOUR_BY_FIVE.name) } catch (_: Exception) { GridSize.FOUR_BY_FIVE }

        val iconSizeStr = prefs.getString("icon_size", IconSize.STANDARD.name)
        val iconSize = try { IconSize.valueOf(iconSizeStr ?: IconSize.STANDARD.name) } catch (_: Exception) { IconSize.STANDARD }

        val iconShapeStr = prefs.getString("icon_shape", IconShape.SQUIRCLE.name)
        val iconShape = try { IconShape.valueOf(iconShapeStr ?: IconShape.SQUIRCLE.name) } catch (_: Exception) { IconShape.SQUIRCLE }

        val fontChoiceStr = prefs.getString("font_choice", FontChoice.MODERN_POPPINS.name)
        val fontChoice = try { FontChoice.valueOf(fontChoiceStr ?: FontChoice.MODERN_POPPINS.name) } catch (_: Exception) { FontChoice.MODERN_POPPINS }

        val clockStyleStr = prefs.getString("clock_style", ClockStyle.DIGITAL_LARGE.name)
        val clockStyle = try { ClockStyle.valueOf(clockStyleStr ?: ClockStyle.DIGITAL_LARGE.name) } catch (_: Exception) { ClockStyle.DIGITAL_LARGE }

        val greetingStr = prefs.getString("greeting_choice", CulturalGreeting.NAMASTE.name)
        val greetingChoice = try { CulturalGreeting.valueOf(greetingStr ?: CulturalGreeting.NAMASTE.name) } catch (_: Exception) { CulturalGreeting.NAMASTE }

        val themeStr = prefs.getString("theme", LauncherTheme.LIGHT.name)
        val theme = try { LauncherTheme.valueOf(themeStr ?: LauncherTheme.LIGHT.name) } catch (_: Exception) { LauncherTheme.LIGHT }

        val langStr = prefs.getString("language", LauncherLanguage.ENGLISH.name)
        val lang = try { LauncherLanguage.valueOf(langStr ?: LauncherLanguage.ENGLISH.name) } catch (_: Exception) { LauncherLanguage.ENGLISH }

        val showLabels = prefs.getBoolean("show_labels", true)
        val wallpaperId = prefs.getString("selected_wallpaper_id", "saffron_dawn") ?: "saffron_dawn"
        val customWallpaperUri = prefs.getString("custom_wallpaper_uri", null)
        val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        val enableVoiceSearch = prefs.getBoolean("enable_voice_search", true)

        val glassOpacity = prefs.getFloat("glass_opacity", 0.28f)
        val glassBorderOpacity = prefs.getFloat("glass_border_opacity", 0.45f)
        val glassIconContainers = prefs.getBoolean("glass_icon_containers", false)
        val reducedEffects = prefs.getBoolean("glass_reduced_effects", false)

        val homeShortcutsStr = prefs.getString("home_shortcuts", "") ?: ""
        val homeShortcuts = if (homeShortcutsStr.isNotEmpty()) homeShortcutsStr.split(",").filter { it.isNotBlank() } else emptyList()

        val dockShortcutsStr = prefs.getString("dock_shortcuts", "") ?: ""
        val dockShortcuts = if (dockShortcutsStr.isNotEmpty()) dockShortcutsStr.split(",").filter { it.isNotBlank() } else emptyList()

        val favorites = prefs.getStringSet("favorites", emptySet()) ?: emptySet()
        val homePagesCount = prefs.getInt("home_pages_count", 2).coerceIn(1, 5)
        val is24Hour = prefs.getBoolean("is_24_hour", false)
        val showSeconds = prefs.getBoolean("show_seconds", false)
        val dockSlots = prefs.getInt("dock_slots", 4).coerceIn(3, 6)

        val activeWidgets = loadPersistedWidgets(gridSize.columns)

        return LauncherSettings(
            gridSize = gridSize,
            iconSize = iconSize,
            iconShape = iconShape,
            fontChoice = fontChoice,
            clockStyle = clockStyle,
            greetingChoice = greetingChoice,
            showLabels = showLabels,
            theme = theme,
            language = lang,
            selectedWallpaperId = wallpaperId,
            customWallpaperUri = customWallpaperUri,
            hasCompletedOnboarding = hasCompletedOnboarding,
            homeShortcutPackages = homeShortcuts,
            dockPackages = dockShortcuts,
            favoritePackages = favorites,
            enableVoiceSearch = enableVoiceSearch,
            glassSettings = GlassSettings(
                opacity = glassOpacity,
                borderOpacity = glassBorderOpacity,
                enableGlassIconContainers = glassIconContainers,
                reducedEffects = reducedEffects
            ),
            homePagesCount = homePagesCount,
            activeWidgets = activeWidgets,
            is24HourClock = is24Hour,
            showClockSeconds = showSeconds,
            dockSlots = dockSlots
        )
    }

    /**
     * Loads persisted widgets with full support for:
     * - V2 format: id|type|pageIndex|order|colSpan|rowSpan|appWidgetId;...
     * - Empty list when user intentionally deleted all widgets (never resurrects deleted widgets!)
     * - Legacy format migration
     */
    private fun loadPersistedWidgets(maxCols: Int): List<LauncherWidgetItem> {
        if (prefs.contains(KEY_ACTIVE_WIDGETS_V2)) {
            val raw = prefs.getString(KEY_ACTIVE_WIDGETS_V2, "") ?: ""
            if (raw.isBlank()) {
                // User explicitly deleted all widgets — respect empty state!
                return emptyList()
            }
            return raw.split(";").mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size >= 6) {
                    try {
                        val id = parts[0].trim()
                        val type = WidgetType.valueOf(parts[1].trim())
                        val pageIndex = parts[2].trim().toIntOrNull()?.coerceIn(0, 4) ?: 0
                        val order = parts[3].trim().toIntOrNull() ?: 0
                        val colSpan = (parts[4].trim().toIntOrNull() ?: type.defaultColSpan)
                            .coerceIn(type.minColSpan, maxCols.coerceAtLeast(type.minColSpan))
                        val rowSpan = (parts[5].trim().toIntOrNull() ?: type.defaultRowSpan)
                            .coerceIn(type.minRowSpan, type.maxRowSpan)
                        val appWidgetId = if (parts.size >= 7) parts[6].trim().toIntOrNull() ?: -1 else -1
                        LauncherWidgetItem(
                            id = id,
                            type = type,
                            pageIndex = pageIndex,
                            order = order,
                            colSpan = colSpan,
                            rowSpan = rowSpan,
                            appWidgetId = appWidgetId
                        )
                    } catch (_: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
        }

        if (prefs.contains(KEY_ACTIVE_WIDGETS_LEGACY)) {
            val legacyStr = prefs.getString(KEY_ACTIVE_WIDGETS_LEGACY, "") ?: ""
            if (legacyStr.isBlank()) {
                persistWidgets(emptyList())
                return emptyList()
            }
            val migrated = legacyStr.split(",").mapIndexedNotNull { index, name ->
                try {
                    val type = WidgetType.valueOf(name.trim())
                    val pageIdx = if (type == WidgetType.CLOCK || type == WidgetType.GREETING) 0 else 1
                    LauncherWidgetItem(
                        id = "widget_${type.id}",
                        type = type,
                        pageIndex = pageIdx,
                        order = index,
                        colSpan = type.defaultColSpan.coerceAtMost(maxCols),
                        rowSpan = type.defaultRowSpan
                    )
                } catch (_: Exception) {
                    null
                }
            }
            persistWidgets(migrated)
            return migrated
        }

        // First launch: seed default widgets and persist
        persistWidgets(DEFAULT_WIDGET_ITEMS)
        return DEFAULT_WIDGET_ITEMS
    }

    private fun persistWidgets(widgets: List<LauncherWidgetItem>) {
        val serializedV2 = widgets.joinToString(";") { w ->
            "${w.id}|${w.type.name}|${w.pageIndex}|${w.order}|${w.colSpan}|${w.rowSpan}|${w.appWidgetId}"
        }
        val serializedLegacy = widgets.joinToString(",") { it.type.name }
        prefs.edit()
            .putString(KEY_ACTIVE_WIDGETS_V2, serializedV2)
            .putString(KEY_ACTIVE_WIDGETS_LEGACY, serializedLegacy)
            .commit()
    }

    fun updateGridSize(gridSize: GridSize) {
        // Clamp any active widget colSpan so it never exceeds the new grid column count
        val adjustedWidgets = _settings.value.activeWidgets.map { w ->
            val maxCols = gridSize.columns.coerceAtLeast(w.type.minColSpan)
            if (w.colSpan > maxCols) w.copy(colSpan = maxCols) else w
        }
        prefs.edit().putString("grid_size", gridSize.name).apply()
        persistWidgets(adjustedWidgets)
        _settings.value = _settings.value.copy(gridSize = gridSize, activeWidgets = adjustedWidgets)
    }

    fun updateIconSize(iconSize: IconSize) {
        prefs.edit().putString("icon_size", iconSize.name).apply()
        _settings.value = _settings.value.copy(iconSize = iconSize)
    }

    fun updateIconShape(shape: IconShape) {
        prefs.edit().putString("icon_shape", shape.name).apply()
        _settings.value = _settings.value.copy(iconShape = shape)
    }

    fun updateFontChoice(fontChoice: FontChoice) {
        prefs.edit().putString("font_choice", fontChoice.name).apply()
        _settings.value = _settings.value.copy(fontChoice = fontChoice)
    }

    fun updateClockStyle(clockStyle: ClockStyle) {
        prefs.edit().putString("clock_style", clockStyle.name).apply()
        _settings.value = _settings.value.copy(clockStyle = clockStyle)
    }

    fun updateCulturalGreeting(greeting: CulturalGreeting) {
        prefs.edit().putString("greeting_choice", greeting.name).apply()
        _settings.value = _settings.value.copy(greetingChoice = greeting)
    }

    fun updateShowLabels(showLabels: Boolean) {
        prefs.edit().putBoolean("show_labels", showLabels).apply()
        _settings.value = _settings.value.copy(showLabels = showLabels)
    }

    fun updateTheme(theme: LauncherTheme) {
        prefs.edit().putString("theme", theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateLanguage(lang: LauncherLanguage) {
        prefs.edit().putString("language", lang.name).apply()
        _settings.value = _settings.value.copy(language = lang)
    }

    fun updateWallpaper(wallpaperId: String, customUri: String? = null) {
        prefs.edit()
            .putString("selected_wallpaper_id", wallpaperId)
            .putString("custom_wallpaper_uri", customUri)
            .apply()
        _settings.value = _settings.value.copy(
            selectedWallpaperId = wallpaperId,
            customWallpaperUri = customUri
        )
    }

    fun updateGlassSettings(
        opacity: Float,
        borderOpacity: Float,
        enableGlassContainers: Boolean,
        reducedEffects: Boolean = _settings.value.glassSettings.reducedEffects
    ) {
        prefs.edit()
            .putFloat("glass_opacity", opacity)
            .putFloat("glass_border_opacity", borderOpacity)
            .putBoolean("glass_icon_containers", enableGlassContainers)
            .putBoolean("glass_reduced_effects", reducedEffects)
            .apply()
        _settings.value = _settings.value.copy(
            glassSettings = _settings.value.glassSettings.copy(
                opacity = opacity,
                borderOpacity = borderOpacity,
                enableGlassIconContainers = enableGlassContainers,
                reducedEffects = reducedEffects
            )
        )
    }

    fun updateReducedEffects(reduced: Boolean) {
        val g = _settings.value.glassSettings
        updateGlassSettings(g.opacity, g.borderOpacity, g.enableGlassIconContainers, reduced)
    }

    fun updateHomePagesCount(count: Int) {
        val clamped = count.coerceIn(1, 5)
        prefs.edit().putInt("home_pages_count", clamped).apply()
        // Move any widgets on removed pages onto the last valid page
        val updatedWidgets = _settings.value.activeWidgets.map { w ->
            if (w.pageIndex >= clamped) w.copy(pageIndex = clamped - 1) else w
        }
        persistWidgets(updatedWidgets)
        _settings.value = _settings.value.copy(homePagesCount = clamped, activeWidgets = updatedWidgets)
    }

    fun updateDockSlots(slots: Int) {
        val clamped = slots.coerceIn(3, 6)
        prefs.edit().putInt("dock_slots", clamped).apply()
        _settings.value = _settings.value.copy(dockSlots = clamped)
    }

    fun toggleWidget(type: WidgetType, targetPageIndex: Int? = null) {
        val current = _settings.value.activeWidgets.toMutableList()
        val existingIndex = current.indexOfFirst { it.type == type }
        if (existingIndex >= 0) {
            val removed = current.removeAt(existingIndex)
            cleanupAppWidgetIdIfNecessary(removed.appWidgetId)
            DiagnosticsTracker.recordEvent("WidgetManager", "Removed widget ${removed.id} (${removed.type.displayName})")
        } else {
            val page = targetPageIndex
                ?: if (type == WidgetType.CLOCK || type == WidgetType.GREETING) 0 else 1.coerceAtMost(_settings.value.homePagesCount - 1)
            val maxOrder = current.filter { it.pageIndex == page }.maxOfOrNull { it.order } ?: -1
            val maxCols = _settings.value.gridSize.columns
            val newWidget = LauncherWidgetItem(
                id = "widget_${type.id}",
                type = type,
                pageIndex = page,
                order = maxOrder + 1,
                colSpan = type.defaultColSpan.coerceAtMost(maxCols),
                rowSpan = type.defaultRowSpan
            )
            current.add(newWidget)
            DiagnosticsTracker.recordEvent("WidgetManager", "Added widget ${newWidget.id} (${type.displayName}) on page ${page + 1}")
        }
        persistWidgets(current)
        _settings.value = _settings.value.copy(activeWidgets = current)
    }

    fun removeWidgetById(widgetId: String): Boolean {
        val current = _settings.value.activeWidgets.toMutableList()
        val index = current.indexOfFirst { it.id == widgetId }
        if (index < 0) return false
        val removed = current.removeAt(index)
        cleanupAppWidgetIdIfNecessary(removed.appWidgetId)
        persistWidgets(current)
        _settings.value = _settings.value.copy(activeWidgets = current)
        DiagnosticsTracker.recordEvent("WidgetManager", "Deleted widget ${removed.id} (${removed.type.displayName})")
        return true
    }

    fun resizeWidget(widgetId: String, newColSpan: Int, newRowSpan: Int): Boolean {
        val current = _settings.value.activeWidgets.toMutableList()
        val index = current.indexOfFirst { it.id == widgetId }
        if (index < 0) return false

        val existing = current[index]
        val maxGridCols = _settings.value.gridSize.columns.coerceAtLeast(existing.type.minColSpan)
        val clampedCol = newColSpan.coerceIn(existing.type.minColSpan, maxGridCols.coerceAtMost(existing.type.maxColSpan))
        val clampedRow = newRowSpan.coerceIn(existing.type.minRowSpan, existing.type.maxRowSpan)

        val updated = existing.copy(colSpan = clampedCol, rowSpan = clampedRow)
        current[index] = updated
        persistWidgets(current)
        _settings.value = _settings.value.copy(activeWidgets = current)
        DiagnosticsTracker.recordEvent(
            "WidgetManager",
            "Resized ${existing.type.displayName} to ${clampedCol}×${clampedRow}"
        )
        return true
    }

    fun moveWidgetToPage(widgetId: String, targetPage: Int): Boolean {
        val clampedPage = targetPage.coerceIn(0, (_settings.value.homePagesCount - 1).coerceAtLeast(0))
        val current = _settings.value.activeWidgets.toMutableList()
        val index = current.indexOfFirst { it.id == widgetId }
        if (index < 0) return false

        val maxOrderOnPage = current.filter { it.pageIndex == clampedPage }.maxOfOrNull { it.order } ?: -1
        current[index] = current[index].copy(pageIndex = clampedPage, order = maxOrderOnPage + 1)
        persistWidgets(current)
        _settings.value = _settings.value.copy(activeWidgets = current)
        return true
    }

    fun reorderWidget(widgetId: String, moveUp: Boolean): Boolean {
        val current = _settings.value.activeWidgets.toMutableList()
        val target = current.find { it.id == widgetId } ?: return false
        val pageWidgets = current.filter { it.pageIndex == target.pageIndex }.sortedBy { it.order }.toMutableList()
        val pos = pageWidgets.indexOfFirst { it.id == widgetId }
        if (pos < 0) return false

        val swapPos = if (moveUp) pos - 1 else pos + 1
        if (swapPos !in pageWidgets.indices) return false

        val temp = pageWidgets[pos]
        pageWidgets[pos] = pageWidgets[swapPos]
        pageWidgets[swapPos] = temp

        val reorderedPageMap = pageWidgets.mapIndexed { idx, item -> item.id to idx }.toMap()
        val updatedAll = current.map { w ->
            val newOrder = reorderedPageMap[w.id]
            if (newOrder != null) w.copy(order = newOrder) else w
        }
        persistWidgets(updatedAll)
        _settings.value = _settings.value.copy(activeWidgets = updatedAll)
        return true
    }

    private fun cleanupAppWidgetIdIfNecessary(appWidgetId: Int) {
        if (appWidgetId >= 0) {
            try {
                val host = AppWidgetHost(context, APPWIDGET_HOST_ID)
                host.deleteAppWidgetId(appWidgetId)
            } catch (_: Exception) {}
        }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).apply()
        _settings.value = _settings.value.copy(hasCompletedOnboarding = completed)
    }

    fun toggleFavorite(packageName: String) {
        val current = _settings.value.favoritePackages.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        prefs.edit().putStringSet("favorites", current).apply()
        _settings.value = _settings.value.copy(favoritePackages = current)
    }

    fun addHomeShortcut(packageName: String) {
        val current = _settings.value.homeShortcutPackages.toMutableList()
        if (!current.contains(packageName)) {
            current.add(packageName)
            prefs.edit().putString("home_shortcuts", current.joinToString(",")).apply()
            _settings.value = _settings.value.copy(homeShortcutPackages = current)
        }
    }

    fun removeHomeShortcut(packageName: String) {
        val current = _settings.value.homeShortcutPackages.toMutableList()
        if (current.remove(packageName)) {
            prefs.edit().putString("home_shortcuts", current.joinToString(",")).apply()
            _settings.value = _settings.value.copy(homeShortcutPackages = current)
        }
    }

    fun setHomeShortcuts(packageNames: List<String>) {
        prefs.edit().putString("home_shortcuts", packageNames.joinToString(",")).apply()
        _settings.value = _settings.value.copy(homeShortcutPackages = packageNames)
    }

    fun setDockPackages(packageNames: List<String>) {
        prefs.edit().putString("dock_shortcuts", packageNames.joinToString(",")).apply()
        _settings.value = _settings.value.copy(dockPackages = packageNames)
    }

    fun toggleDockPackage(packageName: String) {
        val current = _settings.value.dockPackages.toMutableList()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else if (current.size < _settings.value.dockSlots) {
            current.add(packageName)
        }
        setDockPackages(current)
    }

    fun resetLayout(defaultHome: List<String>, defaultDock: List<String>) {
        setHomeShortcuts(defaultHome)
        setDockPackages(defaultDock)
        persistWidgets(DEFAULT_WIDGET_ITEMS)
        _settings.value = _settings.value.copy(activeWidgets = DEFAULT_WIDGET_ITEMS)
    }
}
