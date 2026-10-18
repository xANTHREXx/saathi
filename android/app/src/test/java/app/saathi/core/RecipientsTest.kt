package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipientsTest {
    private val amma = Contact("Amma", "+919800000001", "anywhere")
    private val riya = Contact("Riya", "+919800000002", "Bengaluru")
    private val aditi = Contact("Aditi", "+919800000003", "Pune")

    @Test
    fun recipientsCityPlusAnywhere() {
        assertEquals(listOf(amma, riya), recipients(listOf(amma, riya, aditi), " bengaluru "))
    }

    @Test
    fun recipientsNoneMatchMeansAllSaved() {
        val mumbai = Contact("Meera", "+919800000004", "Mumbai")
        assertEquals(listOf(aditi, mumbai), recipients(listOf(aditi, mumbai), "Delhi"))
    }

    @Test
    fun recipientsUnknownCityUsesAnywhereFirst() {
        assertEquals(listOf(amma), recipients(listOf(amma, aditi), null))
    }
}
