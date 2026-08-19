package com.passvault.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passvault.app.domain.EntryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: PasswordEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Editar entrada" else "Nueva entrada") },
                navigationIcon = { TextButton(onClick = onCancel) { Text("← Cancelar") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ===== Selector de tipo de entrada =====
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.entryType == EntryType.PASSWORD,
                    onClick = { viewModel.onEntryTypeChange(EntryType.PASSWORD) },
                    label = { Text("Contraseña") }
                )
                FilterChip(
                    selected = uiState.entryType == EntryType.SECRET_QUESTION,
                    onClick = { viewModel.onEntryTypeChange(EntryType.SECRET_QUESTION) },
                    label = { Text("Pregunta secreta") }
                )
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            when (uiState.entryType) {
                EntryType.PASSWORD -> {
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Usuario") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.reminderQuestion,
                        onValueChange = viewModel::onReminderQuestionChange,
                        label = { Text("Pregunta para recordarla (opcional)") },
                        supportingText = { Text("Ej: ¿Qué variante uso para el email?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                EntryType.SECRET_QUESTION -> {
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = { Text("Usuario (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.secretQuestion,
                        onValueChange = viewModel::onSecretQuestionChange,
                        label = { Text("Pregunta secreta") },
                        supportingText = { Text("Se mostrará; la respuesta nunca se guarda visible.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.secretAnswer,
                        onValueChange = viewModel::onSecretAnswerChange,
                        label = {
                            Text(
                                if (uiState.hasExistingSecretAnswer)
                                    "Nueva respuesta (déjalo en blanco para no cambiarla)"
                                else
                                    "Respuesta secreta"
                            )
                        },
                        supportingText = {
                            if (uiState.hasExistingSecretAnswer) {
                                Text("Ya hay una respuesta guardada. No se puede mostrar; solo reemplazar.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = uiState.isValid,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }
        }
    }
}