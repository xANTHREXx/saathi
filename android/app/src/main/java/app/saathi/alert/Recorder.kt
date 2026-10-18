package app.saathi.alert

import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import java.io.File

/** Records audio to the app's private storage. The file never leaves the phone. */
class Recorder(private val ctx: Context) {
    private var rec: MediaRecorder? = null
    var file: File? = null
        private set

    fun start(): Boolean {
        if (rec != null) return true
        return try {
            val f = File(ctx.filesDir, "rec-${System.currentTimeMillis()}.m4a")
            rec = MediaRecorder(ctx).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(f.absolutePath)
                prepare()
                start()
            }
            file = f
            true
        } catch (e: Exception) {
            Log.w("Saathi", "recording unavailable: ${e.message}")
            rec = null
            false
        }
    }

    fun stop(): File? {
        rec?.runCatching { stop(); release() }
        rec = null
        return file
    }
}
