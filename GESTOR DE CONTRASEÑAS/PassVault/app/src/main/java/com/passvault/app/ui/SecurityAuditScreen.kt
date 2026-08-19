package com.passvault.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuditScreen(
    onBack: () -> Unit,
    viewModel: SecurityAuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auditoría de seguridad") },
                navigationIcon = { TextButton(onClick = onBack) { Text("← Volver") } }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.weakEntries.isEmpty() && uiState.reusedGroups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("✅ No se han encontrado contraseñas débiles ni reutilizadas")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.weakEntries.isNotEmpty()) {
                item {
                    Text("Contraseñas débiles (${uiState.weakEntries.size})", style = MaterialTheme.typography.titleMedium)
                }
                items(uiState.weakEntries) { entry ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.titleSmall)
                            Text("Usuario: ${entry.username}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            if (uiState.reusedGroups.isNotEmpty()) {
                item {
                    Text(
                        "Contraseñas reutilizadas (${uiState.reusedGroups.size} grupos)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(uiState.reusedGroups) { group ->
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                "Estas ${group.size} entradas comparten la misma contraseña:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            group.forEach { entry ->
                                Text("• ${entry.title} (${entry.username})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}