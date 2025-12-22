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
            initialValue = "#FF000000" // Default black
        )

    // Function to save the selected color
    fun saveWidgetTextColor(color: String) {
        viewModelScope.launch {
            settingsDataStore.saveWidgetTextColor(color)
        }
    }
}