package com.zamcan.nisaacare.domain.relationship

import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.UserRole
import java.time.Instant
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Transport-neutral pairing payload.
 *
 * It contains only the short-lived pairing credential and inviter metadata.
 * It deliberately contains no health, cycle, fertility, relationship-permission,
 * or other private health data.
 */
data class PairingPayload(
    val token: String,
    val inviterUserId: String,
    val inviterRole: UserRole,
    val expiresAt: Instant
) {
    fun toInvitation(): PairingInvitation = PairingInvitation(
        token = token,
        inviterUserId = inviterUserId,
        inviterRole = inviterRole,
        expiresAt = expiresAt
    )

    fun toUri(): String {
        val query = listOf(
            "token" to token,
            "inviter" to inviterUserId,
            "role" to inviterRole.name,
            "expires" to expiresAt.toEpochMilli().toString()
        ).joinToString("&") { (key, value) ->
            "${key}=${URLEncoder.encode(value, StandardCharsets.UTF_8.name())}"
        }
        return "nisaacare://pair?$query"
    }

    companion object {
        fun fromInvitation(invitation: PairingInvitation): PairingPayload = PairingPayload(
            token = invitation.token,
            inviterUserId = invitation.inviterUserId,
            inviterRole = invitation.inviterRole,
            expiresAt = invitation.expiresAt
        )

        fun parse(raw: String): PairingPayload? = runCatching {
            val uri = android.net.Uri.parse(raw.trim())
            if (uri.scheme != "nisaacare" || uri.host != "pair") return@runCatching null
            val token = uri.getQueryParameter("token")?.trim().orEmpty()
            val inviter = uri.getQueryParameter("inviter")?.trim().orEmpty()
            val role = uri.getQueryParameter("role")?.trim()?.let {
                UserRole.entries.firstOrNull { role -> role.name == it }
            } ?: return@runCatching null
            val expires = uri.getQueryParameter("expires")?.toLongOrNull() ?: return@runCatching null
            if (token.isBlank() || inviter.isBlank()) return@runCatching null
            PairingPayload(token, inviter, role, Instant.ofEpochMilli(expires))
        }.getOrNull()
    }
}

object PairingLinks {
    const val INSTALL_URL = "https://play.google.com/store/apps/details?id=com.zamcan.nisaacare"
}
