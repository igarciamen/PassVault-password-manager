package com.passvault.app.autofill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutofillUnlockScreen(
    onEntrySelected: (username: String, password: String) -> Unit,
    onCancelled: () -> Unit,
    viewModel: AutofillUnlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("PassVault") }) }) { padding ->
        if (!uiState.isUnlocked) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Introduce tu contraseña maestra para autocompletar")
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Contraseña maestra") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )
                uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onClick = { viewModel.unlock() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Desbloquear")
                }
                TextButton(onClick = onCancelled, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Text("Elige qué contraseña usar:", style = MaterialTheme.typography.titleMedium) }
                items(uiState.entries) { entry ->
                    ElevatedCard(
                        onClick = { onEntrySelected(entry.username, entry.password) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.titleSmall)
                            Text(entry.username, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}