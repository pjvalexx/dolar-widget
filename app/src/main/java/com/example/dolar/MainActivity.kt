package com.example.dolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
                        ConversorScreen(onNavigateToSettings = { navController.navigate("settings") })
                    }
                    composable("settings") {
                        SettingsScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversorScreen(
    modifier: Modifier = Modifier, 
    dolarViewModel: DolarViewModel = viewModel(),
    onNavigateToSettings: () -> Unit
) {
    // ... (rest of the composable is the same as before)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversor de Moneda") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        }
    ) {
         val oficialRate by dolarViewModel.oficialRate.collectAsState()
    val paraleloRate by dolarViewModel.paraleloRate.collectAsState()
    val lastUpdate by dolarViewModel.lastUpdate.collectAsState()
    val error by dolarViewModel.error.collectAsState()
    val selectedRateType by dolarViewModel.selectedRateTye.collectAsState()

    val currentRate = if (selectedRateType == RateType.OFICIAL) oficialRate else paraleloRate

    var amountBs by remember { mutableStateOf("") }
    var amountUsd by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text("Conversor de Moneda", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // Rate display cards
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RateCard("Oficial (BCV)", oficialRate)
            RateCard("Paralelo", paraleloRate)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Last update and refresh button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizado: $lastUpdate", style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = { dolarViewModel.fetchDolarRates() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refrescar tasas")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Rate type selector
        SegmentedButtonRow(selectedRateType, onRateTypeChange = {
            dolarViewModel.selectRateType(it)
            val bsValue = amountBs.toDoubleOrNull()
            if (bsValue != null) {
                val newRate = if (it == RateType.OFICIAL) oficialRate else paraleloRate
                if (newRate > 0) {
                    amountUsd = String.format("%.2f", bsValue / newRate)
                }
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        // --- Converters: Swapped order and reduced width ---
        OutlinedTextField(
            value = amountUsd,
            onValueChange = {
                amountUsd = it
                val usdValue = it.toDoubleOrNull()
                if (usdValue != null && currentRate > 0) {
                    amountBs = String.format("%.2f", usdValue * currentRate)
                } else if (it.isEmpty()) {
                    amountBs = ""
                }
            },
            label = { Text("Monto en dólares (USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(280.dp) // Reduced width
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountBs,
            onValueChange = {
                amountBs = it
                val bsValue = it.toDoubleOrNull()
                if (bsValue != null && currentRate > 0) {
                    amountUsd = String.format("%.2f", bsValue / currentRate)
                } else if (it.isEmpty()) {
                    amountUsd = ""
                }
            },
            label = { Text("Monto en bolívares (Bs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(280.dp) // Reduced width
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
    }
}

@Composable
fun RateCard(title: String, rate: Double) {
    Card(modifier = Modifier.width(150.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(String.format("%.2f Bs", rate), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

// Add OptIn here as this composable uses the experimental components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedButtonRow(selected: RateType, onRateTypeChange: (RateType) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp),
            selected = selected == RateType.OFICIAL,
            onClick = { onRateTypeChange(RateType.OFICIAL) }
        ) {
            Text("Oficial")
        }
        SegmentedButton(
            shape = RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp),
            selected = selected == RateType.PARALELO,
            onClick = { onRateTypeChange(RateType.PARALELO) }
        ) {
            Text("Paralelo")
        }
    }
}
