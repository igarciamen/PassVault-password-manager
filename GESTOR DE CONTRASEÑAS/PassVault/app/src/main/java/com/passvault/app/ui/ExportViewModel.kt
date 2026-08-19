package com.passvault.app.ui

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExportUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val exportedOk: Boolean = false
) {
    val isValid: Boolean get() = password.length >= 8 && password == confirmPassword
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: PasswordRepository,
    private val exportManager: ExportManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    /**
     * Bytes cifrados pendientes de escribir, en Base64 dentro de
     * SavedStateHandle. Algunos dispositivos (visto en MIUI) matan el
     * proceso mientras el selector de archivos del sistema está
     * abierto para liberar memoria. Un `remember` de Compose se pierde
     * en ese caso; SavedStateHandle sobrevive porque Android lo
     * restaura al recrear la Activity, así el archivo se escribe bien
     * incluso si hubo una recreación de por medio.
     */
    val pendingExportBase64: StateFlow<String?> =
        savedStateHandle.getStateFlow("pending_export_b64", null)

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun prepareExport(onReady: () -> Unit) {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.value = state.copy(errorMessage = "Revisa que ambas contraseñas coincidan (mínimo 8 caracteres)")
            return
        }
        viewModelScope.launch {
            val entries = repository.getAllOnce()
            val bytes = exportManager.buildEncryptedExport(entries, state.password.toCharArray())
            savedStateHandle["pending_export_b64"] = Base64.encodeToString(bytes, Base64.NO_WRAP)
            onReady()
        }
    }

    fun onExportWritten() {
        savedStateHandle["pending_export_b64"] = null
        _uiState.value = _uiState.value.copy(exportedOk = true)
    }

    fun clearPendingExport() {
        savedStateHandle["pending_export_b64"] = null
    }
}