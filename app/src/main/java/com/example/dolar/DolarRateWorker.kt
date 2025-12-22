package com.example.dolar

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DolarRateWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val response = RetrofitClient.instance.getOficialDolar()
            val rate = response.promedio
            updateWidget(applicationContext, rate)
            Result.success()
        } catch (e: Exception) {
            // Optionally, handle the error, e.g., show an error message in the widget
            Result.failure()
        }
    }

    private fun updateWidget(context: Context, rate: Double) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, DolarWidgetProvider::class.java)
        val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        for (widgetId in allWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.dolar_widget_layout)
            views.setTextViewText(R.id.widget_dolar_rate, String.format("Bs %.2f", rate))
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}