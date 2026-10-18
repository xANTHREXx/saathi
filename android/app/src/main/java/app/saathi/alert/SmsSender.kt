package app.saathi.alert

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import app.saathi.core.Contact

/** Sends from her own SIM, so alerts go out with no mobile data. */
object SmsSender {
    data class Result(val sent: List<String>, val failed: List<String>)

    fun send(ctx: Context, to: List<Contact>, body: String): Result {
        val sms = ctx.getSystemService(SmsManager::class.java)
        val sent = mutableListOf<String>()
        val failed = mutableListOf<String>()
        for (c in to) {
            try {
                val parts = sms.divideMessage(body)
                sms.sendMultipartTextMessage(c.phone, null, parts, null, null)
                sent += c.name
                Log.i("Saathi", "SMS to ${c.name}")
            } catch (e: Exception) {
                failed += c.name
                Log.w("Saathi", "SMS to ${c.name} failed: ${e.message}")
            }
        }
        return Result(sent, failed)
    }
}
