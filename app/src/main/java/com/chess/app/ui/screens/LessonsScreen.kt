package com.chess.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chess.app.data.model.Lesson
import com.chess.app.data.repository.LESSONS
import com.chess.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonsScreen(
    onBack: () -> Unit
) {
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }

    if (selectedLesson == null) {
        // Lesson list
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Chess Lessons",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    Text(
                        text = "Learn chess from the basics to advanced concepts.",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(LESSONS) { lesson ->
                    LessonCard(
                        lesson = lesson,
                        onClick = { selectedLesson = lesson }
                    )
                }
            }
        }
    } else {
        // Lesson detail view
        val lesson = selectedLesson!!

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = lesson.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = lesson.difficulty,
                                color = ChessGreen,
                                fontSize = 12.sp
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedLesson = null }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back to lessons",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Lesson content rendered as markdown-ish text
                LessonContent(content = lesson.content)

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation between lessons
                val currentIndex = LESSONS.indexOfFirst { it.id == lesson.id }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentIndex > 0) {
                        OutlinedButton(
                            onClick = { selectedLesson = LESSONS[currentIndex - 1] },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text("← Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (currentIndex < LESSONS.size - 1) {
                        Button(
                            onClick = { selectedLesson = LESSONS[currentIndex + 1] },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChessGreen
                            )
                        ) {
                            Text("Next →")
                        }
                    } else {
                        Button(
                            onClick = { selectedLesson = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ChessGreen
                            )
                        ) {
                            Text("Complete!")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonCard(
    lesson: Lesson,
    onClick: () -> Unit
) {
    val difficultyColor = when (lesson.difficulty) {
        "Beginner" -> Color(0xFF4CAF50)
        "Intermediate" -> Color(0xFFFF9800)
        "Advanced" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(difficultyColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lesson.iconText,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lesson.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(difficultyColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = lesson.difficulty,
                            color = difficultyColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.subtitle,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }

            Text(
                text = "${lesson.id}/${LESSONS.size}",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun LessonContent(content: String) {
    val lines = content.lines()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            when {
                line.startsWith("# ") -> {
                    Text(
                        text = line.removePrefix("# "),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                line.startsWith("## ") -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = line.removePrefix("## "),
                        color = ChessGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.removePrefix("### "),
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                line.startsWith("**") && line.endsWith("**") -> {
                    Text(
                        text = line.removeSurrounding("**"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                line.startsWith("- ") -> {
                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = ChessGreen,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                        )
                        Text(
                            text = line.removePrefix("- "),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp
                        )
                    }
                }
                line.startsWith("|") -> {
                    // Simple table row
                    if (!line.contains("---")) {
                        val cells = line.split("|").filter { it.isNotBlank() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface)
                                .padding(4.dp)
                        ) {
                            cells.forEach { cell ->
                                Text(
                                    text = cell.trim(),
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 13.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
                line.isEmpty() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                else -> {
                    // Handle inline bold
                    val processedText = line
                        .replace("**", "")
                        .replace("*", "")
                    Text(
                        text = processedText,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
