package com.passvault.app.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.passvault.app.domain.PasswordEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportImportRoundTripTest {

    private val sampleEntries = listOf(
        PasswordEntry(title = "Gmail", username = "yo@gmail.com", password = "Clave123!", category = "Personal", isFavorite = true),
        PasswordEntry(title = "GitHub", username = "dev", password = "OtraClave456!", category = "Trabajo", reminderQuestion = "La de siempre")
    )

    @Test
    fun exportarEImportarConLaMismaContraseñaRecuperaLosDatos() {
        val exportManager = ExportManager()
        val importManager = ImportManager()
        val exportPassword = "MiPasswordDeExportacion".toCharArray()

        val fileBytes = exportManager.buildEncryptedExport(sampleEntries, exportPassword)
        val recovered = importManager.parseEncryptedExport(fileBytes, "MiPasswordDeExportacion".toCharArray())

        assertEquals(2, recovered.size)
        assertEquals("Gmail", recovered[0].title)
        assertTrue(recovered[0].isFavorite)
        assertEquals("La de siempre", recovered[1].reminderQuestion)
    }

    @Test(expected = ImportManager.WrongExportPasswordException::class)
    fun importarConLaContraseñaIncorrectaFalla() {
        val exportManager = ExportManager()
        val importManager = ImportManager()

        val fileBytes = exportManager.buildEncryptedExport(sampleEntries, "PasswordCorrecta".toCharArray())
        importManager.parseEncryptedExport(fileBytes, "PasswordIncorrecta".toCharArray())
    }

    @Test(expected = ImportManager.InvalidFileException::class)
    fun importarUnArchivoQueNoEsUnBackupFalla() {
        val importManager = ImportManager()
        importManager.parseEncryptedExport("esto no es json".toByteArray(), "cualquiera".toCharArray())
    }
}