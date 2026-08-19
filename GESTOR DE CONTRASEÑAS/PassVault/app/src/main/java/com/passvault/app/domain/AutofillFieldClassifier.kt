package com.passvault.app.domain

import android.text.InputType

/**
 * Bloque 8 (Parte 2) — decide si un campo de un formulario (de OTRA app)
 * es de usuario/email o de contraseña. Usa primero los "autofill hints"
 * oficiales que Android/HTML definen (cuando la app o página web los
 * declaró correctamente), y si no existen, cae a heurísticas por tipo
 * de teclado y por el texto del identificador/hint del campo.
 *
 * IMPORTANTE: formularios web (renderizados por Chrome) usan los
 * valores del atributo HTML "autocomplete", que no son exactamente
 * los mismos que los AUTOFILL_HINT_* de Android nativo. En concreto:
 * "current-password" (login) y "new-password" (registro) son los
 * valores reales que Chrome expone para campos de contraseña — no
 * basta con comprobar solo "password".
 */
object AutofillFieldClassifier {

    private val PASSWORD_HINTS = setOf("password", "current-password", "new-password")
    private val USERNAME_HINTS = setOf("username", "emailaddress", "email")

    fun isPasswordField(
        hints: List<String>,
        inputType: Int,
        idEntry: String?,
        hintText: String?
    ): Boolean {
        if (hints.any { it.lowercase() in PASSWORD_HINTS }) return true

        val variation = inputType and InputType.TYPE_MASK_VARIATION
        val isPasswordVariation = variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isPasswordVariation) return true

        val text = ((idEntry ?: "") + " " + (hintText ?: "")).lowercase()
        return text.contains("password") || text.contains("contraseña") || text.contains("pass")
    }

    fun isUsernameField(
        hints: List<String>,
        inputType: Int,
        idEntry: String?,
        hintText: String?
    ): Boolean {
        if (hints.any { it.lowercase() in USERNAME_HINTS }) return true

        val variation = inputType and InputType.TYPE_MASK_VARIATION
        if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
            variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        ) return true

        val text = ((idEntry ?: "") + " " + (hintText ?: "")).lowercase()
        return text.contains("user") || text.contains("email") ||
                text.contains("correo") || text.contains("usuario") || text.contains("login")
    }
}