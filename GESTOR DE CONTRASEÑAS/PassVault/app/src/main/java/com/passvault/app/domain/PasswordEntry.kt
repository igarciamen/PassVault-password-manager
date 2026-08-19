package com.passvault.app.domain

data class PasswordEntry(
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val url: String? = null,
    val notes: String? = null,
    val category: String = "General",
    val reminderQuestion: String? = null,
    val isFavorite: Boolean = false,
    val entryType: EntryType = EntryType.PASSWORD,
    val secretQuestion: String? = null,
    val secretAnswerHash: String? = null,
    val secretAnswerSalt: String? = null
)