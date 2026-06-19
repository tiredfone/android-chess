package app.tiredfone.droidchess.engine

import android.content.Context
import android.util.Log
import app.tiredfone.droidchess.data.model.StockfishRelease
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── Status sealed class ───────────────────────────────────────────────────────

sealed class StockfishStatus {
    object NotInstalled : StockfishStatus()
    data class Downloading(val progress: Float, val releaseTag: String) : StockfishStatus()
    data class Ready(val version: String) : StockfishStatus()
    data class UpdateAvailable(val currentVersion: String, val newVersion: String) : StockfishStatus()
    data class Error(val message: String) : StockfishStatus()
}

// ── Manager singleton ─────────────────────────────────────────────────────────

object StockfishManager {

    private const val TAG = "StockfishManager"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _status = MutableStateFlow<StockfishStatus>(StockfishStatus.NotInstalled)
    val status: StateFlow<StockfishStatus> = _status.asStateFlow()

    private val _availableVersions = MutableStateFlow<List<StockfishRelease>>(emptyList())
    val availableVersions: StateFlow<List<StockfishRelease>> = _availableVersions.asStateFlow()

    private val _isLoadingVersions = MutableStateFlow(false)
    val isLoadingVersions: StateFlow<Boolean> = _isLoadingVersions.asStateFlow()

    /** Null until the engine binary is ready and the engine has been initialised. */
    var engine: StockfishEngine? = null
        private set

    private lateinit var downloader: StockfishDownloader

    // ── Init ─────────────────────────────────────────────────────────────────

    fun init(context: Context) {
        downloader = StockfishDownloader(context.applicationContext)

        val installedVersion = downloader.getInstalledVersion()
        val binaryFile = downloader.getInstalledBinaryFile()

        if (installedVersion == null || !binaryFile.exists()) {
            Log.i(TAG, "No installed binary found — starting auto-download")
            _status.value = StockfishStatus.NotInstalled
            downloadLatest(context.applicationContext)
        } else {
            Log.i(TAG, "Binary present ($installedVersion) — launching engine and checking for updates")
            // Launch engine immediately with existing binary
            scope.launch {
                launchEngine(context.applicationContext, installedVersion)
            }
            // Background update check (respects 1-hour cache)
            scope.launch {
                checkForUpdate(context.applicationContext, forced = false)
            }
        }
    }

    // ── Download ─────────────────────────────────────────────────────────────

    /**
     * Downloads and installs the latest Stockfish for the current channel.
     * [forceChannel] overrides the saved channel preference for this call.
     */
    fun downloadLatest(context: Context, forceChannel: String? = null) {
        scope.launch {
            val useDevChannel = (forceChannel ?: downloader.getChannel()) == "dev"
            val releaseResult = runCatching { downloader.fetchLatestRelease(useDevChannel) }
                .getOrElse { e ->
                    Log.e(TAG, "fetchLatestRelease failed", e)
                    _status.value = StockfishStatus.Error("Could not reach GitHub: ${e.message}")
                    return@launch
                }

            releaseResult.fold(
                onSuccess = { release ->
                    Log.i(TAG, "Fetched release ${release.tagName} (${release.assetName})")
                    _status.value = StockfishStatus.Downloading(0f, release.tagName)

                    val installResult = downloader.downloadAndInstall(release) { progress ->
                        _status.value = StockfishStatus.Downloading(progress, release.tagName)
                    }

                    installResult.fold(
                        onSuccess = {
                            launchEngine(context, release.tagName)
                        },
                        onFailure = { e ->
                            Log.e(TAG, "downloadAndInstall failed", e)
                            _status.value = StockfishStatus.Error("Download failed: ${e.message}")
                        }
                    )
                },
                onFailure = { e ->
                    Log.e(TAG, "fetchLatestRelease inner failure", e)
                    _status.value = StockfishStatus.Error(e.message ?: "Unknown error")
                }
            )
        }
    }

    // ── Update check ─────────────────────────────────────────────────────────

    /**
     * Checks GitHub for a newer release and emits [StockfishStatus.UpdateAvailable] if found.
     * Respects the 1-hour cache unless [forced] is true.
     */
    fun checkForUpdate(context: Context, forced: Boolean = false) {
        scope.launch {
            if (downloader.shouldSkipCheck(forced)) {
                Log.d(TAG, "Skipping update check (within cache window)")
                return@launch
            }

            val installedVersion = downloader.getInstalledVersion() ?: return@launch
            val useDevChannel = downloader.getChannel() == "dev"

            val result = runCatching { downloader.fetchLatestRelease(useDevChannel) }
                .getOrElse { e ->
                    Log.w(TAG, "Update check network error: ${e.message}")
                    return@launch
                }

            result.fold(
                onSuccess = { release ->
                    if (release.tagName != installedVersion) {
                        Log.i(TAG, "Update available: $installedVersion -> ${release.tagName}")
                        _status.value = StockfishStatus.UpdateAvailable(
                            currentVersion = installedVersion,
                            newVersion = release.tagName
                        )
                    } else {
                        Log.d(TAG, "Already on latest: $installedVersion")
                        // Keep the current Ready status — don't overwrite it
                    }
                },
                onFailure = { e ->
                    Log.w(TAG, "Update check failed: ${e.message}")
                }
            )
        }
    }

    // ── Engine lifecycle ─────────────────────────────────────────────────────

    /**
     * [isRetry] = true means we already tried a safer binary — don't loop again.
     */
    private suspend fun launchEngine(context: Context, version: String, isRetry: Boolean = false) {
        try {
            val newEngine = StockfishEngine(context)
            val ok = newEngine.init()
            if (ok) {
                engine?.destroy()
                engine = newEngine
                _status.value = StockfishStatus.Ready(version)
                Log.i(TAG, "Engine ready: $version")
            } else {
                // init() returns false only when no binary was found at all.
                // Clear the version record so the next launch triggers a fresh download.
                downloader.clearInstalledVersion()
                _status.value = StockfishStatus.Error("Engine binary missing — tap Retry")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Engine process failed to start: ${e.message}", e)
            // Either the binary crashed immediately (SIGILL — wrong CPU extension) or it
            // started but never sent UCI output.  Both indicate a bad binary for this device.
            // Auto-retry once with the baseline (non-dotprod/non-NEON) binary.
            val isEngineFault = e.message?.contains("exited immediately") == true ||
                                e.message?.contains("did not respond") == true
            if (!isRetry && isEngineFault) {
                Log.i(TAG, "ABI crash detected — retrying with safer (baseline) binary")
                downloader.getInstalledBinaryFile().delete()
                downloader.clearInstalledVersion()
                retryWithSaferBinary(context)
            } else {
                _status.value = StockfishStatus.Error("Cannot run engine: ${e.message}")
            }
        }
    }

    private suspend fun retryWithSaferBinary(context: Context) {
        val useDevChannel = downloader.getChannel() == "dev"
        _status.value = StockfishStatus.Downloading(0f, "")

        val releaseResult = runCatching { downloader.fetchLatestRelease(useDevChannel, preferSafer = true) }
            .getOrElse { e ->
                Log.e(TAG, "fetchLatestRelease (safer retry) failed", e)
                _status.value = StockfishStatus.Error("Could not reach GitHub: ${e.message}")
                return
            }

        releaseResult.fold(
            onSuccess = { release ->
                Log.i(TAG, "Safer retry: downloading ${release.assetName}")
                _status.value = StockfishStatus.Downloading(0f, release.tagName)

                val installResult = downloader.downloadAndInstall(release) { progress ->
                    _status.value = StockfishStatus.Downloading(progress, release.tagName)
                }

                installResult.fold(
                    onSuccess = {
                        launchEngine(context, release.tagName, isRetry = true)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "downloadAndInstall (safer retry) failed", e)
                        _status.value = StockfishStatus.Error("Download failed: ${e.message}")
                    }
                )
            },
            onFailure = { e ->
                Log.e(TAG, "fetchLatestRelease (safer retry) inner failure", e)
                _status.value = StockfishStatus.Error(e.message ?: "Unknown error")
            }
        )
    }

    // ── Helpers for ViewModel ─────────────────────────────────────────────────

    fun getInstalledVersion(): String? = downloader.getInstalledVersion()

    fun getChannel(): String = downloader.getChannel()

    fun setChannel(context: Context, channel: String) {
        downloader.setChannel(channel)
    }

    // ── Available versions ────────────────────────────────────────────────────

    fun fetchAvailableVersions(context: Context) {
        scope.launch {
            _isLoadingVersions.value = true
            downloader.fetchAllReleases().fold(
                onSuccess = { _availableVersions.value = it },
                onFailure = { e -> Log.w(TAG, "fetchAllReleases failed: ${e.message}") }
            )
            _isLoadingVersions.value = false
        }
    }

    /** Downloads [release] and starts the engine with it, replacing any current binary. */
    fun downloadVersion(context: Context, release: StockfishRelease) {
        scope.launch {
            _status.value = StockfishStatus.Downloading(0f, release.tagName)
            downloader.downloadAndInstall(release) { progress ->
                _status.value = StockfishStatus.Downloading(progress, release.tagName)
            }.fold(
                onSuccess = { launchEngine(context, release.tagName) },
                onFailure = { e ->
                    Log.e(TAG, "downloadVersion failed: ${e.message}", e)
                    _status.value = StockfishStatus.Error("Download failed: ${e.message}")
                }
            )
        }
    }

    // ── Custom binary ─────────────────────────────────────────────────────────

    fun installCustomBinary(context: Context, uri: android.net.Uri) {
        scope.launch {
            _status.value = StockfishStatus.Downloading(0.5f, "custom")
            val result = downloader.installCustomBinary(context.applicationContext, uri)
            result.fold(
                onSuccess = { launchEngine(context.applicationContext, "custom") },
                onFailure = { e ->
                    Log.e(TAG, "Custom binary install failed: ${e.message}", e)
                    _status.value = StockfishStatus.Error("Custom binary error: ${e.message}")
                }
            )
        }
    }
}
