package com.example.dolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dolar.ui.theme.DolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DolarTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "conversor") {
                    composable("conversor") {
                        ConversorScreen(
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToHistorico = { navController.navigate("historico") }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable("historico") {
                        HistoricoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

// Acepta "," o "." como separador decimal (el teclado de algunos teléfonos
// muestra una "," que rompía la conversión a número) y descarta cualquier
// otro carácter, dejando como mucho un solo punto decimal.
private fun sanitizarMonto(input: String): String {
    val normalizado = input.replace(',', '.')
    val resultado = StringBuilder()
    var puntoVisto = false
    for (c in normalizado) {
        if (c.isDigit()) {
            resultado.append(c)
        } else if (c == '.' && !puntoVisto) {
            puntoVisto = true
            resultado.append(c)
        }
    }
    return resultado.toString()
}

private fun etiquetaTasa(tasa: TasaTipo): String = when (tasa) {
    TasaTipo.BCV -> "dólares (USD)"
    TasaTipo.EURO_BCV -> "euros (EUR)"
    TasaTipo.USDT -> "USDT"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversorScreen(
    modifier: Modifier = Modifier,
    dolarViewModel: DolarViewModel = viewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToHistorico: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversor de Moneda") },
                actions = {
                    IconButton(onClick = onNavigateToHistorico) {
                        Icon(Icons.Default.DateRange, contentDescription = "Histórico")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) { padding ->
        val bcvRate by dolarViewModel.bcvRate.collectAsState()
        val euroBcvRate by dolarViewModel.euroBcvRate.collectAsState()
        val usdtRate by dolarViewModel.usdtRate.collectAsState()
        val bcvFecha by dolarViewModel.bcvFecha.collectAsState()
        val euroBcvFecha by dolarViewModel.euroBcvFecha.collectAsState()
        val lastUpdate by dolarViewModel.lastUpdate.collectAsState()
        val isRefreshing by dolarViewModel.isRefreshing.collectAsState()
        val error by dolarViewModel.error.collectAsState()
        val selectedTasa by dolarViewModel.selectedTasa.collectAsState()

        val currentRate = when (selectedTasa) {
            TasaTipo.BCV -> bcvRate
            TasaTipo.EURO_BCV -> euroBcvRate
            TasaTipo.USDT -> usdtRate
        }
        val monedaLabel = etiquetaTasa(selectedTasa)

        var amountBs by remember { mutableStateOf("") }
        var amountMoneda by remember { mutableStateOf("") }

        fun seleccionarTasa(tasa: TasaTipo) {
            dolarViewModel.selectTasa(tasa)
            val bsValue = amountBs.toDoubleOrNull()
            if (bsValue != null) {
                val nuevaTasa = when (tasa) {
                    TasaTipo.BCV -> bcvRate
                    TasaTipo.EURO_BCV -> euroBcvRate
                    TasaTipo.USDT -> usdtRate
                }
                if (nuevaTasa > 0) {
                    amountMoneda = String.format("%.2f", bsValue / nuevaTasa)
                }
            }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Conversor de Moneda", style = MaterialTheme.typography.headlineLarge)

            Spacer(modifier = Modifier.height(20.dp))

            // Las 3 tasas de la app. Tocar una la selecciona para la calculadora de abajo.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TasaCard("BCV", bcvRate, selected = selectedTasa == TasaTipo.BCV, fecha = bcvFecha) {
                    seleccionarTasa(TasaTipo.BCV)
                }
                TasaCard("USDT", usdtRate, selected = selectedTasa == TasaTipo.USDT) {
                    seleccionarTasa(TasaTipo.USDT)
                }
                TasaCard("Euro BCV", euroBcvRate, selected = selectedTasa == TasaTipo.EURO_BCV, fecha = euroBcvFecha) {
                    seleccionarTasa(TasaTipo.EURO_BCV)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Last update and refresh button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizado: $lastUpdate", style = MaterialTheme.typography.bodySmall)
                IconButton(onClick = { dolarViewModel.refreshAll() }, enabled = !isRefreshing) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar tasas")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = amountMoneda,
                onValueChange = { raw ->
                    val texto = sanitizarMonto(raw)
                    amountMoneda = texto
                    val valor = texto.toDoubleOrNull()
                    if (valor != null && currentRate > 0) {
                        amountBs = String.format("%.2f", valor * currentRate)
                    } else if (texto.isEmpty()) {
                        amountBs = ""
                    }
                },
                label = { Text("Monto en $monedaLabel") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(280.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountBs,
                onValueChange = { raw ->
                    val texto = sanitizarMonto(raw)
                    amountBs = texto
                    val bsValue = texto.toDoubleOrNull()
                    if (bsValue != null && currentRate > 0) {
                        amountMoneda = String.format("%.2f", bsValue / currentRate)
                    } else if (texto.isEmpty()) {
                        amountMoneda = ""
                    }
                },
                label = { Text("Monto en bolívares (Bs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(280.dp)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            // Espacio de sobra para que el teclado nunca deje el último campo pegado al borde.
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TasaCard(title: String, rate: Double, selected: Boolean, fecha: String = "-", onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(108.dp)
            .clickable(onClick = onClick),
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (rate > 0) String.format("%.2f", rate) else "--",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text("Bs", style = MaterialTheme.typography.labelSmall)
            // Fecha que reporta la API para esta tasa (BCV/Euro BCV no
            // publican fin de semana, así que ahí se ve la del viernes).
            if (fecha != "-") {
                Text("($fecha)", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
