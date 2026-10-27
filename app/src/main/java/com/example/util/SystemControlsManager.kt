package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.Log

data class BatteryStatus(
    val levelPercent: Int = 100,
    val isCharging: Boolean = false,
    val isUsbCharging: Boolean = false,
    val isAcCharging: Boolean = false,
    val temperatureCelsius: Float = 28.0f
)

data class SystemDiagnostics(
    val jvmHeapUsedMb: Long,
    val jvmHeapMaxMb: Long,
    val jvmHeapFreeMb: Long,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val cpuCores: Int,
    val androidVersion: String,
    val apiLevel: Int,
    val deviceModel: String,
    val manufacturer: String,
    val estimatedRefreshRateHz: Int
)

object SystemControlsManager {

    private const val TAG = "SystemControlsManager"
    private var isTorchOn: Boolean = false
    private var torchCameraId: String? = null

    /**
     * Toggles device flashlight safely via CameraManager.
     * Returns the new state (true if ON, false if OFF).
     */
    fun toggleTorch(context: Context): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager == null) {
                isTorchOn = false
                return false
            }

            if (torchCameraId == null) {
                // Find camera with flash capability
                for (id in cameraManager.cameraIdList) {
                    try {
                        val chars = cameraManager.getCameraCharacteristics(id)
                        val hasFlash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                        if (hasFlash) {
                            torchCameraId = id
                            break
                        }
                    } catch (_: Exception) {}
                }
                if (torchCameraId == null) {
                    torchCameraId = cameraManager.cameraIdList.firstOrNull() ?: "0"
                }
            }

            val targetId = torchCameraId ?: "0"
            val newState = !isTorchOn
            cameraManager.setTorchMode(targetId, newState)
            isTorchOn = newState
            isTorchOn
        } catch (e: CameraAccessException) {
            Log.w(TAG, "CameraAccessException while toggling torch: ${e.message}")
            isTorchOn = false
            false
        } catch (e: Exception) {
            Log.w(TAG, "Exception while toggling torch: ${e.message}")
            isTorchOn = false
            false
        }
    }

    fun isTorchActive(): Boolean = isTorchOn

    /**
     * Real intent to open Wi-Fi / Network settings
     */
    fun openWifiSettings(context: Context): Boolean {
        val intents = listOf(
            Intent(Settings.ACTION_WIFI_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Real intent to open Bluetooth settings
     */
    fun openBluetoothSettings(context: Context): Boolean {
        val intents = listOf(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
            Intent(Settings.ACTION_WIRELESS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Real intent to open Sound & Vibration settings
     */
    fun openSoundSettings(context: Context): Boolean {
        val intents = listOf(
            Intent(Settings.ACTION_SOUND_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Real intent to open Display & Brightness settings
     */
    fun openDisplaySettings(context: Context): Boolean {
        val intents = listOf(
            Intent(Settings.ACTION_DISPLAY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Real intent to open Battery / Power settings
     */
    fun openBatterySettings(context: Context): Boolean {
        val intents = listOf(
            Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Fetches current battery status via sticky Intent.ACTION_BATTERY_CHANGED broadcast.
     */
    fun getBatteryStatus(context: Context): BatteryStatus {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, filter)

            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 85

                val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

                val tempRaw = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 280)
                val tempC = tempRaw / 10.0f

                BatteryStatus(
                    levelPercent = percent.coerceIn(0, 100),
                    isCharging = isCharging,
                    isUsbCharging = usbCharge,
                    isAcCharging = acCharge,
                    temperatureCelsius = tempC
                )
            } else {
                BatteryStatus(levelPercent = 90, isCharging = false)
            }
        } catch (_: Exception) {
            BatteryStatus(levelPercent = 88, isCharging = false)
        }
    }

    /**
     * Gathers real device diagnostics for performance & bug center
     */
    fun getSystemDiagnostics(context: Context): SystemDiagnostics {
        val runtime = Runtime.getRuntime()
        val heapMax = runtime.maxMemory() / (1024 * 1024)
        val heapTotal = runtime.totalMemory() / (1024 * 1024)
        val heapFree = runtime.freeMemory() / (1024 * 1024)
        val heapUsed = heapTotal - heapFree

        var totalRam = 4096L
        var availRam = 2048L
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager?.getMemoryInfo(memInfo)
            totalRam = memInfo.totalMem / (1024 * 1024)
            availRam = memInfo.availMem / (1024 * 1024)
        } catch (_: Exception) {}

        val cores = runtime.availableProcessors()

        return SystemDiagnostics(
            jvmHeapUsedMb = heapUsed,
            jvmHeapMaxMb = heapMax,
            jvmHeapFreeMb = heapFree,
            totalRamMb = totalRam,
            availableRamMb = availRam,
            cpuCores = cores,
            androidVersion = Build.VERSION.RELEASE ?: "14.0",
            apiLevel = Build.VERSION.SDK_INT,
            deviceModel = Build.MODEL ?: "Android Device",
            manufacturer = Build.MANUFACTURER ?: "Generic",
            estimatedRefreshRateHz = 60
        )
    }
}
