package com.ailikegpt.app.runtime

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Test

class Utf8IncrementalDecoderTest {
    @Test
    fun decodesArmenianAndEmojiAcrossSingleByteChunks() {
        val expected = "Բարև աշխարհ 🌍 — offline AI"
        val emitted = StringBuilder()
        val decoder = Utf8IncrementalDecoder(emitted::append)

        expected.toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            decoder.push(byteArrayOf(byte))
        }
        decoder.finish()

        assertEquals(expected, emitted.toString())
    }

    @Test
    fun replacesIncompleteTrailingUtf8SequenceAtStreamEnd() {
        val emitted = StringBuilder()
        val decoder = Utf8IncrementalDecoder(emitted::append)

        decoder.push(byteArrayOf(0xE2.toByte(), 0x82.toByte()))
        decoder.finish()

        assertEquals("�", emitted.toString())
    }
}
