# Stockfish Engine Setup

The app uses Stockfish for bot AI and move analysis. The binary is **not included** in this repo — download it separately from the official releases.

## The 4 Android variants

Stockfish ships four Android binaries. The app picks the best one automatically at runtime by checking `Build.SUPPORTED_ABIS` and `/proc/cpuinfo`:

| Variant | Folder to place it in | Best for |
|---|---|---|
| `stockfish-android-armv8-dotprod` | `app/src/main/assets/engine/arm64-v8a-dotprod/` | ARM64 phones with dot-product extension (most phones since ~2019) |
| `stockfish-android-armv8` | `app/src/main/assets/engine/arm64-v8a/` | ARM64 phones without dotprod (older devices) |
| `stockfish-android-armv7-neon` | `app/src/main/assets/engine/armeabi-v7a-neon/` | 32-bit ARMv7 phones with NEON (most ARMv7 devices) |
| `stockfish-android-armv7` | `app/src/main/assets/engine/armeabi-v7a/` | 32-bit ARMv7 phones without NEON |

**You only need the variants you want to support.** For a modern release, just `arm64-v8a-dotprod` + `arm64-v8a` covers virtually all current Android phones. Add the `armeabi-v7a` variants only if you need to support very old (pre-2015) devices.

## Step 1 — Download

1. Go to [github.com/official-stockfish/Stockfish/releases/latest](https://github.com/official-stockfish/Stockfish/releases/latest)
2. Download the Android zip(s) you want, e.g.:
   - `stockfish-android-armv8-dotprod.zip`
   - `stockfish-android-armv8.zip`

## Step 2 — Place the binaries

Each zip contains a single executable. Place it like this (the file must be named exactly `stockfish` with no extension):

```
app/src/main/assets/engine/
├── arm64-v8a-dotprod/
│   └── stockfish          ← from stockfish-android-armv8-dotprod.zip
├── arm64-v8a/
│   └── stockfish          ← from stockfish-android-armv8.zip
├── armeabi-v7a-neon/
│   └── stockfish          ← from stockfish-android-armv7-neon.zip
└── armeabi-v7a/
    └── stockfish          ← from stockfish-android-armv7.zip
```

## Step 3 — How selection works at runtime

`StockfishEngine.selectAssetPath()` runs this logic on first launch:

1. Iterates `Build.SUPPORTED_ABIS` (device's preferred ABI order, e.g. `["arm64-v8a", "armeabi-v7a"]`)
2. For `arm64-v8a`: prefers `arm64-v8a-dotprod` if `/proc/cpuinfo` reports `asimddp`, otherwise `arm64-v8a`
3. For `armeabi-v7a`: prefers `armeabi-v7a-neon` if `/proc/cpuinfo` reports `asimd`/`neon`, otherwise `armeabi-v7a`
4. Skips any folder whose asset file doesn't exist — missing variants are silently skipped
5. Copies the chosen binary to the app's private files directory and `chmod +x`s it

## Without the binary

If no binary is found the app still works — bots fall back to random legal moves. Puzzles, lessons, and the game board all work normally without the engine.

## UCI options used

```
setoption name UCI_LimitStrength value true
setoption name UCI_Elo value <elo>   # 200-3250 depending on chosen bot
```

## ELO reference

| Bot | ELO | Bot | ELO |
|-----|-----|-----|-----|
| Martin | 200 | Emma | 1500 |
| Ana | 400 | Noah | 1600 |
| Max | 600 | Olivia | 1700 |
| Sofia | 800 | Lucas | 1800 |
| Carlos | 1000 | Mia | 1900 |
| Lily | 1200 | Ethan | 2000 |
| Daniel | 1400 | Aiden | 2200 |
| Isabella | 2300 | James | 2400 |
| Charlotte | 2500 | Alexander | 2700 |
| Hikaru | 2900 | Magnus | 3250 |
