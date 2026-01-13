package ru.ibelieve.glorymusic25

import javax.sound.sampled.*
import java.io.File
import kotlin.concurrent.thread

object AudioPlayer {
    private var clip: Clip? = null
    private var lastFramePosition: Long = 0L // позиция кадра, сохранённая при паузе
    private var isPaused = false

    /**
     * Воспроизводит аудиофайл по указанному пути.
     *
     * @param filePath Путь к аудиофайлу.
     * @param startFromBeginning Если true, начнется воспроизведение с начала, иначе — с последней позиции.
     */
    fun play(filePath: String?, startFromBeginning: Boolean = false) {
        if (filePath == null || !File(filePath).exists()) return

        // Если нужно стартовать с начала, обнуляем позицию
        if (startFromBeginning) {
            lastFramePosition = 0L
        }

        thread(start = true, isDaemon = true) {
            try {
                if (clip == null) {
                    // Создание нового клипа и настройка его
                    val audioInputStream = AudioSystem.getAudioInputStream(File(filePath))
                    val format = audioInputStream.format

                    // Декодируем формат, если это MP3
                    val decodedFormat = if (format.encoding == AudioFormat.Encoding.PCM_SIGNED) {
                        format
                    } else {
                        AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            format.sampleRate,
                            16,
                            format.channels,
                            format.channels * 2,
                            format.sampleRate,
                            false
                        )
                    }

                    val decodedInputStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)

                    clip = AudioSystem.getClip()
                    clip?.open(decodedInputStream)
                }

                // Продолжаем с последней позиции
                if (lastFramePosition > 0) {
                    clip?.framePosition = lastFramePosition.toInt()
                }

                clip?.start()
            } catch (e: Exception) {
                println("Ошибка воспроизведения: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Ставит воспроизведение на паузу.
     */
    fun pause() {
        if (clip?.isRunning == true) {
            isPaused = true
            lastFramePosition = clip!!.framePosition.toLong() // запоминаем последнюю позицию
            clip?.stop()
        }
    }

    /**
     * Возобновляет воспроизведение.
     */
    fun resume() {
        if (isPaused && clip != null) {
            isPaused = false
            clip?.start()
        }
    }

    /**
     * Остановка воспроизведения и освобождение ресурсов.
     */
    fun stop() {
        clip?.let {
            it.stop()
            it.close()
            clip = null
            lastFramePosition = 0L // очищаем позицию
        }
    }
}