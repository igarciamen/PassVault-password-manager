package com.passvault.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordStrengthCheckerTest {

    @Test
    fun unaContrasenaCortaEsDebil() {
        assertTrue(PasswordStrengthChecker.isWeak("abc123"))
    }

    @Test
    fun unaContrasenaComunEsDebil() {
        assertTrue(PasswordStrengthChecker.isWeak("password"))
    }

    @Test
    fun unaContrasenaSinNumerosEsDebil() {
        assertTrue(PasswordStrengthChecker.isWeak("SoloLetrasAqui"))
    }

    @Test
    fun unaContrasenaConCaracteresSecuencialesEsDebil() {
        assertTrue(PasswordStrengthChecker.isWeak("abcd1234"))
    }

    @Test
    fun unaContrasenaFuerteNoEsDebil() {
        assertFalse(PasswordStrengthChecker.isWeak("Tr8!kQ9zXm2p"))
    }

    @Test
    fun detectaContrasenasReutilizadas() {
        val entries = listOf(
            PasswordEntry(id = 1, title = "Gmail", username = "a", password = "MiClave123"),
            PasswordEntry(id = 2, title = "Amazon", username = "b", password = "MiClave123"),
            PasswordEntry(id = 3, title = "Netflix", username = "c", password = "OtraClave456")
        )

        val reused = PasswordStrengthChecker.findReusedPasswords(entries)

        assertEquals(1, reused.size)
        assertEquals(2, reused.first().size)
    }
}