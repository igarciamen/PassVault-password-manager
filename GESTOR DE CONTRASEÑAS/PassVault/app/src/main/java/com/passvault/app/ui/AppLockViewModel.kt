package com.passvault.app.ui

import androidx.lifecycle.ViewModel
import com.passvault.app.security.UnlockSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    unlockSessionManager: UnlockSessionManager
) : ViewModel() {
    val isUnlocked: StateFlow<Boolean> = unlockSessionManager.isUnlockedState
}