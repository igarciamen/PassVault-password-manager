package com.passvault.app.domain

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutofillFieldClassifierTest {

    @Test
    fun unHintOficialDePasswordSeDetecta() {
        assertTrue(AutofillFieldClassifier.isPasswordField(listOf("password"), 0, null, null))
    }

    @Test
    fun unInputTypeDePasswordSeDetectaSinHint() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        assertTrue(AutofillFieldClassifier.isPasswordField(emptyList(), inputType, null, null))
    }

    @Test
    fun unIdEntryConPassSeDetectaPorTexto() {
        assertTrue(AutofillFieldClassifier.isPasswordField(emptyList(), 0, "login_pass_field", null))
    }

    @Test
    fun unCampoDeTextoNormalNoEsPassword() {
        assertFalse(AutofillFieldClassifier.isPasswordField(emptyList(), InputType.TYPE_CLASS_TEXT, "nombre", null))
    }

    @Test
    fun unHintOficialDeUsernameSeDetecta() {
        assertTrue(AutofillFieldClassifier.isUsernameField(listOf("username"), 0, null, null))
    }

    @Test
    fun unInputTypeDeEmailSeDetectaSinHint() {
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        assertTrue(AutofillFieldClassifier.isUsernameField(emptyList(), inputType, null, null))
    }

    @Test
    fun unHintTextConUsuarioSeDetectaPorTexto() {
        assertTrue(AutofillFieldClassifier.isUsernameField(emptyList(), 0, null, "Nombre de usuario"))
    }

    @Test
    fun unCampoDeTextoNormalNoEsUsername() {
        assertFalse(AutofillFieldClassifier.isUsernameField(emptyList(), InputType.TYPE_CLASS_TEXT, "direccion", null))
    }
    @Test
    fun unHintWebDeCurrentPasswordSeDetecta() {
        assertTrue(AutofillFieldClassifier.isPasswordField(listOf("current-password"), 0, null, null))
    }

    @Test
    fun unHintWebDeNewPasswordSeDetecta() {
        assertTrue(AutofillFieldClassifier.isPasswordField(listOf("new-password"), 0, null, null))
    }
}