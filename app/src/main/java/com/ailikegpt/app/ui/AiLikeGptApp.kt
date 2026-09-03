package com.ailikegpt.app.ui

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ailikegpt.app.hardware.DeviceProfileRecommender
import com.ailikegpt.app.hardware.HardwareSnapshot
import com.ailikegpt.app.runtime.GenerationResult
import com.ailikegpt.app.runtime.LocalModelFile
import com.ailikegpt.app.runtime.LocalModelStore
import com.ailikegpt.app.runtime.ModelImportResult
import com.ailikegpt.app.runtime.ModelLoadResult
import com.ailikegpt.app.runtime.NativeRuntime
import com.ailikegpt.app.runtime.NativeRuntimeStatus
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AiLikeGptApp(
    hardware: HardwareSnapshot,
    nativeStatus: NativeRuntimeStatus,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val recommendedProfile = DeviceProfileRecommender.recommend(hardware)

    var localModels by remember { mutableStateOf(LocalModelStore.listModels(context)) }
    var selectedModelName by remember { mutableStateOf<String?>(null) }
    var modelLoaded by remember { mutableStateOf(NativeRuntime.isModelLoaded()) }
    var modelBusy by remember { mutableStateOf(false) }
    var modelStatus by remember {
        mutableStateOf(
            if (localModels.isEmpty()) {
                "Import a local GGUF model to begin."
            } else {
                "${localModels.size} local GGUF model(s) discovered."
            },
        )
    }
    var lastImportedSha256 by remember { mutableStateOf<String?>(null) }

    var prompt by remember { mutableStateOf("") }
    var lastUserPrompt by remember { mutableStateOf("") }
    var assistantText by remember { mutableStateOf("") }
    var generationRunning by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf("Idle") }

    fun loadLocalModel(model: LocalModelFile) {
        if (modelBusy || generationRunning) return

        modelBusy = true
        modelStatus = "Loading ${model.name}..."
        scope.launch(Dispatchers.IO) {
            val result = NativeRuntime.loadModel(
                absolutePath = model.absolutePath,
                contextSize = 4096,
                threads = hardware.cpuCores.coerceAtLeast(1),
            )

            withContext(Dispatchers.Main) {
                when (result) {
                    ModelLoadResult.Success -> {
                        selectedModelName = model.name
                        modelLoaded = true
                        modelStatus = "Loaded ${model.name}"
                    }

                    is ModelLoadResult.Failure -> {
                        modelLoaded = NativeRuntime.isModelLoaded()
                        modelStatus = "Load failed: ${result.message}"
                    }
                }
                modelBusy = false
            }
        }
    }

    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null || modelBusy || generationRunning) return@rememberLauncherForActivityResult

        modelBusy = true
        modelStatus = "Importing GGUF into private app storage..."
        scope.launch(Dispatchers.IO) {
            when (val importResult = LocalModelStore.importFromUri(context, uri)) {
                is ModelImportResult.Failure -> {
                    withContext(Dispatchers.Main) {
                        modelStatus = "Import failed: ${importResult.message}"
                        modelBusy = false
                    }
                }

                is ModelImportResult.Success -> {
                    val imported = importResult.model
                    val loadResult = NativeRuntime.loadModel(
                        absolutePath = imported.absolutePath,
                        contextSize = 4096,
                        threads = hardware.cpuCores.coerceAtLeast(1),
                    )

                    withContext(Dispatchers.Main) {
                        localModels = LocalModelStore.listModels(context)
                        lastImportedSha256 = imported.sha256
                        when (loadResult) {
                            ModelLoadResult.Success -> {
                                selectedModelName = imported.name
                                modelLoaded = true
                                modelStatus = "Imported and loaded ${imported.name}"
                            }

                            is ModelLoadResult.Failure -> {
                                modelLoaded = NativeRuntime.isModelLoaded()
                                modelStatus = "Imported ${imported.name}, but load failed: ${loadResult.message}"
                            }
                        }
                        modelBusy = false
                    }
                }
            }
        }
    }

    fun sendPrompt() {
        val submitted = prompt.trim()
        if (submitted.isEmpty() || generationRunning || modelBusy || !modelLoaded) return

        lastUserPrompt = submitted
        assistantText = ""
        prompt = ""
        generationRunning = true
        generationStatus = "Generating locally..."

        scope.launch(Dispatchers.IO) {
            val result = NativeRuntime.generateChat(
                userPrompt = submitted,
                systemPrompt = "You are AiLikeGPT, a private offline AI assistant running locally on Android.",
            ) { chunk ->
                mainHandler.post {
                    assistantText += chunk
                }
            }

            withContext(Dispatchers.Main) {
                generationRunning = false
                generationStatus = when (result) {
                    GenerationResult.Completed -> "Completed"
                    GenerationResult.Cancelled -> "Stopped"
                    is GenerationResult.Failure -> "Generation failed: ${result.message}"
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            NativeRuntime.cancelGeneration()
        }
    }

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
                    text = "Offline Android AI — local GGUF streaming foundation",
                    style = MaterialTheme.typography.titleMedium,
                )

                StatusCard(
                    title = "Native runtime",
                    lines = listOf(
                        "Loaded: ${nativeStatus.loaded}",
                        "Version: ${nativeStatus.version}",
                        "Backend: ${nativeStatus.backend}",
                        "Model loaded: $modelLoaded",
                    ) + nativeStatus.error?.let { listOf("Error: $it") }.orEmpty(),
                )

                ModelManagerCard(
                    localModels = localModels,
                    selectedModelName = selectedModelName,
                    busy = modelBusy,
                    generationRunning = generationRunning,
                    status = modelStatus,
                    lastImportedSha256 = lastImportedSha256,
                    onImport = {
                        modelPicker.launch(arrayOf("*/*"))
                    },
                    onLoadModel = ::loadLocalModel,
                )

                ChatCard(
                    modelLoaded = modelLoaded,
                    prompt = prompt,
                    onPromptChange = { prompt = it },
                    lastUserPrompt = lastUserPrompt,
                    assistantText = assistantText,
                    generationRunning = generationRunning,
                    generationStatus = generationStatus,
                    sendEnabled = modelLoaded && !modelBusy && !generationRunning && prompt.isNotBlank(),
                    onSend = ::sendPrompt,
                    onStop = {
                        generationStatus = "Stopping..."
                        NativeRuntime.cancelGeneration()
                    },
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
                    text = "No INTERNET permission is requested by this build. Imported models are copied into private app storage and inference stays local.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ModelManagerCard(
    localModels: List<LocalModelFile>,
    selectedModelName: String?,
    busy: Boolean,
    generationRunning: Boolean,
    status: String,
    lastImportedSha256: String?,
    onImport: () -> Unit,
    onLoadModel: (LocalModelFile) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Local model manager",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = status, style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = onImport,
                enabled = !busy && !generationRunning,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (busy) "Working..." else "Import GGUF from device")
            }

            if (localModels.isNotEmpty()) {
                Text(
                    text = "Models in private app storage",
                    style = MaterialTheme.typography.labelLarge,
                )
                localModels.forEach { model ->
                    OutlinedButton(
                        onClick = { onLoadModel(model) },
                        enabled = !busy && !generationRunning,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val selected = if (selectedModelName == model.name) " • loaded" else ""
                        Text("${model.name} (${formatGiB(model.sizeBytes)} GiB)$selected")
                    }
                }
            }

            lastImportedSha256?.let { checksum ->
                Text(
                    text = "Last import SHA-256: $checksum",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun ChatCard(
    modelLoaded: Boolean,
    prompt: String,
    onPromptChange: (String) -> Unit,
    lastUserPrompt: String,
    assistantText: String,
    generationRunning: Boolean,
    generationStatus: String,
    sendEnabled: Boolean,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Offline chat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (!modelLoaded) {
                Text(
                    text = "Load a local GGUF model before sending a message.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (lastUserPrompt.isNotEmpty()) {
                Text(
                    text = "You",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(text = lastUserPrompt, style = MaterialTheme.typography.bodyMedium)
            }

            if (assistantText.isNotEmpty() || generationRunning) {
                Text(
                    text = "AiLikeGPT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = assistantText.ifEmpty { "…" },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                enabled = modelLoaded && !generationRunning,
                label = { Text("Message") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onSend,
                    enabled = sendEnabled,
                ) {
                    Text("Send")
                }

                OutlinedButton(
                    onClick = onStop,
                    enabled = generationRunning,
                ) {
                    Text("Stop")
                }
            }

            Text(
                text = "Generation: $generationStatus",
                style = MaterialTheme.typography.bodySmall,
            )
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
