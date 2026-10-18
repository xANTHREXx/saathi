package app.saathi.alert

import app.saathi.BuildConfig
import app.saathi.core.Fix
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** The live-location page contacts open from the SMS. Needs data; everything else works without it. */
object LiveShare {
    private val http = OkHttpClient.Builder().callTimeout(4, TimeUnit.SECONDS).build()
    private val jsonType = "application/json".toMediaType()
    var token: String? = null
        private set

    /** Returns the page URL, or null when offline or the server is unreachable. */
    fun create(name: String, plate: String?): String? = runCatching {
        val body = """{"name":${q(name)},"plate":${plate?.let { q(it) } ?: "null"}}"""
        http.newCall(Request.Builder().url("${BuildConfig.BASE_URL}/live").post(body.toRequestBody(jsonType)).build())
            .execute().use { r ->
                if (!r.isSuccessful) return null
                val o = Json.parseToJsonElement(r.body!!.string()).jsonObject
                token = o["token"]!!.jsonPrimitive.content
                o["url"]!!.jsonPrimitive.content
            }
    }.getOrNull()

    fun push(fix: Fix, events: List<String>) {
        val t = token ?: return
        runCatching {
            val ev = events.joinToString(",") { q(it) }
            val body = """{"lat":${fix.lat},"lng":${fix.lng},"t":${fix.t},"events":[$ev]}"""
            http.newCall(Request.Builder().url("${BuildConfig.BASE_URL}/live/$t").post(body.toRequestBody(jsonType)).build())
                .execute().close()
        }
    }

    fun stop() { token = null }

    private fun q(s: String) = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}
