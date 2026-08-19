package com.passvault.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
class PassphraseManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun limpiarEstadoAnterior() {
        context.getSharedPreferences("passvault_security_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun laPassphraseEsEstableEntreLlamadasConLaMismaClaveMaestra() {
        val manager = PassphraseManager(context, KeyStoreManager())
        val masterKey = ByteArray(32).also { SecureRandom().nextBytes(it) }

        val first = manager.getOrCreateDatabasePassphrase(masterKey)
        val second = manager.getOrCreateDatabasePassphrase(masterKey)

        assertArrayEquals(first, second)
        assertEquals(32, first.size)
    }

    @Test(expected = Exception::class)
    fun conUnaClaveMaestraDistintaFallaAlDescifrar() {
        val manager = PassphraseManager(context, KeyStoreManager())
        val correctKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val wrongKey = ByteArray(32).also { SecureRandom().nextBytes(it) }

        manager.getOrCreateDatabasePassphrase(correctKey)
        manager.getOrCreateDatabasePassphrase(wrongKey) // debe fallar
    }

    @Test
    fun rewrapConNuevaClaveMaestraPreservaLaMismaPassphrase() {
        val manager = PassphraseManager(context, KeyStoreManager())
        val originalKey = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val newKey = ByteArray(32).also { SecureRandom().nextBytes(it) }

        val passphrase = manager.getOrCreateDatabasePassphrase(originalKey)
        manager.rewrapWithNewMasterKey(passphrase, newKey)

        val recovered = manager.getOrCreateDatabasePassphrase(newKey)
        assertArrayEquals(passphrase, recovered)
    }
}