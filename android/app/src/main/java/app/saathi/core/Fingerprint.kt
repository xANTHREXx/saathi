package app.saathi.core

import java.security.MessageDigest

/** SHA-256 of a file's exact bytes, as 64 hex characters. The server keeps this, not the file. */
object Fingerprint {
    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
