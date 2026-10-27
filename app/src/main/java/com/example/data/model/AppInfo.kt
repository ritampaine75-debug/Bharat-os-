package com.example.data.model

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import java.util.Locale

@Immutable
data class AppInfo(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isSystemApp: Boolean = false,
    val installTime: Long = 0L,
    val isFavorite: Boolean = false,
    val category: AppCategory = AppCategory.ALL,
    // Pre-decoded Compose ImageBitmap and Android Bitmap cached off the main thread
    val iconBitmap: Bitmap? = null,
    val iconImageBitmap: ImageBitmap? = null,
    val normalizedLabel: String = label.trim().lowercase(Locale.ROOT)
) {
    val compositeKey: String
        get() = "$packageName#$activityName"
}

enum class AppCategory(val id: String) {
    ALL("all"),
    FAVORITES("favorites"),
    COMMUNICATION("communication"),
    TOOLS("tools"),
    PRODUCTIVITY("productivity"),
    MEDIA("media"),
    SYSTEM("system")
}
