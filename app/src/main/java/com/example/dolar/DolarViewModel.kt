package com.example.dolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// Enum to represent the selected rate type
enum class RateType {
    OFICIAL, PARALELO
}

class DolarViewModel : ViewModel() {

    // Holds the rate for "Oficial"
    private val _oficialRate = MutableStateFlow(0.0)
    val oficialRate: StateFlow<Double> = _oficialRate

    // Holds the rate for "Paralelo"
    private val _paraleloRate = MutableStateFlow(0.0)
    val paraleloRate: StateFlow<Double> = _paraleloRate

    // Holds the last update date as a formatted string
    private val _lastUpdate = MutableStateFlow("-")
    val lastUpdate: StateFlow<String> = _lastUpdate

    // Holds any potential error message
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Holds the currently selected rate type for conversion
    private val _selectedRateType = MutableStateFlow(RateType.OFICIAL)
    val selectedRateTye: StateFlow<RateType> = _selectedRateType

    init {
        fetchDolarRates()
    }

    // Public function to allow the UI to refresh the data
    fun fetchDolarRates() {
        viewModelScope.launch {
            try {
                _error.value = null
                val response = RetrofitClient.instance.getDolares() // Use the new API call

                response.find { it.fuente == "oficial" }?.let {
                    _oficialRate.value = it.promedio
                    // Format the date for display
                    _lastUpdate.value = formatUpdateDate(it.fechaActualizacion)
                }
                response.find { it.fuente == "paralelo" }?.let {
                    _paraleloRate.value = it.promedio
                }

            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    // Function to change the selected rate type
    fun selectRateType(type: RateType) {
        _selectedRateType.value = type
    }

    // Helper to format the date string
    private fun formatUpdateDate(dateString: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            parser.parse(dateString)?.let { formatter.format(it) } ?: "-"
        } catch (e: Exception) {
            "-"
        }
    }
}
