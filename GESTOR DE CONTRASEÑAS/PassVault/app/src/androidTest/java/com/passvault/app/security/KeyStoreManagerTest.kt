package com.passvault.app.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyStoreManagerTest {

    @Test
    fun cifrarYDescifrarDevuelveElTextoOriginal() {
        val manager = KeyStoreManager()
        val original = "Bloque 3: cifrado real".toByteArray()

        val encrypted = manager.encrypt(original)
        val decrypted = manager.decrypt(encrypted)

        assertArrayEquals(original, decrypted)
    }

    @Test
    fun elTextoCifradoNoContieneElTextoPlano() {
        val manager = KeyStoreManager()
        val original = "informacion-secreta-12345".toByteArray()

        val encrypted = manager.encrypt(original)

        assertFalse(String(encrypted.cipherBytes).contains("informacion-secreta"))
    }
}