package com.passvault.app.domain

import kotlinx.coroutines.flow.Flow

interface PasswordRepository {
    fun getAll(): Flow<List<PasswordEntry>>
    fun search(query: String): Flow<List<PasswordEntry>>
    fun getCategories(): Flow<List<String>>
    suspend fun getAllOnce(): List<PasswordEntry>
    suspend fun getById(id: Long): PasswordEntry?
    suspend fun save(entry: PasswordEntry): Long
    suspend fun delete(entry: PasswordEntry)
}