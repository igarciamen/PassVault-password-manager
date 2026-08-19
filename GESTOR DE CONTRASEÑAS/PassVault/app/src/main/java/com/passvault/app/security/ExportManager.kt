package com.passvault.app.security

import android.util.Base64
import com.passvault.app.domain.PasswordEntry
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

class ExportManager {

    companion object {
        private const val SALT_LENGTH_BYTES = 16
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val FORMAT_VERSION = 2 // ===== subido: incluye entryType/pregunta secreta =====
    }

    fun buildEncryptedExport(entries: List<PasswordEntry>, exportPassword: CharArray): ByteArray {
        val plainBytes = entriesToJson(entries).toString().toByteArray(Charsets.UTF_8)

        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val key = KeyDerivation.derive(exportPassword, salt)
        val iv = ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        val cipherBytes = AesGcmUtils.encrypt(plainBytes, key, iv)

        val container = JSONObject()
            .put("format", FORMAT_VERSION)
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
            .put("data", Base64.encodeToString(cipherBytes, Base64.NO_WRAP))

        return container.toString().toByteArray(Charsets.UTF_8)
    }

    private fun entriesToJson(entries: List<PasswordEntry>): JSONArray {
        val array = JSONArray()
        entries.forEach { entry ->
            array.put(
                JSONObject()
                    .put("title", entry.title)
                    .put("username", entry.username)
                    .put("password", entry.password)
                    .put("url", entry.url ?: JSONObject.NULL)
                    .put("notes", entry.notes ?: JSONObject.NULL)
                    .put("category", entry.category)
                    .put("reminderQuestion", entry.reminderQuestion ?: JSONObject.NULL)
                    .put("isFavorite", entry.isFavorite)
                    .put("entryType", entry.entryType.name)
                    .put("secretQuestion", entry.secretQuestion ?: JSONObject.NULL)
                    .put("secretAnswerHash", entry.secretAnswerHash ?: JSONObject.NULL)
                    .put("secretAnswerSalt", entry.secretAnswerSalt ?: JSONObject.NULL)
            )
        }
        return array
    }
}