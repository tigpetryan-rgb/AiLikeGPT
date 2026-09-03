package com.ailikegpt.app.runtime

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

private const val MODEL_DIRECTORY = "models"
private const val COPY_BUFFER_BYTES = 1024 * 1024
private const val STORAGE_HEADROOM_BYTES = 64L * 1024L * 1024L

data class LocalModelFile(
    val name: String,
    val absolutePath: String,
    val sizeBytes: Long,
    val sha256: String? = null,
)

sealed interface ModelImportResult {
    data class Success(val model: LocalModelFile) : ModelImportResult
    data class Failure(val message: String) : ModelImportResult
}

object LocalModelStore {
    fun listModels(context: Context): List<LocalModelFile> =
        modelDirectory(context)
            .listFiles()
            .orEmpty()
            .asSequence()
            .filter { file -> file.isFile && file.extension.equals("gguf", ignoreCase = true) }
            .sortedBy { file -> file.name.lowercase() }
            .map { file ->
                LocalModelFile(
                    name = file.name,
                    absolutePath = file.absolutePath,
                    sizeBytes = file.length(),
                )
            }
            .toList()

    fun importFromUri(context: Context, uri: Uri): ModelImportResult {
        val metadata = readMetadata(context, uri)
        val displayName = metadata.first ?: "imported-model.gguf"
        val declaredSize = metadata.second

        if (!displayName.endsWith(".gguf", ignoreCase = true)) {
            return ModelImportResult.Failure("selected file is not a .gguf model")
        }

        val directory = modelDirectory(context)
        if (!directory.exists() && !directory.mkdirs()) {
            return ModelImportResult.Failure("unable to create local model directory")
        }

        val availableBytes = StatFs(directory.absolutePath).availableBytes
        if (declaredSize != null && declaredSize > 0L) {
            val required = declaredSize + STORAGE_HEADROOM_BYTES
            if (required > availableBytes) {
                return ModelImportResult.Failure("not enough app storage to import this model safely")
            }
        }

        val safeName = sanitizeFileName(displayName)
        val target = uniqueTarget(directory, safeName)
        val temporary = File(directory, ".${target.name}.part")
        val digest = MessageDigest.getInstance("SHA-256")

        return runCatching {
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "unable to open selected model" }

                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(COPY_BUFFER_BYTES)
                    var totalBytes = 0L

                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        if (count == 0) continue

                        output.write(buffer, 0, count)
                        digest.update(buffer, 0, count)
                        totalBytes += count

                        if (totalBytes + STORAGE_HEADROOM_BYTES > availableBytes) {
                            throw IllegalStateException("model import exceeded available app storage")
                        }
                    }
                    output.fd.sync()
                }
            }

            if (!hasGgufMagic(temporary)) {
                throw IllegalArgumentException("selected file does not contain a valid GGUF header")
            }

            if (!temporary.renameTo(target)) {
                throw IllegalStateException("unable to finalize imported model file")
            }

            ModelImportResult.Success(
                LocalModelFile(
                    name = target.name,
                    absolutePath = target.absolutePath,
                    sizeBytes = target.length(),
                    sha256 = digest.digest().joinToString(separator = "") { byte ->
                        "%02x".format(byte.toInt() and 0xff)
                    },
                ),
            )
        }.getOrElse { failure ->
            temporary.delete()
            ModelImportResult.Failure(
                failure.message ?: "local model import failed",
            )
        }
    }

    private fun modelDirectory(context: Context): File =
        File(context.filesDir, MODEL_DIRECTORY)

    private fun readMetadata(context: Context, uri: Uri): Pair<String?, Long?> {
        var displayName: String? = null
        var size: Long? = null

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && !cursor.isNull(nameIndex)) {
                    displayName = cursor.getString(nameIndex)
                }

                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        return displayName to size
    }

    private fun sanitizeFileName(displayName: String): String {
        val leaf = displayName
            .substringAfterLast('/')
            .substringAfterLast('\\')
            .trim()
            .ifBlank { "imported-model.gguf" }

        return buildString(leaf.length) {
            leaf.forEach { char ->
                append(
                    when {
                        char == '/' || char == '\\' || char.code < 32 -> '_'
                        else -> char
                    },
                )
            }
        }.let { sanitized ->
            if (sanitized.endsWith(".gguf", ignoreCase = true)) {
                sanitized
            } else {
                "$sanitized.gguf"
            }
        }
    }

    private fun uniqueTarget(directory: File, preferredName: String): File {
        val initial = File(directory, preferredName)
        if (!initial.exists()) return initial

        val base = preferredName.removeSuffix(".gguf").removeSuffix(".GGUF")
        var index = 2
        while (true) {
            val candidate = File(directory, "$base ($index).gguf")
            if (!candidate.exists()) return candidate
            index += 1
        }
    }

    private fun hasGgufMagic(file: File): Boolean {
        if (file.length() < 4L) return false

        val magic = ByteArray(4)
        FileInputStream(file).use { input ->
            var offset = 0
            while (offset < magic.size) {
                val read = input.read(magic, offset, magic.size - offset)
                if (read < 0) return false
                offset += read
            }
        }

        return magic[0] == 'G'.code.toByte() &&
            magic[1] == 'G'.code.toByte() &&
            magic[2] == 'U'.code.toByte() &&
            magic[3] == 'F'.code.toByte()
    }
}
