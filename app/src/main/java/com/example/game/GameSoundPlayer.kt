package com.example.game

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioAttributes
import android.os.Build
import android.util.Log
import kotlin.concurrent.thread
import kotlin.math.sin

object GameSoundPlayer {
    private const val TAG = "GameSoundPlayer"
    private const val SAMPLE_RATE = 22050

    /**
     * Synthesizes 16-bit PCM mono square/sine audio and writes it directly to AudioTrack.
     * Played inside a safe thread block to ensure zero UI thread stuttering or blocking.
     */
    private fun playSynthBeeps(notes: List<Pair<Int, Int>>) {
        thread {
            try {
                val minBufferSize = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
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
                        .setBufferSizeInBytes(maxOf(minBufferSize, 4096))
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        maxOf(minBufferSize, 4096),
                        AudioTrack.MODE_STREAM
                    )
                }

                audioTrack.play()

                for ((freq, durationMs) in notes) {
                    val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                    val sample = ShortArray(numSamples)
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / SAMPLE_RATE
                        // Smoothly fade out the last 20% of the sample duration to prevent pop/click noise
                        val fadeFactor = if (i > numSamples * 0.8) {
                            (numSamples - i).toDouble() / (numSamples * 0.2)
                        } else {
                            1.0
                        }
                        
                        // We use a clean sine wave with a subtle secondary harmonic for 8-bit retro gaming warmth
                        val sine1 = sin(2.0 * java.lang.Math.PI * freq * t)
                        val sine2 = sin(4.0 * java.lang.Math.PI * freq * t) * 0.25
                        val mixedSine = (sine1 + sine2) / 1.25
                        
                        val sampleVal = mixedSine * 16383.0 * fadeFactor
                        sample[i] = sampleVal.toInt().toShort()
                    }
                    audioTrack.write(sample, 0, numSamples)
                }

                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom synth chime: ${e.message}")
                fallbackTone()
            }
        }
    }

    private fun fallbackTone() {
        try {
            val tg = android.media.ToneGenerator(AudioManager.STREAM_MUSIC, 70)
            tg.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 200)
        } catch (e: Exception) {
            Log.e(TAG, "Failed playing fallback beep: ${e.message}")
        }
    }

    /**
     * Plays a beautiful 8-bit arcade arpeggio to denote strategic tech development or employee upgrade success.
     */
    fun playUpgradeSuccessSound() {
        val notes = listOf(
            Pair(523, 70),  // C5
            Pair(659, 70),  // E5
            Pair(784, 70),  // G5
            Pair(1046, 200) // C6
        )
        playSynthBeeps(notes)
    }

    /**
     * Plays an advanced cosmic rocket-launch SFX chime for launching the new phone.
     */
    fun playPhoneLaunchSound() {
        val notes = listOf(
            Pair(294, 50),  // D4
            Pair(440, 50),  // A4
            Pair(587, 50),  // D5
            Pair(880, 50),  // A5
            Pair(1174, 50), // D6
            Pair(1760, 250) // A6
        )
        playSynthBeeps(notes)
    }

    /**
     * Plays a pleasant quick synth-chirp during interactive configurations (optional).
     */
    fun playInteractSound() {
        val notes = listOf(
            Pair(880, 45) // A5 (very fast)
        )
        playSynthBeeps(notes)
    }
}
