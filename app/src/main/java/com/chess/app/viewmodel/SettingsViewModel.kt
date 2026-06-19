package com.chess.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chess.app.engine.StockfishManager
import com.chess.app.engine.StockfishStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    val stockfishStatus: StateFlow<StockfishStatus> = StockfishManager.status

    fun getChannel(): String = StockfishManager.getChannel()

    fun setChannel(channel: String) {
        StockfishManager.setChannel(getApplication(), channel)
        // Channel switch triggers a fresh download
        StockfishManager.downloadLatest(getApplication(), forceChannel = channel)
    }

    fun checkForUpdate() {
        viewModelScope.launch {
            StockfishManager.checkForUpdate(getApplication(), forced = true)
        }
    }

    fun downloadUpdate() {
        StockfishManager.downloadLatest(getApplication())
    }

    fun getInstalledVersion(): String? = StockfishManager.getInstalledVersion()
}
