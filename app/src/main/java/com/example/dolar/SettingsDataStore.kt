package com.example.dolar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        val WIDGET_TEXT_COLOR_KEY = stringPreferencesKey("widget_text_color")
    }

    val widgetTextColorFlow: Flow<String> = context.dataStore.data
        .map {
            it[WIDGET_TEXT_COLOR_KEY] ?: "#FF000000" // Default to black
        }

    suspend fun saveWidgetTextColor(color: String) {
        context.dataStore.edit {
            it[WIDGET_TEXT_COLOR_KEY] = color
        }
    }
}