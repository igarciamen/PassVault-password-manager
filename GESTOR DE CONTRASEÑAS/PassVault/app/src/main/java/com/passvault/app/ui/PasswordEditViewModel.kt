package com.passvault.app.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.EntryType
import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.SecretAnswerHasher
import com.passvault.app.ui.navigation.PassVaultDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PasswordEditUiState(
    val entryType: EntryType = EntryType.PASSWORD,
    val title: String = "",
    val username: String = "",
    val password: String = "",
    val reminderQuestion: String = "",
    val secretQuestion: String = "",
    val secretAnswer: String = "", // nunca se precarga con la respuesta real al editar
    val hasExistingSecretAnswer: Boolean = false
) {
    val isValid: Boolean
        get() = when (entryType) {
            EntryType.PASSWORD ->
                title.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            EntryType.SECRET_QUESTION -> {
                val baseValid = title.isNotBlank() && secretQuestion.isNotBlank()
                val answerValid = hasExistingSecretAnswer || secretAnswer.isNotBlank()
                baseValid && answerValid
            }
        }
}

@HiltViewModel
class PasswordEditViewModel @Inject constructor(
    private val repository: PasswordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long =
        savedStateHandle.get<Long>(PassVaultDestinations.ARG_ENTRY_ID) ?: -1L

    val isEditing: Boolean get() = entryId != -1L

    // conservamos el hash/salt existentes si el usuario no cambia la respuesta al editar
    private var existingSecretAnswerHash: String? = null
    private var existingSecretAnswerSalt: String? = null

    private val _uiState = MutableStateFlow(PasswordEditUiState())
    val uiState: StateFlow<PasswordEditUiState> = _uiState.asStateFlow()

    init {
        if (isEditing) {
            viewModelScope.launch {
                repository.getById(entryId)?.let { entry ->
                    existingSecretAnswerHash = entry.secretAnswerHash
                    existingSecretAnswerSalt = entry.secretAnswerSalt
                    _uiState.value = PasswordEditUiState(
                        entryType = entry.entryType,
                        title = entry.title,
                        username = entry.username,
                        password = entry.password,
                        reminderQuestion = entry.reminderQuestion.orEmpty(),
                        secretQuestion = entry.secretQuestion.orEmpty(),
                        secretAnswer = "",
                        hasExistingSecretAnswer = entry.secretAnswerHash != null
                    )
                }
            }
        }
    }

    fun onEntryTypeChange(value: EntryType) {
        _uiState.value = _uiState.value.copy(entryType = value)
    }

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun onReminderQuestionChange(value: String) {
        _uiState.value = _uiState.value.copy(reminderQuestion = value)
    }

    fun onSecretQuestionChange(value: String) {
        _uiState.value = _uiState.value.copy(secretQuestion = value)
    }

    fun onSecretAnswerChange(value: String) {
        _uiState.value = _uiState.value.copy(secretAnswer = value)
    }

    fun save(onSaved: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value

            val entry = when (state.entryType) {
                EntryType.PASSWORD -> PasswordEntry(
                    id = if (isEditing) entryId else 0,
                    title = state.title,
                    username = state.username,
                    password = state.password,
                    reminderQuestion = state.reminderQuestion.takeIf { it.isNotBlank() },
                    entryType = EntryType.PASSWORD
                )
                EntryType.SECRET_QUESTION -> {
                    val (hash, salt) = if (state.secretAnswer.isNotBlank()) {
                        val result = SecretAnswerHasher.hash(state.secretAnswer)
                        result.hash to result.salt
                    } else {
                        // no se cambió la respuesta al editar: conservamos la existente
                        (existingSecretAnswerHash ?: "") to (existingSecretAnswerSalt ?: "")
                    }
                    PasswordEntry(
                        id = if (isEditing) entryId else 0,
                        title = state.title,
                        username = state.username,
                        password = "",
                        entryType = EntryType.SECRET_QUESTION,
                        secretQuestion = state.secretQuestion,
                        secretAnswerHash = hash,
                        secretAnswerSalt = salt
                    )
                }
            }

            repository.save(entry)
            onSaved()
        }
    }
}