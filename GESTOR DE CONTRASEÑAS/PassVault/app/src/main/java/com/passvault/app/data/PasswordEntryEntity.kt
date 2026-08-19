package com.passvault.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.passvault.app.domain.EntryType

@Entity(tableName = "password_entries")
data class PasswordEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val url: String? = null,
    val notes: String? = null,
    val category: String = "General",
    @ColumnInfo(name = "reminder_question")
    val reminderQuestion: String? = null,
    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "entry_type", defaultValue = "PASSWORD")
    val entryType: EntryType = EntryType.PASSWORD,
    @ColumnInfo(name = "secret_question")
    val secretQuestion: String? = null,
    @ColumnInfo(name = "secret_answer_hash")
    val secretAnswerHash: String? = null,
    @ColumnInfo(name = "secret_answer_salt")
    val secretAnswerSalt: String? = null
)