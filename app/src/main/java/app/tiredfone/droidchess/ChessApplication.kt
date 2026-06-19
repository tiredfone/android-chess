package app.tiredfone.droidchess

import android.app.Application
import app.tiredfone.droidchess.data.database.ChessDatabase
import app.tiredfone.droidchess.engine.StockfishManager

class ChessApplication : Application() {
    val database: ChessDatabase by lazy {
        ChessDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        StockfishManager.init(this)
    }
}
