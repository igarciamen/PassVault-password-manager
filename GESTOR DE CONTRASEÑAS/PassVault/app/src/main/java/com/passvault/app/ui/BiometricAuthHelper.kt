package com.passvault.app.ui

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher

/**
 * Bloque 5 — envuelve BiometricPrompt para poder lanzarlo desde Compose.
 * Requiere que la Activity anfitriona sea (o herede de) FragmentActivity.
 */
class BiometricAuthHelper(private val activity: FragmentActivity) {

    fun authenticate(
        cipher: Cipher,
        title: String,
        onSuccess: (Cipher) -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                val resultCipher = result.cryptoObject?.cipher
                if (resultCipher != null) {
                    onSuccess(resultCipher)
                } else {
                    onError("No se pudo completar la autenticación")
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                // Huella/rostro no reconocido: el prompt sigue abierto,
                // Android deja reintentar solo.
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        BiometricPrompt(activity, executor, callback)
            .authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
    }
}

@Composable
fun rememberBiometricAuthHelper(): BiometricAuthHelper? {
    val context = LocalContext.current
    return remember(context) { context.findFragmentActivity()?.let { BiometricAuthHelper(it) } }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}