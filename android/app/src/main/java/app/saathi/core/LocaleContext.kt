package app.saathi.core

private val stateLanguage = mapOf(
    "karnataka" to "kn-IN",
    "maharashtra" to "mr-IN",
    "tamil nadu" to "ta-IN",
    "telangana" to "te-IN",
    "andhra pradesh" to "te-IN",
    "kerala" to "ml-IN",
    "west bengal" to "bn-IN",
    "gujarat" to "gu-IN",
)

/** Speak-for-me and companion languages: local first, then Hindi, then English. Indian languages only. */
fun languagesFor(countryCode: String?, state: String?): List<String> {
    val local = if (countryCode.equals("IN", ignoreCase = true)) {
        stateLanguage[state?.trim()?.lowercase()] ?: "hi-IN"
    } else "hi-IN"
    return listOf(local, "hi-IN", "en-IN").distinct()
}
