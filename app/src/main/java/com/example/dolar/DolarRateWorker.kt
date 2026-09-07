package com.example.dolar

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DolarRateWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = SettingsDataStore(applicationContext)
        return try {
            // Fetch the list of rates
            val response = RetrofitClient.instance.getDolares()

            val oficial = response.find { it.fuente == "oficial" }
            val paralelo = response.find { it.fuente == "paralelo" }
            val oficialRate = oficial?.promedio ?: 0.0
            val paraleloRate = paralelo?.promedio ?: 0.0
            // Hora local del chequeo, no la fecha que reporta la API: el BCV
            // solo publica una vez al día, así que si mostrábamos esa fecha
            // el botón de refrescar parecía no hacer nada.
            val updateText = horaActual()

            // Guardar como "última tasa buena conocida" y repintar el widget.
            dataStore.saveRates(oficialRate, paraleloRate, updateText)
            WidgetUpdater.refreshFromCache(applicationContext)
            Result.success()
        } catch (e: Exception) {
            // No borramos los valores anteriores: solo avisamos que ya no
            // son frescos y repintamos el widget con lo último que sabíamos,
            // en vez de dejarlo en "0.00 Bs".
            dataStore.markRatesStale()
            WidgetUpdater.refreshFromCache(applicationContext)
            Result.retry()
        }
    }

    private fun horaActual(): String {
        val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }
}
