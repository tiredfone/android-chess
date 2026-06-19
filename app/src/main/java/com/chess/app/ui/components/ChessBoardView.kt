package com.chess.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.chess.app.ui.theme.*

// Chess piece Unicode map
val PIECE_UNICODE = mapOf(
    'K' to "♔", 'Q' to "♕", 'R' to "♖", 'B' to "♗", 'N' to "♘", 'P' to "♙",
    'k' to "♚", 'q' to "♛", 'r' to "♜", 'b' to "♝", 'n' to "♞", 'p' to "♟"
)

data class BoardThemeColors(
    val lightSquare: Color,
    val darkSquare: Color
)

fun getBoardThemeColors(theme: String): BoardThemeColors {
    return when (theme) {
        "blue" -> BoardThemeColors(BoardLightBlue, BoardDarkBlue)
        "brown" -> BoardThemeColors(BoardLightBrown, BoardDarkBrown)
        else -> BoardThemeColors(BoardLightGreen, BoardDarkGreen) // green is default
    }
}

// Parse FEN board part into a 8x8 array (rank 8 to rank 1, file a to h)
fun parseFen(fen: String): Array<CharArray> {
    val board = Array(8) { CharArray(8) { '.' } }
    val boardPart = fen.split(" ")[0]
    val ranks = boardPart.split("/")

    for (rankIdx in 0..7) {
        var fileIdx = 0
        val rank = ranks.getOrNull(rankIdx) ?: continue
        for (ch in rank) {
            if (ch.isDigit()) {
                fileIdx += ch.digitToInt()
            } else {
                if (fileIdx < 8) {
                    board[rankIdx][fileIdx] = ch
                    fileIdx++
                }
            }
        }
    }
    return board
}

// Convert square name (e.g. "e4") to board array indices (rankIdx, fileIdx)
// rankIdx: 0 = rank 8 (top), 7 = rank 1 (bottom)
// fileIdx: 0 = file a, 7 = file h
fun squareToIndices(square: String): Pair<Int, Int>? {
    if (square.length < 2) return null
    val file = square[0].lowercaseChar() - 'a'
    val rank = square[1].digitToInt() - 1
    if (file !in 0..7 || rank !in 0..7) return null
    val rankIdx = 7 - rank
    return Pair(rankIdx, file)
}

fun indicesToSquare(rankIdx: Int, fileIdx: Int): String {
    val file = ('a' + fileIdx).toString()
    val rank = (8 - rankIdx).toString()
    return "$file$rank"
}

@Composable
fun ChessBoardView(
    fen: String,
    selectedSquare: String?,
    legalMoves: List<String>,
    lastMoveFrom: String?,
    lastMoveTo: String?,
    kingInCheckSquare: String?,
    hintMove: String?,
    boardTheme: String = "green",
    isFlipped: Boolean = false,
    onSquareTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = getBoardThemeColors(boardTheme)
    val board = remember(fen) { parseFen(fen) }

    val density = LocalDensity.current

    val legalMoveDests = remember(legalMoves) {
        legalMoves.mapNotNull { move ->
            if (move.length >= 4) move.substring(2, 4) else null
        }.toSet()
    }

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val boardSize = maxWidth

        Canvas(
            modifier = Modifier
                .size(boardSize)
                .pointerInput(isFlipped) {
                    detectTapGestures { offset ->
                        val sqPx = size.width / 8f
                        val col = (offset.x / sqPx).toInt().coerceIn(0, 7)
                        val row = (offset.y / sqPx).toInt().coerceIn(0, 7)
                        val fileIdx = if (isFlipped) 7 - col else col
                        val rankIdx = if (isFlipped) 7 - row else row
                        val squareName = indicesToSquare(rankIdx, fileIdx)
                        onSquareTapped(squareName.uppercase())
                    }
                }
        ) {
            val sqSize = size.width / 8f

            // Draw squares
            for (row in 0..7) {
                for (col in 0..7) {
                    val displayRank = if (isFlipped) 7 - row else row
                    val displayFile = if (isFlipped) 7 - col else col
                    val squareName = indicesToSquare(displayRank, displayFile).uppercase()

                    val isLight = (row + col) % 2 == 0
                    val baseColor = if (isLight) themeColors.lightSquare else themeColors.darkSquare

                    val squareColor = when {
                        squareName == kingInCheckSquare -> Color(0xFFCC0000).copy(alpha = 0.85f)
                        squareName == selectedSquare -> if (isLight)
                            themeColors.lightSquare.copy(green = themeColors.lightSquare.green * 0.8f, blue = 0.2f)
                        else
                            themeColors.darkSquare.copy(green = themeColors.darkSquare.green * 0.8f, blue = 0.2f)
                        squareName == lastMoveFrom || squareName == lastMoveTo ->
                            if (isLight) Color(0xFFF6F669) else Color(0xFFBBB134)
                        squareName == hintMove?.substring(0, 2)?.uppercase() ||
                        squareName == hintMove?.substring(2, 4)?.uppercase() ->
                            if (isLight) Color(0xFFB0E2FF) else Color(0xFF6DB4D7)
                        else -> baseColor
                    }

                    drawRect(
                        color = squareColor,
                        topLeft = Offset(col * sqSize, row * sqSize),
                        size = Size(sqSize, sqSize)
                    )

                    // Legal move dots
                    if (squareName in legalMoveDests) {
                        val centerX = col * sqSize + sqSize / 2
                        val centerY = row * sqSize + sqSize / 2
                        val piece = board[displayRank][displayFile]

                        if (piece != '.') {
                            // Draw ring for captures
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.2f),
                                radius = sqSize * 0.46f,
                                center = Offset(centerX, centerY),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = sqSize * 0.08f
                                )
                            )
                        } else {
                            // Draw dot for regular moves
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.2f),
                                radius = sqSize * 0.15f,
                                center = Offset(centerX, centerY)
                            )
                        }
                    }

                    // Draw file letters (a-h) on bottom rank
                    if (row == 7) {
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = if (isLight) android.graphics.Color.parseColor("#769656")
                                        else android.graphics.Color.parseColor("#EEEED2")
                                textSize = sqSize * 0.22f
                                isAntiAlias = true
                            }
                            val letter = ('a' + (if (isFlipped) 7 - col else col)).toString()
                            canvas.nativeCanvas.drawText(
                                letter,
                                col * sqSize + sqSize * 0.05f,
                                row * sqSize + sqSize * 0.95f,
                                paint
                            )
                        }
                    }

                    // Draw rank numbers (1-8) on leftmost file
                    if (col == 0) {
                        drawIntoCanvas { canvas ->
                            val paint = android.graphics.Paint().apply {
                                color = if (isLight) android.graphics.Color.parseColor("#769656")
                                        else android.graphics.Color.parseColor("#EEEED2")
                                textSize = sqSize * 0.22f
                                isAntiAlias = true
                            }
                            val number = if (isFlipped) (row + 1).toString() else (8 - row).toString()
                            canvas.nativeCanvas.drawText(
                                number,
                                col * sqSize + sqSize * 0.05f,
                                row * sqSize + sqSize * 0.3f,
                                paint
                            )
                        }
                    }
                }
            }

            // Draw pieces
            for (row in 0..7) {
                for (col in 0..7) {
                    val displayRank = if (isFlipped) 7 - row else row
                    val displayFile = if (isFlipped) 7 - col else col
                    val piece = board[displayRank][displayFile]

                    if (piece != '.') {
                        val pieceChar = PIECE_UNICODE[piece] ?: continue
                        val centerX = col * sqSize + sqSize / 2
                        val centerY = row * sqSize + sqSize / 2

                        drawIntoCanvas { canvas ->
                            // Shadow
                            val shadowPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                alpha = 60
                                textSize = sqSize * 0.78f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            canvas.nativeCanvas.drawText(
                                pieceChar,
                                centerX + sqSize * 0.025f,
                                centerY + sqSize * 0.36f + sqSize * 0.025f,
                                shadowPaint
                            )

                            // Piece
                            val piecePaint = android.graphics.Paint().apply {
                                textSize = sqSize * 0.78f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                // White pieces: white fill; Black pieces: near-black fill
                                color = if (piece.isUpperCase()) {
                                    android.graphics.Color.WHITE
                                } else {
                                    android.graphics.Color.parseColor("#1a1a1a")
                                }
                            }
                            canvas.nativeCanvas.drawText(
                                pieceChar,
                                centerX,
                                centerY + sqSize * 0.36f,
                                piecePaint
                            )

                            // Outline for white pieces to make them visible on light squares
                            if (piece.isUpperCase()) {
                                val outlinePaint = android.graphics.Paint().apply {
                                    textSize = sqSize * 0.78f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isAntiAlias = true
                                    color = android.graphics.Color.parseColor("#555555")
                                    style = android.graphics.Paint.Style.STROKE
                                    strokeWidth = sqSize * 0.02f
                                }
                                canvas.nativeCanvas.drawText(
                                    pieceChar,
                                    centerX,
                                    centerY + sqSize * 0.36f,
                                    outlinePaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
