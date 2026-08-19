package com.passvault.app.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryCodeGeneratorTest {

    @Test
    fun elCodigoTieneElFormatoEsperado() {
        val code = RecoveryCodeGenerator.generate()
        val groups = code.split("-")

        assertEquals(6, groups.size)
        groups.forEach { group ->
            assertEquals(4, group.length)
            assertTrue(group.all { it in RecoveryCodeGenerator.ALPHABET })
        }
    }

    @Test
    fun dosCodigosGeneradosSonDistintos() {
        val first = RecoveryCodeGenerator.generate()
        val second = RecoveryCodeGenerator.generate()

        assertNotEquals(first, second)
    }

    @Test
    fun elAlfabetoNoContieneCaracteresAmbiguos() {
        val alphabet = RecoveryCodeGenerator.ALPHABET
        listOf('0', 'O', '1', 'I').forEach { ambiguous ->
            assertTrue(ambiguous !in alphabet)
        }
    }
}