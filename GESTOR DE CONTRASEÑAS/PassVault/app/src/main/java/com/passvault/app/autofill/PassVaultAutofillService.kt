package com.passvault.app.autofill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Intent
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.util.Log
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.passvault.app.domain.PasswordRepository
import com.passvault.app.security.UnlockSessionManager
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class PassVaultAutofillService : AutofillService() {

    @Inject lateinit var unlockSessionManager: UnlockSessionManager
    @Inject lateinit var repositoryLazy: Lazy<PasswordRepository>

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        Log.d("PassVaultAutofill", "onFillRequest recibido")

        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            Log.d("PassVaultAutofill", "structure es null")
            callback.onSuccess(null)
            return
        }

        val fields = AutofillStructureParser.parse(structure)
        Log.d("PassVaultAutofill", "usernameIds=${fields.usernameIds.size} passwordIds=${fields.passwordIds.size}")

        if (fields.isEmpty) {
            Log.d("PassVaultAutofill", "no se detectaron campos relevantes")
            callback.onSuccess(null)
            return
        }

        if (!unlockSessionManager.isUnlocked()) {
            Log.d("PassVaultAutofill", "vault bloqueado, pidiendo auth")
            callback.onSuccess(buildAuthenticationResponse(fields, structure))
            return
        }

        Log.d("PassVaultAutofill", "vault desbloqueado, construyendo datasets")
        callback.onSuccess(buildDatasetsResponse(fields))
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }

    private fun buildAuthenticationResponse(fields: AutofillFormFields, structure: AssistStructure): FillResponse {
        val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "Desbloquear PassVault")
        }
        val authIntent = Intent(this, AutofillUnlockActivity::class.java).apply {
            putExtra(AutofillManager.EXTRA_ASSIST_STRUCTURE, structure)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(this, System.currentTimeMillis().toInt(), authIntent, flags)

        return FillResponse.Builder()
            .setAuthentication(fields.allIds.toTypedArray(), pendingIntent.intentSender, presentation)
            .build()
    }

    private fun buildDatasetsResponse(fields: AutofillFormFields): FillResponse {
        val entries = runBlocking { repositoryLazy.get().getAllOnce() }
        Log.d("PassVaultAutofill", "entries encontradas: ${entries.size}")

        val builder = FillResponse.Builder()
        var datasetsAdded = 0 // AÑADIDO

        entries.take(10).forEach { entry ->
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, "${entry.title} (${entry.username})")
            }
            val datasetBuilder = Dataset.Builder()
            fields.usernameIds.forEach {
                datasetBuilder.setValue(it, AutofillValue.forText(entry.username), presentation)
            }
            fields.passwordIds.forEach {
                datasetBuilder.setValue(it, AutofillValue.forText(entry.password), presentation)
            }
            builder.addDataset(datasetBuilder.build())
            datasetsAdded++ // AÑADIDO
        }

        Log.d("PassVaultAutofill", "datasets añadidos: $datasetsAdded") // AÑADIDO (reemplaza la línea anterior)
        return builder.build()
    }
}