package com.chess.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.sin
import kotlin.random.Random

enum class BoardBackground(val displayName: String) {
    CLASSIC("Classic"),
    SPACE("Space"),
    FOREST("Forest"),
    OCEAN("Ocean"),
    CATS("Cats"),
    SUNSET("Sunset")
}

@Composable
fun ThemeBackground(theme: BoardBackground, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        when (theme) {
            BoardBackground.SPACE -> drawSpaceBackground()
            BoardBackground.FOREST -> drawForestBackground()
            BoardBackground.OCEAN -> drawOceanBackground()
            BoardBackground.CATS -> drawCatsBackground()
            BoardBackground.SUNSET -> drawSunsetBackground()
            else -> drawRect(Color(0xFF1a1a2e))
        }
    }
}

private fun DrawScope.drawSpaceBackground() {
    // Deep space background
    drawRect(Color(0xFF050518))

    // Stars — deterministic using seed
    val rng = Random(7)
    repeat(150) {
        val x = rng.nextFloat() * size.width
        val y = rng.nextFloat() * size.height
        val radius = rng.nextFloat() * 2f + 0.5f
        val alpha = rng.nextFloat() * 0.7f + 0.3f
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
    }

    // Nebulae — 3 glowing blobs
    val nebulae = listOf(
        Triple(size.width * 0.2f, size.height * 0.3f, Color(0xFFFF00FF)),
        Triple(size.width * 0.75f, size.height * 0.6f, Color(0xFF4444FF)),
        Triple(size.width * 0.5f, size.height * 0.15f, Color(0xFF9900CC))
    )
    nebulae.forEach { (cx, cy, color) ->
        for (ring in 1..5) {
            val radius = size.width * 0.18f * ring / 5f
            val alpha = 0.08f * (6 - ring) / 5f
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = radius,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun DrawScope.drawForestBackground() {
    // Gradient sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1B4332), Color(0xFF081C15)),
            startY = 0f,
            endY = size.height
        )
    )

    // Trees in the bottom third
    val rng = Random(42)
    val treeBaseY = size.height * 0.65f
    val treeCount = 8
    repeat(treeCount) { i ->
        val cx = size.width * (i + 0.5f) / treeCount
        val treeHeight = size.height * (0.15f + rng.nextFloat() * 0.2f)
        val halfWidth = size.width * (0.04f + rng.nextFloat() * 0.03f)
        val topY = treeBaseY - treeHeight
        val alpha = 0.6f + rng.nextFloat() * 0.4f

        val path = Path().apply {
            moveTo(cx, topY)
            lineTo(cx + halfWidth, treeBaseY)
            lineTo(cx - halfWidth, treeBaseY)
            close()
        }
        drawPath(path, color = Color(0xFF2D6A4F).copy(alpha = alpha))
    }

    // Ground
    drawRect(
        color = Color(0xFF052e16),
        topLeft = Offset(0f, treeBaseY),
        size = Size(size.width, size.height - treeBaseY)
    )
}

private fun DrawScope.drawOceanBackground() {
    // Ocean gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0077B6), Color(0xFF03045E)),
            startY = 0f,
            endY = size.height
        )
    )

    // Wave lines
    val waveData = listOf(
        Triple(size.height * 0.35f, 0.3f, Color(0xFF90E0EF)),
        Triple(size.height * 0.50f, 0.4f, Color(0xFF48CAE4)),
        Triple(size.height * 0.65f, 0.5f, Color(0xFF00B4D8)),
        Triple(size.height * 0.80f, 0.6f, Color(0xFF0096C7))
    )
    waveData.forEach { (baseY, alpha, color) ->
        val path = Path()
        val segments = 6
        val segWidth = size.width / segments
        path.moveTo(0f, baseY)
        for (s in 0 until segments) {
            val x1 = s * segWidth + segWidth * 0.25f
            val x2 = s * segWidth + segWidth * 0.75f
            val xEnd = (s + 1) * segWidth
            val ampUp = baseY - size.height * 0.03f
            val ampDown = baseY + size.height * 0.03f
            path.quadraticBezierTo(x1, ampUp, x2, baseY)
            path.quadraticBezierTo(x2 + segWidth * 0.25f, ampDown, xEnd, baseY)
        }
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        drawPath(path, color = color.copy(alpha = alpha))
    }
}

private fun DrawScope.drawCatsBackground() {
    // Soft pink-white background
    drawRect(Color(0xFFFFF0F5))

    // Draw 6 simple cat faces scattered around
    val positions = listOf(
        Offset(size.width * 0.15f, size.height * 0.15f),
        Offset(size.width * 0.80f, size.height * 0.10f),
        Offset(size.width * 0.05f, size.height * 0.55f),
        Offset(size.width * 0.90f, size.height * 0.50f),
        Offset(size.width * 0.25f, size.height * 0.85f),
        Offset(size.width * 0.70f, size.height * 0.80f)
    )

    positions.forEach { center ->
        val r = size.width * 0.065f
        val faceColor = Color(0xFFFFB3C1).copy(alpha = 0.5f)
        val featureColor = Color(0xFFCC7A8A).copy(alpha = 0.6f)

        // Head
        drawCircle(color = faceColor, radius = r, center = center)

        // Ears (triangles)
        val earPath = Path().apply {
            // Left ear
            moveTo(center.x - r * 0.6f, center.y - r * 0.7f)
            lineTo(center.x - r * 1.05f, center.y - r * 1.4f)
            lineTo(center.x - r * 0.1f, center.y - r * 0.9f)
            close()
            // Right ear
            moveTo(center.x + r * 0.6f, center.y - r * 0.7f)
            lineTo(center.x + r * 1.05f, center.y - r * 1.4f)
            lineTo(center.x + r * 0.1f, center.y - r * 0.9f)
            close()
        }
        drawPath(earPath, color = faceColor)

        // Eyes
        drawCircle(color = featureColor, radius = r * 0.15f, center = Offset(center.x - r * 0.3f, center.y - r * 0.1f))
        drawCircle(color = featureColor, radius = r * 0.15f, center = Offset(center.x + r * 0.3f, center.y - r * 0.1f))

        // Nose
        drawCircle(color = featureColor, radius = r * 0.1f, center = Offset(center.x, center.y + r * 0.1f))

        // Whiskers
        val whiskerStroke = Stroke(width = r * 0.04f)
        // Left whiskers
        drawLine(featureColor, Offset(center.x - r * 0.1f, center.y + r * 0.1f), Offset(center.x - r * 0.9f, center.y), whiskerStroke.width)
        drawLine(featureColor, Offset(center.x - r * 0.1f, center.y + r * 0.15f), Offset(center.x - r * 0.9f, center.y + r * 0.25f), whiskerStroke.width)
        // Right whiskers
        drawLine(featureColor, Offset(center.x + r * 0.1f, center.y + r * 0.1f), Offset(center.x + r * 0.9f, center.y), whiskerStroke.width)
        drawLine(featureColor, Offset(center.x + r * 0.1f, center.y + r * 0.15f), Offset(center.x + r * 0.9f, center.y + r * 0.25f), whiskerStroke.width)
    }
}

private fun DrawScope.drawSunsetBackground() {
    // Multi-layer gradient from orange/yellow to deep purple
    val layerColors = listOf(
        Color(0xFFFFD166).copy(alpha = 1f),
        Color(0xFFEF8C4E).copy(alpha = 0.9f),
        Color(0xFFE05D3C).copy(alpha = 0.85f),
        Color(0xFF9B2C5B).copy(alpha = 0.8f),
        Color(0xFF4A1578).copy(alpha = 0.9f),
        Color(0xFF1A0533).copy(alpha = 1f)
    )

    layerColors.forEachIndexed { i, color ->
        val layerHeight = size.height / layerColors.size
        val y = i * layerHeight
        drawRect(
            color = color,
            topLeft = Offset(0f, y),
            size = Size(size.width, layerHeight + 2f)
        )
    }

    // Sun disc
    drawCircle(
        color = Color(0xFFFFE082).copy(alpha = 0.85f),
        radius = size.width * 0.08f,
        center = Offset(size.width * 0.5f, size.height * 0.28f)
    )
    drawCircle(
        color = Color(0xFFFFF9C4).copy(alpha = 0.5f),
        radius = size.width * 0.13f,
        center = Offset(size.width * 0.5f, size.height * 0.28f)
    )
}
