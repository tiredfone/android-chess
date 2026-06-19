package app.tiredfone.droidchess.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.tiredfone.droidchess.ChessApplication
import app.tiredfone.droidchess.data.model.GameMove
import app.tiredfone.droidchess.data.model.GameRecord
import app.tiredfone.droidchess.data.model.GameResult
import app.tiredfone.droidchess.data.model.BOTS
import app.tiredfone.droidchess.data.repository.GameRepository
import app.tiredfone.droidchess.engine.MoveEvaluator
import app.tiredfone.droidchess.engine.StockfishManager
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameState(
    val fen: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    val selectedSquare: String? = null,
    val legalMoves: List<String> = emptyList(),
    val lastMoveFrom: String? = null,
    val lastMoveTo: String? = null,
    val isPlayerTurn: Boolean = true,
    val gameResult: GameResult = GameResult.IN_PROGRESS,
    val moveHistory: List<GameMove> = emptyList(),
    val isInCheck: Boolean = false,
    val kingInCheckSquare: String? = null,
    val hintMove: String? = null,
    val isEngineThinking: Boolean = false,
    val savedGameId: Long? = null,
    val statusMessage: String = ""
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    // Engine is managed globally by StockfishManager; we obtain it when needed.
    private val repository: GameRepository
    private val board = Board()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var currentBot: app.tiredfone.droidchess.data.model.Bot? = null
    private var playerSide = Side.WHITE
    private var moveNumber = 0
    private val pendingMoves = mutableListOf<GameMove>()

    /** Returns the engine if it is currently ready, or null for random-move fallback. */
    private val engine get() = StockfishManager.engine

    init {
        val db = (application as ChessApplication).database
        repository = GameRepository(db.gameDao())
        // StockfishManager.init() was already called in ChessApplication.onCreate().
        // No additional init needed here.
    }

    fun startGame(botId: Int) {
        val bot = BOTS.find { it.id == botId } ?: BOTS.first()
        currentBot = bot
        playerSide = Side.WHITE // Player always plays white
        board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        moveNumber = 0
        pendingMoves.clear()

        engine?.setElo(bot.elo)

        _gameState.value = GameState(
            fen = board.fen,
            isPlayerTurn = true,
            statusMessage = "Game started vs ${bot.name} (${bot.elo} ELO)"
        )
    }

    fun selectSquare(squareName: String) {
        val state = _gameState.value
        if (!state.isPlayerTurn || state.gameResult != GameResult.IN_PROGRESS) return
        if (state.isEngineThinking) return

        val square = try { Square.valueOf(squareName.uppercase()) } catch (e: Exception) { return }

        if (state.selectedSquare != null) {
            val fromSquare = state.selectedSquare

            // Check if this is a legal move destination
            val moveUci = "${fromSquare.lowercase()}${squareName.lowercase()}"
            val legalMovesLower = state.legalMoves.map { it.lowercase() }

            if (legalMovesLower.contains(moveUci) || legalMovesLower.any { it.startsWith(moveUci) }) {
                val piece = board.getPiece(Square.valueOf(fromSquare.uppercase()))
                val isPawnPromotion = piece.getPieceType() == PieceType.PAWN &&
                    (squareName.uppercase().endsWith("8") || squareName.uppercase().endsWith("1"))

                if (isPawnPromotion) {
                    makeMove(fromSquare, squareName.uppercase(), "q")
                } else {
                    makeMove(fromSquare, squareName.uppercase(), null)
                }
                return
            }

            // If clicking own piece, select it instead
            val clickedPiece = board.getPiece(square)
            if (clickedPiece != Piece.NONE && clickedPiece.getPieceSide() == playerSide) {
                selectNewSquare(squareName.uppercase())
                return
            }

            // Deselect
            _gameState.value = state.copy(selectedSquare = null, legalMoves = emptyList(), hintMove = null)
        } else {
            val piece = board.getPiece(square)
            if (piece != Piece.NONE && piece.getPieceSide() == playerSide) {
                selectNewSquare(squareName.uppercase())
            }
        }
    }

    private fun selectNewSquare(squareName: String) {
        val square = try { Square.valueOf(squareName) } catch (e: Exception) { return }
        val legalMoves = board.legalMoves()
            .filter { it.from == square }
            .map {
                val promoStr = if (it.promotion != Piece.NONE) it.promotion.getFenSymbol().lowercase() else ""
                "${it.from.name.lowercase()}${it.to.name.lowercase()}$promoStr"
            }

        _gameState.value = _gameState.value.copy(
            selectedSquare = squareName,
            legalMoves = legalMoves,
            hintMove = null
        )
    }

    fun makeMove(from: String, to: String, promotion: String?) {
        viewModelScope.launch {
            val state = _gameState.value
            if (!state.isPlayerTurn || state.gameResult != GameResult.IN_PROGRESS) return@launch

            val fromSq = try { Square.valueOf(from.uppercase()) } catch (e: Exception) { return@launch }
            val toSq = try { Square.valueOf(to.uppercase()) } catch (e: Exception) { return@launch }

            val promotionPiece = when (promotion?.lowercase()) {
                "q" -> if (playerSide == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
                "r" -> if (playerSide == Side.WHITE) Piece.WHITE_ROOK else Piece.BLACK_ROOK
                "b" -> if (playerSide == Side.WHITE) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
                "n" -> if (playerSide == Side.WHITE) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
                else -> Piece.NONE
            }

            val move = Move(fromSq, toSq, promotionPiece)
            val fenBefore = board.fen
            val currentEngine = engine
            val evalBefore = withContext(Dispatchers.IO) {
                currentEngine?.evaluatePosition(fenBefore, 100) ?: 0
            }

            val legalMoves = board.legalMoves()
            if (!legalMoves.contains(move)) {
                Log.w("GameViewModel", "Illegal move attempted: $move")
                return@launch
            }

            board.doMove(move)
            val fenAfter = board.fen
            val evalAfter = withContext(Dispatchers.IO) {
                currentEngine?.evaluatePosition(fenAfter, 100) ?: 0
            }

            val classification = MoveEvaluator.classifyMove(evalBefore, evalAfter, playerSide == Side.WHITE)
            moveNumber++

            val uciStr = "${from.lowercase()}${to.lowercase()}${promotion ?: ""}"
            val gameMove = GameMove(
                gameId = 0,
                moveNumber = moveNumber,
                uciMove = uciStr,
                sanMove = uciStr,
                fenBefore = fenBefore,
                fenAfter = fenAfter,
                evalBefore = evalBefore,
                evalAfter = evalAfter,
                classification = classification,
                isPlayerMove = true
            )
            pendingMoves.add(gameMove)

            val isInCheck = board.isKingAttacked
            val kingSquare = if (isInCheck) findKingSquare(board.sideToMove) else null
            val gameResult = checkGameResult()

            _gameState.value = state.copy(
                fen = board.fen,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMoveFrom = from.uppercase(),
                lastMoveTo = to.uppercase(),
                isPlayerTurn = gameResult == GameResult.IN_PROGRESS,
                gameResult = gameResult,
                moveHistory = pendingMoves.toList(),
                isInCheck = isInCheck,
                kingInCheckSquare = kingSquare,
                hintMove = null,
                statusMessage = if (gameResult != GameResult.IN_PROGRESS) getResultMessage(gameResult) else ""
            )

            if (gameResult == GameResult.IN_PROGRESS) {
                makeBotMove()
            } else {
                saveGame(gameResult)
            }
        }
    }

    private suspend fun makeBotMove() {
        _gameState.value = _gameState.value.copy(isEngineThinking = true)

        val currentFen = board.fen
        val bot = currentBot ?: return

        delay(300) // Small delay for UX

        val thinkTime = MoveEvaluator.thinkTimeForElo(bot.elo)
        val currentEngine = engine
        val botMoveUci = withContext(Dispatchers.IO) {
            if (currentEngine != null) {
                currentEngine.getMove(currentFen, thinkTime)
            } else {
                // Engine not ready (still downloading) — fall back to random legal move
                val moves = board.legalMoves()
                if (moves.isNotEmpty()) {
                    val randomMove = moves.random()
                    "${randomMove.from.name.lowercase()}${randomMove.to.name.lowercase()}"
                } else null
            }
        }

        if (botMoveUci == null) {
            _gameState.value = _gameState.value.copy(isEngineThinking = false)
            return
        }

        val fromStr = botMoveUci.substring(0, 2).uppercase()
        val toStr = botMoveUci.substring(2, 4).uppercase()
        val promoStr = if (botMoveUci.length > 4) botMoveUci[4].toString() else null

        val fromSq = try { Square.valueOf(fromStr) } catch (e: Exception) {
            _gameState.value = _gameState.value.copy(isEngineThinking = false)
            return
        }
        val toSq = try { Square.valueOf(toStr) } catch (e: Exception) {
            _gameState.value = _gameState.value.copy(isEngineThinking = false)
            return
        }

        val botSide = if (playerSide == Side.WHITE) Side.BLACK else Side.WHITE
        val promotionPiece = when (promoStr?.lowercase()) {
            "q" -> if (botSide == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            "r" -> if (botSide == Side.WHITE) Piece.WHITE_ROOK else Piece.BLACK_ROOK
            "b" -> if (botSide == Side.WHITE) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
            "n" -> if (botSide == Side.WHITE) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
            else -> Piece.NONE
        }

        val botMove = Move(fromSq, toSq, promotionPiece)
        val fenBefore = board.fen
        val evalBefore = withContext(Dispatchers.IO) {
            currentEngine?.evaluatePosition(fenBefore, 100) ?: 0
        }

        val legalMoves = board.legalMoves()
        if (!legalMoves.contains(botMove)) {
            Log.w("GameViewModel", "Bot attempted illegal move: $botMove")
            _gameState.value = _gameState.value.copy(isEngineThinking = false)
            return
        }

        board.doMove(botMove)
        val fenAfter = board.fen
        val evalAfter = withContext(Dispatchers.IO) {
            currentEngine?.evaluatePosition(fenAfter, 100) ?: 0
        }

        val classification = MoveEvaluator.classifyMove(evalBefore, evalAfter, botSide == Side.WHITE)
        moveNumber++

        val botGameMove = GameMove(
            gameId = 0,
            moveNumber = moveNumber,
            uciMove = botMoveUci,
            sanMove = botMoveUci,
            fenBefore = fenBefore,
            fenAfter = fenAfter,
            evalBefore = evalBefore,
            evalAfter = evalAfter,
            classification = classification,
            isPlayerMove = false
        )
        pendingMoves.add(botGameMove)

        val isInCheck = board.isKingAttacked
        val kingSquare = if (isInCheck) findKingSquare(board.sideToMove) else null
        val gameResult = checkGameResult()

        _gameState.value = _gameState.value.copy(
            fen = board.fen,
            lastMoveFrom = fromStr,
            lastMoveTo = toStr,
            isPlayerTurn = gameResult == GameResult.IN_PROGRESS,
            gameResult = gameResult,
            moveHistory = pendingMoves.toList(),
            isInCheck = isInCheck,
            kingInCheckSquare = kingSquare,
            isEngineThinking = false,
            statusMessage = if (gameResult != GameResult.IN_PROGRESS) getResultMessage(gameResult) else ""
        )

        if (gameResult != GameResult.IN_PROGRESS) {
            saveGame(gameResult)
        }
    }

    fun undoMove() {
        val state = _gameState.value
        if (state.gameResult != GameResult.IN_PROGRESS) return
        if (pendingMoves.size < 2) return

        board.undoMove()
        board.undoMove()

        if (pendingMoves.size >= 2) {
            pendingMoves.removeAt(pendingMoves.size - 1)
            pendingMoves.removeAt(pendingMoves.size - 1)
        }
        moveNumber = maxOf(0, moveNumber - 2)

        val isInCheck = board.isKingAttacked
        val kingSquare = if (isInCheck) findKingSquare(board.sideToMove) else null

        _gameState.value = state.copy(
            fen = board.fen,
            selectedSquare = null,
            legalMoves = emptyList(),
            lastMoveFrom = pendingMoves.lastOrNull()?.uciMove?.substring(0, 2)?.uppercase(),
            lastMoveTo = pendingMoves.lastOrNull()?.uciMove?.substring(2, 4)?.uppercase(),
            isPlayerTurn = true,
            moveHistory = pendingMoves.toList(),
            isInCheck = isInCheck,
            kingInCheckSquare = kingSquare,
            hintMove = null,
            statusMessage = ""
        )
    }

    fun requestHint() {
        viewModelScope.launch {
            val state = _gameState.value
            if (!state.isPlayerTurn || state.gameResult != GameResult.IN_PROGRESS) return@launch

            _gameState.value = state.copy(isEngineThinking = true)

            val currentEngine = engine
            if (currentEngine == null) {
                // Engine not yet available — tell the user instead of showing a random move
                _gameState.value = _gameState.value.copy(
                    isEngineThinking = false,
                    statusMessage = "Hint unavailable — Stockfish is still downloading"
                )
                return@launch
            }

            val hintMove = withContext(Dispatchers.IO) {
                // Always use full engine strength for hints regardless of bot ELO
                currentEngine.getBestMoveAndEval(board.fen, thinkTimeMs = 800).first
            }

            _gameState.value = _gameState.value.copy(
                hintMove = hintMove,
                isEngineThinking = false,
                statusMessage = if (hintMove != null) "Hint: try ${hintMove.take(2)}→${hintMove.drop(2).take(2)}" else ""
            )
        }
    }

    fun resignGame() {
        val state = _gameState.value
        if (state.gameResult != GameResult.IN_PROGRESS) return

        val result = if (playerSide == Side.WHITE) GameResult.BLACK_WIN else GameResult.WHITE_WIN
        _gameState.value = state.copy(
            gameResult = result,
            isPlayerTurn = false,
            statusMessage = "You resigned."
        )
        viewModelScope.launch { saveGame(result) }
    }

    private fun checkGameResult(): GameResult {
        if (board.isMated) {
            return if (board.sideToMove == playerSide) {
                if (playerSide == Side.WHITE) GameResult.BLACK_WIN else GameResult.WHITE_WIN
            } else {
                if (playerSide == Side.WHITE) GameResult.WHITE_WIN else GameResult.BLACK_WIN
            }
        }
        if (board.isDraw || board.isStaleMate || board.isInsufficientMaterial ||
            board.isRepetition || (board.halfMoveCounter ?: 0) >= 100) {
            return GameResult.DRAW
        }
        return GameResult.IN_PROGRESS
    }

    private fun getResultMessage(result: GameResult): String {
        return when (result) {
            GameResult.WHITE_WIN -> if (playerSide == Side.WHITE) "You won!" else "${currentBot?.name} won!"
            GameResult.BLACK_WIN -> if (playerSide == Side.BLACK) "You won!" else "${currentBot?.name} won!"
            GameResult.DRAW -> "Draw!"
            GameResult.IN_PROGRESS -> ""
        }
    }

    private fun findKingSquare(side: Side): String? {
        for (sq in Square.values()) {
            if (sq == Square.NONE) continue
            val piece = board.getPiece(sq)
            if (piece != Piece.NONE && piece.getPieceType() == PieceType.KING && piece.getPieceSide() == side) {
                return sq.name
            }
        }
        return null
    }

    private suspend fun saveGame(result: GameResult) {
        val bot = currentBot ?: return
        try {
            val playerMoves = pendingMoves.filter { it.isPlayerMove }
            val botMoves = pendingMoves.filter { !it.isPlayerMove }

            val playerAccuracy = MoveEvaluator.calculateAccuracy(playerMoves.map { it.classification })
            val botAccuracy = MoveEvaluator.calculateAccuracy(botMoves.map { it.classification })

            val gameRecord = GameRecord(
                botId = bot.id,
                botName = bot.name,
                botElo = bot.elo,
                playerColor = playerSide.name.lowercase(),
                result = result,
                pgn = board.toString(),
                playerAccuracy = playerAccuracy,
                botAccuracy = botAccuracy
            )

            val gameId = repository.saveGame(gameRecord)
            val movesWithGameId = pendingMoves.map { it.copy(gameId = gameId) }
            repository.saveMoves(movesWithGameId)

            _gameState.value = _gameState.value.copy(savedGameId = gameId)
        } catch (e: Exception) {
            Log.e("GameViewModel", "Error saving game", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Engine lifecycle is managed by StockfishManager — do not destroy it here.
    }
}
