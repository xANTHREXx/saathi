package app.saathi.data

import android.content.Context
import android.provider.Settings
import app.saathi.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import java.util.concurrent.TimeUnit

/** Calls to the Saathi server. Every call can fail; callers fall back to what the phone knows. */
object Api {
    private val http = OkHttpClient.Builder().callTimeout(12, TimeUnit.SECONDS).build()
    private val jsonType = "application/json".toMediaType()

    data class Flag(val reports: Int, val flagged: Boolean)

    fun vehicle(plate: String): Flag? = runCatching {
        get("/community/vehicle?plate=${java.net.URLEncoder.encode(plate, "UTF-8")}")?.let {
            Flag(it["reports"]?.jsonPrimitive?.intOrNull ?: 0, it["flagged"]?.jsonPrimitive?.booleanOrNull ?: false)
        }
    }.getOrNull()

    /** Plate read from a photo by Gemini; null when the server or the model is unavailable. */
    fun readPlate(jpeg: ByteArray): String? = runCatching {
        val b64 = Base64.getEncoder().encodeToString(jpeg)
        post("/trip/read", """{"image_b64":"$b64","mime":"image/jpeg"}""")?.get("plate")?.jsonPrimitive?.content
            ?.takeIf { it != "null" && it.isNotBlank() }
    }.getOrNull()

    fun point(ctx: Context, lat: Double, lng: Double, t: Long, kind: String, plate: String?) = runCatching {
        val p = plate?.let { "\"$it\"" } ?: "null"
        post("/community/point", """{"lat":$lat,"lng":$lng,"t":$t,"kind":"$kind","plate":$p}""", device(ctx))
    }

    fun device(ctx: Context): String = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    private fun get(path: String): JsonObject? =
        http.newCall(Request.Builder().url(BuildConfig.BASE_URL + path).build()).execute().use { r ->
            if (r.isSuccessful) Json.parseToJsonElement(r.body!!.string()).jsonObject else null
        }

    private fun post(path: String, body: String, deviceId: String? = null): JsonObject? {
        val req = Request.Builder().url(BuildConfig.BASE_URL + path).post(body.toRequestBody(jsonType))
        deviceId?.let { req.header("x-saathi-device", it) }
        return http.newCall(req.build()).execute().use { r ->
            if (r.isSuccessful) Json.parseToJsonElement(r.body!!.string()).jsonObject else null
        }
    }
}
