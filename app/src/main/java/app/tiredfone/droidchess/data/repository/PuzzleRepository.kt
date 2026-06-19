package app.tiredfone.droidchess.data.repository

import app.tiredfone.droidchess.data.model.Puzzle

class PuzzleRepository {
    fun getAllPuzzles(): List<Puzzle> = PUZZLES

    fun getPuzzleById(id: Int): Puzzle? = PUZZLES.find { it.id == id }

    fun getPuzzlesByTheme(theme: String): List<Puzzle> = PUZZLES.filter { it.theme == theme }

    fun getPuzzlesByDifficulty(difficulty: String): List<Puzzle> = PUZZLES.filter { it.difficulty == difficulty }
}

val PUZZLES = listOf(
    Puzzle(
        id = 1,
        title = "Mate in One",
        description = "White to move. Deliver checkmate in one move.",
        fen = "r1bqkb1r/pppp1Qpp/2n2n2/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4",
        solution = listOf("f7f8"),
        difficulty = "Beginner",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 2,
        title = "Back Rank Mate",
        description = "White to move. Use the back rank weakness.",
        fen = "6k1/5ppp/8/8/8/8/8/3R2K1 w - - 0 1",
        solution = listOf("d1d8"),
        difficulty = "Beginner",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 3,
        title = "Knight Fork",
        description = "White to move. Fork the king and queen with your knight.",
        fen = "r3k2r/ppp2ppp/2n5/3pp3/1b1PP3/2N2N2/PPP2PPP/R1BQKB1R w KQkq - 0 7",
        solution = listOf("f3e5"),
        difficulty = "Beginner",
        theme = "Fork"
    ),
    Puzzle(
        id = 4,
        title = "Pin the Queen",
        description = "White to move. Pin the queen to the king.",
        fen = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 4 5",
        solution = listOf("c4f7"),
        difficulty = "Intermediate",
        theme = "Pin"
    ),
    Puzzle(
        id = 5,
        title = "Skewer the King",
        description = "White to move. Use a skewer to win material.",
        fen = "6k1/8/8/8/8/8/2B5/6K1 w - - 0 1",
        solution = listOf("c2h7"),
        difficulty = "Beginner",
        theme = "Skewer"
    ),
    Puzzle(
        id = 6,
        title = "Queen Sacrifice Mate",
        description = "White to move. Sacrifice the queen for checkmate.",
        fen = "r3kb1r/ppp2ppp/2n5/3pp3/2B1P1Q1/2N5/PPPP1PPP/R1B1K2R w KQkq - 0 1",
        solution = listOf("g4g7"),
        difficulty = "Intermediate",
        theme = "Sacrifice"
    ),
    Puzzle(
        id = 7,
        title = "Double Check",
        description = "White to move. Deliver check from two pieces.",
        fen = "r1bqk2r/pppp1ppp/2n5/2b1p3/4P1n1/3P1N2/PPP2PPP/RNBQKB1R w KQkq - 0 5",
        solution = listOf("f3e5"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 8,
        title = "Discovered Attack",
        description = "White to move. Uncover a devastating discovered attack.",
        fen = "r1bqkb1r/pppp1ppp/2n2n2/4p3/4P3/3B1N2/PPPP1PPP/RNBQK2R w KQkq - 4 4",
        solution = listOf("d3h7"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 9,
        title = "Rook Checkmate",
        description = "White to move. Use your rook to deliver checkmate.",
        fen = "8/8/8/8/8/6k1/8/6KR w - - 0 1",
        solution = listOf("h1h3"),
        difficulty = "Beginner",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 10,
        title = "Two Rooks Checkmate",
        description = "White to move. Use your two rooks to checkmate.",
        fen = "8/8/8/8/8/7k/8/RR4K1 w - - 0 1",
        solution = listOf("b1h1"),
        difficulty = "Beginner",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 11,
        title = "Knight and Bishop Mate",
        description = "White to move. Coordinate pieces for checkmate.",
        fen = "8/8/8/8/8/5k2/8/3B1NK1 w - - 0 1",
        solution = listOf("f1e3"),
        difficulty = "Advanced",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 12,
        title = "Desperado Tactic",
        description = "White to move. A piece will be lost — make it count!",
        fen = "r1bq1rk1/pp2ppbp/2np1np1/8/3NP3/2N1BP2/PPP3PP/R2QKB1R w KQ - 0 9",
        solution = listOf("d4c6"),
        difficulty = "Advanced",
        theme = "Tactics"
    ),
    Puzzle(
        id = 13,
        title = "Zwischenzug",
        description = "White to move. Find the in-between move!",
        fen = "r1b2rk1/ppq1bppp/2n1p3/3pP3/3P4/2NB1N2/PPQ2PPP/R4RK1 w - - 0 13",
        solution = listOf("e5d6"),
        difficulty = "Advanced",
        theme = "Tactics"
    ),
    Puzzle(
        id = 14,
        title = "Pawn Promotion",
        description = "White to move. Promote your pawn to win.",
        fen = "8/P7/8/8/8/8/8/k1K5 w - - 0 1",
        solution = listOf("a7a8"),
        difficulty = "Beginner",
        theme = "Promotion"
    ),
    Puzzle(
        id = 15,
        title = "Underpromotion",
        description = "White to move. Promote to a knight for checkmate!",
        fen = "8/6P1/8/8/8/8/8/k1K3r1 w - - 0 1",
        solution = listOf("g7g8"),
        difficulty = "Intermediate",
        theme = "Promotion"
    ),
    Puzzle(
        id = 16,
        title = "Smothered Mate",
        description = "White to move. The king is smothered by its own pieces.",
        fen = "6rk/6pp/8/8/8/8/8/4K1N1 w - - 0 1",
        solution = listOf("g1f3"),
        difficulty = "Intermediate",
        theme = "Checkmate"
    ),
    Puzzle(
        id = 17,
        title = "Greek Gift Sacrifice",
        description = "White to move. Sacrifice the bishop on h7.",
        fen = "r1b2rk1/ppp1nppp/3p4/3Pp3/2B1P3/2N2N2/PPP2PPP/R2QK2R w KQ - 0 9",
        solution = listOf("c4h7"),
        difficulty = "Advanced",
        theme = "Sacrifice"
    ),
    Puzzle(
        id = 18,
        title = "Removing the Defender",
        description = "White to move. Eliminate the key defender.",
        fen = "r2qr1k1/ppp2ppp/2n5/3p4/3P4/2N1BN2/PPP2PPP/R2QR1K1 w - - 0 11",
        solution = listOf("e3h6"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 19,
        title = "X-Ray Attack",
        description = "White to move. Use an x-ray attack to win material.",
        fen = "r3k2r/ppp2ppp/2nb4/3q4/3P4/2NB4/PPP2PPP/R2QK2R w KQkq - 0 10",
        solution = listOf("d3b5"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 20,
        title = "Windmill",
        description = "White to move. Use the windmill technique to gain material.",
        fen = "6k1/ppp2p1p/8/8/2R5/2B5/PPP3PP/6K1 w - - 0 1",
        solution = listOf("c4c8"),
        difficulty = "Advanced",
        theme = "Tactics"
    ),
    Puzzle(
        id = 21,
        title = "Overloaded Piece",
        description = "White to move. The defender is overloaded!",
        fen = "r1bqk2r/pppp1ppp/2n2n2/4p3/1bB1P3/2N2N2/PPPP1PPP/R1BQK2R w KQkq - 4 5",
        solution = listOf("c4f7"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 22,
        title = "En Passant Tactics",
        description = "White to move. Use en passant to your advantage.",
        fen = "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3",
        solution = listOf("e5f6"),
        difficulty = "Intermediate",
        theme = "Tactics"
    ),
    Puzzle(
        id = 23,
        title = "Castling Tactics",
        description = "Black to move. Take advantage of White's exposed king.",
        fen = "r3kbnr/ppp2ppp/2nq4/3pp3/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 5",
        solution = listOf("d6h2"),
        difficulty = "Advanced",
        theme = "Attack"
    ),
    Puzzle(
        id = 24,
        title = "Queen and Rook Battery",
        description = "White to move. Set up a deadly battery.",
        fen = "r1b1k2r/ppppqppp/2n5/4p3/2B1P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 4 5",
        solution = listOf("e1g1"),
        difficulty = "Beginner",
        theme = "Tactics"
    ),
    Puzzle(
        id = 25,
        title = "Knight on the Rim",
        description = "White to move. Centralize your knight for a decisive attack.",
        fen = "r1bqkb1r/pppp1ppp/2n2n2/4p3/3PP3/5N2/PPP2PPP/RNBQKB1R w KQkq - 2 4",
        solution = listOf("f3g5"),
        difficulty = "Beginner",
        theme = "Tactics"
    ),
    Puzzle(
        id = 26,
        title = "Philidor Position",
        description = "White to move and draw. Find the defensive resource.",
        fen = "8/8/8/8/3k4/8/3K4/3R4 w - - 0 1",
        solution = listOf("d1d4"),
        difficulty = "Intermediate",
        theme = "Endgame"
    ),
    Puzzle(
        id = 27,
        title = "King and Pawn Endgame",
        description = "White to move and win. Activate your king.",
        fen = "8/8/8/8/4k3/8/4P3/4K3 w - - 0 1",
        solution = listOf("e1e2"),
        difficulty = "Beginner",
        theme = "Endgame"
    ),
    Puzzle(
        id = 28,
        title = "Rook vs Pawn",
        description = "White to move. Stop the passed pawn.",
        fen = "8/6p1/8/8/8/8/8/R3K1k1 w Q - 0 1",
        solution = listOf("a1g1"),
        difficulty = "Intermediate",
        theme = "Endgame"
    ),
    Puzzle(
        id = 29,
        title = "Bishop vs Knight",
        description = "White to move. Exploit the superior bishop.",
        fen = "8/8/3b4/8/8/3N4/8/K1k5 w - - 0 1",
        solution = listOf("d3e5"),
        difficulty = "Advanced",
        theme = "Endgame"
    ),
    Puzzle(
        id = 30,
        title = "Trébuchet",
        description = "White to move. This is a zugzwang position!",
        fen = "8/8/8/8/8/1k6/1p6/1K6 w - - 0 1",
        solution = listOf("b1a1"),
        difficulty = "Advanced",
        theme = "Endgame"
    )
)
