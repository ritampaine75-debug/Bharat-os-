package com.example.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.data.model.AppCategory
import com.example.data.model.AppInfo
import com.example.util.AppSearchRanker
import com.example.util.DiagnosticsTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

data class CachedAppIcon(
    val bitmap: Bitmap,
    val imageBitmap: ImageBitmap
)

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Pre-sized bounded LruCache storing both Bitmap and Compose ImageBitmap so zero allocation occurs during scrolling
    private val iconCache = LruCache<String, CachedAppIcon>(180)
    private val cacheHits = AtomicInteger(0)
    private val cacheMisses = AtomicInteger(0)

    // Debounce guard to prevent duplicate rapid launches within 400ms
    private var lastLaunchedPackage: String? = null
    private var lastLaunchTimestampMs: Long = 0L

    companion object {
        // Cap icon bitmap dimensions to 132px (~48-64dp @ 2.25x-2.5x density) to eliminate GPU scaling overhead and memory spikes
        private const val MAX_ICON_PX = 132
        private const val LAUNCH_DEBOUNCE_MS = 400L
    }

    suspend fun getInstalledApps(favoritePackages: Set<String>): List<AppInfo> = withContext(Dispatchers.IO) {
        val startMs = SystemClock.uptimeMillis()
        val rawApps = ArrayList<AppInfo>(128)
        val selfPackage = context.packageName

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val activities = launcherApps?.getActivityList(null, Process.myUserHandle())
                if (!activities.isNullOrEmpty()) {
                    for (activity in activities) {
                        val pkgName = activity.applicationInfo.packageName
                        if (pkgName == selfPackage) continue

                        val actName = activity.componentName.className
                        val cacheKey = "$pkgName#$actName"
                        val label = activity.label?.toString()?.trim().orEmpty().ifEmpty { pkgName }
                        val isSystem = (activity.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val category = categorizePackage(pkgName, label)
                        val cachedIcon = getCachedOrRenderIcon(cacheKey) { activity.getIcon(0) }

                        rawApps.add(
                            AppInfo(
                                packageName = pkgName,
                                activityName = actName,
                                label = label,
                                isSystemApp = isSystem,
                                isFavorite = favoritePackages.contains(pkgName),
                                category = category,
                                iconBitmap = cachedIcon?.bitmap,
                                iconImageBitmap = cachedIcon?.imageBitmap,
                                normalizedLabel = AppSearchRanker.normalize(label)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            DiagnosticsTracker.recordError("AppRepository", "LauncherApps query fallback: ${e.message}")
        }

        if (rawApps.isEmpty()) {
            try {
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = packageManager.queryIntentActivities(mainIntent, 0)
                for (ri in resolveInfos) {
                    val pkgName = ri.activityInfo.packageName
                    if (pkgName == selfPackage) continue

                    val actName = ri.activityInfo.name ?: pkgName
                    val cacheKey = "$pkgName#$actName"
                    val label = ri.loadLabel(packageManager)?.toString()?.trim().orEmpty().ifEmpty { pkgName }
                    val isSystem = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val category = categorizePackage(pkgName, label)
                    val cachedIcon = getCachedOrRenderIcon(cacheKey) { ri.loadIcon(packageManager) }

                    rawApps.add(
                        AppInfo(
                            packageName = pkgName,
                            activityName = actName,
                            label = label,
                            isSystemApp = isSystem,
                            isFavorite = favoritePackages.contains(pkgName),
                            category = category,
                            iconBitmap = cachedIcon?.bitmap,
                            iconImageBitmap = cachedIcon?.imageBitmap,
                            normalizedLabel = AppSearchRanker.normalize(label)
                        )
                    )
                }
            } catch (e: Exception) {
                DiagnosticsTracker.recordError("AppRepository", "PackageManager query error: ${e.message}")
            }
        }

        // Deduplicate apps by composite key and sort deterministically by normalized label
        val distinctSorted = rawApps
            .distinctBy { it.compositeKey }
            .sortedWith(
                compareBy<AppInfo> { it.normalizedLabel }
                    .thenBy { it.packageName }
                    .thenBy { it.activityName }
            )

        val elapsed = (SystemClock.uptimeMillis() - startMs).coerceAtLeast(1L)
        DiagnosticsTracker.recordAppCatalogLoad(
            durationMs = elapsed,
            appCount = distinctSorted.size,
            cacheSize = iconCache.size(),
            hits = cacheHits.get(),
            misses = cacheMisses.get()
        )

        distinctSorted
    }

    fun invalidatePackageCache(packageName: String? = null) {
        if (packageName == null) {
            iconCache.evictAll()
        } else {
            val snapshot = iconCache.snapshot().keys.toList()
            for (key in snapshot) {
                if (key.startsWith("$packageName#") || key == packageName) {
                    iconCache.remove(key)
                }
            }
        }
    }

    private inline fun getCachedOrRenderIcon(
        cacheKey: String,
        drawableProvider: () -> Drawable?
    ): CachedAppIcon? {
        val existing = iconCache.get(cacheKey)
        if (existing != null) {
            cacheHits.incrementAndGet()
            return existing
        }

        cacheMisses.incrementAndGet()
        val drawable = try { drawableProvider() } catch (_: Exception) { null } ?: return null
        val bitmap = drawableToScaledBitmap(drawable) ?: return null
        val cached = CachedAppIcon(
            bitmap = bitmap,
            imageBitmap = bitmap.asImageBitmap()
        )
        iconCache.put(cacheKey, cached)
        return cached
    }

    private fun drawableToScaledBitmap(drawable: Drawable): Bitmap? {
        return try {
            if (drawable is BitmapDrawable && drawable.bitmap != null) {
                val src = drawable.bitmap
                if (src.width in 1..MAX_ICON_PX && src.height in 1..MAX_ICON_PX) {
                    return src
                }
            }
            val rawW = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else MAX_ICON_PX
            val rawH = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else MAX_ICON_PX
            val targetW = rawW.coerceIn(48, MAX_ICON_PX)
            val targetH = rawH.coerceIn(48, MAX_ICON_PX)

            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }

    private fun categorizePackage(packageName: String, label: String): AppCategory {
        val lower = "$packageName $label".lowercase(Locale.ROOT)
        return when {
            lower.contains("dialer") || lower.contains("phone") || lower.contains("call") ||
            lower.contains("message") || lower.contains("sms") || lower.contains("mms") ||
            lower.contains("contact") || lower.contains("whatsapp") || lower.contains("telegram") -> {
                AppCategory.COMMUNICATION
            }
            lower.contains("camera") || lower.contains("gallery") || lower.contains("photo") ||
            lower.contains("music") || lower.contains("audio") || lower.contains("video") ||
            lower.contains("youtube") || lower.contains("media") || lower.contains("player") -> {
                AppCategory.MEDIA
            }
            lower.contains("chrome") || lower.contains("browser") || lower.contains("calc") ||
            lower.contains("clock") || lower.contains("file") || lower.contains("manager") ||
            lower.contains("deskclock") || lower.contains("calendar") -> {
                AppCategory.TOOLS
            }
            lower.contains("mail") || lower.contains("gmail") || lower.contains("note") ||
            lower.contains("doc") || lower.contains("sheet") || lower.contains("drive") -> {
                AppCategory.PRODUCTIVITY
            }
            lower.contains("android") || lower.contains("setting") -> {
                AppCategory.SYSTEM
            }
            else -> AppCategory.TOOLS
        }
    }

    /**
     * Launches an installed application reliably by trying:
     * 1. Explicit ComponentName(packageName, activityName) with ACTION_MAIN + CATEGORY_LAUNCHER
     * 2. LauncherApps.startMainActivity (API 26+)
     * 3. PackageManager.getLaunchIntentForPackage(packageName)
     * Includes rapid-tap debounce protection and diagnostic telemetry.
     */
    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        if (packageName.isBlank()) {
            DiagnosticsTracker.recordAppLaunch(packageName, false, "Blank package name")
            return false
        }

        val now = SystemClock.uptimeMillis()
        if (packageName == lastLaunchedPackage && (now - lastLaunchTimestampMs) < LAUNCH_DEBOUNCE_MS) {
            // Ignore accidental double-tap within debounce window
            return true
        }

        // Strategy 1: Explicit ComponentName if provided and resolves
        if (!activityName.isNullOrBlank()) {
            try {
                val component = ComponentName(packageName, activityName)
                val explicitIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    this.component = component
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                }
                if (explicitIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(explicitIntent)
                    lastLaunchedPackage = packageName
                    lastLaunchTimestampMs = now
                    DiagnosticsTracker.recordAppLaunch(packageName, true)
                    return true
                }
            } catch (_: Exception) {
                // Fall through to Strategy 2
            }
        }

        // Strategy 2: PackageManager.getLaunchIntentForPackage
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                context.startActivity(launchIntent)
                lastLaunchedPackage = packageName
                lastLaunchTimestampMs = now
                DiagnosticsTracker.recordAppLaunch(packageName, true)
                return true
            }
        } catch (e: Exception) {
            DiagnosticsTracker.recordAppLaunch(packageName, false, e.javaClass.simpleName + ": " + e.message)
            return false
        }

        // Strategy 3: LauncherApps startMainActivity fallback
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activityName.isNullOrBlank()) {
            try {
                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
                val component = ComponentName(packageName, activityName)
                if (launcherApps != null && launcherApps.isActivityEnabled(component, Process.myUserHandle())) {
                    launcherApps.startMainActivity(component, Process.myUserHandle(), null, null)
                    lastLaunchedPackage = packageName
                    lastLaunchTimestampMs = now
                    DiagnosticsTracker.recordAppLaunch(packageName, true)
                    return true
                }
            } catch (e: Exception) {
                DiagnosticsTracker.recordAppLaunch(packageName, false, e.message)
                return false
            }
        }

        DiagnosticsTracker.recordAppLaunch(packageName, false, "No launchable MAIN/LAUNCHER activity found")
        return false
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }
}
