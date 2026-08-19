package com.passvault.app.security

import android.util.Base64
import com.passvault.app.domain.EntryType
import com.passvault.app.domain.PasswordEntry
import org.json.JSONArray
import org.json.JSONObject

class ImportManager {

    class WrongExportPasswordException : Exception("Contraseña de exportación incorrecta")
    class InvalidFileException : Exception("El archivo no es un backup válido de PassVault")

    fun parseEncryptedExport(fileBytes: ByteArray, exportPassword: CharArray): List<PasswordEntry> {
        val container = try {
            JSONObject(String(fileBytes, Charsets.UTF_8))
        } catch (e: Exception) {
            throw InvalidFileException()
        }

        val salt = Base64.decode(container.getString("salt"), Base64.NO_WRAP)
        val iv = Base64.decode(container.getString("iv"), Base64.NO_WRAP)
        val cipherBytes = Base64.decode(container.getString("data"), Base64.NO_WRAP)

        val key = KeyDerivation.derive(exportPassword, salt)
        val plainBytes = try {
            AesGcmUtils.decrypt(cipherBytes, key, iv)
        } catch (e: Exception) {
            throw WrongExportPasswordException()
        }

        val array = JSONArray(String(plainBytes, Charsets.UTF_8))
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            PasswordEntry(
                title = obj.getString("title"),
                username = obj.getString("username"),
                password = obj.getString("password"),
                url = obj.optNullableString("url"),
                notes = obj.optNullableString("notes"),
                category = obj.optString("category", "General"),
                reminderQuestion = obj.optNullableString("reminderQuestion"),
                isFavorite = obj.optBoolean("isFavorite", false),
                // compatible con backups antiguos (formato 1) sin este campo
                entryType = obj.optNullableString("entryType")
                    ?.let { runCatching { EntryType.valueOf(it) }.getOrDefault(EntryType.PASSWORD) }
                    ?: EntryType.PASSWORD,
                secretQuestion = obj.optNullableString("secretQuestion"),
                secretAnswerHash = obj.optNullableString("secretAnswerHash"),
                secretAnswerSalt = obj.optNullableString("secretAnswerSalt")
            )
        }
    }

    private fun JSONObject.optNullableString(key: String): String? =
        if (has(key) && !isNull(key)) getString(key) else null
}