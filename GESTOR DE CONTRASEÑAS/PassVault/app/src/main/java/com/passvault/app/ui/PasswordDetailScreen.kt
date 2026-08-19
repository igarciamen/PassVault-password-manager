package com.passvault.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passvault.app.domain.EntryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: PasswordDetailViewModel = hiltViewModel()
) {
    val entry by viewModel.entry.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    var secretQuestionVisible by remember { mutableStateOf(false) }

    val answerAttempt by viewModel.answerAttempt.collectAsState()
    val checkResult by viewModel.checkResult.collectAsState()
    val isChecking by viewModel.isChecking.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }
            )
        }
    ) { padding ->
        val current = entry
        if (current == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(current.title, style = MaterialTheme.typography.headlineSmall)
                if (current.username.isNotBlank()) {
                    Text("Usuario: ${current.username}")
                }

                when (current.entryType) {
                    // ===== CASO "CONTRASEÑA": se quitó "Copiar" por no ser fiable el borrado =====
                    EntryType.PASSWORD -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (passwordVisible) "Contraseña: ${current.password}" else "Contraseña: ••••••••")
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Ocultar" else "Mostrar")
                            }
                        }

                        if (!current.reminderQuestion.isNullOrBlank()) {
                            Text(
                                "💡 ${current.reminderQuestion}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    EntryType.SECRET_QUESTION -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (secretQuestionVisible) "Pregunta secreta configurada" else "Pregunta secreta: ••••••••")
                            TextButton(onClick = {
                                secretQuestionVisible = !secretQuestionVisible
                                if (!secretQuestionVisible) viewModel.resetAnswerCheck()
                            }) {
                                Text(if (secretQuestionVisible) "Ocultar" else "Mostrar")
                            }
                        }

                        if (secretQuestionVisible) {
                            Text(
                                "Pregunta: ${current.secretQuestion.orEmpty()}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            OutlinedTextField(
                                value = answerAttempt,
                                onValueChange = viewModel::onAnswerAttemptChange,
                                label = { Text("Tu respuesta") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                enabled = !isChecking,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.checkAnswer() },
                                    enabled = answerAttempt.isNotBlank() && !isChecking
                                ) {
                                    if (isChecking) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text("Comprobar")
                                    }
                                }

                                when (checkResult) {
                                    SecretAnswerCheckResult.CORRECT -> Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Respuesta correcta",
                                        tint = Color(0xFF2E7D32)
                                    )
                                    SecretAnswerCheckResult.INCORRECT -> Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Respuesta incorrecta",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    SecretAnswerCheckResult.NONE -> {}
                                }
                            }

                            Text(
                                "Por seguridad, la respuesta nunca se muestra ni se guarda en texto legible.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onEdit(current.id) }) { Text("Editar") }
                    OutlinedButton(onClick = { viewModel.delete(onDeleted) }) { Text("Eliminar") }
                }
            }
        }
    }
}