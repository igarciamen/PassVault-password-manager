package com.passvault.app.data

import androidx.room.TypeConverter
import com.passvault.app.domain.EntryType

class Converters {
    @TypeConverter
    fun fromEntryType(value: EntryType): String = value.name

    @TypeConverter
    fun toEntryType(value: String): EntryType = EntryType.valueOf(value)
}