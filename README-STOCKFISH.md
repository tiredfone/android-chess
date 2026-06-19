# Stockfish Engine Setup

The chess app uses the Stockfish chess engine for bot opponents and move analysis.
The Stockfish binary is **not included** in this repository and must be downloaded separately.

## Download Instructions

### Step 1: Download Stockfish for Android

1. Go to the [official Stockfish GitHub releases page](https://github.com/official-stockfish/Stockfish/releases)
2. Download the latest release for Android (ARM64)
3. Look for a file named `stockfish-android-armv8.zip` or similar

Alternatively, you can build Stockfish from source for Android:
- Clone: `git clone https://github.com/official-stockfish/Stockfish.git`
- Follow the Android build instructions in their README

### Step 2: Place the Binary

Place the Stockfish binary at:
```
app/src/main/assets/engine/stockfish
```

The binary must be named exactly `stockfish` (no extension).

### Step 3: Verify

The app will automatically:
1. Copy the binary to the app's private files directory on first launch
2. Set the execute permission (`chmod +x`)
3. Start the engine via the UCI protocol

## Without the Binary

If the Stockfish binary is not present, the app will still work but bots will
make **random legal moves** instead of intelligent ones. All other features
(puzzles, lessons, game review) work without the engine.

## UCI Protocol

The app communicates with Stockfish using the Universal Chess Interface (UCI) protocol:

- `uci` — Initialize UCI mode
- `setoption name UCI_LimitStrength value true` — Enable ELO limiting
- `setoption name UCI_Elo value <elo>` — Set the bot's playing strength
- `position fen <fen>` — Set the current position
- `go movetime <ms>` — Request the best move with a time limit
- `quit` — Shut down the engine

## ELO Configuration

Each bot uses `UCI_LimitStrength` to play at a specific ELO level:

| Bot | ELO |
|-----|-----|
| Martin | 200 |
| Ana | 400 |
| Max | 600 |
| Sofia | 800 |
| Carlos | 1000 |
| Lily | 1200 |
| Daniel | 1400 |
| Emma | 1500 |
| Noah | 1600 |
| Olivia | 1700 |
| Lucas | 1800 |
| Mia | 1900 |
| Ethan | 2000 |
| Aiden | 2200 |
| Isabella | 2300 |
| James | 2400 |
| Charlotte | 2500 |
| Alexander | 2700 |
| Hikaru | 2900 |
| Magnus | 3250 |

## Supported Architectures

Stockfish binaries are available for:
- `arm64-v8a` (recommended for modern Android phones)
- `armeabi-v7a` (older 32-bit ARM devices)
- `x86_64` (emulators)

For a production app, you would typically ship multiple ABI-specific binaries
using Android's ABI splits in `build.gradle`.
