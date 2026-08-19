package com.passvault.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM password_entries ORDER BY is_favorite DESC, title ASC")
    fun getAll(): Flow<List<PasswordEntryEntity>>

    @Query("SELECT * FROM password_entries WHERE id = :id")
    suspend fun getById(id: Long): PasswordEntryEntity?

    @Query("""
        SELECT * FROM password_entries
        WHERE title LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'
        ORDER BY is_favorite DESC, title ASC
    """)
    fun search(query: String): Flow<List<PasswordEntryEntity>>

    @Query("SELECT DISTINCT category FROM password_entries ORDER BY category ASC")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM password_entries")
    suspend fun getAllOnce(): List<PasswordEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PasswordEntryEntity): Long

    @Update
    suspend fun update(entry: PasswordEntryEntity)

    @Delete
    suspend fun delete(entry: PasswordEntryEntity)
}