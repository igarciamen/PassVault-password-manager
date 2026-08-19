package com.passvault.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MasterPasswordManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun limpiarEstadoAnterior() {
        context.getSharedPreferences("passvault_master_password_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun noHayContrasenaMaestraAntesDeCrearla() {
        val manager = MasterPasswordManager(context)
        assertFalse(manager.isMasterPasswordSet())
    }

    @Test
    fun crearYVerificarLaContrasenaCorrectaFunciona() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("MiClaveSegura123".toCharArray())

        assertTrue(manager.isMasterPasswordSet())
        assertNotNull(manager.verifyMasterPassword("MiClaveSegura123".toCharArray()))
    }

    @Test
    fun verificarUnaContrasenaIncorrectaDevuelveNull() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("MiClaveSegura123".toCharArray())

        assertNull(manager.verifyMasterPassword("otra-clave-cualquiera".toCharArray()))
    }

    @Test
    fun losIntentosFallidosSeIncrementanYResetean() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        assertEquals(0, manager.getFailedAttempts())
        repeat(3) { manager.recordFailedAttempt() }
        assertEquals(3, manager.getFailedAttempts())
        assertFalse(manager.isLockedOut())

        manager.resetFailedAttempts()
        assertEquals(0, manager.getFailedAttempts())
    }

    @Test
    fun seBloqueaAlQuintoIntentoFallido() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        repeat(4) { manager.recordFailedAttempt() }
        assertFalse(manager.isLockedOut())

        manager.recordFailedAttempt() // 5º intento
        assertTrue(manager.isLockedOut())
    }

    @Test
    fun cambiarLaContrasenaMaestraPermiteVerificarConLaNueva() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        manager.changeMasterPassword("ClaveNueva456".toCharArray())

        assertNull(manager.verifyMasterPassword("ClaveOriginal123".toCharArray()))
        assertNotNull(manager.verifyMasterPassword("ClaveNueva456".toCharArray()))
        assertEquals(0, manager.getFailedAttempts())
    }

    @Test
    fun sinIntentosFallidosNoHayBackoff() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        assertEquals(0, manager.getBackoffRemainingSeconds())
    }

    @Test
    fun trasUnFalloElBackoffEsMayorQueCero() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        manager.recordFailedAttempt()

        assertTrue(manager.getBackoffRemainingSeconds() > 0)
    }

    @Test
    fun elBackoffCreceConMasIntentosFallidos() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        manager.recordFailedAttempt()
        val backoffTrasPrimerFallo = manager.getBackoffRemainingSeconds()

        manager.recordFailedAttempt()
        val backoffTrasSegundoFallo = manager.getBackoffRemainingSeconds()

        assertTrue(backoffTrasSegundoFallo > backoffTrasPrimerFallo)
    }

    @Test
    fun resetFailedAttemptsEliminaElBackoff() {
        val manager = MasterPasswordManager(context)
        manager.createMasterPassword("ClaveOriginal123".toCharArray())

        manager.recordFailedAttempt()
        manager.resetFailedAttempts()

        assertEquals(0, manager.getBackoffRemainingSeconds())
    }
}