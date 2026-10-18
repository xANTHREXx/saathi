package app.saathi.core

import org.junit.Assert.assertEquals
import org.junit.Test

class FingerprintTest {
    @Test
    fun fingerprintKnownValue() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Fingerprint.sha256("abc".toByteArray()),
        )
    }
}
