package com.chess.app.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter

class StockfishEngine(private val context: Context) {
    private var process: Process? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var isReady = false
    private var currentElo = 1500

    companion object {
        const val ENGINE_FILENAME = "stockfish"
        private const val THINK_TIME_MS = 500
    }

    suspend fun init(): Boolean = withContext(Dispatchers.IO) {
        try {
            val engineFile = copyEngineToFilesDir()
            if (!engineFile.exists()) {
                android.util.Log.w("StockfishEngine", "Engine binary not found, using fallback")
                return@withContext false
            }

            val processBuilder = ProcessBuilder(engineFile.absolutePath)
            processBuilder.redirectErrorStream(true)
            process = processBuilder.start()

            writer = PrintWriter(process!!.outputStream, true)
            reader = BufferedReader(InputStreamReader(process!!.inputStream))

            sendCommand("uci")
            waitForResponse("uciok", timeoutMs = 5000)

            sendCommand("isready")
            waitForResponse("readyok", timeoutMs = 5000)

            sendCommand("ucinewgame")
            isReady = true
            true
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Failed to initialize engine", e)
            false
        }
    }

    private fun copyEngineToFilesDir(): File {
        val engineDir = File(context.filesDir, "engine")
        engineDir.mkdirs()
        val engineFile = File(engineDir, ENGINE_FILENAME)

        try {
            context.assets.open("engine/$ENGINE_FILENAME").use { inputStream ->
                engineFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            engineFile.setExecutable(true)
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Failed to copy engine binary", e)
        }

        return engineFile
    }

    fun setElo(elo: Int) {
        currentElo = elo
        if (isReady) {
            sendCommand("setoption name UCI_LimitStrength value true")
            sendCommand("setoption name UCI_Elo value $elo")
        }
    }

    suspend fun getMove(fen: String, thinkTimeMs: Int = THINK_TIME_MS): String? = withContext(Dispatchers.IO) {
        if (!isReady) return@withContext null

        try {
            sendCommand("ucinewgame")
            sendCommand("setoption name UCI_LimitStrength value true")
            sendCommand("setoption name UCI_Elo value $currentElo")
            sendCommand("position fen $fen")
            sendCommand("go movetime $thinkTimeMs")

            var bestMove: String? = null
            val deadline = System.currentTimeMillis() + thinkTimeMs + 3000

            while (System.currentTimeMillis() < deadline) {
                val line = reader?.readLine() ?: break
                if (line.startsWith("bestmove")) {
                    val parts = line.split(" ")
                    if (parts.size >= 2 && parts[1] != "(none)") {
                        bestMove = parts[1]
                    }
                    break
                }
            }
            bestMove
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Error getting move", e)
            null
        }
    }

    suspend fun evaluatePosition(fen: String, depthOrTime: Int = 200): Int = withContext(Dispatchers.IO) {
        if (!isReady) return@withContext 0

        try {
            sendCommand("position fen $fen")
            sendCommand("go movetime $depthOrTime")

            var score = 0
            val deadline = System.currentTimeMillis() + depthOrTime + 2000

            while (System.currentTimeMillis() < deadline) {
                val line = reader?.readLine() ?: break
                if (line.startsWith("info") && line.contains("score cp")) {
                    val scoreIndex = line.indexOf("score cp") + 9
                    val rest = line.substring(scoreIndex).trim()
                    val scoreStr = rest.split(" ").firstOrNull() ?: "0"
                    score = scoreStr.toIntOrNull() ?: 0
                }
                if (line.startsWith("bestmove")) break
            }
            score
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Error evaluating position", e)
            0
        }
    }

    suspend fun getBestMoveAndEval(fen: String, thinkTimeMs: Int = THINK_TIME_MS): Pair<String?, Int> = withContext(Dispatchers.IO) {
        if (!isReady) return@withContext Pair(null, 0)

        try {
            sendCommand("ucinewgame")
            sendCommand("setoption name UCI_LimitStrength value false")
            sendCommand("position fen $fen")
            sendCommand("go movetime $thinkTimeMs")

            var bestMove: String? = null
            var score = 0
            val deadline = System.currentTimeMillis() + thinkTimeMs + 3000

            while (System.currentTimeMillis() < deadline) {
                val line = reader?.readLine() ?: break
                if (line.startsWith("info") && line.contains("score cp")) {
                    val scoreIndex = line.indexOf("score cp") + 9
                    val rest = line.substring(scoreIndex).trim()
                    val scoreStr = rest.split(" ").firstOrNull() ?: "0"
                    score = scoreStr.toIntOrNull() ?: 0
                }
                if (line.startsWith("bestmove")) {
                    val parts = line.split(" ")
                    if (parts.size >= 2 && parts[1] != "(none)") {
                        bestMove = parts[1]
                    }
                    break
                }
            }
            Pair(bestMove, score)
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Error getting move and eval", e)
            Pair(null, 0)
        }
    }

    private fun sendCommand(command: String) {
        writer?.println(command)
    }

    private fun waitForResponse(expectedToken: String, timeoutMs: Long = 3000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val line = reader?.readLine() ?: return false
            if (line.contains(expectedToken)) return true
        }
        return false
    }

    fun destroy() {
        isReady = false
        try {
            sendCommand("quit")
            writer?.close()
            reader?.close()
            process?.destroy()
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Error destroying engine", e)
        }
        writer = null
        reader = null
        process = null
    }
}
