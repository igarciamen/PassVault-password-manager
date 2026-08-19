package com.passvault.app.security

import android.content.Context

/**
 * Bloque 6 — guarda cuánto tiempo puede estar la app en segundo plano
 * antes de bloquearse automáticamente al volver.
 */
class AutoLockPreferenceManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "passvault_autolock_prefs"
        private const val KEY_TIMEOUT_SECONDS = "autolock_timeout_seconds"
        const val DEFAULT_TIMEOUT_SECONDS = 30

        /** Opciones disponibles para la UI: Inmediato, 30s, 1min, 5min. */
        val AVAILABLE_OPTIONS = listOf(0, 30, 60, 300)
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTimeoutSeconds(): Int = prefs.getInt(KEY_TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS)

    fun setTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_TIMEOUT_SECONDS, seconds).apply()
    }

    fun label(seconds: Int): String = when (seconds) {
        0 -> "Inmediato"
        30 -> "30 segundos"
        60 -> "1 minuto"
        300 -> "5 minutos"
        else -> "$seconds s"
    }
}