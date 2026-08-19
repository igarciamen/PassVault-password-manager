package com.passvault.app.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnlockSessionManager @Inject constructor() {

    @Volatile
    private var sessionKey: ByteArray? = null

    // ===== INICIO CAMBIOS BLOQUE 6 =====
    @Volatile
    private var backgroundedAtMillis: Long? = null

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlockedState: StateFlow<Boolean> = _isUnlocked.asStateFlow()
    // ===== FIN CAMBIOS BLOQUE 6 =====

    fun unlock(derivedKey: ByteArray) {
        sessionKey = derivedKey
        _isUnlocked.value = true // ===== AÑADIDO =====
    }

    fun getSessionKey(): ByteArray? = sessionKey

    fun isUnlocked(): Boolean = sessionKey != null

    fun lock() {
        sessionKey?.let { Arrays.fill(it, 0) }
        sessionKey = null
        backgroundedAtMillis = null // ===== AÑADIDO =====
        _isUnlocked.value = false // ===== AÑADIDO =====
    }

    // ===== INICIO CAMBIOS BLOQUE 6 =====
    /** Se llama cuando la app pasa a segundo plano estando desbloqueada. */
    fun recordBackgrounded() {
        backgroundedAtMillis = System.currentTimeMillis()
    }

    fun clearBackgroundTimestamp() {
        backgroundedAtMillis = null
    }

    /**
     * true si ha pasado más tiempo del permitido desde que la app se fue a
     * segundo plano. timeoutSeconds = 0 significa "bloquear siempre que
     * vuelva de segundo plano, sin importar cuánto tiempo pasó".
     */
    fun shouldAutoLock(timeoutSeconds: Int): Boolean {
        val ts = backgroundedAtMillis ?: return false
        if (timeoutSeconds <= 0) return true
        val elapsedSeconds = (System.currentTimeMillis() - ts) / 1000
        return elapsedSeconds >= timeoutSeconds
    }
    // ===== FIN CAMBIOS BLOQUE 6 =====
}