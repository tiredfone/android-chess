package app.tiredfone.droidchess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.tiredfone.droidchess.data.model.Puzzle
import app.tiredfone.droidchess.data.repository.PUZZLES
import app.tiredfone.droidchess.ui.components.ChessBoardView
import app.tiredfone.droidchess.ui.theme.*
import app.tiredfone.droidchess.viewmodel.PuzzleStatus
import app.tiredfone.droidchess.viewmodel.PuzzleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzlesScreen(
    onBack: () -> Unit
) {
    val viewModel: PuzzleViewModel = viewModel()
    val puzzleState by viewModel.puzzleState.collectAsState()
    var showPuzzleList by remember { mutableStateOf(true) }
    var selectedPuzzle by remember { mutableStateOf<Puzzle?>(null) }

    if (showPuzzleList) {
        // Puzzle list screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Chess Puzzles",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Group by theme
                val themes = PUZZLES.map { it.theme }.distinct()
                themes.forEach { theme ->
                    val themePuzzles = PUZZLES.filter { it.theme == theme }
                    item {
                        PuzzleThemeHeader(theme = theme, count = themePuzzles.size)
                    }
                    items(themePuzzles) { puzzle ->
                        PuzzleCard(
                            puzzle = puzzle,
                            onClick = {
                                selectedPuzzle = puzzle
                                viewModel.loadPuzzle(puzzle)
                                showPuzzleList = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        // Puzzle solving screen
        val puzzle = selectedPuzzle ?: return

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = puzzle.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${puzzle.difficulty} • ${puzzle.theme}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showPuzzleList = true }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to puzzles",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.resetPuzzle() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset puzzle",
                                tint = Color.White
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Status message
                val statusColor = when (puzzleState.status) {
                    PuzzleStatus.SOLVED -> Color(0xFF1B5E20)
                    PuzzleStatus.FAILED -> Color(0xFF4E0000)
                    else -> DarkSurface
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = puzzleState.message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                }

                // Chess board
                ChessBoardView(
                    fen = puzzleState.fen.ifEmpty { puzzle.fen },
                    selectedSquare = puzzleState.selectedSquare,
                    legalMoves = puzzleState.legalMoves,
                    lastMoveFrom = puzzleState.lastMoveFrom,
                    lastMoveTo = puzzleState.lastMoveTo,
                    kingInCheckSquare = if (puzzleState.isInCheck) null else null,
                    hintMove = null,
                    onSquareTapped = { square ->
                        viewModel.selectSquare(square)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (puzzleState.status == PuzzleStatus.FAILED) {
                        Button(
                            onClick = { viewModel.resetPuzzle() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Try Again")
                        }
                    }

                    if (puzzleState.status == PuzzleStatus.SOLVED) {
                        Button(
                            onClick = {
                                // Go to next puzzle
                                val currentIndex = PUZZLES.indexOfFirst { it.id == puzzle.id }
                                val nextPuzzle = PUZZLES.getOrNull(currentIndex + 1)
                                if (nextPuzzle != null) {
                                    selectedPuzzle = nextPuzzle
                                    viewModel.loadPuzzle(nextPuzzle)
                                } else {
                                    showPuzzleList = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChessGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next Puzzle →")
                        }
                    }
                }

                // Progress indicator
                if (puzzleState.status == PuzzleStatus.PLAYING && puzzle.solution.size > 1) {
                    Text(
                        text = "Move ${puzzleState.currentSolutionIndex + 1} of ${puzzle.solution.size}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun PuzzleThemeHeader(theme: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = theme,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$count puzzles",
            color = ChessGreen,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleCard(
    puzzle: Puzzle,
    onClick: () -> Unit
) {
    val difficultyColor = when (puzzle.difficulty) {
        "Beginner" -> Color(0xFF4CAF50)
        "Intermediate" -> Color(0xFFFF9800)
        "Advanced" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Puzzle number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(difficultyColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${puzzle.id}",
                    color = difficultyColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = puzzle.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = puzzle.description,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(difficultyColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = puzzle.difficulty,
                    color = difficultyColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
