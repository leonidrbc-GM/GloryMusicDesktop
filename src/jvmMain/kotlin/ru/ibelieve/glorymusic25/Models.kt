package ru.ibelieve.glorymusic25

enum class PlayMode {
    PLUS, MINUS, TEXT
}

data class Song(
    val id: Int,
    val title: String,
    val category: String
)

data class SongFragment(
    val text: String,
    val timestamp: Int,
    val isLast: Boolean = false
)