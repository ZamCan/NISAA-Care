package com.zamcan.nisaacare.domain.relationship

import android.net.Uri

/**
 * Platform boundary for QR scanning.
 * The domain does not depend on a camera or scanning library.
 */
interface PairingScanner {
    fun start()
    fun stop()
    fun onPayload(payload: String): PairingPayload?
}

object PairingScannerParser {
    fun parse(raw: String, nowEpochSeconds: Long): PairingPayload? {
        val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return null
        if (uri.scheme != "nisaacare" || uri.host != "pair") return null

        val token = uri.getQueryParameter("t")?.takeIf { it.length >= 16 } ?: return null
        val role = uri.getQueryParameter("r")?.takeIf { it.isNotBlank() } ?: return null
        val expires = uri.getQueryParameter("e")?.toLongOrNull() ?: return null
        val version = uri.getQueryParameter("v")?.toIntOrNull() ?: return null

        if (version != 1 || expires <= nowEpochSeconds) return null

        return PairingPayload(token, role, expires, version)
    }
}
