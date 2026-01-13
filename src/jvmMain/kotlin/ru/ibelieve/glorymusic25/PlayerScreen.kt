package ru.ibelieve.glorymusic25

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    songId: Int,
    mode: PlayMode,
    isPlaying: Boolean,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onPlaybackComplete: () -> Unit
) {
    val rawText = remember(songId) { FileLogic.getSongTextRaw(songId) }
    val fragments = remember(rawText) { parseSongText(rawText) }
    var currentTime by remember { mutableStateOf(0) }

    // сброс времени при новой песне
    LaunchedEffect(songId) { currentTime = 0 }

    // логика воспроизведения аудио
    LaunchedEffect(isPlaying, mode) {
        if (mode != PlayMode.TEXT) {
            if (isPlaying) {
                // Проверка и получение пути к файлу
                val path = FileLogic.getAudioFilePath(songId, mode)
                println("Получили путь к файлу: $path") // Логирование

                if (path != null) {
                    AudioPlayer.play(path)
                } else {
                    println("Файл не найден!") // Сообщение о невозможности найти файл
                }
            } else {
                AudioPlayer.pause()
            }
        }
    }

    // авто-прокрутка текста по таймеру (для PLUS/MINUS)
    LaunchedEffect(isPlaying, mode, currentIndex) {
        if (isPlaying && mode != PlayMode.TEXT) {
            while (true) {
                delay(1000)
                currentTime++
                val currentFragment = fragments.getOrNull(currentIndex)
                if (currentFragment != null) {
                    if (currentTime >= currentFragment.timestamp && !currentFragment.isLast) {
                        if (currentIndex < fragments.size - 1) onIndexChange(currentIndex + 1)
                    } else if (currentFragment.isLast && currentTime >= currentFragment.timestamp) {
                        onPlaybackComplete()
                        break
                    }
                }
            }
        }
    }

    // отображение текста на экране
    Box(modifier = Modifier.fillMaxSize().background(Color.Black).padding(40.dp), contentAlignment = Alignment.Center) {
        Text(
            text = fragments.getOrNull(currentIndex)?.text ?: "",
            fontSize = 90.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 120.sp
        )
    }
}

fun parseSongText(raw: String): List<SongFragment> {
    if (raw.isBlank()) return emptyList()
    val fragments = mutableListOf<SongFragment>()
    val regex = "([\\s\\S]+?)([\\*\\+])(\\d+)".toRegex()
    val matches = regex.findAll(raw)
    if (matches.none()) return listOf(SongFragment(raw.trim(), 9999, true))
    for (match in matches) {
        val textPart = match.groups[1]?.value?.trim() ?: ""
        val symbol = match.groups[2]?.value ?: ""
        val time = match.groups[3]?.value?.toInt() ?: 0
        fragments.add(SongFragment(textPart, time, symbol == "+"))
    }
    return fragments
}