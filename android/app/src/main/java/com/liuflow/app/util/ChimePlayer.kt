package com.liuflow.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.liuflow.app.FlowApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.sin

/**
 * Synthesises a 2-note ascending bell chime (C5 → E5) using [AudioTrack].
 *
 * Why not a recorded asset?
 *  - No APK bloat, no licensing, deterministic on every device.
 *  - The chime matches the "restraint" tone of the app: soft attack, fast
 *    exponential decay, short total duration (~900 ms).
 *
 * Safe to call from the main thread; playback runs on a background
 * dispatcher owned by the application scope.
 */
object ChimePlayer {

    private const val SAMPLE_RATE = 44_100
    private const val NOTE_MS = 400
    private const val GAP_MS = 100
    private const val TOTAL_MS = NOTE_MS * 2 + GAP_MS  // 900 ms
    private const val NOTE_1_HZ = 523.25f  // C5
    private const val NOTE_2_HZ = 659.25f  // E5
    private const val NOTE_1_AMP = 0.50f
    private const val NOTE_2_AMP = 0.45f

    fun play(context: Context) {
        val scope = (context.applicationContext as FlowApp).container.appScope
        scope.launch(Dispatchers.IO) {
            val buffer = synthesize()
            val track = buildTrack(buffer.size)
            try {
                track.write(buffer, 0, buffer.size)
                track.play()
                delay(TOTAL_MS.toLong() + 100L)
                track.stop()
            } catch (t: Throwable) {
                // AudioTrack failures (e.g. denied audio focus) should not
                // crash the app — just swallow and let the user notice.
            } finally {
                track.release()
            }
        }
    }

    private fun synthesize(): ShortArray {
        val totalSamples = SAMPLE_RATE * TOTAL_MS / 1000
        val buffer = ShortArray(totalSamples)
        val noteSamples = NOTE_MS * SAMPLE_RATE / 1000
        val gapSamples = GAP_MS * SAMPLE_RATE / 1000

        // Note 1: C5
        fillTone(
            buffer = buffer,
            startSample = 0,
            endSample = noteSamples,
            frequencyHz = NOTE_1_HZ,
            amplitude = NOTE_1_AMP,
        )
        // Gap is silent (ShortArray already zeroed)
        // Note 2: E5
        fillTone(
            buffer = buffer,
            startSample = noteSamples + gapSamples,
            endSample = noteSamples + gapSamples + noteSamples,
            frequencyHz = NOTE_2_HZ,
            amplitude = NOTE_2_AMP,
        )
        return buffer
    }

    private fun fillTone(
        buffer: ShortArray,
        startSample: Int,
        endSample: Int,
        frequencyHz: Float,
        amplitude: Float,
    ) {
        val length = endSample - startSample
        if (length <= 0) return
        for (i in 0 until length) {
            val t = i.toFloat() / SAMPLE_RATE
            val phase = i.toFloat() / length
            // 5% quick attack, then exponential decay
            val envelope = if (phase < 0.05f) {
                phase / 0.05f
            } else {
                exp(-(phase - 0.05f) * 4f)
            }
            val sample = (sin(2.0 * Math.PI * frequencyHz * t) * amplitude * envelope * Short.MAX_VALUE).toInt()
            buffer[startSample + i] = sample
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
    }

    private fun buildTrack(bufferSamples: Int): AudioTrack {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        ).coerceAtLeast(bufferSamples * 2)
        return AudioTrack.Builder()
            .setAudioAttributes(attrs)
            .setAudioFormat(format)
            .setBufferSizeInBytes(minBuffer)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
    }
}
