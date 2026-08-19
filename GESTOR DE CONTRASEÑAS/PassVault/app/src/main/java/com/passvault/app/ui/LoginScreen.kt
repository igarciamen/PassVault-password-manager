package com.passvault.app.ui

import androidx.compose.foundation.layout.*
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
fun LoginScreen(
    onSuccess: () -> Unit,
    onRecoverAccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val biometricHelper = rememberBiometricAuthHelper()

    LaunchedEffect(uiState.biometricEnabled, uiState.biometricAvailable) {
        if (uiState.biometricEnabled && uiState.biometricAvailable) {
            viewModel.prepareBiometricDecryptCipher()?.let { cipher ->
                biometricHelper?.authenticate(
                    cipher = cipher,
                    title = "Desbloquea PassVault",
                    onSuccess = { viewModel.onBiometricUnlockSuccess(it, onSuccess) },
                    onError = { viewModel.onBiometricError(it) }
                )
            }
        }
    }

    val isWaitingBackoff = uiState.backoffRemainingSeconds > 0

    Scaffold(topBar = { TopAppBar(title = { Text("PassVault") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Introduce tu contraseña maestra para desbloquear")
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text("Contraseña maestra") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !uiState.isLockedOut && !isWaitingBackoff && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (isWaitingBackoff) {
                Text(
                    "Espera ${uiState.backoffRemainingSeconds}s antes de volver a intentarlo",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            if (uiState.isLockedOut) {
                Button(onClick = onRecoverAccess, modifier = Modifier.fillMaxWidth()) {
                    Text("Recuperar acceso")
                }
            } else {
                Button(
                    onClick = { viewModel.login(onSuccess) },
                    enabled = uiState.password.isNotBlank() && !isWaitingBackoff && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // ===== AÑADIDO: spinner mientras se verifica =====
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Desbloquear")
                    }
                }

                if (uiState.biometricEnabled && uiState.biometricAvailable) {
                    OutlinedButton(
                        onClick = {
                            viewModel.prepareBiometricDecryptCipher()?.let { cipher ->
                                biometricHelper?.authenticate(
                                    cipher = cipher,
                                    title = "Desbloquea PassVault",
                                    onSuccess = { viewModel.onBiometricUnlockSuccess(it, onSuccess) },
                                    onError = { viewModel.onBiometricError(it) }
                                )
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Usar huella / rostro") }
                }

                TextButton(
                    onClick = onRecoverAccess,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¿Olvidaste tu contraseña?")
                }
            }
        }
    }
}