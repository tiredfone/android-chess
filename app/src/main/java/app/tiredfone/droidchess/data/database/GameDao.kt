package app.tiredfone.droidchess.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.tiredfone.droidchess.data.model.GameMove
import app.tiredfone.droidchess.data.model.GameRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMove(move: GameMove)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoves(moves: List<GameMove>)

    @Query("SELECT * FROM games ORDER BY timestamp DESC")
    fun getAllGames(): Flow<List<GameRecord>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGame(gameId: Long): GameRecord?

    @Query("SELECT * FROM game_moves WHERE gameId = :gameId ORDER BY moveNumber ASC")
    suspend fun getMovesForGame(gameId: Long): List<GameMove>

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)
}
