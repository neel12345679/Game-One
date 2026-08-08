package com.example.util

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class SoundManager(private val context: Context) {
    private var isMuted = false
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun playSplash() {
        if (isMuted) return
        vibrate(30)
        playSoundTone(startFreq = 440f, endFreq = 880f, durationMs = 80)
    }

    fun playCollectCoin() {
        if (isMuted) return
        vibrate(20)
        playSoundTone(startFreq = 880f, endFreq = 1320f, durationMs = 100)
    }

    fun playPowerup() {
        if (isMuted) return
        vibrate(60)
        scope.launch {
            playSoundTone(startFreq = 300f, endFreq = 900f, durationMs = 150)
            playSoundTone(startFreq = 900f, endFreq = 1500f, durationMs = 200)
        }
    }

    fun playObstacleHit() {
        if (isMuted) return
        vibrate(120)
        playSoundTone(startFreq = 220f, endFreq = 80f, durationMs = 200)
    }

    fun playLevelWin() {
        if (isMuted) return
        vibrate(150)
        scope.launch {
            playSoundTone(startFreq = 523.25f, endFreq = 523.25f, durationMs = 120) // C5
            playSoundTone(startFreq = 659.25f, endFreq = 659.25f, durationMs = 120) // E5
            playSoundTone(startFreq = 783.99f, endFreq = 783.99f, durationMs = 120) // G5
            playSoundTone(startFreq = 1046.50f, endFreq = 1046.50f, durationMs = 300) // C6
        }
    }

    private fun playSoundTone(startFreq: Float, endFreq: Float, durationMs: Int) {
        scope.launch {
            try {
                val sampleRate = 22050
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch

                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toFloat() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * progress
                    val angle = 2.0 * Math.PI * i * currentFreq / sampleRate
                    // Apply smooth envelope to prevent clicking
                    val envelope = sin(Math.PI * progress).toFloat()
                    buffer[i] = (sin(angle) * 16383 * envelope).toInt().toShort()
                }

                @Suppress("DEPRECATION")
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    AudioTrack.MODE_STATIC
                )
                track.write(buffer, 0, numSamples)
                track.play()
                // Release after sound finishes
                Thread.sleep(durationMs.toLong() + 50)
                track.release()
            } catch (_: Exception) {
                // Audio safety fallback
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(durationMs)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
