package com.zamcan.nisaacare.domain.relationship

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PairingExchangeTest {
    @Test
    fun code_is_short_lived_and_usable_before_expiry() {
        val now = Instant.parse("2026-09-25T18:00:00Z")
        val code = PairingExchange.createCode(now)
        assertTrue(code.value.matches(Regex("[A-Z0-9]+(-[A-Z0-9]+)?")))
        assertTrue(code.isUsable(now.plusSeconds(1)))
        assertTrue(!code.isUsable(code.expiresAt))
    }

    @Test
    fun qr_payload_round_trips_without_sensitive_data() {
        val now = Instant.parse("2026-09-25T18:00:00Z")
        val payload = PairingExchange.payload(
            token = "0123456789abcdef0123456789abcdef",
            inviterRole = "WOMAN",
            expiresAt = now.plusSeconds(600)
        )
        val parsed = PairingScannerParser.parse(payload.encode(), now.epochSecond)
        assertEquals(payload, parsed)
    }

    @Test
    fun expired_qr_payload_is_rejected() {
        val now = Instant.parse("2026-09-25T18:00:00Z")
        val payload = PairingExchange.payload(
            token = "0123456789abcdef0123456789abcdef",
            inviterRole = "WOMAN",
            expiresAt = now
        )
        assertNull(PairingScannerParser.parse(payload.encode(), now.epochSecond))
    }

    @Test
    fun whatsapp_message_includes_install_fallback() {
        val message = PairingExchange.whatsappMessage(
            "https://example.invalid/pair?t=temporary",
            "https://play.google.com/store/apps/details?id=com.zamcan.nisaacare"
        )
        assertTrue(message.contains("https://example.invalid/pair"))
        assertTrue(message.contains("com.zamcan.nisaacare"))
    }
}
