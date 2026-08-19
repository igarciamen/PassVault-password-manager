package com.passvault.app.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.security.KeyStoreException

@RunWith(AndroidJUnit4::class)
class BiometricKeyManagerTest {

    @Test
    fun laClaveNoExisteAntesDeUsarla() {
        val manager = BiometricKeyManager()
        manager.deleteKey() // por si quedó de un test anterior
        assertFalse(manager.isKeyConfigured())
    }

    @Test(expected = Exception::class)
    fun crearUnCipherSinAutenticacionBiometricaFalla() {
        val manager = BiometricKeyManager()
        manager.deleteKey()

        // Sin biometría reciente, el propio Android Keystore debe rechazar
        // la operación (UserNotAuthenticatedException o similar) — esto
        // ES el comportamiento de seguridad correcto, no un fallo.
        val cipher = manager.createEncryptCipher()
        cipher.doFinal("prueba".toByteArray())
    }

    @Test
    fun laClaveSeCreaTrasElPrimerIntento() {
        val manager = BiometricKeyManager()
        manager.deleteKey()

        try {
            manager.createEncryptCipher()
        } catch (e: Exception) {
            // esperado sin biometría real; lo relevante es que la clave
            // ya quedó generada en el Keystore aunque el cipher no se pudiera usar
        }

        assertTrue(manager.isKeyConfigured())
    }
}