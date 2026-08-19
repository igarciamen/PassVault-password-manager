package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import com.passvault.app.security.PendingPassphraseHolder
import com.passvault.app.security.RecoveryCodeGenerator
import com.passvault.app.security.RecoveryCodeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ShowRecoveryCodeViewModel @Inject constructor(
    private val recoveryCodeManager: RecoveryCodeManager,
    private val pendingPassphraseHolder: PendingPassphraseHolder
) : ViewModel() {

    val recoveryCode: String = RecoveryCodeGenerator.generate()

    private val _confirmed = MutableStateFlow(false)
    val confirmed: StateFlow<Boolean> = _confirmed.asStateFlow()

    init {
        pendingPassphraseHolder.get()?.let { passphrase ->
            recoveryCodeManager.setup(passphrase, recoveryCode)
        }
    }

    fun onConfirmedChange(value: Boolean) {
        _confirmed.value = value
    }

    fun finish(onDone: () -> Unit) {
        pendingPassphraseHolder.clear()
        onDone()
    }
}