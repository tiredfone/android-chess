package app.tiredfone.droidchess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiredfone.droidchess.data.model.Bot
import app.tiredfone.droidchess.data.model.BOTS
import app.tiredfone.droidchess.ui.components.BotAvatar
import app.tiredfone.droidchess.ui.theme.ChessGreen
import app.tiredfone.droidchess.ui.theme.DarkBackground
import app.tiredfone.droidchess.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotSelectionScreen(
    onBotSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Choose Your Opponent",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Group bots by difficulty range
            val beginners = BOTS.filter { it.elo <= 800 }
            val intermediate = BOTS.filter { it.elo in 801..1600 }
            val advanced = BOTS.filter { it.elo in 1601..2200 }
            val master = BOTS.filter { it.elo > 2200 }

            item {
                DifficultyHeader("Beginners", "200–800 ELO")
            }
            items(beginners) { bot ->
                BotCard(bot = bot, onClick = { onBotSelected(bot.id) })
            }

            item {
                DifficultyHeader("Intermediate", "1000–1600 ELO")
            }
            items(intermediate) { bot ->
                BotCard(bot = bot, onClick = { onBotSelected(bot.id) })
            }

            item {
                DifficultyHeader("Advanced", "1700–2200 ELO")
            }
            items(advanced) { bot ->
                BotCard(bot = bot, onClick = { onBotSelected(bot.id) })
            }

            item {
                DifficultyHeader("Master", "2300–3250 ELO")
            }
            items(master) { bot ->
                BotCard(bot = bot, onClick = { onBotSelected(bot.id) })
            }
        }
    }
}

@Composable
fun DifficultyHeader(title: String, range: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = range,
            color = ChessGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotCard(
    bot: Bot,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotAvatar(
                avatarColor = bot.avatarColor,
                avatarText = bot.avatarText,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bot.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = bot.description,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ELO badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(getEloColor(bot.elo).copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${bot.elo}",
                        color = getEloColor(bot.elo),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "ELO",
                        color = getEloColor(bot.elo).copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

fun getEloColor(elo: Int): Color {
    return when {
        elo < 600 -> Color(0xFF78909C)
        elo < 1000 -> Color(0xFF4CAF50)
        elo < 1400 -> Color(0xFF2196F3)
        elo < 1800 -> Color(0xFF9C27B0)
        elo < 2200 -> Color(0xFFFF9800)
        elo < 2600 -> Color(0xFFF44336)
        else -> Color(0xFFFFD700)
    }
}
