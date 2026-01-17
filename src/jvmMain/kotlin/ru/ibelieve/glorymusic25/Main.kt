package ru.ibelieve.glorymusic25

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

public fun main(): Unit = application {
    val state = rememberWindowState(width = 600.dp, height = 800.dp) // первоначальный размер окна

    Window(
        onCloseRequest = ::exitApplication,
        state = state,
        title = "Glory Music 26",
    ) {
        App()
    }
}