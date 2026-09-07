package com.example.dolar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Las 3 tasas que muestra la app. Reemplaza a los viejos selectores
// USD/EUR y Oficial/Paralelo, que no tenían mucho sentido para el usuario.
enum class TasaTipo {
    BCV, EURO_BCV, USDT
}

class DolarViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    private val _bcvRate = MutableStateFlow(0.0)
    val bcvRate: StateFlow<Double> = _bcvRate

    private val _euroBcvRate = MutableStateFlow(0.0)
    val euroBcvRate: StateFlow<Double> = _euroBcvRate

    private val _usdtRate = MutableStateFlow(0.0)
    val usdtRate: StateFlow<Double> = _usdtRate

    // Fecha (dd/MM) que la propia API reporta para esa tasa. El BCV no
    // publica sábado ni domingo, así que estos campos dejan ver con
    // claridad que el fin de semana se sigue mostrando la del viernes
    // (no la del próximo lunes, que todavía no existe).
    private val _bcvFecha = MutableStateFlow("-")
    val bcvFecha: StateFlow<String> = _bcvFecha

    private val _euroBcvFecha = MutableStateFlow("-")
    val euroBcvFecha: StateFlow<String> = _euroBcvFecha

    // Hora del último refresco exitoso (hora del teléfono, no la que reporta
    // la API: el BCV solo publica su tasa una vez al día, así que si
    // mostrábamos esa fecha, tocar "refrescar" parecía no hacer nada).
    private val _lastUpdate = MutableStateFlow("-")
    val lastUpdate: StateFlow<String> = _lastUpdate

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedTasa = MutableStateFlow(TasaTipo.BCV)
    val selectedTasa: StateFlow<TasaTipo> = _selectedTasa

    init {
        // Mostrar de inmediato la última tasa BCV conocida (la que cachea el
        // widget) mientras se espera la respuesta de red.
        viewModelScope.launch {
            _bcvRate.value = dataStore.lastOficialFlow.first()
            _lastUpdate.value = dataStore.lastUpdateFlow.first()
        }
        refreshAll()
    }

    fun selectTasa(tasa: TasaTipo) {
        _selectedTasa.value = tasa
    }

    // Refresca las 3 tasas a la vez. Deja "isRefreshing" en true mientras
    // corre (para el spinner) y al terminar actualiza "lastUpdate" a la hora
    // actual, así siempre se ve un cambio al tocar el botón.
    fun refreshAll() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            coroutineScope {
                launch { fetchBcv() }
                launch { fetchEuroBcv() }
                launch { fetchUsdt() }
            }
            _lastUpdate.value = horaActual()
            _isRefreshing.value = false
        }
    }

    // BCV (dólar oficial). También guarda en caché y repinta el widget, que
    // sigue mostrando BCV + Paralelo (esta última no se muestra en la app,
    // pero se sigue guardando para no romper el widget).
    private suspend fun fetchBcv() {
        try {
            val response = RetrofitClient.instance.getDolares()
            val oficial = response.find { it.fuente == "oficial" }
            val oficialRate = oficial?.promedio ?: 0.0
            val paralelo = response.find { it.fuente == "paralelo" }?.promedio ?: 0.0
            _bcvRate.value = oficialRate
            _bcvFecha.value = formatearFecha(oficial?.fechaActualizacion)
            dataStore.saveRates(oficialRate, paralelo, horaActual())
            WidgetUpdater.refreshFromCache(getApplication())
        } catch (e: Exception) {
            _error.value = "Error al actualizar BCV: ${e.message}"
        }
    }

    private suspend fun fetchEuroBcv() {
        try {
            val response = RetrofitClient.instance.getEuros()
            response.find { it.fuente == "oficial" }?.let {
                _euroBcvRate.value = it.promedio
                _euroBcvFecha.value = formatearFecha(it.fechaActualizacion)
            }
        } catch (e: Exception) {
            _error.value = "Error al actualizar Euro BCV: ${e.message}"
        }
    }

    private suspend fun fetchUsdt() {
        try {
            val response = CriptoYaRetrofitClient.instance.getUsdtVes()
            response["binancep2p"]?.let {
                _usdtRate.value = (it.ask + it.bid) / 2.0
            }
        } catch (e: Exception) {
            _error.value = "Error al actualizar USDT: ${e.message}"
        }
    }

    private fun horaActual(): String {
        val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    // La API devuelve fechas ISO con formatos de huso ligeramente distintos
    // según la fuente ("...Z" o "...-04:00"), así que en vez de parsear con
    // un patrón exacto (frágil) simplemente se toman los primeros 10
    // caracteres ("yyyy-MM-dd"), que siempre vienen así.
    private fun formatearFecha(dateString: String?): String {
        if (dateString == null || dateString.length < 10) return "-"
        val soloFecha = dateString.substring(0, 10)
        val partes = soloFecha.split("-")
        return if (partes.size == 3) "${partes[2]}/${partes[1]}" else "-"
    }
}
