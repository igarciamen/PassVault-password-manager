package com.passvault.app.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.MasterPasswordManager
import com.passvault.app.security.UnlockSessionManager
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AutofillUnlockUiState(
    val password: String = "",
    val errorMessage: String? = null,
    val isUnlocked: Boolean = false,
    val entries: List<PasswordEntry> = emptyList()
)

/**
 * Bloque 8 (Parte 2) — versión reducida del login, solo para el flujo de
 * autocompletado.
 *
 * IMPORTANTE: igual que en PassVaultAutofillService, PasswordRepository
 * se inyecta como dagger.Lazy<...>, NO directamente. Esta pantalla se
 * crea (y este ViewModel con ella) justo cuando el vault SIGUE
 * bloqueado — una inyección directa fuerza a Dagger a construir Room
 * de inmediato, y DatabaseModule exige sesión desbloqueada para eso.
 * Solo llamamos a repositoryLazy.get() DESPUÉS de un unlock() exitoso.
 */
@HiltViewModel
class AutofillUnlockViewModel @Inject constructor(
    private val masterPasswordManager: MasterPasswordManager,
    private val unlockSessionManager: UnlockSessionManager,
    private val repositoryLazy: Lazy<PasswordRepository>
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AutofillUnlockUiState(isUnlocked = unlockSessionManager.isUnlocked())
    )
    val uiState: StateFlow<AutofillUnlockUiState> = _uiState.asStateFlow()

    init {
        // Solo seguro si isUnlocked() ya es true (caso raro: la pantalla
        // se abre pero el vault ya se desbloqueó por otra vía mientras tanto).
        if (unlockSessionManager.isUnlocked()) loadEntries()
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun unlock() {
        val derivedKey = masterPasswordManager.verifyMasterPassword(_uiState.value.password.toCharArray())
        if (derivedKey != null) {
            masterPasswordManager.resetFailedAttempts()
            unlockSessionManager.unlock(derivedKey)
            _uiState.value = _uiState.value.copy(isUnlocked = true, password = "")
            loadEntries()
        } else {
            _uiState.value = _uiState.value.copy(errorMessage = "Contraseña incorrecta")
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            // Seguro aquí: solo se llama tras confirmar isUnlocked() == true.
            _uiState.value = _uiState.value.copy(entries = repositoryLazy.get().getAllOnce())
        }
    }
}