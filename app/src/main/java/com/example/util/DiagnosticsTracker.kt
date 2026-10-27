package com.example.util

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.FrameMetrics
import android.view.Window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

data class DiagnosticLogEntry(
    val id: Long = SystemClock.elapsedRealtimeNanos(),
    val timestamp: String,
    val tag: String,
    val message: String,
    val isError: Boolean = false
)

data class FrameTelemetrySnapshot(
    val measuredFrames: Int = 0,
    val jankyFrames: Int = 0,
    val avgFrameTimeMs: Float = 0f,
    val maxFrameTimeMs: Float = 0f,
    val estimatedFps: Int = 0,
    val isMeasured: Boolean = false
) {
    val jankPercent: Float
        get() = if (measuredFrames > 0) (jankyFrames * 100f) / measuredFrames else 0f
}

data class RuntimeTelemetryState(
    val startupTimeMs: Long? = null,
    val lastAppLoadTimeMs: Long? = null,
    val installedAppCount: Int = 0,
    val iconCacheCount: Int = 0,
    val iconCacheHitCount: Int = 0,
    val iconCacheMissCount: Int = 0,
    val frameTelemetry: FrameTelemetrySnapshot = FrameTelemetrySnapshot(),
    val logs: List<DiagnosticLogEntry> = emptyList(),
    val launchFailuresCount: Int = 0,
    val successfulLaunchesCount: Int = 0
)

/**
 * Real runtime diagnostics & frame-time collector using Android's Window.OnFrameMetricsAvailableListener.
 * Never fabricates FPS or crash reports; reports "Not measured" when frame metrics are unavailable.
 */
object DiagnosticsTracker {

    private val _state = MutableStateFlow(RuntimeTelemetryState())
    val state: StateFlow<RuntimeTelemetryState> = _state.asStateFlow()

    private val logEntries = CopyOnWriteArrayList<DiagnosticLogEntry>()
    private const val MAX_LOGS = 60

    private var processStartMarkMs: Long = SystemClock.uptimeMillis()
    private var startupRecorded = false

    // Rolling window of frame durations in milliseconds
    private val recentFrameDurationsMs = ArrayDeque<Float>(120)
    private var totalFramesRecorded = 0
    private var totalJankyFrames = 0

    private var frameMetricsListener: Window.OnFrameMetricsAvailableListener? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun markActivityCreateStart() {
        if (!startupRecorded) {
            processStartMarkMs = SystemClock.uptimeMillis()
        }
    }

    fun markFirstFrameDrawn() {
        if (!startupRecorded) {
            startupRecorded = true
            val duration = (SystemClock.uptimeMillis() - processStartMarkMs).coerceAtLeast(1L)
            _state.update { it.copy(startupTimeMs = duration) }
            recordEvent("Startup", "Initial launcher UI ready in ${duration}ms")
        }
    }

    fun attachFrameMetrics(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            detachFrameMetrics(activity)
            val listener = Window.OnFrameMetricsAvailableListener { _, frameMetrics, _ ->
                val totalDurationNs = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
                if (totalDurationNs > 0) {
                    val durationMs = totalDurationNs / 1_000_000f
                    synchronized(recentFrameDurationsMs) {
                        if (recentFrameDurationsMs.size >= 120) {
                            recentFrameDurationsMs.removeFirst()
                        }
                        recentFrameDurationsMs.addLast(durationMs)
                        totalFramesRecorded++
                        if (durationMs > 16.67f) {
                            totalJankyFrames++
                        }

                        // Update snapshot periodically (every 10 frames or first frame) to avoid state churn
                        if (totalFramesRecorded == 1 || totalFramesRecorded % 10 == 0) {
                            val avg = recentFrameDurationsMs.average().toFloat()
                            val max = recentFrameDurationsMs.maxOrNull() ?: durationMs
                            val fps = if (avg > 0f) (1000f / avg).roundToInt().coerceIn(1, 120) else 60
                            val snapshot = FrameTelemetrySnapshot(
                                measuredFrames = totalFramesRecorded,
                                jankyFrames = totalJankyFrames,
                                avgFrameTimeMs = avg,
                                maxFrameTimeMs = max,
                                estimatedFps = fps,
                                isMeasured = true
                            )
                            _state.update { it.copy(frameTelemetry = snapshot) }
                        }
                    }
                }
            }
            try {
                activity.window.addOnFrameMetricsAvailableListener(listener, mainHandler)
                frameMetricsListener = listener
            } catch (e: Exception) {
                recordError("FrameMetrics", "Unable to attach FrameMetrics listener: ${e.message}")
            }
        }
    }

    fun detachFrameMetrics(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            frameMetricsListener?.let { listener ->
                try {
                    activity.window.removeOnFrameMetricsAvailableListener(listener)
                } catch (_: Exception) {}
            }
            frameMetricsListener = null
        }
    }

    fun recordAppCatalogLoad(durationMs: Long, appCount: Int, cacheSize: Int, hits: Int, misses: Int) {
        _state.update {
            it.copy(
                lastAppLoadTimeMs = durationMs,
                installedAppCount = appCount,
                iconCacheCount = cacheSize,
                iconCacheHitCount = hits,
                iconCacheMissCount = misses
            )
        }
        recordEvent("AppRepository", "Loaded $appCount apps in ${durationMs}ms (Icon cache: $cacheSize items, hits=$hits, misses=$misses)")
    }

    fun updateIconCacheStats(cacheSize: Int, hits: Int, misses: Int) {
        _state.update {
            it.copy(
                iconCacheCount = cacheSize,
                iconCacheHitCount = hits,
                iconCacheMissCount = misses
            )
        }
    }

    fun recordAppLaunch(packageName: String, success: Boolean, reason: String? = null) {
        if (success) {
            _state.update { it.copy(successfulLaunchesCount = it.successfulLaunchesCount + 1) }
            recordEvent("AppLaunch", "Launched $packageName")
        } else {
            _state.update { it.copy(launchFailuresCount = it.launchFailuresCount + 1) }
            recordError("AppLaunch", "Failed to launch $packageName: ${reason ?: "No launchable activity"}")
        }
    }

    fun recordEvent(tag: String, message: String) {
        appendLog(tag, message, isError = false)
    }

    fun recordError(tag: String, message: String) {
        appendLog(tag, message, isError = true)
    }

    private fun appendLog(tag: String, message: String, isError: Boolean) {
        val timeStr = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val entry = DiagnosticLogEntry(
            timestamp = timeStr,
            tag = tag,
            message = message,
            isError = isError
        )
        logEntries.add(0, entry)
        while (logEntries.size > MAX_LOGS) {
            logEntries.removeAt(logEntries.size - 1)
        }
        _state.update { it.copy(logs = logEntries.toList()) }
    }

    fun clearLogs() {
        logEntries.clear()
        synchronized(recentFrameDurationsMs) {
            recentFrameDurationsMs.clear()
            totalFramesRecorded = 0
            totalJankyFrames = 0
        }
        _state.update {
            it.copy(
                logs = emptyList(),
                launchFailuresCount = 0,
                frameTelemetry = FrameTelemetrySnapshot(isMeasured = false)
            )
        }
        recordEvent("Diagnostics", "Diagnostic logs and frame counters reset by user")
    }
}
