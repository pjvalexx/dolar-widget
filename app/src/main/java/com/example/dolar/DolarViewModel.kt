package com.example.dolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DolarViewModel : ViewModel() {

    private val _dolarPromedio = MutableStateFlow(0.0)
    val dolarPromedio: StateFlow<Double> = _dolarPromedio

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchDolarRate()
    }

    fun fetchDolarRate() {
        viewModelScope.launch {
            try {
                _error.value = null
                val response = RetrofitClient.instance.getOficialDolar()
                _dolarPromedio.value = response.promedio
            } catch (e: Exception) {
                _error.value = "Error fetching dollar rate: ${e.message}"
            }
        }
    }
}