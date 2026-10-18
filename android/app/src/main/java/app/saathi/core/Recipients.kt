package app.saathi.core

import kotlinx.serialization.Serializable

/** One of her Saathi emergency contacts. [city] is a city name or "anywhere". */
@Serializable
data class Contact(val name: String, val phone: String, val city: String)

const val ANYWHERE = "anywhere"

/**
 * Who gets an alert: contacts tagged with her current city plus "anywhere" contacts.
 * If that is nobody, every Saathi emergency contact. Never her phone book, never nobody.
 */
fun recipients(all: List<Contact>, city: String?): List<Contact> {
    val here = city?.trim()?.lowercase()
    val matched = all.filter {
        val tag = it.city.trim().lowercase()
        tag == ANYWHERE || (here != null && tag == here)
    }
    return matched.ifEmpty { all }
}
