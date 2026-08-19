package com.passvault.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Bloque 0 — no hay lógica de negocio todavía, así que este test unitario
 * solo confirma que el módulo `app` compila y que JUnit está bien
 * configurado. A partir del Bloque 1 aquí empezarán los tests reales
 * (Repository, DAO...).
 */
class Block0SetupTest {

    @Test
    fun `el proyecto esta correctamente configurado`() {
        val proyectoConfigurado = true
        assertTrue(proyectoConfigurado)
    }

    @Test
    fun `el applicationId coincide con el paquete base`() {
        val applicationId = "com.passvault.app"
        assertEquals("com.passvault.app", applicationId)
    }
}