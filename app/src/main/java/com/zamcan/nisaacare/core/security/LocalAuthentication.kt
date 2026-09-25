package com.zamcan.nisaacare.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local opt-in lock foundation. It is deliberately separate from health data
 * access so a future biometric/device-credential gate can wrap repositories
 * without changing domain rules.
 */
class LocalAuthentication(
    private val random: SecureRandom = SecureRandom()
) {
    fun createCredential(password: CharArray): StoredCredential {
        require(password.size >= MIN_PASSWORD_LENGTH) { "Password is too short" }
        val salt = ByteArray(SALT_BYTES).also(random::nextBytes)
        val hash = derive(password, salt)
        return StoredCredential(
            salt = salt.toHex(),
            hash = hash.toHex(),
            iterations = ITERATIONS
        )
    }

    fun verify(password: CharArray, credential: StoredCredential): Boolean {
        if (credential.iterations != ITERATIONS) return false
        val salt = credential.salt.hexToBytes()
        val expected = credential.hash.hexToBytes()
        val actual = derive(password, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { byte -> "%02x".format(byte) }

    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val ITERATIONS = 210_000
        private const val SALT_BYTES = 16
        private const val HASH_BITS = 256
    }
}

data class StoredCredential(
    val salt: String,
    val hash: String,
    val iterations: Int
)
