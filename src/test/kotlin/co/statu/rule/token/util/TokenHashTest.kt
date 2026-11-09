package co.statu.rule.token.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenHashTest {
    private val jwt = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwb3J0YWwifQ.c2lnbmF0dXJl"

    @Test
    fun `new rows hold the sha-256 of the token`() {
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", TokenHash.hash("hello"))
        assertTrue(TokenHash.looksHashed(TokenHash.hash(jwt)))
    }

    @Test
    fun `a new hashed row and an old raw row both match the presented token`() {
        assertTrue(TokenHash.matches(TokenHash.hash(jwt), jwt))
        assertTrue(TokenHash.matches(jwt, jwt))
        assertTrue(TokenHash.matches("abcde12345", "abcde12345"))
        assertFalse(TokenHash.matches(TokenHash.hash(jwt), "$jwt-other"))
        assertFalse(TokenHash.matches(TokenHash.hash(jwt), null))
    }

    @Test
    fun `a hash read from the table is never accepted as the token`() {
        val stored = TokenHash.hash(jwt)

        assertFalse(TokenHash.matches(stored, stored))
        assertEquals(TokenHash.hash(stored) to TokenHash.hash(stored), TokenHash.lookupValues(stored))
    }

    @Test
    fun `revoking every token except the live one keeps the live one in old and new rows`() {
        val live = "$jwt.live"
        val rows = listOf(TokenHash.hash(live), live, TokenHash.hash("$jwt.old"), "$jwt.older")

        val revoked = rows.filter { !TokenHash.matches(it, live) }

        assertEquals(listOf(TokenHash.hash("$jwt.old"), "$jwt.older"), revoked)
        assertEquals(rows, rows.filter { !TokenHash.matches(it, null) })
    }
}
