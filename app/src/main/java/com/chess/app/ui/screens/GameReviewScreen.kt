package com.chess.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chess.app.data.model.GameMove
import com.chess.app.data.model.MoveClassification
import com.chess.app.ui.components.ChessBoardView
import com.chess.app.ui.theme.*

@Composable
fun GameReviewScreen(
    gameId: Long,
    onBack: () -> Unit
) {
    // Simple local state for review - load moves from provided game
    var moves by remember { mutableStateOf<List<GameMove>>(emptyList()) }
    var currentMoveIndex by remember { mutableIntStateOf(-1) }
    var currentFen by remember { mutableStateOf("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1") }

    val initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    // Update FEN based on current move index
    LaunchedEffect(currentMoveIndex, moves) {
        currentFen = when {
            currentMoveIndex < 0 -> initialFen
            currentMoveIndex < moves.size -> moves[currentMoveIndex].fenAfter
            else -> moves.lastOrNull()?.fenAfter ?: initialFen
        }
    }

    val currentMove = moves.getOrNull(currentMoveIndex)
    val playerMoves = moves.filter { it.isPlayerMove }
    val botMoves = moves.filter { !it.isPlayerMove }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Game Review",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
        ) {
            // Accuracy cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccuracyCard(
                    label = "Your Accuracy",
                    moves = playerMoves,
                    modifier = Modifier.weight(1f)
                )
                AccuracyCard(
                    label = "Bot Accuracy",
                    moves = botMoves,
                    modifier = Modifier.weight(1f)
                )
            }

            // Chess board
            ChessBoardView(
                fen = currentFen,
                selectedSquare = null,
                legalMoves = emptyList(),
                lastMoveFrom = currentMove?.uciMove?.substring(0, 2)?.uppercase(),
                lastMoveTo = currentMove?.uciMove?.substring(2, 4)?.uppercase(),
                kingInCheckSquare = null,
                hintMove = null,
                onSquareTapped = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Navigation controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMoveIndex = -1 },
                    enabled = currentMoveIndex >= 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "First move",
                        tint = if (currentMoveIndex >= 0) Color.White else Color.Gray
                    )
                }

                IconButton(
                    onClick = { if (currentMoveIndex > -1) currentMoveIndex-- },
                    enabled = currentMoveIndex >= 0
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Previous move",
                        tint = if (currentMoveIndex >= 0) Color.White else Color.Gray
                    )
                }

                // Current move label
                Text(
                    text = if (currentMoveIndex < 0) "Start" else "Move ${currentMoveIndex + 1}/${moves.size}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = { if (currentMoveIndex < moves.size - 1) currentMoveIndex++ },
                    enabled = currentMoveIndex < moves.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next move",
                        tint = if (currentMoveIndex < moves.size - 1) Color.White else Color.Gray
                    )
                }

                IconButton(
                    onClick = { currentMoveIndex = moves.size - 1 },
                    enabled = currentMoveIndex < moves.size - 1
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Last move",
                        tint = if (currentMoveIndex < moves.size - 1) Color.White else Color.Gray
                    )
                }
            }

            // Move classification summary
            if (moves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No moves recorded for this game.\n\nPlay a complete game to see the review!",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                // Move list with classifications
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(moves) { index, move ->
                        ReviewMoveRow(
                            move = move,
                            moveIndex = index,
                            isSelected = index == currentMoveIndex,
                            onClick = { currentMoveIndex = index }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewMoveRow(
    move: GameMove,
    moveIndex: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val classification = move.classification
    val classColor = Color(classification.colorHex)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurface.copy(red = 0.15f) else Color.Transparent
        ),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Move number
            Text(
                text = "${moveIndex / 2 + 1}${if (moveIndex % 2 == 0) "." else "..."}",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                modifier = Modifier.width(40.dp)
            )

            // Player indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (move.isPlayerMove) Color.White else Color(0xFF333333))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Move
            Text(
                text = move.uciMove,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            // Classification badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(classColor.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = classification.label + if (classification.symbol.isNotEmpty()) " ${classification.symbol}" else "",
                    color = classColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AccuracyCard(
    label: String,
    moves: List<GameMove>,
    modifier: Modifier = Modifier
) {
    val classificationCounts = moves.groupBy { it.classification }
    val brilliant = classificationCounts[MoveClassification.BRILLIANT]?.size ?: 0
    val best = classificationCounts[MoveClassification.BEST]?.size ?: 0
    val blunders = classificationCounts[MoveClassification.BLUNDER]?.size ?: 0
    val mistakes = classificationCounts[MoveClassification.MISTAKE]?.size ?: 0

    // Simple accuracy calculation
    val accuracy = if (moves.isEmpty()) 100f else {
        val score = moves.sumOf {
            when (it.classification) {
                MoveClassification.BRILLIANT, MoveClassification.BEST -> 100
                MoveClassification.EXCELLENT -> 90
                MoveClassification.GOOD -> 75
                MoveClassification.INACCURACY -> 50
                MoveClassification.MISTAKE -> 25
                MoveClassification.BLUNDER -> 0
            }
        }
        score.toFloat() / moves.size
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
            Text(
                text = "${accuracy.toInt()}%",
                color = when {
                    accuracy >= 85 -> BestColor
                    accuracy >= 60 -> InaccuracyColor
                    else -> BlunderColor
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (brilliant > 0) MiniStat("!!", brilliant, BrilliantColor)
                if (blunders > 0) MiniStat("??", blunders, BlunderColor)
                if (mistakes > 0) MiniStat("?", mistakes, MistakeColor)
            }
            Text(
                text = "${moves.size} moves",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MiniStat(symbol: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = symbol, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(text = "$count", color = color.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}
