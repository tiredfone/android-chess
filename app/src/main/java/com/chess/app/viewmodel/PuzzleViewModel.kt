package com.chess.app.viewmodel

import androidx.lifecycle.ViewModel
import com.chess.app.data.model.Puzzle
import com.chess.app.data.repository.PUZZLES
import com.github.bhlangonijr.chesslib.Board
import com.github.bhlangonijr.chesslib.Piece
import com.github.bhlangonijr.chesslib.PieceType
import com.github.bhlangonijr.chesslib.Side
import com.github.bhlangonijr.chesslib.Square
import com.github.bhlangonijr.chesslib.move.Move
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PuzzleStatus {
    IDLE, PLAYING, SOLVED, FAILED
}

data class PuzzleState(
    val puzzle: Puzzle? = null,
    val fen: String = "",
    val selectedSquare: String? = null,
    val legalMoves: List<String> = emptyList(),
    val lastMoveFrom: String? = null,
    val lastMoveTo: String? = null,
    val status: PuzzleStatus = PuzzleStatus.IDLE,
    val currentSolutionIndex: Int = 0,
    val message: String = "",
    val isInCheck: Boolean = false
)

class PuzzleViewModel : ViewModel() {
    private val board = Board()
    private val _puzzleState = MutableStateFlow(PuzzleState())
    val puzzleState: StateFlow<PuzzleState> = _puzzleState.asStateFlow()

    val allPuzzles: List<Puzzle> = PUZZLES

    fun loadPuzzle(puzzle: Puzzle) {
        board.loadFromFen(puzzle.fen)
        _puzzleState.value = PuzzleState(
            puzzle = puzzle,
            fen = puzzle.fen,
            status = PuzzleStatus.PLAYING,
            message = puzzle.description
        )
    }

    fun selectSquare(squareName: String) {
        val state = _puzzleState.value
        if (state.status != PuzzleStatus.PLAYING) return

        val square = try { Square.valueOf(squareName.uppercase()) } catch (e: Exception) { return }

        if (state.selectedSquare != null) {
            val fromSquare = state.selectedSquare
            val moveUci = "${fromSquare.lowercase()}${squareName.lowercase()}"

            val legalMovesLower = state.legalMoves.map { it.lowercase() }
            if (legalMovesLower.contains(moveUci) || legalMovesLower.any { it.startsWith(moveUci) }) {
                val piece = board.getPiece(Square.valueOf(fromSquare.uppercase()))
                val isPawnPromotion = piece.getPieceType() == PieceType.PAWN &&
                    (squareName.uppercase().endsWith("8") || squareName.uppercase().endsWith("1"))

                if (isPawnPromotion) {
                    submitMove(fromSquare, squareName.uppercase(), "q")
                } else {
                    submitMove(fromSquare, squareName.uppercase(), null)
                }
                return
            }

            val clickedPiece = board.getPiece(square)
            if (clickedPiece != Piece.NONE && clickedPiece.getPieceSide() == board.sideToMove) {
                selectNewSquare(squareName.uppercase())
                return
            }

            _puzzleState.value = state.copy(selectedSquare = null, legalMoves = emptyList())
        } else {
            val piece = board.getPiece(square)
            if (piece != Piece.NONE && piece.getPieceSide() == board.sideToMove) {
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

        _puzzleState.value = _puzzleState.value.copy(
            selectedSquare = squareName,
            legalMoves = legalMoves
        )
    }

    private fun submitMove(from: String, to: String, promotion: String?) {
        val state = _puzzleState.value
        val puzzle = state.puzzle ?: return

        val moveUci = "${from.lowercase()}${to.lowercase()}${promotion ?: ""}"
        val expectedMove = puzzle.solution.getOrNull(state.currentSolutionIndex) ?: return

        val fromSq = try { Square.valueOf(from.uppercase()) } catch (e: Exception) { return }
        val toSq = try { Square.valueOf(to.uppercase()) } catch (e: Exception) { return }
        val promoSide = board.sideToMove
        val promotionPiece = when (promotion?.lowercase()) {
            "q" -> if (promoSide == Side.WHITE) Piece.WHITE_QUEEN else Piece.BLACK_QUEEN
            "r" -> if (promoSide == Side.WHITE) Piece.WHITE_ROOK else Piece.BLACK_ROOK
            "b" -> if (promoSide == Side.WHITE) Piece.WHITE_BISHOP else Piece.BLACK_BISHOP
            "n" -> if (promoSide == Side.WHITE) Piece.WHITE_KNIGHT else Piece.BLACK_KNIGHT
            else -> Piece.NONE
        }

        val move = Move(fromSq, toSq, promotionPiece)
        val legalMoves = board.legalMoves()

        if (!legalMoves.contains(move)) return

        // Check if move matches solution
        val movesMatch = moveUci.startsWith(expectedMove.substring(0, minOf(4, expectedMove.length)))

        if (!movesMatch) {
            _puzzleState.value = state.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                status = PuzzleStatus.FAILED,
                message = "Incorrect! Try again."
            )
            return
        }

        board.doMove(move)
        val newSolutionIndex = state.currentSolutionIndex + 1
        val isInCheck = board.isKingAttacked

        if (newSolutionIndex >= puzzle.solution.size) {
            _puzzleState.value = state.copy(
                fen = board.fen,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMoveFrom = from.uppercase(),
                lastMoveTo = to.uppercase(),
                status = PuzzleStatus.SOLVED,
                currentSolutionIndex = newSolutionIndex,
                message = "Excellent! Puzzle solved!",
                isInCheck = isInCheck
            )
        } else {
            _puzzleState.value = state.copy(
                fen = board.fen,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMoveFrom = from.uppercase(),
                lastMoveTo = to.uppercase(),
                currentSolutionIndex = newSolutionIndex,
                message = "Good! Continue...",
                isInCheck = isInCheck
            )
        }
    }

    fun resetPuzzle() {
        val puzzle = _puzzleState.value.puzzle ?: return
        loadPuzzle(puzzle)
    }
}
