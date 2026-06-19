package com.chess.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chess.app.ui.theme.ChessGreen

@Composable
fun GameControls(
    onHint: () -> Unit,
    onUndo: () -> Unit,
    onResign: () -> Unit,
    isPlayerTurn: Boolean,
    isEngineThinking: Boolean,
    canUndo: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlButton(
            icon = Icons.Default.Lightbulb,
            label = "Hint",
            onClick = onHint,
            enabled = isPlayerTurn && !isEngineThinking,
            color = Color(0xFFFFC107),
            modifier = Modifier.weight(1f)
        )

        ControlButton(
            icon = Icons.Default.Undo,
            label = "Undo",
            onClick = onUndo,
            enabled = canUndo && !isEngineThinking,
            color = Color(0xFF2196F3),
            modifier = Modifier.weight(1f)
        )

        ControlButton(
            icon = Icons.Default.Flag,
            label = "Resign",
            onClick = onResign,
            enabled = !isEngineThinking,
            color = Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ControlButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    color: Color = ChessGreen,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color,
            disabledContentColor = Color.Gray
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) color.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
