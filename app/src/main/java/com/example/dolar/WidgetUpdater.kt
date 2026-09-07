package com.example.dolar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import kotlinx.coroutines.flow.first

/**
 * Único lugar donde se arma el RemoteViews del widget.
 *
 * Antes esto estaba repetido en DolarWidgetProvider (que ponía el click
 * para abrir la app) y en DolarRateWorker (que ponía el texto/color pero
 * sin el click). Como cada updateAppWidget() reemplaza el RemoteViews
 * completo, en cuanto el worker corría por primera vez el widget dejaba de
 * abrir la app al tocarlo. Ahora todo se pinta desde acá, siempre completo:
 * texto, color, click para abrir la app y click en el botón de refresco.
 */
object WidgetUpdater {

    const val ACTION_REFRESH = "com.example.dolar.ACTION_REFRESH_WIDGET"

    /** Repinta todos los widgets usando lo último que haya en la caché (sin red). */
    suspend fun refreshFromCache(context: Context) {
        val dataStore = SettingsDataStore(context)
        val oficial = dataStore.lastOficialFlow.first()
        val paralelo = dataStore.lastParaleloFlow.first()
        val lastUpdate = dataStore.lastUpdateFlow.first()
        val isFresh = dataStore.lastUpdateOkFlow.first()
        val colorHex = dataStore.widgetTextColorFlow.first()
        val color = try {
            Color.parseColor(colorHex)
        } catch (e: Exception) {
            Color.WHITE
        }
        applyToWidgets(context, oficial, paralelo, lastUpdate, isFresh, color)
    }

    fun applyToWidgets(
        context: Context,
        oficialRate: Double,
        paraleloRate: Double,
        lastUpdateText: String,
        isFresh: Boolean,
        color: Int
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, DolarWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        if (widgetIds.isEmpty()) return

        val oficialText = if (oficialRate > 0) String.format("%.2f", oficialRate) else "--"
        val paraleloText = if (paraleloRate > 0) String.format("%.2f", paraleloRate) else "--"
        val statusText = when {
            lastUpdateText == "-" -> "Actualizando..."
            isFresh -> "Act: $lastUpdateText"
            else -> "Sin conexión ($lastUpdateText)"
        }

        for (widgetId in widgetIds) {
            val views = RemoteViews(context.packageName, R.layout.dolar_widget_layout)

            views.setTextViewText(R.id.widget_oficial_rate, oficialText)
            views.setTextViewText(R.id.widget_paralelo_rate, paraleloText)
            views.setTextViewText(R.id.widget_last_update, statusText)

            views.setTextColor(R.id.widget_oficial_rate_label, color)
            views.setTextColor(R.id.widget_oficial_rate, color)
            views.setTextColor(R.id.widget_paralelo_rate_label, color)
            views.setTextColor(R.id.widget_paralelo_rate, color)
            views.setTextColor(R.id.widget_last_update, color)

            // Tocar el widget abre la app (desde ahí se puede refrescar,
            // el botón ⟳ propio del widget se quitó por pedido del usuario).
            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPending = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, openAppPending)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
