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

/**
 * Parses a pairing payload from a scanned QR code or an imported link.
 *
 * Two wire formats are accepted, because a pairing code may have been produced
 * by a different build of the app than the one receiving it:
 *
 *  - **Current**: `nisaacare://pair?v=1&t=<token>&r=<role>&e=<expiry>`,
 *    produced by [PairingPayload.encode].
 *  - **Legacy**: `nisaacare://pair?token=<token>&inviter=<id>&role=<role>&expires=<expiry>`,
 *    produced by an earlier transport-neutral payload.
 *
 * Both carry only a short-lived pairing credential and inviter metadata. Neither
 * carries health, cycle, fertility or relationship-permission data.
 *
 * The parser is deliberately free of any Android framework call. Using
 * `android.net.Uri` here would make the domain untestable on the JVM, because
 * the framework stub throws outside an instrumented test.
 */
object PairingScannerParser {

    /** The scheme and host every pairing link uses. */
    private const val PREFIX = "nisaacare://pair"

    /**
     * Minimum token length accepted.
     *
     * A pairing token is a long random string, so anything shorter is not a
     * real credential and is rejected before it reaches the repository.
     */
    private const val MINIMUM_TOKEN_LENGTH = 16

    /**
     * Parses a raw pairing payload.
     *
     * @param raw the scanned or imported text.
     * @param nowEpochSeconds the current time, used to reject expired payloads.
     * @return the payload, or null when the input is not a valid pairing link
     *   or has already expired.
     */
    fun parse(raw: String, nowEpochSeconds: Long): PairingPayload? {
        val trimmed = raw.trim()
        if (!trimmed.startsWith(PREFIX, ignoreCase = true)) return null

        val query = trimmed.substring(PREFIX.length).removePrefix("?").removePrefix("/")
        if (query.isEmpty()) return null

        val parameters = mutableMapOf<String, String>()
        query.split("&").forEach { part ->
            val name = part.substringBefore("=", missingDelimiterValue = "").trim().lowercase()
            val value = part.substringAfter("=", missingDelimiterValue = "").trim()
            if (name.isNotEmpty()) parameters[name] = decode(value)
        }

        return parseCurrentFormat(parameters, nowEpochSeconds)
            ?: parseLegacyFormat(parameters, nowEpochSeconds)
    }

    /** The current format: `v`, `t`, `r`, `e`. */
    private fun parseCurrentFormat(
        parameters: Map<String, String>,
        nowEpochSeconds: Long
    ): PairingPayload? {
        val token = parameters["t"]?.takeIf { it.length >= MINIMUM_TOKEN_LENGTH } ?: return null
        val role = parameters["r"]?.takeIf { it.isNotBlank() } ?: return null
        val expires = parameters["e"]?.toLongOrNull() ?: return null
        val version = parameters["v"]?.toIntOrNull() ?: return null

        if (version != 1 || expires <= nowEpochSeconds) return null

        return PairingPayload(token, role, expires, version)
    }

    /**
     * The legacy format: `token`, `inviter`, `role`, `expires`.
     *
     * The expiry in this format is in milliseconds rather than seconds, which
     * is why the two formats cannot simply share one code path.
     */
    private fun parseLegacyFormat(
        parameters: Map<String, String>,
        nowEpochSeconds: Long
    ): PairingPayload? {
        val token = parameters["token"]?.takeIf { it.length >= MINIMUM_TOKEN_LENGTH } ?: return null
        val role = parameters["role"]?.takeIf { it.isNotBlank() } ?: return null
        val expiresMillis = parameters["expires"]?.toLongOrNull() ?: return null

        val expiresSeconds = expiresMillis / 1000
        if (expiresSeconds <= nowEpochSeconds) return null

        // The legacy format carried no protocol version, so it is recorded as
        // version 1, the only version that has ever existed.
        return PairingPayload(token, role, expiresSeconds, 1)
    }

    private fun decode(value: String): String =
        runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.toString()) }
            .getOrDefault(value)
}
