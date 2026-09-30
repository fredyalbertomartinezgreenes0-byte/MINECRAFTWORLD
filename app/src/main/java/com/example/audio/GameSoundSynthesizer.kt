package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

object GameSoundSynthesizer {
    private const val SAMPLE_RATE = 22050
    private val scope = CoroutineScope(Dispatchers.Default)
    private val random = Random()
    var soundEnabled: Boolean = true
    var volume: Float = 0.8f

    private fun playPcm(pcm: ShortArray) {
        if (!soundEnabled || volume <= 0f) return
        scope.launch {
            try {
                // Apply volume
                val adjusted = ShortArray(pcm.size)
                for (i in pcm.indices) {
                    adjusted[i] = (pcm[i] * volume).toInt().coerceIn(-32768, 32767).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(adjusted.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(adjusted, 0, adjusted.size)
                audioTrack.play()
                // Auto release after sound finishes
                Thread.sleep((adjusted.size * 1000L / SAMPLE_RATE) + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio errors gracefully
            }
        }
    }

    fun playBlockBreak() {
        val duration = (SAMPLE_RATE * 0.12f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val env = 1f - (i.toFloat() / duration)
            val noise = (random.nextFloat() * 2f - 1f)
            val tone = sin(2 * PI * (140 - i * 0.1f) * i / SAMPLE_RATE).toFloat()
            buffer[i] = ((noise * 0.7f + tone * 0.3f) * env * 24000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playBlockPlace() {
        val duration = (SAMPLE_RATE * 0.08f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val env = 1f - (i.toFloat() / duration)
            val tone = sin(2 * PI * 220 * (1f - i.toFloat() / duration) * i / SAMPLE_RATE).toFloat()
            val click = if (i < 40) (random.nextFloat() * 2f - 1f) else 0f
            buffer[i] = ((tone * 0.6f + click * 0.4f) * env * 22000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playFootstep() {
        val duration = (SAMPLE_RATE * 0.05f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val env = 1f - (i.toFloat() / duration)
            val noise = (random.nextFloat() * 2f - 1f) * 0.4f
            buffer[i] = (noise * env * 12000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playHit() {
        val duration = (SAMPLE_RATE * 0.15f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val env = 1f - (i.toFloat() / duration)
            val tone = sin(2 * PI * (350 - i * 0.3f) * i / SAMPLE_RATE).toFloat()
            val noise = (random.nextFloat() * 2f - 1f) * 0.5f
            buffer[i] = ((tone * 0.5f + noise * 0.5f) * env * 26000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playExplosion() {
        val duration = (SAMPLE_RATE * 0.45f).toInt()
        val buffer = ShortArray(duration)
        var lastNoise = 0f
        for (i in 0 until duration) {
            val env = (1f - (i.toFloat() / duration))
            // Low-pass filtered noise for rumble
            val white = (random.nextFloat() * 2f - 1f)
            lastNoise = lastNoise * 0.85f + white * 0.15f
            val subBass = sin(2 * PI * (70 - i * 0.08f) * i / SAMPLE_RATE).toFloat()
            buffer[i] = ((lastNoise * 0.7f + subBass * 0.3f) * env * 28000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playPortalWarp() {
        val duration = (SAMPLE_RATE * 0.6f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val t = i.toFloat() / SAMPLE_RATE
            val freq = 200 + sin(2 * PI * 6 * t) * 80 + t * 300
            val wave = sin(2 * PI * freq * t).toFloat()
            val env = if (i < duration * 0.5f) (i.toFloat() / (duration * 0.5f)) else (1f - (i - duration * 0.5f) / (duration * 0.5f))
            buffer[i] = (wave * env * 20000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playItemPickup() {
        val duration = (SAMPLE_RATE * 0.14f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val env = 1f - (i.toFloat() / duration)
            val freq = if (i < duration / 2) 880f else 1320f
            val wave = sin(2 * PI * freq * i / SAMPLE_RATE).toFloat()
            buffer[i] = (wave * env * 18000).toInt().toShort()
        }
        playPcm(buffer)
    }

    fun playBossRoar() {
        val duration = (SAMPLE_RATE * 0.7f).toInt()
        val buffer = ShortArray(duration)
        for (i in 0 until duration) {
            val t = i.toFloat() / SAMPLE_RATE
            val env = (1f - (i.toFloat() / duration))
            val roarFreq = 85.0 + sin(2 * PI * 12.0 * t) * 25.0
            val sub = sin(2 * PI * roarFreq * t).toFloat()
            val distortion = if (sub > 0.4f) 0.8f else if (sub < -0.4f) -0.8f else sub * 2f
            buffer[i] = (distortion * env * 25000).toInt().toShort()
        }
        playPcm(buffer)
    }
}
