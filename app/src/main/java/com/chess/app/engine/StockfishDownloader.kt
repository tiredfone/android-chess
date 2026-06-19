package com.chess.app.engine

import android.content.Context
import android.os.Build
import android.util.Log
import com.chess.app.data.model.StockfishRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class StockfishDownloader(private val context: Context) {

    companion object {
        private const val TAG = "StockfishDownloader"
        private const val PREFS_NAME = "stockfish_prefs"
        private const val PREF_INSTALLED_VERSION = "sf_installed_version"
        private const val PREF_CHANNEL = "sf_channel"
        private const val PREF_LAST_CHECK_MS = "sf_last_check_ms"
        private const val CHECK_INTERVAL_MS = 60 * 60 * 1000L // 1 hour
        private const val GITHUB_LATEST_URL =
            "https://api.github.com/repos/official-stockfish/Stockfish/releases/latest"
        private const val GITHUB_RELEASES_URL =
            "https://api.github.com/repos/official-stockfish/Stockfish/releases?per_page=10"
        private const val USER_AGENT = "android-chess-app/1.0"
        private const val CHANNEL_STABLE = "stable"
        private const val CHANNEL_DEV = "dev"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Channel ──────────────────────────────────────────────────────────────

    fun getChannel(): String = prefs.getString(PREF_CHANNEL, CHANNEL_STABLE) ?: CHANNEL_STABLE

    fun setChannel(channel: String) {
        prefs.edit().putString(PREF_CHANNEL, channel).apply()
    }

    // ── Installed version ─────────────────────────────────────────────────────

    fun getInstalledVersion(): String? = prefs.getString(PREF_INSTALLED_VERSION, null)

    fun saveInstalledVersion(tag: String) {
        prefs.edit().putString(PREF_INSTALLED_VERSION, tag).apply()
    }

    // ── Update-check timestamp ────────────────────────────────────────────────

    private fun getLastCheckMs(): Long = prefs.getLong(PREF_LAST_CHECK_MS, 0L)

    private fun saveLastCheckMs() {
        prefs.edit().putLong(PREF_LAST_CHECK_MS, System.currentTimeMillis()).apply()
    }

    fun shouldSkipCheck(forced: Boolean): Boolean {
        if (forced) return false
        val elapsed = System.currentTimeMillis() - getLastCheckMs()
        return elapsed < CHECK_INTERVAL_MS
    }

    // ── ABI / CPU-feature detection ──────────────────────────────────────────

    private data class DeviceCapabilities(
        val primaryAbi: String,
        val hasDotProd: Boolean,
        val hasNeon: Boolean
    )

    private fun detectDevice(): DeviceCapabilities {
        val cpuInfo = runCatching { File("/proc/cpuinfo").readText() }.getOrDefault("")
        val hasDotProd = cpuInfo.contains("asimddp")
        val hasNeon = cpuInfo.contains("asimd") || cpuInfo.contains("neon")
        val primaryAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        return DeviceCapabilities(primaryAbi, hasDotProd, hasNeon)
    }

    /**
     * Scores an asset name for the current device. Higher = better match.
     * Returns -1 if the asset is not usable.
     */
    private fun scoreAsset(name: String, caps: DeviceCapabilities): Int {
        val lower = name.lowercase()
        if (!lower.contains("android")) return -1

        return when (caps.primaryAbi) {
            "arm64-v8a" -> when {
                // Best: dotprod build, device supports it
                lower.contains("dotprod") && caps.hasDotProd -> 100
                // Good: armv8 without dotprod, no dotprod requirement
                lower.contains("armv8") && !lower.contains("dotprod") -> 80
                // Acceptable: any android arm64 build
                lower.contains("arm64") -> 60
                else -> -1
            }
            "armeabi-v7a" -> when {
                lower.contains("neon") && caps.hasNeon -> 100
                lower.contains("armv7") && !lower.contains("neon") -> 80
                lower.contains("arm") && !lower.contains("arm64") -> 60
                else -> -1
            }
            else -> if (lower.contains("android")) 10 else -1
        }
    }

    // ── GitHub API ───────────────────────────────────────────────────────────

    /**
     * Fetches the best release for this device from GitHub.
     * If [useDevChannel] is true, considers all releases (including pre-releases).
     * Otherwise only considers the latest non-pre-release.
     */
    suspend fun fetchLatestRelease(useDevChannel: Boolean): Result<StockfishRelease> =
        withContext(Dispatchers.IO) {
            runCatching {
                val caps = detectDevice()
                Log.d(TAG, "Device ABI=${caps.primaryAbi}, dotprod=${caps.hasDotProd}, neon=${caps.hasNeon}")

                val releaseJson: JSONObject = if (useDevChannel) {
                    // Fetch several releases and pick the newest (first in list)
                    val json = httpGet(GITHUB_RELEASES_URL)
                    val arr = JSONArray(json)
                    if (arr.length() == 0) error("No releases found on GitHub")
                    arr.getJSONObject(0)
                } else {
                    JSONObject(httpGet(GITHUB_LATEST_URL))
                }

                saveLastCheckMs()
                parseRelease(releaseJson, caps)
            }
        }

    private fun parseRelease(json: JSONObject, caps: DeviceCapabilities): StockfishRelease {
        val tagName = json.getString("tag_name")
        val publishedAt = json.optString("published_at", "")
        val isPreRelease = json.optBoolean("prerelease", false)
        val assets = json.getJSONArray("assets")

        var bestScore = -1
        var bestAsset: JSONObject? = null

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            val score = scoreAsset(name, caps)
            Log.d(TAG, "Asset '$name' score=$score")
            if (score > bestScore) {
                bestScore = score
                bestAsset = asset
            }
        }

        checkNotNull(bestAsset) { "No suitable Stockfish asset found for ABI=${caps.primaryAbi}" }

        return StockfishRelease(
            tagName = tagName,
            publishedAt = publishedAt,
            isPreRelease = isPreRelease,
            assetName = bestAsset.getString("name"),
            downloadUrl = bestAsset.getString("browser_download_url"),
            sizeBytes = bestAsset.getLong("size")
        )
    }

    private fun httpGet(urlStr: String): String {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.connect()

            val code = conn.responseCode
            if (code == 403) error("GitHub API rate limit exceeded (HTTP 403)")
            if (code != 200) error("GitHub API returned HTTP $code")

            return conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    // ── Download & Install ───────────────────────────────────────────────────

    /**
     * Downloads [release], extracts the binary from the zip, saves it to
     * `filesDir/engine/stockfish`, sets executable bit, and stores the version.
     *
     * Follows redirects inline (up to 5 hops) using GET requests so CDN tokens
     * are preserved and we can detect failures at each hop.
     */
    suspend fun downloadAndInstall(release: StockfishRelease, onProgress: (Float) -> Unit): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val engineDir = File(context.filesDir, "engine").also { it.mkdirs() }
                val destFile = File(engineDir, "stockfish")
                val tmpFile = File(engineDir, "stockfish.tmp")
                if (tmpFile.exists()) tmpFile.delete()

                Log.i(TAG, "Downloading ${release.assetName} from ${release.downloadUrl}")
                onProgress(0.01f)

                // Stream download with manual redirect following (up to 5 hops)
                var downloadUrl = release.downloadUrl
                var responseCode: Int
                var conn: HttpURLConnection
                var hops = 0
                while (true) {
                    conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", USER_AGENT)
                        instanceFollowRedirects = false
                        connectTimeout = 20_000
                        readTimeout = 120_000
                    }
                    conn.connect()
                    responseCode = conn.responseCode
                    if (responseCode in 301..308 && hops < 5) {
                        val loc = conn.getHeaderField("Location") ?: break
                        conn.disconnect()
                        downloadUrl = loc
                        hops++
                        Log.d(TAG, "Redirect $hops → $downloadUrl")
                    } else break
                }

                if (responseCode != 200) {
                    conn.disconnect()
                    error("Download failed: HTTP $responseCode for $downloadUrl")
                }

                val totalBytes = conn.contentLengthLong.takeIf { it > 0 } ?: release.sizeBytes
                var bytesRead = 0L

                try {
                    ZipInputStream(conn.inputStream.buffered(65_536)).use { zis ->
                        var entry = zis.nextEntry
                        while (entry != null && entry.isDirectory) { zis.closeEntry(); entry = zis.nextEntry }
                        checkNotNull(entry) { "No file found inside ${release.assetName}" }
                        Log.i(TAG, "Extracting: ${entry.name}")
                        tmpFile.outputStream().buffered(65_536).use { out ->
                            val buf = ByteArray(65_536)
                            var n: Int
                            while (zis.read(buf).also { n = it } != -1) {
                                out.write(buf, 0, n)
                                bytesRead += n
                                if (totalBytes > 0) onProgress((bytesRead.toFloat() / totalBytes).coerceIn(0.01f, 0.99f))
                            }
                        }
                    }
                } finally { conn.disconnect() }

                check(tmpFile.exists() && tmpFile.length() > 100_000) {
                    "Extracted file is too small (${tmpFile.length()} bytes) — may be corrupt"
                }
                if (destFile.exists()) destFile.delete()
                check(tmpFile.renameTo(destFile)) { "Failed to move tmp to final path" }
                destFile.setExecutable(true, false)
                Log.i(TAG, "Installed ${release.tagName} at ${destFile.absolutePath} (${destFile.length()} bytes)")
                saveInstalledVersion(release.tagName)
                onProgress(1f)
                destFile
            }
        }

    // ── Installed binary path ────────────────────────────────────────────────

    fun getInstalledBinaryFile(): File =
        File(File(context.filesDir, "engine"), "stockfish")
}
