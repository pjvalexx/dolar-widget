package com.example.dolar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val WIDGET_TEXT_COLOR_KEY = stringPreferencesKey("widget_text_color")
        val LAST_OFICIAL_KEY = stringPreferencesKey("last_oficial_rate")
        val LAST_PARALELO_KEY = stringPreferencesKey("last_paralelo_rate")
        val LAST_UPDATE_KEY = stringPreferencesKey("last_update_text")
        val LAST_UPDATE_OK_KEY = booleanPreferencesKey("last_update_ok")
    }

    // Color del texto del widget. El fondo del widget es transparente (usa
    // el fondo del launcher del usuario), así que si el texto blanco por
    // defecto no se lee bien en su pantalla, puede cambiarlo desde los
    // ajustes de la app.
    val widgetTextColorFlow: Flow<String> = context.dataStore.data
        .map {
            it[WIDGET_TEXT_COLOR_KEY] ?: "#FFFFFFFF" // Default a blanco
        }

    // Última tasa "oficial" que se pudo obtener con éxito (0.0 si nunca hubo ninguna).
    val lastOficialFlow: Flow<Double> = context.dataStore.data
        .map { it[LAST_OFICIAL_KEY]?.toDoubleOrNull() ?: 0.0 }

    // Última tasa "paralelo" que se pudo obtener con éxito (0.0 si nunca hubo ninguna).
    val lastParaleloFlow: Flow<Double> = context.dataStore.data
        .map { it[LAST_PARALELO_KEY]?.toDoubleOrNull() ?: 0.0 }

    // Texto formateado de la fecha/hora de la última actualización exitosa.
    val lastUpdateFlow: Flow<String> = context.dataStore.data
        .map { it[LAST_UPDATE_KEY] ?: "-" }

    // true si la última tasa mostrada vino de una actualización exitosa;
    // false si estamos mostrando un valor viejo porque falló la última consulta.
    val lastUpdateOkFlow: Flow<Boolean> = context.dataStore.data
        .map { it[LAST_UPDATE_OK_KEY] ?: true }

    suspend fun saveWidgetTextColor(color: String) {
        context.dataStore.edit {
            it[WIDGET_TEXT_COLOR_KEY] = color
        }
    }

    // Guarda una tasa obtenida con éxito. Esto es lo que el widget usa como
    // "última tasa conocida" cuando no hay internet.
    suspend fun saveRates(oficial: Double, paralelo: Double, updateText: String) {
        context.dataStore.edit {
            it[LAST_OFICIAL_KEY] = oficial.toString()
            it[LAST_PARALELO_KEY] = paralelo.toString()
            it[LAST_UPDATE_KEY] = updateText
            it[LAST_UPDATE_OK_KEY] = true
        }
    }

    // Se llama cuando falla una actualización: no se borran los últimos
    // valores buenos, solo se marca que ya no son "frescos".
    suspend fun markRatesStale() {
        context.dataStore.edit {
            it[LAST_UPDATE_OK_KEY] = false
        }
    }
}
