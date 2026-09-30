package com.zamcan.nisaacare.domain.relationship

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
        val trimmed = raw.trim()
        val prefix = "nisaacare://pair"
        if (!trimmed.startsWith(prefix, ignoreCase = true)) return null

        val query = trimmed.substring(prefix.length).removePrefix("?").removePrefix("/")
        if (query.isEmpty()) return null

        val parameters = mutableMapOf<String, String>()
        query.split("&").forEach { part ->
            val name = part.substringBefore("=", missingDelimiterValue = "").trim()
            val value = part.substringAfter("=", missingDelimiterValue = "").trim()
            if (name.isNotEmpty()) parameters[name.lowercase()] = decode(value)
        }

        val token = parameters["t"]?.takeIf { it.length >= 16 } ?: return null
        val role = parameters["r"]?.takeIf { it.isNotBlank() } ?: return null
        val expires = parameters["e"]?.toLongOrNull() ?: return null
        val version = parameters["v"]?.toIntOrNull() ?: return null

        if (version != 1 || expires <= nowEpochSeconds) return null

        return PairingPayload(token, role, expires, version)
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.toString()) }
            .getOrDefault(value)
}
