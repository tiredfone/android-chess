package com.chess.app.data.repository

import com.chess.app.data.model.Bot
import com.chess.app.data.model.BOTS

class BotRepository {
    fun getAllBots(): List<Bot> = BOTS

    fun getBotById(id: Int): Bot? = BOTS.find { it.id == id }

    fun getBotByElo(targetElo: Int): Bot {
        return BOTS.minByOrNull { Math.abs(it.elo - targetElo) } ?: BOTS.first()
    }
}
