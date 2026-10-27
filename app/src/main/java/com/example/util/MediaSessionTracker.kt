package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.service.BharatMediaNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class ActiveMediaSessionUi(
    val hasNotificationPermission: Boolean = false,
    val hasActiveSession: Boolean = false,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val trackTitle: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val sourcePackage: String = "",
    val sourceAppLabel: String = "",
    val canPlayPause: Boolean = false,
    val canSkipNext: Boolean = false,
    val canSkipPrevious: Boolean = false,
    val activeSessionsCount: Int = 0,
    val selectedSessionIndex: Int = 0,
    val artworkBitmap: ImageBitmap? = null
)

/**
 * Real Android MediaSessionManager + MediaController tracker for SmartIsland.
 * Never fabricates track titles or playback states.
 */
object MediaSessionTracker {

    private val _mediaState = MutableStateFlow(ActiveMediaSessionUi())
    val mediaState: StateFlow<ActiveMediaSessionUi> = _mediaState.asStateFlow()

    private var activeControllers: List<MediaController> = emptyList()
    private var selectedIndex: Int = 0
    private var registeredController: MediaController? = null
    private var sessionListenerRegistered = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateCurrentControllerState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateCurrentControllerState()
        }

        override fun onSessionDestroyed() {
            registeredController = null
            _mediaState.update {
                it.copy(
                    hasActiveSession = false,
                    isPlaying = false,
                    trackTitle = "",
                    artistName = "",
                    artworkBitmap = null
                )
            }
        }
    }

    private var appContextRef: Context? = null

    private val activeSessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        onControllersUpdated(controllers ?: emptyList())
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        return try {
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            val pkg = context.packageName
            flat.split(":").any { it.startsWith("$pkg/") || it.contains(pkg) }
        } catch (_: Exception) {
            false
        }
    }

    fun openNotificationListenerSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            DiagnosticsTracker.recordError("MediaTracker", "Failed to open notification listener settings: ${e.message}")
            try {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun onNotificationListenerConnected(context: Context) {
        appContextRef = context.applicationContext
        refreshSessions(context)
    }

    fun onNotificationListenerDisconnected() {
        unregisterCurrentController()
        activeControllers = emptyList()
        _mediaState.update {
            it.copy(
                hasActiveSession = false,
                isPlaying = false,
                trackTitle = "",
                artistName = "",
                artworkBitmap = null
            )
        }
    }

    fun refreshSessions(context: Context) {
        val appCtx = context.applicationContext
        appContextRef = appCtx
        val hasPerm = isNotificationListenerEnabled(appCtx)

        if (!hasPerm) {
            // Without NotificationListener permission, check if AudioManager reports active music stream
            val audioManager = appCtx.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val systemMusicActive = try { audioManager?.isMusicActive == true } catch (_: Exception) { false }

            _mediaState.update {
                ActiveMediaSessionUi(
                    hasNotificationPermission = false,
                    hasActiveSession = systemMusicActive,
                    isPlaying = systemMusicActive,
                    trackTitle = if (systemMusicActive) "System Audio Active" else "",
                    artistName = if (systemMusicActive) "Grant Notification Access for track details" else "",
                    canPlayPause = systemMusicActive,
                    canSkipNext = systemMusicActive,
                    canSkipPrevious = systemMusicActive
                )
            }
            return
        }

        try {
            val mediaSessionManager = appCtx.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(appCtx, BharatMediaNotificationListener::class.java)
            if (mediaSessionManager != null) {
                if (!sessionListenerRegistered) {
                    try {
                        mediaSessionManager.addOnActiveSessionsChangedListener(
                            activeSessionsListener,
                            componentName,
                            mainHandler
                        )
                        sessionListenerRegistered = true
                    } catch (_: SecurityException) {
                        sessionListenerRegistered = false
                    }
                }
                val controllers = mediaSessionManager.getActiveSessions(componentName)
                onControllersUpdated(controllers)
            }
        } catch (e: SecurityException) {
            _mediaState.update {
                ActiveMediaSessionUi(
                    hasNotificationPermission = false,
                    hasActiveSession = false
                )
            }
        } catch (e: Exception) {
            DiagnosticsTracker.recordError("MediaTracker", "Error querying media sessions: ${e.message}")
        }
    }

    private fun onControllersUpdated(controllers: List<MediaController>) {
        activeControllers = controllers
        if (controllers.isEmpty()) {
            unregisterCurrentController()
            _mediaState.update {
                ActiveMediaSessionUi(
                    hasNotificationPermission = true,
                    hasActiveSession = false,
                    isPlaying = false
                )
            }
            return
        }

        // Prefer a currently playing controller if available, else keep selected index in bounds
        val playingIdx = controllers.indexOfFirst {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        }
        selectedIndex = when {
            playingIdx >= 0 -> playingIdx
            selectedIndex in controllers.indices -> selectedIndex
            else -> 0
        }

        bindController(controllers[selectedIndex])
    }

    fun cycleActiveSession() {
        if (activeControllers.size <= 1) return
        selectedIndex = (selectedIndex + 1) % activeControllers.size
        bindController(activeControllers[selectedIndex])
    }

    private fun bindController(controller: MediaController) {
        if (registeredController != controller) {
            unregisterCurrentController()
            try {
                controller.registerCallback(controllerCallback, mainHandler)
                registeredController = controller
            } catch (_: Exception) {}
        }
        updateCurrentControllerState()
    }

    private fun unregisterCurrentController() {
        try {
            registeredController?.unregisterCallback(controllerCallback)
        } catch (_: Exception) {}
        registeredController = null
    }

    private fun updateCurrentControllerState() {
        val controller = registeredController ?: return
        val playbackState = controller.playbackState
        val metadata = controller.metadata
        val pkg = controller.packageName ?: ""

        val stateCode = playbackState?.state ?: PlaybackState.STATE_NONE
        val isPlaying = stateCode == PlaybackState.STATE_PLAYING
        val isBuffering = stateCode == PlaybackState.STATE_BUFFERING || stateCode == PlaybackState.STATE_CONNECTING

        val actions = playbackState?.actions ?: 0L
        val canPlayPause = (actions and (PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE)) != 0L || stateCode != PlaybackState.STATE_NONE
        val canNext = (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L
        val canPrev = (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L

        val rawTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""
        val rawArtist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: ""
        val rawAlbum = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

        val rawBitmap: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)

        val scaledArt: ImageBitmap? = rawBitmap?.let { bmp ->
            try {
                val target = 96
                if (bmp.width > target || bmp.height > target) {
                    Bitmap.createScaledBitmap(bmp, target, target, true).asImageBitmap()
                } else {
                    bmp.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }

        val appLabel = resolveAppLabel(pkg)

        _mediaState.update {
            ActiveMediaSessionUi(
                hasNotificationPermission = true,
                hasActiveSession = true,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                trackTitle = rawTitle.ifBlank { appLabel.ifBlank { "Active Media" } },
                artistName = rawArtist.ifBlank { if (isPlaying) "Playing on $appLabel" else "Paused on $appLabel" },
                albumName = rawAlbum,
                sourcePackage = pkg,
                sourceAppLabel = appLabel,
                canPlayPause = canPlayPause,
                canSkipNext = canNext,
                canSkipPrevious = canPrev,
                activeSessionsCount = activeControllers.size,
                selectedSessionIndex = selectedIndex,
                artworkBitmap = scaledArt
            )
        }
    }

    private fun resolveAppLabel(packageName: String): String {
        val ctx = appContextRef ?: return packageName
        if (packageName.isBlank()) return ""
        return try {
            val pm = ctx.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
        }
    }

    fun togglePlayPause(context: Context) {
        val controller = registeredController
        if (controller != null) {
            try {
                val state = controller.playbackState?.state
                if (state == PlaybackState.STATE_PLAYING) {
                    controller.transportControls.pause()
                } else {
                    controller.transportControls.play()
                }
                return
            } catch (e: Exception) {
                DiagnosticsTracker.recordError("MediaTracker", "Transport play/pause error: ${e.message}")
            }
        }
        // Fallback hardware media key dispatch via AudioManager
        dispatchMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        refreshSessions(context)
    }

    fun skipToNext(context: Context) {
        val controller = registeredController
        if (controller != null) {
            try {
                controller.transportControls.skipToNext()
                return
            } catch (e: Exception) {
                DiagnosticsTracker.recordError("MediaTracker", "Transport skipToNext error: ${e.message}")
            }
        }
        dispatchMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_NEXT)
        refreshSessions(context)
    }

    fun skipToPrevious(context: Context) {
        val controller = registeredController
        if (controller != null) {
            try {
                controller.transportControls.skipToPrevious()
                return
            } catch (e: Exception) {
                DiagnosticsTracker.recordError("MediaTracker", "Transport skipToPrevious error: ${e.message}")
            }
        }
        dispatchMediaKeyEvent(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
        refreshSessions(context)
    }

    fun openSourceMediaApp(context: Context): Boolean {
        val controller = registeredController
        if (controller != null) {
            try {
                val pendingIntent = controller.sessionActivity
                if (pendingIntent != null) {
                    pendingIntent.send()
                    return true
                }
            } catch (_: Exception) {}

            val pkg = controller.packageName
            if (!pkg.isNullOrBlank()) {
                try {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return true
                    }
                } catch (_: Exception) {}
            }
        }
        return false
    }

    private fun dispatchMediaKeyEvent(context: Context, keyCode: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
        } catch (_: Exception) {}
    }
}
