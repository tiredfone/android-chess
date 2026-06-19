package com.chess.app.data.model

data class StockfishRelease(
    val tagName: String,
    val publishedAt: String,
    val isPreRelease: Boolean,
    val assetName: String,
    val downloadUrl: String,
    val sizeBytes: Long
)
