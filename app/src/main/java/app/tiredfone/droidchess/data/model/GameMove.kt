package app.tiredfone.droidchess.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MoveClassification(
    val label: String,
    val colorHex: Long,
    val symbol: String
) {
    BRILLIANT("Brilliant", 0xFF1BACA6, "!!"),
    BEST("Best", 0xFF96BC4B, "!"),
    EXCELLENT("Excellent", 0xFF96BC4B, ""),
    GOOD("Good", 0xFF96BC4B, ""),
    INACCURACY("Inaccuracy", 0xFFF6C700, "?!"),
    MISTAKE("Mistake", 0xFFE58C1A, "?"),
    BLUNDER("Blunder", 0xFFCA3431, "??")
}

@Entity(tableName = "game_moves")
data class GameMove(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val moveNumber: Int,
    val uciMove: String,
    val sanMove: String,
    val fenBefore: String,
    val fenAfter: String,
    val evalBefore: Int,
    val evalAfter: Int,
    val classification: MoveClassification,
    val isPlayerMove: Boolean
)
