package com.zamcan.nisaacare.domain.relationship

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

enum class PairingMethod { CODE, QR, WHATSAPP }

data class PairingCode(
    val value: String,
    val createdAt: Instant,
    val expiresAt: Instant
) {
    fun isUsable(now: Instant = Instant.now()): Boolean = now.isBefore(expiresAt)
}

data class PairingPayload(
    val token: String,
    val inviterRole: String,
    val expiresAtEpochSeconds: Long,
    val protocolVersion: Int = 1
) {
    fun encode(): String =
        "nisaacare://pair?v=" + protocolVersion +
            "&t=" + token.urlEncode() +
            "&r=" + inviterRole.urlEncode() +
            "&e=" + expiresAtEpochSeconds

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
}

object PairingExchange {
    private val random = SecureRandom()

    fun createCode(
        now: Instant = Instant.now(),
        lifetime: Duration = Duration.ofMinutes(10)
    ): PairingCode {
        val bytes = ByteArray(5)
        random.nextBytes(bytes)
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
            .uppercase()
            .replace("-", "")
            .replace("_", "")
        val value = encoded.chunked(4).joinToString("-").take(11)
        return PairingCode(value, now, now.plus(lifetime))
    }

    fun payload(
        token: String,
        inviterRole: String,
        expiresAt: Instant
    ): PairingPayload = PairingPayload(token, inviterRole, expiresAt.epochSecond)

    fun whatsappMessage(pairingLink: String, installLink: String): String =
        "NISAA CARE pairing invitation\n\n" +
            "Open the pairing link on your phone:\n" + pairingLink + "\n\n" +
            "If NISAA CARE is not installed, install it here:\n" + installLink + "\n\n" +
            "The invitation is temporary. Do not forward it to anyone else."
}
