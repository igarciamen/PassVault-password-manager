package com.passvault.app.security

import java.security.SecureRandom

/**
 * Genera un código de recuperación aleatorio de alta entropía, siguiendo
 * el mismo patrón que 1Password (clave de 256 bits) o Bitwarden/KeePassXC
 * (recovery code de alta entropía guardado fuera del dispositivo).
 *
 * 24 caracteres útiles en 6 grupos de 4, sobre un alfabeto de 32 símbolos
 * sin caracteres ambiguos (sin 0/O, sin 1/I). Entropía: log2(32)*24 = 120 bits.
 */
object RecoveryCodeGenerator {
    const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private const val GROUP_SIZE = 4
    private const val GROUP_COUNT = 6

    fun generate(): String {
        val random = SecureRandom()
        return (1..GROUP_COUNT)
            .joinToString("-") {
                (1..GROUP_SIZE)
                    .map { ALPHABET[random.nextInt(ALPHABET.length)] }
                    .joinToString("")
            }
    }
}