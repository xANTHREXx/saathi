package app.saathi.core

object Plate {
    // State code, district number, optional series, four digits: "KA 05 AB 1234".
    private val indian = Regex("""\b([A-Z]{2})[\s-]?(\d{1,2})[\s-]?([A-Z]{0,3})[\s-]?(\d{4})\b""")

    fun normalise(s: String): String = s.uppercase().filter { it.isLetterOrDigit() }

    /** O and 0, I and 1 are the same here, so a camera misread doesn't raise a false mismatch. */
    fun matches(booked: String, seen: String): Boolean = canonical(booked) == canonical(seen)

    /** First Indian plate in free text, such as a ride-app notification. Works offline. */
    fun find(text: String): String? =
        indian.find(text.uppercase())?.groupValues?.drop(1)?.joinToString("")

    private fun canonical(s: String) = normalise(s).replace('O', '0').replace('I', '1')
}
