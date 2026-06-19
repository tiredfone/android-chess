package app.tiredfone.droidchess.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.tiredfone.droidchess.data.model.PieceTheme
import app.tiredfone.droidchess.engine.StockfishManager
import app.tiredfone.droidchess.engine.StockfishStatus
import app.tiredfone.droidchess.ui.components.BoardBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("chess_settings", Context.MODE_PRIVATE)

    val stockfishStatus: StateFlow<StockfishStatus> = StockfishManager.status

    private val _pieceTheme = MutableStateFlow(
        try {
            PieceTheme.valueOf(prefs.getString("piece_theme", PieceTheme.CLASSIC.name) ?: PieceTheme.CLASSIC.name)
        } catch (e: Exception) { PieceTheme.CLASSIC }
    )
    val pieceTheme: StateFlow<PieceTheme> = _pieceTheme.asStateFlow()

    private val _boardBackground = MutableStateFlow(
        try {
            BoardBackground.valueOf(prefs.getString("board_background", BoardBackground.CLASSIC.name) ?: BoardBackground.CLASSIC.name)
        } catch (e: Exception) { BoardBackground.CLASSIC }
    )
    val boardBackground: StateFlow<BoardBackground> = _boardBackground.asStateFlow()

    fun setPieceTheme(theme: PieceTheme) {
        _pieceTheme.value = theme
        prefs.edit().putString("piece_theme", theme.name).apply()
    }

    fun setBoardBackground(bg: BoardBackground) {
        _boardBackground.value = bg
        prefs.edit().putString("board_background", bg.name).apply()
    }

    fun getChannel(): String = StockfishManager.getChannel()

    fun setChannel(channel: String) {
        StockfishManager.setChannel(getApplication(), channel)
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

    fun installCustomBinary(context: Context, uri: android.net.Uri) {
        StockfishManager.installCustomBinary(context, uri)
    }
}
