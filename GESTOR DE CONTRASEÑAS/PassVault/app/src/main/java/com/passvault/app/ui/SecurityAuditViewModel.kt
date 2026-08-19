package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.domain.PasswordStrengthChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityAuditUiState(
    val weakEntries: List<PasswordEntry> = emptyList(),
    val reusedGroups: List<List<PasswordEntry>> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SecurityAuditViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityAuditUiState())
    val uiState: StateFlow<SecurityAuditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val all = repository.getAllOnce()
            _uiState.value = SecurityAuditUiState(
                weakEntries = all.filter { PasswordStrengthChecker.isWeak(it.password) },
                reusedGroups = PasswordStrengthChecker.findReusedPasswords(all),
                isLoading = false
            )
        }
    }
}