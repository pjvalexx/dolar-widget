package com.example.dolar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dolar.ui.theme.DolarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DolarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ConversorScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ConversorScreen(modifier: Modifier = Modifier, dolarViewModel: DolarViewModel = viewModel()) {
    val dolarRate by dolarViewModel.dolarPromedio.collectAsState()
    val error by dolarViewModel.error.collectAsState()

    var amountBs by remember { mutableStateOf("") }
    var amountUsd by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Changed from Center to Top
    ) {
        Spacer(modifier = Modifier.height(32.dp)) // Added space at the top

        Text(
            text = "Conversor de Moneda",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = amountBs,
            onValueChange = { newBs ->
                amountBs = newBs
                val bsValue = newBs.toDoubleOrNull()
                if (bsValue != null && dolarRate > 0) {
                    amountUsd = String.format("%.2f", bsValue / dolarRate)
                } else if (newBs.isEmpty()) {
                    amountUsd = ""
                }
            },
            label = { Text("Monto en bolívares (Bs)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = amountUsd,
            onValueChange = { newUsd ->
                amountUsd = newUsd
                val usdValue = newUsd.toDoubleOrNull()
                if (usdValue != null && dolarRate > 0) {
                    amountBs = String.format("%.2f", usdValue * dolarRate)
                } else if (newUsd.isEmpty()) {
                    amountBs = ""
                }
            },
            label = { Text("Monto en dólares (USD)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (error != null) {
            Text(text = error!!, color = MaterialTheme.colorScheme.error)
        } else {
            Text(
                text = "Tasa del día (promedio): ${String.format("%.2f", dolarRate)} Bs",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}