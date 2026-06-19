package com.chess.app.data.model

data class Puzzle(
    val id: Int,
    val title: String,
    val description: String,
    val fen: String,
    val solution: List<String>, // UCI format moves
    val difficulty: String,
    val theme: String
)
