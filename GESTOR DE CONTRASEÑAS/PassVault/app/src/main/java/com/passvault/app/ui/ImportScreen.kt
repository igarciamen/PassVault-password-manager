package com.passvault.app.ui

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
fun ImportScreen(
    onBack: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pickedFileBase64 by viewModel.pickedFileBase64.collectAsState()
    val context = LocalContext.current

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) viewModel.onFilePicked(bytes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar copia de seguridad") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { openDocumentLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (pickedFileBase64 == null) "Elegir archivo de backup" else "Archivo seleccionado ✓") }

            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña de exportación") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            uiState.importedCount?.let {
                Text("✅ Se importaron $it contraseñas correctamente", color = MaterialTheme.colorScheme.primary)
            }

            Button(
                onClick = { viewModel.importFrom() },
                enabled = pickedFileBase64 != null && uiState.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Importar") }
        }
    }
}