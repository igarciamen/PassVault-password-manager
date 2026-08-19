package com.passvault.app.security

import java.util.Arrays
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda temporalmente en memoria (nunca en disco) una passphrase mientras
 * dura un flujo de dos pantallas: crear contraseña -> mostrar código de
 * recuperación, o recuperar acceso -> fijar contraseña nueva.
 */
@Singleton
class PendingPassphraseHolder @Inject constructor() {

    @Volatile
    private var passphrase: ByteArray? = null

    fun set(value: ByteArray) {
        passphrase = value
    }

    fun get(): ByteArray? = passphrase

    fun clear() {
        passphrase?.let { Arrays.fill(it, 0) }
        passphrase = null
    }
}