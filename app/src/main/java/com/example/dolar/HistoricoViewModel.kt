package com.example.dolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

// Una fila del histórico: fecha + tasas (null = no hubo dato ese día,
// típicamente fines de semana para BCV/Euro BCV).
data class DiaHistorico(
    val fechaTexto: String,
    val oficial: Double?,
    val euro: Double?,
    val paralelo: Double?
)

// Calendar.DAY_OF_WEEK va de 1 (domingo) a 7 (sábado); el índice de este
// arreglo coincide restando 1.
private val NOMBRES_DIA = arrayOf(
    "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
)

// Una fecha ya formateada, lista para pedir a la API (año/mes/día en texto).
private data class FechaConsulta(
    val fechaTexto: String,
    val anio: String,
    val mes: String,
    val dia: String
)

class HistoricoViewModel : ViewModel() {

    private val _dias = MutableStateFlow<List<DiaHistorico>>(emptyList())
    val dias: StateFlow<List<DiaHistorico>> = _dias

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        cargarHistorico()
    }

    fun cargarHistorico() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val calendar = Calendar.getInstance()
                val fechas = mutableListOf<FechaConsulta>()

                // Los últimos 7 días, del más reciente al más viejo.
                repeat(7) {
                    val anio = String.format(Locale.US, "%04d", calendar.get(Calendar.YEAR))
                    val mes = String.format(Locale.US, "%02d", calendar.get(Calendar.MONTH) + 1)
                    val dia = String.format(Locale.US, "%02d", calendar.get(Calendar.DAY_OF_MONTH))
                    val nombreDia = NOMBRES_DIA[calendar.get(Calendar.DAY_OF_WEEK) - 1]
                    fechas.add(FechaConsulta("$nombreDia $dia/$mes", anio, mes, dia))
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                }

                // Se piden los 7 días en paralelo (cada uno pide BCV, Euro
                // BCV y Paralelo) para que la pantalla no se sienta lenta:
                // son 21 llamaditas en total, pero livianas.
                val resultado = coroutineScope {
                    fechas.map { f -> async { cargarUnDia(f) } }.map { it.await() }
                }

                _dias.value = resultado

                if (resultado.all { it.oficial == null && it.euro == null && it.paralelo == null }) {
                    _error.value = "No se pudo cargar el histórico. Revisa tu conexión."
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar histórico: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun cargarUnDia(f: FechaConsulta): DiaHistorico {
        val oficial = try {
            RetrofitClient.instance.getHistoricoDolar("oficial", f.anio, f.mes, f.dia).promedio
        } catch (e: Exception) {
            null // no hubo publicación ese día (fin de semana) o falló la red
        }

        val euro = try {
            RetrofitClient.instance.getHistoricoEuro("oficial", f.anio, f.mes, f.dia).promedio
        } catch (e: Exception) {
            null
        }

        val paralelo = try {
            RetrofitClient.instance.getHistoricoDolar("paralelo", f.anio, f.mes, f.dia).promedio
        } catch (e: Exception) {
            null
        }

        return DiaHistorico(f.fechaTexto, oficial, euro, paralelo)
    }
}
