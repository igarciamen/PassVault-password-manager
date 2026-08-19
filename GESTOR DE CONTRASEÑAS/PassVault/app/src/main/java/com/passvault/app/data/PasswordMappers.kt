package com.passvault.app.data

import com.passvault.app.domain.PasswordEntry

fun PasswordEntryEntity.toDomain() = PasswordEntry(
    id = id,
    title = title,
    username = username,
    password = password,
    url = url,
    notes = notes,
    category = category,
    reminderQuestion = reminderQuestion,
    isFavorite = isFavorite,
    entryType = entryType,
    secretQuestion = secretQuestion,
    secretAnswerHash = secretAnswerHash,
    secretAnswerSalt = secretAnswerSalt
)

fun PasswordEntry.toEntity(
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
) = PasswordEntryEntity(
    id = id,
    title = title,
    username = username,
    password = password,
    url = url,
    notes = notes,
    category = category,
    reminderQuestion = reminderQuestion,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
    entryType = entryType,
    secretQuestion = secretQuestion,
    secretAnswerHash = secretAnswerHash,
    secretAnswerSalt = secretAnswerSalt
)