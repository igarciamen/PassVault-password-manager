package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import com.passvault.app.security.MasterPasswordManager
import com.passvault.app.security.PassphraseManager
import com.passvault.app.security.PendingPassphraseHolder
import com.passvault.app.security.UnlockSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CreateMasterPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null
) {
    val isValid: Boolean
        get() = password.length >= 8 && password == confirmPassword
}

@HiltViewModel
class CreateMasterPasswordViewModel @Inject constructor(
    private val masterPasswordManager: MasterPasswordManager,
    private val unlockSessionManager: UnlockSessionManager,
    private val passphraseManager: PassphraseManager,
    private val pendingPassphraseHolder: PendingPassphraseHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMasterPasswordUiState())
    val uiState: StateFlow<CreateMasterPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun createMasterPassword(onCreated: () -> Unit) {
        val state = _uiState.value
        if (state.password.length < 8) {
            _uiState.value = state.copy(errorMessage = "Debe tener al menos 8 caracteres")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        val derivedKey = masterPasswordManager.createMasterPassword(state.password.toCharArray())
        unlockSessionManager.unlock(derivedKey)

        val passphrase = passphraseManager.getOrCreateDatabasePassphrase(derivedKey)
        pendingPassphraseHolder.set(passphrase)

        onCreated()
    }
}