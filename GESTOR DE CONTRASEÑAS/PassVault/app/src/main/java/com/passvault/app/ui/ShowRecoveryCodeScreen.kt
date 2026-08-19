package com.passvault.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowRecoveryCodeScreen(
    onDone: () -> Unit,
    viewModel: ShowRecoveryCodeViewModel = hiltViewModel()
) {
    val confirmed by viewModel.confirmed.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Tu código de recuperación") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Este código es la única forma de recuperar el acceso si olvidas tu " +
                    "contraseña maestra. Solo se muestra una vez, ahora mismo."
            )

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = viewModel.recoveryCode,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier.fillMaxWidth().padding(20.dp)
                )
            }

            Text(
                "Escríbelo a mano y guárdalo fuera del móvil (papel, caja fuerte). " +
                    "No lo guardes como captura de pantalla ni en el propio teléfono.",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onConfirmedChange(!confirmed) } // ===== AÑADIDO =====
            ) {
                Checkbox(checked = confirmed, onCheckedChange = viewModel::onConfirmedChange)
                Text("He guardado este código en un lugar seguro")
            }

            Button(
                onClick = { viewModel.finish(onDone) },
                enabled = confirmed,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continuar") }
        }
    }
}