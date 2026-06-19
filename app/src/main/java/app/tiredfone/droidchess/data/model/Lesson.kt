package app.tiredfone.droidchess.data.model

data class Lesson(
    val id: Int,
    val title: String,
    val subtitle: String,
    val content: String,
    val iconText: String,
    val difficulty: String,
    val position: String? = null // optional FEN position for interactive demo
)
