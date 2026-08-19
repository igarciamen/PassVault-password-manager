package com.passvault.app.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passvault.app.security.AutoLockPreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenSecurityAudit: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenImport: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val biometricHelper = rememberBiometricAuthHelper()
    val context = LocalContext.current

    // ===== AÑADIDO BLOQUE 8 (Parte 2) =====
    val autofillSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshAutofillStatus() }
    // ===== FIN AÑADIDO =====

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // ===== AÑADIDO =====
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Desbloqueo biométrico", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (uiState.biometricAvailable)
                            "Usa tu huella o rostro para desbloquear la app."
                        else
                            "Tu dispositivo no tiene biometría configurada.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = uiState.biometricEnabled,
                    enabled = uiState.biometricAvailable,
                    onCheckedChange = { checked ->
                        if (checked) {
                            val cipher = viewModel.prepareEncryptCipher()
                            if (cipher != null && biometricHelper != null) {
                                biometricHelper.authenticate(
                                    cipher = cipher,
                                    title = "Confirma tu identidad",
                                    onSuccess = { viewModel.onBiometricEnrollSuccess(it) },
                                    onError = { viewModel.onBiometricError(it) }
                                )
                            }
                        } else {
                            viewModel.disableBiometric()
                        }
                    }
                )
            }

            uiState.message?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider()

            Text("Bloqueo automático", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tiempo que puede pasar en segundo plano antes de pedir desbloquear de nuevo.",
                style = MaterialTheme.typography.bodySmall
            )

            AutoLockPreferenceManager.AVAILABLE_OPTIONS.forEach { seconds ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.autoLockTimeoutSeconds == seconds,
                            onClick = { viewModel.setAutoLockTimeout(seconds) }
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.autoLockTimeoutSeconds == seconds,
                        onClick = { viewModel.setAutoLockTimeout(seconds) }
                    )
                    Text(
                        text = when (seconds) {
                            0 -> "Inmediato"
                            30 -> "30 segundos"
                            60 -> "1 minuto"
                            300 -> "5 minutos"
                            else -> "$seconds s"
                        }
                    )
                }
            }

            HorizontalDivider()

            // ===== INICIO AÑADIDO BLOQUE 8 (Parte 2) =====
            Text("Autocompletado", style = MaterialTheme.typography.titleMedium)
            Text(
                if (uiState.autofillEnabled)
                    "PassVault está activo como servicio de autocompletado."
                else
                    "Actívalo para rellenar contraseñas en otras apps sin copiar y pegar.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    autofillSettingsLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.autofillEnabled) "Cambiar servicio de autocompletado" else "Activar autocompletado")
            }
            // ===== FIN AÑADIDO =====

            HorizontalDivider()

            Text("Datos y seguridad", style = MaterialTheme.typography.titleMedium)

            Button(onClick = onOpenSecurityAudit, modifier = Modifier.fillMaxWidth()) {
                Text("Auditoría de contraseñas débiles/reutilizadas")
            }
            Button(onClick = onOpenExport, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar copia de seguridad")
            }
            Button(onClick = onOpenImport, modifier = Modifier.fillMaxWidth()) {
                Text("Importar copia de seguridad")
            }
        }
    }
}