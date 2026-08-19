package com.passvault.app.security

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

/**
 * La passphrase de SQLCipher queda protegida en DOS capas:
 *   1) Cifrada con la clave derivada de la contraseña maestra.
 *   2) Ese resultado se cifra otra vez con la clave del Keystore.
 * Hacen falta las dos cosas a la vez.
 */
class PassphraseManager(
    private val context: Context,
    private val keyStoreManager: KeyStoreManager
) {
    companion object {
        private const val PREFS_NAME = "passvault_security_prefs"
        private const val KEY_OUTER_CIPHER = "db_passphrase_outer_cipher"
        private const val KEY_OUTER_IV = "db_passphrase_outer_iv"
        private const val KEY_INNER_IV = "db_passphrase_inner_iv"
        private const val PASSPHRASE_LENGTH_BYTES = 32
        private const val GCM_IV_LENGTH_BYTES = 12
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getOrCreateDatabasePassphrase(masterKey: ByteArray): ByteArray {
        readExisting(masterKey)?.let { return it }

        val newPassphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        store(newPassphrase, masterKey)
        return newPassphrase
    }

    /**
     * Vuelve a envolver la MISMA passphrase (la base de datos cifrada no
     * cambia) bajo una nueva clave maestra. Se usa tras una recuperación
     * exitosa por código de recuperación, cuando el usuario fija una
     * contraseña maestra nueva.
     */
    fun rewrapWithNewMasterKey(passphrase: ByteArray, newMasterKey: ByteArray) {
        store(passphrase, newMasterKey)
    }

    private fun readExisting(masterKey: ByteArray): ByteArray? {
        val outerCipherText = prefs.getString(KEY_OUTER_CIPHER, null) ?: return null
        val outerIv = prefs.getString(KEY_OUTER_IV, null) ?: return null
        val innerIv = prefs.getString(KEY_INNER_IV, null) ?: return null

        val innerCipherBytes = keyStoreManager.decrypt(
            EncryptedData(
                cipherBytes = Base64.decode(outerCipherText, Base64.NO_WRAP),
                iv = Base64.decode(outerIv, Base64.NO_WRAP)
            )
        )
        return AesGcmUtils.decrypt(innerCipherBytes, masterKey, Base64.decode(innerIv, Base64.NO_WRAP))
    }

    private fun store(passphrase: ByteArray, masterKey: ByteArray) {
        val innerIv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val innerCipherBytes = AesGcmUtils.encrypt(passphrase, masterKey, innerIv)

        val outer = keyStoreManager.encrypt(innerCipherBytes)

        prefs.edit()
            .putString(KEY_OUTER_CIPHER, Base64.encodeToString(outer.cipherBytes, Base64.NO_WRAP))
            .putString(KEY_OUTER_IV, Base64.encodeToString(outer.iv, Base64.NO_WRAP))
            .putString(KEY_INNER_IV, Base64.encodeToString(innerIv, Base64.NO_WRAP))
            .apply()
    }
}