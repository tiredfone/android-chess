package com.chess.app.engine

import android.content.Context
import android.os.Build
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
        private const val THINK_TIME_MS = 500

        // Candidate asset subdirs per Android ABI, in preference order.
        // Folder names match the Stockfish release zip suffixes:
        //   arm64-v8a-dotprod, arm64-v8a, armeabi-v7a-neon, armeabi-v7a
        private val ABI_CANDIDATES = mapOf(
            "arm64-v8a"   to listOf("arm64-v8a-dotprod", "arm64-v8a"),
            "armeabi-v7a" to listOf("armeabi-v7a-neon",  "armeabi-v7a")
        )
    }

    /**
     * Picks the best Stockfish binary available in assets for this device.
     * Prefers dotprod (ARM64) or NEON (ARMv7) when the CPU reports support,
     * then falls back to the baseline variant for the same ABI.
     */
    private fun selectAssetPath(): String? {
        val cpuInfo = runCatching { File("/proc/cpuinfo").readText() }.getOrDefault("")
        val hasDotProd = cpuInfo.contains("asimddp")
        val hasNeon    = cpuInfo.contains("asimd") || cpuInfo.contains("neon")

        for (abi in Build.SUPPORTED_ABIS) {
            val candidates = ABI_CANDIDATES[abi] ?: continue
            // First pass: respect CPU feature flags
            for (dir in candidates) {
                if (dir.endsWith("dotprod") && !hasDotProd) continue
                if (dir.endsWith("neon")    && !hasNeon)    continue
                val path = "engine/$dir/stockfish"
                if (assetExists(path)) {
                    android.util.Log.i("StockfishEngine", "Selected: $path")
                    return path
                }
            }
            // Second pass: take any available candidate for this ABI
            for (dir in candidates) {
                val path = "engine/$dir/stockfish"
                if (assetExists(path)) {
                    android.util.Log.i("StockfishEngine", "Selected (no-feature-check): $path")
                    return path
                }
            }
        }
        return null
    }

    private fun assetExists(assetPath: String): Boolean =
        runCatching { context.assets.open(assetPath).close() }.isSuccess

    suspend fun init(): Boolean = withContext(Dispatchers.IO) {
        try {
            val engineFile = copyEngineToFilesDir()
            if (engineFile == null || !engineFile.exists()) {
                android.util.Log.w("StockfishEngine", "No engine binary found — bots will play randomly")
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

    private fun copyEngineToFilesDir(): File? {
        val assetPath = selectAssetPath() ?: return null

        val engineDir = File(context.filesDir, "engine")
        engineDir.mkdirs()
        // Use the asset dir name as the destination file name so different ABIs
        // don't overwrite each other if someone swaps binaries mid-session.
        val destName = assetPath.removePrefix("engine/").replace("/", "-") // e.g. arm64-v8a-dotprod-stockfish
        val engineFile = File(engineDir, destName)

        try {
            context.assets.open(assetPath).use { src ->
                engineFile.outputStream().use { dst -> src.copyTo(dst) }
            }
            engineFile.setExecutable(true)
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Failed to copy $assetPath", e)
            return null
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
                    if (parts.size >= 2 && parts[1] != "(none)") bestMove = parts[1]
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
                    val scoreStr = line.substring(scoreIndex).trim().split(" ").firstOrNull() ?: "0"
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
                    val scoreStr = line.substring(scoreIndex).trim().split(" ").firstOrNull() ?: "0"
                    score = scoreStr.toIntOrNull() ?: 0
                }
                if (line.startsWith("bestmove")) {
                    val parts = line.split(" ")
                    if (parts.size >= 2 && parts[1] != "(none)") bestMove = parts[1]
                    break
                }
            }
            Pair(bestMove, score)
        } catch (e: Exception) {
            android.util.Log.e("StockfishEngine", "Error getting best move and eval", e)
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
