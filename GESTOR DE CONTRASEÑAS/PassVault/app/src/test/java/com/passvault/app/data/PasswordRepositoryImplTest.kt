package com.passvault.app.data

import com.passvault.app.domain.PasswordEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Fake en memoria de PasswordDao: permite testear PasswordRepositoryImpl
 * sin Room ni emulador (test unitario puro, rápido).
 */
private class FakePasswordDao : PasswordDao {
    private val state = MutableStateFlow<List<PasswordEntryEntity>>(emptyList())
    private var nextId = 1L

    override fun getAll(): Flow<List<PasswordEntryEntity>> = state
    override fun search(query: String): Flow<List<PasswordEntryEntity>> = state
    override suspend fun getById(id: Long): PasswordEntryEntity? = state.value.find { it.id == id }

    override suspend fun insert(entry: PasswordEntryEntity): Long {
        val id = nextId++
        state.value = state.value + entry.copy(id = id)
        return id
    }

    override suspend fun update(entry: PasswordEntryEntity) {
        state.value = state.value.map { if (it.id == entry.id) entry else it }
    }

    override suspend fun delete(entry: PasswordEntryEntity) {
        state.value = state.value.filterNot { it.id == entry.id }
    }

    override fun getCategories(): Flow<List<String>> =
        MutableStateFlow(state.value.map { it.category }.distinct().sorted())

    override suspend fun getAllOnce(): List<PasswordEntryEntity> = state.value
}

class PasswordRepositoryImplTest {

    @Test
    fun `guardar una entrada nueva la anade a la lista`() = runBlocking {
        val repository = PasswordRepositoryImpl(FakePasswordDao())

        repository.save(PasswordEntry(title = "GitHub", username = "yo", password = "abc"))

        val all = repository.getAll().first()
        assertEquals(1, all.size)
        assertEquals("GitHub", all.first().title)
    }

    @Test
    fun `eliminar una entrada la quita de la lista`() = runBlocking {
        val repository = PasswordRepositoryImpl(FakePasswordDao())
        repository.save(PasswordEntry(title = "GitHub", username = "yo", password = "abc"))
        val saved = repository.getAll().first().first()

        repository.delete(saved)

        assertEquals(0, repository.getAll().first().size)
    }

    @Test
    fun `la pregunta de recordatorio se guarda y se recupera`() = runBlocking {
        val repository = PasswordRepositoryImpl(FakePasswordDao())

        repository.save(
            PasswordEntry(
                title = "GitHub",
                username = "yo",
                password = "abc",
                reminderQuestion = "¿Cuál es mi variante habitual?"
            )
        )

        val saved = repository.getAll().first().first()
        assertEquals("¿Cuál es mi variante habitual?", saved.reminderQuestion)
    }
}