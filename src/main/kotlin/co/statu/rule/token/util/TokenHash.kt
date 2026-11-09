package co.statu.rule.token.util

import java.security.MessageDigest

/**
 * Tokens are kept in the database only as their SHA-256 (lower-case hex), so a leaked table or backup holds no
 * usable session, API token or code.
 *
 * Rows written before this change still hold the raw value. They keep working until they expire: a lookup tries the
 * hash and the raw value. The raw value is only tried when it does not itself look like a hash (64 hex chars), so a
 * hash read from the table can never be presented as the token. No token this backend issues is 64 hex chars.
 */
object TokenHash {
    private val HASH = Regex("^[0-9a-f]{64}$")

    fun hash(raw: String): String =
        MessageDigest.getInstance("SHA-256").digest(raw.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    /** Whether [value] has the shape of a stored hash. */
    fun looksHashed(value: String) = HASH.matches(value)

    /** The two values a lookup for [raw] matches: its hash, and [raw] itself for old rows (or the hash again). */
    fun lookupValues(raw: String): Pair<String, String> {
        val hashed = hash(raw)

        return hashed to (if (looksHashed(raw)) hashed else raw)
    }

    /** Whether the stored [storedToken] (a hash, or an old raw row) belongs to the presented [raw] token. */
    fun matches(storedToken: String, raw: String?): Boolean {
        if (raw == null) return false

        val (hashed, legacy) = lookupValues(raw)

        return storedToken == hashed || storedToken == legacy
    }
}
