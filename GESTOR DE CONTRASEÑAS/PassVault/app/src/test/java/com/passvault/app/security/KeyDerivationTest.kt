package com.passvault.app.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class KeyDerivationTest {

    @Test
    fun derivarConLaMismaEntradaYSaltDaSiempreElMismoResultado() {
        val input = "MiClaveDePrueba123".toCharArray()
        val salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        val first = KeyDerivation.derive(input, salt)
        val second = KeyDerivation.derive(input, salt)

        assertArrayEquals(first, second)
    }

    @Test
    fun derivarConSaltDistintoDaResultadosDistintos() {
        val input = "MiClaveDePrueba123".toCharArray()
        val saltA = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val saltB = byteArrayOf(8, 7, 6, 5, 4, 3, 2, 1)

        val resultA = KeyDerivation.derive(input, saltA)
        val resultB = KeyDerivation.derive(input, saltB)

        assertFalse(resultA.contentEquals(resultB))
    }

    @Test
    fun derivarConEntradaDistintaDaResultadosDistintos() {
        val salt = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        val resultA = KeyDerivation.derive("ClaveA".toCharArray(), salt)
        val resultB = KeyDerivation.derive("ClaveB".toCharArray(), salt)

        assertFalse(resultA.contentEquals(resultB))
    }

    @Test
    fun elResultadoTieneLaLongitudDeClaveEsperada() {
        val derived = KeyDerivation.derive("cualquiera".toCharArray(), byteArrayOf(1, 2, 3, 4))
        // 256 bits = 32 bytes, el tamaño de clave que usamos en todo el proyecto.
        assertEquals(32, derived.size)
    }
}