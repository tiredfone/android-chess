package app.tiredfone.droidchess.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameResult {
    WHITE_WIN, BLACK_WIN, DRAW, IN_PROGRESS
}

@Entity(tableName = "games")
data class GameRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: Int,
    val botName: String,
    val botElo: Int,
    val playerColor: String, // "white" or "black"
    val result: GameResult,
    val pgn: String,
    val timestamp: Long = System.currentTimeMillis(),
    val playerAccuracy: Float = 0f,
    val botAccuracy: Float = 0f
)
