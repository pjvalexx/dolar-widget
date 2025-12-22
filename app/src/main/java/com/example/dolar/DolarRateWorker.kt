package com.example.dolar

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class DolarRateWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dataStore = SettingsDataStore(applicationContext)
        return try {
            // Fetch the list of rates
            val response = RetrofitClient.instance.getDolares()

            // Find the specific rates
            val oficialRate = response.find { it.fuente == "oficial" }?.promedio ?: 0.0
            val paraleloRate = response.find { it.fuente == "paralelo" }?.promedio ?: 0.0

            // Get the saved color
            val colorHex = dataStore.widgetTextColorFlow.first()
            val color = Color.parseColor(colorHex)

            // Update the widget with both rates and the chosen color
            updateWidget(applicationContext, oficialRate, paraleloRate, color)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun updateWidget(context: Context, oficialRate: Double, paraleloRate: Double, color: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, DolarWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.dolar_widget_layout)

            // Update text
            views.setTextViewText(R.id.widget_oficial_rate, String.format("%.2f Bs", oficialRate))
            views.setTextViewText(R.id.widget_paralelo_rate, String.format("%.2f Bs", paraleloRate))

            // Update color
            views.setTextColor(R.id.widget_oficial_rate_label, color)
            views.setTextColor(R.id.widget_oficial_rate, color)
            views.setTextColor(R.id.widget_paralelo_rate_label, color)
            views.setTextColor(R.id.widget_paralelo_rate, color)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}