package com.passvault.app.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.security.BiometricKeyManager
import com.passvault.app.security.BiometricPreferenceManager
import com.passvault.app.security.MasterPasswordManager
import com.passvault.app.security.UnlockSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import javax.inject.Inject

data class LoginUiState(
    val password: String = "",
    val errorMessage: String? = null,
    val remainingAttempts: Int = MasterPasswordManager.MAX_ATTEMPTS,
    val isLockedOut: Boolean = false,
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
    val backoffRemainingSeconds: Int = 0,
    val isLoading: Boolean = false // ===== AÑADIDO: spinner al verificar =====
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val masterPasswordManager: MasterPasswordManager,
    private val unlockSessionManager: UnlockSessionManager,
    private val biometricKeyManager: BiometricKeyManager,
    private val biometricPreferenceManager: BiometricPreferenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            remainingAttempts = MasterPasswordManager.MAX_ATTEMPTS - masterPasswordManager.getFailedAttempts(),
            isLockedOut = masterPasswordManager.isLockedOut(),
            biometricAvailable = isBiometricAvailable(),
            biometricEnabled = biometricPreferenceManager.isEnabled()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        val remaining = masterPasswordManager.getBackoffRemainingSeconds()
        if (remaining > 0) startCountdown(remaining)
    }

    private fun startCountdown(initialSeconds: Int) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = initialSeconds
            while (remaining > 0) {
                _uiState.value = _uiState.value.copy(backoffRemainingSeconds = remaining)
                delay(1000)
                remaining -= 1
            }
            _uiState.value = _uiState.value.copy(backoffRemainingSeconds = 0)
        }
    }

    private fun isBiometricAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    // ===== MODIFICADO: verificación en segundo plano + spinner =====
    fun login(onSuccess: () -> Unit) {
        if (_uiState.value.isLockedOut) return
        if (_uiState.value.backoffRemainingSeconds > 0) return
        if (_uiState.value.isLoading) return

        val password = _uiState.value.password
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val derivedKey = withContext(Dispatchers.Default) {
                masterPasswordManager.verifyMasterPassword(password.toCharArray())
            }

            if (derivedKey != null) {
                masterPasswordManager.resetFailedAttempts()
                unlockSessionManager.unlock(derivedKey)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } else {
                val attempts = masterPasswordManager.recordFailedAttempt()
                val remaining = (MasterPasswordManager.MAX_ATTEMPTS - attempts).coerceAtLeast(0)
                val lockedOut = attempts >= MasterPasswordManager.MAX_ATTEMPTS

                val backoffSeconds = masterPasswordManager.getBackoffRemainingSeconds()
                if (!lockedOut && backoffSeconds > 0) startCountdown(backoffSeconds)

                _uiState.value = _uiState.value.copy(
                    password = "",
                    remainingAttempts = remaining,
                    isLockedOut = lockedOut,
                    isLoading = false,
                    errorMessage = if (lockedOut)
                        "Demasiados intentos fallidos. Usa la recuperación de acceso."
                    else
                        "Contraseña incorrecta. Te quedan $remaining intentos."
                )
            }
        }
    }
    // ===== FIN MODIFICADO =====

    fun prepareBiometricDecryptCipher(): Cipher? {
        val (_, iv) = biometricPreferenceManager.getWrappedMasterKey() ?: return null
        return try {
            biometricKeyManager.createDecryptCipher(iv)
        } catch (e: Exception) {
            null
        }
    }

    fun onBiometricUnlockSuccess(cipher: Cipher, onSuccess: () -> Unit) {
        val (cipherBytes, _) = biometricPreferenceManager.getWrappedMasterKey() ?: return
        try {
            val masterKey = cipher.doFinal(cipherBytes)
            masterPasswordManager.resetFailedAttempts()
            unlockSessionManager.unlock(masterKey)
            onSuccess()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "No se pudo desbloquear con biometría")
        }
    }

    fun onBiometricError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}