package com.passvault.app.data

import com.passvault.app.domain.PasswordEntry
import com.passvault.app.domain.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PasswordRepositoryImpl @Inject constructor(
    private val dao: PasswordDao
) : PasswordRepository {

    override fun getAll(): Flow<List<PasswordEntry>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<PasswordEntry>> =
        dao.search(query).map { list -> list.map { it.toDomain() } }

    override fun getCategories(): Flow<List<String>> = dao.getCategories()

    override suspend fun getAllOnce(): List<PasswordEntry> =
        dao.getAllOnce().map { it.toDomain() }

    override suspend fun getById(id: Long): PasswordEntry? =
        dao.getById(id)?.toDomain()

    override suspend fun save(entry: PasswordEntry): Long {
        return if (entry.id == 0L) {
            dao.insert(entry.toEntity())
        } else {
            val existing = dao.getById(entry.id)
            dao.update(entry.toEntity(createdAt = existing?.createdAt ?: System.currentTimeMillis()))
            entry.id
        }
    }

    override suspend fun delete(entry: PasswordEntry) {
        dao.delete(entry.toEntity())
    }
}