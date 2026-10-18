package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleContextTest {
    @Test
    fun karnatakaLanguages() {
        assertEquals(listOf("kn-IN", "hi-IN", "en-IN"), languagesFor("IN", "Karnataka"))
    }

    @Test
    fun maharashtraLanguages() {
        assertEquals(listOf("mr-IN", "hi-IN", "en-IN"), languagesFor("IN", "Maharashtra"))
    }

    @Test
    fun delhiHasNoDuplicateHindi() {
        assertEquals(listOf("hi-IN", "en-IN"), languagesFor("IN", "Delhi"))
    }

    @Test
    fun abroadIsHindiAndEnglish() {
        assertEquals(listOf("hi-IN", "en-IN"), languagesFor("DE", "Berlin"))
    }

    @Test
    fun unknownPlaceIsHindiAndEnglish() {
        assertEquals(listOf("hi-IN", "en-IN"), languagesFor(null, null))
    }
}
