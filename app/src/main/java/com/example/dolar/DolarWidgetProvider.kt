package com.example.dolar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DolarWidgetProvider : AppWidgetProvider() {

    companion object {
        // Nombres fijos para que WorkManager no cree trabajos duplicados.
        private const val PERIODIC_WORK_NAME = "dolar_rate_periodic_update"
        private const val REFRESH_WORK_NAME = "dolar_rate_manual_refresh"
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Pintar de inmediato con lo último que tengamos guardado, sin
        // esperar a que termine ninguna llamada de red. Así el widget nunca
        // se queda mostrando "..." más de una fracción de segundo.
        CoroutineScope(Dispatchers.IO).launch {
            WidgetUpdater.refreshFromCache(context)
        }

        scheduleWork(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Se agregó el primer widget: programar el ciclo periódico y pedir
        // datos frescos ya mismo (no esperar hasta el próximo ciclo de 5h).
        scheduleWork(context)
        requestImmediateRefresh(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdater.ACTION_REFRESH) {
            // El usuario tocó el botón ⟳ del widget.
            requestImmediateRefresh(context)
        }
    }

    // Sin conexión, que WorkManager espere en vez de gastar un intento
    // fallido: apenas vuelva la red, corre solo (no hay que esperar al
    // próximo ciclo de 15 min).
    private val constraintsDeRed = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private fun scheduleWork(context: Context) {
        // 15 minutos es el mínimo real que permite WorkManager para trabajo
        // periódico (Android no deja programar más seguido sin usar un
        // servicio en primer plano con notificación fija). Antes eran 5
        // horas; así el widget se mantiene prácticamente siempre al día.
        val workRequest = PeriodicWorkRequestBuilder<DolarRateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraintsDeRed)
            .build()

        // enqueueUniquePeriodicWork + KEEP evita que cada llamada a onUpdate
        // (que Android dispara periódicamente además de al agregar el
        // widget) cree un nuevo trabajo periódico duplicado en paralelo.
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun requestImmediateRefresh(context: Context) {
        val request = OneTimeWorkRequestBuilder<DolarRateWorker>()
            .setConstraints(constraintsDeRed)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            REFRESH_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
