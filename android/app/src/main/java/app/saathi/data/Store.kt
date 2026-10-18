package app.saathi.data

import android.content.Context
import app.saathi.core.Contact
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class Settings(
    val name: String = "",
    val contacts: List<Contact> = emptyList(),
    val homes: Map<String, String> = emptyMap(),
    val phraseKey: String = "touched",
    val powerPresses: Int = 3,
    val communityConsent: Boolean = false,
    val onboarded: Boolean = false,
)

@Serializable
data class Evidence(val name: String, val sha256: String, val receivedAt: Long)

@Serializable
data class TripRecord(
    val caseId: String,
    val createdAt: Long,
    val mode: String,
    val app: String? = null,
    val plate: String? = null,
    val events: List<String> = emptyList(),
    val alerts: List<String> = emptyList(),
    val plateCheck: String? = null,
    val evidence: List<Evidence> = emptyList(),
    val seal: String? = null,
    val feltOff: String? = null,
)

/** Small JSON files in the app's private storage. */
object Store {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = false }

    fun settings(ctx: Context): Settings =
        runCatching { json.decodeFromString<Settings>(file(ctx, "settings.json").readText()) }
            .getOrDefault(Settings())

    fun save(ctx: Context, s: Settings) = file(ctx, "settings.json").writeText(json.encodeToString(s))

    fun records(ctx: Context): List<TripRecord> =
        runCatching { json.decodeFromString<List<TripRecord>>(file(ctx, "records.json").readText()) }
            .getOrDefault(emptyList())

    fun saveRecord(ctx: Context, r: TripRecord) {
        val all = records(ctx).filterNot { it.caseId == r.caseId } + r
        file(ctx, "records.json").writeText(json.encodeToString(all))
    }

    fun deleteRecord(ctx: Context, caseId: String) {
        file(ctx, "records.json").writeText(json.encodeToString(records(ctx).filterNot { it.caseId == caseId }))
    }

    private fun file(ctx: Context, name: String) = File(ctx.filesDir, name)
}
