package ru.ibelieve.glorymusic25

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import com.google.accompanist.flowlayout.FlowRow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isPlayerActive: Boolean,
    isPlaying: Boolean,
    mode: PlayMode,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onStop: () -> Unit,
    onNavigateToPlayer: (Int, PlayMode) -> Unit
) {
    val allSongs = remember { FileLogic.loadSongs() }
    val favorites = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            FileLogic.loadFavorites().forEach { put(it, true) }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ВСЕ") }
    var selectedSongId by remember { mutableStateOf(-1) }
    var currentAvailability by remember { mutableStateOf(SongAvailability(false, false, false)) }

    val categories = listOf("МОЛИТВА", "ПОКЛОНЕНИЕ", "ВЕРУЮ", "РОЖДЕСТВО", "ПАСХА", "ЗАПОВЕДЬ", "ИЗБРАННОЕ", "ДЕТИ", "ВСЕ")
    val lazyListState = rememberLazyListState()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {

        // Верхний заголовок с вашей иконкой
        TopAppBar(title = {
            Row(
                horizontalArrangement = Arrangement.Center, // Центрируем содержимое по горизонтали
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth() // Заголовок занимает всю ширину
            ) {
                // Используем прямую загрузку иконки
                Image(
                    painter = painterResource(resourcePath = "icons/gm_icon.png"),
                    contentDescription = "Главная иконка"
                )
                Spacer(Modifier.width(8.dp))
                Text("БЛАГО ЕСТЬ СЛАВИТЬ ГОСПОДА!")
            }
        })

        // категории песнопений
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                //.border(BorderStroke(1.dp, Color.Green))
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Поиск песнопения...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        val displaySongs = allSongs.filter { song ->
            val matchesCategory = when (selectedCategory) {
                "ВСЕ" -> true
                "ИЗБРАННОЕ" -> favorites[song.id] == true
                else -> song.category.trim().equals(selectedCategory.trim(), ignoreCase = true)
            }
            val matchesSearch = song.title.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedBy { it.title }

        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
            LazyColumn(state = lazyListState, modifier = Modifier.fillMaxSize()) {
                items(displaySongs) { song ->
                    SongRow(
                        song = song,
                        isSelected = selectedSongId == song.id,
                        isFavorite = favorites[song.id] == true,
                        onSelect = {
                            selectedSongId = song.id
                            currentAvailability = FileLogic.checkSongAvailability(song.id)
                        },
                        onToggleFavorite = {
                            val newState = !(favorites[song.id] ?: false)
                            if (newState) favorites[song.id] = true else favorites.remove(song.id)
                            FileLogic.saveFavorites(favorites.keys.toSet())
                        }
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(lazyListState)
            )
        }

        // ПАНЕЛЬ УПРАВЛЕНИЯ (меняется при воспроизведении)
        Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isPlayerActive) {
                    // выбор режима воспроизведения
                    ModeButton("PLUS", currentAvailability.hasPlus) { onNavigateToPlayer(selectedSongId, PlayMode.PLUS) }
                    ModeButton("MINUS", currentAvailability.hasMinus) { onNavigateToPlayer(selectedSongId, PlayMode.MINUS) }
                    ModeButton("TEXT", currentAvailability.hasText) { onNavigateToPlayer(selectedSongId, PlayMode.TEXT) }
                } else {
                    // управление воспроизведением
                    if (mode == PlayMode.TEXT) {
                        // режим текста
                        Button(onClick = onPrev, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("Назад")
                        }
                        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("Вперед")
                        }
                        Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))) {
                            Text("Стоп")
                        }
                    } else {
                        // режим PLUS/MINUS
                        Button(onClick = onTogglePlay, modifier = Modifier.width(150.dp)) {
                            Text(if (isPlaying) "Пауза" else "Играть")
                        }
                        Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C))) {
                            Text("Стоп")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SongRow(song: Song, isSelected: Boolean, isFavorite: Boolean, onSelect: () -> Unit, onToggleFavorite: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp) // Расстояние между строками уменьшено на 50%
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(vertical = 1.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "${song.id}", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold)
        Text(text = song.title, modifier = Modifier.weight(1f), fontSize = 20.sp)
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) Color.Red else Color.Gray
            )
        }
    }
}

@Composable
fun ModeButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled && label != "",
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label)
    }
}