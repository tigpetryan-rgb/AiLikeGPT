package com.ailikegpt.app.runtime

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

internal class Utf8IncrementalDecoder(
    private val emit: (String) -> Unit,
) {
    private val decoder = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE)
        .replaceWith("\uFFFD")

    private var carry = ByteArray(0)

    @Synchronized
    fun push(bytes: ByteArray) {
        if (bytes.isEmpty()) return

        val combined = if (carry.isEmpty()) {
            bytes
        } else {
            ByteArray(carry.size + bytes.size).also { merged ->
                carry.copyInto(merged, destinationOffset = 0)
                bytes.copyInto(merged, destinationOffset = carry.size)
            }
        }

        val input = ByteBuffer.wrap(combined)
        val output = CharBuffer.allocate(outputCapacity(combined.size))
        val result = decoder.decode(input, output, false)
        check(!result.isOverflow) { "UTF-8 decoder output buffer overflow" }
        check(!result.isError) { "UTF-8 decoder failed: $result" }

        emitOutput(output)
        carry = ByteArray(input.remaining()).also { remaining ->
            input.get(remaining)
        }
    }

    @Synchronized
    fun finish() {
        val input = ByteBuffer.wrap(carry)
        val output = CharBuffer.allocate(outputCapacity(carry.size + 4))

        val decodeResult = decoder.decode(input, output, true)
        check(!decodeResult.isOverflow) { "UTF-8 decoder output buffer overflow at stream end" }
        check(!decodeResult.isError) { "UTF-8 decoder failed at stream end: $decodeResult" }

        val flushResult = decoder.flush(output)
        check(!flushResult.isOverflow) { "UTF-8 decoder output buffer overflow while flushing" }
        check(!flushResult.isError) { "UTF-8 decoder flush failed: $flushResult" }

        emitOutput(output)
        carry = ByteArray(0)
        decoder.reset()
    }

    private fun outputCapacity(inputBytes: Int): Int {
        val estimated = (inputBytes * decoder.maxCharsPerByte()).toInt() + 8
        return estimated.coerceAtLeast(8)
    }

    private fun emitOutput(output: CharBuffer) {
        output.flip()
        if (output.hasRemaining()) {
            emit(output.toString())
        }
    }
}
