package com.ailikegpt.app.hardware

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceProfileRecommenderTest {
    @Test
    fun weakDeviceUsesLiteProfile() {
        assertEquals(
            ModelProfile.LITE,
            DeviceProfileRecommender.recommend(snapshot(ramGiB = 4, storageGiB = 8, cpuCores = 4)),
        )
    }

    @Test
    fun midRangeDeviceUsesBalancedProfile() {
        assertEquals(
            ModelProfile.BALANCED,
            DeviceProfileRecommender.recommend(snapshot(ramGiB = 8, storageGiB = 32, cpuCores = 8)),
        )
    }

    @Test
    fun highEndDeviceUsesPowerProfile() {
        assertEquals(
            ModelProfile.POWER,
            DeviceProfileRecommender.recommend(snapshot(ramGiB = 16, storageGiB = 64, cpuCores = 8)),
        )
    }

    private fun snapshot(
        ramGiB: Long,
        storageGiB: Long,
        cpuCores: Int,
    ): HardwareSnapshot {
        val gib = 1024L * 1024L * 1024L
        return HardwareSnapshot(
            totalRamBytes = ramGiB * gib,
            availableRamBytes = ramGiB * gib / 2,
            availableStorageBytes = storageGiB * gib,
            cpuCores = cpuCores,
            supportedAbis = listOf("arm64-v8a"),
            openGlEsVersion = "3.2",
            vulkanHardwareLevel = null,
        )
    }
}
