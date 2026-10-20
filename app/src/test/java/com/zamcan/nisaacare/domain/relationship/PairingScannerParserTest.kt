package com.zamcan.nisaacare.domain.relationship

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/**
 * Tests for the pairing payload parser.
 *
 * The parser accepts two wire formats, because a pairing code may have been
 * produced by a different build of the app than the one receiving it. These
 * tests cover both, and the rejections that keep a malformed or expired
 * payload from reaching the repository.
 */
class PairingScannerParserTest {

    private val now = Instant.parse("2026-09-25T18:00:00Z")

    @Test
    fun currentFormatParses() {
        val payload = PairingExchange.payload(
            token = "0123456789abcdef0123456789abcdef",
            inviterRole = "WOMAN",
            expiresAt = now.plusSeconds(600)
        )
        val parsed = PairingScannerParser.parse(payload.encode(), now.epochSecond)
        assertEquals(payload, parsed)
    }

    @Test
    fun legacyFormatParses() {
        // The legacy format carried the expiry in milliseconds and used
        // different parameter names.
        val raw = "nisaacare://pair?token=0123456789abcdef0123456789abcdef" +
            "&inviter=user-123&role=WOMAN&expires=${now.plusSeconds(600).toEpochMilli()}"
        val parsed = PairingScannerParser.parse(raw, now.epochSecond)
        assertNotNull(parsed)
        assertEquals("0123456789abcdef0123456789abcdef", parsed?.token)
        assertEquals("WOMAN", parsed?.inviterRole)
        assertEquals(now.plusSeconds(600).epochSecond, parsed?.expiresAtEpochSeconds)
    }

    @Test
    fun expiredPayloadIsRejectedInBothFormats() {
        val expired = PairingExchange.payload(
            token = "0123456789abcdef0123456789abcdef",
            inviterRole = "WOMAN",
            expiresAt = now
        )
        assertNull(PairingScannerParser.parse(expired.encode(), now.epochSecond))

        val legacyExpired = "nisaacare://pair?token=0123456789abcdef0123456789abcdef" +
            "&inviter=user-123&role=WOMAN&expires=${now.toEpochMilli()}"
        assertNull(PairingScannerParser.parse(legacyExpired, now.epochSecond))
    }

    @Test
    fun nonPairingLinkIsRejected() {
        assertNull(PairingScannerParser.parse("https://example.invalid/pair?t=abc", now.epochSecond))
        assertNull(PairingScannerParser.parse("nisaacare://other?t=abc", now.epochSecond))
        assertNull(PairingScannerParser.parse("", now.epochSecond))
    }

    @Test
    fun shortTokenIsRejected() {
        // A real pairing token is a long random string. Anything shorter is
        // rejected before it reaches the repository.
        val raw = "nisaacare://pair?v=1&t=short&r=WOMAN&e=${now.plusSeconds(600).epochSecond}"
        assertNull(PairingScannerParser.parse(raw, now.epochSecond))
    }

    @Test
    fun unknownProtocolVersionIsRejected() {
        val raw = "nisaacare://pair?v=2&t=0123456789abcdef0123456789abcdef" +
            "&r=WOMAN&e=${now.plusSeconds(600).epochSecond}"
        assertNull(PairingScannerParser.parse(raw, now.epochSecond))
    }

    @Test
    fun encodedCharactersAreDecoded() {
        val role = "HUSBAND"
        val raw = "nisaacare://pair?v=1&t=0123456789abcdef0123456789abcdef" +
            "&r=$role&e=${now.plusSeconds(600).epochSecond}"
        val parsed = PairingScannerParser.parse(raw, now.epochSecond)
        assertEquals(role, parsed?.inviterRole)
    }
}
