package com.passvault.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.passvault.app.ui.navigation.PassVaultNavGraph
import com.passvault.app.ui.theme.PassVaultTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== AÑADIDO BLOQUE 6 =====
        // Bloquea capturas de pantalla/grabación y oculta el contenido en el
        // multitarea (recent apps) — muestra un rectángulo en blanco en vez
        // de la última pantalla vista.
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        // ===== FIN AÑADIDO BLOQUE 6 =====

        enableEdgeToEdge()
        setContent {
            PassVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PassVaultNavGraph()
                }
            }
        }
    }
}