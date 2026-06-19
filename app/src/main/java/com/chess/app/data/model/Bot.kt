package com.chess.app.data.model

data class Bot(
    val id: Int,
    val name: String,
    val elo: Int,
    val description: String,
    val avatarColor: Long,
    val avatarText: String
)

val BOTS = listOf(
    Bot(1, "Martin", 200, "A complete beginner. Makes random moves.", 0xFF4CAF50, "M"),
    Bot(2, "Ana", 400, "Learning the basics. Sometimes hangs pieces.", 0xFFE91E63, "A"),
    Bot(3, "Max", 600, "Knows basic rules but misses tactics.", 0xFF2196F3, "MX"),
    Bot(4, "Sofia", 800, "A casual player. Occasionally sets traps.", 0xFF9C27B0, "S"),
    Bot(5, "Carlos", 1000, "Intermediate player with decent opening knowledge.", 0xFFFF9800, "C"),
    Bot(6, "Lily", 1200, "Plays solid but misses complex combinations.", 0xFFE91E63, "L"),
    Bot(7, "Daniel", 1400, "Strong club player with good tactical awareness.", 0xFF3F51B5, "D"),
    Bot(8, "Emma", 1500, "Chess enthusiast with good positional understanding.", 0xFF009688, "E"),
    Bot(9, "Noah", 1600, "Aggressive player who loves attacking chess.", 0xFFF44336, "N"),
    Bot(10, "Olivia", 1700, "Strategic player with excellent endgame technique.", 0xFF673AB7, "O"),
    Bot(11, "Lucas", 1800, "Tournament player with deep opening preparation.", 0xFF795548, "Lu"),
    Bot(12, "Mia", 1900, "Expert-level player with sharp tactical vision.", 0xFF607D8B, "Mi"),
    Bot(13, "Ethan", 2000, "National master with strong positional play.", 0xFF1976D2, "Et"),
    Bot(14, "Aiden", 2200, "FIDE Master with excellent opening repertoire.", 0xFFD32F2F, "Ai"),
    Bot(15, "Isabella", 2300, "International Master with deep strategic understanding.", 0xFFC2185B, "Is"),
    Bot(16, "James", 2400, "Grandmaster-level tactics and strategy.", 0xFF1565C0, "Ja"),
    Bot(17, "Charlotte", 2500, "Top-level GM with near-perfect endgame technique.", 0xFF6A1B9A, "Ch"),
    Bot(18, "Alexander", 2700, "Super-GM level. Incredibly precise and calculating.", 0xFF0D47A1, "Al"),
    Bot(19, "Hikaru", 2900, "Lightning-fast calculation. Near-perfect play.", 0xFFBF360C, "Hi"),
    Bot(20, "Magnus", 3250, "The highest rated bot. Plays almost perfectly.", 0xFF37474F, "Ma"),
)
