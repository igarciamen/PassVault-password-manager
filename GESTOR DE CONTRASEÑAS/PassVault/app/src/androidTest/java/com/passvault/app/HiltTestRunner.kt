package com.passvault.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Bloque 0 — runner necesario para que los tests instrumentados con Hilt
 * arranquen con HiltTestApplication en lugar de PassVaultApplication.
 * Ya está referenciado desde app/build.gradle.kts (testInstrumentationRunner).
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}