package com.passvault.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiometricPreferenceManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun limpiarEstadoAnterior() {
        context.getSharedPreferences("passvault_biometric_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun noEstaActivadaPorDefecto() {
        val manager = BiometricPreferenceManager(context)
        assertFalse(manager.isEnabled())
        assertNull(manager.getWrappedMasterKey())
    }

    @Test
    fun guardarYRecuperarLaClaveEnvueltaFunciona() {
        val manager = BiometricPreferenceManager(context)
        val cipherBytes = byteArrayOf(1, 2, 3, 4)
        val iv = byteArrayOf(5, 6, 7)

        manager.saveWrappedMasterKey(cipherBytes, iv)

        assertTrue(manager.isEnabled())
        val (storedCipher, storedIv) = manager.getWrappedMasterKey()!!
        assertArrayEquals(cipherBytes, storedCipher)
        assertArrayEquals(iv, storedIv)
    }

    @Test
    fun desactivarBorraTodoElEstado() {
        val manager = BiometricPreferenceManager(context)
        manager.saveWrappedMasterKey(byteArrayOf(1), byteArrayOf(2))

        manager.disable()

        assertFalse(manager.isEnabled())
        assertNull(manager.getWrappedMasterKey())
    }
}