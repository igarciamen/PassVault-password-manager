package com.passvault.app.ui

import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.ImportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val password: String = "",
    val errorMessage: String? = null,
    val importedCount: Int? = null
)

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val repository: PasswordRepository,
    private val importManager: ImportManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    /**
     * Igual que en ExportViewModel: guardamos el archivo elegido en
     * Base64 dentro de SavedStateHandle en vez de en un remember de
     * Compose, para que sobreviva si el sistema mata el proceso entre
     * elegir el archivo y pulsar "Importar".
     */
    val pickedFileBase64: StateFlow<String?> =
        savedStateHandle.getStateFlow("picked_file_b64", null)

    fun onFilePicked(bytes: ByteArray) {
        savedStateHandle["picked_file_b64"] = Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun importFrom() {
        val base64 = pickedFileBase64.value ?: return
        val fileBytes = Base64.decode(base64, Base64.NO_WRAP)
        viewModelScope.launch {
            try {
                val entries = importManager.parseEncryptedExport(fileBytes, _uiState.value.password.toCharArray())
                entries.forEach { repository.save(it.copy(id = 0)) }
                _uiState.value = _uiState.value.copy(importedCount = entries.size, errorMessage = null)
            } catch (e: ImportManager.WrongExportPasswordException) {
                _uiState.value = _uiState.value.copy(errorMessage = "Contraseña de exportación incorrecta")
            } catch (e: ImportManager.InvalidFileException) {
                _uiState.value = _uiState.value.copy(errorMessage = "El archivo no es un backup válido")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "No se pudo importar el archivo")
            }
        }
    }
}