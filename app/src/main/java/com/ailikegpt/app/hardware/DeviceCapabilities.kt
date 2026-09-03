package com.ailikegpt.app.hardware

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.StatFs

data class HardwareSnapshot(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val availableStorageBytes: Long,
    val cpuCores: Int,
    val supportedAbis: List<String>,
    val openGlEsVersion: String,
    val vulkanHardwareLevel: Int?,
)

object DeviceCapabilities {
    fun read(context: Context): HardwareSnapshot {
        val activityManager = context.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        val statFs = StatFs(context.filesDir.absolutePath)

        val glVersionValue = activityManager.deviceConfigurationInfo.reqGlEsVersion
        val glMajor = glVersionValue shr 16
        val glMinor = glVersionValue and 0xffff

        val vulkanLevel = context.packageManager.systemAvailableFeatures
            .firstOrNull { feature ->
                feature.name == PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL
            }
            ?.version

        return HardwareSnapshot(
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            availableStorageBytes = statFs.availableBytes,
            cpuCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
            openGlEsVersion = "$glMajor.$glMinor",
            vulkanHardwareLevel = vulkanLevel,
        )
    }
}
