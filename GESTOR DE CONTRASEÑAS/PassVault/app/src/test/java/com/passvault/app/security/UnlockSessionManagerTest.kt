package com.passvault.app.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UnlockSessionManagerTest {

    private lateinit var manager: UnlockSessionManager

    @Before
    fun setUp() {
        manager = UnlockSessionManager()
    }

    @Test
    fun alPrincipioNoEstaDesbloqueado() {
        assertFalse(manager.isUnlocked())
    }

    @Test
    fun unlockMarcaLaSesionComoDesbloqueada() {
        manager.unlock(byteArrayOf(1, 2, 3))
        assertTrue(manager.isUnlocked())
    }

    @Test
    fun lockBorraLaSesion() {
        manager.unlock(byteArrayOf(1, 2, 3))
        manager.lock()
        assertFalse(manager.isUnlocked())
    }

    @Test
    fun sinHaberIdoASegundoPlanoNuncaDebeAutobloquearse() {
        manager.unlock(byteArrayOf(1, 2, 3))
        assertFalse(manager.shouldAutoLock(30))
    }

    @Test
    fun conTimeoutInmediatoBloqueaEnCuantoVuelveDeSegundoPlano() {
        manager.unlock(byteArrayOf(1, 2, 3))
        manager.recordBackgrounded()
        assertTrue(manager.shouldAutoLock(0))
    }

    @Test
    fun conTimeoutCortoBloqueaSoloTrasSuperarlo() {
        manager.unlock(byteArrayOf(1, 2, 3))
        manager.recordBackgrounded()

        assertFalse(manager.shouldAutoLock(2)) // todavía no han pasado 2s

        Thread.sleep(2100) // espera real, por eso este test tarda ~2s

        assertTrue(manager.shouldAutoLock(2))
    }
}