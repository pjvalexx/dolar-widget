package com.example.dolar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class DolarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        
        // Schedule the periodic work
        scheduleWork(context)

        // Set up the click listener for all widgets
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.dolar_widget_layout)
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent) // widget_root should be the ID of your root layout in the widget
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start the work when the first widget is created
        scheduleWork(context)
    }

    private fun scheduleWork(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<DolarRateWorker>(5, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}