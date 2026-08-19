package com.passvault.app.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PasswordDaoTest {

    private lateinit var db: PassVaultDatabase
    private lateinit var dao: PasswordDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PassVaultDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.passwordDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertarYLeerUnaEntrada() = runBlocking {
        val id = dao.insert(
            PasswordEntryEntity(
                title = "Gmail",
                username = "yo@gmail.com",
                password = "1234"
            )
        )
        val stored = dao.getById(id)

        Assert.assertEquals("Gmail", stored?.title)
        Assert.assertEquals("yo@gmail.com", stored?.username)
    }

    @Test
    fun laListaSeOrdenaPorTitulo() = runBlocking {
        dao.insert(PasswordEntryEntity(title = "Zoom", username = "a", password = "1"))
        dao.insert(PasswordEntryEntity(title = "Amazon", username = "b", password = "2"))

        val all = dao.getAll().first()

        Assert.assertEquals("Amazon", all.first().title)
        Assert.assertEquals("Zoom", all.last().title)
    }

    @Test
    fun eliminarUnaEntrada() = runBlocking {
        val id = dao.insert(PasswordEntryEntity(title = "Netflix", username = "a", password = "1"))
        val stored = dao.getById(id)!!

        dao.delete(stored)

        Assert.assertNull(dao.getById(id))
    }

    @Test
    fun laPreguntaDeRecordatorioEsOpcionalYPersiste() = runBlocking {
        val id = dao.insert(
            PasswordEntryEntity(
                title = "Banco",
                username = "yo",
                password = "1234",
                reminderQuestion = "La de siempre + año"
            )
        )
        val stored = dao.getById(id)

        assertEquals("La de siempre + año", stored?.reminderQuestion)
    }

    @Test
    fun losFavoritosApareceenPrimeroEnLaLista() = runBlocking {
        dao.insert(PasswordEntryEntity(title = "Amazon", username = "a", password = "1", isFavorite = false))
        dao.insert(PasswordEntryEntity(title = "Zoom", username = "b", password = "2", isFavorite = true))

        val all = dao.getAll().first()

        assertEquals("Zoom", all.first().title) // favorito, aunque alfabéticamente va después
    }

    @Test
    fun getCategoriesDevuelveCategoriasUnicas() = runBlocking {
        dao.insert(PasswordEntryEntity(title = "A", username = "a", password = "1", category = "Trabajo"))
        dao.insert(PasswordEntryEntity(title = "B", username = "b", password = "2", category = "Trabajo"))
        dao.insert(PasswordEntryEntity(title = "C", username = "c", password = "3", category = "Personal"))

        val categories = dao.getCategories().first()

        assertEquals(listOf("Personal", "Trabajo"), categories)
    }
}