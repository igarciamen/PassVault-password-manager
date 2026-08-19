package com.passvault.app.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * Bloque 6 — borra el portapapeles del sistema tras un retardo, usando
 * WorkManager para que se ejecute igual aunque el usuario haya salido de
 * la app antes de que pase el tiempo (a diferencia de una simple
 * coroutine, que se cancelaría al abandonar la pantalla).
 */
class ClipboardClearWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val clipboard = applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("", ""))
        return Result.success()
    }
}