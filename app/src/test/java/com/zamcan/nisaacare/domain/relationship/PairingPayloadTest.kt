package com.zamcan.nisaacare.domain.relationship

import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PairingPayloadTest {
    @Test
    fun payload_preserves_only_pairing_metadata() {
        val invitation = PairingInvitation(
            token = "short-lived-token",
            inviterUserId = "user-123",
            inviterRole = UserRole.WOMAN,
            expiresAt = Instant.parse("2026-09-25T18:15:00Z")
        )

        val payload = PairingPayload.fromInvitation(invitation)

        assertEquals("short-lived-token", payload.token)
        assertEquals("user-123", payload.inviterUserId)
        assertEquals(UserRole.WOMAN, payload.inviterRole)
        assertTrue(payload.toUri().startsWith("nisaacare://pair?"))
        assertTrue(payload.toUri().contains("token=short-lived-token"))
        assertTrue(payload.toUri().contains("role=WOMAN"))
    }
}
