package com.chess.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chess.app.engine.StockfishStatus
import com.chess.app.ui.theme.*
import com.chess.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    // In a real app, these would be backed by DataStore/SharedPreferences
    var moveFeedback by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var boardTheme by remember { mutableStateOf("green") }
    var pieceStyle by remember { mutableStateOf("classic") }

    val settingsViewModel: SettingsViewModel = viewModel()
    val sfStatus by settingsViewModel.stockfishStatus.collectAsState()
    var selectedChannel by remember { mutableStateOf(settingsViewModel.getChannel()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ── Stockfish Engine Section ──────────────────────────────────────
            item {
                SettingsSectionHeader("Stockfish Engine")
            }

            item {
                StockfishEngineCard(
                    status = sfStatus,
                    selectedChannel = selectedChannel,
                    onChannelSelected = { channel ->
                        selectedChannel = channel
                        settingsViewModel.setChannel(channel)
                    },
                    onCheckForUpdates = { settingsViewModel.checkForUpdate() },
                    onDownloadUpdate = { settingsViewModel.downloadUpdate() }
                )
            }

            // ── Gameplay Section ──────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader("Gameplay")
            }

            item {
                SettingsToggleRow(
                    title = "Move Feedback",
                    subtitle = "Show move quality indicators (Brilliant, Blunder, etc.)",
                    checked = moveFeedback,
                    onCheckedChange = { moveFeedback = it }
                )
            }

            item {
                SettingsToggleRow(
                    title = "Sound Effects",
                    subtitle = "Play sounds for moves and events",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
            }

            // Board Theme
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader("Board Theme")
            }

            item {
                BoardThemeSelector(
                    currentTheme = boardTheme,
                    onThemeSelected = { boardTheme = it }
                )
            }

            // Piece Style
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader("Piece Style")
            }

            item {
                PieceStyleSelector(
                    currentStyle = pieceStyle,
                    onStyleSelected = { pieceStyle = it }
                )
            }

            // About
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSectionHeader("About")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Chess App",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Powered by Stockfish chess engine.\nFeatures 20 bots from 200 to 3250 ELO, 30+ puzzles, and 10 chess lessons.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StockfishEngineCard(
    status: StockfishStatus,
    selectedChannel: String,
    onChannelSelected: (String) -> Unit,
    onCheckForUpdates: () -> Unit,
    onDownloadUpdate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Installed version ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Installed:",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.width(80.dp)
                )
                val versionLabel = when (status) {
                    is StockfishStatus.Ready -> status.version
                    is StockfishStatus.UpdateAvailable -> status.currentVersion
                    is StockfishStatus.Downloading -> "Downloading…"
                    is StockfishStatus.Error -> "Error"
                    is StockfishStatus.NotInstalled -> "Not installed"
                }
                Text(
                    text = versionLabel,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // ── Channel selector ─────────────────────────────────────────────
            Text(
                text = "Channel:",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("stable" to "Stable", "dev" to "Dev").forEach { (key, label) ->
                    val isSelected = selectedChannel == key
                    OutlinedButton(
                        onClick = { if (!isSelected) onChannelSelected(key) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) ChessGreen.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isSelected) ChessGreen else Color.White.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) ChessGreen else Color.White.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = if (isSelected) "$label ●" else "$label ○",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // ── Action buttons ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCheckForUpdates,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text("Check for Updates", fontSize = 12.sp)
                }

                if (status is StockfishStatus.UpdateAvailable) {
                    Button(
                        onClick = onDownloadUpdate,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ChessGreen)
                    ) {
                        Text("Update to ${status.newVersion}", fontSize = 12.sp)
                    }
                }
            }

            // ── Progress bar when downloading ────────────────────────────────
            if (status is StockfishStatus.Downloading) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { status.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = ChessGreen,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
                Text(
                    text = "${(status.progress * 100).toInt()}% — ${status.releaseTag}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Error message ────────────────────────────────────────────────
            if (status is StockfishStatus.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = status.message,
                    color = Color(0xFFEF9A9A),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = ChessGreen,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ChessGreen,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun BoardThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        Triple("green", Color(0xFF769656), Color(0xFFEEEED2)),
        Triple("blue", Color(0xFF8CA2AD), Color(0xFFDEE3E6)),
        Triple("brown", Color(0xFFB58863), Color(0xFFF0D9B5))
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                themes.forEach { (name, darkColor, lightColor) ->
                    ThemePreview(
                        name = name.replaceFirstChar { it.uppercase() },
                        darkColor = darkColor,
                        lightColor = lightColor,
                        isSelected = currentTheme == name,
                        onClick = { onThemeSelected(name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemePreview(
    name: String,
    darkColor: Color,
    lightColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            // 2x2 mini board
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(lightColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(darkColor)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(darkColor)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(lightColor)
                    )
                }
            }

            // Check mark overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            color = if (isSelected) ChessGreen else Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PieceStyleSelector(
    currentStyle: String,
    onStyleSelected: (String) -> Unit
) {
    val styles = listOf("Classic" to "♔♕♖♗♘♙", "Neo" to "♚♛♜♝♞♟")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            styles.forEach { (name, preview) ->
                val styleLower = name.lowercase()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStyleSelected(styleLower) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentStyle == styleLower,
                        onClick = { onStyleSelected(styleLower) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ChessGreen,
                            unselectedColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = preview,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 20.sp
                        )
                    }
                }
                if (name != styles.last().first) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                }
            }
        }
    }
}
