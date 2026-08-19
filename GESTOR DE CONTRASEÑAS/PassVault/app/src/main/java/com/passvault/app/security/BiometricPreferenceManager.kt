package com.passvault.app.security

import android.content.Context
import android.util.Base64

/**
 * Bloque 5 — guarda si la biometría está activada y la clave de sesión
 * (derivada de la contraseña maestra) ya envuelta con BiometricKeyManager.
 * Sin la clave del Keystore ligada a biometría, este blob es ilegible.
 */
class BiometricPreferenceManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "passvault_biometric_prefs"
        private const val KEY_ENABLED = "biometric_enabled"
        private const val KEY_WRAPPED_CIPHER = "biometric_wrapped_master_key"
        private const val KEY_IV = "biometric_iv"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false) && prefs.contains(KEY_WRAPPED_CIPHER)

    fun saveWrappedMasterKey(cipherBytes: ByteArray, iv: ByteArray) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, true)
            .putString(KEY_WRAPPED_CIPHER, Base64.encodeToString(cipherBytes, Base64.NO_WRAP))
            .putString(KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
    }

    /** Devuelve (cipherBytes, iv) o null si no hay biometría configurada. */
    fun getWrappedMasterKey(): Pair<ByteArray, ByteArray>? {
        val cipherEncoded = prefs.getString(KEY_WRAPPED_CIPHER, null) ?: return null
        val ivEncoded = prefs.getString(KEY_IV, null) ?: return null
        return Base64.decode(cipherEncoded, Base64.NO_WRAP) to Base64.decode(ivEncoded, Base64.NO_WRAP)
    }

    fun disable() {
        prefs.edit().clear().apply()
    }
}