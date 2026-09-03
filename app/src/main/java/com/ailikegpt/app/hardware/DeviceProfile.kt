package com.ailikegpt.app.hardware

enum class ModelProfile {
    LITE,
    BALANCED,
    POWER,
}

object DeviceProfileRecommender {
    private const val GIB = 1024L * 1024L * 1024L

    fun recommend(snapshot: HardwareSnapshot): ModelProfile {
        val totalRamGiB = snapshot.totalRamBytes.toDouble() / GIB
        val freeStorageGiB = snapshot.availableStorageBytes.toDouble() / GIB

        return when {
            totalRamGiB >= 12.0 &&
                freeStorageGiB >= 20.0 &&
                snapshot.cpuCores >= 8 -> ModelProfile.POWER

            totalRamGiB >= 6.0 &&
                freeStorageGiB >= 10.0 &&
                snapshot.cpuCores >= 6 -> ModelProfile.BALANCED

            else -> ModelProfile.LITE
        }
    }
}
