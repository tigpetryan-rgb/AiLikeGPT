package com.ailikegpt.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ailikegpt.app.hardware.DeviceProfileRecommender
import com.ailikegpt.app.hardware.HardwareSnapshot
import com.ailikegpt.app.runtime.NativeRuntimeStatus
import java.util.Locale

@Composable
fun AiLikeGptApp(
    hardware: HardwareSnapshot,
    nativeStatus: NativeRuntimeStatus,
) {
    val recommendedProfile = DeviceProfileRecommender.recommend(hardware)

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "AiLikeGPT",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Offline Android AI foundation",
                    style = MaterialTheme.typography.titleMedium,
                )

                StatusCard(
                    title = "Native runtime",
                    lines = listOf(
                        "Loaded: ${nativeStatus.loaded}",
                        "Version: ${nativeStatus.version}",
                        "Backend: ${nativeStatus.backend}",
                    ) + nativeStatus.error?.let { listOf("Error: $it") }.orEmpty(),
                )

                StatusCard(
                    title = "Device capabilities",
                    lines = listOf(
                        "RAM: ${formatGiB(hardware.totalRamBytes)} GiB total / ${formatGiB(hardware.availableRamBytes)} GiB available",
                        "Storage available: ${formatGiB(hardware.availableStorageBytes)} GiB",
                        "CPU cores: ${hardware.cpuCores}",
                        "ABI: ${hardware.supportedAbis.joinToString()}",
                        "OpenGL ES: ${hardware.openGlEsVersion}",
                        "Vulkan hardware level: ${hardware.vulkanHardwareLevel ?: "not reported"}",
                    ),
                )

                StatusCard(
                    title = "Recommended model profile",
                    lines = listOf(
                        recommendedProfile.name,
                        "This is a first-pass device policy and will be tuned with real model benchmarks.",
                    ),
                )

                Text(
                    text = "No INTERNET permission is requested by this build.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    lines: List<String>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            lines.forEach { line ->
                Text(text = line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun formatGiB(bytes: Long): String =
    String.format(Locale.US, "%.1f", bytes.toDouble() / (1024.0 * 1024.0 * 1024.0))
