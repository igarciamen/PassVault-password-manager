package com.passvault.app.ui

import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit,
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        // Leemos los bytes pendientes DESDE EL VIEWMODEL en el momento del
        // callback, no de una variable capturada — así funciona igual si
        // hubo una recreación de proceso mientras el selector estaba abierto.
        val base64 = viewModel.pendingExportBase64.value
        if (uri != null && base64 != null) {
            val bytes = Base64.decode(base64, Base64.NO_WRAP)
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
            viewModel.onExportWritten()
        } else {
            viewModel.clearPendingExport()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar copia de seguridad") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Elige una contraseña para proteger este archivo. La necesitarás " +
                        "para importarlo de vuelta — sin ella, el archivo es inútil."
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña de exportación") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text("Repite la contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (uiState.exportedOk) {
                Text("✅ Copia de seguridad guardada correctamente", color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = {
                    viewModel.prepareExport {
                        createDocumentLauncher.launch("passvault_backup.pvexport")
                    }
                },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Elegir dónde guardar") }
        }
    }
}