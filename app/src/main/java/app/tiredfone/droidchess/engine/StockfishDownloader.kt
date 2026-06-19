package app.tiredfone.droidchess.engine

import android.content.Context
import android.os.Build
import android.util.Log
import app.tiredfone.droidchess.data.model.StockfishRelease
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream
import java.util.zip.ZipInputStream

class StockfishDownloader(private val context: Context) {

    companion object {
        private const val TAG = "StockfishDownloader"
        private const val PREFS_NAME = "stockfish_prefs"
        private const val PREF_INSTALLED_VERSION = "sf_installed_version"
        private const val PREF_CHANNEL = "sf_channel"
        private const val PREF_LAST_CHECK_MS = "sf_last_check_ms"
        private const val CHECK_INTERVAL_MS = 60 * 60 * 1000L
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

    fun clearInstalledVersion() {
        prefs.edit().remove(PREF_INSTALLED_VERSION).apply()
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
     *
     * Stockfish 18+ releases Android assets as .tar files:
     *   stockfish-android-armv8-dotprod.tar
     *   stockfish-android-armv8.tar
     *   stockfish-android-armv7-neon.tar
     *   stockfish-android-armv7.tar
     */
    /**
     * [preferSafer] = true skips dotprod/NEON variants so the baseline binary is chosen.
     * Used on retry after an "exited immediately" crash (likely SIGILL from ABI mismatch).
     */
    private fun scoreAsset(name: String, caps: DeviceCapabilities, preferSafer: Boolean = false): Int {
        val lower = name.lowercase()
        if (!lower.contains("android")) return -1

        return when (caps.primaryAbi) {
            "arm64-v8a" -> when {
                lower.contains("armv8") && lower.contains("dotprod") && caps.hasDotProd && !preferSafer -> 100
                lower.contains("armv8") && !lower.contains("dotprod") -> 80
                lower.contains("arm64") -> 60
                else -> -1
            }
            "armeabi-v7a" -> when {
                lower.contains("armv7") && lower.contains("neon") && caps.hasNeon && !preferSafer -> 100
                lower.contains("armv7") && !lower.contains("neon") -> 80
                lower.contains("arm") && !lower.contains("arm64") -> 60
                else -> -1
            }
            else -> if (lower.contains("android")) 10 else -1
        }
    }

    // ── GitHub API ───────────────────────────────────────────────────────────

    suspend fun fetchLatestRelease(useDevChannel: Boolean, preferSafer: Boolean = false): Result<StockfishRelease> =
        withContext(Dispatchers.IO) {
            runCatching {
                val caps = detectDevice()
                Log.d(TAG, "Device ABI=${caps.primaryAbi}, dotprod=${caps.hasDotProd}, neon=${caps.hasNeon}, preferSafer=$preferSafer")

                val releaseJson: JSONObject = if (useDevChannel) {
                    val json = httpGet(GITHUB_RELEASES_URL)
                    val arr = JSONArray(json)
                    if (arr.length() == 0) error("No releases found on GitHub")
                    arr.getJSONObject(0)
                } else {
                    JSONObject(httpGet(GITHUB_LATEST_URL))
                }

                saveLastCheckMs()
                parseRelease(releaseJson, caps, preferSafer)
            }
        }

    private fun parseRelease(json: JSONObject, caps: DeviceCapabilities, preferSafer: Boolean = false): StockfishRelease {
        val tagName = json.getString("tag_name")
        val publishedAt = json.optString("published_at", "")
        val isPreRelease = json.optBoolean("prerelease", false)
        val assets = json.getJSONArray("assets")

        var bestScore = -1
        var bestAsset: JSONObject? = null

        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            val score = scoreAsset(name, caps, preferSafer)
            Log.d(TAG, "Asset '$name' score=$score (preferSafer=$preferSafer)")
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
     * Downloads [release], extracts the binary, saves it to filesDir/engine/stockfish,
     * sets executable bit, and stores the version.
     *
     * Handles plain .tar, gzip-compressed .tar (.tar.gz), and .zip archives.
     * Auto-detects gzip by magic bytes (0x1f 0x8b) regardless of file extension,
     * to cope with CDN transparent compression.
     *
     * Redirect following is done inline with GET requests so CDN tokens are preserved.
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

                // Follow redirects inline with GET (up to 5 hops)
                var downloadUrl = release.downloadUrl
                var responseCode: Int
                var conn: HttpURLConnection
                var hops = 0
                while (true) {
                    conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        setRequestProperty("User-Agent", USER_AGENT)
                        // Explicitly disable gzip accept-encoding so the CDN doesn't
                        // transparently gzip binary content and confuse TAR parsing.
                        setRequestProperty("Accept-Encoding", "identity")
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
                        Log.d(TAG, "Redirect $hops -> $downloadUrl")
                    } else break
                }

                if (responseCode != 200) {
                    conn.disconnect()
                    error("HTTP $responseCode downloading ${release.assetName}")
                }

                val totalBytes = conn.contentLengthLong.takeIf { it > 0 } ?: release.sizeBytes
                val contentEncoding = conn.contentEncoding
                Log.d(TAG, "Content-Length=$totalBytes, Content-Encoding=$contentEncoding, asset=${release.assetName}")

                var bytesExtracted = 0L

                try {
                    // Use a BufferedInputStream with mark/reset support so we can peek at magic bytes.
                    val buffered = conn.inputStream.buffered(65_536)

                    // Peek at first two bytes to detect gzip magic (0x1f 0x8b).
                    buffered.mark(2)
                    val b0 = buffered.read()
                    val b1 = buffered.read()
                    buffered.reset()
                    val isGzip = b0 == 0x1f && b1 == 0x8b

                    val assetLower = release.assetName.lowercase()
                    val isTarAsset = assetLower.endsWith(".tar") || assetLower.endsWith(".tar.gz")
                    val isZipAsset = assetLower.endsWith(".zip")

                    Log.i(TAG, "Format detection: gzip=$isGzip, isTarAsset=$isTarAsset, isZipAsset=$isZipAsset")

                    // Build the final stream to read from:
                    // - gzip bytes detected -> unwrap with GZIPInputStream first (handles .tar.gz too)
                    // - then treat the result as TAR or ZIP based on extension
                    val decompressedStream: InputStream = if (isGzip) {
                        Log.i(TAG, "Decompressing gzip stream on-the-fly")
                        GZIPInputStream(buffered)
                    } else {
                        buffered
                    }

                    decompressedStream.use { input ->
                        tmpFile.outputStream().buffered(65_536).use { out ->
                            if (isTarAsset || isGzip) {
                                Log.i(TAG, "Extracting TAR archive")
                                bytesExtracted = extractFromTar(input, out) { written ->
                                    if (totalBytes > 0)
                                        onProgress((written.toFloat() / totalBytes).coerceIn(0.01f, 0.99f))
                                }
                            } else if (isZipAsset) {
                                Log.i(TAG, "Extracting ZIP archive")
                                ZipInputStream(input).use { zis ->
                                    var entry = zis.nextEntry
                                    while (entry != null && entry.isDirectory) {
                                        zis.closeEntry()
                                        entry = zis.nextEntry
                                    }
                                    checkNotNull(entry) { "No file found inside ${release.assetName}" }
                                    Log.i(TAG, "ZIP entry: ${entry.name}")
                                    val buf = ByteArray(65_536)
                                    var n: Int
                                    while (zis.read(buf).also { n = it } != -1) {
                                        out.write(buf, 0, n)
                                        bytesExtracted += n
                                        if (totalBytes > 0)
                                            onProgress((bytesExtracted.toFloat() / totalBytes).coerceIn(0.01f, 0.99f))
                                    }
                                }
                            } else {
                                // Unknown format — try TAR as best guess
                                Log.w(TAG, "Unknown archive format for ${release.assetName}, attempting TAR")
                                bytesExtracted = extractFromTar(input, out) { written ->
                                    if (totalBytes > 0)
                                        onProgress((written.toFloat() / totalBytes).coerceIn(0.01f, 0.99f))
                                }
                            }
                        }
                    }
                } finally {
                    conn.disconnect()
                }

                Log.i(TAG, "Extracted $bytesExtracted bytes to ${tmpFile.absolutePath}")
                check(tmpFile.exists() && tmpFile.length() > 100_000) {
                    "Extracted file too small (${tmpFile.length()} bytes) — expected Stockfish binary > 100 KB"
                }
                if (destFile.exists()) destFile.delete()
                check(tmpFile.renameTo(destFile)) { "Failed to rename tmp -> dest" }
                destFile.setExecutable(true, false)
                Log.i(TAG, "Installed ${release.tagName} -> ${destFile.absolutePath} (${destFile.length()} bytes)")
                saveInstalledVersion(release.tagName)
                onProgress(1f)
                destFile
            }
        }

    /**
     * Extracts the first regular file from a plain (uncompressed) TAR stream.
     *
     * TAR format: each entry = 512-byte header + file data padded to 512-byte blocks.
     * File size is at header offset 124, 12 bytes, stored as ASCII octal (NUL/space padded).
     * File type is at header offset 156: '0' or NUL = regular file, '5' = directory, etc.
     *
     * GNU TAR may encode sizes > 8 GB using base-256 (first byte 0x80 or 0xFF).
     * PAX extended headers (type 'x'/'g') and GNU long-name entries (type 'L'/'K')
     * are skipped: their data blocks are drained using the size from their own header.
     */
    private fun extractFromTar(
        input: InputStream,
        output: OutputStream,
        onProgress: (Long) -> Unit
    ): Long {
        val header = ByteArray(512)
        var totalWritten = 0L

        fun readFully(buf: ByteArray): Boolean {
            var off = 0
            while (off < buf.size) {
                val n = input.read(buf, off, buf.size - off)
                if (n == -1) return false
                off += n
            }
            return true
        }

        fun drainBytes(count: Long) {
            var rem = count
            val skip = ByteArray(65_536)
            while (rem > 0) {
                val n = input.read(skip, 0, minOf(skip.size.toLong(), rem).toInt())
                if (n == -1) break
                rem -= n
            }
        }

        // Parses a 12-byte TAR size/offset field at [offset] in [header] without string conversion.
        // Handles GNU base-256 encoding (first byte 0x80/0xFF) and NUL/space-padded ASCII octal.
        // Never throws — unknown bytes are silently skipped.
        fun parseTarSize(offset: Int): Long {
            val first = header[offset].toInt() and 0xFF
            if (first == 0x80 || first == 0xFF) {
                // GNU base-256: bytes 1..11 are big-endian binary
                var result = 0L
                for (i in 1..11) result = (result shl 8) or (header[offset + i].toLong() and 0xFF)
                return result
            }
            // Standard ASCII octal: skip NUL (0x00) and space (0x20) padding bytes
            var result = 0L
            for (i in 0..11) {
                val b = header[offset + i].toInt() and 0xFF
                if (b == 0 || b == 0x20) continue       // NUL or space: padding
                if (b < 0x30 || b > 0x37) continue      // not '0'..'7': unexpected byte, skip
                result = result * 8L + (b - 0x30)
            }
            return result
        }

        while (true) {
            if (!readFully(header)) break
            // End-of-archive sentinel: two consecutive all-zero 512-byte blocks
            if (header.all { it == 0.toByte() }) break

            val fileSize = parseTarSize(124)
            val fileType = header[156].toInt() and 0xFF
            val fileName = String(header, 0, 100, Charsets.US_ASCII).trimEnd(' ', ' ')
            // '0' (0x30) or NUL (0x00) = regular file; both appear in real-world TARs
            val isRegular = fileType == 0x30 || fileType == 0x00
            // Padded size rounds up to nearest 512-byte block
            val paddedSize = if (fileSize == 0L) 0L else ((fileSize + 511L) / 512L) * 512L

            Log.d(TAG, "TAR entry: '$fileName' type=0x${fileType.toString(16)} size=$fileSize regular=$isRegular")

            if (isRegular && fileSize > 0) {
                val buf = ByteArray(65_536)
                var rem = fileSize
                while (rem > 0) {
                    val toRead = minOf(buf.size.toLong(), rem).toInt()
                    val n = input.read(buf, 0, toRead)
                    if (n == -1) break
                    output.write(buf, 0, n)
                    rem -= n
                    totalWritten += n
                    onProgress(totalWritten)
                }
                val padding = paddedSize - fileSize
                if (padding > 0) drainBytes(padding)
                Log.i(TAG, "TAR: extracted '$fileName' ($totalWritten bytes)")
                return totalWritten
            } else {
                // Directory, symlink, PAX header, GNU long-name, etc. — drain and continue
                drainBytes(paddedSize)
            }
        }
        return totalWritten
    }

    // ── Installed binary path ────────────────────────────────────────────────

    fun getInstalledBinaryFile(): File =
        File(File(context.filesDir, "engine"), "stockfish")

    // ── Custom binary installation ────────────────────────────────────────────

    fun installCustomBinary(context: Context, uri: android.net.Uri): Result<File> = runCatching {
        val engineDir = File(context.filesDir, "engine").also { it.mkdirs() }
        val destFile = File(engineDir, "stockfish")
        val tmpFile = File(engineDir, "stockfish_custom.tmp")

        context.contentResolver.openInputStream(uri)?.use { input ->
            tmpFile.outputStream().use { out -> input.copyTo(out) }
        } ?: error("Cannot open selected file")

        // Validate ELF magic
        val magic = ByteArray(4)
        tmpFile.inputStream().use { it.read(magic) }
        check(
            magic[0] == 0x7f.toByte() &&
            magic[1] == 0x45.toByte() &&
            magic[2] == 0x4c.toByte() &&
            magic[3] == 0x46.toByte()
        ) { "Selected file is not a valid ELF binary" }

        if (destFile.exists()) destFile.delete()
        check(tmpFile.renameTo(destFile)) { "Failed to install custom binary" }
        destFile.setExecutable(true, false)
        saveInstalledVersion("custom")
        destFile
    }
}
