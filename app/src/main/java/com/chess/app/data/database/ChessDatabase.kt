package com.chess.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.chess.app.data.model.GameMove
import com.chess.app.data.model.GameRecord
import com.chess.app.data.model.GameResult
import com.chess.app.data.model.MoveClassification

class Converters {
    @TypeConverter
    fun fromGameResult(value: GameResult): String = value.name

    @TypeConverter
    fun toGameResult(value: String): GameResult = GameResult.valueOf(value)

    @TypeConverter
    fun fromMoveClassification(value: MoveClassification): String = value.name

    @TypeConverter
    fun toMoveClassification(value: String): MoveClassification = MoveClassification.valueOf(value)
}

@Database(
    entities = [GameRecord::class, GameMove::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ChessDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: ChessDatabase? = null

        fun getDatabase(context: Context): ChessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChessDatabase::class.java,
                    "chess_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
