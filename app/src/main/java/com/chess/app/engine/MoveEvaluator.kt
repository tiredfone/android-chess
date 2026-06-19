package com.chess.app.engine

import com.chess.app.data.model.MoveClassification

object MoveEvaluator {
    /**
     * Classifies a move based on centipawn evaluation before and after.
     * Positive eval = good for White, negative = good for Black.
     *
     * @param evalBefore centipawn eval before the move (from mover's perspective)
     * @param evalAfter centipawn eval after the move (from mover's perspective, negated for opponent's response)
     * @param isWhiteMove true if this was White's move
     */
    fun classifyMove(evalBefore: Int, evalAfter: Int, isWhiteMove: Boolean): MoveClassification {
        // Normalize evaluations to be from the moving player's perspective
        val before = if (isWhiteMove) evalBefore else -evalBefore
        val after = if (isWhiteMove) -evalAfter else evalAfter  // After eval is opponent to move

        // Delta: positive means the position improved for the moving player
        // (i.e., after the move, the opponent's eval is better, which means the move was good for us)
        val delta = before - after  // How much did we "lose" (negative = we gained)

        return when {
            delta <= -300 -> MoveClassification.BRILLIANT  // Massive improvement (sacrifices etc)
            delta <= -100 -> MoveClassification.BEST
            delta <= 0 -> MoveClassification.EXCELLENT
            delta <= 50 -> MoveClassification.GOOD
            delta <= 100 -> MoveClassification.INACCURACY
            delta <= 200 -> MoveClassification.MISTAKE
            else -> MoveClassification.BLUNDER
        }
    }

    /**
     * Calculate accuracy percentage for a list of moves.
     * Based on a simplified version of the chess.com accuracy formula.
     */
    fun calculateAccuracy(classifications: List<MoveClassification>): Float {
        if (classifications.isEmpty()) return 100f

        val totalScore = classifications.map { classification ->
            when (classification) {
                MoveClassification.BRILLIANT -> 100
                MoveClassification.BEST -> 100
                MoveClassification.EXCELLENT -> 90
                MoveClassification.GOOD -> 75
                MoveClassification.INACCURACY -> 50
                MoveClassification.MISTAKE -> 25
                MoveClassification.BLUNDER -> 0
            }
        }.sum()

        return (totalScore.toFloat() / classifications.size).coerceIn(0f, 100f)
    }

    /**
     * Simple heuristic for ELO-based think time.
     */
    fun thinkTimeForElo(elo: Int): Int {
        return when {
            elo < 400 -> 100
            elo < 800 -> 200
            elo < 1200 -> 300
            elo < 1600 -> 500
            elo < 2000 -> 750
            elo < 2400 -> 1000
            else -> 1500
        }
    }
}
