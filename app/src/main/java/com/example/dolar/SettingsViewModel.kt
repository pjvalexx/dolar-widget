package com.example.dolar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    // Get an instance of our DataStore
    private val settingsDataStore = SettingsDataStore(application)

    // Expose the color flow as a StateFlow so the UI can observe it
    val widgetTextColor = settingsDataStore.widgetTextColorFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "#FFFFFFFF" // Default blanco (fondo del widget es oscuro)
        )

    // Function to save the selected color
    fun saveWidgetTextColor(color: String) {
        viewModelScope.launch {
            settingsDataStore.saveWidgetTextColor(color)
            // Repintar el widget YA con el nuevo color, en vez de esperar
            // hasta el próximo ciclo de WorkManager (podían ser hasta 5h).
            WidgetUpdater.refreshFromCache(getApplication())
        }
    }
}