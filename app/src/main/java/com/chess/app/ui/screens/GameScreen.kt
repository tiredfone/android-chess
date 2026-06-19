package com.chess.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chess.app.data.model.GameResult
import com.chess.app.data.model.BOTS
import com.chess.app.ui.components.BotAvatar
import com.chess.app.ui.components.ChessBoardView
import com.chess.app.ui.components.GameControls
import com.chess.app.ui.components.MoveList
import com.chess.app.ui.theme.ChessGreen
import com.chess.app.ui.theme.DarkBackground
import com.chess.app.ui.theme.DarkSurface
import com.chess.app.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    botId: Int,
    onBack: () -> Unit,
    onGameEnd: (Long) -> Unit
) {
    val viewModel: GameViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()

    // Settings (hardcoded defaults for now; in a real app, read from DataStore)
    val boardTheme = "green"
    val showMoveFeedback = true

    LaunchedEffect(botId) {
        viewModel.startGame(botId)
    }

    val bot = remember(botId) { BOTS.find { it.id == botId } }

    // Game end dialog
    var showEndDialog by remember { mutableStateOf(false) }
    var showResignDialog by remember { mutableStateOf(false) }

    LaunchedEffect(gameState.gameResult) {
        if (gameState.gameResult != GameResult.IN_PROGRESS) {
            showEndDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (bot != null) {
                            BotAvatar(
                                avatarColor = bot.avatarColor,
                                avatarText = bot.avatarText,
                                size = 32.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bot.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${bot.elo} ELO",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
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
                ),
                actions = {
                    if (gameState.isEngineThinking) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            color = ChessGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Status message
            if (gameState.statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (gameState.gameResult) {
                            GameResult.WHITE_WIN -> Color(0xFF1B5E20)
                            GameResult.BLACK_WIN -> Color(0xFF4A148C)
                            GameResult.DRAW -> Color(0xFF1A237E)
                            else -> DarkSurface
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = gameState.statusMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Chess board
            ChessBoardView(
                fen = gameState.fen,
                selectedSquare = gameState.selectedSquare,
                legalMoves = gameState.legalMoves,
                lastMoveFrom = gameState.lastMoveFrom,
                lastMoveTo = gameState.lastMoveTo,
                kingInCheckSquare = gameState.kingInCheckSquare,
                hintMove = gameState.hintMove,
                boardTheme = boardTheme,
                onSquareTapped = { square ->
                    viewModel.selectSquare(square)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Game controls
            GameControls(
                onHint = { viewModel.requestHint() },
                onUndo = { viewModel.undoMove() },
                onResign = { showResignDialog = true },
                isPlayerTurn = gameState.isPlayerTurn,
                isEngineThinking = gameState.isEngineThinking,
                canUndo = gameState.moveHistory.size >= 2
            )

            // Move list
            if (gameState.moveHistory.isNotEmpty()) {
                MoveList(
                    moves = gameState.moveHistory,
                    showClassification = showMoveFeedback,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // Game end dialog
        if (showEndDialog) {
            AlertDialog(
                onDismissRequest = { },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = when (gameState.gameResult) {
                            GameResult.WHITE_WIN -> "White Wins!"
                            GameResult.BLACK_WIN -> "Black Wins!"
                            GameResult.DRAW -> "Draw!"
                            else -> "Game Over"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column {
                        Text(
                            text = gameState.statusMessage,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Show accuracy if available
                        val playerMoves = gameState.moveHistory.filter { it.isPlayerMove }
                        if (playerMoves.isNotEmpty()) {
                            val blunders = playerMoves.count { it.classification == com.chess.app.data.model.MoveClassification.BLUNDER }
                            val mistakes = playerMoves.count { it.classification == com.chess.app.data.model.MoveClassification.MISTAKE }
                            Text(
                                text = "Your moves: ${playerMoves.size} total, $blunders blunders, $mistakes mistakes",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showEndDialog = false
                            val gameId = gameState.savedGameId ?: 0L
                            onGameEnd(gameId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ChessGreen)
                    ) {
                        Text("Review Game")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showEndDialog = false
                            onBack()
                        }
                    ) {
                        Text("Home", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }

        // Resign confirmation dialog
        if (showResignDialog) {
            AlertDialog(
                onDismissRequest = { showResignDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Resign?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to resign this game?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResignDialog = false
                            viewModel.resignGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Resign")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResignDialog = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    }
}
