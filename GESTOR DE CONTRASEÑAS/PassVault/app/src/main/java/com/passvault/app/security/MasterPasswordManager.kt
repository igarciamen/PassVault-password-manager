package com.passvault.app.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

class MasterPasswordManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "passvault_master_password_prefs"
        private const val KEY_SALT = "master_salt"
        private const val KEY_VERIFIER = "master_verifier"
        private const val KEY_FAILED_ATTEMPTS = "master_failed_attempts"
        // ===== INICIO CAMBIOS BLOQUE 7 =====
        private const val KEY_LAST_FAILED_AT_MILLIS = "master_last_failed_at"
        private const val MAX_BACKOFF_SECONDS = 60
        // ===== FIN CAMBIOS BLOQUE 7 =====
        private const val SALT_LENGTH_BYTES = 16
        const val MAX_ATTEMPTS = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isMasterPasswordSet(): Boolean =
        prefs.contains(KEY_SALT) && prefs.contains(KEY_VERIFIER)

    fun createMasterPassword(password: CharArray): ByteArray = setPassword(password)

    fun changeMasterPassword(newPassword: CharArray): ByteArray {
        val derivedKey = setPassword(newPassword)
        resetFailedAttempts()
        return derivedKey
    }

    private fun setPassword(password: CharArray): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val derivedKey = KeyDerivation.derive(password, salt)

        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_VERIFIER, Base64.encodeToString(derivedKey, Base64.NO_WRAP))
            .apply()

        return derivedKey
    }

    fun verifyMasterPassword(password: CharArray): ByteArray? {
        val saltEncoded = prefs.getString(KEY_SALT, null) ?: return null
        val verifierEncoded = prefs.getString(KEY_VERIFIER, null) ?: return null

        val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
        val storedVerifier = Base64.decode(verifierEncoded, Base64.NO_WRAP)
        val candidate = KeyDerivation.derive(password, salt)

        return if (MessageDigest.isEqual(candidate, storedVerifier)) candidate else null
    }

    fun getFailedAttempts(): Int = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

    fun isLockedOut(): Boolean = getFailedAttempts() >= MAX_ATTEMPTS

    fun recordFailedAttempt(): Int {
        val next = getFailedAttempts() + 1
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, next)
            // ===== AÑADIDO BLOQUE 7 =====
            .putLong(KEY_LAST_FAILED_AT_MILLIS, System.currentTimeMillis())
            // ===== FIN AÑADIDO =====
            .apply()
        return next
    }

    fun resetFailedAttempts() {
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LAST_FAILED_AT_MILLIS) // ===== AÑADIDO BLOQUE 7 =====
            .apply()
    }

    // ===== INICIO CAMBIOS BLOQUE 7 =====
    /**
     * Segundos que quedan de espera obligatoria antes de poder reintentar,
     * calculados con retardo exponencial: 1, 2, 4, 8, 16, 32, hasta un
     * máximo de 60s. Es una barrera adicional, previa al bloqueo duro a
     * los MAX_ATTEMPTS intentos (que exige recuperación de acceso).
     */
    fun getBackoffRemainingSeconds(): Int {
        val attempts = getFailedAttempts()
        if (attempts <= 0) return 0

        val lastFailedAt = prefs.getLong(KEY_LAST_FAILED_AT_MILLIS, 0L)
        if (lastFailedAt == 0L) return 0

        val backoffSeconds = (1 shl (attempts - 1).coerceAtMost(6)).coerceAtMost(MAX_BACKOFF_SECONDS)
        val elapsedSeconds = (System.currentTimeMillis() - lastFailedAt) / 1000
        val remaining = backoffSeconds - elapsedSeconds

        return remaining.coerceAtLeast(0).toInt()
    }
    // ===== FIN CAMBIOS BLOQUE 7 =====
}