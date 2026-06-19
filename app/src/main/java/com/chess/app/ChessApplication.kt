package com.chess.app

import android.app.Application
import com.chess.app.data.database.ChessDatabase

class ChessApplication : Application() {
    val database: ChessDatabase by lazy {
        ChessDatabase.getDatabase(this)
    }
}
