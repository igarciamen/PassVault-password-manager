package com.passvault.app.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bloque 6 — observa el ciclo de vida de TODA la app (no de una Activity
 * concreta), usando ProcessLifecycleOwner. onStop() se dispara solo cuando
 * ninguna pantalla de la app es visible (pasó a segundo plano de verdad,
 * no solo al navegar entre pantallas internas).
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
    private val unlockSessionManager: UnlockSessionManager,
    private val autoLockPreferenceManager: AutoLockPreferenceManager
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        if (unlockSessionManager.isUnlocked()) {
            unlockSessionManager.recordBackgrounded()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        val timeoutSeconds = autoLockPreferenceManager.getTimeoutSeconds()
        if (unlockSessionManager.shouldAutoLock(timeoutSeconds)) {
            unlockSessionManager.lock()
        }
        unlockSessionManager.clearBackgroundTimestamp()
    }
}