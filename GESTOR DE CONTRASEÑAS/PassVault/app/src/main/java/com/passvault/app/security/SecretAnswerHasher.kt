package com.passvault.app.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hashea la respuesta de una "pregunta secreta" con PBKDF2 + sal,
 * igual que la contraseña maestra: la respuesta real nunca se guarda
 * ni se puede recuperar, solo verificar.
 */
object SecretAnswerHasher {

    private const val ITERATIONS = 150_000
    private const val KEY_LENGTH_BITS = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    data class HashResult(val hash: String, val salt: String)

    fun hash(answer: String, salt: ByteArray = generateSalt()): HashResult {
        val hashBytes = deriveHash(normalize(answer), salt)
        return HashResult(
            hash = Base64.getEncoder().encodeToString(hashBytes),
            salt = Base64.getEncoder().encodeToString(salt)
        )
    }

    fun verify(answer: String, storedHash: String, storedSalt: String): Boolean {
        val salt = Base64.getDecoder().decode(storedSalt)
        val expectedHash = Base64.getDecoder().decode(storedHash)
        val candidateHash = deriveHash(normalize(answer), salt)
        return constantTimeEquals(expectedHash, candidateHash)
    }

    private fun deriveHash(normalizedAnswer: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(normalizedAnswer.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    private fun normalize(answer: String): String = answer.trim().lowercase()

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) result = result or (a[i].toInt() xor b[i].toInt())
        return result == 0
    }
}