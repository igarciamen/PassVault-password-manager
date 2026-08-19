package com.passvault.app.ui

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import com.passvault.app.security.AutoLockPreferenceManager
import com.passvault.app.security.BiometricKeyManager
import com.passvault.app.security.BiometricPreferenceManager
import com.passvault.app.security.UnlockSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.crypto.Cipher
import javax.inject.Inject

data class SettingsUiState(
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
    val message: String? = null,
    val autoLockTimeoutSeconds: Int = AutoLockPreferenceManager.DEFAULT_TIMEOUT_SECONDS,
    // ===== AÑADIDO BLOQUE 8 (Parte 2) =====
    val autofillEnabled: Boolean = false
    // ===== FIN AÑADIDO =====
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricKeyManager: BiometricKeyManager,
    private val biometricPreferenceManager: BiometricPreferenceManager,
    private val unlockSessionManager: UnlockSessionManager,
    private val autoLockPreferenceManager: AutoLockPreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            biometricAvailable = isBiometricAvailable(),
            biometricEnabled = biometricPreferenceManager.isEnabled(),
            autoLockTimeoutSeconds = autoLockPreferenceManager.getTimeoutSeconds(),
            autofillEnabled = isAutofillEnabled() // ===== AÑADIDO =====
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun isBiometricAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    // ===== INICIO CAMBIOS BLOQUE 8 (Parte 2) =====
    private fun isAutofillEnabled(): Boolean {
        val afm = context.getSystemService(AutofillManager::class.java)
        return afm?.hasEnabledAutofillServices() == true
    }

    /** Se llama al volver de los Ajustes del sistema, para refrescar el estado. */
    fun refreshAutofillStatus() {
        _uiState.value = _uiState.value.copy(autofillEnabled = isAutofillEnabled())
    }
    // ===== FIN CAMBIOS BLOQUE 8 (Parte 2) =====

    fun prepareEncryptCipher(): Cipher? = try {
        biometricKeyManager.createEncryptCipher()
    } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(message = "No se pudo preparar la biometría")
        null
    }

    fun onBiometricEnrollSuccess(cipher: Cipher) {
        val sessionKey = unlockSessionManager.getSessionKey()
        if (sessionKey == null) {
            _uiState.value = _uiState.value.copy(message = "No hay sesión activa")
            return
        }
        val cipherBytes = cipher.doFinal(sessionKey)
        biometricPreferenceManager.saveWrappedMasterKey(cipherBytes, cipher.iv)
        _uiState.value = _uiState.value.copy(biometricEnabled = true, message = "Desbloqueo biométrico activado")
    }

    fun onBiometricError(message: String) {
        _uiState.value = _uiState.value.copy(message = message)
    }

    fun disableBiometric() {
        biometricPreferenceManager.disable()
        biometricKeyManager.deleteKey()
        _uiState.value = _uiState.value.copy(biometricEnabled = false, message = "Desbloqueo biométrico desactivado")
    }

    fun setAutoLockTimeout(seconds: Int) {
        autoLockPreferenceManager.setTimeoutSeconds(seconds)
        _uiState.value = _uiState.value.copy(autoLockTimeoutSeconds = seconds)
    }
}