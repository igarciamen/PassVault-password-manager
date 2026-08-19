package com.passvault.app.domain

/**
 * Bloque 8 — análisis básico de fortaleza y reutilización, 100% local
 * (nunca sale del dispositivo, no se envía a ningún servidor).
 */
object PasswordStrengthChecker {

    private val COMMON_PASSWORDS = setOf(
        "123456", "password", "12345678", "qwerty", "111111", "123456789",
        "12345", "1234", "1234567", "contraseña", "abc123", "000000"
    )

    fun isWeak(password: String): Boolean {
        if (password.length < 8) return true
        if (password.lowercase() in COMMON_PASSWORDS) return true
        if (!password.any { it.isDigit() }) return true
        if (!password.any { it.isLetter() }) return true
        if (hasSequentialChars(password)) return true
        return false
    }

    private fun hasSequentialChars(password: String): Boolean {
        if (password.length < 4) return false
        for (i in 0..password.length - 4) {
            val a = password[i]
            val b = password[i + 1]
            val c = password[i + 2]
            val d = password[i + 3]
            if (b == a + 1 && c == b + 1 && d == c + 1) return true
        }
        return false
    }

    /** Agrupa entradas que comparten exactamente la misma contraseña. */
    fun findReusedPasswords(entries: List<PasswordEntry>): List<List<PasswordEntry>> =
        entries.groupBy { it.password }.values.filter { it.size > 1 }
}