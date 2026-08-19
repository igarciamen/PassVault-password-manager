package com.passvault.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.passvault.app.domain.EntryType
import com.passvault.app.domain.PasswordEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(
    onEntryClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: PasswordListViewModel = hiltViewModel()
) {

    android.util.Log.d("PassVaultTest", "PasswordListScreen composed")

    val passwords by viewModel.passwords.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()

    android.util.Log.d("PassVaultTest", "passwords.size=${passwords.size}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PassVault") },
                actions = {
                    TextButton(onClick = onSettingsClick) { Text("Ajustes") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Añadir contraseña")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = showOnlyFavorites,
                        onClick = { viewModel.toggleShowOnlyFavorites() },
                        label = { Text("★ Favoritos") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Todas") }
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category) }
                    )
                }
            }

            if (passwords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay contraseñas que coincidan con el filtro")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(passwords, key = { it.id }) { entry ->
                        PasswordListItem(
                            entry = entry,
                            onClick = { onEntryClick(entry.id) },
                            onDelete = { viewModel.delete(entry) },
                            onToggleFavorite = { viewModel.toggleFavorite(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordListItem(
    entry: PasswordEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ElevatedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    // ===== AÑADIDO: distinguir tipo de entrada en la lista =====
                    if (entry.entryType == EntryType.SECRET_QUESTION) "🔒 Pregunta secreta" else entry.username,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(entry.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                if (!entry.reminderQuestion.isNullOrBlank()) {
                    Text(
                        text = "💡 ${entry.reminderQuestion}",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TextButton(
                onClick = onToggleFavorite,
                modifier = Modifier.semantics {
                    contentDescription = if (entry.isFavorite) "Quitar de favoritos" else "Añadir a favoritos"
                }
            ) {
                Text(if (entry.isFavorite) "★" else "☆", style = MaterialTheme.typography.titleLarge)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}