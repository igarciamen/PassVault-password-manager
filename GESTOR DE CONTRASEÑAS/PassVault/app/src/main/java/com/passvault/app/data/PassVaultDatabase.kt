package com.passvault.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * version = 4: se añadió entryType/secretQuestion/secretAnswerHash/secretAnswerSalt
 * para el modo "pregunta secreta". Seguimos en desarrollo temprano;
 * DatabaseModule usa fallbackToDestructiveMigration.
 */
@Database(entities = [PasswordEntryEntity::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PassVaultDatabase : RoomDatabase() {
    abstract fun passwordDao(): PasswordDao
}