package com.chess.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chess.app.engine.StockfishManager
import com.chess.app.engine.StockfishStatus
import com.chess.app.ui.theme.ChessGreen
import com.chess.app.ui.theme.DarkBackground
import com.chess.app.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPlayBot: () -> Unit,
    onPuzzles: () -> Unit,
    onLessons: () -> Unit,
    onSettings: () -> Unit
) {
    val sfStatus by StockfishManager.status.collectAsState()
    val context = LocalContext.current

    val engineReady = sfStatus is StockfishStatus.Ready || sfStatus is StockfishStatus.UpdateAvailable
    val isDownloading = sfStatus is StockfishStatus.NotInstalled || sfStatus is StockfishStatus.Downloading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Update-available banner (non-blocking)
            if (sfStatus is StockfishStatus.UpdateAvailable) {
                val update = sfStatus as StockfishStatus.UpdateAvailable
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF57F17).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Update available: ${update.newVersion}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { StockfishManager.downloadLatest(context) }) {
                            Text("Update", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Error banner (non-blocking, but Play is disabled)
            if (sfStatus is StockfishStatus.Error) {
                val err = sfStatus as StockfishStatus.Error
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = err.message,
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { StockfishManager.downloadLatest(context) }) {
                            Text("Retry", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            // DroidChess title
            Text(
                text = "♞",
                fontSize = 80.sp,
                color = ChessGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "DroidChess",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Play • Learn • Improve",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Main play button — disabled until engine is ready
            Button(
                onClick = onPlayBot,
                enabled = engineReady,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChessGreen,
                    disabledContainerColor = ChessGreen.copy(alpha = 0.35f),
                    disabledContentColor = Color.White.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (isDownloading) "Engine Loading…" else "Play vs Bot",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuCard(
                    title = "Puzzles",
                    subtitle = "30+ Tactics",
                    icon = Icons.Default.Extension,
                    color = Color(0xFF9C27B0),
                    onClick = onPuzzles,
                    modifier = Modifier.weight(1f)
                )

                HomeMenuCard(
                    title = "Lessons",
                    subtitle = "10 Topics",
                    icon = Icons.Default.MenuBook,
                    color = Color(0xFF1976D2),
                    onClick = onLessons,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chess board decoration row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                val colors = listOf(
                    Color(0xFFEEEED2), Color(0xFF769656), Color(0xFFEEEED2), Color(0xFF769656),
                    Color(0xFF769656), Color(0xFFEEEED2), Color(0xFF769656), Color(0xFFEEEED2)
                )
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onSettings,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Settings")
            }
        }

        // Full-screen download overlay — blocks Play while engine is being set up
        if (isDownloading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.88f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "♞",
                            fontSize = 52.sp,
                            color = ChessGreen,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Setting up engine",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Downloading Stockfish chess engine.\nThis only happens once.",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (sfStatus is StockfishStatus.Downloading) {
                            val dl = sfStatus as StockfishStatus.Downloading
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = dl.releaseTag,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${(dl.progress * 100).toInt()}%",
                                    color = ChessGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = dl.progress,
                                modifier = Modifier.fillMaxWidth(),
                                color = ChessGreen,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = ChessGreen,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "You can still use Puzzles & Lessons",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onSettings,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.6f)
                            )
                        ) {
                            Text("Settings", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        }
    }
}
