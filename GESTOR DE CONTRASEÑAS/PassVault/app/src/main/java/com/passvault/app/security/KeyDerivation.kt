package com.passvault.app.security

import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Derivación PBKDF2 compartida entre la contraseña maestra y el código
 * de recuperación — misma lógica, mismo número de iteraciones.
 */
object KeyDerivation {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    const val DEFAULT_ITERATIONS = 210_000
    private const val DEFAULT_KEY_LENGTH_BITS = 256

    fun derive(
        input: CharArray,
        salt: ByteArray,
        iterations: Int = DEFAULT_ITERATIONS,
        keyLengthBits: Int = DEFAULT_KEY_LENGTH_BITS
    ): ByteArray {
        val spec: KeySpec = PBEKeySpec(input, salt, iterations, keyLengthBits)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}