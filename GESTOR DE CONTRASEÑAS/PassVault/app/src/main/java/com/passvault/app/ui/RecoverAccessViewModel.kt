package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import com.passvault.app.security.PendingPassphraseHolder
import com.passvault.app.security.RecoveryCodeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RecoverAccessUiState(
    val code: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class RecoverAccessViewModel @Inject constructor(
    private val recoveryCodeManager: RecoveryCodeManager,
    private val pendingPassphraseHolder: PendingPassphraseHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecoverAccessUiState())
    val uiState: StateFlow<RecoverAccessUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(code = value, errorMessage = null)
    }

    fun recover(onRecovered: () -> Unit) {
        val recoveredPassphrase = recoveryCodeManager.tryRecover(_uiState.value.code)
        if (recoveredPassphrase != null) {
            pendingPassphraseHolder.set(recoveredPassphrase)
            onRecovered()
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Código de recuperación incorrecto."
            )
        }
    }
}