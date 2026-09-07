package com.example.dolar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(onNavigateBack: () -> Unit, historicoViewModel: HistoricoViewModel = viewModel()) {
    val dias by historicoViewModel.dias.collectAsState()
    val isLoading by historicoViewModel.isLoading.collectAsState()
    val error by historicoViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Histórico (7 días)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (!isLoading && dias.isNotEmpty()) {
                HistoricoHeader()
                HorizontalDivider()
                LazyColumn {
                    items(dias) { dia ->
                        HistoricoRow(dia)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoricoHeader() {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text("Fecha", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium)
        Text("BCV", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
        Text("EUR", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
        Text("Paralelo", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun HistoricoRow(dia: DiaHistorico) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(dia.fechaTexto, modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.bodySmall)
        Text(formatearTasa(dia.oficial), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(formatearTasa(dia.euro), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(formatearTasa(dia.paralelo), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatearTasa(valor: Double?): String {
    return if (valor != null) String.format("%.2f", valor) else "-"
}
