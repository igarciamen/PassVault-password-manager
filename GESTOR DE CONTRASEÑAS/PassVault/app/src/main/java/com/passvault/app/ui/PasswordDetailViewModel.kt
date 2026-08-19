package com.passvault.app.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.SecretAnswerHasher
import com.passvault.app.ui.navigation.PassVaultDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class SecretAnswerCheckResult { NONE, CORRECT, INCORRECT }

@HiltViewModel
class PasswordDetailViewModel @Inject constructor(
    private val repository: PasswordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: Long =
        checkNotNull(savedStateHandle.get<Long>(PassVaultDestinations.ARG_ENTRY_ID))

    private val _entry = MutableStateFlow<PasswordEntry?>(null)
    val entry: StateFlow<PasswordEntry?> = _entry.asStateFlow()

    private val _answerAttempt = MutableStateFlow("")
    val answerAttempt: StateFlow<String> = _answerAttempt.asStateFlow()

    private val _checkResult = MutableStateFlow(SecretAnswerCheckResult.NONE)
    val checkResult: StateFlow<SecretAnswerCheckResult> = _checkResult.asStateFlow()

    private val _isChecking = MutableStateFlow(false)
    val isChecking: StateFlow<Boolean> = _isChecking.asStateFlow()

    init {
        viewModelScope.launch {
            _entry.value = repository.getById(entryId)
        }
    }

    fun onAnswerAttemptChange(value: String) {
        _answerAttempt.value = value
        _checkResult.value = SecretAnswerCheckResult.NONE
    }

    fun checkAnswer() {
        val current = _entry.value ?: return
        val hash = current.secretAnswerHash
        val salt = current.secretAnswerSalt
        if (hash.isNullOrBlank() || salt.isNullOrBlank()) return
        if (_isChecking.value) return

        _isChecking.value = true
        viewModelScope.launch {
            val isCorrect = withContext(Dispatchers.Default) {
                SecretAnswerHasher.verify(_answerAttempt.value, hash, salt)
            }
            _checkResult.value = if (isCorrect) SecretAnswerCheckResult.CORRECT else SecretAnswerCheckResult.INCORRECT
            _isChecking.value = false
        }
    }

    // ===== AÑADIDO: limpia el intento al ocultar la pregunta de nuevo =====
    fun resetAnswerCheck() {
        _answerAttempt.value = ""
        _checkResult.value = SecretAnswerCheckResult.NONE
    }
    // ===== FIN AÑADIDO =====

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            _entry.value?.let { repository.delete(it) }
            onDeleted()
        }
    }
}