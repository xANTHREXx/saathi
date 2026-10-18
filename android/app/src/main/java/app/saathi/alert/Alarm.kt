package app.saathi.alert

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.VibrationEffect
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/**
 * The check-in sound: two tones that rise in pitch, played on the alarm stream so it sounds
 * even when the phone is on silent. Generated, so there is no audio file to go missing.
 */
class Alarm(private val ctx: Context) {
    private var track: AudioTrack? = null

    fun start() {
        stop()
        val am = ctx.getSystemService(AudioManager::class.java)
        am.setStreamVolume(AudioManager.STREAM_ALARM, am.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        val rate = 22_050
        val pcm = chirp(rate)
        val t = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(pcm.size * 2)
            .build()
        t.write(pcm, 0, pcm.size)
        t.setLoopPoints(0, pcm.size, -1)
        t.play()
        track = t
        vibrate(longArrayOf(0, 600, 300, 600, 300, 600), repeat = 0)
    }

    fun stop() {
        track?.runCatching { stop(); release() }
        track = null
        ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator?.cancel()
    }

    fun buzzOnce() = vibrate(longArrayOf(0, 120), repeat = -1)

    private fun vibrate(pattern: LongArray, repeat: Int) {
        ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator
            ?.vibrate(VibrationEffect.createWaveform(pattern, repeat))
    }

    // 0.35 s at 880 Hz, 0.35 s at 1320 Hz, 0.3 s silence.
    private fun chirp(rate: Int): ShortArray {
        val out = ArrayList<Short>()
        for ((hz, sec) in listOf(880.0 to 0.35, 1320.0 to 0.35, 0.0 to 0.3)) {
            val n = (rate * sec).toInt()
            for (i in 0 until n) {
                val v = if (hz == 0.0) 0.0 else sin(2 * PI * hz * i / rate) * 0.9
                out += (v * Short.MAX_VALUE).toInt().toShort()
            }
        }
        return out.toShortArray()
    }
}
