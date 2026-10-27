package com.example.data.model

import androidx.compose.runtime.Immutable
import com.example.R

enum class GridSize(val columns: Int, val rows: Int, val displayName: String) {
    THREE_BY_FOUR(3, 4, "3 × 4 (Spacious)"),
    FOUR_BY_FOUR(4, 4, "4 × 4"),
    FOUR_BY_FIVE(4, 5, "4 × 5 (Standard)"),
    FIVE_BY_FIVE(5, 5, "5 × 5 (Dense)"),
    FIVE_BY_SIX(5, 6, "5 × 6 (Compact)")
}

enum class IconSize(val dpSize: Int, val displayName: String) {
    COMPACT(44, "Compact (44dp)"),
    STANDARD(54, "Standard (54dp)"),
    LARGE(64, "Large (64dp)")
}

enum class IconShape(val id: String, val displayName: String, val cornerPercent: Int) {
    CIRCLE("circle", "Circle", 50),
    SQUIRCLE("squircle", "Squircle", 35),
    ROUNDED_SQUARE("rounded_square", "Rounded", 22),
    EXTRA_ROUNDED("extra_rounded", "Pebble", 40),
    ORIGINAL("original", "Original", 0)
}

enum class FontChoice(val id: String, val displayName: String, val fontResId: Int?) {
    MODERN_POPPINS("poppins", "Poppins (Modern)", R.font.poppins),
    ELEGANT_CINZEL("cinzel", "Cinzel (Heritage)", R.font.cinzel),
    DIGITAL_MONO("roboto_mono", "Roboto Mono (Digital)", R.font.roboto_mono),
    SYSTEM_SANS("system", "System Default", null)
}

enum class CulturalGreeting(
    val id: String,
    val hindiText: String,
    val bengaliText: String,
    val englishText: String
) {
    NAMASTE("namaste", "नमस्ते", "নমস্কার", "Namaste"),
    JAI_SHRI_RAM("jai_shri_ram", "जय श्री राम", "জয় শ্রী রাম", "Jai Shri Ram"),
    JAI_SHRI_KRISHNA("jai_shri_krishna", "जय श्री कृष्ण", "জয় শ্রী কৃষ্ণ", "Jai Shri Krishna"),
    NEUTRAL("neutral", "सुप्रभात / शुभ दिन", "শুভ সকাল / দিন", "Good Day")
}

enum class ClockStyle(val id: String, val displayName: String) {
    DIGITAL_MINIMAL("digital_minimal", "Digital Minimal"),
    DIGITAL_LARGE("digital_large", "Digital Bold"),
    ANALOG_GLASS("analog_glass", "Analog Glass"),
    SPLIT_TYPOGRAPHY("split_typography", "Split Typography")
}

enum class LauncherTheme(val key: String, val displayName: String) {
    SYSTEM("system", "Follow System"),
    LIGHT("light", "Warm Cream Liquid Glass"),
    DARK("dark", "Charcoal Liquid Glass"),
    FROST_CLEAR("frost_clear", "Crystal Frost Liquid Glass"),
    SAFFRON_GLOW("saffron_glow", "Saffron Glow Liquid Glass")
}

enum class LauncherLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    BENGALI("bn", "Bengali", "বাংলা")
}

@Immutable
data class GlassSettings(
    val opacity: Float = 0.28f,          // 0.1f - 0.7f
    val blurRadiusDp: Int = 18,          // 0 - 32
    val borderWidthDp: Float = 1.2f,     // 0.5f - 3f
    val borderOpacity: Float = 0.45f,    // 0.1f - 0.9f
    val cornerRadiusDp: Int = 22,        // 12 - 36
    val showHighlights: Boolean = true,
    val enableGlassIconContainers: Boolean = false,
    val reducedEffects: Boolean = false
)

enum class WidgetType(
    val id: String,
    val displayName: String,
    val minColSpan: Int = 2,
    val defaultColSpan: Int = 4,
    val maxColSpan: Int = 5,
    val minRowSpan: Int = 1,
    val defaultRowSpan: Int = 1,
    val maxRowSpan: Int = 3
) {
    CLOCK("clock", "Clock & Time", minColSpan = 2, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 2, maxRowSpan = 3),
    WEATHER("weather", "Weather & Season", minColSpan = 2, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 1, maxRowSpan = 2),
    CALENDAR("calendar", "Calendar & Panchang", minColSpan = 2, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 1, maxRowSpan = 2),
    BATTERY("battery", "Battery & Device", minColSpan = 2, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 1, maxRowSpan = 2),
    GREETING("greeting", "Cultural Greeting", minColSpan = 2, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 1, maxRowSpan = 2),
    SEARCH("search", "Search Capsule", minColSpan = 3, defaultColSpan = 4, maxColSpan = 5, minRowSpan = 1, defaultRowSpan = 1, maxRowSpan = 1)
}

@Immutable
data class LauncherWidgetItem(
    val id: String,
    val type: WidgetType,
    val pageIndex: Int = 0,
    val order: Int = 0,
    val colSpan: Int = type.defaultColSpan,
    val rowSpan: Int = type.defaultRowSpan,
    val appWidgetId: Int = -1
)

@Immutable
data class FolderItem(
    val id: String,
    val name: String,
    val packageNames: List<String>,
    val pageIndex: Int = 0,
    val iconShape: IconShape = IconShape.SQUIRCLE
)

@Immutable
data class PresetWallpaper(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val nameBn: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val accentColorHex: Long
)

val BHARAT_WALLPAPERS = listOf(
    PresetWallpaper(
        id = "saffron_dawn",
        nameEn = "Saffron Dawn",
        nameHi = "भगवा प्रभात",
        nameBn = "গেরুয়া প্রভাত",
        primaryColorHex = 0xFFFF7722,
        secondaryColorHex = 0xFFFFF1EB,
        accentColorHex = 0xFFFFB300
    ),
    PresetWallpaper(
        id = "ashoka_midnight",
        nameEn = "Ashoka Midnight",
        nameHi = "अशोक मध्यरात्रि",
        nameBn = "অশোক মধ্যরাত্রি",
        primaryColorHex = 0xFF0D1B2A,
        secondaryColorHex = 0xFF1B263B,
        accentColorHex = 0xFF38BDF8
    ),
    PresetWallpaper(
        id = "deccan_emerald",
        nameEn = "Deccan Emerald",
        nameHi = "दक्खन मरकत",
        nameBn = "দাক্ষিণাত্য পান্না",
        primaryColorHex = 0xFF0A2E20,
        secondaryColorHex = 0xFF1E523A,
        accentColorHex = 0xFF34D399
    ),
    PresetWallpaper(
        id = "imperial_lotus",
        nameEn = "Imperial Lotus",
        nameHi = "राजसी कमल",
        nameBn = "রাজকীয় পদ্ম",
        primaryColorHex = 0xFF3B1E2B,
        secondaryColorHex = 0xFF1F121A,
        accentColorHex = 0xFFF472B6
    ),
    PresetWallpaper(
        id = "golden_temple",
        nameEn = "Golden Aura",
        nameHi = "स्वर्ण आभा",
        nameBn = "স্বর্ণ আভা",
        primaryColorHex = 0xFF2A1C0E,
        secondaryColorHex = 0xFF4A3416,
        accentColorHex = 0xFFFBBF24
    )
)

val DEFAULT_WIDGET_ITEMS = listOf(
    LauncherWidgetItem("widget_clock", WidgetType.CLOCK, pageIndex = 0, order = 0, colSpan = 4, rowSpan = 2),
    LauncherWidgetItem("widget_greeting", WidgetType.GREETING, pageIndex = 0, order = 1, colSpan = 4, rowSpan = 1),
    LauncherWidgetItem("widget_battery", WidgetType.BATTERY, pageIndex = 1, order = 0, colSpan = 4, rowSpan = 1),
    LauncherWidgetItem("widget_weather", WidgetType.WEATHER, pageIndex = 1, order = 1, colSpan = 4, rowSpan = 1),
    LauncherWidgetItem("widget_calendar", WidgetType.CALENDAR, pageIndex = 1, order = 2, colSpan = 4, rowSpan = 1)
)

@Immutable
data class LauncherSettings(
    val gridSize: GridSize = GridSize.FOUR_BY_FIVE,
    val iconSize: IconSize = IconSize.STANDARD,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val fontChoice: FontChoice = FontChoice.MODERN_POPPINS,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL_LARGE,
    val greetingChoice: CulturalGreeting = CulturalGreeting.NAMASTE,
    val showLabels: Boolean = true,
    val theme: LauncherTheme = LauncherTheme.LIGHT,
    val language: LauncherLanguage = LauncherLanguage.ENGLISH,
    val selectedWallpaperId: String = "saffron_dawn",
    val customWallpaperUri: String? = null,
    val hasCompletedOnboarding: Boolean = false,
    val homeShortcutPackages: List<String> = emptyList(),
    val dockPackages: List<String> = emptyList(),
    val favoritePackages: Set<String> = emptySet(),
    val enableVoiceSearch: Boolean = true,
    val glassSettings: GlassSettings = GlassSettings(),
    val homePagesCount: Int = 2,
    val activeWidgets: List<LauncherWidgetItem> = DEFAULT_WIDGET_ITEMS,
    val folders: List<FolderItem> = emptyList(),
    val is24HourClock: Boolean = false,
    val showClockSeconds: Boolean = false,
    val dockSlots: Int = 4
)
