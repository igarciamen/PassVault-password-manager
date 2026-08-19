package com.passvault.app.autofill

import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.passvault.app.ui.theme.PassVaultTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Bloque 8 (Parte 2) — Activity que Android lanza cuando el vault está
 * bloqueado y el usuario pulsa la sugerencia "Desbloquear PassVault" en
 * el teclado de autocompletado de otra app. Pide la contraseña maestra,
 * y si acierta, deja elegir qué entrada usar para rellenar el formulario.
 */
@AndroidEntryPoint
class AutofillUnlockActivity : FragmentActivity() {

    private var formFields: AutofillFormFields? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val structure = intent.getParcelableExtra<AssistStructure>(AutofillManager.EXTRA_ASSIST_STRUCTURE)
        android.util.Log.d("PassVaultAutofill", "AutofillUnlockActivity onCreate, structure=${structure != null}, formFieldsEmpty=${structure?.let { AutofillStructureParser.parse(it).isEmpty }}")
        formFields = structure?.let { AutofillStructureParser.parse(it) }


        if (formFields == null || formFields!!.isEmpty) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setContent {
            PassVaultTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AutofillUnlockScreen(
                        onEntrySelected = { username, password -> finishWithDataset(username, password) },
                        onCancelled = {
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun finishWithDataset(username: String, password: String) {
        val fields = formFields
        if (fields == null) {
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "PassVault")
        }
        val datasetBuilder = Dataset.Builder()
        fields.usernameIds.forEach { datasetBuilder.setValue(it, AutofillValue.forText(username), presentation) }
        fields.passwordIds.forEach { datasetBuilder.setValue(it, AutofillValue.forText(password), presentation) }

        val replyIntent = Intent().putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, datasetBuilder.build())
        setResult(RESULT_OK, replyIntent)
        finish()
    }
}