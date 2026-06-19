package com.chess.app

import android.app.Application
import com.chess.app.data.database.ChessDatabase
import com.chess.app.engine.StockfishManager

class ChessApplication : Application() {
    val database: ChessDatabase by lazy {
        ChessDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        StockfishManager.init(this)
    }
}
