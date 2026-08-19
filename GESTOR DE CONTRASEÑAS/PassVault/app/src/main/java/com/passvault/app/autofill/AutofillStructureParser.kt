package com.passvault.app.autofill

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId
import com.passvault.app.domain.AutofillFieldClassifier

/** Campos de usuario y contraseña detectados en un formulario ajeno. */
data class AutofillFormFields(
    val usernameIds: List<AutofillId>,
    val passwordIds: List<AutofillId>
) {
    val allIds: List<AutofillId> get() = usernameIds + passwordIds
    val isEmpty: Boolean get() = usernameIds.isEmpty() && passwordIds.isEmpty()
}

/**
 * Recorre el árbol de vistas de OTRA app (AssistStructure) que Android
 * nos entrega, y localiza qué campos son de usuario/email y cuáles de
 * contraseña, usando AutofillFieldClassifier para la decisión.
 */
object AutofillStructureParser {

    fun parse(structure: AssistStructure): AutofillFormFields {
        val usernameIds = mutableListOf<AutofillId>()
        val passwordIds = mutableListOf<AutofillId>()

        for (i in 0 until structure.windowNodeCount) {
            traverse(structure.getWindowNodeAt(i).rootViewNode, usernameIds, passwordIds)
        }

        return AutofillFormFields(usernameIds, passwordIds)
    }

    private fun traverse(
        node: AssistStructure.ViewNode,
        usernameIds: MutableList<AutofillId>,
        passwordIds: MutableList<AutofillId>
    ) {
        val autofillId = node.autofillId
        if (autofillId != null) {
            val hints = node.autofillHints?.toList() ?: emptyList()

            // AÑADIDO: log de diagnóstico de cada campo con autofillId
            android.util.Log.d(
                "PassVaultAutofill",
                "campo: idEntry=${node.idEntry} hint=${node.hint} inputType=${node.inputType} hints=$hints"
            )

            when {
                AutofillFieldClassifier.isPasswordField(hints, node.inputType, node.idEntry, node.hint) ->
                    passwordIds.add(autofillId)
                AutofillFieldClassifier.isUsernameField(hints, node.inputType, node.idEntry, node.hint) ->
                    usernameIds.add(autofillId)
            }
        }
        for (i in 0 until node.childCount) {
            traverse(node.getChildAt(i), usernameIds, passwordIds)
        }
    }
}