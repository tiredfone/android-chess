package app.tiredfone.droidchess.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiredfone.droidchess.data.model.GameMove
import app.tiredfone.droidchess.data.model.MoveClassification
import app.tiredfone.droidchess.ui.theme.DarkSurface

@Composable
fun MoveList(
    moves: List<GameMove>,
    showClassification: Boolean = true,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to bottom
    LaunchedEffect(moves.size) {
        if (moves.isNotEmpty()) {
            listState.animateScrollToItem(moves.size - 1)
        }
    }

    // Group into move pairs (white, black)
    val movePairs = mutableListOf<Pair<GameMove, GameMove?>>()
    var i = 0
    while (i < moves.size) {
        val white = moves[i]
        val black = moves.getOrNull(i + 1)
        movePairs.add(Pair(white, black))
        i += 2
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(movePairs) { index, pair ->
            MoveRow(
                moveNumber = index + 1,
                whiteMove = pair.first,
                blackMove = pair.second,
                showClassification = showClassification
            )
        }
    }
}

@Composable
fun MoveRow(
    moveNumber: Int,
    whiteMove: GameMove,
    blackMove: GameMove?,
    showClassification: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Move number
        Text(
            text = "$moveNumber.",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier.width(28.dp)
        )

        // White move
        MoveChip(
            move = whiteMove,
            showClassification = showClassification,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Black move
        if (blackMove != null) {
            MoveChip(
                move = blackMove,
                showClassification = showClassification,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun MoveChip(
    move: GameMove,
    showClassification: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = move.uciMove,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        if (showClassification) {
            val symbol = move.classification.symbol
            if (symbol.isNotEmpty()) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = symbol,
                    color = Color(move.classification.colorHex),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun classificationToEmoji(classification: MoveClassification): String {
    return when (classification) {
        MoveClassification.BRILLIANT -> "!!"
        MoveClassification.BEST -> "!"
        MoveClassification.EXCELLENT -> ""
        MoveClassification.GOOD -> ""
        MoveClassification.INACCURACY -> "?!"
        MoveClassification.MISTAKE -> "?"
        MoveClassification.BLUNDER -> "??"
    }
}
