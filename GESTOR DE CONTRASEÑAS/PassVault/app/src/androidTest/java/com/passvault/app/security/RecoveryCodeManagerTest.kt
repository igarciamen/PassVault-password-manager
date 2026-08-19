package com.passvault.app.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecoveryCodeManagerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun limpiarEstadoAnterior() {
        context.getSharedPreferences("passvault_recovery_code_prefs", Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    @Test
    fun conElCodigoCorrectoSeRecuperaLaPassphrase() {
        val manager = RecoveryCodeManager(context, KeyStoreManager())
        val originalPassphrase = ByteArray(32) { it.toByte() }
        val code = RecoveryCodeGenerator.generate()

        manager.setup(originalPassphrase, code)
        val recovered = manager.tryRecover(code)

        assertArrayEquals(originalPassphrase, recovered)
    }

    @Test
    fun conUnCodigoIncorrectoNoSeRecuperaNada() {
        val manager = RecoveryCodeManager(context, KeyStoreManager())
        val originalPassphrase = ByteArray(32) { it.toByte() }
        val code = RecoveryCodeGenerator.generate()

        manager.setup(originalPassphrase, code)

        assertNull(manager.tryRecover("ZZZZ-ZZZZ-ZZZZ-ZZZZ-ZZZZ-ZZZZ"))
    }

    @Test
    fun elCodigoNoDistingueMayusculasNiEspacios() {
        val manager = RecoveryCodeManager(context, KeyStoreManager())
        val originalPassphrase = ByteArray(32) { it.toByte() }
        val code = "ABCD-EFGH-JKLM-NPQR-STUV-WXYZ"

        manager.setup(originalPassphrase, code)
        val recovered = manager.tryRecover("  abcd-efgh-jklm-npqr-stuv-wxyz  ")

        assertArrayEquals(originalPassphrase, recovered)
    }

    @Test
    fun isConfiguredReflejaElEstadoReal() {
        val manager = RecoveryCodeManager(context, KeyStoreManager())
        assertTrue(!manager.isConfigured())

        manager.setup(ByteArray(32), RecoveryCodeGenerator.generate())
        assertTrue(manager.isConfigured())
    }
}