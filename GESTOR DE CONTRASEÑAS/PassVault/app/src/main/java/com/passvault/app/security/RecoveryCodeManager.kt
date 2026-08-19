package com.passvault.app.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import java.util.Locale

/**
 * Envuelve la passphrase de la base de datos con una clave derivada del
 * código de recuperación, en dos capas (mismo patrón que PassphraseManager):
 * derivación PBKDF2 + Keystore.
 */
class RecoveryCodeManager(
    private val context: Context,
    private val keyStoreManager: KeyStoreManager
) {
    companion object {
        private const val PREFS_NAME = "passvault_recovery_code_prefs"
        private const val KEY_SALT = "recovery_salt"
        private const val KEY_INNER_IV = "recovery_inner_iv"
        private const val KEY_OUTER_CIPHER = "recovery_outer_cipher"
        private const val KEY_OUTER_IV = "recovery_outer_iv"
        private const val SALT_LENGTH_BYTES = 16
        private const val GCM_IV_LENGTH_BYTES = 12
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isConfigured(): Boolean = prefs.contains(KEY_SALT)

    fun setup(passphrase: ByteArray, recoveryCode: String) {
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val derivedKey = KeyDerivation.derive(normalize(recoveryCode).toCharArray(), salt)

        val innerIv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val innerCipher = AesGcmUtils.encrypt(passphrase, derivedKey, innerIv)
        val outer = keyStoreManager.encrypt(innerCipher)

        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_INNER_IV, Base64.encodeToString(innerIv, Base64.NO_WRAP))
            .putString(KEY_OUTER_CIPHER, Base64.encodeToString(outer.cipherBytes, Base64.NO_WRAP))
            .putString(KEY_OUTER_IV, Base64.encodeToString(outer.iv, Base64.NO_WRAP))
            .apply()
    }

    /** Devuelve la passphrase recuperada si el código es correcto, o null. */
    fun tryRecover(enteredCode: String): ByteArray? {
        val saltEncoded = prefs.getString(KEY_SALT, null) ?: return null
        val innerIvEncoded = prefs.getString(KEY_INNER_IV, null) ?: return null
        val outerCipherEncoded = prefs.getString(KEY_OUTER_CIPHER, null) ?: return null
        val outerIvEncoded = prefs.getString(KEY_OUTER_IV, null) ?: return null

        val salt = Base64.decode(saltEncoded, Base64.NO_WRAP)
        val derivedKey = KeyDerivation.derive(normalize(enteredCode).toCharArray(), salt)

        return try {
            val innerCipher = keyStoreManager.decrypt(
                EncryptedData(
                    cipherBytes = Base64.decode(outerCipherEncoded, Base64.NO_WRAP),
                    iv = Base64.decode(outerIvEncoded, Base64.NO_WRAP)
                )
            )
            AesGcmUtils.decrypt(innerCipher, derivedKey, Base64.decode(innerIvEncoded, Base64.NO_WRAP))
        } catch (e: Exception) {
            null
        }
    }

    private fun normalize(code: String): String =
        code.trim().uppercase(Locale.ROOT).replace(" ", "")
}