package com.example.dolar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class DolarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        // Start the periodic work
        val workRequest = PeriodicWorkRequestBuilder<DolarRateWorker>(5, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}