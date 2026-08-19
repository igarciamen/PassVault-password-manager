package com.passvault.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AutoLockPreferenceManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun limpiarEstadoAnterior() {
        context.getSharedPreferences("passvault_autolock_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun elValorPorDefectoEs30Segundos() {
        val manager = AutoLockPreferenceManager(context)
        assertEquals(30, manager.getTimeoutSeconds())
    }

    @Test
    fun guardarUnValorNuevoPersiste() {
        val manager = AutoLockPreferenceManager(context)
        manager.setTimeoutSeconds(300)
        assertEquals(300, manager.getTimeoutSeconds())
    }
}