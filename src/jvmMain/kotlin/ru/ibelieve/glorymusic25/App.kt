package ru.ibelieve.glorymusic25

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    // Состояние окон и плеере
    var isPlayerWindowOpen by remember { mutableStateOf(false) }
    var selectedSongId by remember { mutableStateOf(-1) }
    var selectedPlayMode by remember { mutableStateOf(PlayMode.TEXT) } // объявляем mode здесь

    var isPlaying by remember { mutableStateOf(false) }
    var currentFragmentIndex by remember { mutableStateOf(0) }

    // 1. КОНТЕНТ ГЛАВНОГО ОКНА (Пульт + Список)
    MainScreen(
        isPlayerActive = isPlayerWindowOpen,
        isPlaying = isPlaying,
        mode = selectedPlayMode, // передаем mode в MainScreen
        onTogglePlay = { isPlaying = !isPlaying },
        onNext = { currentFragmentIndex++ },
        onPrev = { if (currentFragmentIndex > 0) currentFragmentIndex-- },
        onStop = {
            isPlayerWindowOpen = false
            isPlaying = false
            AudioPlayer.stop()
        },
        onNavigateToPlayer = { id, mode ->
            selectedSongId = id
            selectedPlayMode = mode // устанавливаем новое значение mode
            currentFragmentIndex = 0
            isPlaying = true
            isPlayerWindowOpen = true
        }
    )

    // 2. ВТОРОЕ ОКНО (Для проектора)
    if (isPlayerWindowOpen) {
        val screenBounds = FileLogic.getSecondaryScreenBounds()

        val windowState = rememberWindowState(
            position = if (screenBounds != null) {
                WindowPosition(screenBounds.x.dp, screenBounds.y.dp)
            } else {
                WindowPosition(Alignment.Center)
            },
            size = if (screenBounds != null) {
                DpSize(screenBounds.width.dp, screenBounds.height.dp)
            } else {
                DpSize(1000.dp, 700.dp) // если второй экран не обнаружен то второе окно с таким размером
            }
        )

        // Скалирование шрифта в зависимости от размера окна
        val largeScaleFactor = if (windowState.size.width > 1200.dp) 1.5f else 1f
        val scaledFontSize = 24.sp * largeScaleFactor

        Window(
            onCloseRequest = {
                isPlayerWindowOpen = false
                isPlaying = false
                AudioPlayer.stop()
            },
            state = windowState,
            title = "Glory Music - Презентация",
            undecorated = screenBounds != null, // Убираем рамки если на втором экране
            alwaysOnTop = true
        ) {
            PlayerScreen(
                songId = selectedSongId,
                mode = selectedPlayMode, // передаем mode в PlayerScreen
                isPlaying = isPlaying,
                currentIndex = currentFragmentIndex,
                onIndexChange = { currentFragmentIndex = it },
                onPlaybackComplete = {
                    isPlayerWindowOpen = false
                    isPlaying = false
                    AudioPlayer.stop()
                }
            )
        }
    }
}