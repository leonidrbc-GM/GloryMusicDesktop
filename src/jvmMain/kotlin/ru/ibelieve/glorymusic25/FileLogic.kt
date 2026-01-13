package ru.ibelieve.glorymusic25

import java.io.File
import java.awt.GraphicsEnvironment
import java.awt.Rectangle

data class SongAvailability(
    val hasText: Boolean,
    val hasPlus: Boolean,
    val hasMinus: Boolean
)

object FileLogic {
    // 1. Пытаемся найти папку music_data рядом с исполняемым файлом
    private val localDataRoot = File(System.getProperty("user.dir"), "music_data")

    // 2. Если рядом нет, определяем путь в Документах пользователя (для установленной версии)
    private val userHomeRoot = File(System.getProperty("user.home"), "GloryMusic")

    // Выбираем активный корень
    private val activeAppRoot = if (localDataRoot.exists()) {
        File(System.getProperty("user.dir"))
    } else {
        userHomeRoot
    }

    private val dataRoot = File(activeAppRoot, "music_data")

    init {
        // Создаем папку в Документах при первом запуске, если мы не в портативном режиме
        if (!activeAppRoot.exists()) {
            activeAppRoot.mkdirs()
        }
        if (!dataRoot.exists()) {
            dataRoot.mkdirs()
            // Создаем пустые подпапки для удобства пользователя
            File(dataRoot, "song").mkdirs()
            File(dataRoot, "music/plus").mkdirs()
            File(dataRoot, "music/minus").mkdirs()
        }
    }

    // Получаем границы второго экрана (если он есть)
    fun getSecondaryScreenBounds(): Rectangle? {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screens = ge.screenDevices
        return if (screens.size > 1) {
            screens[1].defaultConfiguration.bounds
        } else {
            null
        }
    }

    fun loadSongs(): List<Song> {
        val listFile = File(dataRoot, "list.txt")
        if (!listFile.exists()) return emptyList()

        return try {
            listFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size >= 3) {
                        Song(parts[0].trim().toInt(), parts[1].trim(), parts[2].trim())
                    } else null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun checkSongAvailability(songId: Int) = SongAvailability(
        hasText = File(dataRoot, "song/$songId.txt").exists(),
        hasPlus = File(dataRoot, "music/plus/$songId.mp3").exists(),
        hasMinus = File(dataRoot, "music/minus/$songId.mp3").exists()
    )

    fun getAudioFilePath(songId: Int, mode: PlayMode): String? {
        val sub = if (mode == PlayMode.PLUS) "plus" else "minus"
        val file = File(dataRoot, "music/$sub/$songId.mp3")
        return if (file.exists()) file.absolutePath else null
    }

    fun getSongTextRaw(songId: Int) =
        File(dataRoot, "song/$songId.txt").let { if (it.exists()) it.readText() else "" }

    fun saveFavorites(ids: Set<Int>) {
        // Изменено: сохраняем в корень приложения (там, где музыка)
        File(activeAppRoot, "favorites.txt").writeText(ids.joinToString(","))
    }

    fun loadFavorites(): Set<Int> {
        val file = File(activeAppRoot, "favorites.txt")
        return if (file.exists()) {
            try {
                file.readText().split(",").filter { it.isNotBlank() }.map { it.toInt() }.toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else emptySet()
    }
}