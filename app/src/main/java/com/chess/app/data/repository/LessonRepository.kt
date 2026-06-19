package com.chess.app.data.repository

import com.chess.app.data.model.Lesson

class LessonRepository {
    fun getAllLessons(): List<Lesson> = LESSONS

    fun getLessonById(id: Int): Lesson? = LESSONS.find { it.id == id }
}

val LESSONS = listOf(
    Lesson(
        id = 1,
        title = "Opening Principles",
        subtitle = "Build a strong foundation",
        iconText = "♙",
        difficulty = "Beginner",
        content = """
# Opening Principles

The opening phase sets the tone for the entire game. Follow these key principles:

## 1. Control the Center
The center squares (e4, d4, e5, d5) are the most important squares on the board. Control them with pawns and pieces.

**Good:** Place pawns on e4 and d4 (or e5/d5 for Black)
**Why:** Center control gives your pieces more space and mobility

## 2. Develop Your Pieces
Move your knights and bishops to active squares early.

**Rule:** Develop a new piece with each move in the opening
**Priority:** Knights before bishops, develop toward the center

## 3. King Safety — Castle Early
Your king is vulnerable in the center. Castle to safety on the kingside or queenside.

**Goal:** Castle within the first 10 moves
**Benefit:** Connects your rooks and tucks the king away

## 4. Don't Move the Same Piece Twice
Each move should develop a new piece unless you must react to a threat.

## 5. Don't Bring Your Queen Out Early
The queen can be attacked and forced to retreat, losing tempo.

## The Three Opening Goals
1. Control the center
2. Develop all pieces
3. Castle for king safety

**Practice:** Try the Italian Game: 1.e4 e5 2.Nf3 Nc6 3.Bc4
        """.trimIndent(),
        position = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
    ),
    Lesson(
        id = 2,
        title = "Piece Values",
        subtitle = "Know what your pieces are worth",
        iconText = "♛",
        difficulty = "Beginner",
        content = """
# Piece Values

Understanding how much each piece is worth helps you make good trades.

## Standard Values

| Piece | Symbol | Value |
|-------|--------|-------|
| Pawn | ♙ | 1 point |
| Knight | ♘ | 3 points |
| Bishop | ♗ | 3 points |
| Rook | ♖ | 5 points |
| Queen | ♕ | 9 points |
| King | ♔ | Infinite (can't be traded!) |

## Good Trades vs Bad Trades

**Good trades:** Trading pieces of equal or lesser value
- Rook (5) for Bishop (3) + Knight (3) = winning a pawn worth of material

**Bad trades:** Giving up more than you get
- Queen (9) for Rook (5) = losing 4 points

## Positional Value Adjustments

Pieces can be worth more or less depending on the position:

**Bishops** are stronger in open positions with long diagonals
**Knights** are stronger in closed positions and near the center
**Rooks** become much stronger in the endgame with open files

## The Bishop Pair
Two bishops working together are often worth more than a bishop + knight or two knights (approximately +0.5 pawns)

## Remember
- Don't sacrifice material without good reason (attack, checkmate, winning back more)
- "When in doubt, don't" — avoid risky sacrifices without a clear plan
        """.trimIndent()
    ),
    Lesson(
        id = 3,
        title = "Castling",
        subtitle = "Protect your king",
        iconText = "♖",
        difficulty = "Beginner",
        content = """
# Castling

Castling is a special move that simultaneously moves your king to safety and activates a rook.

## How to Castle

**Kingside Castling (0-0):**
- King moves from e1 to g1 (White) or e8 to g8 (Black)
- Rook moves from h1 to f1 (White) or h8 to f8 (Black)
- All squares between them must be empty

**Queenside Castling (0-0-0):**
- King moves from e1 to c1 (White) or e8 to c8 (Black)
- Rook moves from a1 to d1 (White) or a8 to d8 (Black)
- All squares between them must be empty

## When You CANNOT Castle

1. **King has moved** — even if it moved back
2. **Rook has moved** — that specific rook cannot be used
3. **Squares are occupied** — clear the path first
4. **King is in check** — you must get out of check another way
5. **King passes through check** — the transit squares must be safe
6. **King ends up in check** — can't castle into check

## When Should You Castle?

**Castle early** (moves 5-10) for king safety
**Choose kingside** when your queenside pieces aren't developed
**Choose queenside** for aggressive counter-play (more advanced)

## Benefits of Castling
1. King safety — away from center
2. Rook activation — connects to the center/open files
3. Both achieved in one move!

## Tip
In most games, **castle kingside** as it requires moving fewer pieces out of the way.
        """.trimIndent(),
        position = "r1bqk2r/pppp1ppp/2n2n2/2b1p3/2B1P3/2NP1N2/PPP2PPP/R1BQK2R w KQkq - 2 5"
    ),
    Lesson(
        id = 4,
        title = "Pins and Skewers",
        subtitle = "Powerful tactical weapons",
        iconText = "♗",
        difficulty = "Intermediate",
        content = """
# Pins and Skewers

These are two of the most common tactical motifs in chess.

## The Pin

A pin occurs when a piece cannot move because doing so would expose a more valuable piece behind it to attack.

### Absolute Pin
The piece cannot legally move because moving it would expose the king to check.
**Example:** Bishop on b5 pins a knight on c6 to the king on e8

### Relative Pin
The piece could move, but doing so would lose a more valuable piece behind it.
**Example:** Bishop pins a knight to the queen

### How to Exploit a Pin
1. **Attack the pinned piece** with a less valuable piece
2. **Pile up pressure** on the pinned piece
3. **Break the pin** with your own pieces if you're the victim

## The Skewer

A skewer is like a reverse pin — it attacks a valuable piece, forcing it to move and exposing a less valuable piece behind it.

**Example:** Rook attacks the king, king must move, rook takes the rook behind it

### Types of Skewers
- **Absolute skewer:** Forces the king to move
- **Relative skewer:** Forces any valuable piece to move

## Key Pieces for Pins/Skewers
- **Bishops** — Pin/skewer along diagonals
- **Rooks** — Pin/skewer along ranks and files
- **Queens** — Can do both!
- **Knights** — Cannot create pins or skewers (they don't attack in lines)

## Defending Against Pins
1. **Break the pin** by interposing a piece
2. **Attack the pinning piece**
3. **Move the piece being shielded** (if it's not the king)
4. **Counter-attack** elsewhere on the board
        """.trimIndent()
    ),
    Lesson(
        id = 5,
        title = "Forks",
        subtitle = "Attack two pieces at once",
        iconText = "♘",
        difficulty = "Intermediate",
        content = """
# Forks

A fork is when one piece attacks two or more enemy pieces simultaneously. Since your opponent can only save one piece per move, you win material!

## The Knight Fork

Knights are the most common forking pieces because their L-shaped movement makes their attacks hard to see.

**Classic knight fork:**
- Knight jumps to a square attacking both king and queen
- Opponent must move the king
- Knight captures the queen!

### Knight Fork Patterns to Remember
- **Royal fork:** Attacks king and queen simultaneously
- **Family fork:** Attacks king, queen, and rook at the same time

## The Pawn Fork

Pawns can fork two pieces side by side on the 4th rank (if you're White).

**Example:** Pawn on e5 attacks both d6 and f6

## The Bishop/Rook/Queen Fork

Any piece can fork, but it's less common with longer-range pieces since they're easier to spot.

## How to Set Up a Fork

1. **Look for knight fork squares** — where could your knight jump to attack two pieces?
2. **Drive pieces to forked positions** — use your moves to position enemy pieces
3. **Look for forcing moves** — checks and threats that force pieces where you want them

## Defending Against Forks

1. **Don't place two valuable pieces on squares a knight can attack from one square**
2. **Watch for knight checks** — often used to set up forks
3. **Move one of the attacked pieces** to safety before the fork lands

## Practice

Always ask: "Where can my knight jump to attack two pieces at once?"
        """.trimIndent()
    ),
    Lesson(
        id = 6,
        title = "Endgame Basics",
        subtitle = "Convert your advantage to a win",
        iconText = "♔",
        difficulty = "Intermediate",
        content = """
# Endgame Basics

The endgame begins when most pieces have been traded off. Many games are decided in the endgame!

## Key Endgame Principles

### 1. Activate Your King
In the endgame, the king becomes a powerful fighting piece!
- **Rush your king to the center** or toward passed pawns
- The king helps escort pawns to promotion

### 2. Passed Pawns Are Powerful
A passed pawn (no enemy pawns blocking or capturing it) can become a queen!
- **Create passed pawns** by trading off the blocking pawns
- **Push passed pawns** toward promotion
- **A passed pawn on the 7th rank** is usually worth a rook

### 3. Rook Behind Passed Pawns
- **Your rooks** should go behind your passed pawns (pushing them forward)
- **Enemy rooks** should go behind enemy passed pawns (stopping them)

## Basic Checkmates

### King and Queen vs King
Relatively easy — drive the enemy king to the corner.

### King and Rook vs King
The "box method" — use the rook to shrink the box, then deliver checkmate.

### King and Two Rooks vs King
Even easier — use the "lawnmower" technique.

## Key Endgame Positions

### The Opposition
When kings face each other with one square between them, the player NOT to move has the "opposition" — a key advantage in king-pawn endgames.

### The Rule of the Square
Draw a square from a passed pawn to the promotion square. If the enemy king can enter this square, it can catch the pawn!

### Zugzwang
A position where whoever has to move is at a disadvantage. Common in king-pawn endgames.

## Endgame Study Tips
- Practice king and pawn endgames — they appear in most games
- Learn basic checkmate patterns
- Study rook endgames — they're the most common type
        """.trimIndent()
    ),
    Lesson(
        id = 7,
        title = "Tactics: Combinations",
        subtitle = "Calculate multiple moves ahead",
        iconText = "♕",
        difficulty = "Intermediate",
        content = """
# Tactical Combinations

A combination is a sequence of moves (often involving sacrifices) that leads to a concrete advantage.

## Building Blocks of Combinations

### Forcing Moves
**Checks, captures, and threats** force your opponent's response, allowing you to calculate further ahead.

When calculating, always start with **checks**, then **captures**, then **threats**.

## Common Combination Types

### The Double Check
Two pieces check the king simultaneously. The only escape is to move the king (can't block both checks).

### Discovered Attack (Discovery)
Moving one piece reveals an attack by another piece behind it.
**Extra powerful:** Discovered check!

### Deflection
Forcing a key defensive piece to leave its post.
**Example:** "If I attack your queen, it can't guard that rook anymore!"

### Decoy
Luring a piece to a bad square where it can be attacked.
**Example:** Sacrificing material to bring the king to a mating square.

### Interference
Blocking a defensive piece's line of action.

## How to Find Combinations

1. **Identify the target** — what do you want to win or achieve?
2. **Look for motifs** — pin, fork, skewer, double check near the target
3. **Work backwards** — if X is checkmate, what allows X?
4. **Calculate forcing lines** — follow checks and captures
5. **Verify** — make sure your combination works and you haven't missed a defense

## The Two-Step Method
1. "If I do THIS, opponent MUST do THAT"
2. "After THAT, I can do THIS FINAL MOVE for the win"

## Practice Makes Perfect
Solve puzzles daily! Even 10 minutes of puzzle solving dramatically improves your tactical vision.
        """.trimIndent()
    ),
    Lesson(
        id = 8,
        title = "Pawn Structure",
        subtitle = "The skeleton of your position",
        iconText = "♙",
        difficulty = "Advanced",
        content = """
# Pawn Structure

Pawns cannot move backwards, so pawn structure decisions are permanent. Understanding pawn structure is key to strategic chess.

## Types of Pawns

### Passed Pawn
No enemy pawns can block or capture it on its path to promotion.
**Value:** Can become a queen! Push it forward.

### Isolated Pawn
No friendly pawns on adjacent files to protect it.
**Weakness:** Must be defended by pieces, which limits their activity.

### Doubled Pawns
Two pawns of the same color on the same file.
**Usually a weakness:** Can't protect each other, may block each other.

### Backward Pawn
Cannot be protected by other pawns, and advancing would put it en prise.
**Weakness:** Especially weak if it's on a half-open file.

## Pawn Majorities

A **pawn majority** on one side of the board can create a passed pawn.
**Example:** 3 vs 2 majority on the queenside can generate a passed pawn.

## Key Pawn Structures

### The IQP (Isolated Queen Pawn)
- Pawn on d4 with no c or e pawns
- **Pro:** Active pieces, space in the center
- **Con:** Weak pawn that needs defending

### Hanging Pawns
- Two adjacent center pawns (like c5 and d5) without supporting pawns
- **Pro:** Control space, can advance
- **Con:** Both can become targets

### Pawn Chains
- A diagonal chain of pawns protecting each other
- **Attack the base** of the chain to undermine it

## Practical Advice

1. **Avoid doubled, isolated pawns** when possible
2. **Create passed pawns** as the game enters the endgame
3. **Use pawn breaks** to open lines for your pieces
4. **Study the pawn structure** before making strategic plans — let it guide you
        """.trimIndent()
    ),
    Lesson(
        id = 9,
        title = "Positional Play",
        subtitle = "Strategy beyond tactics",
        iconText = "♗",
        difficulty = "Advanced",
        content = """
# Positional Play

While tactics are about calculating specific moves, positional play is about improving your pieces, controlling key squares, and building long-term advantages.

## Outposts

An **outpost** is a square deep in enemy territory that cannot be attacked by enemy pawns.
- Knights on outposts are especially powerful
- A knight on d5 or e5 (or d4/e4 for Black) is often dominant

## Open Files and Ranks

- **Open file:** No pawns of either color
- **Half-open file:** No friendly pawns (enemy pawn may be there)

**Rooks belong on open files!** They need open lines to be active.

## The 7th Rank

Rooks on the 7th rank are incredibly powerful:
- Attack all enemy pawns that haven't moved
- Restrict the enemy king
- Two rooks on the 7th = "pig rooks" (they eat everything!)

## Weak Squares

A **weak square** is one that can't be defended by pawns.
- Often created by advancing pawns (can't go back!)
- Occupy weak squares with your pieces

## Piece Activity

**The most important positional principle:** Keep your pieces active!

- Active piece > passive piece, even if the passive piece is "safe"
- Ask: "What is my worst-placed piece? How do I improve it?"

## Prophylaxis

**Preventing your opponent's plans** before they become threats.
- Ask: "What does my opponent want to do?"
- Make moves that stop their best plan while improving your position

## Imbalances

Chess is a game of imbalances. Common ones:
- **Bishop vs Knight** (depends on pawn structure)
- **Two bishops vs bishop + knight**
- **Space advantage** (more space = more mobility)
- **Pawn structure** (passed pawns, weaknesses)

Use your imbalances to form a plan!
        """.trimIndent()
    ),
    Lesson(
        id = 10,
        title = "Checkmate Patterns",
        subtitle = "Finish the game in style",
        iconText = "♚",
        difficulty = "Intermediate",
        content = """
# Checkmate Patterns

Recognizing checkmate patterns allows you to spot winning opportunities instantly!

## Scholar's Mate
**4-move checkmate** (for beginners to watch out for!)
1. e4 e5 2. Bc4 Nc6 3. Qh5 Nf6?? 4. Qxf7#

**Defense:** Play 3...g6! to attack the queen.

## Back Rank Mate
The king is trapped on the back rank by its own pawns.
- A rook or queen delivers check on the 1st (or 8th) rank
- No escape!

**Defense:** Create a "luft" (escape square) with h3 or g3 (or h6/g6 for Black).

## Smothered Mate
A knight delivers checkmate to a king surrounded by its own pieces.
- Classic pattern: Qg8+! Rxg8 Nf7#

## Anastasia's Mate
Knight + Rook combination where the knight cuts off escape squares.

## Arabian Mate
Knight + Rook deliver checkmate in a corner.

## Boden's Mate
Two bishops delivering checkmate on crossing diagonals when the king's escape squares are blocked by its own pieces.

## Ladder Mate (Lawnmower)
Two rooks alternately check the king, pushing it to the edge row by row.

## Epaulette Mate
The king is checkmated in the center, with pieces (usually its own rooks) blocking escape on both sides.

## How to Recognize Checkmate Opportunities

1. **Is the enemy king trapped?** — by the edge, by pieces, by pawns
2. **Are there escape squares?** — can all be covered?
3. **Do you have enough firepower?** — attacking pieces in the area
4. **Is there a sequence?** — calculate check by check

## Practical Tip
In every tactical position, ask: "Can I checkmate the king in the next 2-3 moves?" Before looking for material wins, look for checkmate!
        """.trimIndent()
    )
)
