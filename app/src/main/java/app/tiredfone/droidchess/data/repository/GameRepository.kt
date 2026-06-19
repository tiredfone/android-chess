package app.tiredfone.droidchess.data.repository

import app.tiredfone.droidchess.data.database.GameDao
import app.tiredfone.droidchess.data.model.GameMove
import app.tiredfone.droidchess.data.model.GameRecord
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allGames: Flow<List<GameRecord>> = gameDao.getAllGames()

    suspend fun saveGame(game: GameRecord): Long {
        return gameDao.insertGame(game)
    }

    suspend fun saveMove(move: GameMove) {
        gameDao.insertMove(move)
    }

    suspend fun saveMoves(moves: List<GameMove>) {
        gameDao.insertMoves(moves)
    }

    suspend fun getGame(gameId: Long): GameRecord? {
        return gameDao.getGame(gameId)
    }

    suspend fun getMovesForGame(gameId: Long): List<GameMove> {
        return gameDao.getMovesForGame(gameId)
    }
}
